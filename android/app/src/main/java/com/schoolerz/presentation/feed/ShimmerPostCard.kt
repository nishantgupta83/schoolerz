package com.schoolerz.presentation.feed

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.schoolerz.presentation.theme.Tokens

@Composable
fun ShimmerPostCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmer"
    )
    val brush = Brush.linearGradient(
        colors = listOf(Color.LightGray.copy(alpha = 0.3f), Color.LightGray.copy(alpha = 0.5f), Color.LightGray.copy(alpha = 0.3f)),
        start = Offset(translateAnim.value - 500, 0f),
        end = Offset(translateAnim.value, 0f)
    )

    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(Tokens.Radius.medium)) {
        Column(modifier = Modifier.padding(Tokens.Spacing.m)) {
            Row {
                Box(Modifier.size(40.dp).clip(CircleShape).background(brush))
                Spacer(Modifier.width(Tokens.Spacing.s))
                Column {
                    Box(Modifier.width(120.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                    Spacer(Modifier.height(4.dp))
                    Box(Modifier.width(80.dp).height(10.dp).clip(RoundedCornerShape(4.dp)).background(brush))
                }
            }
            Spacer(Modifier.height(Tokens.Spacing.m))
            Box(Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
            Spacer(Modifier.height(Tokens.Spacing.s))
            Box(Modifier.width(200.dp).height(14.dp).clip(RoundedCornerShape(4.dp)).background(brush))
        }
    }
}
