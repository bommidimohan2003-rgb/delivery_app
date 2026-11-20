package com.payent.delivery.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payent.delivery.domain.model.ChartDataPoint
import java.util.Locale

@Composable
fun PayentLineGraph(
    dataPoints: List<ChartDataPoint>,
    selectedPoint: ChartDataPoint?,
    onPointSelected: (ChartDataPoint) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        if (dataPoints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No earnings recorded for this period (Demo)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header Tooltip Badge for Selected Point
                if (selectedPoint != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedPoint.label}: ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", selectedPoint.amountRupees)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${selectedPoint.deliveriesCount} deliveries)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Tap any data point to view period details",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val lineColor = MaterialTheme.colorScheme.primary
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                val innerDotColor = MaterialTheme.colorScheme.surface

                // Canvas Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(dataPoints) {
                                detectTapGestures { tapOffset ->
                                    val width = size.width
                                    val count = dataPoints.size
                                    if (count > 0 && width > 0) {
                                        val stepX = width / count.toFloat()
                                        val index = (tapOffset.x / stepX).toInt().coerceIn(0, count - 1)
                                        onPointSelected(dataPoints[index])
                                    }
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val maxAmount = dataPoints.maxOfOrNull { it.amountRupees }?.takeIf { it > 0 } ?: 1000.0

                        // Draw Grid Horizontal Lines
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = canvasHeight * (i / gridLines.toFloat())
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        // Calculate Coordinates
                        val points = mutableListOf<Offset>()
                        val count = dataPoints.size
                        val stepX = if (count > 1) canvasWidth / (count - 1) else canvasWidth / 2f

                        for (i in dataPoints.indices) {
                            val x = if (count == 1) canvasWidth / 2f else i * stepX
                            val fraction = (dataPoints[i].amountRupees / maxAmount).toFloat()
                            val y = canvasHeight - (fraction * (canvasHeight - 20.dp.toPx())) - 10.dp.toPx()
                            points.add(Offset(x, y))
                        }

                        // Draw Gradient Area under Line
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, canvasHeight)
                                points.forEach { lineTo(it.x, it.y) }
                                lineTo(points.last().x, canvasHeight)
                                close()
                            }
                            drawPath(
                                path = path,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        lineColor.copy(alpha = 0.25f),
                                        lineColor.copy(alpha = 0.02f)
                                    )
                                )
                            )

                            // Draw Line
                            val linePath = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = lineColor,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }

                        // Draw Data Point Circles
                        for (i in points.indices) {
                            val pt = points[i]
                            val isSelected = selectedPoint?.label == dataPoints[i].label

                            if (isSelected) {
                                drawCircle(
                                    color = lineColor.copy(alpha = 0.3f),
                                    radius = 12.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = innerDotColor,
                                    radius = 6.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            } else {
                                drawCircle(
                                    color = lineColor,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // X-Axis Labels Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dataPoints.forEach { point ->
                        val isSelected = selectedPoint?.label == point.label
                        Text(
                            text = point.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
