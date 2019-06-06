package hz.ffmpegfirst

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import android.widget.Toast

import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Toast.makeText(this, "permission yes!!!", Toast.LENGTH_LONG).show()
        } else {
            EasyPermissions.requestPermissions(this, "申请权限", 0, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "permission yes!!!", Toast.LENGTH_LONG).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(this, "permission no!!!", Toast.LENGTH_LONG).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    fun onButtonClick(view: View) {
        // Example of a call to a native method
        val tv = findViewById<TextView>(R.id.sample_text)
        val a = Decode.decodeMp4ToYUV("sdcard/111/test.mp4")
        tv.text = "" + a
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    external fun getAvFormatInfo(filePath: String): Int

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("ff-decode")
        }
    }
}
