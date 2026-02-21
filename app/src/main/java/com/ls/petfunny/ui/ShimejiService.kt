package com.ls.petfunny.ui

import android.app.KeyguardManager
import android.app.Notification.PRIORITY_LOW
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.view.Choreographer
import android.view.Gravity
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.ls.petfunny.R
import com.ls.petfunny.data.model.Sprites
import com.ls.petfunny.di.repository.Helper
import com.ls.petfunny.di.repository.SpritesService
import com.ls.petfunny.ui.custom.ShimejiView
import com.ls.petfunny.utils.AppConstants
import com.ls.petfunny.utils.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ShimejiService : Service(), Choreographer.FrameCallback {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    @Inject
    lateinit var helper: Helper

    @Inject
    lateinit var spritesService: SpritesService

    internal var isShimejiVisible = true

    lateinit var mWindowManager: WindowManager

    private var prefListener: PreferenceChangeListener? = null
    private var prefs: SharedPreferences? = null

    private var screenStatusReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val kgMgr = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val action = intent.action
            when {
                "android.intent.action.SCREEN_OFF" == action -> {
                    this@ShimejiService.onScreenOff()
                }

                kgMgr.isKeyguardLocked -> {
                    setForegroundNotification(true, true)
                }

                "android.intent.action.USER_PRESENT" == action -> {
                    if (isShimejiVisible) {
                        setForegroundNotification(true)
                        this@ShimejiService.onScreenOn()
                    } else {
                        setForegroundNotification(false)
                    }
                }
            }
        }
    }

    internal val shimejiViews = ArrayList<ShimejiView >(12)

    private val viewParams = HashMap<Int, LayoutParams>(12)

    private inner class PreferenceChangeListener() :
        SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
            if (isShimejiVisible && (key == AppConstants.ACTIVE_SHIMEJI_IDS || key == AppConstants.SIZE_MULTIPLIER)
            ) {
                removeMascotViews()
                loadMascotViews()
            } else if (key == AppConstants.ANIMATION_SPEED) {
                val speed = helper.getSpeedMultiplier(baseContext)
                for (view in shimejiViews) {
                    view.setSpeedMultiplier(speed)
                }
            } else if (key == AppConstants.SHOW_NOTIFICATION) {
                if (helper.getNotificationVisibility(baseContext)) {
                    setForegroundNotification(isShimejiVisible)
                    return
                }
                if (!isShimejiVisible) {
                    toggleShimejiStatus()
                }
                stopForeground(true)
            } else if (key == AppConstants.REAPPEAR_DELAY) {
                for (mascotView2 in shimejiViews) {
                    if (isShimejiVisible) {
                        mascotView2.cancelReappearTimer()
                        mascotView2.resumeAnimation()
                    }
                }
            }

        }
    }

    override fun onCreate() {
        super.onCreate()
        AppLogger.d("", "HIHI ${TAG} --->onCreate ShimejiService")
        try {
            mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            prefs = getSharedPreferences((AppConstants.MY_PREFS), Context.MODE_MULTI_PROCESS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            val action = intent?.action
            AppLogger.e("${TAG} ---> onStartCommand action=%s", action)
            if (action == ACTION_TOGGLE) {
                toggleShimejiStatus()
            } else {
                setForegroundNotification(isShimejiVisible)
                removeMascotViews()
                loadMascotViews()
                if (this.prefs == null) {
                    this.prefListener = PreferenceChangeListener()
                    this.prefs = getSharedPreferences((AppConstants.MY_PREFS), Context.MODE_MULTI_PROCESS)
                    this.prefs!!.registerOnSharedPreferenceChangeListener(this.prefListener)
                }
                registerReceiver(this.screenStatusReceiver, IntentFilter("android.intent.action.SCREEN_OFF"))
                registerReceiver(this.screenStatusReceiver, IntentFilter("android.intent.action.SCREEN_ON"))
                registerReceiver(this.screenStatusReceiver, IntentFilter("android.intent.action.USER_PRESENT"))
            }
        } catch (e: Exception) {
            AppLogger.e(e.message)
        }
        return START_STICKY
    }

    internal fun toggleShimejiStatus() {
        this.isShimejiVisible = !this.isShimejiVisible
        setForegroundNotification(isShimejiVisible)
        for (mascotView: ShimejiView in shimejiViews) {
            if (mascotView.isAttachedToWindow) {
                if (isShimejiVisible) {
                    add(mascotView)
                } else {
                    mascotView.cancelReappearTimer()
                    remove(mascotView)
                }
            }
        }
        if (isShimejiVisible) {
            startFrameLoop()
        } else {
            stopFrameLoop()
        }
    }

    fun add(mascotView: ShimejiView) {
        startFrameLoop()
        mascotView.resumeAnimation()
    }


    fun remove(mascotView: ShimejiView?) {
        if (mascotView != null && mascotView.isShown) {
            mascotView.pauseAnimation()
            mascotView.isHidden = true
            mWindowManager.removeViewImmediate(mascotView)
        }
    }

    fun loadMascotViews() {
        serviceScope.launch {
            val mascots: List<Int> = helper.getActiveTeamMembers()
            withContext(Dispatchers.IO) {
                AppLogger.e("${TAG} ---> Loading list mascots active: %s", mascots)
                spritesService.loadSpritesForMascots(mascots)
            }
            lateinit var params: LayoutParams
            AppLogger.e("${TAG} ---> Start loop add mascots to screen")
            loop@ for (intValue in mascots) {
                AppLogger.e("${TAG} ---> Start loop add mascots to screen 222222 intvalue: $intValue")
                if (intValue != -1) {
                    val spritesById: Sprites? = spritesService.getSpritesById(intValue)
                    if (spritesById == null || spritesById.isEmpty()) {
                        AppLogger.e("${TAG} ---> Frames for mascot $intValue were empty. Skipping")
                    } else {
                        AppLogger.e("${TAG} ---> Start add mascost View sprite from cache")
                        params = LayoutParams(
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT,
                            LayoutParams.TYPE_APPLICATION_OVERLAY,
                            520,
                            PixelFormat.TRANSLUCENT
                        )

                        params.gravity = Gravity.START or Gravity.TOP
                        params.width = spritesById.width
                        params.height = spritesById.height
                        var view: ShimejiView?
                        view = ShimejiView(this@ShimejiService)
                        view.width1 = spritesById.width
                        view.height1 = spritesById.height
                        view.setupMascot(intValue, true, true,spritesService,helper)
                        view.setMascotEventsListener(object : ShimejiView.MascotEventNotifier {
                            override fun hideMascot() {
                                this@ShimejiService.remove(view)
                            }

                            override fun showMascot() {
                                this@ShimejiService.add(view)
                            }
                        })
                        view.let { this@ShimejiService.shimejiViews.add(it) }
                        viewParams[view.getUniqueId().toInt()] = params
                        AppLogger.e("${TAG} ---> prepare add mascots view w = " + view.width + ",h = " + view.height)
                        mWindowManager.addView(view, params)
                        AppLogger.e("${TAG} ---> add mascots to screen mascots: ${intValue}")
                    }

                }
            }
            startFrameLoop()
        }
    }

    internal fun removeMascotViews() {
        AppLogger.d("", "HIHI ${TAG} ---> remove all MascotViews")
        stopFrameLoop()
        for (view in shimejiViews) {
            if (view.isShown) {
                view.pauseAnimation()
                mWindowManager.removeViewImmediate(view)
            }
        }
        shimejiViews.clear()
        viewParams.clear()
    }

    private fun startFrameLoop() {
        Choreographer.getInstance().removeFrameCallback(this)
        Choreographer.getInstance().postFrameCallback(this)

    }

    private fun stopFrameLoop() {
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(p0: Long) {
        if (!isShimejiVisible || shimejiViews.isEmpty()) return
        shimejiViews.forEach { view ->
            if (view.isAttachedToWindow && !view.isHidden && view.isVisible) {
                val params = viewParams[view.getUniqueId().toInt()]
                if (params != null) {
                    try {
                        // Chỉ update nếu tọa độ có thay đổi thật sự để đỡ tốn CPU
                        if (params.x != view.x.toInt() || params.y != view.y.toInt() || params.height != view.height1) {
                            params.height = view.height1
                            params.width = view.width1
                            params.x = view.x.toInt()
                            params.y = view.y.toInt()
                            mWindowManager.updateViewLayout(view, params)
                        }
                    } catch (e: Exception) { AppLogger.e(e.message) }
                }
            }
        }
        Choreographer.getInstance().postFrameCallback(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopFrameLoop()
        try {
            AppLogger.d("HIHI ${TAG} --> onDestroy")
            prefs?.unregisterOnSharedPreferenceChangeListener(this.prefListener)
            unregisterReceiver(this.screenStatusReceiver)
        } catch (e: Exception) {
            AppLogger.e("Prevented unregister Receiver crash")
        }
        removeMascotViews()
    }

    internal fun onScreenOff() {
        stopFrameLoop()
        shimejiViews.filter { !it.isHidden }.forEach { it.pauseAnimation() }
    }

    internal fun onScreenOn() {
        shimejiViews.filter { !it.isHidden }.forEach { it.resumeAnimation() }
        startFrameLoop()
    }

    internal fun setForegroundNotification(start: Boolean, islockscreen: Boolean = false) {
        if (helper.getNotificationVisibility(this)) {
            //Title
            var text: CharSequence = if (start) {
                if (islockscreen) {
                    getText(R.string.shimeji_notif_visible_lockscreen)
                } else {
                    getText(R.string.shimeji_notif_visible)
                }
            } else {
                getText(R.string.shimeji_notif_hidden)
            }
            val sub = if (start) {
                if (islockscreen) {
                    getText(R.string.shimeji_notif_visible_lockscreen_subtitle)
                } else {
                    getText(R.string.shimeji_notif_disable)
                }
            } else {
                getText(R.string.shimeji_notif_enable)
            }
            val intent =
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, ShimejiService::class.java).setAction(ACTION_TOGGLE),
                    FLAG_IMMUTABLE
                )
            if (Build.VERSION.SDK_INT >= 26) {
                setupNotificationChannel()
            }

            val largeIcon = try {
                val d = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, theme)
                helper.drawableToBitmap(d!!)
            } catch (e: Exception) {
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            }
            val builder = NotificationCompat.Builder(this, CHANNEL_NOTIFY)
                .setContentTitle(text)
                .setContentText(sub)
                .setPriority(PRIORITY_LOW)
                .setColor(getColor(R.color.purple_200))
                .setLargeIcon(largeIcon)
                .setOngoing(true).setContentIntent(intent)
                .setSmallIcon(R.mipmap.ic_launcher)
            val notification = builder.build()
            if (start) {
                startForeground(99, notification)
                return
            }
            val noti: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            noti.notify(99, notification)

        }
    }


    @RequiresApi(api = 26)
    private fun setupNotificationChannel() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_NOTIFY, "Shimeji notifications", NotificationManager.IMPORTANCE_LOW)
        channel.enableLights(false)
        channel.enableVibration(false)
        channel.setShowBadge(false)
        mNotificationManager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        try {
            for (view in shimejiViews) {
                if (!view.isHidden) {
                    view.notifyLayoutChange(this)
                }
            }
        } catch (e: Throwable) {

        }
    }



    companion object {
        const val TAG = "ShimejiService"
        const val ACTION_TOGGLE = "com.redbox.shimeji.live.shimejilife.TOGGLE_SHIMEJI"
        const val CHANNEL_NOTIFY = "shimeji_channel"
    }
}