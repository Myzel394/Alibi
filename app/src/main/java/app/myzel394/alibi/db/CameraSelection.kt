package app.myzel394.alibi.db

import kotlinx.serialization.Serializable

/** Camera IDs are opaque strings, not lens-facing integers. */
@Serializable
data class CameraSelection(val logicalId: String, val physicalId: String? = null)
