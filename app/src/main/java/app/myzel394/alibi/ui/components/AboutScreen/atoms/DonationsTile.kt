package app.myzel394.alibi.ui.components.AboutScreen.atoms

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.CurrencyFranc
import androidx.compose.material.icons.filled.CurrencyLira
import androidx.compose.material.icons.filled.CurrencyPound
import androidx.compose.material.icons.filled.CurrencyRuble
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.CurrencyYen
import androidx.compose.material.icons.filled.CurrencyYuan
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.CRYPTO_DONATIONS
import app.myzel394.alibi.ui.PUBLIC_KEY

@Composable
fun DonationsTile() {
    var donationsOpened by rememberSaveable {
        mutableStateOf(false)
    }
    val label = stringResource(R.string.ui_about_contribute_donatation)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .semantics {
                contentDescription = label
            }
            .clickable {
                donationsOpened = !donationsOpened
            }
            .background(
                MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
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

        val rotation by animateFloatAsState(
            if (donationsOpened) -180f else 0f,
            label = "iconRotation"
        )

        Icon(
            Icons.Default.ArrowDropDown,
            contentDescription = null,
            modifier = Modifier
                .size(ButtonDefaults.IconSize.times(1.2f))
                .rotate(rotation)
        )
    }

    val clipboardManager =
        LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    AnimatedVisibility(
        visible = donationsOpened,
        enter = expandVertically(),
    ) {
        Column {
            for (crypto in CRYPTO_DONATIONS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            val clip = ClipData.newPlainText("text", crypto.value)
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
                        crypto.key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        crypto.value,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize.times(0.5),
                    )
                }
            }
        }
    }
}
