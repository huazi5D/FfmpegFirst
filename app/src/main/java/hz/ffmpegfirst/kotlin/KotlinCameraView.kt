package hz.ffmpegfirst.kotlin

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class KotlinCameraView(context: Context?, attrs: AttributeSet?) : GLSurfaceView(context, attrs) {

    init {
        this.setEGLContextClientVersion(2)
        setRenderer(Render())
    }

    inner class Render : Renderer {

        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }

    }
}