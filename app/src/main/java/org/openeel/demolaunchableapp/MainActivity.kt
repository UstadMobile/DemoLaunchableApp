package org.openeel.demolaunchableapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.collections.listOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println(
            XapiStatement(
                actor = XapiAgent(),
                verb = XapiVerb(id = ""),
                `object` = XapiActivity(
                    id = ""
                )
            )
        )
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
)

@Serializable
object FavoritesDestination

@Serializable
object ProfileDestination

@PreviewScreenSizes
@Composable
fun OpenEelDemoLaunchableAppApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    val navController = rememberNavController()

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
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
                        modifier = Modifier.padding(innerPadding),
                        lesson = lesson,
                    )
                }

                composable<FavoritesDestination> {
                    Text("Favorites")
                }
            }
        }
    }
}


enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
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