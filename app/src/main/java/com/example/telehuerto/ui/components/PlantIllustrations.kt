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
import com.example.telehuerto.ui.theme.GreenDark
import com.example.telehuerto.ui.theme.GreenPrimary
import com.example.telehuerto.ui.theme.LeafGreen
import com.example.telehuerto.ui.theme.LeafLight
import com.example.telehuerto.ui.theme.LimeAccent
import com.example.telehuerto.ui.theme.PepperRed
import com.example.telehuerto.ui.theme.SoilBrown
import com.example.telehuerto.ui.theme.SoilDark
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
// Maceta por tipo de planta (listas y detalle)
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
                PlantIconType.GENERIC -> drawSproutPlant()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Piezas de dibujo
// ---------------------------------------------------------------------------

/** Montículo de tierra en la parte baja del contenedor. */
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
    // Puntitos de textura
    val dot = min(w, h) * 0.018f
    listOf(0.30f to 0.83f, 0.44f to 0.80f, 0.58f to 0.84f, 0.70f to 0.81f, 0.38f to 0.87f, 0.64f to 0.875f)
        .forEach { (x, y) -> drawCircle(SoilDark, radius = dot, center = Offset(w * x, h * y)) }
}

private fun DrawScope.drawStem(points: List<Offset>, width: Float, color: Color = LeafGreen) {
    if (points.size < 2) return
    val path = Path().apply {
        moveTo(points[0].x, points[0].y)
        if (points.size == 3) {
            quadraticBezierTo(points[1].x, points[1].y, points[2].x, points[2].y)
        } else if (points.size >= 4) {
            cubicTo(points[1].x, points[1].y, points[2].x, points[2].y, points[3].x, points[3].y)
        } else {
            lineTo(points[1].x, points[1].y)
        }
    }
    drawPath(path, color, style = Stroke(width = width, cap = StrokeCap.Round))
}

private fun DrawScope.drawTomatoPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tallo principal y ramas
    drawStem(listOf(Offset(w * 0.50f, h * 0.76f), Offset(w * 0.48f, h * 0.55f), Offset(w * 0.52f, h * 0.38f), Offset(w * 0.50f, h * 0.16f)), s * 0.035f)
    drawStem(listOf(Offset(w * 0.50f, h * 0.48f), Offset(w * 0.38f, h * 0.44f), Offset(w * 0.30f, h * 0.50f)), s * 0.025f)
    drawStem(listOf(Offset(w * 0.51f, h * 0.36f), Offset(w * 0.64f, h * 0.34f), Offset(w * 0.70f, h * 0.42f)), s * 0.025f)

    // Hojas dentadas (simplificadas como hojas puntiagudas)
    drawLeaf(Offset(w * 0.50f, h * 0.60f), Offset(w * 0.20f, h * 0.56f), s * 0.07f, LeafGreen, GreenDark.copy(alpha = 0.4f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.54f), Offset(w * 0.80f, h * 0.50f), s * 0.07f, GreenPrimary, LeafLight.copy(alpha = 0.6f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.28f), Offset(w * 0.30f, h * 0.14f), s * 0.06f, LeafLight, GreenDark.copy(alpha = 0.35f), s * 0.01f)
    drawLeaf(Offset(w * 0.50f, h * 0.22f), Offset(w * 0.70f, h * 0.10f), s * 0.06f, LeafGreen, GreenDark.copy(alpha = 0.35f), s * 0.01f)

    // Racimo de tomates cherry
    drawTomato(Offset(w * 0.30f, h * 0.55f), s * 0.085f)
    drawTomato(Offset(w * 0.40f, h * 0.60f), s * 0.075f)
    drawTomato(Offset(w * 0.70f, h * 0.47f), s * 0.085f)
    drawTomato(Offset(w * 0.61f, h * 0.40f), s * 0.065f)
}

private fun DrawScope.drawTomato(center: Offset, r: Float) {
    drawCircle(TomatoRed, radius = r, center = center)
    // Sombra inferior
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
            center = Offset(center.x - r * 0.3f, center.y - r * 0.3f),
            radius = r * 1.4f
        ),
        radius = r,
        center = center
    )
    // Brillo
    drawCircle(Color.White.copy(alpha = 0.55f), radius = r * 0.22f, center = Offset(center.x - r * 0.38f, center.y - r * 0.36f))
    // Cáliz en estrella
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

private fun DrawScope.drawBasilPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tallos cortos que salen de la tierra
    drawStem(listOf(Offset(w * 0.50f, h * 0.76f), Offset(w * 0.50f, h * 0.55f), Offset(w * 0.50f, h * 0.36f)), s * 0.035f)
    drawStem(listOf(Offset(w * 0.48f, h * 0.74f), Offset(w * 0.40f, h * 0.62f), Offset(w * 0.32f, h * 0.56f)), s * 0.028f)
    drawStem(listOf(Offset(w * 0.52f, h * 0.74f), Offset(w * 0.60f, h * 0.62f), Offset(w * 0.68f, h * 0.56f)), s * 0.028f)

    // Hojas anchas y redondeadas en roseta (de atrás hacia adelante)
    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.012f
    drawLeaf(Offset(w * 0.50f, h * 0.44f), Offset(w * 0.50f, h * 0.10f), s * 0.12f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.48f, h * 0.46f), Offset(w * 0.20f, h * 0.24f), s * 0.11f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.52f, h * 0.46f), Offset(w * 0.80f, h * 0.24f), s * 0.11f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.34f, h * 0.60f), Offset(w * 0.10f, h * 0.52f), s * 0.10f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.66f, h * 0.60f), Offset(w * 0.90f, h * 0.52f), s * 0.10f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.46f, h * 0.56f), Offset(w * 0.34f, h * 0.34f), s * 0.09f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.54f, h * 0.56f), Offset(w * 0.66f, h * 0.34f), s * 0.09f, LeafGreen, vein, vw)
    // Brote central lima
    drawLeaf(Offset(w * 0.50f, h * 0.40f), Offset(w * 0.46f, h * 0.24f), s * 0.05f, LimeAccent, null, 0f)
    drawLeaf(Offset(w * 0.50f, h * 0.40f), Offset(w * 0.55f, h * 0.25f), s * 0.05f, LimeAccent, null, 0f)
}

private fun DrawScope.drawPepperPlant() {
    val w = size.width
    val h = size.height
    val s = min(w, h)

    // Tallo y rama
    drawStem(listOf(Offset(w * 0.46f, h * 0.76f), Offset(w * 0.44f, h * 0.56f), Offset(w * 0.46f, h * 0.38f), Offset(w * 0.44f, h * 0.18f)), s * 0.035f)
    drawStem(listOf(Offset(w * 0.45f, h * 0.40f), Offset(w * 0.56f, h * 0.34f), Offset(w * 0.64f, h * 0.36f)), s * 0.028f)

    // Hojas lisas y alargadas
    val vein = GreenDark.copy(alpha = 0.35f)
    val vw = s * 0.01f
    drawLeaf(Offset(w * 0.45f, h * 0.60f), Offset(w * 0.14f, h * 0.52f), s * 0.07f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.45f, h * 0.46f), Offset(w * 0.18f, h * 0.32f), s * 0.065f, GreenPrimary, LeafLight.copy(alpha = 0.5f), vw)
    drawLeaf(Offset(w * 0.45f, h * 0.26f), Offset(w * 0.24f, h * 0.10f), s * 0.06f, LeafLight, vein, vw)
    drawLeaf(Offset(w * 0.45f, h * 0.22f), Offset(w * 0.66f, h * 0.08f), s * 0.06f, LeafGreen, vein, vw)
    drawLeaf(Offset(w * 0.60f, h * 0.35f), Offset(w * 0.86f, h * 0.24f), s * 0.06f, LeafLight, vein, vw)

    // Pimiento morrón colgando de la rama
    drawBellPepper(top = Offset(w * 0.64f, h * 0.40f), width = s * 0.30f, height = s * 0.34f)
}

private fun DrawScope.drawBellPepper(top: Offset, width: Float, height: Float) {
    val cx = top.x
    val y0 = top.y + height * 0.10f
    val left = cx - width / 2f
    val right = cx + width / 2f
    val bottom = y0 + height

    // Cuerpo con tres lóbulos abajo
    val body = Path().apply {
        moveTo(cx, y0)
        cubicTo(right + width * 0.05f, y0 - height * 0.02f, right + width * 0.06f, y0 + height * 0.55f, right - width * 0.06f, bottom - height * 0.08f)
        quadraticBezierTo(right - width * 0.18f, bottom + height * 0.02f, cx + width * 0.16f, bottom - height * 0.06f)
        quadraticBezierTo(cx, bottom + height * 0.04f, cx - width * 0.16f, bottom - height * 0.06f)
        quadraticBezierTo(left + width * 0.18f, bottom + height * 0.02f, left + width * 0.06f, bottom - height * 0.08f)
        cubicTo(left - width * 0.06f, y0 + height * 0.55f, left - width * 0.05f, y0 - height * 0.02f, cx, y0)
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
    // Surcos
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
    // Brillo
    drawRoundRect(
        color = Color.White.copy(alpha = 0.45f),
        topLeft = Offset(left + width * 0.16f, y0 + height * 0.16f),
        size = Size(width * 0.09f, height * 0.36f),
        cornerRadius = CornerRadius(width * 0.05f)
    )
    // Cáliz y pedúnculo
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
    // Vector perpendicular unitario
    val px = -dy / len
    val py = dx / len
    // Punto de control desplazado hacia la base para una forma de gota
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
