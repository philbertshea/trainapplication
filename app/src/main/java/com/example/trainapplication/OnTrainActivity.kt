package com.example.trainapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.math.abs

class OnTrainActivity : AppCompatActivity() {
    var currentCode: Int = 0
    private lateinit var tvStatus : TextView
    private lateinit var tvStatus2 : TextView
    private lateinit var stationList: List<Station>
    private lateinit var notifBuilder : NotificationCompat.Builder
    private lateinit var pendingIntent : PendingIntent
    var curDist = 0.0
    var nextDist = 0.0
    var curDistAfter = 0.0
    var nextDistAfter = 0.0
    var nextCode : Int = 0
    var NOTIF_ID : Int = 0
    var i : Int = 0

    @SuppressLint("MissingPermission")
    private fun updateLocation(locClient: FusedLocationProviderClient, stationList: List<Station>, lineName: String, startCode: Int, endCode: Int) {
        i++
        val locTask = locClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        )
        locTask.addOnSuccessListener { result ->
            Log.i("TrainActivity", "Before: ${result.latitude}, ${result.longitude}")
            curDist = Utils().getDistance(
                result.latitude,
                result.longitude,
                stationList.get(currentCode - 1).latitude,
                stationList.get(currentCode - 1).longitude
            )
            if (startCode > endCode) nextCode = currentCode - 1
            else nextCode = currentCode + 1
            nextDist = Utils().getDistance(
                result.latitude,
                result.longitude,
                stationList.get(nextCode - 1).latitude,
                stationList.get(nextCode - 1).longitude
            )

            Thread.sleep(5000)
            val locTask2 = locClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                CancellationTokenSource().token
            )
            locTask2.addOnSuccessListener { result ->
                Log.i("TrainActivity", "After: ${result.latitude}, ${result.longitude}")
                curDistAfter = Utils().getDistance(
                    result.latitude,
                    result.longitude,
                    stationList.get(currentCode - 1).latitude,
                    stationList.get(currentCode - 1).longitude
                )
                if (startCode > endCode) nextCode = currentCode - 1
                else nextCode = currentCode + 1
                nextDistAfter = Utils().getDistance(
                    result.latitude,
                    result.longitude,
                    stationList.get(nextCode - 1).latitude,
                    stationList.get(nextCode - 1).longitude
                )


                tvStatus2.text = "${i} ${String.format("%.2f", curDist)}-${
                    String.format(
                        "%.2f",
                        curDistAfter
                    )
                } ${currentCode}>${nextCode} ${
                    String.format(
                        "%.2f",
                        nextDist
                    )
                }-${String.format("%.2f", nextDistAfter)}"
                val curStation = getString(
                    R.string.station_name,
                    lineName,
                    currentCode,
                    stationList.get(currentCode).name,
                    String.format("%.2f", curDistAfter)
                )
                val nextStation = getString(
                    R.string.station_name,
                    lineName,
                    nextCode,
                    stationList.get(nextCode).name,
                    String.format("%.2f", nextDistAfter)
                )
                if (curDistAfter < 0.1) {
                    tvStatus.text = "At " + curStation
                } else if (nextDistAfter < 0.1) {
                    tvStatus.text = "At " + nextStation
                    currentCode = nextCode
                }
                if (nextDistAfter > nextDist && curDistAfter > curDist && nextDistAfter < curDistAfter) {
                    tvStatus.text = "Passed Station: Reset."
                    currentCode = nextCode
                } else if (nextDistAfter > nextDist && curDistAfter > curDist && nextDistAfter > curDistAfter) {
                    tvStatus.text = "Wrong Direction: Reset."
                } else if (nextDistAfter < nextDist && curDistAfter < curDist && curDistAfter < nextDistAfter) {
                    tvStatus.text = "Reaching " + curStation
                } else if (nextDistAfter < nextDist && curDistAfter > curDist && nextDistAfter > 0.5) {
                    tvStatus.text = curStation + ">" + nextStation
                } else if (nextDistAfter < nextDist && curDistAfter > curDist && nextDistAfter < 0.5) {
                    tvStatus.text = "Reaching " + nextStation
                    val stationsFromEnd = abs(nextCode - endCode)
                    if (stationsFromEnd in 2..3) {
                        callNotification(
                            "${stationsFromEnd} Stops Away",
                            "Reaching: ${nextStation}"
                        )
                    } else if (stationsFromEnd == 1) {
                        callNotification(
                            "Prepare to Alight",
                            "Reaching: ${nextStation}"
                        )
                    } else if (stationsFromEnd == 0) {
                        callNotification(
                            "Alight NOW",
                            "Reaching: ${nextStation}"
                        )
                    }
                } else {
                    tvStatus.text = "Error."
                }
            }
        }
    }
    private fun callNotification(title: String, text: String) {
        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@OnTrainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@with
            }
            // notificationId is a unique int for each notification that you must define.
            notifBuilder = NotificationCompat.Builder(applicationContext, "Train Updates")
                .setSmallIcon(R.drawable.eastwestcircle)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
            notify(NOTIF_ID, notifBuilder.build())
            NOTIF_ID++
        }
    }

    private fun updateStations(stationList: List<Station>, lineName: String, startName: String, startCode: Int, endName: String, endCode: Int, circleParent: LinearLayout, buttonParent: LinearLayout) {
        val utilObj = Utils()
        var circleParams = LinearLayout.LayoutParams(140, 140)
        circleParams.setMargins(5, 0, 0, 11)
        var buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        buttonParams.setMargins(5, 0, 0, 20)

        var i = 0
        if (startCode < endCode) i = startCode - 1
        else i = endCode - 1
        while ((startCode < endCode && i < endCode) || (startCode > endCode && i < startCode)) {
            val station = stationList.get(i)
            var newCircle : ImageView = ImageView(this)
            newCircle.setImageResource(utilObj.getCircleXML(lineName))
            circleParent.addView(newCircle)
            newCircle.setPadding(5, 0, 0, 5)
            newCircle.layoutParams = circleParams

            var newButton : Button = Button(this)
            newButton.text = getString(R.string.station_name_wo_dist, lineName, station.code, station.name)
            buttonParent.addView(newButton)
            newButton.textSize = 30F
            if (startName == station.name) {
                newButton.setBackgroundResource(R.drawable.from_button)
            } else if (endName == station.name) {
                newButton.setBackgroundResource(R.drawable.to_button)
            } else {
                newButton.setBackgroundResource(utilObj.getButtonXML(lineName))
            }
            newButton.setPadding(5, 0, 0, 5)
            newButton.layoutParams = buttonParams

            i++
        }
    }
    private fun createNotificationChannel(channelId: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        createNotificationChannel("Train Service")
        startService(Intent(this, OnTrainService::class.java))
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ontrain)
        val utilObj = Utils()

        val tvFromStation : TextView = findViewById(R.id.tvFromStation)
        val tvToStation : TextView = findViewById(R.id.tvToStation)
        val tvTowardsStart : TextView = findViewById(R.id.tvTowardsStart)
        val tvTowardsEnd : TextView = findViewById(R.id.tvTowardsEnd)
        val circleParent : LinearLayout = findViewById(R.id.circleparent)
        val buttonParent : LinearLayout = findViewById(R.id.buttonparent)
        val btnChangeStart : Button = findViewById(R.id.btnChangeStart)
        val btnChangeEnd : Button = findViewById(R.id.btnChangeEnd)
        val line_name = intent.getStringExtra("LINE_NAME")
        val start_name = intent.getStringExtra("START_NAME")
        val start_code = intent.getIntExtra("START_CODE", 0)
        val end_name = intent.getStringExtra("END_NAME")
        val end_code = intent.getIntExtra("END_CODE", 0)
        tvStatus = findViewById(R.id.tvStatus)
        tvStatus2 = findViewById(R.id.tvStatus2)

        // Build Notification
        // Create an explicit intent for an Activity in your app.
        val intent = Intent(this, OnTrainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        createNotificationChannel("Train Updates")
        callNotification("Hello:", "This is a test notification")

        line_name?.let {
            stationList = utilObj.getStationsList(line_name, applicationContext)
            val towardsStartStation = utilObj.getFromStationName(line_name)
            val towardsEndStation = utilObj.getToStationName(line_name)

            tvTowardsStart.text = getString(R.string.towards_station_name, towardsStartStation)
            tvTowardsEnd.text = getString(R.string.towards_station_name, towardsEndStation)
            tvFromStation.text = getString(R.string.from_station_name, line_name, start_code, start_name)
            tvToStation.text = getString(R.string.to_station_name, line_name, end_code, end_name)
            btnChangeEnd.setOnClickListener {
                val intent = Intent(this, SelectEndActivity::class.java)
                intent.putExtra("LINE_NAME", line_name)
                intent.putExtra("STN_NAME", start_name)
                intent.putExtra("STN_CODE", start_code)
                startActivity(intent)
                finish()
            }
            btnChangeStart.setOnClickListener {
                val intent = Intent(this, SelectStartActivity::class.java)
                startActivity(intent)
                finish()
            }
            start_name?.let {
                end_name?.let {
                    updateStations(stationList, line_name, start_name, start_code, end_name, end_code, circleParent, buttonParent)
                }
            }

            val locClient = LocationServices.getFusedLocationProviderClient(this)
            currentCode = start_code

            val mainHandler = Handler(Looper.getMainLooper())

            mainHandler.post(object : Runnable {
                override fun run() {
                    updateLocation(locClient, stationList, line_name, start_code, end_code)
                    mainHandler.postDelayed(this, 20000)
                }
            })

        }

    }
}