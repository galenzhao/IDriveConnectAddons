package io.bimmergestalt.idriveconnectaddons.screenmirror

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.UiModeManager
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.Process
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import io.bimmergestalt.idriveconnectaddons.lib.CarCapabilities
import io.bimmergestalt.idriveconnectaddons.screenmirror.carapp.CarApp
import io.bimmergestalt.idriveconnectkit.android.CarAppAssetResources
import io.bimmergestalt.idriveconnectkit.android.IDriveConnectionReceiver
import io.bimmergestalt.idriveconnectkit.android.IDriveConnectionStatus
import io.bimmergestalt.idriveconnectkit.android.security.SecurityAccess


class CarAppService: Service() {
    var thread: CarThread? = null
    var app: CarApp? = null

    override fun onCreate() {
        super.onCreate()
        SecurityAccess.getInstance(applicationContext).connect()
    }

    /**
     * When a car is connected, it will bind the Addon Service
     */
    override fun onBind(intent: Intent?): IBinder? {
        intent ?: return null
        IDriveConnectionReceiver().onReceive(applicationContext, intent)
        startThread()
        return null
    }

    /**
     * If the thread crashes for any reason,
     * opening the main app will trigger a Start on the Addon Services
     * as a chance to reconnect
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent ?: return START_NOT_STICKY
        IDriveConnectionReceiver().onReceive(applicationContext, intent)
        startThread()
        return START_STICKY
    }

    /**
     * The car has disconnected, so forget the previous details
     */
    override fun onUnbind(intent: Intent?): Boolean {
        IDriveConnectionStatus.reset()
        return super.onUnbind(intent)
    }

    private fun hasProjectMediaPermission(): Boolean {
        val appOps = getSystemService(AppOpsManager::class.java)
        val mode = if (Build.VERSION.SDK_INT >= 29) {
            appOps.unsafeCheckOpNoThrow("android:project_media", Process.myUid(), packageName)
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow("android:project_media", Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
    /**
     * Starts the thread for the car app, if it isn't running
     */
    //@RequiresApi(Build.VERSION_CODES.O)
    fun startThread() {
        val iDriveConnectionStatus = IDriveConnectionReceiver()
        val securityAccess = SecurityAccess.getInstance(applicationContext)
        if (iDriveConnectionStatus.isConnected &&
            securityAccess.isConnected() &&
            thread?.isAlive != true) {

            L.loadResources(applicationContext)
            thread = CarThread("ScreenMirroring") {
                Log.i(TAG, "CarThread is ready, starting CarApp")
                val carCapabilities = CarCapabilities(applicationContext)
                val screenMirrorProvider = ScreenMirrorProvider(thread?.handler!!)
                if (iDriveConnectionStatus.port == 4007) {
                    // running over bluetooth, decimate image quality
                    screenMirrorProvider.jpgQuality = 30
                }
                AppSettings.loadSettings(applicationContext)
                app = CarApp(
                    iDriveConnectionStatus,
                    securityAccess,
                    CarAppAssetResources(applicationContext, "smartthings"),
                    AndroidResources(applicationContext),
                    carCapabilities,
                    applicationContext.getSystemService(UiModeManager::class.java),
                    screenMirrorProvider
                ) {
                    // start up the notification when we enter the app
                    val foreground = NotificationService.shouldBeForeground()
                    NotificationService.startNotification(applicationContext, foreground)

                    // try fetching permission automatically
                    if (hasProjectMediaPermission()) {
                        AppSettings.loadSettings(applicationContext)

                        val settingsViewer = AppSettingsViewer()
                        if (settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].isNotEmpty()) {
                            if (settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].toInt()%100 == 11) {
                                val pm = getSystemService(POWER_SERVICE) as PowerManager
                                val wl = pm.newWakeLock(
                                    PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                                    "myalarmapp:alarm."
                                )
                                wl.acquire(5000)

                                val startAlarmActivity: Intent = Intent(
                                    applicationContext,
                                    RequestActivity::class.java
                                )

                                startAlarmActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startAlarmActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                startActivity(startAlarmActivity)

                                wl.release()
                            } else if (settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].toInt()%100 == 21) {
//                                MainController(applicationContext).promptPermission(true)
                            }else if (settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].toInt()%100 == 31) {
                                val fullScreenIntent = Intent(applicationContext, RequestActivity::class.java)
                                val fullScreenPendingIntent = PendingIntent.getActivity(
                                    applicationContext, 0,
                                    fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT
                                )
                                val PERMISSION_CHANNEL_ID = "PermissionNotification1"
                                val PERMISSION_NOTIFICATION_ID =
                                    534561      // dismissable permission prompt
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                    val channel = NotificationChannel(
                                        PERMISSION_CHANNEL_ID,
                                        "PennSkanvTicChannel",
                                        NotificationManager.IMPORTANCE_HIGH
                                    )
                                    channel.description =
                                        "PennSkanvTic channel for foreground service notification"

                                    val notificationManager =
                                        getSystemService<NotificationManager>(NotificationManager::class.java)
                                    notificationManager.createNotificationChannel(channel)
                                }

                                val notificationBuilder =
                                    NotificationCompat.Builder(applicationContext, PERMISSION_CHANNEL_ID)
                                        .setSmallIcon(R.drawable.ic_notify)
                                        .setContentTitle("Incoming call")
                                        .setContentText("(919) 555-1234")
                                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                                        .setCategory(NotificationCompat.CATEGORY_CALL)

                                        // Use a full-screen intent only for the highest-priority alerts where you
                                        // have an associated activity that you would like to launch after the user
                                        // interacts with the notification. Also, if your app targets Android 10
                                        // or higher, you need to request the USE_FULL_SCREEN_INTENT permission in
                                        // order for the platform to invoke this notification.
                                        .setFullScreenIntent(fullScreenPendingIntent, true)

                                val incomingCallNotification = notificationBuilder.build()
                                // Provide a unique integer for the "notificationId" of each notification.
                                startForeground(
                                    PERMISSION_NOTIFICATION_ID,
                                    incomingCallNotification
                                )
                            }
                        }
                    }
                }
            }
            thread?.start()
        } else if (thread?.isAlive != true) {
            if (thread?.isAlive != true) {
                Log.i(TAG, "Not connecting to car, because: iDriveConnectionStatus.isConnected=${iDriveConnectionStatus.isConnected} securityAccess.isConnected=${securityAccess.isConnected()}")
            } else {
                Log.d(TAG, "CarThread is still running, not trying to start it again")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        app?.onDestroy()
        NotificationService.stopNotification(applicationContext)
    }
}