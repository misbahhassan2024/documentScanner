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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.transformer1.ui.theme.Transformer1Theme

class ExampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IdealPerspectiveTransformationApp()
        }
    }
}

@Composable
fun IdealPerspectiveTransformationApp() {
    val context = LocalContext.current

    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.img)

    val originalBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

    val originalCanvas = android.graphics.Canvas(originalBitmap)
    originalCanvas.drawBitmap(bitmap, 0f, 0f, null)

    val paint = Paint().apply {
        color = android.graphics.Color.RED
        style = android.graphics.Paint.Style.FILL
    }

    originalCanvas.drawCircle(400f, 200f, 20f, paint)
    originalCanvas.drawCircle(bitmap.width - 200f, 400f, 20f, paint)
    originalCanvas.drawCircle(bitmap.width - 400f, bitmap.height - 200f, 20f, paint)
    originalCanvas.drawCircle(200f, bitmap.height - 400f, 20f, paint)


    val imageWidth = bitmap.width.toFloat()
    val imageHeight = bitmap.height.toFloat()

    val srcPoints = floatArrayOf(
        400f, 200f, // Top-left corner shifted inward
        bitmap.width - 200f, 400f, // Top-right corner shifted down
        bitmap.width - 400f, bitmap.height - 200f, // Bottom-right corner shifted inward
        200f, bitmap.height - 400f // Bottom-left corner shifted up
    )
//    val srcPoints = floatArrayOf(
//        50f, 50f,
//        bitmap.width - 50f, 50f,
//        bitmap.width - 50f, bitmap.height - 50f,
//        50f, bitmap.height - 50f
//    )
    val dstPoints = floatArrayOf(
        0f, 0f,      // Top-left corner
        imageWidth, 0f,     // Top-right corner
        imageWidth, imageHeight,  // Bottom-right corner
        0f, imageHeight     // Bottom-left corner
    )
    Log.d("Bitmap Info", "Width: ${imageWidth}, Height: ${imageHeight}")

    val matrix = Matrix()
    matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

//    val matrixValues = FloatArray(9)
//    matrix.getValues(matrixValues)


    val transformedBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
//    val transformedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    val canvas = android.graphics.Canvas(transformedBitmap)
    canvas.drawBitmap(bitmap, matrix, Paint())

    val srcOffsets = List(srcPoints.size / 2) { i ->
        Offset(srcPoints[i * 2], srcPoints[i * 2 + 1])
    }



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Image(
                bitmap = originalBitmap.asImageBitmap(),
                contentDescription = "Sample Image",
                modifier = Modifier
                    .fillMaxWidth()
            )
//            Canvas(modifier = Modifier.fillMaxWidth()) {
//                srcOffsets.forEachIndexed { index, offset ->
//                    drawCircle(
//                        color = Color.Red,
//                        radius = 10f,
//                        center = offset
//                    )
//                }
//            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Transformed Image", style = MaterialTheme.typography.titleMedium)
        Image(
            bitmap = transformedBitmap.asImageBitmap(),
            contentDescription = "Transformed Image",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
@Preview
fun IdealPerspectiveTransformationAppPreview() {
    IdealPerspectiveTransformationApp()
}
