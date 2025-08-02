package de.no3x.filetimesync.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedGlowingProgressBar(progress: Float) {
    val glowColors = listOf(Color(0xFF38BDF8), Color(0xFF34D399))
    val brush = Brush.horizontalGradient(colors = glowColors)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(5.dp))
            .padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .height(10.dp)
                .blur(20.dp)
                .background(brush, RoundedCornerShape(5.dp))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .height(10.dp)
                .background(brush, RoundedCornerShape(5.dp))
        )
    }
}

@Preview
@Composable
fun AnimatedGlowingProgressBarPreview() {
    AnimatedGlowingProgressBar(12f)
}