package app.myzel394.alibi.enums

enum class RecorderState {
    STOPPED,
    RECORDING,
    PAUSED,

    // Only used by the model to indicate that the service is not running
    IDLE
}