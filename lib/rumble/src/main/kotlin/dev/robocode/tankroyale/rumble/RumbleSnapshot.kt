package dev.robocode.tankroyale.rumble

import java.io.IOException

/**
 * The Rumble's catalog and per-bot rules read at one commit of its bot repository. Either is null when it could not be
 * read, and [problem] then says why in plain words. A part that cannot be read never stops anything: the bot repository
 * checks the same things itself.
 */
class RumbleSnapshot(val catalog: Catalog?, val rules: RumbleRules?, val problem: String?) {
    companion object {
        /**
         * Reads the snapshot. The commit is looked up with the signed-in [token] when there is one, because GitHub allows
         * only a few requests an hour without it; when GitHub refuses that, for example because the token has just run
         * out, it is looked up once more without it.
         */
        fun read(api: GitHubApi, environment: RumbleEnvironment, source: CatalogSource, token: String? = null): RumbleSnapshot {
            val commit = try {
                headCommit(api, environment, token)
            } catch (e: IOException) {
                return RumbleSnapshot(null, null, e.message.orEmpty())
            }
            var problem: String? = null
            val catalog = orNull({ problem = problem ?: it }) { Catalog.parse(source.catalogText(commit)) }
            val rules = orNull({ problem = problem ?: it }) { RumbleRules.read(source, commit) }
            return RumbleSnapshot(catalog, rules, problem)
        }

        private fun headCommit(api: GitHubApi, environment: RumbleEnvironment, token: String?): String =
            try {
                RumbleHead.commit(api, environment, token)
            } catch (e: IOException) {
                if (token == null) throw e
                RumbleHead.commit(api, environment)
            }

        /** The result of [read], or null when it cannot be read; [failed] is told why. */
        private inline fun <T> orNull(failed: (String) -> Unit, read: () -> T): T? =
            try {
                read()
            } catch (e: CatalogUnreadable) {
                failed(e.message.orEmpty())
                null
            } catch (e: IOException) {
                failed(e.message.orEmpty())
                null
            }
    }
}
