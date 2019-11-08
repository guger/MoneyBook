/*
 * Copyright 2019 Daniel Guger
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.guger.moneybook.core.ui.transition

import android.content.Context
import android.graphics.Path
import android.os.Build
import android.transition.ArcMotion
import android.util.AttributeSet
import androidx.annotation.RequiresApi
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tan


/**
 * A tweak to [ArcMotion] which slightly alters the path calculation. In the real world
 * gravity slows upward motion and accelerates downward motion. This class emulates this behavior
 * to make motion paths appear more natural.
 *
 * See https://www.google.com/design/spec/motion/movement.html#movement-movement-within-screen-bounds
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class GravityArcMotion : ArcMotion {

    //region Variables

    private var mMinimumHorizontalAngle = 0f
    private var mMinimumVerticalAngle = 0f
    private var mMaximumAngle = DEFAULT_MAX_ANGLE_DEGREES
    private var mMinimumHorizontalTangent = 0f
    private var mMinimumVerticalTangent = 0f
    private var mMaximumTangent = DEFAULT_MAX_TANGENT

    //endregion

    constructor()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    /**
     * @inheritDoc
     */
    override fun setMinimumHorizontalAngle(angleInDegrees: Float) {
        mMinimumHorizontalAngle = angleInDegrees
        mMinimumHorizontalTangent = toTangent(angleInDegrees)
    }

    /**
     * @inheritDoc
     */
    override fun getMinimumHorizontalAngle(): Float {
        return mMinimumHorizontalAngle
    }

    /**
     * @inheritDoc
     */
    override fun setMinimumVerticalAngle(angleInDegrees: Float) {
        mMinimumVerticalAngle = angleInDegrees
        mMinimumVerticalTangent = toTangent(angleInDegrees)
    }

    /**
     * @inheritDoc
     */
    override fun getMinimumVerticalAngle(): Float {
        return mMinimumVerticalAngle
    }

    /**
     * @inheritDoc
     */
    override fun setMaximumAngle(angleInDegrees: Float) {
        mMaximumAngle = angleInDegrees
        mMaximumTangent = toTangent(angleInDegrees)
    }

    /**
     * @inheritDoc
     */
    override fun getMaximumAngle(): Float {
        return mMaximumAngle
    }

    override fun getPath(startX: Float, startY: Float, endX: Float, endY: Float): Path {
        // Here's a little ascii art to show how this is calculated:
        // c---------- b
        //  \        / |
        //    \     d  |
        //      \  /   e
        //        a----f
        // This diagram assumes that the horizontal distance is less than the vertical
        // distance between The start point (a) and end point (b).
        // d is the midpoint between a and b. c is the center point of the circle with
        // This path is formed by assuming that start and end points are in
        // an arc on a circle. The end point is centered in the circle vertically
        // and start is a point on the circle.

        // Triangles bfa and bde form similar right triangles. The control points
        // for the cubic Bezier arc path are the midpoints between a and e and e and b.

        val path = Path()
        path.moveTo(startX, startY)

        var ex: Float
        var ey: Float
        if (startY == endY) {
            ex = (startX + endX) / 2
            ey = startY + mMinimumHorizontalTangent * abs(endX - startX) / 2
        } else if (startX == endX) {
            ex = startX + mMinimumVerticalTangent * abs(endY - startY) / 2
            ey = (startY + endY) / 2
        } else {
            val deltaX = endX - startX

            /*
              This is the only change to ArcMotion
             */
            val deltaY: Float = if (endY < startY) {
                startY - endY // Y is inverted compared to diagram above.
            } else {
                endY - startY
            }
            /*
              End changes
             */

            // hypotenuse squared.
            val h2 = deltaX * deltaX + deltaY * deltaY

            // Midpoint between start and end
            val dx = (startX + endX) / 2
            val dy = (startY + endY) / 2

            // Distance squared between end point and mid point is (1/2 hypotenuse)^2
            val midDist2 = h2 * 0.25f

            val minimumArcDist2: Float

            if (abs(deltaX) < abs(deltaY)) {
                // Similar triangles bfa and bde mean that (ab/fb = eb/bd)
                // Therefore, eb = ab * bd / fb
                // ab = hypotenuse
                // bd = hypotenuse/2
                // fb = deltaY
                val eDistY = h2 / (2 * deltaY)
                ey = endY + eDistY
                ex = endX

                minimumArcDist2 = (midDist2 * mMinimumVerticalTangent
                        * mMinimumVerticalTangent)
            } else {
                // Same as above, but flip X & Y
                val eDistX = h2 / (2 * deltaX)
                ex = endX + eDistX
                ey = endY

                minimumArcDist2 = (midDist2 * mMinimumHorizontalTangent
                        * mMinimumHorizontalTangent)
            }
            val arcDistX = dx - ex
            val arcDistY = dy - ey
            val arcDist2 = arcDistX * arcDistX + arcDistY * arcDistY

            val maximumArcDist2 = midDist2 * mMaximumTangent * mMaximumTangent

            var newArcDistance2 = 0f
            if (arcDist2 < minimumArcDist2) {
                newArcDistance2 = minimumArcDist2
            } else if (arcDist2 > maximumArcDist2) {
                newArcDistance2 = maximumArcDist2
            }
            if (newArcDistance2 != 0f) {
                val ratio2 = newArcDistance2 / arcDist2
                val ratio = sqrt(ratio2.toDouble()).toFloat()
                ex = dx + ratio * (ex - dx)
                ey = dy + ratio * (ey - dy)
            }
        }
        val controlX1 = (startX + ex) / 2
        val controlY1 = (startY + ey) / 2
        val controlX2 = (ex + endX) / 2
        val controlY2 = (ey + endY) / 2
        path.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY)
        return path
    }

    companion object {

        private const val DEFAULT_MIN_ANGLE_DEGREES = 0f
        private const val DEFAULT_MAX_ANGLE_DEGREES = 70f
        private val DEFAULT_MAX_TANGENT = tan(Math.toRadians((DEFAULT_MAX_ANGLE_DEGREES / 2).toDouble())).toFloat()

        private fun toTangent(arcInDegrees: Float): Float {
            require(!(arcInDegrees < 0 || arcInDegrees > 90)) { "Arc must be between 0 and 90 degrees" }
            return tan(Math.toRadians((arcInDegrees / 2).toDouble())).toFloat()
        }
    }
}