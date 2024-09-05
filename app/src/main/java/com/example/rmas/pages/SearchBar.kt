// SearchBar.kt
package com.example.rmas

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearchClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.weight(1f),
            placeholder = { Text(text = "Pretraži...") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSearchClicked) {
            Text(text = "Pretraži")
        }
    }
}
