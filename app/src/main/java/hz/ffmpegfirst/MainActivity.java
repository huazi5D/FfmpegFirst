package hz.ffmpegfirst;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ff-decode");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "permission yes!!!", Toast.LENGTH_LONG).show();
        } else {
            EasyPermissions.requestPermissions(this, "申请权限", 0, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "permission yes!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Toast.makeText(this, "permission no!!!", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }


    public void onButtonClick(View view) {
        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        int a = Decode.decodeMp4ToYUV("sdcard/111/test.mp4");
        tv.setText("" + a);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native int getAvFormatInfo(String filePath);
}
