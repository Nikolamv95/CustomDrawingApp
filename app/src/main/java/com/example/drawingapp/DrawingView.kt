package com.example.drawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import com.google.android.material.textfield.TextInputEditText

class DrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextInputEditText(context, attrs) {

    private var drawPath: LinePath? = null
    private var canvasBitmap: Bitmap? = null
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private var brushSize: Float = 0f
    private var color = Color.BLACK
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<LinePath>()
    private val mUndoPath = ArrayList<LinePath>()

    init {
        setUpDrawing()
    }

    fun setSizeForBrush(newSize: Float) {
        brushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        drawPaint?.strokeWidth ?: brushSize
    }

    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPath.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap!!, 0f, 0f, canvasPaint)

        // Draw the lines on screen
        for (path in mPaths) {
            drawPaint?.strokeWidth = path.brushThickness
            drawPaint?.color = path.color
            canvas.drawPath(path, drawPaint!!)
        }

        if (drawPath?.isEmpty == false) {
            drawPaint?.strokeWidth = drawPath!!.brushThickness
            drawPaint?.color = drawPath!!.color
            canvas.drawPath(drawPath!!, drawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawPath?.color = color
                drawPath?.brushThickness = brushSize
                drawPath?.reset()
                drawPath?.moveTo(touchX, touchY)
            }
            MotionEvent.ACTION_MOVE -> {
                drawPath?.lineTo(touchX, touchY)
            }
            MotionEvent.ACTION_UP -> {
                mPaths.add(drawPath!!)
                drawPath = LinePath(color, brushSize)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    private fun setUpDrawing() {
        drawPaint = Paint()
        drawPath = LinePath(color, brushSize)
        drawPaint?.color = color
        drawPaint?.style = Paint.Style.STROKE
        drawPaint?.strokeJoin = Paint.Join.ROUND
        drawPaint?.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, wprev: Int, hprev: Int) {
        super.onSizeChanged(w, h, wprev, hprev)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(canvasBitmap!!)
    }

    fun setColor(newColor: String) {
        color = Color.parseColor(newColor)
        drawPaint?.color = color
    }

    private class LinePath(var color: Int, var brushThickness: Float) : Path()
}
