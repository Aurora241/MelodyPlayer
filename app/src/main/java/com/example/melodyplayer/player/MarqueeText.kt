package com.example.melodyplayer.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color

/**
 * MarqueeText - Text tự động chạy ngang khi quá dài
 *
 * @param text Nội dung text cần hiển thị
 * @param modifier Modifier tùy chỉnh
 * @param fontSize Kích thước font chữ
 * @param fontWeight Độ đậm của font
 * @param color Màu chữ
 * @param alignment Căn chỉnh khi text ngắn (Center hoặc Start)
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.White,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally
) {
    var textWidth by remember { mutableStateOf(0) }
    var containerWidth by remember { mutableStateOf(0) }
    val shouldScroll = textWidth > containerWidth

    val infiniteTransition = rememberInfiniteTransition(label = "marquee")

    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldScroll) -(textWidth - containerWidth).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (shouldScroll) {
                    ((textWidth - containerWidth) * 20).coerceAtLeast(5000)
                } else 1000,
                easing = LinearEasing,
                delayMillis = 2000
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(2000)
        ),
        label = "marquee_offset"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { size ->
                containerWidth = size.width
            },
        contentAlignment = when (alignment) {
            Alignment.CenterHorizontally -> if (shouldScroll) Alignment.CenterStart else Alignment.Center
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = text,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .wrapContentWidth(unbounded = shouldScroll)
                .offset { IntOffset(if (shouldScroll) offsetX.toInt() else 0, 0) }
                .onGloballyPositioned { coordinates ->
                    textWidth = coordinates.size.width
                }
        )
    }
}