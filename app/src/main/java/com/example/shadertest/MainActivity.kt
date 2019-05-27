package com.example.shadertest

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.shadertest.vo.Obj
import kotlinx.android.synthetic.main.activity_main.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {
    private lateinit var obj: Obj


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glView.setEGLContextClientVersion(2)
        glView.setRenderer(object: GLSurfaceView.Renderer {
            override fun onDrawFrame(gl: GL10?) {
                obj.draw()

            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                GLES20.glViewport(0, 0, width, height)
            }

            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                glView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                obj = Obj(context = applicationContext)
            }

        })

    }
}
