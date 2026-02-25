package com.tech.kojo.utils

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.tech.kojo.R

/**
 * Matches Figma design exactly:
 *
 * FILL:
 *   - Layer 1: Linear gradient (top → bottom)
 *       Stop 0%   = #FFFFFF at 0% opacity  (fully transparent white at top)
 *       Stop 100% = #FFFFFF at 100% opacity (fully opaque white at bottom)
 *       Opacity of this entire fill = 15%
 *   - Layer 2: Solid #FFFFFF at 100% opacity
 *
 * EFFECT:
 *   - Inner shadow (very soft, subtle — default Figma inner shadow settings)
 *     Color: black, low opacity, small blur, offset 0,0
 *
 * Result: Mostly white shape, very slightly darker at top due to 15% gradient,
 *         with a barely-visible inner shadow rim around edges.
 */
class RoundedGradientView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val rectF = RectF()
    private val clipPath = Path()

    // ── 1. Solid white base (the #FFFFFF 100% fill layer) ────────────────────
    // To this:
    private val solidWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.white)
        style = Paint.Style.FILL
    }

    // ── 2. Vertical gradient overlay at 15% opacity ───────────────────────────
    //    Top: #FFFFFF 0% alpha  →  Bottom: #FFFFFF 100% alpha
    //    But the whole layer is at 15% opacity in Figma, so:
    //    Top effective alpha   = 0%   of 15%  = 0   → alpha 0
    //    Bottom effective alpha = 100% of 15%  = 15% → alpha ~38
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    // ── 3. Inner shadow — very thin, very soft blur on the inside edges ───────
    //    Figma default inner shadow: color #000000 ~10% opacity, blur ~4px, x=0 y=2
    private val innerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(20, 0, 0, 0)   // ~8% black — very subtle
    }

    // ── 4. Top-edge darkening (simulates the y=2 offset of inner shadow) ──────
    private val topEdgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val minSide = minOf(w, h)

        // Corner radius — 50% of height gives perfect pill-like rounded rect
        // matching the Figma squircle look at this aspect ratio
        val cr = minSide * 0.25f

        // Proportional shadow values
        val blurRadius = minSide * 0.18f
        val strokeWidth = minSide * 0.10f

        innerShadowPaint.strokeWidth = strokeWidth
        innerShadowPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.NORMAL)

        // ── Clip to rounded rect ──────────────────────────────────────────────
        clipPath.reset()
        rectF.set(0f, 0f, w, h)
        clipPath.addRoundRect(rectF, cr, cr, Path.Direction.CW)

        canvas.save()
        canvas.clipPath(clipPath)

        // ── Draw 1: Solid white ───────────────────────────────────────────────
        canvas.drawRoundRect(rectF, cr, cr, solidWhitePaint)

        // ── Draw 2: Vertical gradient overlay (top=transparent → bottom=white)
        //    at effective 15% max opacity — gives the very subtle top darkening
        gradientPaint.shader = LinearGradient(
            w / 2f, 0f,      // start: top-center
            w / 2f, h,       // end:   bottom-center
            intArrayOf(
                Color.argb(0, 200, 200, 200),   // top: transparent grey (no tint)
                Color.argb(38, 255, 255, 255)    // bottom: 15% white (barely visible)
            ), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, gradientPaint)

        // ── Draw 3: Very subtle top-left darkening to match Figma grey corner ─
        topEdgePaint.shader = RadialGradient(
            w * 0.1f, h * 0.1f,           // slightly inside top-left
            maxOf(w, h) * 0.9f, intArrayOf(
                Color.argb(28, 160, 160, 160),    // very light grey at top-left
                Color.argb(10, 200, 200, 200),
                Color.argb(0, 255, 255, 255)     // transparent at bottom-right
            ), floatArrayOf(0f, 0.45f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, topEdgePaint)

        // ── Draw 4: Inner shadow stroke (soft rim around inside of shape) ─────
        val inset = strokeWidth / 2f
        val innerCr = (cr - inset).coerceAtLeast(0f)
        val shadowRect = RectF(inset, inset, w - inset, h - inset)
        canvas.drawRoundRect(shadowRect, innerCr, innerCr, innerShadowPaint)

        // ── Draw 5: Slight top-edge shadow line (Figma inner shadow y=+2) ─────
        topEdgePaint.shader = LinearGradient(
            0f, 0f, 0f, minSide * 0.4f, intArrayOf(
                Color.argb(22, 0, 0, 0),   // very dark at very top inside
                Color.argb(0, 0, 0, 0)    // gone quickly
            ), floatArrayOf(0f, 1f), Shader.TileMode.CLAMP
        )
        canvas.drawRoundRect(rectF, cr, cr, topEdgePaint)

        canvas.restore()
    }
}