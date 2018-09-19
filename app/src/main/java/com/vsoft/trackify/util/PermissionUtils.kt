package com.vsoft.trackify.util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.vsoft.trackify.R

object PermissionUtils {

    val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 10
    fun requestPermissions(activity: Activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.
            val builder = AlertDialog.Builder(activity)
            builder.setPositiveButton(R.string.ok, DialogInterface.OnClickListener { dialog, id ->
                // User clicked OK button
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
                )
            })

            builder.setTitle("Location access permission")
            builder.setMessage("Trackify needs to access your device's location information in order to be useful.")
            builder.show()

        } else {
            println("No explanation needed!")
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    fun checkForLocationPermissions(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}