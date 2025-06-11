package com.example.proyecto.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.proyecto.data.HistoryRepository
import com.example.proyecto.ui.navigation.BottomNavigationBar
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    var capturarFoto by remember { mutableStateOf(false) }
    var imagenCapturada by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Captura de imagen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                    ) {
                        CameraPreview(
                            context = context,
                            capturarFoto = rememberUpdatedState(capturarFoto),
                            onFotoProcesada = { letra ->
                                HistoryRepository.addToHistory(letra)
                                capturarFoto = false
                            },
                            onImagenCapturada = { bitmap ->
                                imagenCapturada = bitmap
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { capturarFoto = true }) {
                        Text("Capturar letra")
                    }

                    imagenCapturada?.let { bitmap ->
                        Spacer(modifier = Modifier.height(16.dp))
                        AndroidView(
                            factory = { context ->
                                android.widget.ImageView(context).apply {
                                    setImageBitmap(bitmap)
                                    layoutParams = FrameLayout.LayoutParams(200, 200)
                                    scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
                                }
                            }
                        )
                    }

                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Se requiere permiso para acceder a la cámara.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Solicitar permiso")
                    }
                }
            }
        }
    )
}

@Composable
fun CameraPreview(
    context: Context,
    capturarFoto: State<Boolean>,
    onFotoProcesada: (String) -> Unit,
    onImagenCapturada: (Bitmap) -> Unit
) {
    val previewView = remember { PreviewView(context) }

    AndroidView(factory = {
        previewView.apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }.also {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = ImageAnalysis.Builder()
                    .setTargetResolution(Size(640, 480))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(
                            ContextCompat.getMainExecutor(context),
                            MyImageAnalyzer(context, capturarFoto, onFotoProcesada, onImagenCapturada)
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        context as LifecycleOwner,
                        cameraSelector,
                        preview,
                        analyzer
                    )
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Error al mostrar cámara", e)
                }

            }, ContextCompat.getMainExecutor(context))
        }
    })
}

class MyImageAnalyzer(
    private val context: Context,
    private val capturarFoto: State<Boolean>,
    private val onLetraDetectada: (String) -> Unit,
    private val onImagenCapturada: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private val interpreter: Interpreter by lazy {
        val afd = context.assets.openFd("lenguajeSenas.tflite")
        val inputStream = afd.createInputStream()
        val fileChannel = inputStream.channel
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
        Interpreter(modelBuffer)
    }

    private val labels = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H",
        "I", "J", "M", "N", "O", "P",
        "Q", "R", "S", "T", "U", "V", "W", "X"
    )

    override fun analyze(image: ImageProxy) {
        if (!capturarFoto.value) {
            image.close()
            return
        }

        try {
            val bitmap = image.toBitmap()

            // 🔄 Convertir a HSV, binarizar, y redimensionar
            val hsvBinary = bitmap.toHSVBinary()
            val resized = Bitmap.createScaledBitmap(hsvBinary, 40, 40, true)

            // Mostrar imagen binarizada
            onImagenCapturada(resized)

            // Preparar buffer de entrada
            val inputBuffer = ByteBuffer.allocateDirect(40 * 40 * 4).order(ByteOrder.nativeOrder())
            for (y in 0 until 40) {
                for (x in 0 until 40) {
                    val pixel = resized.getPixel(x, y)
                    val gray = if (Color.red(pixel) == 0) 0f else 1f
                    inputBuffer.putFloat(gray)
                }
            }

            val output = Array(1) { FloatArray(24) }
            interpreter.run(inputBuffer, output)

            val pred = output[0]
            val maxIndex = pred.indices.maxByOrNull { pred[it] } ?: -1
            val letter = labels.getOrNull(maxIndex) ?: "?"

            onLetraDetectada(letter)

        } catch (e: Exception) {
            Log.e("MyImageAnalyzer", "Error en análisis", e)
        } finally {
            image.close()
        }
    }
}

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val vuBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)
    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)

    val imageBytes = out.toByteArray()
    val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
    }

    return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
}

fun Bitmap.toHSVBinary(threshold: Float = 0.5f): Bitmap {
    val width = this.width
    val height = this.height
    val binaryBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val hsv = FloatArray(3)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = getPixel(x, y)
            Color.colorToHSV(pixel, hsv)
            val v = hsv[2] // Canal de valor

            // Invertimos: fondo oscuro, mano blanca
            val color = if (v > threshold) Color.BLACK else Color.WHITE
            binaryBitmap.setPixel(x, y, color)
        }
    }

    return binaryBitmap
}
