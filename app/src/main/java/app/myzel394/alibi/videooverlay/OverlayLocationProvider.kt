package app.myzel394.alibi.videooverlay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import app.myzel394.alibi.ui.utils.PermissionHelper

class OverlayLocationProvider(
    private val context: Context,
) {
    @Volatile
    private var latestLocation: OverlayLocation? = null
    private var started = false

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            latestLocation = location.toOverlayLocation()
        }

        @Deprecated("Deprecated in Android framework")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    fun getLatestLocation(): OverlayLocation? = latestLocation

    @SuppressLint("MissingPermission")
    fun start() {
        if (started) {
            return
        }

        if (!PermissionHelper.hasGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            latestLocation = null
            return
        }

        started = true

        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        ).filter { provider ->
            locationManager.allProviders.contains(provider)
        }

        latestLocation = providers
            .mapNotNull { provider -> locationManager.getLastKnownLocation(provider) }
            .maxByOrNull { location -> location.time }
            ?.toOverlayLocation()

        providers
            .filter { provider -> locationManager.isProviderEnabled(provider) }
            .forEach { provider ->
                locationManager.requestLocationUpdates(
                    provider,
                    MIN_UPDATE_TIME_MS,
                    MIN_UPDATE_DISTANCE_METERS,
                    listener,
                    Looper.getMainLooper(),
                )
            }
    }

    fun stop() {
        if (!started) {
            return
        }

        locationManager.removeUpdates(listener)
        started = false
    }

    private fun Location.toOverlayLocation(): OverlayLocation {
        return OverlayLocation(
            latitude = latitude,
            longitude = longitude,
        )
    }

    companion object {
        private const val MIN_UPDATE_TIME_MS = 1000L
        private const val MIN_UPDATE_DISTANCE_METERS = 0f
    }
}
