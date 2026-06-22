package org.openeel.demolaunchableapp.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import io.ktor.http.Url
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.openeel.demolaunchableapp.LessonDestination
import org.openeel.demolaunchableapp.getActivityContext
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.xapi.ipc.client.XapiIpcMessageBridgeServiceConnectionImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent

@Composable
fun LessonScreen(
    modifier: Modifier = Modifier,
    lesson: LessonDestination
) {
    val context = LocalContext.current.getActivityContext()
    val json = remember {
        Json {
            encodeDefaults = false
        }
    }

    val client = remember {
        XapiResourceIpcClient(
            requestSender = XapiIpcMessageBridgeServiceConnectionImpl(
                context = context,
                intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC).also {
                    it.`package` = "world.respect.app"
                }
            ),
            json = json,
            endpoint = lesson.endpoint?.let { Url(it) }!!,
            auth = lesson.auth!!,
        )
    }

    DisposableEffect(client) {
        onDispose {
            client.close()
        }
    }

    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Text("Actor = ${lesson.actor}")
        Text("Activity ID = ${lesson.activity_id}")
        Text("Auth = ${lesson.auth}")
        Text("Endpoint = ${lesson.endpoint}")

        Button(
            onClick = {
                scope.launch {
                    client.statements.post(
                        listOf(
                            XapiStatement(
                                actor = json.decodeFromString(
                                    XapiAgent.serializer(),
                                    lesson.actor!!
                                ),
                                verb = XapiVerb(id = XapiVerb.ID_COMPLETED),
                                `object` = XapiActivity(
                                    id = lesson.activity_id!!
                                )
                            )
                        )
                    )
                    Log.i("Demo", "Sent completion stmt")
                }
            }
        ) {
            Text("Send completion")
        }

    }

}