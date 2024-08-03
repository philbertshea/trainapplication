package com.example.trainapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SelectStartActivity : AppCompatActivity() {

    private fun getStationsByDist(lat: Double, long: Double) : List<Triple<String, Station, Double>>{
        val utilObj = Utils()
        val jsonString = utilObj.getJsonDataFromAsset(applicationContext, "stations.json")

        val listLineType = object : TypeToken<List<Line>>() {}.type
        val linesObj : List<Line> = Gson().fromJson(jsonString, listLineType)
        val lineIterator = linesObj.listIterator()
        val arr = mutableListOf<Triple<String, Station, Double>>()
        while (lineIterator.hasNext()) {
            val line = lineIterator.next()
            val lineName = line.name
            val stationIterator = line.stations.listIterator()
            while (stationIterator.hasNext()) {
                val station = stationIterator.next()
                val distance = utilObj.getDistance(lat, long, station.latitude, station.longitude)
                arr.add(Triple(lineName, station, distance))
                Log.i("TrainActivity", "${station.name} - ${distance}")
            }
        }
        arr.sortBy{it.third}

        Log.i("TrainActivity", "Completed getNearestStation")
        return arr
    }

    private lateinit var locClient: FusedLocationProviderClient
    private lateinit var btnResultOne : Button
    private lateinit var btnResultTwo : Button
    private lateinit var btnResultThree : Button
    private lateinit var btnResultFour : Button
    private lateinit var btnResultFive : Button
    private lateinit var btnSettings : Button
    private var currLoc: Location? = null

    private fun onLocSuccess(result : Location) {
        Log.i("TrainActivity", "${result.latitude}, ${result.longitude}")
        val arr = getStationsByDist(result.latitude, result.longitude)
        btnResultOne.setText(getString(R.string.station_name, arr[0].first, arr[0].second.code, arr[0].second.name, String.format("%.2f", arr[0].third)))
        btnResultTwo.setText(getString(R.string.station_name, arr[1].first, arr[1].second.code, arr[1].second.name, String.format("%.2f", arr[1].third)))
        btnResultThree.setText(getString(R.string.station_name, arr[2].first, arr[2].second.code, arr[2].second.name, String.format("%.2f", arr[2].third)))
        btnResultFour.setText(getString(R.string.station_name, arr[3].first, arr[3].second.code, arr[3].second.name, String.format("%.2f", arr[3].third)))
        btnResultFive.setText(getString(R.string.station_name, arr[4].first, arr[4].second.code, arr[4].second.name, String.format("%.2f", arr[4].third)))

        btnResultOne.setOnClickListener {
            val intent = Intent(this, SelectEndActivity::class.java)
            intent.putExtra("LINE_NAME", arr[0].first)
            intent.putExtra("STN_NAME", arr[0].second.name)
            intent.putExtra("STN_CODE", arr[0].second.code)
            startActivity(intent)
        }
        btnResultTwo.setOnClickListener {
            val intent = Intent(this, SelectEndActivity::class.java)
            intent.putExtra("LINE_NAME", arr[1].first)
            intent.putExtra("STN_NAME", arr[1].second.name)
            intent.putExtra("STN_CODE", arr[1].second.code)
            startActivity(intent)
        }
        btnResultThree.setOnClickListener {
            val intent = Intent(this, SelectEndActivity::class.java)
            intent.putExtra("LINE_NAME", arr[2].first)
            intent.putExtra("STN_NAME", arr[2].second.name)
            intent.putExtra("STN_CODE", arr[2].second.code)
            startActivity(intent)
        }
        btnResultFour.setOnClickListener {
            val intent = Intent(this, SelectEndActivity::class.java)
            intent.putExtra("LINE_NAME", arr[3].first)
            intent.putExtra("STN_NAME", arr[3].second.name)
            intent.putExtra("STN_CODE", arr[3].second.code)
            startActivity(intent)
        }
        btnResultFive.setOnClickListener {
            val intent = Intent(this, SelectEndActivity::class.java)
            intent.putExtra("LINE_NAME", arr[4].first)
            intent.putExtra("STN_NAME", arr[4].second.name)
            intent.putExtra("STN_CODE", arr[4].second.code)
            startActivity(intent)
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateStations() {
        val locTask = locClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            CancellationTokenSource().token
        )
        locTask.addOnSuccessListener { result ->
            onLocSuccess(result)
        }
        locTask.addOnFailureListener { result ->
            val lastLocTask = locClient.lastLocation
            lastLocTask.addOnSuccessListener { result ->
                onLocSuccess(result)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selectstart)
        locClient = LocationServices.getFusedLocationProviderClient(this)
//        locRequest = LocationRequest.Builder(60)
//            .setDurationMillis(60000)
//            .setMinUpdateIntervalMillis(30)
//            .setPriority(PRIORITY_HIGH_ACCURACY)

        btnResultOne = findViewById(R.id.btnResult1)
        btnResultTwo = findViewById(R.id.btnResult2)
        btnResultThree = findViewById(R.id.btnResult3)
        btnResultFour = findViewById(R.id.btnResult4)
        btnResultFive = findViewById(R.id.btnResult5)

        updateStations()

        val btnRefreshLocation : Button = findViewById(R.id.btnRefreshLocation)
        btnRefreshLocation.setOnClickListener {
            updateStations()
        }

        btnSettings = findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

//        locCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                for (location in locationResult.locations){
//                    tvResultOne.text = location.latitude.toBigDecimal().toPlainString()
//                    tvResultTwo.text = location.latitude.toBigDecimal().toPlainString()
//                    Log.i("TrainActivity", "Location found")
//                }
//            }
//        }

    }
}