package com.soundwave

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CanvasView : View {
    var bitmap: Bitmap? = null
        private set
        get() = if (field != null) field else null
    private var mCanvas: Canvas? = null
    private var mPaint: Paint = Paint()
    private var mPath: Path = Path()

    private var mX: Float = 0f
    private var mY: Float = 0f

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = Color.BLACK
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeWidth = 12f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(bitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(bitmap!!, 0f, 0f, mPaint)
        canvas.drawPath(mPath, mPaint)
    }

    private fun touch_start(x: Float, y: Float) {
        mPath.reset()
        mPath.moveTo(x, y)
        mX = x
        mY = y
    }

    private fun touch_move(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touch_up() {
        mPath.lineTo(mX, mY)
        mCanvas!!.drawPath(mPath, mPaint)
        mPath.reset()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                bitmap = Bitmap.createBitmap(bitmap!!.width, bitmap!!.height, Bitmap.Config.ARGB_8888)
                mCanvas = Canvas(bitmap!!)
                touch_start(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touch_move(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touch_up()
                invalidate()
            }
        }
        return true
    }

    companion object {
        private val TOUCH_TOLERANCE = 1f
    }
}
