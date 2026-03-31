package com.musclefit.app.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp

object BodyMapComposeRenderer {
    @JvmStatic
    fun render(
        composeView: ComposeView,
        isMale: Boolean,
        side: Int,
        selectedKey: String?,
        listener: OnMuscleHotspotClickListener?
    ) {
        composeView.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView.setContent {
            BodyMapContent(
                isMale = isMale,
                side = side,
                selectedKey = selectedKey,
                listener = listener
            )
        }
    }
}

@Composable
private fun BodyMapContent(
    isMale: Boolean,
    side: Int,
    selectedKey: String?,
    listener: OnMuscleHotspotClickListener?
) {
    AnimatedContent(
        targetState = side,
        transitionSpec = {
            fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
        },
        label = "body_side"
    ) { targetSide ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val hotspots = remember(targetSide) {
                if (targetSide == BodyMapView.SIDE_FRONT) frontHotspots() else backHotspots()
            }
            val normal = Color(0x6653A5F5)
            val active = Color(0xFF1E3A8A)
            val stroke = Color(0x991E3A8A)

            BodySilhouetteLayer(isMale = isMale, side = targetSide)

            hotspots.forEach { hotspot ->
                val adjusted = hotspot.adjustForGender(isMale)
                val left = maxWidth * adjusted.left
                val top = maxHeight * adjusted.top
                val width = maxWidth * (adjusted.right - adjusted.left)
                val height = maxHeight * (adjusted.bottom - adjusted.top)
                val selected = selectedKey == hotspot.key

                Box(
                    modifier = Modifier
                        .offset(x = left, y = top)
                        .size(width = width, height = height)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (selected) active.copy(alpha = 0.68f) else normal)
                        .border(1.dp, stroke, RoundedCornerShape(18.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            listener?.onMuscleHotspotClick(hotspot.key)
                        }
                )
            }
        }
    }
}

@Composable
private fun BodySilhouetteLayer(isMale: Boolean, side: Int) {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val bodyW = size.width * 0.48f
        val bodyH = size.height * 0.92f
        val left = (size.width - bodyW) / 2f
        val top = (size.height - bodyH) / 2f
        val centerX = left + bodyW / 2f

        val silhouette = Color(0xFFD5DEE8)
        val detail = Color(0xFFBAC7D6)

        val shoulderWidth = bodyW * if (isMale) 0.86f else 0.78f
        val hipWidth = bodyW * if (isMale) 0.58f else 0.68f
        val torsoTop = top + bodyH * 0.20f
        val torsoBottom = top + bodyH * 0.58f

        val headR = bodyW * 0.14f
        drawCircle(
            color = silhouette,
            radius = headR,
            center = androidx.compose.ui.geometry.Offset(centerX, top + headR)
        )

        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - bodyW * 0.09f, top + bodyH * 0.15f),
            size = androidx.compose.ui.geometry.Size(bodyW * 0.18f, bodyH * 0.05f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.04f, bodyW * 0.04f)
        )

        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - shoulderWidth / 2f, torsoTop),
            size = androidx.compose.ui.geometry.Size(shoulderWidth, torsoBottom - torsoTop),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.20f, bodyW * 0.20f)
        )

        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - hipWidth / 2f, torsoBottom - bodyH * 0.05f),
            size = androidx.compose.ui.geometry.Size(hipWidth, bodyH * 0.15f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.16f, bodyW * 0.16f)
        )

        val armWidth = bodyW * 0.13f
        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - shoulderWidth / 2f - armWidth, torsoTop + bodyH * 0.03f),
            size = androidx.compose.ui.geometry.Size(armWidth, bodyH * 0.37f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.08f, bodyW * 0.08f)
        )
        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX + shoulderWidth / 2f, torsoTop + bodyH * 0.03f),
            size = androidx.compose.ui.geometry.Size(armWidth, bodyH * 0.37f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.08f, bodyW * 0.08f)
        )

        val legTop = torsoBottom + bodyH * 0.02f
        val legWidth = bodyW * 0.22f
        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX - bodyW * 0.26f, legTop),
            size = androidx.compose.ui.geometry.Size(legWidth, top + bodyH - legTop),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.10f, bodyW * 0.10f)
        )
        drawRoundRect(
            color = silhouette,
            topLeft = androidx.compose.ui.geometry.Offset(centerX + bodyW * 0.04f, legTop),
            size = androidx.compose.ui.geometry.Size(legWidth, top + bodyH - legTop),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.10f, bodyW * 0.10f)
        )

        if (side == BodyMapView.SIDE_BACK) {
            drawRoundRect(
                color = detail,
                topLeft = androidx.compose.ui.geometry.Offset(centerX - bodyW * 0.02f, torsoTop + bodyH * 0.03f),
                size = androidx.compose.ui.geometry.Size(bodyW * 0.04f, bodyH * 0.43f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(bodyW * 0.02f, bodyW * 0.02f)
            )
        }
    }
}

private data class HotspotRect(
    val key: String,
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun adjustForGender(isMale: Boolean): HotspotRect {
        if (isMale) {
            return this
        }
        val centerX = 0.5f
        val yMid = (top + bottom) / 2f
        val scale = if (yMid < 0.54f) 0.90f else 1.07f
        val l = centerX + ((left - centerX) * scale)
        val r = centerX + ((right - centerX) * scale)
        return copy(left = l, right = r)
    }
}

private fun frontHotspots(): List<HotspotRect> {
    return listOf(
        HotspotRect("shoulder", 0.20f, 0.18f, 0.80f, 0.25f),
        HotspotRect("chest", 0.28f, 0.25f, 0.72f, 0.35f),
        HotspotRect("triceps", 0.11f, 0.28f, 0.23f, 0.52f),
        HotspotRect("triceps", 0.77f, 0.28f, 0.89f, 0.52f),
        HotspotRect("abs", 0.36f, 0.36f, 0.64f, 0.56f),
        HotspotRect("thigh", 0.30f, 0.60f, 0.70f, 0.82f),
        HotspotRect("calf", 0.31f, 0.82f, 0.44f, 0.94f),
        HotspotRect("calf", 0.56f, 0.82f, 0.69f, 0.94f)
    )
}

private fun backHotspots(): List<HotspotRect> {
    return listOf(
        HotspotRect("traps", 0.32f, 0.19f, 0.68f, 0.27f),
        HotspotRect("shoulders_back", 0.20f, 0.24f, 0.80f, 0.32f),
        HotspotRect("back", 0.25f, 0.28f, 0.75f, 0.46f),
        HotspotRect("glutes", 0.33f, 0.50f, 0.67f, 0.61f),
        HotspotRect("hamstrings", 0.30f, 0.62f, 0.70f, 0.80f),
        HotspotRect("calves_back", 0.31f, 0.81f, 0.44f, 0.93f),
        HotspotRect("calves_back", 0.56f, 0.81f, 0.69f, 0.93f)
    )
}
