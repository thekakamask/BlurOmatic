package com.dcac.bluromatic

import android.app.Application
import com.dcac.bluromatic.data.AppContainer
import com.dcac.bluromatic.data.DefaultAppContainer

class BluromaticApplication : Application()  {
    /** AppContainer instance used by the rest of classes to obtain dependencies */
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}