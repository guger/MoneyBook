package at.guger.moneybook.core.ui.shape

import android.annotation.SuppressLint
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.shape.ShapePath

/**
 * A [BottomAppBar] top edge that works with a diamond shaped [FloatingActionButton].
 */
class BottomAppBarCutCornersTopEdge(private val fabMargin: Float, roundedCornerRadius: Float, private val cradleVerticalOffset: Float) :
    BottomAppBarTopEdgeTreatment(fabMargin, roundedCornerRadius, cradleVerticalOffset) {

    @SuppressLint("RestrictedApi")
    override fun getEdgePath(length: Float, center: Float, interpolation: Float, shapePath: ShapePath) {
        if (fabDiameter == 0.0f) {
            shapePath.lineTo(length, 0.0f)
            return
        }

        val diamondSize = fabDiameter / 2.0f
        val middle = center + horizontalOffset

        val verticalOffsetRation = cradleVerticalOffset / diamondSize
        if (verticalOffsetRation >= 1.0f) {
            shapePath.lineTo(length, 0.0f)
            return
        }

        shapePath.lineTo(middle - (fabMargin + diamondSize - cradleVerticalOffset), 0.0f)
        shapePath.lineTo(middle, (diamondSize - cradleVerticalOffset + fabMargin) * interpolation)
        shapePath.lineTo(middle + (fabMargin + diamondSize - cradleVerticalOffset), 0.0f)
        shapePath.lineTo(length, 0.0f)
    }
}