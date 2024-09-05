


package com.example.rmas.pages


import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.example.rmas.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun MapPage(modifier: Modifier = Modifier, navController: NavController) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(43.3210, 21.8954), 10f)
    }

    val locationState = remember { mutableStateOf<LatLng?>(null) }
    val markers = remember { mutableStateOf<List<LocationData>>(emptyList()) }
    val filteredMarkers = remember { mutableStateOf<List<LocationData>>(emptyList()) }
    val searchQuery = remember { mutableStateOf("") }
    val radius = remember { mutableStateOf(1000000.0) } // DIFOLTNI RADIJUS

    val isAddingObject = remember { mutableStateOf(false) }
    val useCurrentLocation = remember { mutableStateOf(true) }
    val newObjectName = remember { mutableStateOf("") }
    val newObjectDescription = remember { mutableStateOf("") }
    val newObjectHours = remember { mutableStateOf("") }
    val newObjectDiscounts = remember { mutableStateOf("") }
    val newObjectLatitude = remember { mutableStateOf("") }
    val newObjectLongitude = remember { mutableStateOf("") }

    // Preuzmi lokacije iz Firestore-a
    fetchLocations { locations ->
        markers.value = locations
        filteredMarkers.value = locations
        Log.d("MapPage", "Markers updated: ${markers.value}") // Loguj preuzete markere
    }

    //  ažuriranja lokacije
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    locationState.value = LatLng(it.latitude, it.longitude)
                    cameraPositionState.position =
                        CameraPosition.fromLatLngZoom(locationState.value!!, 15f)
                }
            }
        } else {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    LaunchedEffect(locationState.value) {
        locationState.value?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }

    // Funkcija za filtriranje lokacija na osnovu upita za pretragu i radijusa
    fun filterLocations() {
        locationState.value?.let { currentLocation ->
            filteredMarkers.value = markers.value.filter { location ->
                val distance = distanceBetween(currentLocation, LatLng(location.latitude, location.longitude))
                val matchesQuery = searchQuery.value.isEmpty() ||
                        location.naziv.contains(searchQuery.value, ignoreCase = true) ||
                        location.opis.contains(searchQuery.value, ignoreCase = true)

                matchesQuery && distance <= radius.value
            }
        }
    }

    // Funkcija za dodavanje nove lokacije
    fun addObject(location: LocationData) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val locationData = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude,
            "naziv" to location.naziv,
            "opis" to location.opis,
            "radnoVreme" to location.radnoVreme,
            "popusti" to location.popusti,
            "createdAt" to FieldValue.serverTimestamp(),
            "autor" to uid
        )

        firestore.collection("trzni_centri")
            .add(locationData)
            .addOnSuccessListener {
                Log.d("MapPage", "Lokacija uspešno dodata u Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("MapPage", "Greška pri dodavanju lokacije u Firestore", e)
            }
    }

    fun saveCommentAndRating(location: LocationData, comment: String, rating: Int) {
        val firestore = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // Dodaj komentar i ocenu za lokaciju
        val commentData = hashMapOf(
            "locationId" to location.id,
            "comment" to comment,
            "rating" to rating,
            "uid" to uid
        )

        firestore.collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                Log.d("MapPage", "Komentar i ocena su uspešno dodati u Firestore")
            }
            .addOnFailureListener { e ->
                Log.w("MapPage", "Greška pri dodavanju komentara i ocene u Firestore", e)
            }

        // Ažuriraj poene korisnika
        if (uid != null) {
            val userRef = firestore.collection("users").document(uid)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val newPoints = snapshot.getLong("points")?.plus(1) ?: 1
                transaction.update(userRef, "points", newPoints)
                val existingRating = snapshot.getLong("rating")?.toInt() ?: 0
                val newRating = (existingRating + rating) / 2
                transaction.update(userRef, "rating", newRating)
                if (newPoints >= 5) {
                    transaction.update(userRef, "rank", "Viši Rang")
                }
            }.addOnSuccessListener {
                Log.d("MapPage", "Poeni i rejting korisnika su uspešno ažurirani")
            }.addOnFailureListener { e ->
                Log.w("MapPage", "Greška pri ažuriranju poena i rejtinga korisnika", e)
            }
        }
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Button(
                    onClick = {
                        navController.navigate("home")
                    },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Nazad")
                }
                // Search UI
                Row(modifier = Modifier.padding(16.dp)) {
                    TextField(
                        value = searchQuery.value,
                        onValueChange = { newText ->
                            searchQuery.value = newText
                            filterLocations()
                        },
                        label = { Text("Pretraga") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = radius.value.toString(),
                        onValueChange = { newText ->
                            radius.value = newText.toDoubleOrNull()
                                ?: 5000.0
                            filterLocations()
                        },
                        label = { Text("Radijus (m)") },
                        modifier = Modifier.width(120.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { filterLocations() }) {
                        Text("Pretraži")
                    }
                }
                // Add Location UI


                if (isAddingObject.value) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .shadow(4.dp, RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "Dodaj Novi Objekat",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        // Opcija za odabir lokacije
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row {
                                RadioButton(
                                    selected = useCurrentLocation.value,
                                    onClick = { useCurrentLocation.value = true }
                                )
                                Text("Trenutna Lokacija", modifier = Modifier.align(Alignment.CenterVertically))
                            }
                            Row {
                                RadioButton(
                                    selected = !useCurrentLocation.value,
                                    onClick = { useCurrentLocation.value = false }
                                )
                                Text("Ručni Unos", modifier = Modifier.align(Alignment.CenterVertically))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextField(
                            value = newObjectName.value,
                            onValueChange = { newText -> newObjectName.value = newText },
                            label = { Text("Naziv Objekta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newObjectDescription.value,
                            onValueChange = { newText -> newObjectDescription.value = newText },
                            label = { Text("Opis Objekta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newObjectHours.value,
                            onValueChange = { newText -> newObjectHours.value = newText },
                            label = { Text("Radno Vreme") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = newObjectDiscounts.value,
                            onValueChange = { newText -> newObjectDiscounts.value = newText },
                            label = { Text("Popusti") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!useCurrentLocation.value) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = newObjectLatitude.value,
                                onValueChange = { newText -> newObjectLatitude.value = newText },
                                label = { Text("Latitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = newObjectLongitude.value,
                                onValueChange = { newText -> newObjectLongitude.value = newText },
                                label = { Text("Longitude") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    val latitude = if (useCurrentLocation.value) {
                                        locationState.value?.latitude
                                    } else {
                                        newObjectLatitude.value.toDoubleOrNull()
                                    }
                                    val longitude = if (useCurrentLocation.value) {
                                        locationState.value?.longitude
                                    } else {
                                        newObjectLongitude.value.toDoubleOrNull()
                                    }

                                    if (latitude != null && longitude != null) {
                                        val newLocation = LocationData(
                                            latitude = latitude,
                                            longitude = longitude,
                                            naziv = newObjectName.value,
                                            opis = newObjectDescription.value,
                                            radnoVreme = newObjectHours.value,
                                            popusti = newObjectDiscounts.value
                                        )
                                        addObject(newLocation)
                                        isAddingObject.value = false
                                    } else {

                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                            ) {
                                Text("Dodaj", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { isAddingObject.value = false },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                            ) {
                                Text("Otkaži", color = Color.White)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = { isAddingObject.value = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    ) {
                        Text("Dodaj Objekat", color = Color.White)
                    }
                }

                var selectedMarker by remember { mutableStateOf<LocationData?>(null) }

                GoogleMap(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    cameraPositionState = cameraPositionState
                ) {
                    locationState.value?.let {
                        Marker(
                            state = rememberMarkerState(position = it),
                            title = "Moja lokacija"
                        )
                    }

                    filteredMarkers.value.forEach { location ->
                        Marker(
                            state = rememberMarkerState(
                                position = LatLng(location.latitude, location.longitude)
                            ),
                            title = location.naziv,
                            onClick = {
                                selectedMarker = location
                                true
                            }
                        )
                    }

                }

                if (selectedMarker != null) {
                    ShowCommentDialog(
                        locationName = selectedMarker!!.naziv,
                                onDismiss = { selectedMarker = null },
                        onSubmit = { comment, rating ->
                            saveCommentAndRating(selectedMarker!!, comment, rating)
                            selectedMarker = null
                        }
                    )

                }

            }
        }
    }


}

// Funkcija za izračunavanje distance između dva LatLng tačke
fun distanceBetween(start: LatLng, end: LatLng): Double {
    val earthRadius = 6371000 // meters
    val dLat = Math.toRadians(end.latitude - start.latitude)
    val dLng = Math.toRadians(end.longitude - start.longitude)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude)) *
            Math.sin(dLng / 2) * Math.sin(dLng / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

@Composable
fun ShowCommentDialog(
    locationName: String,
    onDismiss: () -> Unit,
    onSubmit: (String, Int) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = locationName) },
        text = {
            Column {
                TextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Komentar") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Ocena")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 4
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSubmit(comment, rating)
                onDismiss()
            }) {
                Text("Sačuvaj")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Otkaži")
            }
        }
    )
}
