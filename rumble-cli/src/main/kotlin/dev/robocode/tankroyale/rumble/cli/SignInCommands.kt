package dev.robocode.tankroyale.rumble.cli

import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.long
import com.github.ajalt.clikt.parameters.types.restrictTo
import dev.robocode.tankroyale.rumble.GitHubSignIn
import dev.robocode.tankroyale.rumble.LoadResult
import dev.robocode.tankroyale.rumble.Registration
import dev.robocode.tankroyale.rumble.RegistrationResult
import dev.robocode.tankroyale.rumble.RumbleEnvironment
import dev.robocode.tankroyale.rumble.SavedRenewal
import dev.robocode.tankroyale.rumble.SignInFinish
import dev.robocode.tankroyale.rumble.SignInFlow
import dev.robocode.tankroyale.rumble.SignInStart
import dev.robocode.tankroyale.rumble.SignInStore
import dev.robocode.tankroyale.rumble.WaitingSignIn
import dev.robocode.tankroyale.rumble.signInNotAvailable
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** What the commands that sign in, sign out and register have in common: the shape of their answer. */
internal abstract class SignInLeaf(name: String, io: Io, protected val services: Services) : RumbleCli.EnvironmentLeaf(name, io) {

    protected fun flow(env: RumbleEnvironment): SignInFlow {
        val base = base()
        return SignInFlow(
            env,
            GitHubSignIn(env, services.auth, services.clock),
            SignInStore(env, base),
            services.api,
            services.clock,
            services.sleep,
        )
    }

    /** Builds an answer with an outcome word, the environment, a message, and the next step when there is one. */
    protected fun reply(
        env: RumbleEnvironment,
        code: Int,
        outcome: String,
        message: String,
        next: String? = null,
        keep: Set<String> = emptySet(),
        extra: JsonObjectBuilder.() -> Unit = {},
    ): Reply {
        val json = head(commandName) {
            put("environment", env.name)
            put("outcome", outcome)
            put("message", message)
            extra()
            next?.let { step -> putNext(step) }
        }
        val text = buildString {
            appendLine("Environment: ${env.displayName} (${env.name})")
            append(message)
            next?.let { appendLine(); append(nextLine(it)) }
        }
        return Reply(code, json, text, keep + exactValuesIn(json) + env.displayName)
    }
}

internal class LoginCommand(io: Io, services: Services) : SignInLeaf("login", io, services) {
    override fun help(context: Context) =
        "Signs in with your GitHub account. Shows a code and an address: open the address and type the code yourself, " +
            "then run login --finish. With --wait and no --finish it does both in one call, in text only."

    private val finish by option("--finish", help = "Check whether the code has been approved.").flag()
    private val wait by option("--wait", help = "Keep checking for this many seconds, at most an hour.").long().restrictTo(0, MAX_WAIT_SECONDS)

    override fun run() {
        val env = environment()
        if (wait != null && !finish && json) {
            throw BadInput("login --wait without --finish shows the code before it waits, which one JSON document cannot do. Run login, then login --finish --wait.")
        }
        val flow = flow(env)
        if (finish) {
            answer(finished(env, flow.finish(wait ?: 0)))
        } else {
            val start = flow.start()
            val startReply = started(env, start)
            val seconds = wait
            if (seconds == null || start !is SignInStart.Started) {
                answer(startReply)
            } else {
                io.say(startReply)
                io.say("Waiting up to $seconds seconds for you to approve it.")
                answer(finished(env, flow.finish(seconds)))
            }
        }
    }

    private fun started(env: RumbleEnvironment, start: SignInStart): Reply = when (start) {
        is SignInStart.Started -> {
            val code = start.code
            reply(
                env, ExitCodes.DONE, "started",
                "Open ${code.verificationUri} and type the code ${code.userCode}. It runs out at ${code.expiresAt}.",
                next = "login --finish", keep = setOf(code.verificationUri),
            ) { describe(code); put("reused", start.reused) }
        }
        is SignInStart.AlreadySignedIn -> reply(
            env, ExitCodes.DONE, "already-signed-in",
            start.account?.let { "Already signed in as $it. To change account, run logout first." } ?: "Already signed in, but GitHub would not say as which account. Try login again.",
            keep = setOfNotNull(start.account),
        ) { put("account", start.account) }
        is SignInStart.Unavailable -> reply(env, ExitCodes.REFUSED, "no-sign-in-app", start.message)
        is SignInStart.NotPrivate -> reply(env, ExitCodes.REFUSED, "not-private", start.message)
        is SignInStart.Failed -> reply(env, ExitCodes.FAILED, "failed", start.message, next = "login")
    }

    private fun finished(env: RumbleEnvironment, finish: SignInFinish): Reply = when (finish) {
        is SignInFinish.SignedIn -> reply(
            env, ExitCodes.DONE, "signed-in",
            finish.account?.let { "Signed in as $it." } ?: "Signed in. Which account is not known yet; the next login or register finds out.",
            next = "register", keep = setOfNotNull(finish.account),
        ) { put("account", finish.account) }
        is SignInFinish.Waiting -> reply(
            env, ExitCodes.NOT_FINISHED, "waiting",
            "No approval seen yet. Type the code ${finish.code.userCode} at ${finish.code.verificationUri}, and check again in ${finish.retryAfterSeconds} seconds.",
            next = "login --finish", keep = setOf(finish.code.verificationUri),
        ) { describe(finish.code); put("retryAfterSeconds", finish.retryAfterSeconds) }
        is SignInFinish.NotSaved -> reply(env, ExitCodes.FAILED, "not-saved", "GitHub approved the sign-in, but it could not be kept. ${finish.reason}", next = "login")
        SignInFinish.NoWaitingCode -> reply(env, ExitCodes.SIGN_IN_NEEDED, "no-waiting-code", "No code is waiting. Ask for one first.", next = "login")
        SignInFinish.CodeExpired -> reply(env, ExitCodes.SIGN_IN_NEEDED, "code-expired", "The code ran out of time. Ask for a new one.", next = "login")
        SignInFinish.CodeDenied -> reply(env, ExitCodes.SIGN_IN_NEEDED, "code-denied", "The code was refused on GitHub's page. Ask for a new one to try again.", next = "login")
        is SignInFinish.Unavailable -> reply(env, ExitCodes.REFUSED, "no-sign-in-app", finish.message)
        is SignInFinish.Failed -> reply(env, ExitCodes.FAILED, "failed", finish.message, next = if (finish.codeStillWaiting) "login --finish" else "login") {
            if (finish.codeStillWaiting) put("retryAfterSeconds", finish.retryAfterSeconds)
        }
    }

    private fun JsonObjectBuilder.describe(code: WaitingSignIn) {
        put("userCode", code.userCode)
        put("verificationUri", code.verificationUri)
        put("expiresAt", code.expiresAt.toString())
    }

    private companion object {
        /** A code never lives this long, so a longer wait could only end the same way. */
        const val MAX_WAIT_SECONDS = 3600L
    }
}

internal class LogoutCommand(io: Io, services: Services) : SignInLeaf("logout", io, services) {
    override fun help(context: Context) =
        "Removes the saved sign-in, the waiting code, the account name and the registration for one environment. Access at GitHub stops only when you revoke the App in your GitHub settings."

    override fun run() {
        val env = environment()
        val result = flow(env).signOut()
        val reply = if (result.removed) {
            reply(
                env, ExitCodes.DONE, "signed-out",
                (if (result.removedSomething) "Signed out on this computer." else "Nothing was saved, so nothing was removed.") + " $REVOKE_NOTE",
            ) { put("removedSomething", result.removedSomething) }
        } else {
            reply(env, ExitCodes.FAILED, "not-removed", "The saved sign-in could not be removed. Close other programs that use it and run logout again.", next = "logout")
        }
        answer(reply)
    }

    private companion object {
        const val REVOKE_NOTE = "This only removes the sign-in from this computer. Access at GitHub stops when you revoke the App in your GitHub settings."
    }
}

internal class RegisterCommand(io: Io, services: Services) : SignInLeaf("register", io, services) {
    override fun help(context: Context) =
        "Registers the signed-in GitHub account as a Rumble participant and saves its client ID. Safe to run again."

    private val retry by option("--retry", help = "Open a new registration issue after an earlier refusal, once you have fixed the reason.").flag()

    override fun run() {
        val env = environment()
        if (env.signInClientId == null) {
            answer(reply(env, ExitCodes.REFUSED, "no-sign-in-app", env.signInNotAvailable()))
            return
        }
        val flow = flow(env)
        when (val renewal = flow.renewSaved()) {
            is SavedRenewal.SignInNeeded -> answer(
                reply(env, ExitCodes.SIGN_IN_NEEDED, when (renewal.reason) {
                    SavedRenewal.SignInNeeded.Reason.MISSING -> "no-sign-in"
                    SavedRenewal.SignInNeeded.Reason.UNUSABLE -> "sign-in-unusable"
                    SavedRenewal.SignInNeeded.Reason.REFUSED -> "sign-in-refused"
                }, renewal.message, next = "login"),
            )
            is SavedRenewal.Failed -> answer(reply(env, ExitCodes.FAILED, "failed", renewal.message, next = "register"))
            is SavedRenewal.Renewed -> {
                // The promise is that the next register fills in a name that was missing, however this one ends.
                if (renewal.account == null) flow.identify(renewal)
                val result = Registration(env, services.api, flow.registrationMemory(renewal), services.clock, services.sleep, services.registrationPollSeconds, services.registrationWaitSeconds)
                    .register(renewal.tokens, retry)
                answer(registered(env, flow, result, renewal))
            }
        }
    }

    private fun registered(env: RumbleEnvironment, flow: SignInFlow, result: RegistrationResult, renewal: SavedRenewal.Renewed): Reply {
        val signInKept = renewal.kept
        val lost = if (signInKept) "" else " The sign-in could not be kept on this computer, so sign in again before the next command."
        val afterwards = if (signInKept) null else "login"
        return when (result) {
            is RegistrationResult.Registered -> {
                if (signInKept && !flow.stillSignedIn(renewal)) {
                    return reply(
                        env, ExitCodes.NOT_FINISHED, "sign-in-changed",
                        "${result.account} is registered, but the sign-in changed while it ran, so nothing was saved for it on this computer. Run register again.",
                        next = "register", keep = setOf(result.account),
                    ) { put("account", result.account); put("saved", false) }
                }
                val notSaved = if (result.saved) "" else " It could not be remembered on this computer, so it will be read from the Rumble next time."
                reply(
                    env, ExitCodes.DONE, if (result.alreadyRegistered) "already-registered" else "registered",
                    (if (result.alreadyRegistered) "${result.account} is already registered" else "${result.account} is now registered") + " with client ID ${result.clientId}.$notSaved$lost",
                    next = afterwards, keep = setOf(result.account),
                ) {
                    put("account", result.account)
                    put("clientId", result.clientId)
                    put("alreadyRegistered", result.alreadyRegistered)
                    put("saved", result.saved)
                    put("signInKept", signInKept)
                }
            }
            is RegistrationResult.Refused -> reply(
                env, ExitCodes.REFUSED, "refused",
                "The Rumble refused the registration: ${result.reason}" + (if (result.earlier) " No new issue was opened. When you have fixed the reason, run register again with --retry." else "") + lost,
                next = afterwards, keep = setOfNotNull(result.account),
            ) { put("reason", result.reason); put("earlier", result.earlier) }
            is RegistrationResult.NotFinished -> reply(
                env, ExitCodes.NOT_FINISHED, "registration-pending",
                "The registration is not finished yet." + (result.issueUrl?.let { " You can follow it at $it." } ?: "") + lost,
                next = afterwards ?: "register", keep = setOfNotNull(result.issueUrl),
            ) { result.issueUrl?.let { put("issueUrl", it) } }
            is RegistrationResult.SignInNeeded -> reply(env, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", result.message, next = "login")
            is RegistrationResult.Failed -> reply(env, ExitCodes.FAILED, "failed", result.message + lost, next = afterwards ?: "register")
        }
    }
}

/** What status says about a saved sign-in, a waiting code and a registration, read without contacting anybody. */
internal class StatusFacts(env: RumbleEnvironment, base: java.nio.file.Path, services: Services) {
    private val store = SignInStore(env, base)
    val signIn: LoadResult = store.load()
    val signedIn = signIn is LoadResult.Found
    val account: String? = (signIn as? LoadResult.Found)?.account
    val waiting: WaitingSignIn? = store.loadWaiting()?.takeIf { services.clock.instant().isBefore(it.expiresAt) }
    val registration = (signIn as? LoadResult.Found)?.registration

    val ready = signedIn && account != null && registration != null

    /** The next command, or null when all is ready or no command can help. */
    val next: String? = when {
        env.signInClientId == null -> null
        waiting != null && !signedIn -> "login --finish"
        !signedIn || account == null -> "login"
        registration == null -> "register"
        else -> null
    }
}
