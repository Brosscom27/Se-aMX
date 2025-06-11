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

// Muestra la pantalla con la cámara
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(navController: NavController) {
    val context = LocalContext.current

    // Estado que indica si se tiene permiso de cámara
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Lanza el permiso de cámara al usuario
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // Estado para saber si se debe capturar una foto
    var capturarFoto by remember { mutableStateOf(false) }

    // Almacena la imagen capturada como Bitmap
    var imagenCapturada by remember { mutableStateOf<Bitmap?>(null) }

    // Estructura visual con barra superior e inferior
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
            BottomNavigationBar(navController) // Barra de navegación inferior
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
                    // Caja que contiene la cámara
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

                    // Botón para capturar la imagen
                    Button(onClick = { capturarFoto = true }) {
                        Text("Capturar letra")
                    }

                    // Mostrar imagen capturada si existe
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
                    // Mostrar mensaje si no hay permisos
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
    val previewView = remember { PreviewView(context) } // Vista de la cámara

    // Mostrar cámara en un AndroidView
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

                // Configurar el preview de la cámara
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // Configurar análisis de imagen
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
                    // Desvincular y vincular nuevamente la cámara
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

// Implementa el análisis de cada frame de la cámara
class MyImageAnalyzer(
    private val context: Context,
    private val capturarFoto: State<Boolean>,
    private val onLetraDetectada: (String) -> Unit,
    private val onImagenCapturada: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    // Cargar el modelo de TensorFlow Lite
    private val interpreter: Interpreter by lazy {
        val afd = context.assets.openFd("lenguajeSenas.tflite")
        val inputStream = afd.createInputStream()
        val fileChannel = inputStream.channel
        val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.declaredLength)
        Interpreter(modelBuffer)
    }

    // Etiquetas del modelo
    private val labels = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H",
        "I", "J", "M", "N", "O", "P",
        "Q", "R", "S", "T", "U", "V", "W", "X"
    )

    // Ejecuta en cada frame de la cámara
    override fun analyze(image: ImageProxy) {
        // Si no se solicitó capturar foto, cerrar imagen y salir
        if (!capturarFoto.value) {
            image.close()
            return
        }

        try {
            val bitmap = image.toBitmap() // Convertir imagen a bitmap

            // Procesar la imagen: convertir a binario HSV, redimensionar
            val hsvBinary = bitmap.toHSVBinary()
            val resized = Bitmap.createScaledBitmap(hsvBinary, 40, 40, true)

            // Mostrar imagen al usuario
            onImagenCapturada(resized)

            // Crear buffer para entrada del modelo
            val inputBuffer = ByteBuffer.allocateDirect(40 * 40 * 4).order(ByteOrder.nativeOrder())
            for (y in 0 until 40) {
                for (x in 0 until 40) {
                    val pixel = resized.getPixel(x, y)
                    val gray = if (Color.red(pixel) == 0) 0f else 1f // Binarización
                    inputBuffer.putFloat(gray)
                }
            }

            val output = Array(1) { FloatArray(24) } // Salida del modelo
            interpreter.run(inputBuffer, output) // Ejecutar modelo

            val pred = output[0]
            val maxIndex = pred.indices.maxByOrNull { pred[it] } ?: -1 // Índice con mayor probabilidad
            val letter = labels.getOrNull(maxIndex) ?: "?" // Obtener letra

            onLetraDetectada(letter) // Devolver letra detectada

        } catch (e: Exception) {
            Log.e("MyImageAnalyzer", "Error en análisis", e)
        } finally {
            image.close() // Siempre cerrar el frame
        }
    }
}

// Extensión para convertir un ImageProxy a Bitmap
fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val vuBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)
    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    // Convertir el array NV21 a imagen JPEG y luego a Bitmap
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 90, out)

    val imageBytes = out.toByteArray()
    val originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

    // Rotar la imagen si es necesario
    val matrix = Matrix().apply {
        postRotate(imageInfo.rotationDegrees.toFloat())
    }

    return Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true)
}

// Extensión para convertir un Bitmap a imagen binarizada en HSV
fun Bitmap.toHSVBinary(threshold: Float = 0.5f): Bitmap {
    val width = this.width
    val height = this.height
    val binaryBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val hsv = FloatArray(3)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = getPixel(x, y)
            Color.colorToHSV(pixel, hsv)
            val v = hsv[2] // Canal de luminosidad

            // Si la luminosidad es mayor al umbral es negro, si no blanco
            val color = if (v > threshold) Color.BLACK else Color.WHITE
            binaryBitmap.setPixel(x, y, color)
        }
    }

    return binaryBitmap
}

