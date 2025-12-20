package com.ichi2.anki.ui.windows.reviewer.whiteboard.compose

import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ichi2.anki.R
import com.ichi2.anki.ui.windows.reviewer.whiteboard.BrushInfo
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorBrushButton(
    brush: BrushInfo,
    isSelected: Boolean,
    onClick: (View) -> Unit,
    onLongClick: () -> Unit,
    colorNormal: Color,
    colorHighlight: Color,
    minTouchTargetSize: Dp = 48.dp
) {
    val view = LocalView.current
    val backgroundColor = if (isSelected) colorHighlight else Color.Transparent
    val brushContentDescription = stringResource(
        if (isSelected) R.string.brush_content_description_selected
        else R.string.brush_content_description,
        brush.width.roundToInt()
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .requiredSize(minTouchTargetSize)
            .clip(RoundedCornerShape(100))
            .background(backgroundColor)
            .semantics(mergeDescendants = true) {
                role = Role.Button
                contentDescription = brushContentDescription
            }
            .combinedClickable(
                onClick = { onClick(view) }, onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrushPreviewIcon(
                strokeColor = colorNormal, fillColor = Color(brush.color), size = 18.dp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = brush.width.roundToInt().toString(), color = colorNormal, fontSize = 12.sp
            )
        }
    }
}

@Composable
fun BrushPreviewIcon(
    strokeColor: Color, fillColor: Color, size: Dp
) {
    Box(modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = 2.dp.toPx()
            val paddingPx = 2.dp.toPx()

            // Outer Ring
            drawCircle(
                color = strokeColor, style = Stroke(width = strokeWidthPx),
                // Radius is from center. Size is 18dp.
                // If stroke is center-aligned, we need to subtract half stroke width.
                radius = (size.toPx() - strokeWidthPx) / 2
            )

            // Inner Circle
            // Padding is 2dp from the bounds.
            // Radius = (size / 2) - padding
            val innerRadius = (size.toPx() / 2) - paddingPx

            drawCircle(
                color = fillColor, radius = innerRadius
            )
        }
    }
}

@Composable
fun AddBrushButton(
    onClick: () -> Unit, colorNormal: Color, tooltip: String, minTouchTargetSize: Dp = 48.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .requiredSize(minTouchTargetSize)
            .clip(RoundedCornerShape(100))
            .clickable(
                onClick = onClick
            )
            .semantics { contentDescription = tooltip }) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = null,
            tint = colorNormal
        )
    }
}

@Preview
@Composable
fun PreviewColorBrushButton() {
    Column {
        ColorBrushButton(
            brush = BrushInfo(android.graphics.Color.RED, 12f),
            isSelected = false,
            onClick = {},
            onLongClick = {},
            colorNormal = Color.Black,
            colorHighlight = Color.LightGray
        )
        ColorBrushButton(
            brush = BrushInfo(android.graphics.Color.BLUE, 25f),
            isSelected = true,
            onClick = {},
            onLongClick = {},
            colorNormal = Color.Black,
            colorHighlight = Color.LightGray
        )
    }
}
