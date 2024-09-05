
package com.example.rmas.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.navigation.NavController
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun FilterPage(navController: NavController) {
    var imeAutora by remember { mutableStateOf("") }
    var naziv by remember { mutableStateOf("") }
    var opis by remember { mutableStateOf("") }
    var popusti by remember { mutableStateOf("") }
    var radnoVreme by remember { mutableStateOf("") }
    var odDatum by remember { mutableStateOf("") }
    var doDatum by remember { mutableStateOf("") }

    val firestore = FirebaseFirestore.getInstance()
    val trzniCentri = remember { mutableStateListOf<Map<String, Any>>() }

    Column {
        // Polja za unos filtera
        TextField(value = imeAutora, onValueChange = { imeAutora = it }, label = { Text("Ime autora") })
        TextField(value = naziv, onValueChange = { naziv = it }, label = { Text("Naziv") })
        TextField(value = opis, onValueChange = { opis = it }, label = { Text("Opis") })
        TextField(value = radnoVreme, onValueChange = { radnoVreme = it }, label = { Text("Radno vreme") })
        TextField(value = popusti, onValueChange = { popusti = it }, label = { Text("Popusti") })
        TextField(value = odDatum, onValueChange = { odDatum = it }, label = { Text("Od datum (yyyy-MM-dd)") })
        TextField(value = doDatum, onValueChange = { doDatum = it }, label = { Text("Do datum (yyyy-MM-dd)") })

        Button(onClick = {
            // Preuzmanje filtriranih podatakaa iz Firestore
            filterTrzniCentri(firestore, imeAutora, naziv, opis, radnoVreme, popusti, odDatum, doDatum) { rezultat ->
                trzniCentri.clear()
                trzniCentri.addAll(rezultat)
            }
        }) {
            Text(text = "Filtriraj")
        }
        Button(onClick = {
            navController.popBackStack()
        }) {
            Text(text = "Nazad")
        }

        // Prikaz filtriranih tržnih centara
        LazyColumn {
            items(trzniCentri) { centar ->
                Text(text = centar["naziv"] as String)
            }
        }
    }
}

fun filterTrzniCentri(
    firestore: FirebaseFirestore,
    imeAutora: String,
    naziv: String,
    opis: String,
    radnoVreme: String,
    popusti: String,
    odDatum: String,
    doDatum: String,
    onResult: (List<Map<String, Any>>) -> Unit
) {
    var query = firestore.collection("trzni_centri")

    if (imeAutora.isNotEmpty()) {
        // Trazi se UID autora na osnovu imena iz users kolekcije
        firestore.collection("users")
            .whereEqualTo("ime", imeAutora)
            .get()
            .addOnSuccessListener { userSnapshot ->
                if (!userSnapshot.isEmpty) {
                    val userId = userSnapshot.documents[0].id
                    query = query.whereEqualTo("authorId", userId) as CollectionReference
                }
                applyAdditionalFilters(query, naziv, opis, radnoVreme, popusti, odDatum, doDatum, onResult)
            }
    } else {
        applyAdditionalFilters(query, naziv, opis, radnoVreme, popusti, odDatum, doDatum, onResult)
    }
}


fun applyAdditionalFilters(
    query: Query,
    naziv: String,
    opis: String,
    radnoVreme: String,
    popusti: String,
    odDatum: String,
    doDatum: String,
    onResult: (List<Map<String, Any>>) -> Unit
) {
    var filteredQuery = query

    if (naziv.isNotEmpty()) filteredQuery = filteredQuery.whereEqualTo("naziv", naziv)
    if (opis.isNotEmpty()) filteredQuery = filteredQuery.whereEqualTo("opis", opis)
    if (radnoVreme.isNotEmpty()) filteredQuery = filteredQuery.whereEqualTo("radnoVreme", radnoVreme)
    if (popusti.isNotEmpty()) filteredQuery = filteredQuery.whereEqualTo("popusti", popusti)
    if (odDatum.isNotEmpty() && doDatum.isNotEmpty()) {
        filteredQuery = filteredQuery.whereGreaterThanOrEqualTo("datumKreiranja", odDatum)
            .whereLessThanOrEqualTo("datumKreiranja", doDatum)
    }

    // Vraćanje podataka iz Firestore
    filteredQuery.get().addOnSuccessListener { snapshot ->
        val results = snapshot.documents.mapNotNull { documentSnapshot ->
            documentSnapshot.data
        }
        onResult(results)
    }.addOnFailureListener { exception ->

        onResult(emptyList())
    }
}
