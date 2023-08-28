package app.myzel394.alibi.tasker.AudioRecorder

import android.content.Context
import app.myzel394.alibi.enums.RecorderState
import com.joaomgcd.taskerpluginlibrary.condition.TaskerPluginRunnerConditionState
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultCondition
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionSatisfied
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultConditionUnsatisfied

class AudioRecorderRunner : TaskerPluginRunnerConditionState<AudioRecorderInput, AudioRecorderOutput>() {
    override fun getSatisfiedCondition(context: Context, input: TaskerInput<AudioRecorderInput>, update: Unit?): TaskerPluginResultCondition<AudioRecorderOutput> {
        return if (state.name == input.regular.state)
            TaskerPluginResultConditionSatisfied(context)
        else TaskerPluginResultConditionUnsatisfied()
    }

    companion object {
        var state: RecorderState = RecorderState.IDLE
        fun change(context: Context, newState: RecorderState) {
            state = newState
            AudioRecorderActivity::class.java.requestQuery(context)
        }
    }
}