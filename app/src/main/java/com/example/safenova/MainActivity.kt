package com.example.safenova

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.safenova.navigation.SafeNovaNavGraph
import com.example.safenova.ui.theme.SAFENOVATheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SAFENOVATheme {
                SafeNovaNavGraph()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SafeNovaAppPreview() {
    SAFENOVATheme {
        SafeNovaNavGraph()
    }
}
