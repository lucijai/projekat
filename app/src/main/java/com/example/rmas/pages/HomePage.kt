
package com.example.rmas

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.TextButton
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

import com.example.rmas.pages.LocationData




@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {

    val authState by authViewModel.authState.observeAsState()
    val firestore = FirebaseFirestore.getInstance()

    // State za prikaz lokacija
    val locations = remember { mutableStateOf<List<LocationData>>(emptyList()) }
    val showLocations = remember { mutableStateOf(false) }

    // State za prikaz rangiranih korisnika
    val rankedUsers = remember { mutableStateOf<List<User>>(emptyList()) }
    val showRankedUsers = remember { mutableStateOf(false) }

    // State za upravljanje proširenjem menija
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("Izaberi opciju") }


    val showUserInfo = remember { mutableStateOf(false) }
    val currentUser by authViewModel.currentUser.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            is AuthState.Authenticated -> {
                authViewModel.loadCurrentUserData()
            }
            else -> Unit
        }
    }


    // Funkcija za učitavanje lokacija iz Firestore-a
    fun loadLocations() {
        firestore.collection("trzni_centri")
            .get()
            .addOnSuccessListener { result ->
                val locationList = result.mapNotNull { document ->
                    val latitude = document.getDouble("latitude") ?: return@mapNotNull null
                    val longitude = document.getDouble("longitude") ?: return@mapNotNull null
                    val naziv = document.getString("naziv") ?: ""
                    val opis = document.getString("opis") ?: ""

                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        naziv = naziv,
                        opis = opis
                    )
                }
                locations.value = locationList
                showLocations.value = true
            }
            .addOnFailureListener { e ->
                Log.w("HomePage", "Greška pri učitavanju lokacija iz Firestore", e)
            }
    }



    // Funkcija za učitavanje rangiranih korisnika iz Firestore-a
    fun loadRankedUsers() {
        firestore.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val userList = result.mapNotNull { document ->
                    val fullName = document.getString("fullName") ?: return@mapNotNull null
                    val points = document.getLong("points") ?: return@mapNotNull null

                    User(
                        fullName = fullName,
                        points = points
                    )
                }
                rankedUsers.value = userList
                showRankedUsers.value = true
            }
            .addOnFailureListener { e ->
                Log.w("HomePage", "Greška pri učitavanju rangiranih korisnika iz Firestore", e)
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.img),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(   modifier = modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Dobrodosli u aplikaciju", fontSize = 32.sp,color = Color.White )

            Spacer(modifier = Modifier.height(16.dp))
            currentUser?.let { user ->
                UserInfo(user)
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Dugme za padajući meni
            Box {
                Button(onClick = { expanded = !expanded }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Gray )) {
                    Text(text = selectedOption)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(onClick = {
                        loadLocations()
                        selectedOption = "Prikaz objekata u tabeli"
                        expanded = false
                    }) {
                        Text("Prikazi trzne centre")
                    }
                    DropdownMenuItem(onClick = {
                        navController.navigate("map")
                        selectedOption = "Otvori mapu"
                        expanded = false
                    }) {
                        Text("Pretrazi, dodaj,oceni i pronadji trzne centre")
                    }
                    DropdownMenuItem(onClick = {
                        loadRankedUsers()
                        selectedOption = "Prikazi rangiranu listu korisnika"
                        expanded = false
                    }) {
                        Text("Rangirana lista korisnika po bodovima")
                    }

                }
            }

            if (showLocations.value) {
                Column(modifier = Modifier.weight(1f)) {
                    TextButton(onClick = { showLocations.value = false }) {
                        Text(text = "Zatvori", color = Color.White)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(locations.value) { location ->
                            LocationRow(location)
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }



            // Prikaz rangiranih korisnika
            if (showRankedUsers.value) {
                Column(modifier = Modifier.weight(1f)) {
                    TextButton(onClick = { showRankedUsers.value = false }) {
                        Text(text = "Zatvori", color = Color.White)
                    }
                    LazyColumn {
                        items(rankedUsers.value) { user ->
                            UserRow(user)
                        }
                    }
                }
            }

                // Dugme za navigaciju ka stranici za filtriranje
            Button(
                onClick = { navController.navigate("filterPage") },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RectangleShape
            ) {
                Text(text = "Filtriraj")
            }






            TextButton(onClick = {
                authViewModel.signout()
            }) {
                Text(text = "Odjavi se",color = Color.White )
            }


            // Info deo sa tekstom
            Spacer(modifier = Modifier.height(48.dp))
            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aplikacija vam pomaže da pronađete najbolje lokacije za šoping i trgovinu na našim prostorima!Takođe imate opciju da dodate vaše preporuke, komentare i ocene, i sakupite bodove koji vas dovode do nagrada.",
                    color = Color.White,
                    fontSize = 18.sp,

                    )
            }
        }
    }
}
@Composable
fun LocationRow(location: LocationData) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .fillMaxWidth()
            .heightIn(min = 120.dp)
    ) {

        Text(
            text = location.naziv,

            modifier = Modifier.weight(1f).padding(end = 8.dp),
            color = Color.Black
        )
        Text(
            text = location.opis,
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            color = Color.Black
        )
        Text(
            text = "Lat: ${location.latitude}, Lng: ${location.longitude}",
            modifier = Modifier.weight(1f).padding(end = 8.dp),
            color = Color.Black
        )
    }
}

@Composable
fun UserRow(user: User) {
    Row(modifier = Modifier.padding(8.dp)) {
        Text(
            text = user.fullName,
            modifier = Modifier.weight(1f),
            color = Color.White
        )
        Text(
            text = "Bodovi: ${user.points}",
            modifier = Modifier.weight(1f),
            color = Color.White
        )
    }
}
@Composable
fun UserInfo(user: User) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text = "Ime i Prezime: ${user.fullName}", fontSize = 20.sp,  color = Color.White)

        Text(text = "Broj Bodova: ${user.points}", fontSize = 20.sp,  color = Color.White)
    }
}


data class User(
    val fullName: String,

    val points: Long
)

