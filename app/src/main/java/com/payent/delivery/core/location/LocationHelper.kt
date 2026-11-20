package com.payent.delivery.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class LocationState(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val resolvedAddress: String? = null,
    val hasPermission: Boolean = false,
    val isGpsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    fun isGpsProviderEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun getCurrentOrLastKnownLocation(
        context: Context,
        onLocationResult: (LocationState) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onLocationResult(
                LocationState(
                    hasPermission = false,
                    isGpsEnabled = isGpsProviderEnabled(context),
                    isLoading = false,
                    errorMessage = "Location permission is required to view live GPS tracking."
                )
            )
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (locationManager == null) {
            onLocationResult(
                LocationState(
                    hasPermission = true,
                    isGpsEnabled = false,
                    isLoading = false,
                    errorMessage = "Location service is unavailable on this device."
                )
            )
            return
        }

        val isGpsEnabled = isGpsProviderEnabled(context)
        if (!isGpsEnabled) {
            onLocationResult(
                LocationState(
                    hasPermission = true,
                    isGpsEnabled = false,
                    isLoading = false,
                    errorMessage = "GPS is disabled. Please enable device location."
                )
            )
            return
        }

        try {
            val gpsLocation = try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                null
            }

            val netLocation = try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (e: SecurityException) {
                null
            }

            val bestLocation = gpsLocation ?: netLocation

            if (bestLocation != null) {
                val address = resolveAddressFromCoordinates(bestLocation.latitude, bestLocation.longitude)
                onLocationResult(
                    LocationState(
                        latitude = bestLocation.latitude,
                        longitude = bestLocation.longitude,
                        resolvedAddress = address,
                        hasPermission = true,
                        isGpsEnabled = true,
                        isLoading = false,
                        errorMessage = null
                    )
                )
            } else {
                // If no last known location, register a single request
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        val address = resolveAddressFromCoordinates(location.latitude, location.longitude)
                        onLocationResult(
                            LocationState(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                resolvedAddress = address,
                                hasPermission = true,
                                isGpsEnabled = true,
                                isLoading = false,
                                errorMessage = null
                            )
                        )
                    }

                    override fun onProviderDisabled(provider: String) {}
                    override fun onProviderEnabled(provider: String) {}
                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                }

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null)
                } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null)
                } else {
                    onLocationResult(
                        LocationState(
                            hasPermission = true,
                            isGpsEnabled = true,
                            isLoading = false,
                            errorMessage = "Current location temporarily unavailable. Waiting for GPS fix."
                        )
                    )
                }
            }
        } catch (e: Exception) {
            onLocationResult(
                LocationState(
                    hasPermission = true,
                    isGpsEnabled = true,
                    isLoading = false,
                    errorMessage = "Error resolving device location: ${e.localizedMessage ?: "Unknown error"}"
                )
            )
        }
    }

    fun calculateDistanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    fun calculateEtaMinutes(distanceKm: Double, averageSpeedKmh: Double = 25.0): Int {
        val hours = distanceKm / averageSpeedKmh
        return (hours * 60).toInt().coerceAtLeast(1)
    }

    private fun resolveAddressFromCoordinates(lat: Double, lon: Double): String {
        return String.format("%.4f° N, %.4f° E (GPS Position)", lat, lon)
    }
}
