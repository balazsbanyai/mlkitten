package com.realeyes.mltestapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.common.sdkinternal.ModelType
import com.realeyes.mltestapp.ui.theme.MltestappTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MltestappTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    LazyVerticalGrid(
                        cells = GridCells.Adaptive(260.dp),
                        contentPadding = PaddingValues(
                            16.dp
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                       val i = listOf(
                           R.drawable.ad_1,
                           R.drawable.ad_1_crop,
                           R.drawable.ad_land_2,
                           R.drawable.ad_3,
                           R.drawable.ad_3_crop,
                           R.drawable.ad_4,
                           R.drawable.ad_4_crop,
                           R.drawable.ad_land_5,
                           R.drawable.ad_6,
                           R.drawable.ad_6_crop,
                           R.drawable.ad_7,
                           R.drawable.ad_7_crop,
                           R.drawable.ad_8_land,
                           R.drawable.ad_9,
                           R.drawable.ad_9_crop,
                           R.drawable.apple
                       )
                       items(i.size) {
                           val resid = i[it]
                           Surface(onClick = {
                               startActivity(DetailActivity.create(this@MainActivity, resid))
                           }) {
                               Image(
                                   modifier = Modifier.padding(16.dp),
                                   painter = painterResource(id = resid),
                                   contentDescription = resources.getResourceName(resid).split("/").last()
                               )
                           }

                       }
                    }
                }
            }
        }
    }
}
