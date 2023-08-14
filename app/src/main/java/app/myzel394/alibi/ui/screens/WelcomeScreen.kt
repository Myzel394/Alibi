package app.myzel394.alibi.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.components.WelcomeScreen.atoms.ExplanationPage
import app.myzel394.alibi.ui.components.WelcomeScreen.atoms.ResponsibilityPage
import app.myzel394.alibi.ui.enums.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = {2}
    )

    Scaffold() {padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(state = pagerState) {position ->
                when (position) {
                    0 -> ExplanationPage(
                        onContinue = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        }
                    )
                    1 -> ResponsibilityPage {
                        scope.launch {
                            dataStore.updateData {
                                settings.setHasSeenOnboarding(true)
                            }
                            navController.navigate(Screen.Recorder.route)
                        }
                    }
                }
            }
        }
    }
}