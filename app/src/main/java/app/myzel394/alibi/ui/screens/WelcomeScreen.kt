package app.myzel394.alibi.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.components.WelcomeScreen.pages.ExplanationPage
import app.myzel394.alibi.ui.components.WelcomeScreen.pages.MaxDurationSettingsPage
import app.myzel394.alibi.ui.components.WelcomeScreen.pages.ReadyPage
import app.myzel394.alibi.ui.components.WelcomeScreen.pages.ResponsibilityPage
import app.myzel394.alibi.ui.components.WelcomeScreen.pages.SaveFolderPage
import app.myzel394.alibi.ui.effects.rememberSettings
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WelcomeScreen(
    onNavigateToAudioRecorderScreen: () -> Unit
) {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val settings = rememberSettings()
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
        pageCount = { 5 }
    )

    fun finishTutorial() {
        scope.launch {
            dataStore.updateData {
                settings.setHasSeenOnboarding(true)
            }
            onNavigateToAudioRecorderScreen()
        }
    }

    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
            ) { position ->
                when (position) {
                    0 -> ExplanationPage(
                        onContinue = {
                            scope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )

                    1 -> ResponsibilityPage {
                        scope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    }

                    2 -> MaxDurationSettingsPage {
                        scope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    }

                    3 -> SaveFolderPage(
                        onBack = {
                            scope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        onContinue = { saveFolder ->
                            scope.launch {
                                dataStore.updateData {
                                    settings.setSaveFolder(saveFolder)
                                }

                                pagerState.animateScrollToPage(4)
                            }
                        },
                        appSettings = settings
                    )

                    4 -> ReadyPage {
                        finishTutorial()
                    }
                }
            }
        }
    }
}