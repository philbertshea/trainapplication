package com.example.trainapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
    private lateinit var btnLocationPermission: Button
    private lateinit var btnNotificationPermission : Button
    private lateinit var tvLocationPermission : TextView
    private lateinit var btnCheckPermissions : Button

    private fun showRationaleDialogOrOpenSettings(title: String, message: String, goSettings: Boolean, permission1: String, permission2: String?) {

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }
            .setPositiveButton("Grant") { dialog, _ ->
                dialog.dismiss()
                if (goSettings) {
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    startActivity(intent)
                } else if (permission2 == null) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission1),
                        0
                    )
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(permission1, permission2),
                        0
                    )
                }
            }
        builder.create().show()

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        tvLocationPermission = findViewById(R.id.tvLocationPermission)
        btnCheckPermissions = findViewById(R.id.btnCheckPermissions)
        btnCheckPermissions.setOnClickListener {
            val coarseLocGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val fineLocGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val notifGranted = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            val coarseLocShow = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
            val fineLocShow = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
            val notifShow = shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
            tvLocationPermission.text = "$coarseLocGranted $fineLocGranted $notifGranted $coarseLocShow $fineLocShow $notifShow"
            if (coarseLocGranted && fineLocGranted && notifGranted) {
                val intent = Intent(this, SelectStartActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        btnLocationPermission = findViewById<Button>(R.id.btnLocationPermission)
        btnLocationPermission.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Both location permissions granted
                Log.i("MyActivity", "Location Access Granted")
                Toast.makeText(this, "Location Already Granted", Toast.LENGTH_LONG)
            } else if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Fine location permission denied
                Log.i("MyActivity", "Fine Location Access Denied")
                showRationaleDialogOrOpenSettings(
                    "Precise Location Access Denied",
                    "TrainApp needs your Precise Location to send timely notifications to get off. Click App Permissions > Location > Allow Precise Location",
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION),
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    null
                )

            } else {
                // Both location permission denied
                Log.i("MainActivity", "Both Location Denied")
                showRationaleDialogOrOpenSettings(
                    "Precise Location Access Denied",
                    "TrainApp needs your Precise Location to send timely notifications to get off. Click App Permissions > Location > Allow Precise Location",
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION),
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                )

            }
            btnCheckPermissions.performClick()
        }

        btnNotificationPermission = findViewById<Button>(R.id.btnNotificationPermission)
        btnNotificationPermission.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                // Notification Access Granted
                Log.i("MainActivity", "Notification Access Granted")
                Toast.makeText(this, "Notification Already Granted", Toast.LENGTH_LONG)
            } else {
                Log.i("MainActivity", "Notification Access Denied")
                showRationaleDialogOrOpenSettings(
                    "Notification Access Denied",
                    "TrainApp needs Notification Access to deliver alerts to you, like Get Off alerts. Click App Permissions > Notifications > Show Notifications",
                    !shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS),
                    Manifest.permission.POST_NOTIFICATIONS,
                    null
                )
            }
            btnCheckPermissions.performClick()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS),
            0
        )
        btnCheckPermissions.performClick()
    }
}