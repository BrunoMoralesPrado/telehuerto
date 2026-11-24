package com.example.telehuerto.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.telehuerto.data.PlantIconType
import com.example.telehuerto.ui.theme.CarrotOrange
import com.example.telehuerto.ui.theme.GarlicCream
import com.example.telehuerto.ui.theme.GreenDark
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.LeafGreen
import com.example.telehuerto.ui.theme.LeafLight
import com.example.telehuerto.ui.theme.LemonYellow
import com.example.telehuerto.ui.theme.LimeAccent
import com.example.telehuerto.ui.theme.OnionPurple
import com.example.telehuerto.ui.theme.PepperRed
import com.example.telehuerto.ui.theme.PotatoTan
import com.example.telehuerto.ui.theme.SoilBrown
import com.example.telehuerto.ui.theme.SoilDark
import com.example.telehuerto.ui.theme.StrawberryRed
import com.example.telehuerto.ui.theme.SurfaceBeige
import com.example.telehuerto.ui.theme.Terracotta
import com.example.telehuerto.ui.theme.TerracottaDark
import com.example.telehuerto.ui.theme.TomatoRed
import kotlin.math.min
import kotlin.math.sqrt

// ===========================================================================
// Logo: brote verde oscuro sobre cuadro lima redondeado
// ===========================================================================

@Composable
fun SproutLogoIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28))
            .background(LimeAccent)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val s = min(w, h)
            val stroke = s * 0.075f

            // Tallo curvo
            val stem = Path().apply {
                moveTo(w * 0.50f, h * 0.80f)
                cubicTo(w * 0.50f, h * 0.66f, w * 0.49f, h * 0.56f, w * 0.50f, h * 0.44f)
            }
            drawPath(stem, GreenPrimary, style = Stroke(width = stroke, cap = StrokeCap.Round))

            // Hoja izquierda (más pequeña)
            drawLeaf(
                base = Offset(w * 0.50f, h * 0.56f),
                tip = Offset(w * 0.22f, h * 0.36f),
                halfWidth = s * 0.11f,
                color = GreenPrimary,
                veinColor = LimeAccent.copy(alpha = 0.7f),
                veinWidth = s * 0.025f
            )
            // Hoja derecha (más grande, apunta arriba)
            drawLeaf(
                base = Offset(w * 0.50f, h * 0.46f),
                tip = Offset(w * 0.82f, h * 0.20f),
                halfWidth = s * 0.14f,
                color = GreenPrimary,
                veinColor = LimeAccent.copy(alpha = 0.7f),
                veinWidth = s * 0.025f
            )
            // Pequeña línea de tierra
            drawLine(
                color = GreenPrimary,
                start = Offset(w * 0.34f, h * 0.81f),
                end = Offset(w * 0.66f, h * 0.81f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

// ===========================================================================
// Hero del Dashboard: maceta de terracota con tres hojas orgánicas
// ===========================================================================

@Composable
fun HeroPottedPlantIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val s = min(w, h)

        // Halo suave detrás de la planta
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(LimeAccent.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.42f),
                radius = s * 0.55f
            ),
            radius = s * 0.55f,
            center = Offset(w * 0.5f, h * 0.42f)
        )

        // --- Tallos ---
        val stemColor = LeafGreen
        val stemStroke = s * 0.035f
        val stemMain = Path().apply {
            moveTo(w * 0.50f, h * 0.64f)
            cubicTo(w * 0.50f, h * 0.50f, w * 0.48f, h * 0.38f, w * 0.52f, h * 0.24f)
        }
        val stemLeft = Path().apply {
            moveTo(w * 0.50f, h * 0.58f)
            quadraticBezierTo(w * 0.42f, h * 0.48f, w * 0.34f, h * 0.44f)
        }
        val stemRight = Path().apply {
            moveTo(w * 0.50f, h * 0.52f)
            quadraticBezierTo(w * 0.60f, h * 0.44f, w * 0.68f, h * 0.40f)
        }
        listOf(stemMain, stemLeft, stemRight).forEach {
            drawPath(it, stemColor, style = Stroke(width = stemStroke, cap = StrokeCap.Round))
        }

        // --- Tres hojas grandes (verde oscuro, lima y verde medio) ---
        drawLeaf(
            base = Offset(w * 0.40f, h * 0.47f),
            tip = Offset(w * 0.10f, h * 0.30f),
            halfWidth = s * 0.12f,
            color = GreenPrimary,
            veinColor = LeafLight.copy(alpha = 0.8f),
            veinWidth = s * 0.012f
        )
        drawLeaf(
            base = Offset(w * 0.62f, h * 0.43f),
            tip = Offset(w * 0.92f, h * 0.24f),
            halfWidth = s * 0.12f,
            color = LimeAccent,
            veinColor = LeafGreen.copy(alpha = 0.7f),
            veinWidth = s * 0.012f
        )
        drawLeaf(
            base = Offset(w * 0.52f, h * 0.30f),
            tip = Offset(w * 0.56f, h * 0.02f),
            halfWidth = s * 0.11f,
            color = LeafLight,
            veinColor = GreenDark.copy(alpha = 0.45f),
            veinWidth = s * 0.012f
        )

        // --- Maceta de terracota ---
        val rimTop = h * 0.62f
        val rimHeight = h * 0.08f
        val rimLeft = w * 0.24f
        val rimRight = w * 0.76f
        val bodyTop = rimTop + rimHeight
        val bodyBottom = h * 0.97f
        val bodyInset = w * 0.07f

        // Sombra en el piso
        drawOval(
            color = Color.Black.copy(alpha = 0.10f),
            topLeft = Offset(w * 0.26f, bodyBottom - h * 0.02f),
            size = Size(w * 0.48f, h * 0.04f)
        )

        val body = Path().apply {
            moveTo(rimLeft + w * 0.02f, bodyTop)
            lineTo(rimRight - w * 0.02f, bodyTop)
            lineTo(rimRight - bodyInset, bodyBottom - h * 0.02f)
            quadraticBezierTo(rimRight - bodyInset, bodyBottom, rimRight - bodyInset - w * 0.03f, bodyBottom)
            lineTo(rimLeft + bodyInset + w * 0.03f, bodyBottom)
            quadraticBezierTo(rimLeft + bodyInset, bodyBottom, rimLeft + bodyInset, bodyBottom - h * 0.02f)
            close()
        }
        drawPath(
            body,
            brush = Brush.horizontalGradient(
                colors = listOf(TerracottaDark, Terracotta, Terracotta, TerracottaDark),
                startX = rimLeft,
                endX = rimRight
            )
        )
        // Brillo vertical
        drawRoundRect(
            color = Color.White.copy(alpha = 0.16f),
            topLeft = Offset(w * 0.36f, bodyTop + h * 0.03f),
            size = Size(w * 0.04f, (bodyBottom - bodyTop) * 0.6f),
            cornerRadius = CornerRadius(w * 0.02f)
        )
        // Borde (aro)
        drawRoundRect(
            color = Terracotta,
            topLeft = Offset(rimLeft, rimTop),
            size = Size(rimRight - rimLeft, rimHeight),
            cornerRadius = CornerRadius(s * 0.03f)
        )
        drawRoundRect(
            color = TerracottaDark.copy(alpha = 0.35f),
            topLeft = Offset(rimLeft, rimTop + rimHeight * 0.72f),
            size = Size(rimRight - rimLeft, rimHeight * 0.28f),
            cornerRadius = CornerRadius(s * 0.02f)
        )
        // Tierra visible
        drawOval(
            color = SoilDark,
            topLeft = Offset(rimLeft + w * 0.03f, rimTop - h * 0.015f),
            size = Size(rimRight - rimLeft - w * 0.06f, h * 0.035f)
        )
    }
}

// ===========================================================================
// Ilustración vectorial por tipo de planta (14 especies de huerto)
// ===========================================================================

@Composable
fun PlantPotIllustration(iconType: PlantIconType, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22))
            .background(SurfaceBeige)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
        ) {
            drawSoilMound()
            when (iconType) {
                PlantIconType.TOMATE -> drawTomatoPlant()
                PlantIconType.ALBAHACA -> drawBasilPlant()
                PlantIconType.PIMIENTO -> drawPepperPlant()
                PlantIconType.CEBOLLA -> drawOnionPlant()
                PlantIconType.PAPA -> drawPotatoPlant()
                PlantIconType.MANZANA -> drawApplePlant()
                PlantIconType.ZANAHORIA -> drawCarrotPlant()
                PlantIconType.FRESA -> drawStrawberryPlant()
                PlantIconType.LECHUGA -> drawLettucePlant()
                PlantIconType.MENTA -> drawMintPlant()
                PlantIconType.CHILE -> drawChiliPlant()
                PlantIconType.LIMON -> drawLemonPlant()
                PlantIconType.AJO -> drawGarlicPlant()
                PlantIconType.GENERIC -> drawSproutPlant()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas base de dibujo
// ---------------------------------------------------------------------------

/** Montículo de tierra fértil en la base del lienzo. */
private fun DrawScope.drawSoilMound() {
    val w = size.width
    val h = size.height
    val mound = Path().apply {
        moveTo(w * 0.12f, h * 0.86f)
        cubicTo(w * 0.24f, h * 0.70f, w * 0.76f, h * 0.70f, w * 0.88f, h * 0.86f)
        quadraticBezierTo(w * 0.90f, h * 0.90f, w * 0.84f, h * 0.90f)
        lineTo(w * 0.16f, h * 0.90f)
        quadraticBezierTo(w * 0.10f, h * 0.90f, w * 0.12f, h * 0.86f)
        close()
    }
    drawPath(mound, SoilBrown)
    val dot = min(w, h) * 0.018f
    listOf(
        0.30f to 0.83f,
        0.44f to 0.80f,
        0.58f to 0.84f,
        0.70f to 0.81f,
        0.38f to 0.87f,
        0.64f to 0.875f
    ).forEach { (x, y) ->
        drawCircle(SoilDark, radius = dot, center = Offset(w * x, h * y))
    }
}

private fun DrawScope.drawStem(points: List<Offset>, width: Float, color: Color = LeafGreen) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        when {
            points.size == 3 -> quadraticBezierTo(points[1].x, points[1].y, points[2].x, points[2].y)
            points.size >= 4 -> cubicTo(
                points[1].x,
                points[1].y,
                points[2].x,
                points[2].y,
                points[3].x,
                points[3].y
            )
            else -> lineTo(points[1].x, points[1].y)
        }
    }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round))
}

// ---------------------------------------------------------------------------
// 1. TOMATE CHERRY
// ---------------------------------------------------------------------------

private fun DrawScope.drawTomatoPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    drawStem(
        listOf(
            Offset(w * 0.50f, h * 0.76f),
            Offset(w * 0.48f, h * 0.55f),
            Offset(w * 0.52f, h * 0.38f),
            Offset(w * 0.50f, h * 0.16f)
        ),
        s * 0.035f
    )
    drawStem(
        listOf(
            Offset(w * 0.50f, h * 0.48f),
            Offset(w * 0.38f, h * 0.44f),
            Offset(w * 0.30f, h * 0.50f)
        ),
        s * 0.025f
    )
    drawStem(
        listOf(
            Offset(w * 0.51f, h * 0.36f),
            Offset(w * 0.64f, h * 0.34f),
            Offset(w * 0.70f, h * 0.42f)
        ),
        s * 0.025f
    )

    drawLeaf(Offset(w * 0.50f, h * 0.60f), Offset(w * 0.20f, h * 0.56f), s * 0.07f, LeafGreen, GreenDark.copy(alpha = 0.4f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.54f), Offset(w * 0.80f, h * 0.50f), s * 0.07f, GreenPrimary, LeafLight.copy(alpha = 0.6f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.30f, h * 0.14f), s * 0.06f, LeafLight, GreenDark.copy(alpha = 0.35f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.22f), Offset(w * 0.70f, h * 0.10f), s * 0.06f, LeafGreen, GreenDark.copy(alpha = 0.35f), s * 0.01f)

    drawTomato(Offset(w * 0.30f, h * 0.55f), s * 0.085f)
    drawTomato(Offset(w * 0.40f, h * 0.60f), s * 0.075f)
    drawTomato(Offset(w * 0.70f, h * 0.47f), s * 0.085f)
    drawTomato(Offset(w * 0.61f, h * 0.40f), s * 0.065f)
}

private fun DrawScope.drawTomato(center: Offset, r: Float) {
    drawCircle(TomatoRed, radius = r, center = center)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
            radius = r * 1.4f
        ),
        radius = r,
        center = center
    )
    drawCircle(
        Color.White.copy(alpha = 0.55f),
        radius = r * 0.22f,
        center = Offset(center.x - r * 0.38f, center.y - r * 0.36f)
    )
    val calyx = r * 0.55f
    for (i in 0 until 5) {
        rotate(degrees = i * 72f, pivot = Offset(center.x, center.y - r * 0.82f)) {
            drawLine(
                color = GreenPrimary,
                start = Offset(center.x, center.y - r * 0.82f),
                end = Offset(center.x, center.y - r * 0.82f - calyx * 0.6f),
                strokeWidth = r * 0.18f,
                cap = StrokeCap.Round
            )
        }
    }
}

// ---------------------------------------------------------------------------
// 2. ALBAHACA DULCE
// ---------------------------------------------------------------------------

private fun DrawScope.drawBasilPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    drawStem(listOf(Offset(w * 0.50f, h * 0.76f), Offset(w * 0.50f, h * 0.55f), Offset(w * 0.50f, h * 0.36f)), s * 0.035f)
    drawStem(listOf(Offset(w * 0.48f, h * 0.74f), Offset(w * 0.40f, h * 0.62f), Offset(w * 0.32f, h * 0.56f)), s * 0.028f)
    drawStem(listOf(Offset(w * 0.52f, h * 0.74f), Offset(w * 0.60f, h * 0.62f), Offset(w * 0.68f, h * 0.56f)), s * 0.028f)

    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.012f
    drawLeaf(Offset(w * 0.50f, h * 0.44f), Offset(w * 0.50f, h * 0.10f), s * 0.12f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.48f, h * 0.46f), Offset(w * 0.20f, h * 0.24f), s * 0.11f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.52f, h * 0.46f), Offset(w * 0.80f, h * 0.24f), s * 0.11f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.34f, h * 0.60f), Offset(w * 0.10f, h * 0.52f), s * 0.10f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.66f, h * 0.60f), Offset(w * 0.90f, h * 0.52f), s * 0.10f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.46f, h * 0.56f), Offset(w * 0.34f, h * 0.34f), s * 0.09f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.54f, h * 0.56f), Offset(w * 0.66f, h * 0.34f), s * 0.09f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.40f), Offset(w * 0.46f, h * 0.24f), s * 0.05f, LimeAccent, null, 0f)
    drawLeaf(Offset(w * 0.50f, h * 0.40f), Offset(w * 0.55f, h * 0.25f), s * 0.05f, LimeAccent, null, 0f)
}

// ---------------------------------------------------------------------------
// 3. PIMIENTO MORRÓN
// ---------------------------------------------------------------------------

private fun DrawScope.drawPepperPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    drawStem(
        listOf(
            Offset(w * 0.46f, h * 0.76f),
            Offset(w * 0.44f, h * 0.56f),
            Offset(w * 0.46f, h * 0.38f),
            Offset(w * 0.44f, h * 0.18f)
        ),
        s * 0.035f
    )
    drawStem(
        listOf(
            Offset(w * 0.45f, h * 0.40f),
            Offset(w * 0.56f, h * 0.34f),
            Offset(w * 0.64f, h * 0.36f)
        ),
        s * 0.028f
    )

    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.01f
    drawLeaf(Offset(w * 0.45f, h * 0.60f), Offset(w * 0.14f, h * 0.52f), s * 0.07f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.45f, h * 0.46f), Offset(w * 0.18f, h * 0.32f), s * 0.065f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.45f, h * 0.26f), Offset(w * 0.24f, h * 0.10f), s * 0.06f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.45f, h * 0.22f), Offset(w * 0.66f, h * 0.08f), s * 0.06f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.60f, h * 0.35f), Offset(w * 0.86f, h * 0.24f), s * 0.06f, LeafLight, vein, vw)

    drawBellPepper(top = Offset(w * 0.64f, h * 0.40f), width = s * 0.30f, height = s * 0.34f)
}

private fun DrawScope.drawBellPepper(top: Offset, width: Float, height: Float) {
    val cx = top.x
    val y0 = top.y + height * 0.10f
    val left = cx - width / 2f
    val right = cx + width / 2f
    val bottom = y0 + height

    val body = Path().apply {
        moveTo(cx, y0)
        cubicTo(
            right + width * 0.05f,
            y0 - height * 0.02f,
            right + width * 0.06f,
            y0 + height * 0.55f,
            right - width * 0.06f,
            bottom - height * 0.08f
        )
        quadraticBezierTo(right - width * 0.18f, bottom + height * 0.02f, cx + width * 0.16f, bottom - height * 0.06f)
        quadraticBezierTo(cx, bottom + height * 0.04f, cx - width * 0.16f, bottom - height * 0.06f)
        quadraticBezierTo(left + width * 0.18f, bottom + height * 0.02f, left + width * 0.06f, bottom - height * 0.08f)
        cubicTo(
            left - width * 0.06f,
            y0 + height * 0.55f,
            left - width * 0.05f,
            y0 - height * 0.02f,
            cx,
            y0
        )
        close()
    }
    drawPath(
        body,
        brush = Brush.horizontalGradient(
            colors = listOf(PepperRed.copy(red = 0.70f), PepperRed, TomatoRed, PepperRed.copy(red = 0.66f)),
            startX = left,
            endX = right
        )
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.12f),
        start = Offset(cx - width * 0.12f, y0 + height * 0.18f),
        end = Offset(cx - width * 0.14f, bottom - height * 0.14f),
        strokeWidth = width * 0.04f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.Black.copy(alpha = 0.12f),
        start = Offset(cx + width * 0.12f, y0 + height * 0.18f),
        end = Offset(cx + width * 0.14f, bottom - height * 0.14f),
        strokeWidth = width * 0.04f,
        cap = StrokeCap.Round
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.45f),
        topLeft = Offset(left + width * 0.16f, y0 + height * 0.16f),
        size = Size(width * 0.09f, height * 0.36f),
        cornerRadius = CornerRadius(width * 0.05f)
    )
    drawOval(
        color = GreenPrimary,
        topLeft = Offset(cx - width * 0.24f, y0 - height * 0.07f),
        size = Size(width * 0.48f, height * 0.14f)
    )
    drawLine(
        color = GreenPrimary,
        start = Offset(cx, y0 - height * 0.02f),
        end = Offset(cx + width * 0.04f, top.y - height * 0.06f),
        strokeWidth = width * 0.08f,
        cap = StrokeCap.Round
    )
}

// ---------------------------------------------------------------------------
// 4. CEBOLLA MORADA
// ---------------------------------------------------------------------------

private fun DrawScope.drawOnionPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tallos tubulares verdes que emergen del cuello del bulbo
    drawStem(listOf(Offset(w * 0.50f, h * 0.52f), Offset(w * 0.49f, h * 0.32f), Offset(w * 0.52f, h * 0.12f)), s * 0.045f, GreenPrimary)
    drawStem(listOf(Offset(w * 0.47f, h * 0.54f), Offset(w * 0.36f, h * 0.34f), Offset(w * 0.26f, h * 0.18f)), s * 0.04f, LeafGreen)
    drawStem(listOf(Offset(w * 0.53f, h * 0.54f), Offset(w * 0.64f, h * 0.35f), Offset(w * 0.74f, h * 0.20f)), s * 0.04f, LeafLight)
    drawStem(listOf(Offset(w * 0.49f, h * 0.53f), Offset(w * 0.42f, h * 0.36f), Offset(w * 0.38f, h * 0.16f)), s * 0.032f, LimeAccent)

    // Bulbo de cebolla morada con forma de gota invertida sobre la tierra
    val bulb = Path().apply {
        moveTo(w * 0.50f, h * 0.44f)
        cubicTo(w * 0.68f, h * 0.52f, w * 0.74f, h * 0.68f, w * 0.64f, h * 0.79f)
        quadraticBezierTo(w * 0.50f, h * 0.84f, w * 0.36f, h * 0.79f)
        cubicTo(w * 0.26f, h * 0.68f, w * 0.32f, h * 0.52f, w * 0.50f, h * 0.44f)
        close()
    }
    drawPath(
        bulb,
        brush = Brush.horizontalGradient(
            colors = listOf(OnionPurple.copy(alpha = 0.85f), OnionPurple, Color(0xFFC26D96), OnionPurple),
            startX = w * 0.28f,
            endX = w * 0.72f
        )
    )
    // Líneas verticales de las túnicas de la cebolla
    listOf(-0.09f, 0f, 0.09f).forEach { offsetFactor ->
        val stripe = Path().apply {
            moveTo(w * 0.50f, h * 0.47f)
            quadraticBezierTo(w * (0.50f + offsetFactor * 1.6f), h * 0.64f, w * (0.50f + offsetFactor * 0.7f), h * 0.79f)
        }
        drawPath(stripe, Color.White.copy(alpha = 0.25f), style = Stroke(width = s * 0.015f, cap = StrokeCap.Round))
    }
    // Brillo lateral
    drawOval(
        color = Color.White.copy(alpha = 0.38f),
        topLeft = Offset(w * 0.38f, h * 0.57f),
        size = Size(w * 0.06f, h * 0.13f)
    )
}

// ---------------------------------------------------------------------------
// 5. PAPA ANDINA
// ---------------------------------------------------------------------------

private fun DrawScope.drawPotatoPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Follaje tupido superior
    drawStem(listOf(Offset(w * 0.50f, h * 0.70f), Offset(w * 0.50f, h * 0.46f), Offset(w * 0.50f, h * 0.22f)), s * 0.035f)
    drawStem(listOf(Offset(w * 0.50f, h * 0.52f), Offset(w * 0.36f, h * 0.42f), Offset(w * 0.24f, h * 0.34f)), s * 0.026f)
    drawStem(listOf(Offset(w * 0.50f, h * 0.48f), Offset(w * 0.64f, h * 0.40f), Offset(w * 0.76f, h * 0.32f)), s * 0.026f)

    val vein = GreenDark.copy(alpha = 0.35f)
    drawLeaf(Offset(w * 0.48f, h * 0.48f), Offset(w * 0.18f, h * 0.34f), s * 0.085f, LeafGreen, vein, s * 0.01f)
    drawLeaf(Offset(w * 0.52f, h * 0.46f), Offset(w * 0.82f, h * 0.32f), s * 0.085f, GreenPrimary, LeafLight.copy(alpha = 0.5f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.34f), Offset(w * 0.30f, h * 0.16f), s * 0.075f, LeafLight, vein, s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.32f), Offset(w * 0.70f, h * 0.16f), s * 0.075f, LeafGreen, vein, s * 0.01f)

    // Pequeña flor blanca de papa con centro amarillo
    val flowerCenter = Offset(w * 0.50f, h * 0.18f)
    for (i in 0 until 5) {
        rotate(degrees = i * 72f, pivot = flowerCenter) {
            drawCircle(Color.White, radius = s * 0.036f, center = Offset(flowerCenter.x, flowerCenter.y - s * 0.038f))
        }
    }
    drawCircle(LemonYellow, radius = s * 0.025f, center = flowerCenter)

    // Dos tubérculos de papa dorada asomando en la tierra
    drawPotatoTuber(center = Offset(w * 0.36f, h * 0.72f), rx = s * 0.14f, ry = s * 0.10f, angle = -12f)
    drawPotatoTuber(center = Offset(w * 0.62f, h * 0.73f), rx = s * 0.15f, ry = s * 0.105f, angle = 14f)
}

private fun DrawScope.drawPotatoTuber(center: Offset, rx: Float, ry: Float, angle: Float) {
    rotate(degrees = angle, pivot = center) {
        drawOval(
            color = PotatoTan,
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2f, ry * 2f)
        )
        drawOval(
            color = SoilBrown.copy(alpha = 0.25f),
            topLeft = Offset(center.x - rx, center.y - ry * 0.2f),
            size = Size(rx * 2f, ry * 1.2f)
        )
        // Brillo suave y "ojitos" del tubérculo
        drawOval(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(center.x - rx * 0.55f, center.y - ry * 0.65f),
            size = Size(rx * 0.65f, ry * 0.42f)
        )
        drawCircle(SoilDark.copy(alpha = 0.55f), radius = rx * 0.08f, center = Offset(center.x - rx * 0.3f, center.y + ry * 0.1f))
        drawCircle(SoilDark.copy(alpha = 0.55f), radius = rx * 0.07f, center = Offset(center.x + rx * 0.35f, center.y - ry * 0.15f))
    }
}

// ---------------------------------------------------------------------------
// 6. MANZANO ENANO
// ---------------------------------------------------------------------------

private fun DrawScope.drawApplePlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tronco leñoso y ramas
    drawStem(listOf(Offset(w * 0.50f, h * 0.78f), Offset(w * 0.49f, h * 0.58f), Offset(w * 0.50f, h * 0.40f)), s * 0.055f, SoilBrown)
    drawStem(listOf(Offset(w * 0.50f, h * 0.50f), Offset(w * 0.36f, h * 0.42f)), s * 0.035f, SoilBrown)
    drawStem(listOf(Offset(w * 0.50f, h * 0.46f), Offset(w * 0.65f, h * 0.38f)), s * 0.035f, SoilBrown)

    // Copa frondosa del manzano enano
    drawCircle(GreenPrimary, radius = s * 0.20f, center = Offset(w * 0.34f, h * 0.34f))
    drawCircle(LeafGreen, radius = s * 0.21f, center = Offset(w * 0.66f, h * 0.34f))
    drawCircle(LeafLight, radius = s * 0.22f, center = Offset(w * 0.50f, h * 0.24f))
    drawCircle(LimeAccent.copy(alpha = 0.55f), radius = s * 0.11f, center = Offset(w * 0.46f, h * 0.18f))

    // Manzanas rojas brillantes en la copa
    drawAppleFruit(center = Offset(w * 0.34f, h * 0.38f), r = s * 0.088f)
    drawAppleFruit(center = Offset(w * 0.65f, h * 0.39f), r = s * 0.092f)
    drawAppleFruit(center = Offset(w * 0.51f, h * 0.24f), r = s * 0.080f)
}

private fun DrawScope.drawAppleFruit(center: Offset, r: Float) {
    // Dos lóbulos ligeramente superpuestos para silueta de manzana
    drawCircle(StrawberryRed, radius = r * 0.92f, center = Offset(center.x - r * 0.22f, center.y))
    drawCircle(TomatoRed, radius = r * 0.92f, center = Offset(center.x + r * 0.22f, center.y))
    drawCircle(
        Color.White.copy(alpha = 0.55f),
        radius = r * 0.22f,
        center = Offset(center.x - r * 0.42f, center.y - r * 0.32f)
    )
    // Pedúnculo y hojita
    drawLine(
        color = SoilDark,
        start = Offset(center.x, center.y - r * 0.72f),
        end = Offset(center.x + r * 0.12f, center.y - r * 1.15f),
        strokeWidth = r * 0.18f,
        cap = StrokeCap.Round
    )
    drawLeaf(
        base = Offset(center.x + r * 0.05f, center.y - r * 0.88f),
        tip = Offset(center.x + r * 0.65f, center.y - r * 1.12f),
        halfWidth = r * 0.22f,
        color = LimeAccent,
        veinColor = null,
        veinWidth = 0f
    )
}

// ---------------------------------------------------------------------------
// 7. ZANAHORIA
// ---------------------------------------------------------------------------

private fun DrawScope.drawCarrotPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Penacho frondoso de hojas de zanahoria
    drawStem(listOf(Offset(w * 0.50f, h * 0.46f), Offset(w * 0.50f, h * 0.26f), Offset(w * 0.50f, h * 0.10f)), s * 0.032f, LeafGreen)
    drawStem(listOf(Offset(w * 0.48f, h * 0.46f), Offset(w * 0.35f, h * 0.28f), Offset(w * 0.22f, h * 0.16f)), s * 0.028f, GreenPrimary)
    drawStem(listOf(Offset(w * 0.52f, h * 0.46f), Offset(w * 0.65f, h * 0.28f), Offset(w * 0.78f, h * 0.16f)), s * 0.028f, LeafLight)

    drawLeaf(Offset(w * 0.50f, h * 0.36f), Offset(w * 0.50f, h * 0.08f), s * 0.075f, LeafLight, null, 0f)
    drawLeaf(Offset(w * 0.45f, h * 0.40f), Offset(w * 0.20f, h * 0.16f), s * 0.075f, LeafGreen, null, 0f)
    drawLeaf(Offset(w * 0.55f, h * 0.40f), Offset(w * 0.80f, h * 0.16f), s * 0.075f, GreenPrimary, null, 0f)
    drawLeaf(Offset(w * 0.42f, h * 0.44f), Offset(w * 0.16f, h * 0.34f), s * 0.06f, LimeAccent, null, 0f)
    drawLeaf(Offset(w * 0.58f, h * 0.44f), Offset(w * 0.84f, h * 0.34f), s * 0.06f, LeafLight, null, 0f)

    // Cuerpo cónico naranja de la zanahoria emergiendo de la tierra
    val carrot = Path().apply {
        moveTo(w * 0.36f, h * 0.48f)
        quadraticBezierTo(w * 0.50f, h * 0.42f, w * 0.64f, h * 0.48f)
        cubicTo(w * 0.66f, h * 0.58f, w * 0.58f, h * 0.74f, w * 0.50f, h * 0.83f)
        cubicTo(w * 0.42f, h * 0.74f, w * 0.34f, h * 0.58f, w * 0.36f, h * 0.48f)
        close()
    }
    drawPath(
        carrot,
        brush = Brush.horizontalGradient(
            colors = listOf(CarrotOrange, Color(0xFFF6944D), CarrotOrange, TerracottaDark),
            startX = w * 0.35f,
            endX = w * 0.65f
        )
    )
    // Surcos horizontales característicos de la zanahoria
    listOf(0.55f, 0.63f, 0.71f).forEachIndexed { idx, yRatio ->
        val span = 0.08f - idx * 0.015f
        drawLine(
            color = TerracottaDark.copy(alpha = 0.45f),
            start = Offset(w * (0.48f - span), h * yRatio),
            end = Offset(w * (0.50f + span * 0.6f), h * yRatio),
            strokeWidth = s * 0.016f,
            cap = StrokeCap.Round
        )
    }
    // Brillo vertical
    drawRoundRect(
        color = Color.White.copy(alpha = 0.38f),
        topLeft = Offset(w * 0.41f, h * 0.51f),
        size = Size(w * 0.04f, h * 0.17f),
        cornerRadius = CornerRadius(w * 0.02f)
    )
}

// ---------------------------------------------------------------------------
// 8. FRESA
// ---------------------------------------------------------------------------

private fun DrawScope.drawStrawberryPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tallos arqueados y hojas trifoliadas
    drawStem(listOf(Offset(w * 0.50f, h * 0.74f), Offset(w * 0.36f, h * 0.46f), Offset(w * 0.28f, h * 0.54f)), s * 0.028f)
    drawStem(listOf(Offset(w * 0.50f, h * 0.74f), Offset(w * 0.64f, h * 0.44f), Offset(w * 0.72f, h * 0.52f)), s * 0.028f)

    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.01f
    drawLeaf(Offset(w * 0.50f, h * 0.52f), Offset(w * 0.50f, h * 0.16f), s * 0.105f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.46f, h * 0.52f), Offset(w * 0.16f, h * 0.28f), s * 0.095f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.54f, h * 0.52f), Offset(w * 0.84f, h * 0.28f), s * 0.095f, LeafLight, vein, vw)

    // Dos fresas jugosas con aquenios dorados
    drawStrawberryFruit(top = Offset(w * 0.30f, h * 0.50f), radius = s * 0.125f)
    drawStrawberryFruit(top = Offset(w * 0.68f, h * 0.48f), radius = s * 0.135f)

    // Florecita blanca de fresa al centro
    val flowerPos = Offset(w * 0.50f, h * 0.40f)
    for (i in 0 until 5) {
        rotate(degrees = i * 72f, pivot = flowerPos) {
            drawCircle(Color.White, radius = s * 0.032f, center = Offset(flowerPos.x, flowerPos.y - s * 0.034f))
        }
    }
    drawCircle(LemonYellow, radius = s * 0.024f, center = flowerPos)
}

private fun DrawScope.drawStrawberryFruit(top: Offset, radius: Float) {
    val cx = top.x
    val y0 = top.y
    val berry = Path().apply {
        moveTo(cx, y0 + radius * 0.1f)
        cubicTo(cx + radius * 1.15f, y0 - radius * 0.05f, cx + radius * 1.05f, y0 + radius * 1.15f, cx, y0 + radius * 1.65f)
        cubicTo(cx - radius * 1.05f, y0 + radius * 1.15f, cx - radius * 1.15f, y0 - radius * 0.05f, cx, y0 + radius * 0.1f)
        close()
    }
    drawPath(berry, StrawberryRed)
    // Semillitas doradas
    val seedR = radius * 0.075f
    listOf(
        -0.38f to 0.48f,
        0.0f to 0.45f,
        0.38f to 0.50f,
        -0.24f to 0.88f,
        0.24f to 0.88f,
        0.0f to 1.22f
    ).forEach { (dx, dy) ->
        drawCircle(LemonYellow, radius = seedR, center = Offset(cx + radius * dx, y0 + radius * dy))
    }
    // Sépalos verdes superiores
    drawLeaf(Offset(cx, y0 + radius * 0.15f), Offset(cx - radius * 0.55f, y0 - radius * 0.10f), radius * 0.18f, GreenPrimary, null, 0f)
    drawLeaf(Offset(cx, y0 + radius * 0.15f), Offset(cx + radius * 0.55f, y0 - radius * 0.10f), radius * 0.18f, GreenPrimary, null, 0f)
    drawLeaf(Offset(cx, y0 + radius * 0.15f), Offset(cx, y0 - radius * 0.25f), radius * 0.16f, LimeAccent, null, 0f)
}

// ---------------------------------------------------------------------------
// 9. LECHUGA
// ---------------------------------------------------------------------------

private fun DrawScope.drawLettucePlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Capas exteriores de hojas rizadas
    val vein = LimeAccent.copy(alpha = 0.75f)
    val vw = s * 0.015f
    drawLeaf(Offset(w * 0.46f, h * 0.74f), Offset(w * 0.12f, h * 0.42f), s * 0.15f, GreenPrimary, vein, vw)
    drawLeaf(Offset(w * 0.54f, h * 0.74f), Offset(w * 0.88f, h * 0.42f), s * 0.15f, GreenPrimary, vein, vw)
    drawLeaf(Offset(w * 0.48f, h * 0.74f), Offset(w * 0.24f, h * 0.20f), s * 0.16f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.52f, h * 0.74f), Offset(w * 0.76f, h * 0.20f), s * 0.16f, LeafGreen, vein, vw)

    // Cogollo central tierno en tonos verde claro y lima
    drawLeaf(Offset(w * 0.50f, h * 0.74f), Offset(w * 0.50f, h * 0.14f), s * 0.17f, LeafLight, Color.White.copy(alpha = 0.55f), vw * 1.2f)
    drawLeaf(Offset(w * 0.49f, h * 0.74f), Offset(w * 0.36f, h * 0.30f), s * 0.12f, LimeAccent, Color.White.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.51f, h * 0.74f), Offset(w * 0.64f, h * 0.30f), s * 0.12f, LimeAccent, Color.White.copy(alpha = 0.5f), vw)
}

// ---------------------------------------------------------------------------
// 10. MENTA PIPERITA
// ---------------------------------------------------------------------------

private fun DrawScope.drawMintPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tres tallos erguidos con pares de hojas opuestas aromáticas
    drawStem(listOf(Offset(w * 0.50f, h * 0.76f), Offset(w * 0.50f, h * 0.44f), Offset(w * 0.50f, h * 0.12f)), s * 0.032f, GreenPrimary)
    drawStem(listOf(Offset(w * 0.44f, h * 0.76f), Offset(w * 0.34f, h * 0.54f), Offset(w * 0.26f, h * 0.34f)), s * 0.026f, LeafGreen)
    drawStem(listOf(Offset(w * 0.56f, h * 0.76f), Offset(w * 0.66f, h * 0.54f), Offset(w * 0.74f, h * 0.34f)), s * 0.026f, LeafGreen)

    val vein = LimeAccent.copy(alpha = 0.65f)
    val vw = s * 0.011f
    // Par inferior
    drawLeaf(Offset(w * 0.50f, h * 0.62f), Offset(w * 0.20f, h * 0.52f), s * 0.085f, GreenPrimary, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.62f), Offset(w * 0.80f, h * 0.52f), s * 0.085f, GreenPrimary, vein, vw)
    // Par medio
    drawLeaf(Offset(w * 0.50f, h * 0.44f), Offset(w * 0.22f, h * 0.30f), s * 0.085f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.44f), Offset(w * 0.78f, h * 0.30f), s * 0.085f, LeafGreen, vein, vw)
    // Par superior
    drawLeaf(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.30f, h * 0.14f), s * 0.072f, LeafLight, GreenDark.copy(alpha = 0.35f), vw)
    drawLeaf(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.70f, h * 0.14f), s * 0.072f, LeafLight, GreenDark.copy(alpha = 0.35f), vw)
    // Ápice fresco color lima
    drawLeaf(Offset(w * 0.50f, h * 0.20f), Offset(w * 0.50f, h * 0.06f), s * 0.055f, LimeAccent, null, 0f)
}

// ---------------------------------------------------------------------------
// 11. CHILE SERRANO
// ---------------------------------------------------------------------------

private fun DrawScope.drawChiliPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    drawStem(
        listOf(
            Offset(w * 0.50f, h * 0.76f),
            Offset(w * 0.48f, h * 0.54f),
            Offset(w * 0.52f, h * 0.34f),
            Offset(w * 0.50f, h * 0.14f)
        ),
        s * 0.034f,
        GreenPrimary
    )
    drawStem(listOf(Offset(w * 0.49f, h * 0.48f), Offset(w * 0.34f, h * 0.42f)), s * 0.025f, GreenPrimary)
    drawStem(listOf(Offset(w * 0.51f, h * 0.38f), Offset(w * 0.68f, h * 0.34f)), s * 0.025f, GreenPrimary)

    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.01f
    drawLeaf(Offset(w * 0.48f, h * 0.58f), Offset(w * 0.16f, h * 0.50f), s * 0.065f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.52f, h * 0.54f), Offset(w * 0.84f, h * 0.46f), s * 0.065f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.26f), Offset(w * 0.24f, h * 0.12f), s * 0.06f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.22f), Offset(w * 0.76f, h * 0.10f), s * 0.06f, LeafGreen, vein, vw)

    // Chiles serranos rojos curvos colgando
    drawChiliPod(top = Offset(w * 0.34f, h * 0.42f), length = s * 0.28f, width = s * 0.095f, curveDir = -1f)
    drawChiliPod(top = Offset(w * 0.68f, h * 0.34f), length = s * 0.30f, width = s * 0.10f, curveDir = 1f)
}

private fun DrawScope.drawChiliPod(top: Offset, length: Float, width: Float, curveDir: Float) {
    val pod = Path().apply {
        moveTo(top.x - width * 0.45f, top.y)
        quadraticBezierTo(top.x, top.y - width * 0.2f, top.x + width * 0.45f, top.y)
        cubicTo(
            top.x + width * 0.55f + curveDir * width * 0.3f,
            top.y + length * 0.45f,
            top.x + curveDir * width * 0.95f,
            top.y + length * 0.85f,
            top.x + curveDir * width * 1.15f,
            top.y + length
        )
        cubicTo(
            top.x + curveDir * width * 0.35f,
            top.y + length * 0.85f,
            top.x - width * 0.55f + curveDir * width * 0.1f,
            top.y + length * 0.45f,
            top.x - width * 0.45f,
            top.y
        )
        close()
    }
    drawPath(pod, PepperRed)
    // Brillo alargado
    drawLine(
        color = Color.White.copy(alpha = 0.48f),
        start = Offset(top.x - width * 0.12f, top.y + length * 0.18f),
        end = Offset(top.x + curveDir * width * 0.35f, top.y + length * 0.62f),
        strokeWidth = width * 0.18f,
        cap = StrokeCap.Round
    )
    // Capuchón verde del chile
    drawOval(
        color = GreenPrimary,
        topLeft = Offset(top.x - width * 0.52f, top.y - width * 0.28f),
        size = Size(width * 1.04f, width * 0.52f)
    )
}

// ---------------------------------------------------------------------------
// 12. LIMÓN PERSA
// ---------------------------------------------------------------------------

private fun DrawScope.drawLemonPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    drawStem(
        listOf(
            Offset(w * 0.50f, h * 0.76f),
            Offset(w * 0.49f, h * 0.52f),
            Offset(w * 0.51f, h * 0.32f),
            Offset(w * 0.50f, h * 0.14f)
        ),
        s * 0.038f,
        SoilBrown
    )
    drawStem(listOf(Offset(w * 0.50f, h * 0.46f), Offset(w * 0.33f, h * 0.38f)), s * 0.028f, LeafGreen)
    drawStem(listOf(Offset(w * 0.50f, h * 0.38f), Offset(w * 0.68f, h * 0.32f)), s * 0.028f, LeafGreen)

    val vein = LimeAccent.copy(alpha = 0.65f)
    val vw = s * 0.011f
    drawLeaf(Offset(w * 0.48f, h * 0.56f), Offset(w * 0.16f, h * 0.46f), s * 0.085f, GreenPrimary, vein, vw)
    drawLeaf(Offset(w * 0.52f, h * 0.52f), Offset(w * 0.84f, h * 0.42f), s * 0.085f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.24f, h * 0.12f), s * 0.075f, LeafLight, GreenDark.copy(alpha = 0.35f), vw)
    drawLeaf(Offset(w * 0.50f, h * 0.24f), Offset(w * 0.78f, h * 0.10f), s * 0.075f, GreenPrimary, vein, vw)

    // Dos limones amarillos radiantes
    drawLemonFruit(center = Offset(w * 0.33f, h * 0.48f), rx = s * 0.105f, ry = s * 0.082f, angle = -24f)
    drawLemonFruit(center = Offset(w * 0.68f, h * 0.42f), rx = s * 0.112f, ry = s * 0.086f, angle = 22f)
}

private fun DrawScope.drawLemonFruit(center: Offset, rx: Float, ry: Float, angle: Float) {
    rotate(degrees = angle, pivot = center) {
        // Extremos puntiagudos del limón (umbos)
        drawCircle(LemonYellow, radius = ry * 0.42f, center = Offset(center.x - rx * 0.95f, center.y))
        drawCircle(LemonYellow, radius = ry * 0.42f, center = Offset(center.x + rx * 0.95f, center.y))
        drawOval(
            color = LemonYellow,
            topLeft = Offset(center.x - rx, center.y - ry),
            size = Size(rx * 2f, ry * 2f)
        )
        drawOval(
            color = Color.White.copy(alpha = 0.52f),
            topLeft = Offset(center.x - rx * 0.55f, center.y - ry * 0.62f),
            size = Size(rx * 0.75f, ry * 0.42f)
        )
    }
}

// ---------------------------------------------------------------------------
// 13. AJO BLANCO
// ---------------------------------------------------------------------------

private fun DrawScope.drawGarlicPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Hojas acintadas largas y escapo floral rizado
    drawStem(listOf(Offset(w * 0.50f, h * 0.52f), Offset(w * 0.50f, h * 0.30f), Offset(w * 0.54f, h * 0.12f)), s * 0.036f, GreenPrimary)
    drawStem(listOf(Offset(w * 0.47f, h * 0.54f), Offset(w * 0.34f, h * 0.34f), Offset(w * 0.20f, h * 0.20f)), s * 0.034f, LeafGreen)
    drawStem(listOf(Offset(w * 0.53f, h * 0.54f), Offset(w * 0.66f, h * 0.34f), Offset(w * 0.80f, h * 0.20f)), s * 0.034f, LeafLight)

    // Cabeza de ajo blanco marfil con dientes marcados
    val cy = h * 0.68f
    val r = s * 0.155f
    drawCircle(GarlicCream, radius = r * 0.88f, center = Offset(w * 0.39f, cy + r * 0.06f))
    drawCircle(GarlicCream, radius = r * 0.88f, center = Offset(w * 0.61f, cy + r * 0.06f))
    drawCircle(Color.White, radius = r * 0.96f, center = Offset(w * 0.50f, cy))

    // Cuello del bulbo
    val neck = Path().apply {
        moveTo(w * 0.44f, cy - r * 0.65f)
        lineTo(w * 0.47f, h * 0.46f)
        lineTo(w * 0.53f, h * 0.46f)
        lineTo(w * 0.56f, cy - r * 0.65f)
        close()
    }
    drawPath(neck, GarlicCream)

    // Sombreado entre los dientes con ligero matiz violáceo
    val cloveShadow = OnionPurple.copy(alpha = 0.32f)
    val leftArc = Path().apply {
        moveTo(w * 0.48f, h * 0.49f)
        quadraticBezierTo(w * 0.40f, cy, w * 0.44f, cy + r * 0.86f)
    }
    val rightArc = Path().apply {
        moveTo(w * 0.52f, h * 0.49f)
        quadraticBezierTo(w * 0.60f, cy, w * 0.56f, cy + r * 0.86f)
    }
    drawPath(leftArc, cloveShadow, style = Stroke(width = s * 0.016f, cap = StrokeCap.Round))
    drawPath(rightArc, cloveShadow, style = Stroke(width = s * 0.016f, cap = StrokeCap.Round))
}

// ---------------------------------------------------------------------------
// 14. BROTE GENÉRICO DE HUERTO
// ---------------------------------------------------------------------------

private fun DrawScope.drawSproutPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)
    drawStem(listOf(Offset(w * 0.50f, h * 0.76f), Offset(w * 0.49f, h * 0.60f), Offset(w * 0.50f, h * 0.44f)), s * 0.04f)
    drawLeaf(Offset(w * 0.50f, h * 0.52f), Offset(w * 0.22f, h * 0.34f), s * 0.11f, LeafGreen, GreenDark.copy(alpha = 0.35f), s * 0.012f)
    drawLeaf(Offset(w * 0.50f, h * 0.46f), Offset(w * 0.80f, h * 0.22f), s * 0.13f, LeafLight, GreenDark.copy(alpha = 0.35f), s * 0.012f)
}

/**
 * Hoja orgánica entre [base] y [tip]: dos curvas cuadráticas desplazadas
 * perpendicularmente a cada lado, con una nervadura opcional al centro.
 */
private fun DrawScope.drawLeaf(
    base: Offset,
    tip: Offset,
    halfWidth: Float,
    color: Color,
    veinColor: Color?,
    veinWidth: Float
) {
    val dx = tip.x - base.x
    val dy = tip.y - base.y
    val len = sqrt(dx * dx + dy * dy)
    if (len == 0f) return
    val px = -dy / len
    val py = dx / len
    val cx = base.x + dx * 0.42f
    val cy = base.y + dy * 0.42f
    val ctrl = halfWidth * 2f

    val leaf = Path().apply {
        moveTo(base.x, base.y)
        quadraticBezierTo(cx + px * ctrl, cy + py * ctrl, tip.x, tip.y)
        quadraticBezierTo(cx - px * ctrl, cy - py * ctrl, base.x, base.y)
        close()
    }
    drawPath(leaf, color)

    if (veinColor != null && veinWidth > 0f) {
        drawLine(
            color = veinColor,
            start = Offset(base.x + dx * 0.08f, base.y + dy * 0.08f),
            end = Offset(base.x + dx * 0.82f, base.y + dy * 0.82f),
            strokeWidth = veinWidth,
            cap = StrokeCap.Round
        )
    }
}
