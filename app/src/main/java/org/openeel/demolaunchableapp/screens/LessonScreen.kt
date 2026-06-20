package org.openeel.demolaunchableapp.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.openeel.demolaunchableapp.LessonDestination

@Composable
fun LessonScreen(
    modifier: Modifier = Modifier,
    lesson: LessonDestination
) {
    Column(modifier = modifier) {
        Text("Actor = ${lesson.actor}")
        Text("Activity ID = ${lesson.activity_id}")
        Text("Auth = ${lesson.auth}")
        Text("Endpoint = ${lesson.endpoint}")
    }

}