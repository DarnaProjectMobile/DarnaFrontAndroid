package com.sim.darna

import android.app.Application
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import org.osmdroid.config.Configuration

class DarnaApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Configure osmdroid (OpenStreetMap) with a valid user agent
        val ctx = applicationContext
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        Configuration.getInstance().load(ctx, prefs)
        Configuration.getInstance().userAgentValue = ctx.packageName
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(GifDecoder.Factory())
            }
            .allowRgb565(false) // Use ARGB_8888 for better color quality
            .build()
    }
}

