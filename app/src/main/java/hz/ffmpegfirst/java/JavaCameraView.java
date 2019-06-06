package hz.ffmpegfirst.java;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import hz.ffmpegfirst.utils.ShaderUtil;

public class JavaCameraView extends GLSurfaceView {

    Context context;
    JavaCamera camera = new JavaCamera();

    public JavaCameraView(Context context) {
        this(context, null);
    }

    public JavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setEGLContextClientVersion(2);
        setRenderer(new Render());
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    class Render implements Renderer {

        private int oesProgram;
        private int program;

        private int oesPosHandle;
        private int oesTexHandle;
        private float[] oesPosCoordinate = {-1, -1, -1, 1, 1, -1, 1, 1};
        private float[] oesTexCoordinate = {0, 1, 1, 1, 0, 0, 1, 0};

        private FloatBuffer oesPosBuffer;
        private FloatBuffer oesTexBuffer;

        private SurfaceTexture surfaceTexture;

        private String oes_fragment = "#extension GL_OES_EGL_image_external : require\n" +
                "precision mediump float;\n" +
                "uniform samplerExternalOES videoTex;\n" +
                "varying vec2 textureCoordinate;\n" +
                "\n" +
                "void main() {\n" +
                "   gl_FragColor = texture2D(videoTex, textureCoordinate);\n" +
                "}";

        private String oes_vertex = "uniform mat4 textureTransform;\n" +
                "attribute vec2 inputTextureCoordinate;\n" +
                "attribute vec4 position;\n" +
                "varying   vec2 textureCoordinate;\n" +
                "\n" +
                " void main() {\n" +
                "     gl_Position = position;\n" +
                "     textureCoordinate = inputTextureCoordinate;\n" +
                " }";

        private String vertex = "uniform mat4 u_Matrix;\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec2 a_Coordinate;\n" +
                "varying   vec2 v_Coordinate;\n\n" +
                "void main() {\n" +
                "   gl_Position = u_Matrix * a_Position;\n" +
                "   v_Coordinate = a_Coordinate;\n" +
                "}";

        private String fragment = "precision mediump float;\n" +
                "uniform sampler2D a_Texture;\n" +
                "varying vec2 v_Coordinate;\n\n" +
                "void main() {\n" +
                "   gl_FragColor = texture2D(a_Texture, v_Coordinate);\n" +
                "}";

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

            oesProgram = ShaderUtil.createProgram(oes_vertex, oes_fragment);
            program = ShaderUtil.createProgram(vertex, fragment);
            GLES20.glUseProgram(oesProgram);

            createAndBindVideoTexture();

            oesPosHandle           = GLES20.glGetAttribLocation (oesProgram, "position");
            oesTexHandle           = GLES20.glGetAttribLocation (oesProgram, "inputTextureCoordinate");

            oesPosBuffer = convertToFloatBuffer(oesPosCoordinate);
            oesTexBuffer = convertToFloatBuffer(oesTexCoordinate);

            GLES20.glVertexAttribPointer(oesPosHandle, 2, GLES20.GL_FLOAT, false, 0, oesPosBuffer);
            GLES20.glVertexAttribPointer(oesTexHandle, 2, GLES20.GL_FLOAT, false, 0, oesTexBuffer);

            GLES20.glEnableVertexAttribArray(oesPosHandle);
            GLES20.glEnableVertexAttribArray(oesTexHandle);

            fboTextureId = createTextureWithFBO(camera.getCameraSize().getWidth(), camera.getCameraSize().getHeight());
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
            surfaceTexture.updateTexImage();

//            bindFBO();
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
//            GLES20.glUniform1i(mShaderHandles.mTextureHandle, 0);
//            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mVertexCoordinate.length / 2);
//            unbindFBO();
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, oesPosCoordinate.length / 2);
        }

        private void createAndBindVideoTexture() {
            int[] texture = new int[1];
            GLES20.glGenTextures(1, texture, 0);
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,GL10.GL_LINEAR);
            GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

            surfaceTexture = new SurfaceTexture(texture[0]);
            surfaceTexture.setOnFrameAvailableListener(onFrameAvailableListener);
            camera.initCamera(surfaceTexture);
        }

        private int fboTextureId;
        private int mFrameBufferId;
        private int mRenderBufferId;

        public int createTextureWithFBO(int width, int height) {
            int[] texture=new int[1];
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glGenTextures(1,texture,0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,texture[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            int[] framebuffer = new int[1];
            GLES20.glGenFramebuffers(1, framebuffer, 0);
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, framebuffer[0]);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, texture[0], 0);
            mFrameBufferId = framebuffer[0];

            int[] renderBuffer = new int[1];
            GLES20.glGenRenderbuffers(1, renderBuffer, 0);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, renderBuffer[0]);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, width, height);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER,renderBuffer[0]);
            mRenderBufferId = renderBuffer[0];

            int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException(this+": Failed to set up render buffer with status "+status+" and error "+GLES20.glGetError());
            }
            unbindFBO();

            return texture[0];
        }


        public void bindFBO() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTextureId, 0);
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, mRenderBufferId);
        }

        public void unbindFBO() {
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }

    SurfaceTexture.OnFrameAvailableListener onFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
        @Override
        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
            requestRender();
        }
    };

    private FloatBuffer convertToFloatBuffer(float[] buffer) {
        FloatBuffer fb = ByteBuffer.allocateDirect(buffer.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        fb.put(buffer);
        fb.position(0);
        return fb;
    }
}
