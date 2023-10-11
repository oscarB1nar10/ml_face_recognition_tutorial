package com.b1nar10.ml_face_recognition_tutorial.ui.utils

import com.b1nar10.ml_face_recognition_tutorial.ui.MainActivity

fun MainActivity.getTargetedWidthHeight(): Pair<Int, Int> {
    val maxWidthForPortraitMode: Int =  viewBinding.viewFinder.width
    val maxHeightForPortraitMode: Int = viewBinding.viewFinder.height

    return Pair(maxWidthForPortraitMode, maxHeightForPortraitMode)
}