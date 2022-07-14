package com.realeyes.mltestapp

import android.content.Context
import com.google.mlkit.common.sdkinternal.MlKitContext
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val moko = mockk<Context>()
        every { moko.applicationContext } returns moko
        every { moko.packageManager } returns mockk()
        MlKitContext.initializeIfNeeded(moko)

        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
}