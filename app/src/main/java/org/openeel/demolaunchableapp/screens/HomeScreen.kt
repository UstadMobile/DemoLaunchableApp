package org.openeel.demolaunchableapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "xAPI-IPC Demo",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            modifier = Modifier.padding(16.dp),
            text = "Hi! Add the app manifest https://demo.openeel.org/appmanifest.json to a compatible " +
                "launcher that supports xAPI-IPC, then you can launch a lesson using the deep link " +
                    "and send/receive xAPI using xAPI-IPC."
        )
    }
}