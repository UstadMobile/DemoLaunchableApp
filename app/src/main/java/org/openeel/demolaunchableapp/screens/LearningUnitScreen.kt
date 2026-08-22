package org.openeel.demolaunchableapp.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import okhttp3.Request
import org.openeel.demolaunchableapp.LearningUnitDestination
import org.openeel.demolaunchableapp.ext.defaultItemPadding
import org.openeel.demolaunchableapp.ext.prettyResultString
import org.openeel.demolaunchableapp.getActivityContext
import org.openeel.lib.ipc.messagebridge.ServiceConnectionMessengerProvider
import org.openeel.libcache.ipc.client.IpcHttpClientImpl
import org.openeel.libcache.ipc.core.HttpIpcIntent
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.xapi.ipc.client.XapiIpcClientBuilder
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys

enum class PassFailOption(val verbId: String, val label: String, val isSuccess: Boolean) {
    PASSED(XapiVerb.ID_PASSED, "Passed", true), FAILED(XapiVerb.ID_FAILED, "Failed", false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningUnitScreen(
    modifier: Modifier = Modifier,
    learningUnit: LearningUnitDestination
) {
    val context = LocalContext.current.getActivityContext()
    val json = remember {
        Json {
            encodeDefaults = false
        }
    }

    val ipcPackage = learningUnit.xapiIpcPackage
    val endpointUrl = learningUnit.endpoint?.let { Url(it) }
    val auth = learningUnit.auth

    val client = remember(ipcPackage, endpointUrl, auth) {
        if(ipcPackage != null && endpointUrl != null && auth != null) {
            XapiIpcClientBuilder(context, endpointUrl.toString())
                .setAuth(auth)
                .setJson(json)
                .setIpcServicePackageName(ipcPackage)
                .build()
        }else {
            null
        }
    }

    val ipcHttpClient = remember(ipcPackage) {
        if(ipcPackage != null) {
            IpcHttpClientImpl(
                outgoingMessengerProvider = ServiceConnectionMessengerProvider(
                    context = context,
                    intent = Intent(HttpIpcIntent.ACTION_HTTP_OVER_IPC_CONNECT).also {
                        it.`package` = ipcPackage
                        it.putExtra(XapiIpcKeys.KEY_CLIENT_PACKAGE, context.packageName)
                    }
                ),
            )
        }else {
            null
        }
    }

    LaunchedEffect(ipcHttpClient) {
        try {
            withContext(Dispatchers.IO) {
                Log.i("DemoHTTP", "sending")
                ipcHttpClient?.newCall(
                    Request.Builder().url("http://localhost:8098/").build()
                )?.execute().also {
                    Log.i("DemoHTTP", "executed")
                }?.also {
                    Log.i("DemoHTTP", "response: ${it.body.string()}")
                }
            }
        }catch(e: Throwable) {
            Log.e("DemoHTTP", "FFS", e)
        }

    }


    DisposableEffect(client) {
        onDispose {
            client?.close()
        }
    }

    val scope = rememberCoroutineScope()

    var scoreString: String by remember { mutableStateOf("") }

    var progressString: String by remember { mutableStateOf("") }

    var resultDropdownExpanded by remember { mutableStateOf(false) }

    var selectedResult by remember { mutableStateOf(PassFailOption.PASSED) }

    val actorObject = remember(learningUnit.actor) {
        try {
            learningUnit.actor?.let { json.decodeFromString(XapiAgent.serializer(), it) }
        }catch(e: Throwable) {
            Log.w("DemoApp", "Could not parse actor: ${e.message}")
            null
        }
    }

    val activityIdVal = learningUnit.activity_id

    var resultStmtText: String? by remember { mutableStateOf(null) }

    var completedStmtText: String? by remember { mutableStateOf(null) }

    var progressedStmtText: String? by remember { mutableStateOf(null) }


    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = buildString {
                learningUnit.gradeNum?.also { append("Grade $it ") }
                learningUnit.lessonNum?.also { append("learning unit #$it") }
            },
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Activity ID = ${learningUnit.activity_id}",
            modifier = Modifier.defaultItemPadding(),
        )
        Text(
            text = "Actor = ${actorObject?.name}",
            modifier = Modifier.defaultItemPadding(),
        )

        HorizontalDivider(Modifier.height(1.dp))

        Text(
            modifier = Modifier.defaultItemPadding(),
            text = "Send result (pass/fail)",
            style = MaterialTheme.typography.bodyLarge,
        )

        OutlinedTextField(
            modifier = Modifier
                .defaultItemPadding()
                .fillMaxWidth(),
            value = scoreString,
            onValueChange = {
                scoreString = it
            },
            label = {
                Text("Score")
            },
            supportingText = {
                Text("Must be between 0 and 1")
            }
        )

        ExposedDropdownMenuBox(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            expanded = resultDropdownExpanded,
            onExpandedChange = { resultDropdownExpanded = it }
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                value = selectedResult.label,
                label = {
                    Text("Verb")
                },
                onValueChange = { },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = resultDropdownExpanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            )

            ExposedDropdownMenu(
                expanded = resultDropdownExpanded,
                onDismissRequest = { resultDropdownExpanded = false}
            ) {
                PassFailOption.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(it.label)
                        },
                        onClick = {
                            selectedResult = it
                            resultDropdownExpanded = false
                        }
                    )
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            onClick = {
                scope.launch {
                    val scoreFloat = scoreString.toFloatOrNull()

                    if(actorObject != null && activityIdVal != null && scoreFloat != null && client != null) {
                        val result = client.statements.post(
                            listOf(
                                XapiStatement(
                                    actor = actorObject,
                                    verb = XapiVerb(id = selectedResult.verbId),
                                    `object` = XapiActivity(id = activityIdVal),
                                    result = XapiResult(
                                        completion = true,
                                        success = selectedResult.isSuccess,
                                        score = XapiResult.Score(
                                            scaled = scoreFloat
                                        )
                                    )
                                )
                            )
                        )

                        resultStmtText = result.prettyResultString()
                    }else {
                        resultStmtText = "Could not send stmt: missing params"
                    }

                }
            }
        ) {
            Text("Send result statement")
        }

        resultStmtText?.also {
            Text(
                text = it,
                modifier = Modifier.defaultItemPadding(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider(Modifier.height(1.dp))

        Text(
            modifier = Modifier.defaultItemPadding(),
            text = "Send completed statement",
            style = MaterialTheme.typography.bodyLarge,
        )

        OutlinedButton(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            onClick = {
                scope.launch {
                    if(actorObject != null && client != null && activityIdVal != null) {
                        val result = client.statements.post(
                            listOf(
                                XapiStatement(
                                    actor = actorObject,
                                    verb = XapiVerb(id = XapiVerb.ID_COMPLETED),
                                    `object` = XapiActivity(id = activityIdVal),
                                )
                            )
                        )

                        completedStmtText = result.prettyResultString()
                    }else {
                        completedStmtText = "Could not send stmt: missing params"
                    }
                }

            }
        ) {
            Text("Send completed statement")
        }

        completedStmtText?.also {
            Text(
                text = it,
                modifier = Modifier.defaultItemPadding(),
                style = MaterialTheme.typography.bodySmall
            )
        }

        HorizontalDivider(Modifier.height(1.dp))

        Text(
            text = "Send progressed statement",
            modifier = Modifier.defaultItemPadding(),
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedTextField(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            value = progressString,
            onValueChange = {
                progressString = it
            },
            label = {
                Text("Progress")
            },
            supportingText = {
                Text("Must be between 0 and 100")
            }
        )

        OutlinedButton(
            modifier = Modifier.defaultItemPadding().fillMaxWidth(),
            onClick = {
                scope.launch {
                    val progressInt = progressString.toIntOrNull()
                    if(progressInt != null && client != null && activityIdVal != null && actorObject != null) {
                        val result = client.statements.post(
                            listOf(
                                XapiStatement(
                                    actor = actorObject,
                                    verb = XapiVerb(id = "http://adlnet.gov/expapi/verbs/progressed"),
                                    `object` = XapiActivity(id = activityIdVal),
                                    result = XapiResult(
                                        extensions = mapOf(
                                            "https://w3id.org/xapi/cmi5/result/extensions/progress" to JsonPrimitive(progressInt)
                                        )
                                    )
                                )
                            )
                        )

                        progressedStmtText = result.prettyResultString()
                    }else {
                        progressedStmtText = "Could not send stmt: missing params"
                    }
                }
            }
        ) {
            Text("Send progressed statement")
        }

        progressedStmtText?.also {
            Text(
                text = it,
                modifier = Modifier.defaultItemPadding(),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

}