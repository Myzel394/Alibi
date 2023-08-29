package app.myzel394.alibi.tasker.AudioRecorder

import app.myzel394.alibi.R
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.tasker.AudioRecorder.AudioRecorderOutput.Companion.VAR_STATE
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputField
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputRoot
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerInputRoot
class AudioRecorderInput(
    @field:TaskerInputField(VAR_STATE, labelResIdName = "ui_audioRecorder_recordingState_label") var state: String? = null
) {
    companion object {
        const val VAR_STATE = "audioRecorderState"
    }
}

@TaskerOutputObject
class AudioRecorderOutput(
    @get:TaskerOutputVariable(VAR_STATE, labelResIdName = "ui_audioRecorder_recordingState_label") val state: String = RecorderState.IDLE.name
) {
    companion object {
        const val VAR_STATE = "audioRecorderState"
    }
}
