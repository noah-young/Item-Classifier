package com.example.itemclassifier

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.itemclassifier.domain.Classification
import com.example.itemclassifier.domain.ObjectClassifier

class ObjectAnalyzer (
    private val classifier: ObjectClassifier,
    private val onResults: (List<Classification>) -> Unit
): ImageAnalysis.Analyzer {
    override fun analyze (image: ImageProxy) {

    }
}