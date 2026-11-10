package dev.robocode.tankroyale.rumble

/**
 * The keys of the texts the Download bots screen shows. The texts are in the desktop program's resource files; this
 * object is the one list of which keys the states, the results, the failures and the licenses need, so a test can check
 * that every one of them has a text. Nothing here is a sentence.
 */
object DownloadTexts {
    fun forState(state: DownloadedState): String = "rumble.download.state." + state.name.lowercase()

    fun forFailure(failure: DownloadFailure): String = "rumble.download.failure." + failure.name.lowercase()

    fun forResult(result: DownloadResult): String = when (result) {
        is DownloadResult.Placed -> if (result.updated) "rumble.download.result.updated" else "rumble.download.result.placed"
        is DownloadResult.UpToDate -> "rumble.download.result.up_to_date"
        is DownloadResult.HashDiffers -> "rumble.download.result.hash_differs"
        is DownloadResult.NotInArchive -> "rumble.download.result.not_in_archive"
        is DownloadResult.KeptOwn -> "rumble.download.result.kept_own"
        is DownloadResult.KeptChanged -> "rumble.download.result.kept_changed"
        is DownloadResult.NotPlaced -> "rumble.download.result.not_placed"
        is DownloadResult.KeptAside -> "rumble.download.result.kept_aside"
    }

    fun toneOf(result: DownloadResult): Tone = when (result) {
        is DownloadResult.Placed -> Tone.GOOD
        is DownloadResult.HashDiffers, is DownloadResult.NotInArchive, is DownloadResult.NotPlaced, is DownloadResult.KeptAside -> Tone.BAD
        is DownloadResult.UpToDate, is DownloadResult.KeptOwn, is DownloadResult.KeptChanged -> Tone.NEUTRAL
    }

    /** The few words beside a platform this computer cannot run. */
    fun forPlatformShort(reason: PlatformReason): String = "rumble.download.platform.short." + reason.name.lowercase()

    /**
     * What the dialog says about a platform this computer cannot run. A missing tool is named with the version the
     * Rumble supports, unless the Rumble's list could not be read.
     */
    fun forPlatformExplanation(reason: PlatformReason, versionKnown: Boolean = true): String =
        "rumble.download.platform.explain." + reason.name.lowercase() +
            if (!versionKnown && reason in WITH_VERSION) "_no_version" else ""

    private val WITH_VERSION = setOf(PlatformReason.MISSING, PlatformReason.NPM_MISSING)

    /** The name of the tool a platform's bots need. */
    fun forPlatformTool(platform: BotPlatform): String = "rumble.download.platform.tool." + platform.name.lowercase()

    fun forSetupFailure(step: PythonSetupStep): String = "rumble.download.setup.failed." + step.name.lowercase()

    /** Every key about the platforms and the setup of Python. */
    val ALL_PLATFORM_KEYS: List<String> =
        PlatformReason.entries.filter { it != PlatformReason.READY }.flatMap { listOf(forPlatformShort(it), forPlatformExplanation(it)) } +
            BotPlatform.entries.map { forPlatformTool(it) } + PythonSetupStep.entries.map { forSetupFailure(it) } + listOf(
                "rumble.download.platform.label", "rumble.download.platform.versions_unknown", "rumble.download.platform.venv_package",
                "rumble.download.platform.commands", "rumble.download.setup.running", "rumble.download.setup.done",
                "rumble.download.button.set_up", "rumble.download.button.show_anyway", "rumble.download.button.download_page",
                "rumble.download.setup.left_behind", "rumble.download.setup.nothing_left",
            ) + WITH_VERSION.map { forPlatformExplanation(it, versionKnown = false) }

    /** The parts of the short explanation of a license, in the order the screen shows them. */
    val LICENSE_PARTS: List<String> = listOf("author", "may", "must", "note")

    fun forLicense(license: RumbleLicense, part: String): String =
        "rumble.download.license." + license.id.lowercase().replace(Regex("[^a-z0-9]"), "_") + "." + part

    /** Every key of the short explanations of the licenses the Rumble allows. */
    val ALL_LICENSE_KEYS: List<String> = RumbleLicenses.ALL.flatMap { license -> LICENSE_PARTS.map { forLicense(license, it) } }

    /** The keys that are not about a state, a result, a failure or one license. */
    val OTHER: List<String> = listOf(
        "rumble.download.title", "rumble.download.reading", "rumble.download.intro", "rumble.download.warning", "rumble.download.folder",
        "rumble.download.which", "rumble.download.all", "rumble.download.selected", "rumble.download.platforms", "rumble.download.row",
        "rumble.download.row_license", "rumble.download.row_here", "rumble.download.replace_changed", "rumble.download.none", "rumble.download.downloading",
        "rumble.download.ended", "rumble.download.result_line", "rumble.download.license_of", "rumble.download.no_license",
        "rumble.download.add_folder_hint", "rumble.download.folder_added", "rumble.download.folder_in_new_battle", "rumble.download.kept_at", "rumble.download.details", "rumble.download.status_success", "rumble.download.status_failed",
        "rumble.download.unknown", "rumble.download.licenses", "rumble.download.licenses_intro", "rumble.download.licenses_label",
        "rumble.download.for_author", "rumble.download.for_user", "rumble.download.may", "rumble.download.must", "rumble.download.note",
        "rumble.download.author_later", "rumble.download.credit",
        "rumble.download.button.change_folder", "rumble.download.button.download", "rumble.download.button.show_folder",
        "rumble.download.button.back", "rumble.download.button.try_again", "rumble.download.button.close",
        "rumble.download.button.official", "rumble.download.button.explanation",
    )

    private val bot = CatalogBot("", "", "", "x/y", "", "", emptyList(), "", CatalogBot.ACTIVE)

    /** Every way the download of one bot can end. */
    val ALL_RESULTS: List<DownloadResult> = listOf(
        DownloadResult.Placed(bot, updated = false, license = null), DownloadResult.Placed(bot, updated = true, license = null),
        DownloadResult.UpToDate(bot), DownloadResult.HashDiffers(bot), DownloadResult.NotInArchive(bot),
        DownloadResult.KeptOwn(bot), DownloadResult.KeptChanged(bot), DownloadResult.NotPlaced(bot, ""), DownloadResult.KeptAside(bot, java.nio.file.Path.of("kept")),
    )
}
