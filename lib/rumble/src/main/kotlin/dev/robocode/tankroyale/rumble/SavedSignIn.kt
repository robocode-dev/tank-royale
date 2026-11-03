package dev.robocode.tankroyale.rumble

/** The result of renewing a saved sign-in. */
sealed interface SavedRenewal {
    /** [kept] is false when the new sign-in could not be saved, so the next run must sign in again. */
    data class Renewed(val tokens: GitHubTokens, val kept: Boolean = true) : SavedRenewal

    /** There is no usable saved sign-in, or GitHub refused it; the person must sign in with a code. */
    data class SignInNeeded(val message: String, val reason: Reason = Reason.MISSING) : SavedRenewal {
        enum class Reason { MISSING, UNUSABLE, REFUSED }
    }

    /** The renewal could not be tried or answered; trying again later may work. */
    data class Failed(val message: String) : SavedRenewal
}

/**
 * Keeps a sign-in alive between runs and between programs. A refresh token can be used only once, because GitHub
 * replaces it at every renewal. Renewing therefore takes the lock on the saved file, reads the current refresh token
 * from the file, and saves the new one before the new sign-in is used, so programs that share the file never use a
 * refresh token that another program has replaced.
 */
class SavedSignIn(
    private val store: SignInStore,
    private val signIn: GitHubSignIn,
    /** When given, signing out removes the saved registration too. */
    private val registration: RegistrationStore? = null,
) {

    /** Remembers a fresh sign-in so later runs can renew it. A sign-in without a refresh token is not saved. */
    fun remember(tokens: GitHubTokens): SaveResult = store.locked {
        val refresh = tokens.refreshToken
        if (refresh == null) SaveResult.NotSaved("GitHub gave no refresh token, so this sign-in cannot be saved.")
        else store.save(refresh, tokens.refreshTokenExpiresAt)
    }

    /** Renews the saved sign-in and returns the new tokens. */
    fun renew(): SavedRenewal {
        // Nothing to renew needs no lock, and taking one would leave a lock file where nothing was saved.
        if (store.knownMissing()) return SavedRenewal.SignInNeeded("There is no saved sign-in. Sign in with a code.")
        return store.locked { renewLocked() }
    }

    private fun renewLocked(): SavedRenewal =
        when (val loaded = store.load()) {
            is LoadResult.Found -> renewWith(loaded)
            LoadResult.Missing -> SavedRenewal.SignInNeeded("There is no saved sign-in. Sign in with a code.")
            LoadResult.Disabled -> SavedRenewal.SignInNeeded("Saving the sign-in is switched off. Sign in with a code.")
            is LoadResult.Unusable -> SavedRenewal.SignInNeeded(loaded.reason, SavedRenewal.SignInNeeded.Reason.UNUSABLE)
        }

    /** Signs out: removes the saved sign-in and, when one is kept, the saved registration. */
    fun forget() = store.locked {
        store.forget()
        registration?.forget()
    }

    private fun renewWith(saved: LoadResult.Found): SavedRenewal =
        when (val result = signIn.refresh(GitHubTokens("", saved.refreshToken, null, saved.refreshTokenExpiresAt))) {
            is RefreshResult.Renewed -> {
                val newRefresh = result.tokens.refreshToken
                val kept = newRefresh != null && store.save(newRefresh, result.tokens.refreshTokenExpiresAt) == SaveResult.Saved
                // The old refresh token no longer works at GitHub, so an old file must not be left behind.
                if (!kept) store.forget()
                SavedRenewal.Renewed(result.tokens, kept)
            }
            is RefreshResult.Refused -> {
                store.forget()
                SavedRenewal.SignInNeeded(result.message, SavedRenewal.SignInNeeded.Reason.REFUSED)
            }
            is RefreshResult.Failed -> SavedRenewal.Failed(result.message)
        }
}
