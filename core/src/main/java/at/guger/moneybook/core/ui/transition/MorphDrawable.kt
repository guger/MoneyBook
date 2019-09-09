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

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.FloatProperty
import android.util.IntProperty
import android.util.Property
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import at.guger.moneybook.core.util.Utils

/**
 * A drawable that can morph size, shape (via it's corner radius) and color.
 * Specifically this is useful for animating between a FAB and a dialog.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class MorphDrawable(@ColorInt color: Int, cornerRadius: Float) : Drawable() {

    //region Variables

    private val paint = Paint()

    var cornerRadius: Float = cornerRadius
        set(value) {
            field = value
            invalidateSelf()
        }

    var color: Int
        get() = paint.color
        set(value) {
            paint.color = value
        }

    //endregion

    init {
        this.color = color
    }

    //region Drawable

    override fun draw(canvas: Canvas) {
        canvas.drawRoundRect(
            bounds.left.toFloat(),
            bounds.top.toFloat(),
            bounds.right.toFloat(),
            bounds.bottom.toFloat(),
            cornerRadius,
            cornerRadius,
            paint
        )
    }

    override fun getOutline(outline: Outline) {
        outline.setRoundRect(bounds, cornerRadius)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int {
        return if (paint.alpha == 255) PixelFormat.OPAQUE else PixelFormat.TRANSLUCENT
    }

    //endregion

    abstract class FloatProp<T>(val name: String) {
        abstract fun set(variable: T, value: Float)
        abstract fun get(variable: T): Float
    }

    abstract class IntProp<T>(val name: String) {
        abstract fun set(variable: T, value: Int)
        abstract fun get(variable: T): Int
    }

    companion object {

        val COLOR = makeIntProperty(object : IntProp<MorphDrawable>("color") {

            override fun get(variable: MorphDrawable): Int {
                return variable.color
            }

            override fun set(variable: MorphDrawable, value: Int) {
                variable.color = value
            }
        })

        val CORNERRADIUS = makeFloatProperty(object : FloatProp<MorphDrawable>("corner_radius") {

            override fun get(variable: MorphDrawable): Float {
                return variable.cornerRadius
            }

            override fun set(variable: MorphDrawable, value: Float) {
                variable.cornerRadius = value
            }
        })

        private fun <T> makeFloatProperty(impl: FloatProp<T>) = if (Utils.isNougat()) {
            object : FloatProperty<T>(impl.name) {

                override fun setValue(`object`: T, value: Float) {
                    impl.set(`object`, value)
                }

                override fun get(`object`: T): Float {
                    return impl.get(`object`)
                }
            }
        } else {
            object : Property<T, Float>(Float::class.java, impl.name) {

                override fun set(`object`: T, value: Float?) {
                    impl.set(`object`, value!!)
                }

                override fun get(`object`: T): Float {
                    return impl.get(`object`)
                }
            }
        }

        fun <T> makeIntProperty(impl: IntProp<T>) = if (Utils.isNougat()) {
            object : IntProperty<T>(impl.name) {

                override fun setValue(`object`: T, value: Int) {
                    impl.set(`object`, value)
                }

                override fun get(`object`: T): Int {
                    return impl.get(`object`)
                }
            }
        } else {
            object : Property<T, Int>(Int::class.java, impl.name) {

                override fun set(`object`: T, value: Int?) {
                    impl.set(`object`, value!!)
                }

                override fun get(`object`: T): Int {
                    return impl.get(`object`)
                }
            }
        }
    }
}