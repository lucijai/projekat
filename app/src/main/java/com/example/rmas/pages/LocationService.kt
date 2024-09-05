package com.example.rmas

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val firestore = FirebaseFirestore.getInstance()
    private val userUid = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 60000 // 1 minut
            fastestInterval = 30000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        sendLocationToFirestore(location)
                        detectNearbyObject(location)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun sendLocationToFirestore(location: Location) {
        userUid?.let { uid ->
            val userLocation = hashMapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("users").document(uid)
                .update("location", userLocation)
                .addOnSuccessListener {
                    Log.d("LocationService", "Location successfully updated in Firestore")
                }
                .addOnFailureListener { e ->
                    Log.w("LocationService", "Error updating location", e)
                }
        }
    }

    private fun detectNearbyObject(location: Location) {
        Log.d("LocationService", "Checking nearby objects for location: ${location.latitude}, ${location.longitude}")
        val thresholdDistance = 5000.0 // Udaljenost u metrima za detekciju objekta u blizini

        firestore.collection("trzni_centri").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val objectLatitude = document.getDouble("latitude") ?: continue
                    val objectLongitude = document.getDouble("longitude") ?: continue

                    val objectLocation = Location("object").apply {
                        latitude = objectLatitude
                        longitude = objectLongitude
                    }

                    val distance = location.distanceTo(objectLocation)

                    Log.d("LocationService", "Distance to object: $distance meters")

                    if (distance <= thresholdDistance) {
                        Log.d("LocationService", "Objekat detektovan u blizini")
                        showNotificationForNearbyObject()
                        break
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("LocationService", "Error getting nearby objects", e)
            }
    }



    private fun showNotificationForNearbyObject() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Nearby Object Notification",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Object Nearby")
            .setContentText("Objekat detektovan u blizini")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(getPendingIntent())
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun startForegroundService() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Service ",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Service")
            .setContentText("Praćenje lokacije u pozadini")
            .setSmallIcon(R.drawable.ic_location)
            .setContentIntent(getPendingIntent())
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val NOTIFICATION_ID = 1234
        private const val CHANNEL_ID = "location_service_channel"
    }
}
