package com.sishijiuhack.iflow.core.android

import android.content.Context
import com.sishijiuhack.iflow.IFlowApplication
import com.sishijiuhack.iflow.core.di.AppContainer

fun Context.appContainer(): AppContainer {
    return (applicationContext as IFlowApplication).appContainer
}
