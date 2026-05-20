package com.appylab.lumi

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LumiApp : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components { add(SvgDecoder.Factory()) }
            .build()

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            Coil.imageLoader(this@LumiApp).execute(
                ImageRequest.Builder(this@LumiApp)
                    .data("file:///android_asset/splashscreen.svg")
                    .build()
            )
        }
    }
}
