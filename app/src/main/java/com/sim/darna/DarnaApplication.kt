package com.sim.darna

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder

class DarnaApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .allowRgb565(false) // Use ARGB_8888 for better color quality
            .build()
    }
}

