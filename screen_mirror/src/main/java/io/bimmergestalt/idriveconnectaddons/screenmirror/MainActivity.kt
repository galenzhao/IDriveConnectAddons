package io.bimmergestalt.idriveconnectaddons.screenmirror

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import android.app.AppOpsManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Process

import android.content.pm.PackageManager

import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import androidx.activity.viewModels
import android.app.AppOpsManager
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import android.provider.Settings.canDrawOverlays
import androidx.activity.viewModels
import io.bimmergestalt.idriveconnectaddons.screenmirror.databinding.ActivityMainBinding
import androidx.annotation.Nullable;



const val TAG = "ScreenMirroring"

class MainActivity : AppCompatActivity() {
    val controller by lazy { MainController(this) }
    val viewModel by viewModels<MainModel>{ MainModel.Factory(this.applicationContext) }

    private val REQUEST_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppSettings.loadSettings(applicationContext)
        AppSettings.loadSettings(this)

        setContentView(R.layout.activity_main)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.controller = controller
        binding.viewModel = viewModel
        setContentView(binding.root)

        val context =
            applicationContext // or activity.getApplicationContext()

        val packageManager = context.packageManager
        val packageName = context.packageName

        var myVersionName = "not available" // initialize String

        try {
            myVersionName = packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        findViewById<TextView>(R.id.tv_versionName).text = myVersionName

        // on below line adding click listener for button.
        findViewById<Button>(R.id.hideBtn).setOnClickListener {

            // on below line getting current view.
            val view: View? = this?.currentFocus

            // on below line checking if view is not null.
            if (view != null) {

                AppSettings.loadSettings(applicationContext)
                AppSettings.loadSettings(this)

                val settingsViewer = AppSettingsViewer()

                val origPaddingLeft = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_LEFT]
                val origPaddingTop = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_TOP]

                // on below line we are creating a variable
                // for input manager and initializing it.
                val inputMethodManager =
                    this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

                // on below line hiding our keyboard.
                inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

            }
            AppSettings.loadSettings(applicationContext)

            val settingsViewer = AppSettingsViewer()

            val origPaddingLeft = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_LEFT]
            val origPaddingTop = settingsViewer[AppSettings.KEYS.DIMENSIONS_PADDING_TOP]
            // displaying toast message on below line.
            Toast.makeText(this, "Key board hidden: $origPaddingLeft, $origPaddingTop", Toast.LENGTH_SHORT).show()

            if (isGranted()){

            }else {
                requestPermission()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updatePermissions(this)
        println("PROJECT_MEDIA permission: ${checkSelfPermission("PROJECT_MEDIA")}")
        val appOps = getSystemService(AppOpsManager::class.java)
        println("PROJECT_MEDIA appops: ${appOps.checkOpNoThrow("android:project_media", Process.myUid(), packageName)}")

        if(canDrawOverlays(this)){
            if(getString(R.string.lbl_status_not_allowed).equals(findViewById<TextView>(R.id.mirroringStateText).text.toString(),true)) {
                println("PROJECT_MEDIA permission: ${checkSelfPermission("PROJECT_MEDIA")}")
                AppSettings.loadSettings(applicationContext)

                val settingsViewer = AppSettingsViewer()
                if (settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].isNotEmpty() && settingsViewer[AppSettings.KEYS.AUTO_PERMISSION].toInt() > 0) {
                    val appOps = getSystemService(AppOpsManager::class.java)
                    println("PROJECT_MEDIA appops: ${appOps.checkOpNoThrow("android:project_media", Process.myUid(), packageName)}")
                    controller.promptPermission(false)
                }
            }
        }else{

        }
    }

    private fun requestPermission() {
        val uri = Uri.parse("package:$packageName")
        val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION, uri)
        startActivityForResult(intent, REQUEST_PERMISSION_CODE)
    }

    private fun isGranted(): Boolean {
        return canDrawOverlays(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (canDrawOverlays(this)) {
                // 許可されたときの処理
            } else {
                // 拒否されたときの処理
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}