package app.myzel394.locationtest.ui.components.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun<T> ExampleListRoulette(
    items: List<T>,
    onItemSelected: (T) -> Unit,
    renderValue: @Composable (T) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            horizontal = 32.dp,
        ),
    ) {
        items(items.size) {
            val item = items[it]

            Button(
                onClick = {
                    onItemSelected(item)
                },
                colors = ButtonDefaults.textButtonColors(),
                shape = ButtonDefaults.textShape,
                contentPadding = ButtonDefaults.TextButtonContentPadding,
            ) {
                renderValue(item)
            }
        }
    }
}