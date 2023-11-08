package com.example.itemclassifier.domain

import android.graphics.Bitmap

interface ObjectClassifier {
    fun classify(bitmap: Bitmap, rotation: Int): List<Classification>
}