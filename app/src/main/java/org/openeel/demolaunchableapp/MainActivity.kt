package org.openeel.demolaunchableapp

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.openeel.demolaunchableapp.screens.HomeScreen
import org.openeel.demolaunchableapp.screens.LessonScreen
import org.openeel.demolaunchableapp.ui.theme.OpenEelDemoLaunchableAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OpenEelDemoLaunchableAppTheme {
                OpenEelDemoLaunchableAppApp()
            }
        }
    }
}


@Serializable
object HomeDestination

@Serializable
data class LessonDestination(
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
fun OpenEelDemoLaunchableAppApp() {
    val navController = rememberNavController()

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeDestination
        ) {
            val uri = "https://demo.openeel.org"

            composable<HomeDestination> {
                HomeScreen(modifier = Modifier.padding(innerPadding))
            }

            composable<LessonDestination>(
                deepLinks = listOf(
                    navDeepLink<LessonDestination>(basePath = "$uri/Lesson")
                )
            ) { backStackEntry ->
                val lesson: LessonDestination = backStackEntry.toRoute()

                LessonScreen(
                    modifier = Modifier.padding(innerPadding)
                        .verticalScroll(rememberScrollState()),
                    lesson = lesson,
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