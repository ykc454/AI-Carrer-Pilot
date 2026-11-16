package com.nextgendevs.aicareerpilot.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nextgendevs.aicareerpilot.presentation.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Live animated splash (~2.8 s). It tells the app's story in one motion:
 * a resume is drawn, an AI scan line reads it, a magnifier lands, the
 * name types in, and the app opens.
 *
 *  0.00s  app tile springs in (slight overshoot)
 *  0.15s  resume outline draws itself (path trim)
 *  0.70s  avatar + text lines appear one by one
 *  0.90s  scan line sweeps up and down (continuous)
 *  1.20s  magnifier pops in
 *  1.10s  "AI Career Pilot" letters rise in, staggered
 *  1.70s  tagline fades in, progress bar fills
 *  2.40s  zoom-and-fade exit → [onFinished]
 */
@Composable
fun AnimatedSplashScreen(onFinished: () -> Unit) {
    val ink = AppColors.TextPrimary
    val muted = AppColors.TextSecondary
    val bg = AppColors.Background
    val tileTop = AppColors.Surface
    val border = AppColors.TextPrimary.copy(alpha = 0.08f)
    val track = AppColors.Hairline
    val lensFill = AppColors.Surface

    val tileScale = remember { Animatable(0.4f) }
    val tileAlpha = remember { Animatable(0f) }
    val outline = remember { Animatable(0f) }
    val lines = remember { Animatable(0f) }
    val scanAlpha = remember { Animatable(0f) }
    val magnifier = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val tagline = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    val infinite = rememberInfiniteTransition(label = "splash")
    val scan by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scan"
    )
    val glow by infinite.animateFloat(
        initialValue = 0.7f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    LaunchedEffect(Unit) {
        launch { tileAlpha.animateTo(1f, tween(250)) }
        launch {
            tileScale.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow))
        }
        launch { delay(150); outline.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
        launch { delay(700); lines.animateTo(1f, tween(600)) }
        launch { delay(900); scanAlpha.animateTo(1f, tween(300)) }
        launch {
            delay(1200)
            magnifier.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow))
        }
        launch { delay(1100); title.animateTo(1f, tween(700, easing = LinearEasing)) }
        launch { delay(1700); tagline.animateTo(1f, tween(450)) }
        launch { delay(400); progress.animateTo(1f, tween(2000, easing = FastOutSlowInEasing)) }

        delay(2400)
        exit.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .graphicsLayer {
                alpha = 1f - exit.value
                val s = 1f + 0.06f * exit.value
                scaleX = s; scaleY = s
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            // ---------- App tile with animated resume ----------
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = tileScale.value; scaleY = tileScale.value
                        alpha = tileAlpha.value
                    }
                    .drawBehind {
                        // soft halo behind the tile
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(ink.copy(alpha = 0.10f * glow), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.95f
                        )
                    }
                    .clip(RoundedCornerShape(38.dp))
                    .background(Brush.verticalGradient(listOf(tileTop, bg)))
                    .border(1.dp, border, RoundedCornerShape(38.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(104.dp)) {
                    drawResume(
                        ink = ink,
                        outline = outline.value,
                        lines = lines.value,
                        scan = scan,
                        scanAlpha = scanAlpha.value,
                        magnifier = magnifier.value,
                        lensFill = lensFill
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            // ---------- Title: letters rise in one after another ----------
            StaggeredTitle("AI Career Pilot", title.value, ink)

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Analyze  ·  Improve  ·  Get hired",
                color = muted,
                fontSize = 13.sp,
                letterSpacing = 1.2.sp,
                modifier = Modifier.graphicsLayer {
                    alpha = tagline.value
                    translationY = (1f - tagline.value) * 8.dp.toPx()
                }
            )

            Spacer(Modifier.height(40.dp))

            // ---------- Thin progress bar ----------
            Box(
                Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(track)
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.value)
                        .clip(RoundedCornerShape(50))
                        .background(ink)
                )
            }
        }
    }
}

@Composable
private fun StaggeredTitle(text: String, progress: Float, color: Color) {
    Row {
        val n = text.length
        text.forEachIndexed { i, ch ->
            // each letter animates over its own slice of the timeline
            val start = i.toFloat() / (n + 4)
            val local = ((progress - start) * (n + 4) / 5f).coerceIn(0f, 1f)
            Text(
                text = ch.toString(),
                color = color,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer {
                    alpha = local
                    translationY = (1f - local) * 14.dp.toPx()
                }
            )
        }
    }
}

/** Draws the logo: resume outline, avatar, text lines, scan line, magnifier. */
private fun DrawScope.drawResume(
    ink: Color,
    outline: Float,
    lines: Float,
    scan: Float,
    scanAlpha: Float,
    magnifier: Float,
    lensFill: Color
) {
    val w = size.width
    val h = size.height
    val stroke = w * 0.045f
    val left = w * 0.14f
    val top = h * 0.06f
    val right = w * 0.74f
    val bottom = h * 0.94f
    val fold = w * 0.16f

    // 1. outline (trimmed path → "drawing" effect)
    val doc = Path().apply {
        moveTo(left + fold * 0.4f, top)
        lineTo(right - fold, top)
        lineTo(right, top + fold)
        lineTo(right, bottom - fold * 0.4f)
        quadraticTo(right, bottom, right - fold * 0.4f, bottom)
        lineTo(left + fold * 0.4f, bottom)
        quadraticTo(left, bottom, left, bottom - fold * 0.4f)
        lineTo(left, top + fold * 0.4f)
        quadraticTo(left, top, left + fold * 0.4f, top)
        // folded corner
        moveTo(right - fold, top)
        lineTo(right - fold, top + fold)
        lineTo(right, top + fold)
    }
    val measure = PathMeasure().apply { setPath(doc, false) }
    val partial = Path()
    measure.getSegment(0f, measure.length * outline, partial, true)
    drawPath(
        partial, ink,
        style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // 2. avatar + text lines, appearing one by one
    fun stepAlpha(i: Int, count: Int) = ((lines * (count + 1)) - i).coerceIn(0f, 1f)
    val total = 6
    val avatarA = stepAlpha(0, total)
    val ax = left + w * 0.15f
    val ay = top + h * 0.17f
    drawCircle(ink.copy(alpha = avatarA), radius = w * 0.055f, center = Offset(ax, ay))
    drawArc(
        ink.copy(alpha = avatarA), 180f, 180f, useCenter = true,
        topLeft = Offset(ax - w * 0.09f, ay + w * 0.07f),
        size = Size(w * 0.18f, w * 0.14f)
    )
    val headerA = stepAlpha(1, total)
    drawLine(ink.copy(alpha = headerA), Offset(ax + w * 0.13f, ay - h * 0.01f), Offset(right - w * 0.12f, ay - h * 0.01f), stroke * 0.8f, StrokeCap.Round)
    drawLine(ink.copy(alpha = headerA), Offset(ax + w * 0.13f, ay + h * 0.05f), Offset(right - w * 0.18f, ay + h * 0.05f), stroke * 0.8f, StrokeCap.Round)

    val lineWidths = listOf(0.30f, 0.24f, 0.28f, 0.20f)
    lineWidths.forEachIndexed { i, lw ->
        val a = stepAlpha(i + 2, total)
        val y = top + h * (0.44f + i * 0.12f)
        drawCircle(ink.copy(alpha = a), radius = stroke * 0.55f, center = Offset(left + w * 0.09f, y))
        drawLine(
            ink.copy(alpha = a),
            Offset(left + w * 0.16f, y),
            Offset(left + w * (0.16f + lw * a), y),   // line also "grows"
            stroke * 0.8f, StrokeCap.Round
        )
    }

    // 3. AI scan line sweeping over the page
    if (scanAlpha > 0f) {
        val y = top + fold * 0.6f + (bottom - top - fold) * scan
        drawRect(
            brush = Brush.verticalGradient(
                listOf(Color.Transparent, ink.copy(alpha = 0.18f * scanAlpha), Color.Transparent),
                startY = y - h * 0.08f, endY = y + h * 0.08f
            ),
            topLeft = Offset(left + stroke, y - h * 0.08f),
            size = Size(right - left - stroke * 2, h * 0.16f)
        )
        drawLine(
            ink.copy(alpha = 0.9f * scanAlpha),
            Offset(left + stroke * 1.4f, y), Offset(right - stroke * 1.4f, y),
            stroke * 0.5f, StrokeCap.Round
        )
    }

    // 4. magnifier pops in (spring), with a "lens" filled by the background
    if (magnifier > 0f) {
        val r = w * 0.15f * magnifier
        val c = Offset(right - w * 0.02f, bottom - h * 0.24f)
        drawCircle(lensFill, radius = r, center = c)   // lens hides the lines behind it
        drawCircle(ink, radius = r, center = c, style = Stroke(stroke * 1.1f))
        val hs = Offset(c.x + r * 0.72f, c.y + r * 0.72f)
        val he = Offset(c.x + r * 1.55f, c.y + r * 1.55f)
        drawLine(ink, hs, he, stroke * 1.5f, StrokeCap.Round)
        // lens text
        listOf(0.55f, 0.75f, 0.45f).forEachIndexed { i, lw ->
            val y = c.y - r * 0.35f + i * r * 0.35f
            drawLine(
                ink.copy(alpha = magnifier),
                Offset(c.x - r * 0.45f, y), Offset(c.x - r * 0.45f + r * lw * 1.2f, y),
                stroke * 0.6f, StrokeCap.Round
            )
        }
    }
}
