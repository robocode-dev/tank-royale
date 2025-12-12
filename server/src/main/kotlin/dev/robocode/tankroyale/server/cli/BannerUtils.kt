package dev.robocode.tankroyale.server.cli

private const val RESET_ANSI = "\u001B[0m"
private val PICOCli_TAG = Regex("@\\|([^ ]+) (.*?)\\|@")

internal fun convertPicocliMarkupToAnsi(s: String): String {
    val withAnsi = PICOCli_TAG.replace(s) { matchResult ->
        val attrs = matchResult.groupValues[1].split(",")
        val text = matchResult.groupValues[2]
        val codes = attrs.mapNotNull { raw ->
            when (val attr = raw.trim()) {
                in listOf("bold", "BOLD") -> "1"
                in listOf("green", "GREEN") -> "32"
                in listOf("red", "RED") -> "31"
                in listOf("blue", "BLUE") -> "34"
                else -> when {
                    attr.startsWith("fg(", ignoreCase = true) -> parseFg(attr)
                    else -> null
                }
            }
        }
        val start = if (codes.isNotEmpty()) codes.joinToString("") { "\u001B[${it}m" } else ""
        "$start${text}$RESET_ANSI"
    }
    return withAnsi.replace("@@", "@")
}

private fun parseFg(attr: String): String? {
    val inside = attr.substringAfter('(').substringBeforeLast(')')
    val parts = inside.split(';')
    if (parts.size == 3) {
        val (r, g, b) = parts.map { it.toIntOrNull() }
        if (r != null && g != null && b != null) {
            return if (r in 0..5 && g in 0..5 && b in 0..5) {
                val idx = 16 + 36 * r + 6 * g + b
                "38;5;$idx"
            } else {
                "38;2;${r.coerceIn(0, 255)};${g.coerceIn(0, 255)};${b.coerceIn(0, 255)}"
            }
        }
    }
    return when (inside.lowercase()) {
        "green", "0;1;0" -> "32"
        "red" -> "31"
        "blue" -> "34"
        else -> null
    }
}

internal val SERVER_BANNER_LINES = listOf(
    "@|bold,fg(0;0;5)               ___________|@",
    "@|bold,fg(0;0;5)              /           ||@@|bold,fg(2;2;2) [[[]========((()|@",
    "@|bold,fg(0;0;5)    _________|____________|___________|@",
    "@|bold,fg(2;2;2)  _|@@|bold,fg(0;0;5) /|@@|bold,fg(2;2;2) _|@@|bold,fg(0;0;5) ________________________________|@@|bold,fg(2;2;2) _|@@|bold,fg(0;0;5) \\|@@|bold,fg(2;2;2) _|@",
    "@|bold,fg(2;2;2) / _ ) ___  ___  ___  ___  ___  ___ / ,_||@",
    "@|bold,fg(2;2;2) \\_\\_\\/ _ \\| __)/ _ \\/ __// _ \\| _ \\\\__||@",
    "@|bold,fg(2;2;2)      \\___/|___)\\___/\\___|\\___/|___/|@",
    "",
    "@|bold,green           Robocode Tank Royale|@",
    ""
)

