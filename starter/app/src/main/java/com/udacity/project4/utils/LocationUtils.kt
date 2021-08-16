package com.udacity.project4.utils

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R


@TargetApi(29)
fun Fragment.isBackgroundLocationPermissionGranted(): Boolean {
    return if (runningQ || runningROrLater) {
        PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
    } else {
        true
    }
}

@TargetApi(29)
fun Fragment.isForegroundLocationPermissionGranted(): Boolean {
    return (PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )) && (PackageManager.PERMISSION_GRANTED ==
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
}


@TargetApi(29)
fun Fragment.areForegroundAndBackgroundLocationPermissionsGranted(): Boolean {
    return isForegroundLocationPermissionGranted() && isBackgroundLocationPermissionGranted()
}

@TargetApi(29)
fun Fragment.requestForegroundAndBackgroundLocationPermissions() {
    if (areForegroundAndBackgroundLocationPermissionsGranted()) return

    var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    val resultCode = when {
        runningQ -> {
            permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
            REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_PERMISSION_RESULT_CODE
        }
        !runningROrLater -> REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE
        else -> null
    }

    if (resultCode == null) {
        showLocationRequiredSnackbar()
    } else {
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissionsArray,
            resultCode
        )
    }
}

/*
 *  Requests ACCESS_FINE_LOCATION and (on Android 10+ (Q) ACCESS_BACKGROUND_LOCATION.
 */
@TargetApi(29)
fun Fragment.requestForegroundLocationPermissions() {
    if (isForegroundLocationPermissionGranted()) return

    val permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    val resultCode = REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE

    ActivityCompat.requestPermissions(
        requireActivity(),
        permissionsArray,
        resultCode
    )
}

fun Fragment.showLocationRequiredSnackbar() {
    Snackbar.make(
        requireView(),
        getString(R.string.location_required_error),
        Snackbar.LENGTH_INDEFINITE
    )
        .setAction(android.R.string.ok) {
            val intent = Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        }.show()
}

const val REQUEST_FOREGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 1
const val REQUEST_FOREGROUND_AND_BACKGROUND_LOCATION_PERMISSION_RESULT_CODE = 2

const val REQUEST_TURN_DEVICE_LOCATION_ON = 29

private val runningQ = android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.Q
private val runningROrLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R