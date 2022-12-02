package com.example.kamaz_app

import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.kamaz_app.databinding.ActivityMainBinding
import com.google.mediapipe.framework.TextureFrame
import com.google.mediapipe.solutioncore.CameraInput
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView
import com.google.mediapipe.solutions.hands.HandLandmark
import com.google.mediapipe.solutions.hands.Hands
import com.google.mediapipe.solutions.hands.HandsOptions
import com.google.mediapipe.solutions.hands.HandsResult
import org.opencv.ml.SVM


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val ptr = SVM.load("model.pkl")
        //ptr.predict()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

         binding.camBtn.setOnClickListener {
             if (ContextCompat.
                 checkSelfPermission(this, Manifest.permission.CAMERA)
                 == PackageManager.PERMISSION_GRANTED){
                a()
             }

         }
    }

    fun a(){
        // For camera input and result rendering with OpenGL.
        // For camera input and result rendering with OpenGL.
        val handsOptions = HandsOptions.builder()
            .setStaticImageMode(false)
            .setMaxNumHands(1)
            .setRunOnGpu(false).build()
        val hands = Hands(this, handsOptions)
        hands.setErrorListener { message: String, e: RuntimeException? ->
            Log.e(
                TAG,
                "MediaPipe Hands error:$message"
            )
        }

        // Initializes a new CameraInput instance and connects it to MediaPipe Hands Solution.
        val cameraInput = CameraInput(this)
        cameraInput.setNewFrameListener { textureFrame: TextureFrame? -> hands.send(textureFrame) }

        // Initializes a new GlSurfaceView with a ResultGlRenderer<HandsResult> instance
        // that provides the interfaces to run user-defined OpenGL rendering code.
        // See mediapipe/examples/android/solutions/hands/src/main/java/com/google/mediapipe/examples/hands/HandsResultGlRenderer.java
        // as an example.
        val glSurfaceView = SolutionGlSurfaceView<HandsResult>(
            this, hands.glContext, hands.glMajorVersion
        )
        //glSurfaceView.setSolutionResultRenderer(HandsResultGlRenderer())
        glSurfaceView.setRenderInputImage(true)

        hands.setResultListener { handsResult: HandsResult ->
            if (handsResult.multiHandLandmarks().isEmpty()) {
                return@setResultListener
            }
            val wristLandmark =
                handsResult.multiHandLandmarks()[0].landmarkList[HandLandmark.WRIST]
            Log.i(
                TAG, String.format(
                    "MediaPipe Hand wrist normalized coordinates (value range: [0, 1]): x=%f, y=%f",
                    wristLandmark.x, wristLandmark.y
                )
            )
            // Request GL rendering.
            glSurfaceView.setRenderData(handsResult)
            glSurfaceView.requestRender()
        }
        val toast = Toast.makeText(this, "ахуенна братан", Toast.LENGTH_SHORT)
        toast.show()
// The runnable to start camera after the GLSurfaceView is attached.
        glSurfaceView.post {
            cameraInput.start(
                this,
                hands.glContext,
                CameraInput.CameraFacing.FRONT,
                glSurfaceView.width,
                glSurfaceView.height
            )
        }
        binding.frame.
    }
}