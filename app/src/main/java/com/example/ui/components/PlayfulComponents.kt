package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.MascotBuddy
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun TactileButton(
    onClick: () -> Unit,
    containerColor: Color,
    lipColor: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp = 20.dp,
    minHeight: Dp = 56.dp,
    tag: String? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "tactile_scale"
    )
    val topOffset by animateFloatAsState(
        targetValue = if (isPressed && enabled) 4f else 0f,
        animationSpec = tween(durationMillis = 80),
        label = "tactile_press"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = modifier
        .then(if (tag != null) Modifier.testTag(tag) else Modifier)
        .minimumInteractiveComponentSize()
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }

    Box(
        modifier = baseModifier
            .clip(shape)
            .background(if (enabled) lipColor else lipColor.copy(alpha = 0.45f), shape)
            .padding(bottom = 5.dp)
            .graphicsLayer {
                translationY = topOffset
            }
            .clip(shape)
            .background(if (enabled) containerColor else containerColor.copy(alpha = 0.55f), shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.35f)),
                role = Role.Button,
                onClick = onClick
            )
            .heightIn(min = minHeight)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun MascotAvatarBadge(
    mascot: MascotBuddy,
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(mascot.accentColor.copy(alpha = 0.16f))
            .semantics { contentDescription = mascot.displayName },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val w = this.size.width
            val h = this.size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) * 0.38f

            when (mascot) {
                MascotBuddy.BERUANG -> {
                    val fur = Color(0xFFD98236)
                    val innerEar = Color(0xFFFFD5A5)
                    val snout = Color(0xFFFFEBD2)
                    // Ears
                    drawCircle(fur, radius = r * 0.36f, center = Offset(cx - r * 0.68f, cy - r * 0.68f))
                    drawCircle(fur, radius = r * 0.36f, center = Offset(cx + r * 0.68f, cy - r * 0.68f))
                    drawCircle(innerEar, radius = r * 0.18f, center = Offset(cx - r * 0.68f, cy - r * 0.68f))
                    drawCircle(innerEar, radius = r * 0.18f, center = Offset(cx + r * 0.68f, cy - r * 0.68f))
                    // Head
                    drawCircle(fur, radius = r, center = Offset(cx, cy + r * 0.06f))
                    // Snout
                    drawOval(
                        color = snout,
                        topLeft = Offset(cx - r * 0.42f, cy + r * 0.05f),
                        size = Size(r * 0.84f, r * 0.62f)
                    )
                    // Eyes & Cheeks
                    drawCircle(Color(0xFF231F30), radius = r * 0.11f, center = Offset(cx - r * 0.36f, cy - r * 0.12f))
                    drawCircle(Color(0xFF231F30), radius = r * 0.11f, center = Offset(cx + r * 0.36f, cy - r * 0.12f))
                    drawCircle(Color(0xFFFF9AA2), radius = r * 0.14f, center = Offset(cx - r * 0.54f, cy + r * 0.14f))
                    drawCircle(Color(0xFFFF9AA2), radius = r * 0.14f, center = Offset(cx + r * 0.54f, cy + r * 0.14f))
                    // Nose
                    drawOval(
                        color = Color(0xFF3E2723),
                        topLeft = Offset(cx - r * 0.16f, cy + r * 0.1f),
                        size = Size(r * 0.32f, r * 0.22f)
                    )
                }

                MascotBuddy.KUCING -> {
                    val fur = Color(0xFF38BDF8)
                    val inner = Color(0xFFFDA4AF)
                    // Pointy ears
                    val leftEar = Path().apply {
                        moveTo(cx - r * 0.85f, cy - r * 0.15f)
                        lineTo(cx - r * 0.72f, cy - r * 1.05f)
                        lineTo(cx - r * 0.15f, cy - r * 0.7f)
                        close()
                    }
                    val rightEar = Path().apply {
                        moveTo(cx + r * 0.85f, cy - r * 0.15f)
                        lineTo(cx + r * 0.72f, cy - r * 1.05f)
                        lineTo(cx + r * 0.15f, cy - r * 0.7f)
                        close()
                    }
                    drawPath(leftEar, fur)
                    drawPath(rightEar, fur)
                    // Head
                    drawCircle(fur, radius = r * 0.95f, center = Offset(cx, cy + r * 0.08f))
                    // Eyes
                    drawCircle(Color(0xFF1E1B4B), radius = r * 0.12f, center = Offset(cx - r * 0.35f, cy - r * 0.04f))
                    drawCircle(Color(0xFF1E1B4B), radius = r * 0.12f, center = Offset(cx + r * 0.35f, cy - r * 0.04f))
                    // Nose & Whiskers
                    drawCircle(inner, radius = r * 0.12f, center = Offset(cx, cy + r * 0.18f))
                    drawLine(Color.White, Offset(cx - r * 0.45f, cy + r * 0.16f), Offset(cx - r * 0.95f, cy + r * 0.08f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(Color.White, Offset(cx - r * 0.45f, cy + r * 0.28f), Offset(cx - r * 0.95f, cy + r * 0.32f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(Color.White, Offset(cx + r * 0.45f, cy + r * 0.16f), Offset(cx + r * 0.95f, cy + r * 0.08f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                    drawLine(Color.White, Offset(cx + r * 0.45f, cy + r * 0.28f), Offset(cx + r * 0.95f, cy + r * 0.32f), strokeWidth = 2.5f, cap = StrokeCap.Round)
                }

                MascotBuddy.BURUNG_HANTU -> {
                    val feather = Color(0xFF8B5CF6)
                    val belly = Color(0xFFEDE9FE)
                    // Head/Body
                    drawCircle(feather, radius = r, center = Offset(cx, cy + r * 0.05f))
                    // Eye rings
                    drawCircle(belly, radius = r * 0.38f, center = Offset(cx - r * 0.38f, cy - r * 0.08f))
                    drawCircle(belly, radius = r * 0.38f, center = Offset(cx + r * 0.38f, cy - r * 0.08f))
                    drawCircle(Color(0xFF1E1B4B), radius = r * 0.16f, center = Offset(cx - r * 0.36f, cy - r * 0.08f))
                    drawCircle(Color(0xFF1E1B4B), radius = r * 0.16f, center = Offset(cx + r * 0.36f, cy - r * 0.08f))
                    // Beak
                    val beak = Path().apply {
                        moveTo(cx - r * 0.18f, cy + r * 0.12f)
                        lineTo(cx + r * 0.18f, cy + r * 0.12f)
                        lineTo(cx, cy + r * 0.42f)
                        close()
                    }
                    drawPath(beak, Color(0xFFF59E0B))
                }

                MascotBuddy.KELINCI -> {
                    val fur = Color(0xFFF472B6)
                    val innerEar = Color(0xFFFCE7F3)
                    // Long ears
                    drawOval(fur, topLeft = Offset(cx - r * 0.56f, cy - r * 1.28f), size = Size(r * 0.38f, r * 0.9f))
                    drawOval(fur, topLeft = Offset(cx + r * 0.18f, cy - r * 1.28f), size = Size(r * 0.38f, r * 0.9f))
                    drawOval(innerEar, topLeft = Offset(cx - r * 0.46f, cy - r * 1.12f), size = Size(r * 0.18f, r * 0.6f))
                    drawOval(innerEar, topLeft = Offset(cx + r * 0.28f, cy - r * 1.12f), size = Size(r * 0.18f, r * 0.6f))
                    // Head
                    drawCircle(fur, radius = r * 0.88f, center = Offset(cx, cy + r * 0.18f))
                    // Eyes & Smile
                    drawCircle(Color(0xFF231F30), radius = r * 0.1f, center = Offset(cx - r * 0.32f, cy + r * 0.06f))
                    drawCircle(Color(0xFF231F30), radius = r * 0.1f, center = Offset(cx + r * 0.32f, cy + r * 0.06f))
                    drawCircle(Color(0xFFFFE4E6), radius = r * 0.14f, center = Offset(cx - r * 0.48f, cy + r * 0.26f))
                    drawCircle(Color(0xFFFFE4E6), radius = r * 0.14f, center = Offset(cx + r * 0.48f, cy + r * 0.26f))
                    drawCircle(Color(0xFFBE185D), radius = r * 0.1f, center = Offset(cx, cy + r * 0.26f))
                    drawArc(
                        color = Color(0xFF231F30),
                        startAngle = 20f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = Offset(cx - r * 0.2f, cy + r * 0.28f),
                        size = Size(r * 0.4f, r * 0.25f),
                        style = Stroke(width = 3f, cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

private data class ConfettiPiece(
    val xFraction: Float,
    val delayFraction: Float,
    val speedMultiplier: Float,
    val sizeDp: Float,
    val color: Color,
    val angleOffset: Float
)

@Composable
fun ConfettiCelebrationOverlay(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val pieces = remember {
        val palette = listOf(
            Color(0xFFFF6B35),
            Color(0xFFF59E0B),
            Color(0xFF0EA5E9),
            Color(0xFF10B981),
            Color(0xFFEC4899),
            Color(0xFF8B5CF6)
        )
        val rng = Random(42)
        List(36) { idx ->
            ConfettiPiece(
                xFraction = 0.05f + rng.nextFloat() * 0.9f,
                delayFraction = rng.nextFloat() * 0.35f,
                speedMultiplier = 0.8f + rng.nextFloat() * 0.7f,
                sizeDp = 7f + rng.nextFloat() * 7f,
                color = palette[idx % palette.size],
                angleOffset = rng.nextFloat() * 360f
            )
        }
    }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2800, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val t = progress.value
        if (t >= 0.99f) return@Canvas
        val w = size.width
        val h = size.height

        pieces.forEach { piece ->
            val localT = ((t - piece.delayFraction) / (1f - piece.delayFraction)).coerceIn(0f, 1f)
            if (localT > 0f) {
                val x = piece.xFraction * w + sin(localT * 10f + piece.angleOffset) * 28f
                val y = -30f + localT * (h * 0.85f) * piece.speedMultiplier
                val alpha = (1f - localT).coerceIn(0f, 1f)
                val pxSize = piece.sizeDp.dp.toPx()
                rotate(
                    degrees = piece.angleOffset + localT * 360f,
                    pivot = Offset(x, y)
                ) {
                    drawRect(
                        color = piece.color.copy(alpha = alpha * 0.9f),
                        topLeft = Offset(x - pxSize / 2f, y - pxSize / 4f),
                        size = Size(pxSize, pxSize * 0.55f)
                    )
                }
            }
        }
    }
}

@Composable
fun DecorativeStarsBackdrop(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height
        val positions = listOf(
            Offset(w * 0.88f, h * 0.22f) to 22.dp.toPx(),
            Offset(w * 0.76f, h * 0.68f) to 14.dp.toPx(),
            Offset(w * 0.94f, h * 0.74f) to 10.dp.toPx()
        )
        positions.forEach { (center, radius) ->
            val path = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) radius else radius * 0.45f
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val x = center.x + (r * cos(angle)).toFloat()
                val y = center.y + (r * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color = tint)
        }
    }
}
