package pg.marketpg.app

import android.content.Context
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Small private rolling diagnostic log. Never records request/response bodies or auth headers. */
object MarketDiagnostics {
    private const val FILE_NAME = "marketpg-diagnostics.log"
    private const val MAX_BYTES = 192 * 1024
    @Volatile private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
        record("INFO", "diagnostics", "Logger initialized; Android ${Build.VERSION.RELEASE}; ${safe(Build.MANUFACTURER)} ${safe(Build.MODEL)}")
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            record("FATAL", "uncaught", "thread=${safe(thread.name)} exception=${safe(throwable.javaClass.simpleName)} message=${safe(throwable.message)}")
            previous?.uncaughtException(thread, throwable)
        }
    }

    @Synchronized
    fun record(level: String, tag: String, message: String) {
        val context = appContext ?: return
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists() && file.length() > MAX_BYTES) {
                val tail = file.readText().takeLast(MAX_BYTES / 2)
                file.writeText(tail)
            }
            val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
            file.appendText("$time $level/${safe(tag)}: ${safe(message)}\n")
        } catch (_: Exception) { }
    }

    fun exportReport(): String {
        val context = appContext ?: return "Logger not initialized."
        val file = File(context.filesDir, FILE_NAME)
        return if (file.exists()) file.readText().takeLast(60000) else "No diagnostic events recorded."
    }

    fun deviceSummary(): String = "${safe(Build.MANUFACTURER)} ${safe(Build.MODEL)}; Android ${Build.VERSION.RELEASE}; SDK ${Build.VERSION.SDK_INT}"

    private fun safe(value: String?): String {
        if (value.isNullOrBlank()) return "-"
        return value
            .replace(Regex("(?i)(authorization\\s*[:=]\\s*bearer\\s+)\\S+"), "$1[REDACTED]")
            .replace(Regex("(?i)((?:password|passwd|token|secret|api[_-]?key)\\s*[:=]\\s*)[^\\s,;]+"), "$1[REDACTED]")
            .replace(Regex("[\\r\\n]+"), " ")
            .take(500)
    }
}
