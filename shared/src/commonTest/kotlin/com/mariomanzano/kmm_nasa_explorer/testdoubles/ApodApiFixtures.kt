package com.mariomanzano.kmm_nasa_explorer.testdoubles

import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesService
import com.mariomanzano.kmm_nasa_explorer.network.DailyPicturesServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Answers shaped like the ones of https://science.nasa.gov/wp-json/wp/v2/apod-basic, with made up
 * content.
 */
object ApodApiFixtures {

    const val PAGE_URL =
        "https://science.nasa.gov/image-article/apod-2026-november-10-sample-nebula/"

    const val IMAGE_URL =
        "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/2026/november/" +
                "SampleNebula_Author_2048.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg"

    const val EXPLANATION_TEXT =
        "What lights up this nebula? The glowing gas of the Sample Nebula is energized by " +
                "young stars -- and by one very hot star in particular. The featured image " +
                "spans about 40 light-years."

    /** The whole answer of `GET apod-basic/261110`, with every field the server sends. */
    const val SINGLE_DAY = """
        {
          "date": "2026-11-10",
          "post_id": 1509570,
          "title": "Sample Nebula &amp; Friends",
          "permalink": "https://science.nasa.gov/image-article/apod-2026-november-10-sample-nebula/",
          "media_type": "image",
          "explanation": "<strong>Explanation:</strong> What lights up this nebula? The glowing gas of the <a href=\"https://science.nasa.gov/resource/sample-nebula/\">Sample Nebula</a> is energized by young stars -- and by <em>one</em> very hot star in particular. The <a href=\"https://example.test/image?w=687&amp;fit=crop\">featured image</a> spans about 40 light-years.<br><br><strong>Tomorrow's picture:</strong> open space",
          "credit": "<a href=\"https://example.test/author\">Sample Author</a>",
          "copyright": "<a href=\"https://example.test/author\">Sample Author</a>",
          "alt": "A red cloud of gas in front of a dark field of stars.",
          "url": "https://science.nasa.gov/image-article/apod-2026-november-10-sample-nebula/",
          "hdurl": "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/2026/november/SampleNebula_Author_2048.jpg/jcr:content/renditions/cq5dam.web.1280.1280.jpeg",
          "basic_html": "<!doctype html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n</head>\n<body><h1>Sample Nebula</h1></body>\n</html>",
          "basic_html_url": "https://science.nasa.gov/wp-json/wp/v2/apod-basic/261110/html"
        }
    """

    const val NOT_FOUND =
        """{"code":"apod_basic_not_found","message":"APOD not found.","data":{"status":404}}"""

    const val INVALID_DATES =
        """{"code":"rest_invalid_param","message":"Invalid parameter(s): date_from, date_to","data":{"status":400,"params":{"date_from":"Invalid parameter.","date_to":"Invalid parameter."},"details":[]}}"""

    /** One picture as it comes in a list when only the fields the app reads are requested. */
    fun picture(date: String, mediaType: String = "image"): String =
        """{"date":"$date","title":"Title $date","media_type":"$mediaType",""" +
                """"explanation":"<strong>Explanation:</strong> Text for $date.",""" +
                """"url":"${pageUrl(date)}","hdurl":"${imageUrl(date)}"}"""

    fun pageUrl(date: String) = "https://science.nasa.gov/image-article/apod-$date/"

    fun imageUrl(date: String) =
        "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/$date.jpg" +
                "/jcr:content/renditions/cq5dam.web.1280.1280.jpeg"

    fun page(dates: List<String>): String =
        dates.joinToString(separator = ",", prefix = "[", postfix = "]") { picture(it) }

    /** Generic picture the API gives some days instead of their own image. */
    const val GENERIC_PICTURE =
        "https://assets.science.nasa.gov/dynamicimage/assets/science/astro/programs/" +
                "cosmic-origins/images/misc/news-thumbnail.png?w=594&h=516&fit=clip"

    /** The image linked from the page of a day whose `hdurl` is [GENERIC_PICTURE]. */
    fun linkedImage(date: String) =
        "https://assets.science.nasa.gov/content/dam/science/cds/apod/apod/$date/linked_big.gif"

    /** A picture of a day whose `hdurl` is the generic picture. */
    fun pictureWithGenericImage(date: String, mediaType: String = "image"): String =
        """{"date":"$date","title":"Title $date","media_type":"$mediaType",""" +
                """"explanation":"Text for $date.",""" +
                """"url":"${pageUrl(date)}","hdurl":"$GENERIC_PICTURE"}"""

    /** Answer of `apod-basic/{yymmdd}?_fields=basic_html` for a page that links [image]. */
    fun pageLinking(image: String?): String {
        val link = image?.let { """<a href=\"$it\"><img src=\"https://example.test/small.gif\"></a>""" }
            .orEmpty()
        return """{"basic_html":"<!doctype html><html><body><p>Text</p>$link</body></html>"}"""
    }

    /** [count] consecutive days ending at [newest], newest first, like the server sorts them. */
    fun datesBackFrom(newest: LocalDate, count: Int): List<String> =
        (0 until count).map { newest.minus(it, DateTimeUnit.DAY).toString() }
}

fun MockRequestHandleScope.respondJson(
    body: String,
    status: HttpStatusCode = HttpStatusCode.OK,
    totalPages: Int? = null
): HttpResponseData {
    val headers = buildList {
        add(HttpHeaders.ContentType to listOf("application/json; charset=UTF-8"))
        if (totalPages != null) add("X-WP-TotalPages" to listOf(totalPages.toString()))
    }
    return respond(content = body, status = status, headers = headersOf(*headers.toTypedArray()))
}

/**
 * Creates the real service on top of [engine]. It registers the client in Koin, so the test has
 * to call `stopKoin()` when it finishes.
 */
fun serviceBackedBy(engine: MockEngine): DailyPicturesService {
    val client = HttpClient(engine)
    startKoin { modules(module { single<HttpClient> { client } }) }
    return DailyPicturesServiceImpl()
}
