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

    private val dp           = resources.displayMetrics.density
    private val cornerRadius = 12f * dp

    private val rectF    = RectF()
    private val clipPath = Path()

    // ── Read custom attr or fallback to colorPrimary ──────────────────────────
    private val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.GoldenButtonView)
            val c  = ta.getColor(
                R.styleable.GoldenButtonView_buttonColor,
                ContextCompat.getColor(context, R.color.colorPrimary)  // default
            )
            ta.recycle()
            c
        } else {
            ContextCompat.getColor(context, R.color.colorPrimary)
        }
        style = Paint.Style.FILL
    }

    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val topShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style  = Paint.Style.STROKE
        color  = Color.argb(8, 0, 0, 0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val cr      = cornerRadius
        val minSide = minOf(w, h)

        val blurRadius  = minSide * 0.15f
        val strokeWidth = minSide * 0.10f

        innerShadowPaint.strokeWidth = strokeWidth
        innerShadowPaint.maskFilter  = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)

        clipPath.reset()
        rectF.set(0f, 0f, w, h)
        clipPath.addRoundRect(rectF, cr, cr, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)

        canvas.drawRoundRect(rectF, cr, cr, basePaint)

        gradientPaint.shader = LinearGradient(
            w / 2f, 0f, w / 2f, h,
            intArrayOf(
                Color.argb(0,  255, 255, 255),
                Color.argb(38, 255, 255, 255)
            ),
            floatArrayOf(0f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, gradientPaint)

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

        val inset   = strokeWidth / 2f
        val innerCr = (cr - inset).coerceAtLeast(0f)
        canvas.drawRoundRect(
            RectF(inset, inset, w - inset, h - inset),
            innerCr, innerCr,
            innerShadowPaint
        )

        canvas.restore()
    }
}