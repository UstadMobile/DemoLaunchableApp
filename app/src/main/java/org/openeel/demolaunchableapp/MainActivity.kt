package org.openeel.demolaunchableapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.Serializable
import org.openeel.demolaunchableapp.screens.HomeScreen
import org.openeel.demolaunchableapp.screens.LearningUnitScreen
import org.openeel.demolaunchableapp.ui.theme.OpenEelDemoLaunchableAppTheme

class MainActivity : ComponentActivity() {

    private val navUriFlow = MutableSharedFlow<Uri>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.data?.also { navUriFlow.tryEmit(it) }
        enableEdgeToEdge()
        setContent {
            OpenEelDemoLaunchableAppTheme {
                OpenEelDemoLaunchableAppApp(
                    onFinish = { this.finish() },
                    navToUris = navUriFlow,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.also { navUriFlow.tryEmit(it) }
    }
}


@Serializable
object HomeDestination

@Serializable
data class LearningUnitDestination(
    val gradeNum: String? = null,
    val lessonNum: String? = null,
    val endpoint: String? = null,
    val actor: String? = null,
    val auth: String? = null,
    val activity_id: String? = null,
    val xapiIpcPackage: String? = null,
)

@Serializable
object FavoritesDestination

@PreviewScreenSizes
@Composable
fun OpenEelDemoLaunchableAppApp(
    onFinish: () -> Unit = {},
    navToUris: Flow<Uri> = emptyFlow()
) {
    val navController = rememberNavController()

    /*
     * Handle link opening. Using Jetpack Compose deeplink navigation has side effects: calling
     * finish results in closing the calling activity (e.g. the launcher app) as well.
     *
     * Link pattern:
     * "${DemoConstants.BASE_URI}/{langCode}/grade/{gradeNum}/learningunits/{lessonNum}/learningunit.html?endpoint={endpoint}&actor={actor}&auth={auth}&activity_id={activity_id}&xapiIpcPackage={xapiIpcPackage}"
     */
    LaunchedEffect(navToUris) {
        navToUris.collect {
            if(!it.toString().startsWith(DemoConstants.BASE_URI))
                return@collect

            val segments = it.pathSegments
            navController.navigate(
                route = LearningUnitDestination(
                    gradeNum = segments.getOrNull(2),
                    lessonNum = segments.getOrNull(4),
                    endpoint = it.getQueryParameter("endpoint"),
                    actor = it.getQueryParameter("actor"),
                    auth = it.getQueryParameter("auth"),
                    activity_id = it.getQueryParameter("activity_id"),
                    xapiIpcPackage = it.getQueryParameter("xapiIpcPackage")
                )
            )
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination
        ) {
            composable<HomeDestination> {
                HomeScreen(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            composable<LearningUnitDestination> { backStackEntry ->
                val learningUnit: LearningUnitDestination = backStackEntry.toRoute()

                LearningUnitScreen(
                    modifier = Modifier
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    learningUnit = learningUnit,
                    onFinish = onFinish,
                )
            }

            composable<FavoritesDestination> {
                Text("Favorites")
            }
        }
    }

}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    OpenEelDemoLaunchableAppTheme {
        Greeting("Android")
    }
}