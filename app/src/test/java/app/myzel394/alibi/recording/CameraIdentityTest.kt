package app.myzel394.alibi.recording

import app.myzel394.alibi.db.CameraSelection
import app.myzel394.alibi.ui.utils.CameraInfo
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class CameraIdentityTest {
    @Test fun cameraZeroCanBeRearAndCameraOneCanBeFront() {
        assertEquals(CameraInfo.Lens.BACK, CameraInfo.fromCameraId("0", 1).lens)
        assertEquals(CameraInfo.Lens.FRONT, CameraInfo.fromCameraId("1", 0).lens)
    }
    @Test fun opaqueCameraIdsAreNotParsedAsNumbers() {
        val camera = CameraInfo.fromCameraId("usb:rear-lens", 1)
        assertEquals("usb:rear-lens", camera.id)
        assertEquals(CameraInfo.Lens.BACK, camera.lens)
    }
    @Test fun physicalSelectionPreservesBothIdsAcrossSerialization() {
        val selected = CameraSelection("logical:back", "physical:tele")
        val stored = Json.encodeToString(CameraSelection.serializer(), selected)
        assertEquals(selected, Json.decodeFromString(CameraSelection.serializer(), stored))
    }
    @Test fun physicalLensAndLogicalAutoSelectionHaveDifferentIdentity() {
        assertNotEquals(CameraSelection("0"), CameraSelection("0", "0"))
        assertNotEquals(CameraInfo("0", CameraInfo.Lens.BACK).selectionKey,
            CameraInfo("0", CameraInfo.Lens.BACK, "0").selectionKey)
    }
}
