package com.sishijiuhack.iflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.sishijiuhack.iflow.ui.IFlowApp
import com.sishijiuhack.iflow.ui.theme.IFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IFlowTheme {
                IFlowApp()
            }
        }
    }
}
