package com.tech.kojo.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tech.kojo.R

/**
 * Custom Button View matching Figma exactly.
 *
 * Usage in XML:
 *   Default (uses colorPrimary):
 *     <com.tech.kojo.utils.GoldenButtonView ... />
 *
 *   Custom color:
 *     <com.tech.kojo.utils.GoldenButtonView
 *         app:buttonColor="@color/red" ... />
 */
class GoldenButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dp = resources.displayMetrics.density
    private val cornerRadius = 12f * dp

    private val rectF = RectF()
    private val clipPath = Path()

    private var buttonColor: Int = ContextCompat.getColor(context, R.color.colorPrimary)

    // Paints
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val topShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(8, 0, 0, 0)
    }

    init {
        attrs?.let {
            val ta = context.obtainStyledAttributes(it, R.styleable.GoldenButtonView)
            buttonColor = ta.getColor(
                R.styleable.GoldenButtonView_buttonColor,
                buttonColor
            )
            ta.recycle()
        }
        basePaint.color = buttonColor
    }

    // Allow dynamic update
    fun setButtonColor(color: Int) {
        buttonColor = color
        basePaint.color = color
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth = (120 * dp).toInt()
        val minHeight = (48 * dp).toInt()

        val width = resolveSize(minWidth, widthMeasureSpec)
        val height = resolveSize(minHeight, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val cr = cornerRadius
        val minSide = minOf(w, h)

        val blurRadius = minSide * 0.15f
        val strokeWidth = minSide * 0.10f

        innerShadowPaint.strokeWidth = strokeWidth
        innerShadowPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)

        clipPath.reset()
        rectF.set(0f, 0f, w, h)
        clipPath.addRoundRect(rectF, cr, cr, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)

        // Base
        canvas.drawRoundRect(rectF, cr, cr, basePaint)

        // Gloss gradient
        gradientPaint.shader = LinearGradient(
            w / 2f, 0f, w / 2f, h,
            intArrayOf(
                Color.argb(0, 255, 255, 255),
                Color.argb(38, 255, 255, 255)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, gradientPaint)

        // Top shadow
        topShadowPaint.shader = LinearGradient(
            0f, 0f, 0f, minSide * 0.4f,
            intArrayOf(
                Color.argb(8, 0, 0, 0),
                Color.argb(0, 0, 0, 0)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, topShadowPaint)

        // Inner shadow
        val inset = strokeWidth / 2f
        val innerCr = (cr - inset).coerceAtLeast(0f)

        canvas.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            innerCr, innerCr,
            innerShadowPaint
        )

        canvas.restore()
    }
}