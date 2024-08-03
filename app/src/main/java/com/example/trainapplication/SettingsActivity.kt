package com.example.trainapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.trainapplication.TrainApplication.Companion.ALERT_STOPS_AWAY
import com.example.trainapplication.TrainApplication.Companion.REACHED_DIST
import com.example.trainapplication.TrainApplication.Companion.REACHING_DIST
import com.google.android.material.slider.Slider


class SettingsActivity : AppCompatActivity() {
    private lateinit var tvStopsAway : TextView
    private lateinit var tvReached : TextView
    private lateinit var tvReaching : TextView
    private lateinit var slStopsAway : Slider
    private lateinit var slReached : Slider
    private lateinit var slReaching : Slider

    private fun updateValues() {
        tvStopsAway.text = "Notify: ${ALERT_STOPS_AWAY} stops away"
        tvReaching.text = "Reaching: ${REACHING_DIST} metres away"
        tvReached.text = "Reached: ${REACHED_DIST} metres away"
        slStopsAway.value = ALERT_STOPS_AWAY.toFloat()
        slReaching.value = REACHING_DIST.toFloat()
        slReached.value = REACHED_DIST.toFloat()

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        tvStopsAway = findViewById(R.id.tvStopsAway)
        slStopsAway = findViewById(R.id.slStopsAway)
        tvReached = findViewById(R.id.tvReached)
        slReached = findViewById(R.id.slReached)
        tvReaching = findViewById(R.id.tvReaching)
        slReaching = findViewById(R.id.slReaching)

        updateValues()

        slStopsAway.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            ALERT_STOPS_AWAY = value.toInt()
            updateValues()
        })
        slReached.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            REACHED_DIST = value.toInt()
            updateValues()
        })
        slReaching.addOnChangeListener(Slider.OnChangeListener { slider, value, fromUser ->
            REACHING_DIST = value.toInt()
            updateValues()
        })

        var btnBack : Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            val intent = Intent(this, SelectStartActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}