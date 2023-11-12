package com.example.itemclassifier

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.itemclassifier.domain.Classification
import com.example.itemclassifier.domain.ObjectClassifier

class ObjectAnalyzer (
    private val classifier: ObjectClassifier,
    private val onResults: (List<Classification>) -> Unit
): ImageAnalysis.Analyzer {
    private var frameSkipCounter = 0

    /**
     * Analyzes a 1/n frames
     */
    override fun analyze (image: ImageProxy) {
        val n = 10
        if (frameSkipCounter % n == 0) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap()

            val results = classifier.classify(bitmap, rotationDegrees)
            onResults(results)
        }
        frameSkipCounter++
        image.close()
    }
}