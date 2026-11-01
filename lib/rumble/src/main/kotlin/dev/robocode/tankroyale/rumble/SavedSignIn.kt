package dev.robocode.tankroyale.rumble

/** The result of renewing a saved sign-in. */
sealed interface SavedRenewal {
    data class Renewed(val tokens: GitHubTokens) : SavedRenewal

    /** There is no usable saved sign-in, or GitHub refused it; the person must sign in with a code. */
    data class SignInNeeded(val message: String) : SavedRenewal

    /** The renewal could not be tried or answered; trying again later may work. */
    data class Failed(val message: String) : SavedRenewal
}

/**
 * Keeps a sign-in alive between runs and between programs. A refresh token can be used only once, because GitHub
 * replaces it at every renewal. Renewing therefore takes the lock on the saved file, reads the current refresh token
 * from the file, and saves the new one before the new sign-in is used, so programs that share the file never use a
 * refresh token that another program has replaced.
 */
class SavedSignIn(private val store: SignInStore, private val signIn: GitHubSignIn) {

    /** Remembers a fresh sign-in so later runs can renew it. A sign-in without a refresh token is not saved. */
    fun remember(tokens: GitHubTokens): SaveResult = store.locked {
        val refresh = tokens.refreshToken
        if (refresh == null) SaveResult.NotSaved("GitHub gave no refresh token, so this sign-in cannot be saved.")
        else store.save(refresh, tokens.refreshTokenExpiresAt)
    }

    /** Renews the saved sign-in and returns the new tokens. */
    fun renew(): SavedRenewal = store.locked {
        when (val loaded = store.load()) {
            is LoadResult.Found -> renewWith(loaded)
            LoadResult.Missing -> SavedRenewal.SignInNeeded("There is no saved sign-in. Sign in with a code.")
            LoadResult.Disabled -> SavedRenewal.SignInNeeded("Saving the sign-in is switched off. Sign in with a code.")
            is LoadResult.Unusable -> SavedRenewal.SignInNeeded(loaded.reason)
        }
    }

    /** Signs out: removes the saved sign-in. */
    fun forget() = store.locked { store.forget() }

    private fun renewWith(saved: LoadResult.Found): SavedRenewal =
        when (val result = signIn.refresh(GitHubTokens("", saved.refreshToken, null, saved.refreshTokenExpiresAt))) {
            is RefreshResult.Renewed -> {
                val newRefresh = result.tokens.refreshToken
                val kept = newRefresh != null && store.save(newRefresh, result.tokens.refreshTokenExpiresAt) == SaveResult.Saved
                // The old refresh token no longer works at GitHub, so an old file must not be left behind.
                if (!kept) store.forget()
                SavedRenewal.Renewed(result.tokens)
            }
            is RefreshResult.Refused -> {
                store.forget()
                SavedRenewal.SignInNeeded(result.message)
            }
            is RefreshResult.Failed -> SavedRenewal.Failed(result.message)
        }
}
