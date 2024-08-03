package com.example.trainapplication
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Utils {
    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    // Harvesine Formula
    fun getDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double {
        val R = 6371
        val dLat = deg2rad(lat2-lat1)
        val dLon = deg2rad(lon2-lon1)
        val a =
            sin(dLat/2) * sin(dLat/2) +
                    cos(deg2rad(lat1)) * cos(deg2rad(lat2)) *
                    sin(dLon/2) * sin(dLon/2)
        ;
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        val d = R * c // Distance in km
        return d
    }

    fun deg2rad(deg: Double) : Double {
        return deg * (Math.PI/180)
    }

    fun getLineIndex(lineName: String) : Int {
        if (lineName == "EW") return 0
        else if (lineName == "DT") return 1
        else return -1
    }

    fun getButtonXML(lineName: String) : Int {
        if (lineName == "EW") return R.drawable.eastwestbutton
        else if (lineName == "DT") return R.drawable.downtownbutton
        else return -1
    }

    fun getCircleXML(lineName: String) : Int {
        if (lineName == "EW") return R.drawable.eastwestcircle
        else if (lineName == "DT") return R.drawable.downtowncircle
        else return -1
    }

    fun getFromStationName(lineName: String) : String {
        if (lineName == "EW") return "EW1 Pasir Ris"
        else if (lineName == "DT") return "DT1 Bukit Panjang"
        else return ""
    }

    fun getToStationName(lineName: String) : String {
        if (lineName == "EW") return "EW30 Joo Koon"
        else if (lineName == "DT") return "DT30 Expo"
        else return ""
    }

    fun getStationsList(lineName: String, applicationContext: Context) : List<Station> {
        val jsonString = getJsonDataFromAsset(applicationContext, "stations.json")

        val listLineType = object : TypeToken<List<Line>>() {}.type
        val linesObj : List<Line> = Gson().fromJson(jsonString, listLineType)
        val lineIndex = getLineIndex(lineName)

        return linesObj.get(lineIndex).stations
    }
}