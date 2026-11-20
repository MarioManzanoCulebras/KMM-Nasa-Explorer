package com.mariomanzano.kmm_nasa_explorer.network.entities

private val whitespace = Regex("[\\s\u00A0]+")
private val lineBreakTag = Regex("<br\\s*/?>|</p\\s*>", RegexOption.IGNORE_CASE)
private val anyTag = Regex("<[^>]*>")
private val entity = Regex("&(#[0-9]{1,7}|#[xX][0-9a-fA-F]{1,6}|[a-zA-Z]{2,8});")
private val spaces = Regex("[ \u00A0]+")
private val blankLines = Regex("\\n{3,}")

private val explanationLabel = Regex("^(?:Explanation\\s*:\\s*)+", RegexOption.IGNORE_CASE)
private val tomorrowTeaser = Regex("\\s*Tomorrow['\u2019]s picture\\s*:[^\\n]*$", RegexOption.IGNORE_CASE)

private val namedEntities = mapOf(
    "amp" to "&",
    "lt" to "<",
    "gt" to ">",
    "quot" to "\"",
    "apos" to "'",
    "nbsp" to " ",
    "lsquo" to "\u2018",
    "rsquo" to "\u2019",
    "ldquo" to "\u201C",
    "rdquo" to "\u201D",
    "ndash" to "\u2013",
    "mdash" to "\u2014",
    "hellip" to "\u2026"
)

/**
 * Turns an HTML fragment into the text a reader would see: tags are dropped, `<br>` and `</p>`
 * become line breaks and character entities are decoded.
 */
internal fun String.htmlToPlainText(): String =
    replace(whitespace, " ")
        .replace(lineBreakTag, "\n")
        .replace(anyTag, "")
        .replace(entity) { decodeEntity(it.groupValues[1]) ?: it.value }
        .replace(spaces, " ")
        .split('\n').joinToString("\n") { it.trim() }
        .replace(blankLines, "\n\n")
        .trim()

/**
 * The APOD explanation comes as `<strong>Explanation:</strong> text ... <br><br><strong>Tomorrow's
 * picture:</strong> teaser`. Only the text in the middle describes the picture.
 */
internal fun String.apodExplanationToPlainText(): String =
    htmlToPlainText()
        .replace(tomorrowTeaser, "")
        .replace(explanationLabel, "")
        .trim()

private fun decodeEntity(name: String): String? = when {
    name.startsWith("#x") || name.startsWith("#X") -> name.drop(2).toIntOrNull(16)?.let(::codePointToString)
    name.startsWith("#") -> name.drop(1).toIntOrNull()?.let(::codePointToString)
    else -> namedEntities[name]
}

private fun codePointToString(codePoint: Int): String? = when (codePoint) {
    0x09, 0x0A, 0x0D, in 0x20..0xD7FF, in 0xE000..0xFFFF -> codePoint.toChar().toString()
    in 0x10000..0x10FFFF -> {
        val offset = codePoint - 0x10000
        charArrayOf(
            (0xD800 + (offset shr 10)).toChar(),
            (0xDC00 + (offset and 0x3FF)).toChar()
        ).concatToString()
    }
    else -> null
}
