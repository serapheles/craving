package com.example.craving

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.craving.shared_types.Event
import com.example.craving.shared_types.Event.Initialize
import com.example.craving.shared_types.Event.SetSearch
import com.example.craving.ui.theme.CravingTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CravingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) { Craving() }
            }
        }
    }
}

class MyCore : Core() {
    init {
        viewModelScope.launch {
            update(Initialize())
        }
    }
}

@Composable
fun Craving(core: MyCore = viewModel()) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "I'm Craving...",
                fontSize = 30.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(50.dp),
                textAlign = TextAlign.Center
            )
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
        ) {
            SearchField(core)
            Button(
                onClick = { coroutineScope.launch { core.update(Event.Search()) } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) { Text(text = "Get it", color = Color.White) }
        }
        //Lazy approach due to time constraints.
        Text(text = (core.view?.search_result ?: "").toString(), modifier = Modifier.padding(10.dp))
    }
}

//Probably shouldn't be strings, but maybe
//Look into adding Item to SharedTypes
@Composable
fun ResultColumn(results: List<String>) {
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
    ) {
        items(results) { result ->
            ResultCard(result)
        }
    }
//            Text(text = (core.view?.search_result ?: "0").toString(), modifier = Modifier.padding(10.dp))
}

@Composable
fun ResultCard(result: String) {
    Row(modifier = Modifier.padding(10.dp)) {

    }
}

@Composable
fun SearchField(core: Core = viewModel()) {
    val coroutineScope = rememberCoroutineScope()

    //There's probably a better way of doing this, but this is simple.
    var text by remember { mutableStateOf("") }

    TextField(
        value = text,
        onValueChange = {
            text = it
            coroutineScope.launch { core.update(SetSearch(text)) }
        },
        singleLine = true,
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    CravingTheme { Craving() }
}