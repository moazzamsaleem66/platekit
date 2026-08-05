package com.developer.platekit.android

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import com.developer.platekit.core.template.VehiclePlateLayout
import com.developer.platekit.core.template.VehiclePlateTemplate
import com.developer.platekit.core.template.VehiclePlateTemplates
import kotlin.math.min

/** Draws the plate itself instead of stretching a generic form field across the screen. */
open class VehiclePlateTemplateView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { typeface = Typeface.DEFAULT_BOLD }
    private var template = VehiclePlateTemplates.resolve("QAT")
    private var categoryValue = ""
    private var plateNumber = ""
    var currentNumberMaxLength: Int = 6
        private set

    fun render(
        countryCode: String,
        region: String = "",
        vehicleTypeCode: String = "",
        vehicleTypeName: String = "",
        categoryValue: String = "",
        plateNumber: String = ""
    ) = render(
        VehiclePlateTemplates.resolve(countryCode, region, vehicleTypeCode, vehicleTypeName),
        categoryValue,
        plateNumber
    )

    fun render(template: VehiclePlateTemplate, categoryValue: String, plateNumber: String) {
        val sizeChanged = this.template.widthDp != template.widthDp || this.template.heightDp != template.heightDp
        this.template = template
        this.categoryValue = categoryValue
        this.plateNumber = plateNumber
        currentNumberMaxLength = template.numberMaxLength
        if (sizeChanged) requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(
            resolveSize(dp(template.widthDp), widthMeasureSpec),
            resolveSize(dp(template.heightDp), heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        when (template.layout) {
            VehiclePlateLayout.QATAR -> drawQatar(canvas)
            VehiclePlateLayout.QATAR_DIPLOMATIC -> drawQatarDiplomatic(canvas)
            VehiclePlateLayout.QATAR_POLICE -> drawQatarPolice(canvas)
            VehiclePlateLayout.QATAR_ISF -> drawQatarIsf(canvas)
            VehiclePlateLayout.SAUDI -> drawSaudi(canvas)
            VehiclePlateLayout.UAE_DUBAI -> drawDubai(canvas)
            VehiclePlateLayout.UAE_ABU_DHABI -> drawAbuDhabi(canvas)
            VehiclePlateLayout.UAE_EMIRATE -> drawUaeEmirate(canvas)
            VehiclePlateLayout.KUWAIT -> drawKuwait(canvas)
            VehiclePlateLayout.BAHRAIN -> drawBahrain(canvas)
            VehiclePlateLayout.OMAN -> drawOman(canvas)
            VehiclePlateLayout.GENERIC -> drawGeneric(canvas)
        }
    }

    private fun drawQatar(canvas: Canvas) {
        if (
            template.badgeTop == "حكومي" ||
            template.badgeBottom.equals("GOV.", ignoreCase = true) ||
            template.badgeBottom.equals("COMM.", ignoreCase = true) ||
            template.badgeBottom.equals("LIMO.", ignoreCase = true) ||
            template.badgeBottom.equals("TAXI", ignoreCase = true) ||
            template.badgeBottom.replace('\n', ' ').equals("TEMP. TRANS.", ignoreCase = true)
        ) {
            drawQatarBoxedLabel(canvas)
            return
        }

        val r = plate(canvas, template.backgroundColor, template.textColor)
        val side = r.width() * .18f
        val toothDepth = side * .25f
        val edgeX = r.left + side

        // Qatar's national strip has a serrated white edge, not a straight divider.
        val badgePath = Path().apply {
            moveTo(r.left, r.top)
            lineTo(edgeX, r.top)
            val teeth = 9
            val halfStep = r.height() / (teeth * 2f)
            for (index in 1..teeth * 2) {
                lineTo(if (index % 2 == 0) edgeX else edgeX - toothDepth, r.top + halfStep * index)
            }
            lineTo(r.left, r.bottom)
            close()
        }
        canvas.save()
        val clip = Path().apply { addRoundRect(r, dpF(7f), dpF(7f), Path.Direction.CW) }
        canvas.clipPath(clip)
        paint.style = Paint.Style.FILL
        paint.color = template.badgeColor
        canvas.drawPath(badgePath, paint)
        canvas.restore()

        if (template.badgeTop == "QATAR" && template.badgeBottom.isBlank()) {
            text(canvas, "Q\nA\nT\nA\nR", r.left + (side - toothDepth) * .50f, r.centerY(), side * .34f, r.height() * .80f, template.badgeTextColor)
        } else {
            text(canvas, template.badgeTop, r.left + (side - toothDepth) * .50f, r.top + r.height() * .31f, side * .62f, r.height() * .22f, template.badgeTextColor)
            text(canvas, template.badgeBottom, r.left + (side - toothDepth) * .50f, r.top + r.height() * .70f, side * .66f, r.height() * .18f, template.badgeTextColor)
        }
        val numberLeft = edgeX - toothDepth * .10f
        val numberWidth = r.right - numberLeft
        val numberCenter = numberLeft + numberWidth / 2f
        text(canvas, "قطر", numberCenter, r.top + r.height() * .24f, numberWidth * .48f, r.height() * .25f, template.textColor)
        text(canvas, shownNumber(), numberCenter, r.top + r.height() * .68f, numberWidth * .94f, r.height() * .53f, template.textColor)

        // Reference plate uses a dark inner keyline with a subtle grey outer rim.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(4f)
        paint.color = 0xFF9A9A9A.toInt()
        canvas.drawRoundRect(r, dpF(7f), dpF(7f), paint)
        outline(canvas, r, template.textColor)
    }

    /** Qatar plate variants with a two-row label use the physical boxed-column layout. */
    private fun drawQatarBoxedLabel(canvas: Canvas) {
        val r = plate(canvas, template.backgroundColor, template.textColor)
        val labelWidth = r.width() * .34f
        val dividerX = r.left + labelWidth
        val dividerY = r.centerY()

        // Match the physical plate: a full-height centre divider and two bordered label cells.
        line(canvas, dividerX, r.top, dividerX, r.bottom, template.textColor)
        line(canvas, r.left, dividerY, dividerX, dividerY, template.textColor)

        val topLabelHeight = if ('\n' in template.badgeTop) r.height() * .38f else r.height() * .29f
        val bottomLabelHeight = if ('\n' in template.badgeBottom) r.height() * .38f else r.height() * .28f
        text(
            canvas,
            template.badgeTop,
            r.left + labelWidth / 2f,
            r.top + r.height() * .25f,
            labelWidth * .82f,
            topLabelHeight,
            template.badgeTextColor
        )
        text(
            canvas,
            template.badgeBottom,
            r.left + labelWidth / 2f,
            r.top + r.height() * .75f,
            labelWidth * .82f,
            bottomLabelHeight,
            template.badgeTextColor
        )

        val numberWidth = r.right - dividerX
        val numberCenter = dividerX + numberWidth / 2f
        text(
            canvas,
            "قطر",
            numberCenter,
            r.top + r.height() * .24f,
            numberWidth * .48f,
            r.height() * .25f,
            template.textColor
        )
        text(
            canvas,
            shownNumber(),
            numberCenter,
            r.top + r.height() * .68f,
            numberWidth * .94f,
            r.height() * .53f,
            template.textColor
        )

        // Retain the same outer rim and keyline used by the other Qatar templates.
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(4f)
        paint.color = 0xFF9A9A9A.toInt()
        canvas.drawRoundRect(r, dpF(7f), dpF(7f), paint)
        outline(canvas, r, template.textColor)
    }

    private fun drawQatarDiplomatic(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.RED)
        text(canvas, "قطر", r.left + r.width() * .18f, r.top + r.height() * .22f, r.width() * .22f, r.height() * .18f, Color.RED)
        text(canvas, "هيئة دبلوماسية", r.left + r.width() * .78f, r.top + r.height() * .22f, r.width() * .36f, r.height() * .15f, Color.RED)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .55f, r.width() * .54f, r.height() * .42f, Color.RED)
        text(canvas, "QATAR", r.left + r.width() * .18f, r.top + r.height() * .84f, r.width() * .24f, r.height() * .15f, Color.RED)
        text(canvas, "CD", r.left + r.width() * .84f, r.top + r.height() * .84f, r.width() * .14f, r.height() * .18f, Color.RED)
    }

    private fun drawQatarPolice(canvas: Canvas) {
        val r = plate(canvas, template.backgroundColor, Color.WHITE)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .37f, r.width() * .75f, r.height() * .46f, Color.WHITE)
        val lowerDividerY = r.top + r.height() * .58f
        line(canvas, r.left, lowerDividerY, r.right, lowerDividerY, Color.WHITE)
        line(canvas, r.centerX(), lowerDividerY, r.centerX(), r.bottom, Color.WHITE)
        text(canvas, "قطر", r.left + r.width() * .28f, r.top + r.height() * .79f, r.width() * .28f, r.height() * .22f, Color.WHITE)
        text(canvas, "شرطة", r.left + r.width() * .73f, r.top + r.height() * .79f, r.width() * .28f, r.height() * .22f, Color.WHITE)
    }

    private fun drawQatarIsf(canvas: Canvas) {
        val r = plate(canvas, template.backgroundColor, template.textColor)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .28f, r.width() * .72f, r.height() * .30f, template.textColor)
        line(canvas, r.left, r.top + r.height() * .47f, r.right, r.top + r.height() * .47f, template.textColor)
        text(canvas, "لخويا", r.centerX(), r.top + r.height() * .66f, r.width() * .50f, r.height() * .22f, template.textColor)
        text(canvas, "ISF", r.centerX(), r.top + r.height() * .88f, r.width() * .28f, r.height() * .20f, template.textColor)
    }

    private fun drawSaudi(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.BLACK)
        val badgeWidth = r.width() * .135f
        val badgeLeft = r.right - badgeWidth
        val mainWidth = badgeLeft - r.left
        val middleX = r.left + mainWidth * .55f
        val middleY = r.centerY()

        // Saudi private plates use a bilingual 2x2 grid with a narrow national strip.
        line(canvas, middleX, r.top, middleX, r.bottom, Color.BLACK)
        line(canvas, r.left, middleY, badgeLeft, middleY, Color.BLACK)
        line(canvas, badgeLeft, r.top, badgeLeft, r.bottom, Color.BLACK)

        val number = plateNumber.filter(Char::isDigit).take(4).ifBlank { "7653" }
        val letters = categoryValue.filter(Char::isLetter).uppercase().take(3).ifBlank { "TNJ" }
        val arabicNumber = number.map { digit ->
            if (digit in '0'..'9') "٠١٢٣٤٥٦٧٨٩"[digit - '0'] else digit
        }.joinToString("")
        val arabicLetters = letters.map { letter ->
            mapOf(
                'A' to "ا", 'B' to "ب", 'J' to "ح", 'D' to "د", 'R' to "ر",
                'S' to "س", 'X' to "ص", 'T' to "ط", 'E' to "ع", 'G' to "ق",
                'K' to "ك", 'L' to "ل", 'Z' to "ز", 'N' to "ن", 'H' to "هـ",
                'U' to "و", 'V' to "ى"
            )[letter] ?: letter.toString()
        }.joinToString(" ")

        val numberCenterX = r.left + (middleX - r.left) / 2f
        val lettersCenterX = middleX + (badgeLeft - middleX) / 2f
        text(canvas, arabicNumber, numberCenterX, r.top + r.height() * .27f, (middleX - r.left) * .82f, r.height() * .31f, Color.BLACK)
        text(canvas, arabicLetters, lettersCenterX, r.top + r.height() * .27f, (badgeLeft - middleX) * .82f, r.height() * .27f, Color.BLACK)
        text(canvas, number.toCharArray().joinToString(" "), numberCenterX, r.top + r.height() * .76f, (middleX - r.left) * .86f, r.height() * .36f, Color.BLACK)
        text(canvas, letters.toCharArray().joinToString(" "), lettersCenterX, r.top + r.height() * .76f, (badgeLeft - middleX) * .82f, r.height() * .34f, Color.BLACK)

        val badgeCenterX = badgeLeft + badgeWidth / 2f
        drawSaudiEmblem(canvas, badgeCenterX, r.top + r.height() * .12f, badgeWidth)
        text(canvas, "السعودية", badgeCenterX, r.top + r.height() * .31f, badgeWidth * .82f, r.height() * .09f, Color.BLACK)
        text(canvas, "K\nS\nA", badgeCenterX, r.top + r.height() * .57f, badgeWidth * .55f, r.height() * .30f, Color.BLACK)
        fill(canvas, RectF(badgeLeft + badgeWidth * .25f, r.top + r.height() * .79f, badgeLeft + badgeWidth * .75f, r.top + r.height() * .85f), 0xFFD2D4D8.toInt())
        paint.color = Color.BLACK
        canvas.drawCircle(badgeCenterX, r.top + r.height() * .92f, badgeWidth * .15f, paint)
        outline(canvas, r, Color.BLACK)
    }

    private fun drawSaudiEmblem(canvas: Canvas, cx: Float, cy: Float, width: Float) {
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(1.2f)
        
        // Palm tree
        val palmTop = cy - width * .18f
        val palmBottom = cy + width * .10f
        canvas.drawLine(cx, palmTop, cx, palmBottom, paint)
        
        // Fronds
        val path = Path()
        listOf(-.28f, -.15f, .15f, .28f).forEach { offset ->
            path.moveTo(cx, palmTop + width * .02f)
            path.quadTo(cx + width * offset * 0.5f, palmTop - width * .05f, cx + width * offset, cy - width * .02f)
        }
        canvas.drawPath(path, paint)
        
        // Crossed Swords
        val swordY = cy + width * .18f
        val swordWidth = width * .28f
        // Sword 1 (falling left)
        canvas.drawLine(cx - swordWidth, swordY - width * .05f, cx + swordWidth, swordY + width * .12f, paint)
        // Sword 2 (falling right)
        canvas.drawLine(cx + swordWidth, swordY - width * .05f, cx - swordWidth, swordY + width * .12f, paint)
        
        paint.style = Paint.Style.FILL
    }

    private fun drawDubai(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.BLACK)
        text(canvas, "DUBAI", r.left + r.width() * .21f, r.top + r.height() * .18f, r.width() * .30f, r.height() * .17f, Color.BLACK)
        text(canvas, shownCategory(), r.left + r.width() * .82f, r.top + r.height() * .18f, r.width() * .15f, r.height() * .19f, Color.BLACK)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .63f, r.width() * .78f, r.height() * .55f, Color.BLACK)
    }

    private fun drawAbuDhabi(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.BLACK)
        val header = RectF(r.left, r.top, r.right, r.top + r.height() * .34f)
        fill(canvas, header, template.headerColor)
        text(canvas, "أبوظبي\nA.D", r.left + r.width() * .18f, header.centerY(), r.width() * .28f, header.height() * .62f, Color.WHITE)
        text(canvas, shownCategory(), r.centerX(), header.centerY(), r.width() * .22f, header.height() * .82f, Color.WHITE)
        text(canvas, "الإمارات\nU.A.E", r.left + r.width() * .82f, header.centerY(), r.width() * .30f, header.height() * .60f, Color.WHITE)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .71f, r.width() * .80f, r.height() * .52f, Color.BLACK)
        outline(canvas, r, Color.BLACK)
    }

    private fun drawUaeEmirate(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.BLACK)
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .39f, r.width() * .82f, r.height() * .54f, Color.BLACK)
        line(canvas, r.left, r.top + r.height() * .64f, r.right, r.top + r.height() * .64f, Color.BLACK)
        val emirate = template.footerText.uppercase()
        text(canvas, emirate, r.left + r.width() * .22f, r.top + r.height() * .81f, r.width() * .36f, r.height() * .18f, Color.BLACK)
        text(canvas, shownCategory(), r.centerX(), r.top + r.height() * .81f, r.width() * .13f, r.height() * .24f, Color.BLACK)
        text(canvas, "U.A.E", r.left + r.width() * .80f, r.top + r.height() * .81f, r.width() * .27f, r.height() * .18f, Color.BLACK)
    }

    private fun drawKuwait(canvas: Canvas) {
        val r = plate(canvas, Color.WHITE, Color.BLACK)
        val side = r.width() * .18f
        line(canvas, r.left + side, r.top, r.left + side, r.bottom, Color.BLACK)
        text(canvas, "K\nU\nW\nA\nI\nT", r.left + side / 2, r.centerY(), side * .55f, r.height() * .72f, Color.BLACK)
        text(canvas, shownCategory(), r.left + side + (r.width() - side) * .18f, r.top + r.height() * .20f, r.width() * .15f, r.height() * .20f, Color.BLACK)
        text(canvas, "دولة الكويت", r.left + side + (r.width() - side) * .68f, r.top + r.height() * .20f, r.width() * .39f, r.height() * .17f, Color.BLACK)
        text(canvas, shownNumber(), r.left + side + (r.width() - side) / 2, r.top + r.height() * .68f, (r.width() - side) * .88f, r.height() * .55f, Color.BLACK)
    }

    private fun drawBahrain(canvas: Canvas) {
        val blue = template.textColor
        val r = plate(canvas, Color.WHITE, blue)
        
        // Labels
        text(canvas, "BAHRAIN", r.left + r.width() * .20f, r.top + r.height() * .20f, r.width() * .32f, r.height() * .19f, blue)
        text(canvas, "البحرين", r.left + r.width() * .76f, r.top + r.height() * .20f, r.width() * .34f, r.height() * .18f, blue)
        
        // Flag in the middle
        val flagW = r.width() * .13f
        val flagH = r.height() * .18f
        val flagL = r.centerX() - flagW / 2f
        val flagT = r.top + r.height() * .10f
        val flagR = flagL + flagW
        val flagB = flagT + flagH
        
        // White base
        paint.color = Color.WHITE
        canvas.drawRect(flagL, flagT, flagR, flagB, paint)
        
        // Red part with serrated edge
        paint.color = Color.RED
        val path = Path().apply {
            val serratedX = flagL + flagW * 0.35f
            moveTo(flagR, flagT)
            lineTo(serratedX, flagT)
            val teeth = 5
            val step = flagH / teeth
            for (i in 0 until teeth) {
                lineTo(serratedX + flagW * 0.15f, flagT + step * i + step / 2f)
                lineTo(serratedX, flagT + step * (i + 1))
            }
            lineTo(flagR, flagB)
            close()
        }
        canvas.drawPath(path, paint)
        
        // Flag border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(0.5f)
        paint.color = 0xFFCCCCCC.toInt()
        canvas.drawRect(flagL, flagT, flagR, flagB, paint)
        
        text(canvas, shownNumber(), r.centerX(), r.top + r.height() * .67f, r.width() * .88f, r.height() * .57f, blue)
    }

    private fun drawOman(canvas: Canvas) {
        val r = plate(canvas, template.backgroundColor, Color.BLACK)
        val first = r.left + r.width() * .40f
        val second = r.left + r.width() * .66f
        line(canvas, first, r.top, first, r.bottom, Color.BLACK)
        line(canvas, second, r.top, second, r.bottom, Color.BLACK)
        text(canvas, shownNumber(), r.left + r.width() * .20f, r.centerY(), r.width() * .35f, r.height() * .68f, Color.BLACK)
        text(canvas, shownCategory(), first + (second - first) * .30f, r.centerY(), (second - first) * .48f, r.height() * .62f, Color.BLACK)
        paint.color = 0xFF808080.toInt()
        canvas.drawCircle(second - (second - first) * .18f, r.centerY(), r.height() * .13f, paint)
        text(canvas, "عُمان", second + (r.right - second) / 2, r.centerY(), (r.right - second) * .76f, r.height() * .48f, Color.BLACK)
    }

    private fun drawGeneric(canvas: Canvas) {
        val r = plate(canvas, template.backgroundColor, template.textColor)
        text(canvas, template.badgeTop, r.left + r.width() * .20f, r.centerY(), r.width() * .30f, r.height() * .28f, template.badgeTextColor)
        text(canvas, listOf(categoryValue, plateNumber).filter(String::isNotBlank).joinToString(" - ").ifBlank { "—" }, r.left + r.width() * .66f, r.centerY(), r.width() * .58f, r.height() * .43f, template.textColor)
    }

    private fun plate(canvas: Canvas, fill: Int, stroke: Int): RectF {
        val inset = dpF(2f)
        val r = RectF(inset, inset, width - inset, height - inset)
        paint.style = Paint.Style.FILL
        paint.color = fill
        canvas.drawRoundRect(r, dpF(7f), dpF(7f), paint)
        outline(canvas, r, stroke)
        return r
    }

    private fun outline(canvas: Canvas, r: RectF, color: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(2f)
        paint.color = color
        canvas.drawRoundRect(r, dpF(7f), dpF(7f), paint)
        paint.style = Paint.Style.FILL
    }

    private fun fill(canvas: Canvas, r: RectF, color: Int) {
        paint.style = Paint.Style.FILL
        paint.color = color
        canvas.drawRect(r, paint)
    }

    private fun line(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, color: Int) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = dpF(1.5f)
        paint.color = color
        canvas.drawLine(x1, y1, x2, y2, paint)
        paint.style = Paint.Style.FILL
    }

    private fun text(canvas: Canvas, value: String, cx: Float, cy: Float, maxWidth: Float, maxHeight: Float, color: Int) {
        val lines = value.split('\n')
        var size = maxHeight / lines.size.coerceAtLeast(1)
        paint.typeface = Typeface.DEFAULT_BOLD
        paint.textAlign = Paint.Align.CENTER
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.textSize = size
        val longest = lines.maxOfOrNull { paint.measureText(it) } ?: 0f
        if (longest > maxWidth && longest > 0f) size *= maxWidth / longest
        paint.textSize = min(size, maxHeight / lines.size.coerceAtLeast(1))
        val spacing = paint.textSize * 1.04f
        val firstBaseline = cy - ((lines.size - 1) * spacing / 2f) - (paint.ascent() + paint.descent()) / 2f
        lines.forEachIndexed { index, line -> canvas.drawText(line, cx, firstBaseline + index * spacing, paint) }
    }

    private fun shownCategory() = categoryValue.ifBlank { "—" }
    private fun shownNumber() = plateNumber.ifBlank { "123456" }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun dpF(value: Float) = value * resources.displayMetrics.density
}
