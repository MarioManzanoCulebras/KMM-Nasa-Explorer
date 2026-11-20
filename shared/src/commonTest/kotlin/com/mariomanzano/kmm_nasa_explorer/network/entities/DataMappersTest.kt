package com.mariomanzano.kmm_nasa_explorer.network.entities

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class DataMappersTest {

    @Test
    fun copiesTheTextFieldsIntoTheDomainItem() {
        val item = apiPicture(
            date = "2024-01-16",
            title = "The Orion You Can Almost See",
            explanation = "Do you recognize this constellation?",
            mediaType = "image"
        ).asPictureOfTheDayItem()

        assertEquals("2024-01-16", item.date)
        assertEquals("The Orion You Can Almost See", item.title)
        assertEquals("Do you recognize this constellation?", item.description)
        assertEquals("image", item.mediaType)
    }

    @Test
    fun newItemsAreNotFavoriteAndBelongToTheDailyPictureType() {
        val item = apiPicture(date = "2024-01-16").asPictureOfTheDayItem()

        assertFalse(item.favorite)
        assertEquals("dailyPicture", item.type)
    }

    @Test
    fun idIsTheDayOfYearFollowedByTheYear() {
        assertEquals(162024, apiPicture(date = "2024-01-16").asPictureOfTheDayItem().id)
        assertEquals(12024, apiPicture(date = "2024-01-01").asPictureOfTheDayItem().id)
        assertEquals(3652023, apiPicture(date = "2023-12-31").asPictureOfTheDayItem().id)
    }

    @Test
    fun idCountsTheLeapDay() {
        assertEquals(602024, apiPicture(date = "2024-02-29").asPictureOfTheDayItem().id)
        assertEquals(3662024, apiPicture(date = "2024-12-31").asPictureOfTheDayItem().id)
    }

    @Test
    fun idsAreDifferentForEveryDayOfARange() {
        val dates = (1..31).map { "2024-01-${it.toString().padStart(2, '0')}" } +
                (1..29).map { "2024-02-${it.toString().padStart(2, '0')}" }

        val ids = dates.map { apiPicture(date = it).asPictureOfTheDayItem().id }

        assertEquals(dates.size, ids.toSet().size)
    }

    @Test
    fun missingOptionalFieldsBecomeEmptyStrings() {
        val item = ApiAPOD(
            date = "2024-01-16",
            title = null,
            explanation = null,
            media_type = null,
            url = null
        ).asPictureOfTheDayItem()

        assertEquals("", item.title)
        assertEquals("", item.description)
        assertEquals("", item.url)
        assertEquals("", item.mediaType)
    }

    @Test
    fun aDateThatIsNotIsoFormattedIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            apiPicture(date = "16/01/2024").asPictureOfTheDayItem()
        }
    }

    // region answers of the APOD WordPress API

    @Test
    fun imageAddressComesFromHdurlBecauseUrlIsTheWebPage() {
        val item = apiPicture(
            date = "2026-11-10",
            mediaType = "image",
            url = PAGE,
            hdurl = "$ASSET/jcr:content/renditions/cq5dam.web.1280.1280.jpeg"
        ).asPictureOfTheDayItem()

        assertEquals("$ASSET/jcr:content/renditions/cq5dam.web.1280.1280.jpeg", item.url)
    }

    @Test
    fun videoKeepsTheWebPageBecauseHdurlIsOnlyItsPreviewImage() {
        val item = apiPicture(
            date = "2026-10-28",
            mediaType = "video",
            url = PAGE,
            hdurl = "$ASSET/jcr:content/renditions/cq5dam.web.1280.1280.jpeg"
        ).asPictureOfTheDayItem()

        assertEquals(PAGE, item.url)
        assertEquals("video", item.mediaType)
    }

    @Test
    fun imageWithoutHdurlFallsBackToUrl() {
        val legacyImage = "https://apod.nasa.gov/apod/image/2401/OrionMcc_Guzzini_960.jpg"

        assertEquals(
            legacyImage,
            apiPicture(date = "2024-01-16", url = legacyImage, hdurl = null)
                .asPictureOfTheDayItem().url
        )
        assertEquals(
            legacyImage,
            apiPicture(date = "2024-01-16", url = legacyImage, hdurl = " ")
                .asPictureOfTheDayItem().url
        )
    }

    @Test
    fun tinyThumbnailRenditionIsReplacedByTheOriginalImage() {
        val item = apiPicture(
            date = "2026-11-08",
            url = PAGE,
            hdurl = "$ASSET/jcr:content/renditions/cq5dam.thumbnail.48.48.png"
        ).asPictureOfTheDayItem()

        assertEquals(ASSET, item.url)
    }

    @Test
    fun otherImageAddressesAreLeftUntouched() {
        val dynamicImage = "https://assets.science.nasa.gov/dynamicimage/assets/science/cds/" +
                "apod/apod/2021/october/DarkMatter_1200.jpg?w=1200&h=900&fit=clip"

        assertEquals(dynamicImage, dynamicImage.toFullSizeImageUrl())
        assertEquals(
            "$ASSET/jcr:content/renditions/cq5dam.web.1280.1280.jpeg",
            "$ASSET/jcr:content/renditions/cq5dam.web.1280.1280.jpeg".toFullSizeImageUrl()
        )
    }

    @Test
    fun explanationHtmlBecomesTheDescriptionText() {
        val item = apiPicture(
            date = "2026-11-10",
            explanation = "<strong>Explanation:</strong> Sometimes <a href=\"https://example.test/\">" +
                    "Saturn</a> disappears.<br><br><strong>Tomorrow's picture:</strong> open space"
        ).asPictureOfTheDayItem()

        assertEquals("Sometimes Saturn disappears.", item.description)
    }

    @Test
    fun titleEntitiesAreDecoded() {
        val item = apiPicture(date = "2026-11-10", title = "Rosetta&#8217;s Selfie &amp; More")
            .asPictureOfTheDayItem()

        assertEquals("Rosetta’s Selfie & More", item.title)
    }

    // endregion

    private fun apiPicture(
        date: String,
        title: String? = "Title",
        explanation: String? = "Explanation",
        mediaType: String? = "image",
        url: String? = "https://example.test/picture.jpg",
        hdurl: String? = null
    ) = ApiAPOD(
        date = date,
        title = title,
        explanation = explanation,
        media_type = mediaType,
        url = url,
        hdurl = hdurl
    )

    private companion object {
        const val PAGE = "https://science.nasa.gov/image-article/apod-2026-november-10-sample/"
        const val ASSET = "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/" +
                "2026/november/Sample_Author_1200.jpg"
    }
}
