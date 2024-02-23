package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import kotlinx.coroutines.launch

@Composable
fun Preview(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    primaryColor: Color,
    textColor: Color,
    onSelect: () -> Unit,
    isSelected: Boolean = false,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .width(100.dp)
                .height(200.dp)
                .clip(shape = RoundedCornerShape(10.dp))
                .border(width = 1.dp, color = textColor, shape = RoundedCornerShape(10.dp))
                .background(backgroundColor)
                .clickable { onSelect() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(30.dp)
                        .height(10.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(primaryColor)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = primaryColor,
                )
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(6.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .height(10.dp)
                        .clip(shape = RoundedCornerShape(10.dp))
                        .background(textColor)
                )
            }
            Box {}
        }
        if (isSelected) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(30.dp),
                )
            }
        }
    }
}

@Composable
fun ThemeSelector() {
    val scope = rememberCoroutineScope()

    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Preview(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = Color(0xFFF0F0F0),
            primaryColor = Color(0xFFAAAAAA),
            textColor = Color(0xFFCCCCCC),
            onSelect = {
                scope.launch {
                    dataStore.updateData {
                        it.setTheme(AppSettings.Theme.LIGHT)
                    }
                }
            },
            isSelected = settings.theme == AppSettings.Theme.LIGHT,
        )
        Preview(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            backgroundColor = Color(0xFF444444),
            primaryColor = Color(0xFF888888),
            textColor = Color(0xFF606060),
            onSelect = {
                scope.launch {
                    dataStore.updateData {
                        it.setTheme(AppSettings.Theme.DARK)
                    }
                }
            },
            isSelected = settings.theme == AppSettings.Theme.DARK,
        )
    }
}