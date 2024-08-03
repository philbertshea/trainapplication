package com.example.trainapplication

import android.content.Intent
import android.graphics.Color
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


class SelectEndActivity : AppCompatActivity() {

    private fun updateStations(lineName: String, startName: String, startCode: Int, circleParent: LinearLayout, buttonParent: LinearLayout) {
        val utilObj = Utils()
        val jsonString = utilObj.getJsonDataFromAsset(applicationContext, "stations.json")

        val listLineType = object : TypeToken<List<Line>>() {}.type
        val linesObj : List<Line> = Gson().fromJson(jsonString, listLineType)
        val lineIndex = utilObj.getLineIndex(lineName)

        var stationIterator = linesObj.get(lineIndex).stations.listIterator()

        var circleParams = LinearLayout.LayoutParams(140, 140)
        circleParams.setMargins(5, 0, 0, 11)
        var buttonParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        buttonParams.setMargins(5, 0, 0, 20)

        while (stationIterator.hasNext()) {
            val station = stationIterator.next()
            var newCircle : ImageView = ImageView(this)
            newCircle.setImageResource(utilObj.getCircleXML(lineName))
            circleParent.addView(newCircle)
            newCircle.setPadding(5, 0, 0, 5)
            newCircle.layoutParams = circleParams

            var newButton : Button = Button(this)
            newButton.text = getString(R.string.station_name_wo_dist, lineName, station.code, station.name)
            buttonParent.addView(newButton)
            newButton.textSize = 26F
            newButton.setTextColor(Color.parseColor("#FFFFFF"))
            if (startName == station.name) {
                newButton.setBackgroundResource(R.drawable.from_button)
            } else {
                newButton.setBackgroundResource(utilObj.getButtonXML(lineName))
            }

            newButton.setPadding(5, 0, 0, 5)
            newButton.layoutParams = buttonParams
            newButton.setOnClickListener {
                val intent = Intent(this, OnTrainTestActivity::class.java)
                intent.putExtra("LINE_NAME", lineName)
                intent.putExtra("START_NAME", startName)
                intent.putExtra("START_CODE", startCode)
                intent.putExtra("END_NAME", station.name)
                intent.putExtra("END_CODE", station.code)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_selectend)
        val line_name = intent.getStringExtra("LINE_NAME")
        val start_name = intent.getStringExtra("STN_NAME")
        val start_code = intent.getIntExtra("STN_CODE", 0)
        Log.i("SelectEndActivity", "$line_name $start_name $start_code")
        val tvFromStation : TextView = findViewById(R.id.tvFromStation)
        tvFromStation.text = getString(R.string.from_station_name, line_name, start_code, start_name)

        val btnChangeStart : Button = findViewById(R.id.btnChangeStart)
        btnChangeStart.setOnClickListener {
            val intent = Intent(this, SelectStartActivity::class.java)
            startActivity(intent)
            finish()
        }
        val circleParent : LinearLayout = findViewById(R.id.circleparent)
        val buttonParent : LinearLayout = findViewById(R.id.buttonparent)
        val utilObj = Utils()
        val tvTowardsStart : TextView = findViewById(R.id.tvTowardsStart)
        val tvTowardsEnd : TextView = findViewById(R.id.tvTowardsEnd)
        line_name?.let {
            start_name?.let {
                updateStations(line_name, start_name, start_code, circleParent, buttonParent)
            }
            val towardsStartStation = utilObj.getFromStationName(line_name)
            val towardsEndStation = utilObj.getToStationName(line_name)
            tvTowardsStart.text = getString(R.string.towards_station_name, towardsStartStation)
            tvTowardsEnd.text = getString(R.string.towards_station_name, towardsEndStation)
        }

    }
}