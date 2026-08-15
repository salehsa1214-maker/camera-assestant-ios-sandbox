package app.dyrecto.service

import android.content.Context

/**
 * The single process-scoped holder that vends the current [MonitoringSession].
 *
 * Today it lazily creates one [DefaultMonitoringSession] for the whole process; tomorrow it can
 * vend per-camera or swappable instances without touching call sites (UI + service depend only on
 * [current]). [init] is called once from
 * [app.dyrecto.DyrectoApp.onCreate]; [current] is safe to call thereafter
 * from any thread.
 */
object MonitoringSessionProvider {

    @Volatile
    private var session: MonitoringSession? = null
    private lateinit var appContext: Context

    /** Wire the application context once at startup. Does not create the session yet. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    /** The current session, created lazily on first use. */
    fun current(): MonitoringSession {
        session?.let { return it }
        return synchronized(this) {
            session ?: DefaultMonitoringSession(appContext).also { session = it }
        }
    }
}
