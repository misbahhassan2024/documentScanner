package com.example.transformer1

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.floatListOf
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.transformer1.ui.theme.Transformer1Theme
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PerspectiveTransformationApp()
        }
    }
}

@Composable
fun PerspectiveTransformationApp() {
    val context = LocalContext.current

    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.city_street)
    val bitmapWidthDp = with(LocalDensity.current) { bitmap.width.toDp() }
    val bitmapHeightDp = with(LocalDensity.current) { bitmap.height.toDp() }
    Log.d("Bitmap Info in DP", "Width: ${bitmapWidthDp}, Height: ${bitmapHeightDp}")

    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    val boxWidthDp = with(LocalDensity.current) { (bitmap.width * 0.5f).toDp() }
    val boxWidthPx = with(LocalDensity.current) { boxWidthDp.toPx() }

    val boxHeightPx = boxWidthPx / aspectRatio
    val boxHeightDp = with(LocalDensity.current) { boxHeightPx.toDp() }

    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

    Log.d("Box Dimensions Info: ", "$boxWidthPx $boxHeightPx")

    val srcPoints = floatArrayOf(
        400f, 200f, // Top-left corner shifted inward
        bitmap.width - 200f, 400f, // Top-right corner shifted down
        bitmap.width - 400f, bitmap.height - 200f, // Bottom-right corner shifted inward
        200f, bitmap.height - 400f // Bottom-left corner shifted up
    )

    Log.d("Bitmap Info", "Width: ${imageWidth}, Height: ${imageHeight}")

    var transformedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var boxSize by remember { mutableStateOf<IntSize?>(null) }

    val scaledPoints = if( boxSize != null ) {
        val scaleX = (boxSize?.width?.toFloat() ?: 1f) / bitmap.width
        val scaleY = (boxSize?.height?.toFloat() ?: 1f) / bitmap.height

        srcPoints.mapIndexed { index, value ->
            if (index % 2 == 0) {
                value * scaleX
            } else {
                value * scaleY
            }
        }
    }else {
        srcPoints.toList()
    }

    val pointOffsets = remember {
        mutableStateListOf(
                mutableStateOf(Offset(0f, 0f)),
                mutableStateOf(Offset(50f, 50f)),
                mutableStateOf(Offset(100f, 100f)),
                mutableStateOf(Offset(150f, 150f))
        )
    }

//    pointOffsets[0].value = Offset(scaledPoints[0], scaledPoints[1])
//    pointOffsets[1].value = Offset(scaledPoints[2], scaledPoints[3])
//    pointOffsets[2].value = Offset(scaledPoints[4], scaledPoints[5])
//    pointOffsets[3].value = Offset(scaledPoints[6], scaledPoints[7])



    Log.d("Source Points", srcPoints.joinToString(", "))
    Log.d("Scaled Points", scaledPoints.toString())

    val resultBoxAspectRatio = remember { mutableStateOf<Float?>(null) }
    val handleClick = {
        var origPoints = floatArrayOf()

        pointOffsets.forEach{point ->
            if (boxSize != null) {
                val deScaleX = bitmap.width.toFloat() / boxSize!!.width
                val deScaleY = bitmap.height.toFloat() / boxSize!!.height

                origPoints += point.value.x * deScaleX
                origPoints += point.value.y * deScaleY
            }
        }
        Log.d("Original Points", origPoints.joinToString(", "))

        val xCoordinates = origPoints.filterIndexed { index, _ -> index % 2 == 0 }
        val yCoordinates = origPoints.filterIndexed { index, _ -> index % 2 == 1 }

        val minX = xCoordinates.minOrNull() ?: 0f
        val maxX = xCoordinates.maxOrNull() ?: 0f
        val minY = yCoordinates.minOrNull() ?: 0f
        val maxY = yCoordinates.maxOrNull() ?: 0f

        val bboxWidth = maxX - minX
        val bboxHeight = maxY - minY
        val bBoxAspectRatio = bboxWidth / bboxHeight
        resultBoxAspectRatio.value = bBoxAspectRatio

        val (newWidth, newHeight) = if (bBoxAspectRatio > 1.0) {
            Pair(bitmap.width.toFloat(), bitmap.width.toFloat() / bBoxAspectRatio)
        } else {
            Pair(bitmap.height.toFloat() * bBoxAspectRatio, bitmap.height.toFloat())
        }

        val xOffset = (bitmap.width - newWidth) / 2
        val yOffset = (bitmap.height - newHeight) / 2

        val exp = floatArrayOf(
            xOffset, yOffset,                    // Top-left
            xOffset + newWidth, yOffset,         // Top-right
            xOffset + newWidth, yOffset + newHeight, // Bottom-right
            xOffset, yOffset + newHeight         // Bottom-left
        )
//        val exp = floatArrayOf(
//            0f, 0f,
//            newWidth, 0f,
//            newWidth, newHeight,
//            0f, newHeight
//        )

//        Log.d("X Offset and Y Offset", "$xOffset $yOffset", )
        Log.d("Exp Points", exp.joinToString(", "))

        val dstPoints = floatArrayOf(
            0f, 0f,      // Top-left corner
            imageWidth, 0f,     // Top-right corner
            imageWidth, imageHeight,  // Bottom-right corner
            0f, imageHeight   // Bottom-left corner
        )
        Log.d("Dst Points", dstPoints.joinToString(", "))

        val matrix = Matrix()
        matrix.setPolyToPoly(origPoints, 0, exp, 0, 4)

        val tempBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(tempBitmap)
        canvas.drawBitmap(bitmap, matrix, Paint())

        transformedBitmap = tempBitmap
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.height(26.dp))
        Box(
            modifier = Modifier
                .padding(22.dp)
//                .fillMaxWidth()
//                .aspectRatio(bitmap.width.toFloat() / bitmap.height)
                .onGloballyPositioned { coordinates ->
                    if (boxSize == null) {
                        val size = coordinates.size
                        boxSize = size

                        pointOffsets[0].value = Offset(0f, 0f)
                        pointOffsets[1].value = Offset(size.width.toFloat(), 0f)
                        pointOffsets[2].value = Offset(size.width.toFloat(), size.height.toFloat())
                        pointOffsets[3].value = Offset(0f, size.height.toFloat())

                        Log.d(
                            "Box Size globally",
                            "Width: ${coordinates.size.width}, Height: ${coordinates.size.height}"
                        )
                    }
                }
//                .border(2.dp, Color.White)
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Sample Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(bitmap.width.toFloat() / bitmap.height),
            )
            boxSize?.let { size ->
                Canvas(
                    modifier = Modifier
//                        .fillMaxSize()
                ) {
                    for (i in 0 until pointOffsets.size) {
                        val start = pointOffsets[i].value
                        val end = pointOffsets[(i + 1) % pointOffsets.size].value
                        drawLine(
                            color = Color.Black,
                            start = start,
                            end = end,
                            strokeWidth = 10f
                        )
                        drawLine(
                            color = Color.White,
                            start = start,
                            end = end,
                            strokeWidth = 5f
                        )
                    }
                }
                Log.d("Point Offset", pointOffsets.toString())
                val toCenterPoints = (with(LocalDensity.current) { 20.dp.toPx()} / 2).toInt()
                for (offset in pointOffsets) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    x = offset.value.x.toInt() - toCenterPoints,
                                    y = offset.value.y.toInt() - toCenterPoints
                                )
                            }
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offset.value = Offset(
                                        (offset.value.x + dragAmount.x).coerceIn(
                                            0f,
                                            size.width.toFloat()
                                        ),
                                        (offset.value.y + dragAmount.y).coerceIn(
                                            0f,
                                            size.height.toFloat()
                                        )
                                    )
                                }
                            }
                            .size(20.dp)
                            .background(Color.Magenta, shape = CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {handleClick()}) {
            Text("Apply Transformation")
        }
        if (transformedBitmap != null){
            Text("Transformed Image", style = MaterialTheme.typography.titleMedium)
            Image(
                bitmap = transformedBitmap!!.asImageBitmap(),
                contentDescription = "Transformed Image",
                modifier = Modifier
                    .fillMaxSize()
//                    .aspectRatio(resultBoxAspectRatio.value?: 1f)
                    .padding(16.dp)
            )
        }
    }
}


@Composable
@Preview
fun PerspectiveTransformationAppPreview() {
    PerspectiveTransformationApp()
}
