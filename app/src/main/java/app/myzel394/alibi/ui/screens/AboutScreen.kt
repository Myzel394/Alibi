package app.myzel394.alibi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyLira
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.CurrencyYuan
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.myzel394.alibi.R
import app.myzel394.alibi.BuildConfig
import kotlin.random.Random

const val GITHUB_URL = "https://github.com/Myzel394/Alibi"
const val CROWDIN_URL = "https://crowdin.com/project/alibi"
const val PUBLIC_KEY = """-----BEGIN PGP PUBLIC KEY BLOCK-----

mDMEZTfvnhYJKwYBBAHaRw8BAQdAi2AiLsTaBoLhnQtY5vi3xBU/H428wbNfBSe+
2dhz3r60Jk15emVsMzk0IDxnaXRodWIuN2Eyb3BAc2ltcGxlbG9naW4uY28+iJkE
ExYKAEEWIQR9BS8nNHwqrNgV0B3NE0dCwel5WQUCZTfvngIbAwUJEswDAAULCQgH
AgIiAgYVCgkICwIEFgIDAQIeBwIXgAAKCRDNE0dCwel5WcS8AQCf9g6eEaut1suW
l6jCLIg3b1nWLckmLJaonM6PruUtigEAmVnFOxMpOZEIcILT8CD2Riy+IVN9gTNH
qOHnaFsu8AK4OARlN++eEgorBgEEAZdVAQUBAQdAe4ffDtRundKH9kam746i2TBu
P9sfb3QVi5QqfK+bek8DAQgHiH4EGBYKACYWIQR9BS8nNHwqrNgV0B3NE0dCwel5
WQUCZTfvngIbDAUJEswDAAAKCRDNE0dCwel5WWwSAQDj4ZAl6bSqwbcptEMYQaPM
MMhMafm446MjkhQioeXw+wEAzA8mS6RBx7IZvu1dirmFHXOEYJclwjyQhNs4uEjq
/Ak=
=ICHe
-----END PGP PUBLIC KEY BLOCK-----"""
const val PUBLIC_KEY_FINGERPRINT = "7D05 2F27 347C 2AAC D815  D01D CD13 4742 C1E9 7959"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.ui_about_title))
                },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(200.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Image(
                        painter = painterResource(R.drawable.launcher_foreground),
                        contentDescription = null,
                    )
                }
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Text(
                    text = "Version %s (%s)".format(
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE.toString()
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    stringResource(R.string.ui_about_contribute_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.ui_about_contribute_message),
                    style = MaterialTheme.typography.titleMedium,
                )

                val githubLabel = stringResource(R.string.accessibility_open_in_browser, GITHUB_URL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = githubLabel
                        }
                        .clickable {
                            uriHandler.openUri(GITHUB_URL)
                        }
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_github),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize.times(1.2f))
                    )
                    Text(
                        stringResource(R.string.ui_about_contribute_development),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                val crowdinLabel =
                    stringResource(R.string.accessibility_open_in_browser, CROWDIN_URL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = crowdinLabel
                        }
                        .clickable {
                            uriHandler.openUri(CROWDIN_URL)
                        }
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_crowdin),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize.times(1.2f))
                    )
                    Text(
                        stringResource(R.string.ui_about_contribute_translation),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                var donationsOpened by rememberSaveable {
                    mutableStateOf(false)
                }
                val donationLabel = stringResource(R.string.ui_about_contribute_donatation)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = donationLabel
                        }
                        .clickable {
                            donationsOpened = !donationsOpened
                        }
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        listOf(
                            Icons.Default.CurrencyBitcoin,
                            Icons.Default.CurrencyFranc,
                            Icons.Default.CurrencyLira,
                            Icons.Default.CurrencyPound,
                            Icons.Default.CurrencyRuble,
                            Icons.Default.CurrencyRupee,
                            Icons.Default.CurrencyYen,
                            Icons.Default.CurrencyYuan,
                        ).asSequence().shuffled().first(),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize.times(1.2f))
                    )
                    Text(
                        stringResource(R.string.ui_about_contribute_donatation),
                        fontWeight = FontWeight.Bold,
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer
                        )
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )

                    Text(
                        stringResource(R.string.ui_about_gpg_key_hint),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    val clipboardManager =
                        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    Text(
                        PUBLIC_KEY_FINGERPRINT,
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant
                            )
                            .padding(8.dp),
                    )
                    Button(
                        onClick = {
                            val clip = ClipData.newPlainText("text", PUBLIC_KEY)
                            clipboardManager.setPrimaryClip(clip)
                        },
                        colors = ButtonDefaults.textButtonColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.ui_about_gpg_key_copy))
                    }
                }
            }
        }
    }
}
