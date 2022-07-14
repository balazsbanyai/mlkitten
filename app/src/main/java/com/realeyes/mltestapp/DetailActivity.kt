package com.realeyes.mltestapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.realeyes.mltestapp.ui.theme.MltestappTheme
import com.realeyes.mltestapp.ui.theme.Typography
import java.io.File
import java.nio.ByteBuffer

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

    private fun defaultLabeling(inputImage: InputImage): LiveData<String> {
//        return ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
//            .process(inputImage)
//            .toLiveData()
//            .let {
//                Transformations.map(it) {
//                    it.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
//                }
//            }
        return MutableLiveData("nope")
    }

    private fun msanet13224(inputImage: InputImage): LiveData<String> {
//        val localModel = LocalModel.Builder()
//            .setAssetFilePath("mnasnet_1.3_224_1_metadata_1.tflite")
//            .build()
//        val customImageLabelerOptions = CustomImageLabelerOptions.Builder(localModel)
//            .setConfidenceThreshold(0.5f)
//            .setMaxResultCount(5)
//            .build()
//        val labeler = ImageLabeling.getClient(customImageLabelerOptions)
//        return labeler.process(inputImage)
//            .toLiveData()
//            .let {
//                Transformations.map(it) {
//                    it.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
//                }
//            }
        return MutableLiveData("nope")
    }

    private fun objDet(inputImage: InputImage): LiveData<String> {
//        val options = ObjectDetectorOptions.Builder()
//            .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
//            .enableMultipleObjects()
//            .enableClassification()
//            .build()
//        return ObjectDetection.getClient(options)
//            .process(inputImage)
//            .toLiveData()
//            .let {
//                Transformations.map(it) {
//                    it.mapIndexed { i, o ->
//                        "Object ${i}: " + o.labels.map { "${it.text} ${it.confidence}%" }.joinToString("\n")
//                    }.joinToString("\n")
//                }
//            }
        return MutableLiveData("nope")
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
