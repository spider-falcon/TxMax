package assistant

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// 1. New Data Class to hold the rich response
data class SearchResult(
    val text: String,
    val imageUrl: String? = null,
    val sourceUrl: String? = null
)

object QuickResult {

    suspend fun fetchAnswer(query: String): SearchResult {
        return withContext(Dispatchers.IO) {
            // 1. Try DuckDuckGo First
            var result = tryDuckDuckGo(query)
            if (result != null) return@withContext result

            // 2. Try Wikipedia as fallback
            result = tryWikipedia(query)
            if (result != null) return@withContext result

            // 3. Final Fallback
            return@withContext SearchResult("I couldn't find a short answer, but I'll open the web for you.")
        }
    }

    private fun tryDuckDuckGo(query: String): SearchResult? {
        try {
            val urlString = "https://api.duckduckgo.com/?q=${Uri.encode(query)}&format=json&no_html=1"
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val abstractText = json.optString("AbstractText", "")
                if (abstractText.isNotEmpty()) {

                    // DuckDuckGo sometimes returns relative image paths
                    var img = json.optString("Image", "")
                    if (img.isNotEmpty() && img.startsWith("/")) {
                        img = "https://duckduckgo.com$img"
                    }

                    val link = json.optString("AbstractURL", "")

                    return SearchResult(
                        text = cleanText(abstractText),
                        imageUrl = img.ifEmpty { null },
                        sourceUrl = link.ifEmpty { null }
                    )
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    private fun tryWikipedia(query: String): SearchResult? {
        try {
            // Upgraded Wikipedia API URL to fetch text (extracts), images (pageimages), and URLs (info)
            val urlString = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts|pageimages|info&inprop=url&piprop=thumbnail&pithumbsize=600&exsentences=2&exlimit=1&explaintext=1&formatversion=2&generator=search&gsrsearch=${Uri.encode(query)}&gsrlimit=1"
            val connection = URL(urlString).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "TXMaxAssistant/1.0 (Android)")
            connection.connectTimeout = 3000
            connection.readTimeout = 3000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val pages = json.optJSONObject("query")?.optJSONArray("pages")
                if (pages != null && pages.length() > 0) {
                    val page = pages.getJSONObject(0)
                    val extract = page.optString("extract", "")

                    if (extract.isNotBlank()) {
                        val img = page.optJSONObject("thumbnail")?.optString("source")
                        val link = page.optString("fullurl")

                        return SearchResult(
                            text = cleanText(extract),
                            imageUrl = if (img.isNullOrEmpty()) null else img,
                            sourceUrl = if (link.isNullOrEmpty()) null else link
                        )
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return null
    }

    private fun cleanText(rawText: String): String {
        return rawText
            .replace(Regex("={2,}[^=]+={2,}"), "")
            .replace(Regex("\\[.*?\\]"), "")
            .replace("#", "").replace("*", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}