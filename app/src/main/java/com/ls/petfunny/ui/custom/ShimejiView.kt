package com.ls.petfunny.ui.custom

import android.content.Context
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.PorterDuff.Mode
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Scroller
import com.ls.petfunny.data.model.Playground
import com.ls.petfunny.data.model.Sprites
import com.ls.petfunny.di.repository.Helper
import com.ls.petfunny.di.repository.SpritesService
import com.ls.petfunny.system.shimeji.Shimeji
import com.ls.petfunny.ui.ShimejiService.Companion.TAG
import com.ls.petfunny.utils.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.timerTask


//class ShimejiView(context: Context) :
//    SurfaceView(context), SurfaceHolder.Callback {
//
//    private lateinit var spritesService: SpritesService
//    private lateinit var helper: Helper
//
//    private val renderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//    private var renderJob: Job? = null
//
//    var mascotId: Int = -1
//    var isHidden = false
//    var paidenable: Boolean = false
//    var flinging: Boolean = false
//
//    var eventNotifier: MascotEventNotifier? = null
//
//    interface MascotEventNotifier {
//        fun hideMascot()
//        fun showMascot()
//    }
//
//    fun setMascotEventsListener(mascotEventNotifier: MascotEventNotifier) {
//        this.eventNotifier = mascotEventNotifier
//    }
//
//    var timer = Timer()
//    var timerTask: TimerTask? = null
//    fun cancelReappearTimer() {
//        timerTask?.cancel()
//    }
//
//    //https://www.baeldung.com/kotlin-jvm-synthetic
//    internal var flipHorizontalMatrix = Matrix()
//    internal lateinit var gestureDetector: GestureDetector
//    internal var gestureListener: OnGestureListener = object : SimpleOnGestureListener() {
//        internal var initialX: Int = 0
//        internal var initialY: Int = 0
//
//        override fun onDoubleTap(e: MotionEvent): Boolean {
//            try {
//                if (eventNotifier == null) {
//                    return true
//                }
//                eventNotifier?.hideMascot()
//                val unused: TimerTask = timerTask {
//                    run {
//                        eventNotifier?.showMascot()
//                    }
//                }
//                if (helper.getReappearDelayMs(context).toLong() >= 1) {
//                    timer.schedule(unused, helper.getReappearDelayMs(context).toLong())
//                } else {
//                    timer.schedule(unused, 60000L)//1 minute
//                }
//            } catch (e: Exception) {
//                return true
//            }
//            return true
//            //return super.onDoubleTap(e)
//        }
//
//        // esto podria solucionar el problema de getx null error https://stackoverflow.com/questions/4151385/android-simpleongesturelistener-onfling-getting-a-null-motionevent
//        // lanzarlo en el canal beta
//        // otra alternativa: https://stackoverflow.com/questions/17390873/onfling-motionevent-e1-null
//        override fun onDown(e: MotionEvent): Boolean {
//            this.initialX = shimeji.x
//            this.initialY = shimeji.y
//            /*toast.setGravity(Gravity.getAbsoluteGravity(1,2), 3, 5);
//            toast.show();*/
//            return false
//        }
//
//        /*override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
//            // Log.e(Logger.TAG, "Single tap on shimeji mascotId.id: " + mascotId.id + " mascotId.uniqueId: "+ mascotId.uniqueId)
//            return super.onSingleTapConfirmed(e)
//        }*/
//
//        override fun onFling(
//            e1: MotionEvent?,
//            e2: MotionEvent,
//            velocityX: Float,
//            velocityY: Float
//        ): Boolean {
//            //if (Animation.paidEnabled or flinging) {
//            shimeji.setFlingSpeed(velocityX.toInt(), velocityY.toInt())
//            scroller.fling(
//                shimeji.x,
//                shimeji.y, velocityX.toInt(), velocityY.toInt(),
//                playground.left - 100,
//                playground.right + 100,
//                playground.top - 100,
//                playground.bottom + 100
//            )
//            //Log.v(Logger.TAG, "Fling on shimeji: " + this@ShimejiView.mascotId)
//            //}
//            return true
//        }
//
//        //e1 will always be the initial motion event for a touch event (The ACTION_DOWN event). e2 is the current motion event.
//        override fun onScroll(
//            e1: MotionEvent?,
//            e2: MotionEvent,
//            distanceX: Float,
//            distanceY: Float
//        ): Boolean {
//            drag(
//                this.initialX + (e2.rawX - e1!!.rawX).toInt(),
//                this.initialY + (e2.rawY - e1!!.rawY).toInt()
//            )
//            return false
//        }
//    }
//
//    internal var handler1 = Handler()
//    var height1: Int = 128
//    internal var id = NEXT_ID.getAndIncrement()
//    internal var isVisible = true
//    internal lateinit var shimeji: Shimeji
//    internal lateinit var paint: Paint
//    internal lateinit var playground: Playground
//    internal lateinit var scroller: Scroller
//    internal var sizeMultiplier: Double = 0.0
//
//    // private val sizeMultiplier: Double
//    var speedMultipliers: Double = 0.0
//
//    //internal val spritesService: SpritesService
//    var width1: Int = 128
//
//    suspend fun setupMascot(id: Int, paid: Boolean, fling: Boolean, spritesService : SpritesService,helper : Helper) = withContext(Dispatchers.Main){
//        AppLogger.e("${TAG} ---> setup mascot view w = ${width1} , h = ${height1}")
//        this@ShimejiView.spritesService = spritesService
//        this@ShimejiView.helper = helper
//        this@ShimejiView.mascotId = id
//        this@ShimejiView.paidenable = paid
//        this@ShimejiView.flinging = fling
//        this@ShimejiView.paint = Paint()
//        this@ShimejiView.paint.isAntiAlias = true
//        setBackgroundColor(0)
//        setZOrderOnTop(true)
//        speedMultipliers = helper.getSpeedMultiplier(this@ShimejiView.context)
//        // spritesService.setSizeMultiplier(this@ShimejiView.sizeMultiplier, mascotId)
//        val frames = this@ShimejiView.spritesService.getSpritesById(mascotId)
//       // this@ShimejiView.height1 = frames.height
//       // this@ShimejiView.width1 = frames.width
//        this@ShimejiView.shimeji = Shimeji(mascotId, paidenable, flinging)
//        this@ShimejiView.playground = Playground(this@ShimejiView.context, false)
//        this@ShimejiView.shimeji.initialize(frames, this@ShimejiView.speedMultipliers, this@ShimejiView.playground)
//        this@ShimejiView.shimeji.startAnimation()
//        holder.setFormat(PixelFormat.TRANSPARENT)
//        holder.addCallback(this@ShimejiView)
//        this@ShimejiView.gestureDetector = GestureDetector(this@ShimejiView.context, this@ShimejiView.gestureListener)
//        this@ShimejiView.scroller = Scroller(this@ShimejiView.context)
//        if (Looper.myLooper() == null) {
//            Looper.prepare()
//        }
//        AppLogger.e("${TAG} ---> setup mascot view w = ${width1} , h = ${height1}")
//    }
//
//    fun pauseAnimation() {
//        this.isVisible = false
//    }
//
//    fun resumeAnimation() {
//        Timber.e("isvisible: true")
//        this.isVisible = true
//    }
//
//    fun setSpeedMultiplier(speedMultiplier: Double) {
//        this.shimeji.setSpeedMultiplier(speedMultiplier)
//    }
//
//    fun endDrag() {
//        this.shimeji.endDragging()
//    }
//
//    fun drag(x: Int, y: Int) {
//        this.shimeji.drag(x, y)
//    }
//
//    //invocado desde service cuando la pantalla rota
//    fun notifyLayoutChange(context: Context) {
//        this.playground = Playground(context, false)
//        shimeji.resetEnvironmentVariables(this.playground)
//    }
//
//    override fun getX(): Float {
//        try {
//            if (this.shimeji.isBeingFlung) {
//                if (this.scroller.computeScrollOffset()) {
//                    this.shimeji.fling(this.scroller.currX, this.scroller.currY)
//                } else {
//                    this.shimeji.endFlinging()
//                }
//            }
//
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        return this.shimeji.x.toFloat()
//    }
//
//    override fun getY(): Float {
//        return this.shimeji.y.toFloat()
//    }
//
//
//    override fun surfaceCreated(holder: SurfaceHolder) {
//        try {
//            renderJob = renderScope.launch {
//                while (isActive) {
//                    if (isVisible) drawMascot()
//                    delay(16) // ~60fps rendering
//                }
//            }
//        } catch (e: Exception) {
//        }
//    }
//
//    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
//
//    }
//
//    override fun surfaceDestroyed(holder: SurfaceHolder) {
//        renderJob?.cancel()
//    }
//
//    fun getUniqueId(): Long {
//        return id
//    }
//
//    private fun drawMascot() {
//        if (!this.isVisible) return
//        val canvas = holder.lockCanvas() ?: return
//        try {
//            canvas.drawColor(0, Mode.CLEAR)
//            val mascotBitmap = shimeji.frameBitmap ?: return
//
//            height1 = mascotBitmap.height
//            width1 = mascotBitmap.width
//
//            if (shimeji.isFacingLeft) {
//                canvas.drawBitmap(mascotBitmap, 0.0f, 0.0f, paint)
//            } else {
//                flipHorizontalMatrix.setScale(-1.0f, 1.0f)
//                flipHorizontalMatrix.postTranslate(width1.toFloat(), 0.0f)
//                canvas.drawBitmap(mascotBitmap, flipHorizontalMatrix, paint)
//            }
//        } catch (e: Exception) {
//            Timber.e(e)
//        } finally {
//            holder.unlockCanvasAndPost(canvas)
//        }
//    }
//
//
//
//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        super.dispatchTouchEvent(ev)
//        try {
//            performClick()
//            if (ev.action == MotionEvent.ACTION_UP /*cuando suelto el dedo */) {
//                endDrag()
//            }
//        } catch (e: Exception) {
//            return false
//        }
//        return this.gestureDetector.onTouchEvent(ev)
//    }
//
//    companion object {
//        internal val NEXT_ID = AtomicLong(0)
//    }
//
//
//}

class ShimejiView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var spritesService: SpritesService
    private lateinit var helper: Helper

    private val renderScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var renderJob: Job? = null

    // CỜ BẢO VỆ: Đảm bảo mọi thứ đã init xong mới cho phép render / touch
    @Volatile
    private var isSetupComplete = false

    var mascotId: Int = -1
    var isHidden = false
    var paidenable: Boolean = false
    var flinging: Boolean = false

    var eventNotifier: MascotEventNotifier? = null

    interface MascotEventNotifier {
        fun hideMascot()
        fun showMascot()
    }

    fun setMascotEventsListener(mascotEventNotifier: MascotEventNotifier) {
        this.eventNotifier = mascotEventNotifier
    }

    var timer = Timer()
    var timerTask: TimerTask? = null
    fun cancelReappearTimer() {
        timerTask?.cancel()
    }

    internal var flipHorizontalMatrix = Matrix()
    internal lateinit var gestureDetector: GestureDetector

    internal var gestureListener: GestureDetector.OnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
        internal var initialX: Int = 0
        internal var initialY: Int = 0

        override fun onDoubleTap(e: MotionEvent): Boolean {
            try {
                if (eventNotifier == null) return true
                eventNotifier?.hideMascot()
                val unused: TimerTask = timerTask {
                    eventNotifier?.showMascot()
                }
                val delayMs = helper.getReappearDelayMs(context).toLong()
                timer.schedule(unused, if (delayMs >= 1) delayMs else 60000L)
            } catch (e: Exception) {
                Timber.e(e)
            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            if (!isSetupComplete) return false
            this.initialX = shimeji.x
            this.initialY = shimeji.y
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            if (!isSetupComplete) return false
            shimeji.setFlingSpeed(velocityX.toInt(), velocityY.toInt())
            scroller.fling(
                shimeji.x, shimeji.y, velocityX.toInt(), velocityY.toInt(),
                playground.left - 100, playground.right + 100,
                playground.top - 100, playground.bottom + 100
            )
            return true
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            if (!isSetupComplete || e1 == null) return false
            drag(
                this.initialX + (e2.rawX - e1.rawX).toInt(),
                this.initialY + (e2.rawY - e1.rawY).toInt()
            )
            return false
        }
    }

    internal var handler1 = Handler(Looper.getMainLooper())
    var height1: Int = 128
    internal var id = NEXT_ID.getAndIncrement()
    internal var isVisible = true

    internal lateinit var shimeji: Shimeji
    internal lateinit var paint: Paint
    internal lateinit var playground: Playground
    internal lateinit var scroller: Scroller
    internal var sizeMultiplier: Double = 0.0
    var speedMultipliers: Double = 0.0
    var width1: Int = 128

    suspend fun setupMascot(id: Int, paid: Boolean, fling: Boolean, spritesService: SpritesService, helper: Helper) = withContext(Dispatchers.Main) {
        AppLogger.e("$TAG ---> setup mascot view w = $width1 , h = $height1")

        this@ShimejiView.spritesService = spritesService
        this@ShimejiView.helper = helper
        this@ShimejiView.mascotId = id
        this@ShimejiView.paidenable = paid
        this@ShimejiView.flinging = fling

        this@ShimejiView.paint = Paint().apply { isAntiAlias = true }
        setBackgroundColor(0)
        setZOrderOnTop(true)

        speedMultipliers = helper.getSpeedMultiplier(this@ShimejiView.context)
        val frames = this@ShimejiView.spritesService.getSpritesById(mascotId)

        this@ShimejiView.shimeji = Shimeji(mascotId, paidenable, flinging)
        this@ShimejiView.playground = Playground(this@ShimejiView.context, false)
        this@ShimejiView.shimeji.initialize(
            frames ?: Sprites(hashMapOf()),
            this@ShimejiView.speedMultipliers, this@ShimejiView.playground)
        this@ShimejiView.shimeji.startAnimation()

        holder.setFormat(PixelFormat.TRANSPARENT)
        holder.addCallback(this@ShimejiView)
        this@ShimejiView.gestureDetector = GestureDetector(this@ShimejiView.context, this@ShimejiView.gestureListener)
        this@ShimejiView.scroller = Scroller(this@ShimejiView.context)

        // Đánh dấu khởi tạo thành công -> Cho phép render và touch
        isSetupComplete = true

        AppLogger.e("$TAG ---> finish setup mascot view w = $width1 , h = $height1")
    }

    fun pauseAnimation() { this.isVisible = false }

    fun resumeAnimation() { this.isVisible = true }

    fun setSpeedMultiplier(speedMultiplier: Double) {
        if (isSetupComplete) this.shimeji.setSpeedMultiplier(speedMultiplier)
    }

    fun endDrag() {
        if (isSetupComplete) this.shimeji.endDragging()
    }

    fun drag(x: Int, y: Int) {
        if (isSetupComplete) this.shimeji.drag(x, y)
    }

    fun notifyLayoutChange(context: Context) {
        if (!isSetupComplete) return
        this.playground = Playground(context, false)
        shimeji.resetEnvironmentVariables(this.playground)
    }

    override fun getX(): Float {
        if (!isSetupComplete) return 0f
        try {
            if (this.shimeji.isBeingFlung) {
                if (this.scroller.computeScrollOffset()) {
                    this.shimeji.fling(this.scroller.currX, this.scroller.currY)
                } else {
                    this.shimeji.endFlinging()
                }
            }
        } catch (e: Exception) { Timber.e(e) }
        return this.shimeji.x.toFloat()
    }

    override fun getY(): Float {
        return if (isSetupComplete) this.shimeji.y.toFloat() else 0f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        renderJob?.cancel()
        renderJob = renderScope.launch {
            while (isActive) {
                // Chỉ render khi view visible và đã setup xong
                if (isVisible && isSetupComplete) {
                    drawMascot()
                }
                delay(16) // ~60fps
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        renderJob?.cancel()
        isSetupComplete = false
    }

    fun getUniqueId(): Long = id

    private fun drawMascot() {
            // 1. BẢO VỆ TẦNG 1: Nếu Surface đã chết hoặc chưa sẵn sàng thì bỏ qua luôn
            if (!holder.surface.isValid) return

            var canvas: android.graphics.Canvas? = null
            try {
                // 2. Lock canvas
                canvas = holder.lockCanvas()
                // Đề phòng trường hợp lockCanvas trả về null do Surface vừa bị huỷ tích tắc trước đó
                if (canvas == null) return

                canvas.drawColor(0, Mode.CLEAR)

                val mascotBitmap = shimeji.frameBitmap ?: return
                height1 = mascotBitmap.height
                width1 = mascotBitmap.width

                if (shimeji.isFacingLeft) {
                    canvas.drawBitmap(mascotBitmap, 0.0f, 0.0f, paint)
                } else {
                    flipHorizontalMatrix.setScale(-1.0f, 1.0f)
                    flipHorizontalMatrix.postTranslate(width1.toFloat(), 0.0f)
                    canvas.drawBitmap(mascotBitmap, flipHorizontalMatrix, paint)
                }

            } catch (e: Exception) {
                Timber.e("Lỗi trong quá trình vẽ Shimeji: ${e.message}")
            } finally {
                // 3. BẢO VỆ TẦNG 2: Bọc try-catch cho hàm unlock
                if (canvas != null) {
                    try {
                        // Cố gắng unlock, nếu Surface bị release ngay khoảnh khắc này, Exception sẽ bị tóm gọn
                        holder.unlockCanvasAndPost(canvas)
                    } catch (e: IllegalStateException) {
                        Timber.w("Bỏ qua lỗi: Surface đã bị hệ thống thu hồi trước khi kịp unlock.")
                    } catch (e: Exception) {
                        Timber.e(e)
                    }
                }
            }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Bảo vệ touch event nếu view chưa setup xong
        if (!isSetupComplete || !::gestureDetector.isInitialized) {
            return super.dispatchTouchEvent(ev)
        }

        super.dispatchTouchEvent(ev)
        try {
            if (ev.action == MotionEvent.ACTION_DOWN) performClick()
            if (ev.action == MotionEvent.ACTION_UP) endDrag()
        } catch (e: Exception) {
            Timber.e(e)
            return false
        }
        return this.gestureDetector.onTouchEvent(ev)
    }

    companion object {
        internal val NEXT_ID = AtomicLong(0)
    }
}
