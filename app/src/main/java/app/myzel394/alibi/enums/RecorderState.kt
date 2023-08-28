package app.myzel394.alibi.enums

import app.myzel394.alibi.R

enum class RecorderState {
    IDLE,
    RECORDING,
    PAUSED,
}

val ENUM_LABEL_MAP = mapOf(
    RecorderState.IDLE to R.string.ui_audioRecorder_recordingState_value_idle,
    RecorderState.RECORDING to R.string.ui_audioRecorder_recordingState_value_recording,
    RecorderState.PAUSED to R.string.ui_audioRecorder_recordingState_value_paused,
)
