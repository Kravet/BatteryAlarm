package com.example.batteryalarm.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BatteryAlarmTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        Box(modifier = Modifier.fillMaxSize())
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    BatteryAlarmTheme {
        MainScreen()
    }
}
