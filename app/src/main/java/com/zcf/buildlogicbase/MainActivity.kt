package com.zcf.buildlogicbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.zcf.buildlogicbase.ui.theme.BuildlogicbaseTheme
import com.zcf.chat.ui.ChatRoute
import dagger.hilt.android.AndroidEntryPoint

/** @AndroidEntryPoint is required, or hiltViewModel() resolves to a non-Hilt factory at runtime. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuildlogicbaseTheme {
                ChatRoute()
            }
        }
    }
}
