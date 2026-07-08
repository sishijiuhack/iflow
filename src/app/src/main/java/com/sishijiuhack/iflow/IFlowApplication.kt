package com.sishijiuhack.iflow

import android.app.Application
import com.sishijiuhack.iflow.core.di.AppContainer

class IFlowApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(applicationContext = this)
    }
}
