
package com.example.rmas.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log


data class LocationData(

    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val naziv: String = "",
    val opis: String="",
    val radnoVreme: String = "",
    val popusti: String = "",
    val id: String = "",

)

@Composable
fun fetchLocations(onLocationsFetched: (List<LocationData>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val locationsRef = db.collection("trzni_centri")



    LaunchedEffect(Unit) {
        locationsRef.get().addOnSuccessListener { result ->
            val locations = result.mapNotNull { document ->
                document.toObject(LocationData::class.java)
            }
            onLocationsFetched(locations)
        }.addOnFailureListener { exception ->
            // Loguj grešku
            Log.e("MapPage", "Error fetching locations", exception)
        }
    }
}
