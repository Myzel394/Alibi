package app.myzel394.alibi.ui.utils

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource

class IconResource private constructor(
    @DrawableRes private val resID: Int?,
    private val imageVector: ImageVector?
) {

    @Composable
    fun asPainterResource(): Painter {
        resID?.let {
            return painterResource(id = resID)
        }
        return rememberVectorPainter(image = imageVector!!)
    }

    companion object {
        fun fromDrawableResource(@DrawableRes resID: Int): IconResource {
            return IconResource(resID, null)
        }

        fun fromImageVector(imageVector: ImageVector?): IconResource {
            return IconResource(null, imageVector)
        }
    }
}
