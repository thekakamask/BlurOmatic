package com.dcac.bluromatic.data

import android.content.Context

interface AppContainer {
    val bluromaticRepository: BluromaticRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val bluromaticRepository = WorkManagerBluromaticRepository(context)
}