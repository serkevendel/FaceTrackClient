package com.vsoft.trackify.service

import android.Manifest
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.Task
import com.vsoft.trackify.R
import com.vsoft.trackify.model.User
import com.vsoft.trackify.util.ResolvableApiExceptionHolder
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*


class LocationSharingService : Service() {

    lateinit var user: User

    val builder = LocationSettingsRequest.Builder()
    val locationRequest = LocationRequest()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null
    var locationRequestPeriodChangedBroadcastReceiver: BroadcastReceiver? = null
    var resolvableApiExceptionResolvedBroadcastReceiver: BroadcastReceiver? = null

    companion object {
        const val BASE_URL = "http://10.0.2.2"
        const val PORT = "8080"
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        user = intent?.getSerializableExtra("user") as User

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Create broadcast receivers

        locationRequestPeriodChangedBroadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                fusedLocationClient.removeLocationUpdates(locationCallback)
                val timeInSeconds = intent!!.getIntExtra("interval", 5)
                locationRequest.interval = timeInSeconds * 1000L

                //register updates with the changed request
                if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                }
            }
        }

        resolvableApiExceptionResolvedBroadcastReceiver = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                //User approved the settings, continue service
                requestLocationUpdates()
            }
        }

        var shareInterval: Long = 5 * 1000L //default to 5 sec
        when (getLocationShareIntervalSetting()) {
            0 -> shareInterval = 5 * 1000L
            1 -> shareInterval = 60 * 1000L
            2 -> shareInterval = 600 * 1000L
        }

        locationRequest.apply {
            interval = shareInterval
            priority = PRIORITY_HIGH_ACCURACY
        }

        builder.addLocationRequest(locationRequest)

        //Register broadcast receivers
        LocalBroadcastManager.getInstance(this).registerReceiver(resolvableApiExceptionResolvedBroadcastReceiver as BroadcastReceiver,
                IntentFilter("com.vsoft.trackify.activity.RESOLVABLE_API_EXCEPTION_RESOLVED"))


        LocalBroadcastManager.getInstance(this).registerReceiver(locationRequestPeriodChangedBroadcastReceiver as BroadcastReceiver,
                IntentFilter("com.vsoft.trackify.activity.LOCATION_REQUEST_PERIOD_CHANGED"))

        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener {
            run {
                requestLocationUpdates()
            }
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    //Store the exception and send a broadcast to an active activity to handle the dialog showing
                    ResolvableApiExceptionHolder.resolvableApiException = exception
                    val intent = Intent("com.vsoft.trackify.service.RESOLVABLE_API_EXCEPTION")
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun sendLocation(location: Location) {
        val requestURL = "$BASE_URL:$PORT/users"
        val queue = Volley.newRequestQueue(this)
        // Request a string response from the provided URL.
        val jsonData = JSONObject()

        jsonData.put("id", user.id)
                .put("name", user.name)
                .put("latitude", location.latitude)
                .put("longitude", location.longitude)
                .put("lastLocationDate", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(Date()))
        println(jsonData)

        val currentLocationRequest = JsonObjectRequest(Request.Method.POST, requestURL, jsonData, Response.Listener {
            Log.i("http_response", "Received message:\n$it")
        }, Response.ErrorListener {
            Log.i("http_response", "Error happened!\n$it")
        })

        queue.add(currentLocationRequest)
    }

    private fun requestLocationUpdates() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    sendLocation(location)
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        println("On destroy called!")
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        if(resolvableApiExceptionResolvedBroadcastReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.resolvableApiExceptionResolvedBroadcastReceiver!!)
        }
        if(locationRequestPeriodChangedBroadcastReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.locationRequestPeriodChangedBroadcastReceiver!!)
        }
    }

    private fun getLocationShareIntervalSetting(): Int {
        val sharedPref = this.getSharedPreferences("com.vsoft.trackify.PREFERENCE_FILE_KEY", Context.MODE_PRIVATE)
        return sharedPref.getInt(getString(R.string.share_interval), 0)
    }

}