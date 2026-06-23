package org.openeel.demolaunchableapp.screens

import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.ktor.http.Url
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import org.openeel.demolaunchableapp.LessonDestination
import org.openeel.demolaunchableapp.ext.defaultItemPadding
import org.openeel.demolaunchableapp.ext.prettyResultString
import org.openeel.demolaunchableapp.getActivityContext
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiResult
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import world.respect.xapi.ipc.client.XapiIpcMessageBridgeServiceConnectionImpl
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent

enum class PassFailOption(val verbId: String, val label: String, val isSuccess: Boolean) {
    PASSED(XapiVerb.ID_PASSED, "Passed", true), FAILED(XapiVerb.ID_FAILED, "Failed", false)
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val ipcPackage = lesson.xapiIpcPackage
    val endpointUrl = lesson.endpoint?.let { Url(it) }
    val auth = lesson.auth

    val client = remember(ipcPackage, endpointUrl, auth) {
        if(ipcPackage != null && endpointUrl != null && auth != null) {
            XapiResourceIpcClient(
                requestSender = XapiIpcMessageBridgeServiceConnectionImpl(
                    context = context,
                    intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC).also {
                        it.`package` = ipcPackage
                    }
                ),
                json = json,
                endpoint = endpointUrl,
                auth = auth,
            )
        }else {
            null
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

    val actorObject = remember(lesson.actor) {
        lesson.actor?.let { json.decodeFromString(XapiAgent.serializer(), it) }
    }

    val activityIdVal = lesson.activity_id

    var resultStmtText: String? by remember { mutableStateOf(null) }

    var completedStmtText: String? by remember { mutableStateOf(null) }

    var progressedStmtText: String? by remember { mutableStateOf(null) }


    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "xAPI-IPC Demo",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Activity ID = ${lesson.activity_id}",
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