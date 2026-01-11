package com.example.petfunny.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import com.example.petfunny.R
import kotlin.random.Random

class ShimejiService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var petView: View
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var imgPet: ImageView

    // Biến dùng cho việc kéo thả
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false // Cờ kiểm tra xem người dùng có đang giữ Pet không

    // Biến môi trường
    private var screenWidth = 0
    private var screenHeight = 0

    // Logic chuyển động (AI & Physics)
    private val handler = Handler(Looper.getMainLooper())
    private var actionState = STATE_FALLING // Trạng thái hiện tại
    private var lastActionState = -1 // Trạng thái trước đó (để kiểm tra thay đổi animation)
    private var timeToNextAction = 0 // Thời gian đếm ngược để đổi hành động

    // Định nghĩa các trạng thái
    companion object {
        const val STATE_IDLE = 0        // Đứng chơi
        const val STATE_WALK_LEFT = 1   // Đi bộ sang trái
        const val STATE_WALK_RIGHT = 2  // Đi bộ sang phải
        const val STATE_FALLING = 3     // Đang rơi tự do
        const val STATE_DRAGGING = 4    // Đang bị người dùng kéo
        const val STATE_RUN_LEFT = 5    // Chạy nhanh trái
        const val STATE_RUN_RIGHT = 6   // Chạy nhanh phải
        const val STATE_FLY = 7         // Bay lượn

        const val FLOOR_OFFSET = 150    // Khoảng cách từ đáy màn hình
        const val WALK_SPEED = 10        // Tốc độ đi bộ
        const val RUN_SPEED = 20       // Tốc độ chạy
        const val FLY_SPEED = 15         // Tốc độ bay
        const val GRAVITY_SPEED = 20    // Tốc độ rơi
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Lấy kích thước màn hình để tính toán biên giới
        updateScreenSize()

        setupPetView()
    }

    private fun updateScreenSize() {
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
    }

    private fun setupPetView() {
        petView = LayoutInflater.from(this).inflate(R.layout.layout_pet, null)
        imgPet = petView.findViewById(R.id.imgPet)

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.START
        params.x = screenWidth / 2 - 100
        params.y = screenHeight / 2

        windowManager.addView(petView, params)

        setupTouchListener()

        // Bắt đầu vòng lặp sự sống của Pet (Game Loop)
        handler.post(gameLoopRunnable)
    }

    // --- GAME LOOP: Xử lý Logic mỗi khung hình (60 FPS) ---
    private val gameLoopRunnable = object : Runnable {
        override fun run() {
            if (!isDragging) {
                updatePhysics() // Xử lý trọng lực
                updateAI()      // Xử lý hành vi di chuyển
                updateAnimation() // Cập nhật hình ảnh (Animation)
            }
            handler.postDelayed(this, 16)
        }
    }

    // --- CẬP NHẬT HÌNH ẢNH (ANIMATION) ---
    private fun updateAnimation() {
        // Chỉ cập nhật khi trạng thái thay đổi để tránh load lại ảnh liên tục gây giật
        if (actionState == lastActionState) return
        lastActionState = actionState

        // Reset scale mặc định
        imgPet.scaleX = 1f

        when (actionState) {
            STATE_WALK_LEFT -> {
                imgPet.scaleX = -1f // Lật hình
                // setPetAnimation(R.drawable.anim_walk) // Thay bằng file xml animation của bạn
                imgPet.setImageResource(R.drawable.sonix_left) // Demo tạm
            }
            STATE_WALK_RIGHT -> {
                // setPetAnimation(R.drawable.anim_walk)
                imgPet.setImageResource(R.drawable.sonix_right) // Demo tạm
            }
            STATE_RUN_LEFT -> {
                imgPet.scaleX = -1f
                // setPetAnimation(R.drawable.anim_run) // Chạy nhanh
                imgPet.setImageResource(R.drawable.sonix_chay_trai) // Demo tạm
            }
            STATE_RUN_RIGHT -> {
                // setPetAnimation(R.drawable.anim_run)
                imgPet.setImageResource(R.drawable.sonix_chay_phai) // Demo tạm
            }
            STATE_FLY -> {
                // setPetAnimation(R.drawable.anim_fly) // Bay
                imgPet.setImageResource(R.drawable.sonix_bay) // Demo tạm
            }
            STATE_FALLING -> {
                imgPet.setImageResource(R.drawable.sonix_ngoi) // Demo rơi
            }
            STATE_IDLE -> {
                // setPetAnimation(R.drawable.anim_idle) // Đứng thở
                imgPet.setImageResource(R.drawable.sonix_dung) // Demo đứng yên
            }
        }
    }

    /**
     * Hàm hỗ trợ chạy AnimationDrawable (Frame Animation)
     * Cách dùng: Tạo file res/drawable/anim_walk.xml chứa danh sách các frame
     */
    private fun setPetAnimation(resourceId: Int) {
        imgPet.setBackgroundResource(resourceId)
        val frameAnimation = imgPet.background as? AnimationDrawable
        frameAnimation?.start()
    }

    private fun updatePhysics() {
        // Nếu đang BAY thì bỏ qua trọng lực
        if (actionState == STATE_FLY) return

        // 1. Xử lý Trọng lực (Gravity)
        val floorY = screenHeight - (petView.height + FLOOR_OFFSET)

        if (params.y < floorY) {
            params.y += GRAVITY_SPEED

            // Nếu không phải đang bay mà lơ lửng -> Rơi
            if (actionState != STATE_FLY) {
                actionState = STATE_FALLING
            }

            if (params.y > floorY) {
                params.y = floorY
                actionState = STATE_IDLE // Chạm đất
            }
            windowManager.updateViewLayout(petView, params)
        } else {
            if (actionState == STATE_FALLING) actionState = STATE_IDLE
        }
    }

    private fun updateAI() {
        if (actionState == STATE_FALLING) return

        // 2. Bộ não AI: Quyết định hành động
        if (timeToNextAction <= 0) {
            pickRandomAction()
        } else {
            timeToNextAction--
        }

        // 3. Thực hiện di chuyển
        when (actionState) {
            STATE_WALK_LEFT -> moveHorizontal(-WALK_SPEED)
            STATE_WALK_RIGHT -> moveHorizontal(WALK_SPEED)
            STATE_RUN_LEFT -> moveHorizontal(-RUN_SPEED)
            STATE_RUN_RIGHT -> moveHorizontal(RUN_SPEED)
            STATE_FLY -> moveFlying() // Logic bay riêng
            STATE_IDLE -> { /* Đứng yên */ }
        }
    }

    private fun moveHorizontal(speed: Int) {
        params.x += speed

        // Va chạm tường
        if (params.x < 0) {
            params.x = 0
            pickRandomAction() // Đụng tường thì đổi hành động ngay
        } else if (params.x > screenWidth - petView.width) {
            params.x = screenWidth - petView.width
            pickRandomAction()
        }
        windowManager.updateViewLayout(petView, params)
    }

    private fun moveFlying() {
        // Bay lượn ngẫu nhiên theo hình sin hoặc zig zac
        params.x += if (Random.nextBoolean()) FLY_SPEED else -FLY_SPEED
        params.y -= FLY_SPEED / 2 // Bay hơi hướng lên trên

        // Giới hạn trần nhà (không bay mất hút lên trên)
        if (params.y < 100) {
            params.y = 100
            actionState = STATE_FALLING // Bay cao quá thì mỏi cánh rơi xuống
        }
        // Giới hạn tường
        if (params.x < 0 || params.x > screenWidth - petView.width) {
            params.x = if (params.x < 0) 0 else screenWidth - petView.width
        }

        windowManager.updateViewLayout(petView, params)
    }

    private fun pickRandomAction() {
        // Random hành động phức tạp hơn
        val rand = Random.nextInt(100)
        actionState = when {
            rand < 40 -> STATE_IDLE         // 40% đứng chơi
            rand < 65 -> STATE_WALK_LEFT    // 25% đi bộ
            rand < 90 -> STATE_WALK_RIGHT
            rand < 95 -> if (Random.nextBoolean()) STATE_RUN_LEFT else STATE_RUN_RIGHT // 5% chạy nhanh
            else -> STATE_FLY               // 5% bay lên
        }

        // Random thời gian thực hiện (60 frames ~ 1s)
        timeToNextAction = Random.nextInt(60, 200)
    }

    private fun setupTouchListener() {
        petView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isDragging = true
                    actionState = STATE_DRAGGING
                    // Reset animation
                    imgPet.setImageResource(R.drawable.sonix_keo) // Demo icon khi bị tóm

                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    updateScreenSize()
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(petView, params)
                    return@setOnTouchListener true
                }
                MotionEvent.ACTION_UP -> {
                    isDragging = false
                    actionState = STATE_FALLING
                    lastActionState = -1 // Reset để cập nhật lại animation rơi ngay lập tức
                    return@setOnTouchListener true
                }
                else -> return@setOnTouchListener false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(gameLoopRunnable)
        if (::petView.isInitialized) {
            windowManager.removeView(petView)
        }
    }

    private fun createNotification(): Notification {
        val channelId = "ShimejiChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Shimeji Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.pet_notification_title))
            .setContentText(getString(R.string.pet_notification_text))
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }
}