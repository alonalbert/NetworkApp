package com.alonalbert.networkapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.alonalbert.networkapp.ui.theme.NetworkAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetworkAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    App()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun App() {
        val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) {
            Column(modifier = Modifier.padding(it)) {
                Button(onClick = { callEchoService(snackbarHostState, "Foo") }) {
                    Text("Echo Foo")
                }
                Button(onClick = { callEchoService(snackbarHostState, "Bar") }) {
                    Text("Echo Bar")
                }
                Button(onClick = { callGoogle(snackbarHostState) }) {
                    Text("Google Search")
                }
                Button(onClick = { callLarge(snackbarHostState) }) {
                    Text("Large HTML")
                }
            }
        }
    }

    private fun callEchoService(snackbarHostState: SnackbarHostState, response: String) {
        val query = """{"body":{"type":"text","data":"$response"},"status":200}"""
        val encoded = URLEncoder.encode(query, Charset.defaultCharset().name())
        makeRequest("https://echoserver.dev/server?query=$encoded") { rc, content ->
            Log.i("NetworkApp", "Content: $content")
            snackbarHostState.showSnackbar("RC=$rc Content=$content")
        }
    }

    private fun callGoogle(snackbarHostState: SnackbarHostState) {
        val url = "https://www.google.com/search?q=java"
        makeRequest(url) { rc, content ->
            val contentLength = content?.length ?: 0
            Log.i("NetworkApp", "Content: $contentLength")
            snackbarHostState.showSnackbar("RC=$rc Content=$contentLength")
        }
    }

    private fun callLarge(snackbarHostState: SnackbarHostState) {
        val url = "https://demo.borland.com/testsite/stadyn_largepagewithimages.html"
        makeRequest(url) { rc, content ->
            val contentLength = content?.length ?: 0
            Log.i("NetworkApp", "Content: $contentLength")
            snackbarHostState.showSnackbar("RC=$rc Content=$contentLength")
        }
    }

    private fun makeRequest(url: String, block: suspend (Int, String?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            val connection = URL(url).openConnection() as HttpURLConnection
            val rc = connection.responseCode
            Log.i("NetworkApp", "Response: $rc")

            val content = when (rc) {
                200 -> connection.inputStream.reader().use { it.readText() }
                else -> null
            }
            block(rc, content)
        }
    }
}
