package app.myzel394.alibi.tasker.AudioRecorder

import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper

class AudioRecorderHelper(config: TaskerPluginConfig<AudioRecorderInput>) : TaskerPluginConfigHelper<AudioRecorderInput, AudioRecorderOutput, AudioRecorderRunner>(config) {
    override val runnerClass = AudioRecorderRunner::class.java
    override val inputClass = AudioRecorderInput::class.java
    override val outputClass = AudioRecorderOutput::class.java
}