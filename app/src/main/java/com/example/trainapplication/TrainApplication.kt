package com.example.trainapplication

import android.app.Application

class TrainApplication : Application() {
    companion object {
        var ALERT_STOPS_AWAY : Int = 3
        var REACHING_DIST : Int = 500
        var REACHED_DIST : Int = 200
    }

}