package pg.marketpg.app

import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.json.JSONObject
import retrofit2.HttpException

/**
 * Converts transport, HTTP and response-decoding failures into safe messages for the UI.
 * Never exposes raw response bodies, stack traces, credentials or tokens to users.
 */
object MarketApiErrors {
    fun userMessage(error: Throwable, action: String = "complete this request"): String {
        if (error is kotlinx.coroutines.CancellationException) throw error
        MarketDiagnostics.record("ERROR", "api-error", "${error.javaClass.simpleName}: ${error.message ?: "no detail"}")

        return when (error) {
            is HttpException -> httpMessage(error)
            is UnknownHostException -> "Can't reach MarketPG. Check your internet connection and try again."
            is SocketTimeoutException -> "MarketPG is taking too long to respond. Please try again."
            is ConnectException -> "Couldn't connect to MarketPG. Check your connection or try again shortly."
            is IOException -> "A network error interrupted the request. Check your connection and try again."
            is com.google.gson.JsonParseException,
            is com.google.gson.JsonIOException,
            is java.lang.IllegalStateException -> "MarketPG returned an unexpected response. Please try again later."
            else -> "Unable to $action right now. Please try again."
        }
    }

    private fun httpMessage(error: HttpException): String {
        val code = error.code()
        val payload = try {
            error.response()?.errorBody()?.string().orEmpty()
        } catch (_: Exception) {
            ""
        }
        val json = try { JSONObject(payload) } catch (_: Exception) { null }
        val serverMessage = json?.optString("message")?.takeIf { !it.isNullOrBlank() && it != "null" }
        val validationMessage = firstValidationMessage(json)

        return when (code) {
            400 -> serverMessage ?: "MarketPG couldn't process that request. Check your details and try again."
            401 -> "Incorrect email or password. Please check your details and try again."
            403 -> {
                val lower = serverMessage.orEmpty().lowercase()
                when {
                    "suspend" in lower || "ban" in lower || "disabled" in lower ->
                        serverMessage ?: "Your account is restricted. Please contact MarketPG support."
                    "verif" in lower -> serverMessage ?: "Please verify your account before continuing."
                    else -> serverMessage ?: "You don't have permission to perform this action."
                }
            }
            404 -> serverMessage ?: "The requested MarketPG resource could not be found."
            408 -> "The request timed out. Please try again."
            409 -> serverMessage ?: "This request conflicts with the current account or listing state."
            422 -> if (serverMessage.orEmpty().contains("credentials", ignoreCase = true)) "Incorrect email or password. Please check your details and try again." else validationMessage ?: serverMessage ?: "Some details are missing or invalid. Please check and try again."
            429 -> "Too many attempts. Please wait a moment before trying again."
            in 500..599 -> "MarketPG is temporarily unavailable (server error $code). Please try again shortly."
            else -> serverMessage ?: "MarketPG couldn't complete the request (HTTP $code). Please try again."
        }
    }

    private fun firstValidationMessage(json: JSONObject?): String? {
        val errors = json?.optJSONObject("errors") ?: return null
        val keys = errors.keys()
        while (keys.hasNext()) {
            val value = errors.opt(keys.next())
            if (value is org.json.JSONArray && value.length() > 0) {
                return value.optString(0).takeIf { it.isNotBlank() }
            }
            if (value is String && value.isNotBlank()) return value
        }
        return null
    }
}
