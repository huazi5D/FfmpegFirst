package hz.ffmpegfirst.java;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Size;

import java.io.IOException;

public class JavaCamera {

    private Camera camera;

    public void initCamera(final SurfaceTexture surfaceTexture) {
        try {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
            camera.setPreviewTexture(surfaceTexture);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Size getCameraSize() {
        Camera.Size size = camera.getParameters().getPreviewSize();
        return new Size(size.width, size.height);
    }

}
