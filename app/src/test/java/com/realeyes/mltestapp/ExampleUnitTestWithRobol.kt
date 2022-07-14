package com.realeyes.mltestapp

import android.graphics.ImageFormat
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.nio.ByteBuffer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(RobolectricTestRunner::class)
class ExampleUnitTestWithRobol {
    @Test
    fun addition_isCorrect() {
        MlKitContext.initializeIfNeeded(RuntimeEnvironment.getApplication())
        val cl = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val im = javaClass.classLoader!!.getResourceAsStream("temp.bin")
        val imgb = im.readBytes().let { ByteBuffer.wrap(it) }
        println("imb = ${imgb.capacity()}")
        println("ims = ${1080 * 600 * 4}")


        val ima = InputImage.fromByteBuffer(imgb, 1080, 600, 0, InputImage.IMAGE_FORMAT_NV21)
        cl.process(ima)
    }
}