package app.myzel394.alibi.tasker.AudioRecorder

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.ui.theme.AlibiTheme
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.enums.ENUM_LABEL_MAP
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputForConfig
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputsForConfig

class AudioRecorderActivity : AppCompatActivity(), TaskerPluginConfig<AudioRecorderInput> {
    override val inputForTasker: TaskerInput<AudioRecorderInput>
        get() = TaskerInput(AudioRecorderInput(selectedState.name))

    override val context: Context
        get() = applicationContext

    override fun assignFromInput(input: TaskerInput<AudioRecorderInput>) {
        input.regular.run {
            selectedState = RecorderState.valueOf(state ?: RecorderState.IDLE.name)
        }
    }

    var selectedState by mutableStateOf(RecorderState.IDLE)

    override fun onStart() {
        super.onStart()

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var selectedState by remember { mutableStateOf(RecorderState.IDLE) }

            AlibiTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 16.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                ) {
                    Box {}
                    Text(
                        "Configure Audio Recorder State",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Box {}

                    var opened by remember { mutableStateOf(false) }
                    Column {
                        Text(
                            getString(R.string.ui_audioRecorder_recorderState_taskerConfiguration_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            getString(R.string.ui_audioRecorder_recorderState_taskerConfiguration_state_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            Button(
                                onClick = {
                                    opened = true
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(
                                        4.dp
                                    ),
                                ),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    getString(ENUM_LABEL_MAP[selectedState]!!)
                                )
                            }
                            DropdownMenu(
                                expanded = opened,
                                onDismissRequest = { opened = false },
                            ) {
                                RecorderState.values().forEach { state ->
                                    DropdownMenuItem(
                                        onClick = {
                                            opened = false

                                            selectedState = state
                                        },
                                        text = {
                                            val resourceId = ENUM_LABEL_MAP[state]!!
                                            val text = getString(resourceId)

                                            Text(text)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(BIG_PRIMARY_BUTTON_SIZE),
                        onClick = {
                            val helper = AudioRecorderHelper(this@AudioRecorderActivity)

                            helper.finishForTasker()
                        },
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(id = R.string.dialog_close_neutral_label)
                        )
                    }
                }
            }
        }
    }
}