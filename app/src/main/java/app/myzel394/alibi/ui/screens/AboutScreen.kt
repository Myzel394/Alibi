package app.myzel394.alibi.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ContentCopy
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
import app.myzel394.alibi.BuildConfig
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.CONTACT_METHODS
import app.myzel394.alibi.ui.REPO_URL
import app.myzel394.alibi.ui.TRANSLATION_HELP_URL
import app.myzel394.alibi.ui.components.AboutScreen.atoms.DonationsTile
import app.myzel394.alibi.ui.components.AboutScreen.atoms.GPGKeyOverview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackNavigate: () -> Unit
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
                    IconButton(onClick = onBackNavigate) {
                        val label = stringResource(R.string.goBack)
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = label,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp),
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
                    style = MaterialTheme.typography.bodySmall,
                )

                val githubLabel = stringResource(R.string.accessibility_open_in_browser, REPO_URL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = githubLabel
                        }
                        .clickable {
                            uriHandler.openUri(REPO_URL)
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
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(R.string.ui_about_contribute_development),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                val crowdinLabel =
                    stringResource(R.string.accessibility_open_in_browser, TRANSLATION_HELP_URL)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = crowdinLabel
                        }
                        .clickable {
                            uriHandler.openUri(TRANSLATION_HELP_URL)
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
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(R.string.ui_about_contribute_translation),
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }

                DonationsTile()

                Text(
                    stringResource(R.string.ui_about_support_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    stringResource(R.string.ui_about_support_message),
                    style = MaterialTheme.typography.bodySmall,
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val clipboardManager =
                        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

                    for (contact in CONTACT_METHODS) {
                        val name = contact.key
                        val uri = contact.value

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    val clip = ClipData.newPlainText("text", uri)
                                    clipboardManager.setPrimaryClip(clip)
                                }
                                .padding(16.dp)
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                            )
                            Text(
                                name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                uri,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize.times(0.5),
                            )
                        }
                    }
                }

                GPGKeyOverview()
            }
        }
    }
}
