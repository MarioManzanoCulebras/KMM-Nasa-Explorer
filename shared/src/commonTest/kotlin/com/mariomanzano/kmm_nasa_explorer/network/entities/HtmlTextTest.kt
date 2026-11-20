package com.mariomanzano.kmm_nasa_explorer.network.entities

import kotlin.test.Test
import kotlin.test.assertEquals

class HtmlTextTest {

    // region htmlToPlainText

    @Test
    fun plainTextIsLeftUntouched() {
        assertEquals("Saturn Behind the Moon", "Saturn Behind the Moon".htmlToPlainText())
    }

    @Test
    fun tagsAreRemovedKeepingTheirText() {
        val html = """The <a href="https://example.test/a?b=1">featured <em>image</em></a> is new."""

        assertEquals("The featured image is new.", html.htmlToPlainText())
    }

    @Test
    fun lineBreakTagsBecomeLineBreaks() {
        assertEquals("one\ntwo\nthree\nfour", "one<br>two<br/>three<BR />four".htmlToPlainText())
    }

    @Test
    fun paragraphsAreSeparatedByALineBreak() {
        assertEquals("first\nsecond", "<p>first</p><p>second</p>".htmlToPlainText())
    }

    @Test
    fun moreThanOneBlankLineIsCollapsed() {
        assertEquals("one\n\ntwo", "one<br><br><br><br>two".htmlToPlainText())
    }

    @Test
    fun whitespaceOfTheSourceIsCollapsedIntoSingleSpaces() {
        assertEquals("one two three", "  one \n\t two  three  ".htmlToPlainText())
    }

    @Test
    fun spacesAroundLineBreaksAreTrimmed() {
        assertEquals("one\ntwo", "one <br> two".htmlToPlainText())
    }

    @Test
    fun namedEntitiesAreDecoded() {
        assertEquals(
            "Tom & Jerry <3 \"quotes\" 'apostrophes' and spaces",
            "Tom &amp; Jerry &lt;3 &quot;quotes&quot; &apos;apostrophes&apos; and&nbsp;spaces"
                .htmlToPlainText()
        )
    }

    @Test
    fun typographicEntitiesAreDecoded() {
        assertEquals(
            "“Jupiter’s moons” – a story…",
            "&ldquo;Jupiter&rsquo;s moons&rdquo; &ndash; a story&hellip;".htmlToPlainText()
        )
    }

    @Test
    fun numericEntitiesAreDecoded() {
        assertEquals("It's 5° — cold", "It&#39;s 5&#176; &#x2014; cold".htmlToPlainText())
    }

    @Test
    fun numericEntitiesOutsideTheBasicPlaneAreDecoded() {
        assertEquals("🚀 launch", "&#128640; launch".htmlToPlainText())
        assertEquals("🚀 launch", "&#x1F680; launch".htmlToPlainText())
    }

    @Test
    fun unknownOrInvalidEntitiesAreLeftAsTheyAre() {
        assertEquals("&bogus; &#99999999; R&D", "&bogus; &#99999999; R&D".htmlToPlainText())
    }

    @Test
    fun entitiesAreDecodedOnlyOnce() {
        assertEquals("&lt;b&gt;", "&amp;lt;b&amp;gt;".htmlToPlainText())
    }

    @Test
    fun escapedMarkupIsShownAsTextInsteadOfBeingRemoved() {
        assertEquals("use <br> here", "use &lt;br&gt; here".htmlToPlainText())
    }

    @Test
    fun emptyTextStaysEmpty() {
        assertEquals("", "".htmlToPlainText())
        assertEquals("", "<br> <p></p>".htmlToPlainText())
    }

    // endregion

    // region apodExplanationToPlainText

    @Test
    fun explanationLabelIsRemoved() {
        val html = "<strong>Explanation:</strong> Sometimes Saturn disappears."

        assertEquals("Sometimes Saturn disappears.", html.apodExplanationToPlainText())
    }

    @Test
    fun explanationLabelWithASpaceBeforeTheColonIsRemoved() {
        val html = "<strong>Explanation </strong>: What is Treble Clef?" +
                "<br><br><strong>Tomorrow's picture </strong>: open space"

        assertEquals("What is Treble Clef?", html.apodExplanationToPlainText())
    }

    @Test
    fun repeatedExplanationLabelIsRemoved() {
        // Seen in the picture of 2026-10-19.
        val html = "<strong>Explanation:</strong> <b>Explanation</b> : What is Treble Clef?"

        assertEquals("What is Treble Clef?", html.apodExplanationToPlainText())
    }

    @Test
    fun tomorrowsPictureTeaserIsRemoved() {
        val html = "<strong>Explanation:</strong> Sometimes Saturn disappears." +
                "<br><br><strong>Tomorrow's picture:</strong> open space"

        assertEquals("Sometimes Saturn disappears.", html.apodExplanationToPlainText())
    }

    @Test
    fun teaserWithTypographicApostropheIsRemoved() {
        val html = "Sometimes Saturn disappears.<br><b>Tomorrow&rsquo;s picture:</b> open space"

        assertEquals("Sometimes Saturn disappears.", html.apodExplanationToPlainText())
    }

    @Test
    fun teaserGluedToTheLastSentenceIsRemoved() {
        val html = "Sometimes Saturn disappears.Tomorrow's picture: open space"

        assertEquals("Sometimes Saturn disappears.", html.apodExplanationToPlainText())
    }

    @Test
    fun linksAndEmphasisInsideTheExplanationBecomeText() {
        val html = "<strong>Explanation:</strong> The <a href=\"https://example.test/?a=1&amp;b=2\">" +
                "Milky Way</a> is <em>very</em> far away."

        assertEquals("The Milky Way is very far away.", html.apodExplanationToPlainText())
    }

    @Test
    fun paragraphsInsideTheExplanationAreKept() {
        val html = "<strong>Explanation:</strong> First paragraph.<br><br>Second paragraph." +
                "<br><br><strong>Tomorrow's picture:</strong> open space"

        assertEquals(
            "First paragraph.\n\nSecond paragraph.",
            html.apodExplanationToPlainText()
        )
    }

    @Test
    fun explanationThatIsAlreadyPlainTextIsLeftUntouched() {
        val text = "Do you recognize this constellation?"

        assertEquals(text, text.apodExplanationToPlainText())
    }

    @Test
    fun theWordExplanationInsideTheTextIsNotRemoved() {
        val html = "<strong>Explanation:</strong> One explanation: gravity."

        assertEquals("One explanation: gravity.", html.apodExplanationToPlainText())
    }

    // endregion
}
