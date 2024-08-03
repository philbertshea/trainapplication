package com.example.trainapplication

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.math.pow

class SensorActivity : AppCompatActivity(), SensorEventListener  {

    private lateinit var mSensorManager: SensorManager
    private lateinit var tvAccData : TextView
    private lateinit var tvStatus : TextView
    private lateinit var tvStatus2 : TextView

    private fun updateStations(lineName: String, startName: String, startCode: Int, endName: String, endCode: Int, circleParent: LinearLayout, buttonParent: LinearLayout) {
        val utilObj = Utils()
        val jsonString = utilObj.getJsonDataFromAsset(applicationContext, "stations.json")

        val listLineType = object : TypeToken<List<Line>>() {}.type
        val linesObj : List<Line> = Gson().fromJson(jsonString, listLineType)
        val lineIndex = utilObj.getLineIndex(lineName)

        var stationList = linesObj.get(lineIndex).stations

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

    override fun onCreate(savedInstanceState: Bundle?) {
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
        tvStatus = findViewById(R.id.tvStatus)
        tvStatus2 = findViewById(R.id.tvStatus2)
        val line_name = intent.getStringExtra("LINE_NAME")
        val start_name = intent.getStringExtra("START_NAME")
        val start_code = intent.getIntExtra("START_CODE", 0)
        val end_name = intent.getStringExtra("END_NAME")
        val end_code = intent.getIntExtra("END_CODE", 0)

        line_name?.let {
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
                    updateStations(line_name, start_name, start_code, end_name, end_code, circleParent, buttonParent)
                }
            }

        }

        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)?.also {
            mSensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL
                , SensorManager.SENSOR_DELAY_NORMAL)
        }

    }
    var moveCount = 0
    var stationaryCount = 0
    val MOVE_THRESHOLD = 30
    val STATIONARY_THRESHOLD = 5
    var prev = 0.00
    var incCount = 0
    var decCount = 0
    val INC_CONF = 500
    val DEC_CONF = 500

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            Log.i("TrainActivity", "HI! ${String.format("%.2f", event.values[0])}")
            tvAccData.text = "${String.format("%.2f", event.values[0])} ${String.format("%.2f", event.values[1])} ${String.format("%.2f", event.values[2])}"
            val speed =
                (event.values[0] * event.values[0] + event.values[1] * event.values[1] + event.values[2] * event.values[2]).toDouble().pow(0.5)
            if (speed > MOVE_THRESHOLD && moveCount < 500) {
                stationaryCount = 0
                moveCount++
            } else if (speed < STATIONARY_THRESHOLD && stationaryCount < 500) {
                moveCount = 0
                stationaryCount++
            } else {
                if (moveCount > 0) moveCount--
                if (moveCount > 0) stationaryCount--
            }
            if (speed > prev * 1.01 && incCount < 500) {
                incCount++
            } else if (speed < prev * 0.99 && decCount < 500) {
                decCount++
            }

            if (moveCount > 500) {
                tvStatus.text = "MOVING"
            } else if (stationaryCount > 500) {
                tvStatus.text = "STOPPED"
            }
            if (incCount > INC_CONF) {
                tvStatus2.text = "INCREASE ${incCount} ${decCount}"
                decCount = 0
            } else if (decCount > DEC_CONF) {
                tvStatus2.text = "DECREASE ${incCount} ${decCount}"
                incCount = 0
            } else {
                tvStatus2.text = "${incCount} ${decCount}"
            }
            prev = speed
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    override fun onDestroy() {
        mSensorManager.unregisterListener(this)
        super.onDestroy()
    }
}