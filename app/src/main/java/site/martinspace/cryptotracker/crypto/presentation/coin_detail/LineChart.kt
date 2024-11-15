package site.martinspace.cryptotracker.crypto.presentation.coin_detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.martinspace.cryptotracker.crypto.domain.CoinPrice
import site.martinspace.cryptotracker.ui.theme.CryptoTrackerTheme
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun LineChart(
    dataPoints: List<DataPoint>,
    style: ChartStyle,
    visibleDataPointIndices: IntRange,
    unit: String,
    modifier: Modifier = Modifier,
    selectedDataPoint: DataPoint? = null,
    onSelectedDataPoint: (DataPoint) -> Unit = {},
    onXLabelWidthChange: (Float) -> Unit = {},
    showHelperLines: Boolean = true
) {
    val textStyle = LocalTextStyle.current.copy(
        fontSize = style.labelFontSize
    )

    val visibleDataPoints = remember(dataPoints, visibleDataPointIndices) {
        dataPoints.slice(visibleDataPointIndices)
    }

    val maxYValue = remember(visibleDataPoints) {
        visibleDataPoints.maxOfOrNull { it.y } ?: 0f
    }

    val minYValue = remember(visibleDataPoints) {
        visibleDataPoints.minOfOrNull { it.y } ?: 0f
    }

    val measurer = rememberTextMeasurer()

    var xLabelWidth by remember {
        mutableFloatStateOf(0f)
    }
    LaunchedEffect(key1 = xLabelWidth) {
        onXLabelWidthChange(xLabelWidth)
    }

    val selectedDataPointIndex = remember(selectedDataPoint) {
        dataPoints.indexOf(selectedDataPoint)
    }

    var drawPoints by remember {
        mutableStateOf(listOf<DataPoint>())
    }
    var isShowingDataPoints by remember {
        mutableStateOf(selectedDataPoint != null)
    }

    Canvas(
        modifier = modifier.fillMaxSize().pointerInput(
            drawPoints, xLabelWidth
        ) {
            detectHorizontalDragGestures {change, _ ->
                val newSelectedDataPointIndex = getSelectedDataPointIndex(
                    touchOffsetX = change.position.x,
                    triggerWidth = xLabelWidth,
                    drawPoints = drawPoints
                )
                isShowingDataPoints = (newSelectedDataPointIndex + visibleDataPointIndices.first) in visibleDataPointIndices
                if (isShowingDataPoints) {
                    onSelectedDataPoint(drawPoints[newSelectedDataPointIndex])
                }
            }
        }
    ) {
        val minLabelSpacingYPx = style.minYLabelSpacing.toPx()
        val verticalPaddingPx = style.verticalPadding.toPx()
        val horizontalPaddingPx = style.horizontalPadding.toPx()
        val xAxisLabelSpacingPx = style.xAxisLabelSpacing.toPx()

        val xLabelTextLayoutResults = visibleDataPoints.map {
            measurer.measure(
                text = it.xLabel, style = textStyle.copy(textAlign = TextAlign.Center)
            )
        }
        val maxXLabelWidth = xLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0f
        val maxXLabelHeight = xLabelTextLayoutResults.maxOfOrNull { it.size.height } ?: 0f
        val maxXLabelLineCount = xLabelTextLayoutResults.maxOfOrNull { it.lineCount } ?: 0f
        val xLabelLineHeight = maxXLabelHeight.toDouble() / maxXLabelLineCount.toDouble()

        val viewPortHeightPx =
            size.height - (maxXLabelHeight.toFloat() + (2 * verticalPaddingPx) + xLabelLineHeight + xAxisLabelSpacingPx)

        // Y-Label calculation
        val labelViewPortHeightPx = viewPortHeightPx + xLabelLineHeight
        val labelCountExcludingLastLabel =
            ((labelViewPortHeightPx / (xLabelLineHeight + minLabelSpacingYPx))).toInt()

        val valueIncrement = (maxYValue - minYValue) / labelCountExcludingLastLabel

        val yLabels = (0..labelCountExcludingLastLabel).map {
            ValueLabel(
                value = maxYValue - (valueIncrement * it), unit = unit
            )
        }

        val yLabelTextLayoutResults = yLabels.map {
            measurer.measure(
                text = it.formatted(), style = textStyle
            )
        }

        val maxYLabelWidth = yLabelTextLayoutResults.maxOfOrNull { it.size.width } ?: 0f

        val viewPortTopY = verticalPaddingPx.toFloat() + xLabelLineHeight.toFloat() + 10f
        val viewPortRightX = size.width
        val viewPortBottomY = viewPortTopY + viewPortHeightPx
        val viewPortLeftX = 2f * horizontalPaddingPx.toFloat() + maxYLabelWidth.toFloat()
        val viewPort = Rect(
            left = viewPortLeftX,
            top = viewPortTopY,
            right = viewPortRightX,
            bottom = viewPortBottomY.toFloat()
        )

        drawRect(
            color = Color.Green.copy(alpha = 0.3f), topLeft = viewPort.topLeft, size = viewPort.size
        )

        xLabelWidth = maxXLabelWidth.toFloat() + xAxisLabelSpacingPx
        xLabelTextLayoutResults.forEachIndexed { index, result ->
            val x = viewPortLeftX + xAxisLabelSpacingPx / 2f + xLabelWidth * index
            val y = viewPortBottomY + xAxisLabelSpacingPx
            drawText(
                textLayoutResult = result, topLeft = Offset(
                    x = x.toFloat(), y = y.toFloat()
                ), color = if (index == selectedDataPointIndex) {
                    style.selectedColor
                } else {
                    style.unselectedColor
                }
            )

            if (showHelperLines) {
                drawLine(
                    color = if (index == selectedDataPointIndex) {
                        style.selectedColor
                    } else {
                        style.unselectedColor
                    }, start = Offset(
                        x = x.toFloat() + result.size.width.toFloat() / 2f, y = viewPortTopY
                    ), end = Offset(
                        x = x.toFloat() + result.size.width.toFloat() / 2f,
                        y = viewPortBottomY.toFloat()
                    ), strokeWidth = if (index == selectedDataPointIndex) {
                        style.helperLinesThicknessPx * 1.5f
                    } else {
                        style.helperLinesThicknessPx
                    }
                )
            }

            if (selectedDataPointIndex == index) {
                val valueLabel = ValueLabel(
                    value = visibleDataPoints[index].y, unit = unit
                )
                val valueResult = measurer.measure(
                    text = valueLabel.formatted(), style = textStyle.copy(
                        color = style.selectedColor
                    ), maxLines = 1
                )

                val textPositionX = if (selectedDataPointIndex == visibleDataPointIndices.last) {
                    x - valueResult.size.width
                } else {
                    x - valueResult.size.width / 2f
                } + result.size.width / 2f

                val isTextInVisibleRange =
                    (size.width - textPositionX).roundToInt() in 0..size.width.roundToInt()

                if (isTextInVisibleRange) {
                    drawText(
                        textLayoutResult = valueResult,
                        topLeft = Offset(
                            x = textPositionX, y = viewPortTopY - valueResult.size.height - 10f
                        ),
                    )
                }
            }
        }


        val heightRequiredForLabels = xLabelLineHeight * (labelCountExcludingLastLabel + 1)
        val remainingHeightForLabels = labelViewPortHeightPx - heightRequiredForLabels
        val spaceBetweenLabels = remainingHeightForLabels / labelCountExcludingLastLabel


        yLabelTextLayoutResults.forEachIndexed { index, result ->
            val x = horizontalPaddingPx + maxYLabelWidth.toFloat() - result.size.width
            val y =
                viewPortTopY + index * (xLabelLineHeight + spaceBetweenLabels) - xLabelLineHeight / 2f
            drawText(
                textLayoutResult = result, topLeft = Offset(
                    x = x.toFloat(), y = y.toFloat()
                ), color = style.unselectedColor
            )

            if (showHelperLines) {
                drawLine(
                    color = style.unselectedColor, start = Offset(
                        x = viewPortLeftX.toFloat(),
                        y = y.toFloat() + result.size.height.toFloat() / 2f
                    ), end = Offset(
                        x = viewPortRightX, y = y.toFloat() + result.size.height.toFloat() / 2f
                    )
                )
            }
        }

        drawPoints = visibleDataPointIndices.map {
            val x =
                viewPortLeftX + (it - visibleDataPointIndices.first) * xLabelWidth + xLabelWidth / 2f

            // [minYValue; maxYValue] -> [0;1]
            // [0; 1000] -> [0; 1]
            val ratio = (dataPoints[it].y - minYValue) / (maxYValue - minYValue)
            val y = viewPortBottomY - (ratio * viewPortHeightPx)
            DataPoint(
                x = x,
                y = y.toFloat(),
                xLabel = dataPoints[it].xLabel
            )
        }

        val conPoints1 = mutableListOf<DataPoint>()
        val conPoints2 = mutableListOf<DataPoint>()
        for (i in 1 until drawPoints.size) {
            val p0 = drawPoints[i - 1]
            val p1 = drawPoints[i]

            val x = (p1.x + p0.x) / 2f
            val y1 = p0.y
            val y2 = p1.y

            conPoints1.add(DataPoint(x, y1, ""))
            conPoints2.add(DataPoint(x, y2, ""))
        }

        val linePath = androidx.compose.ui.graphics.Path().apply {
            if (drawPoints.isNotEmpty()) {
                moveTo(drawPoints.first().x, drawPoints.first().y)

                for (i in 1 until drawPoints.size) {
                    cubicTo(
                        conPoints1[i - 1].x,
                        conPoints1[i - 1].y,
                        conPoints2[i - 1].x,
                        conPoints2[i - 1].y,
                        drawPoints[i].x,
                        drawPoints[i].y
                    )
                }
            }
        }

        drawPath(
            path = linePath,
            color = style.chartLineColor,
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        drawPoints.forEachIndexed { index, point ->
            if (isShowingDataPoints) {
                val circleOffset = Offset(
                    x = point.x, y = point.y
                )

                drawCircle(
                    color = style.selectedColor,
                    radius = 10f,
                    center = circleOffset
                )

                if (selectedDataPointIndex == index) {
                    drawCircle(
                        color = Color.White,
                        radius = 15f,
                        center = circleOffset
                    )
                    drawCircle(
                        color = style.selectedColor,
                        radius = 15f,
                        center = circleOffset,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

private fun getSelectedDataPointIndex(
    touchOffsetX: Float,
    triggerWidth: Float,
    drawPoints: List<DataPoint>
): Int {
    val triggerRangeLeft = touchOffsetX - triggerWidth / 2f
    val triggerRangeRight = touchOffsetX + triggerWidth / 2f
    return drawPoints.indexOfFirst {
        it.x in triggerRangeLeft..triggerRangeRight
    }
}

@Preview(widthDp = 500)
@Composable
private fun LineChartPreview() {
    CryptoTrackerTheme {
        val coinHistoryRandomized = remember {
            (1..7).map {
                CoinPrice(
                    priceUsd = Random.nextFloat() * 1000.0,
                    dateTime = ZonedDateTime.now().plusHours(it.toLong())
                )
            }
        }
        val style = ChartStyle(
            chartLineColor = Color.Black,
            unselectedColor = Color(0xFF7C7C7C),
            selectedColor = Color.Black,
            helperLinesThicknessPx = Stroke.HairlineWidth,
            axisLinesThickness = 5f,
            labelFontSize = 14.sp,
            minYLabelSpacing = 25.dp,
            verticalPadding = 8.dp,
            horizontalPadding = 8.dp,
            xAxisLabelSpacing = 8.dp
        )
        val dataPoints = remember {
            coinHistoryRandomized.map {
                DataPoint(
                    x = it.dateTime.hour.toFloat(),
                    y = it.priceUsd.toFloat(),
                    xLabel = DateTimeFormatter.ofPattern("ha\nM/d").format(it.dateTime),
                )
            }
        }
        LineChart(
            dataPoints = dataPoints,
            style = style,
            visibleDataPointIndices = 0..6,
            unit = "$",
            modifier = Modifier
                .width(700.dp)
                .height(300.dp)
                .background(Color.White),
            selectedDataPoint = dataPoints[2]
        )
    }
}