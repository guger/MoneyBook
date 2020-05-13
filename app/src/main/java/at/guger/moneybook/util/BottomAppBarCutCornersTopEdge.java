/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.guger.moneybook.util;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomappbar.BottomAppBarTopEdgeTreatment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.ShapePath;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link BottomAppBar} top edge that works with a Diamond shaped {@link FloatingActionButton}
 * <p>
 * Workaround for {@link BottomAppBar} to respect rectangular shaped cradles.
 * From: https://github.com/material-components/material-components-android/blob/master/catalog/java/io/material/catalog/bottomappbar/BottomAppBarCutCornersTopEdge.java
 */
public class BottomAppBarCutCornersTopEdge extends BottomAppBarTopEdgeTreatment {

    private final float fabMargin;
    private final float cradleVerticalOffset;

    public BottomAppBarCutCornersTopEdge(float fabMargin, float roundedCornerRadius, float cradleVerticalOffset) {
        super(fabMargin, roundedCornerRadius, cradleVerticalOffset);
        this.fabMargin = fabMargin;
        this.cradleVerticalOffset = cradleVerticalOffset;
    }

    @Override
    @SuppressWarnings("RestrictedApi")
    public void getEdgePath(float length, float center, float interpolation, @NotNull ShapePath shapePath) {
        float fabDiameter = getFabDiameter();
        if (fabDiameter == 0) {
            shapePath.lineTo(length, 0);
            return;
        }

        float diamondSize = fabDiameter / 2f;
        float middle = center + getHorizontalOffset();

        float verticalOffsetRatio = cradleVerticalOffset / diamondSize;
        if (verticalOffsetRatio >= 1.0f) {
            shapePath.lineTo(length, 0);
            return;
        }

        shapePath.lineTo(middle - (fabMargin + diamondSize - cradleVerticalOffset), 0);

        shapePath.lineTo(middle, (diamondSize - cradleVerticalOffset + fabMargin) * interpolation);

        shapePath.lineTo(middle + (fabMargin + diamondSize - cradleVerticalOffset), 0);

        shapePath.lineTo(length, 0);
    }
}