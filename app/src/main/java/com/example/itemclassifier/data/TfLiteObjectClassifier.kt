package com.example.itemclassifier.data

import android.content.Context
import android.graphics.Bitmap
import android.view.Surface
import com.example.itemclassifier.domain.Classification
import com.example.itemclassifier.domain.ObjectClassifier
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class TfLiteObjectClassifier (
    private val context: Context,
    private val threshold: Float = 0.5f,
    private val maxResults: Int = 2
): ObjectClassifier {

    private var classifier: ImageClassifier? = null

    /**
     * Builds the Image Processor.
     */
    private fun setupClassifer () {
        val baseOptions = BaseOptions.builder()
            .setNumThreads(2)
            .build()
        val options = ImageClassifier.ImageClassifierOptions.builder()
            .setBaseOptions(baseOptions)
            .setMaxResults(maxResults)
            .setScoreThreshold(threshold)
            .build()

        try {
            classifier = ImageClassifier.createFromFileAndOptions (
                context,
                "objects.tflite",
                options
            )
        } catch (ex: IllegalStateException) {
            ex.printStackTrace()
        }
    }

    /**
     * Classifies the image from the frame.
     * Returns the object's name and confidence score.
     */
    override fun classify(bitmap: Bitmap, rotation: Int): List<Classification> {
        if (classifier == null) {
            setupClassifer()
        }

        val imageProcessor = ImageProcessor.Builder().build()
        val tensorImg = imageProcessor.process(TensorImage.fromBitmap(bitmap))

        val imageProcessingOptions = ImageProcessingOptions.builder()
            .setOrientation(getOrientationFromRotation(rotation))
            .build()

        val results = classifier?.classify(tensorImg, imageProcessingOptions)

        return results?.flatMap { classifications ->
            classifications.categories.map {category ->
                Classification(
                    objName = category.label,
                    objScore = category.score
                )
            }
        }?.distinctBy { it.objName } ?: emptyList()
    }

    /**
     * Returns the Image Processing Orientation from rotation
     */
    private fun getOrientationFromRotation (rotation: Int): ImageProcessingOptions.Orientation {
        return when (rotation) {
            Surface.ROTATION_270 -> ImageProcessingOptions.Orientation.BOTTOM_RIGHT
            Surface.ROTATION_90 -> ImageProcessingOptions.Orientation.TOP_LEFT
            Surface.ROTATION_180 -> ImageProcessingOptions.Orientation.RIGHT_BOTTOM
            else ->  ImageProcessingOptions.Orientation.RIGHT_TOP
        }
    }
}