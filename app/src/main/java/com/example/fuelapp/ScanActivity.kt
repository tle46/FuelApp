package com.example.fuelapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var overlay: OverlayView
    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    private var isProcessing = false
    private var isScanning = true
    private var lastFrameTime = 0L
    private val frameInterval = 100L // BETWEEN FRAMES
    private val maxScanDuration = 10_000L // TIMEOUT
    private var scanStartTime = 0L
    private val consecutiveResults = mutableListOf<Pair<Double, Double>>()
    private val requiredMatches = 3 // Parser must get requiredMatches consecutive parse results

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scanStartTime = System.currentTimeMillis()

        // Programmatically define activity
        // container is a FrameLayout that contains PreviewView and the custom OverLay view
        val container = FrameLayout(this)

        // Camera preview
        previewView = PreviewView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        // Overlay View (view that renders the rectangles over the camera)
        overlay = OverlayView(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        container.addView(previewView)
        container.addView(overlay)
        setContentView(container)

        if (hasPermission()) {
            // Start camera
            startCamera()
        } else {
            // Request camera permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun startCamera() {
        // Request CameraProvider
        val providerFuture = ProcessCameraProvider.getInstance(this)
        providerFuture.addListener({
            // Get camera provider instance when it is available
            val cameraProvider = providerFuture.get()

            // Build preview and set provider to previewView
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // Analyzer to process frames from camera
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Analyzer runs analyzeImage on background thread
            // analyzeImage continuously polls camera images as fast as it can
            analyzer.setAnalyzer(cameraExecutor, ::analyzeImage)

            try {
                // Bind camera to this activity
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analyzer
                )
            } catch (e: Exception) {
                Log.e("ScanActivity", "Camera binding failed", e)
            }
        },
            ContextCompat.getMainExecutor(this) // Camera runs on the main thread
        )
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        if (!isScanning || isProcessing) {
            imageProxy.close()
            return
        }

        // Timeout because I really don't want to add a button
        if (System.currentTimeMillis() - scanStartTime > maxScanDuration) {
            val intent = Intent()
            setResult(RESULT_CANCELED, intent)
            finish()
            imageProxy.close()
            return
        }

        // Enforce time interval between image frames
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastFrameTime < frameInterval) {
            imageProxy.close()
            return
        }
        lastFrameTime = currentTime

        // Get image and store it in media image
        val mediaImage = imageProxy.image ?: run {
            // Close if imageProxy.image doesn't yield an image
            imageProxy.close()
            return
        }

        isProcessing = true

        // Rotate the image and store it in inputImage
        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        // Process the input image
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val rects = mutableListOf<RectF>()
                val imageWidth = mediaImage.width
                val imageHeight = mediaImage.height

                // Draw all OCR bounding boxes
                for (block in visionText.textBlocks) {
                    // Block of text
                    for (line in block.lines) {
                        // Horizontal line of text within block
                        for (element in line.elements) {
                            // Draw box around each token in line
                            element.boundingBox?.let { box ->
                                rects.add(mapRect(box, imageWidth, imageHeight, rotation))
                            }
                        }
                    }
                }
                overlay.setRects(rects)

                // Parse fuel data
                val text = visionText.text
                Log.d("ScanActivity", "Text: $text")
                val result = parseFuelData(visionText.text)
                if (result.first != null && result.second != null) {
                    val result = Pair(result.first!!, result.second!!)

                    // Add to buffer
                    consecutiveResults.add(result)
                    if (consecutiveResults.size > requiredMatches) {
                        consecutiveResults.removeAt(0)
                    }

                    // Check if all entries in buffer are the same
                    if (consecutiveResults.size == requiredMatches &&
                        consecutiveResults.all { it == result }) {

                        isScanning = false
                        val cost = result.first
                        val gallons = result.second

                        val intent = Intent().apply {
                            putExtra("cost", cost)
                            putExtra("gallons", gallons)
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
            .addOnFailureListener { Log.e("ScanActivity", "OCR failed", it) }
            .addOnCompleteListener {
                isProcessing = false
                imageProxy.close()
            }
    }

    private fun parseFuelData(text: String): Pair<Double?, Double?> {

        data class Match(val value: String, val index: Int, val priority: Int)

        val priceRegexes = listOf(
            """(?<!\d)\d+\.\d{2}(?!\d)""".toRegex(), // highest priority
            """(?<!\d)\d{4}(?!\d)""".toRegex(),
            """(?<!\d)\d{5}(?!\d)""".toRegex(),
            """(?<!\d)\d{3}(?!\d)""".toRegex()
        )

        val fuelRegexes = listOf(
            """(?<!\d)\d+\.\d{3}(?!\d)""".toRegex(), // highest priority
            """(?<!\d)\d{5}(?!\d)""".toRegex(),
            """(?<!\d)\d{4}(?!\d)""".toRegex()
        )

        // List all candidates and sort by priority
        val priceCandidates = priceRegexes.flatMapIndexed { priority, regex ->
            regex.findAll(text).map { Match(it.value, it.range.first, priority) }
        }.sortedWith(compareBy<Match> { it.priority })

        val fuelCandidates = fuelRegexes.flatMapIndexed { priority, regex ->
            regex.findAll(text).map { Match(it.value, it.range.first, priority) }
        }.sortedWith(compareBy<Match> { it.priority })

        var rawCost: String? = null
        var rawFuel: String? = null

        // See if any matches where price before fuel
        for (p in priceCandidates) {
            for (f in fuelCandidates) {
                if (p.value != f.value && p.index < f.index) {
                    rawCost = p.value
                    rawFuel = f.value
                    break
                }
            }
            if (rawCost != null) break
        }

        // Pick any distinct pair if ordering failed
        if (rawCost == null) {
            for (p in priceCandidates) {
                for (f in fuelCandidates) {
                    if (p.value != f.value) {
                        rawCost = p.value
                        rawFuel = f.value
                        break
                    }
                }
                if (rawCost != null) break
            }
        }

        if (rawCost == null || rawFuel == null) return Pair(null, null)

        // Normalize decimals
        if (!rawCost.contains(".")) {
            rawCost = rawCost.dropLast(2) + "." + rawCost.takeLast(2)
        }
        if (!rawFuel.contains(".")) {
            rawFuel = rawFuel.dropLast(3) + "." + rawFuel.takeLast(3)
        }

        val cost = rawCost.toDoubleOrNull()
        val fuel = rawFuel.toDoubleOrNull()

        Log.d("ScanActivity", "rawPrice: $cost, rawFuel: $fuel")

        return if (isValid(Pair(cost, fuel))) Pair(cost, fuel) else Pair(null, null)
    }

    private fun isValid(result: Pair<Double?, Double?>): Boolean {
        val (price, fuel) = result
        if (price != null &&
            fuel != null &&
            price in 0.0..999.9 &&
            fuel in 1.0..999.9 &&
            price != fuel) {
            return true
        } else {
            return false
        }
    }

    private fun findFirstMatch(regexList: List<Regex>, text: String): String? {
        for (regex in regexList) {
            regex.find(text)?.value?.let {
                return it
            }
        }
        return null
    }

    // Maps ML Kit rect to PreviewView coordinates with rotation/scaling
    private fun mapRect(rect: Rect, imageWidth: Int, imageHeight: Int, rotation: Int): RectF {
        val viewWidth = previewView.width.toFloat()
        val viewHeight = previewView.height.toFloat()

        val rotated = rotation == 90 || rotation == 270
        val scaleX = if (rotated) viewWidth / imageHeight.toFloat() else viewWidth / imageWidth.toFloat()
        val scaleY = if (rotated) viewHeight / imageWidth.toFloat() else viewHeight / imageHeight.toFloat()

        val left = rect.left * scaleX
        val top = rect.top * scaleY
        val right = rect.right * scaleX
        val bottom = rect.bottom * scaleY

        return RectF(left, top, right, bottom)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private class OverlayView(context: android.content.Context) : View(context) {
        private val rects = mutableListOf<RectF>()
        private val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        fun setRects(newRects: List<RectF>) {
            rects.clear()
            rects.addAll(newRects)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            for (r in rects) {
                canvas.drawRect(r, paint)
            }
        }
    }
}