package com.realeyes.mltestapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import com.realeyes.mltestapp.ui.theme.MltestappTheme
import com.realeyes.mltestapp.ui.theme.Typography
import java.io.File
import java.nio.ByteBuffer
import kotlin.concurrent.thread


class DetailActivity : ComponentActivity() {



    companion object {
        private const val EXTRA_IMGRES = "EXTRA_IMGRES"
        fun create(context: Context, imgRes: Int): Intent {
            return Intent(context, DetailActivity::class.java).apply {
                putExtra(EXTRA_IMGRES, imgRes)
            }
        }
    }

    data class Method(
        val name: String,
        val result: LiveData<String>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val imgRes = intent.extras?.getInt(EXTRA_IMGRES)!!
        val decoded = BitmapFactory.decodeResource(resources, imgRes)

        val bu = ByteBuffer.allocate(decoded.byteCount)
        decoded.copyPixelsToBuffer(bu)
        val f = File(getExternalFilesDir(""), "temp.bin")
        println("DRWX TEMP IMG $f")
        f.writeBytes(bu.array())

        val inputImage = InputImage.fromBitmap(decoded, 0)

        val methods: List<Method> = listOf(
            Method(
                "Text recognition",
                Transformations.map(
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                        .process(inputImage)
                        .toLiveData()
                ) {
                    it.text
                }
            ),
            Method(
                "OCR Tesseract",
                tess(decoded)
            ),
            Method(
                "Labeling (default)",
                defaultLabeling(inputImage)
            ),
            Method(
                "Labeling (mnasnet_1.3_224_1)",
                msanet13224(inputImage)
            ),
            Method(
                "Object detection",
                objDet(inputImage)
            ),

        )


        setContent {
            MltestappTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Row {
                        Column {
                            Image(painter = painterResource(id = imgRes), contentDescription = "image")
                        }
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .fillMaxWidth()
                        ) {
                            Details(methods)
                        }
                    }
                }
            }
        }
    }

    private fun Context.copyModelToRealFile(modelName: String): File {
        val targetFolder = File(noBackupFilesDir, "tessdata")
        targetFolder.mkdir()
        val target = File(targetFolder, modelName)

        if (!target.exists()) {
            val modelAsset = assets.open(modelName)
            modelAsset.use { input ->
                target.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }

        return noBackupFilesDir
    }

    private fun tess(inputImage: Bitmap): LiveData<String> {
        val res = MutableLiveData<String>()
        thread {
            // Create Tesseract instance
            val tess = TessBaseAPI()

            val dataPath = copyModelToRealFile("eng.traineddata").absolutePath

            // Initialize API for specified language (can be called multiple times during Tesseract lifetime)
            if (!tess.init(dataPath, "eng")) {
                //
                tess.recycle()
                Log.e("DRWX", "tess: Error initializing Tesseract (wrong data path or language) " )
            }

            // Specify image and then recognize it and get result (can be called multiple times during Tesseract lifetime)
            tess.setImage(inputImage)
            val text = tess.utF8Text

            // Release Tesseract when you don't want to use it anymore
            tess.recycle()
            res.postValue(text)
        }
        return res
    }

    private fun defaultLabeling(inputImage: InputImage): LiveData<String> {
        return ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            .process(inputImage)
            .toLiveData()
            .let {
                Transformations.map(it) {
                    it.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
                }
            }
    }

    private fun msanet13224(inputImage: InputImage): LiveData<String> {
        val localModel = LocalModel.Builder()
            .setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite")
            .build()
        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.5f)
            .setMaxResultCount(5)
            .build()
        val labeler = ImageLabeling.getClient(customImageLabelerOptions)
        return labeler.process(inputImage)
            .toLiveData()
            .let {
                Transformations.map(it) {
                    it.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
                }
            }
    }

    private fun objDet(inputImage: InputImage): LiveData<String> {
        val options = ObjectDetectorOptions.Builder()
            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableMultipleObjects()
            .enableClassification()
            .build()
        return ObjectDetection.getClient(options)
            .process(inputImage)
            .toLiveData()
            .let {
                Transformations.map(it) {
                    it.mapIndexed { i, o ->
                        "Object ${i}: " + o.labels.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
                    }.joinToString("\n")
                }
            }
    }
}

private fun <TResult> Task<TResult>.toLiveData(): LiveData<TResult> {
    val ld = MutableLiveData<TResult>()
    this.addOnCompleteListener { ld.postValue(this.result) }
    return ld
}

@Composable
fun Details(textRecoResult: List<DetailActivity.Method>) {
    textRecoResult.forEach {

        val state = it.result.observeAsState()
        Text(
            modifier = Modifier.padding(16.dp),
            text = it.name,
            style = Typography.h5
        )
        if (state.value != null) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = state.value!!
            )
        } else {
            CircularProgressIndicator()
        }

    }
}
