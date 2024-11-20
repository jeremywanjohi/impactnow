package com.example.impactnow.ui.admin

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore

data class Opportunity(
    val id: String,
    val organizationName: String,
    val location: String,
    val requiredSkills: String
)

@Composable
fun AdminScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var opportunities by remember { mutableStateOf(listOf<Opportunity>()) }
    var organizationName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredSkills by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch opportunities from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("opportunities").get().addOnSuccessListener { snapshot ->
            opportunities = snapshot.documents.mapNotNull { document ->
                document.toObject(Opportunity::class.java)?.copy(id = document.id)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Admin Panel",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Opportunity Form
        Text(
            text = "Add Opportunity",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = organizationName,
            onValueChange = { organizationName = it },
            label = { Text("Organization Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = requiredSkills,
            onValueChange = { requiredSkills = it },
            label = { Text("Required Skills") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                val opportunity = mapOf(
                    "organizationName" to organizationName,
                    "location" to location,
                    "requiredSkills" to requiredSkills
                )
                firestore.collection("opportunities")
                    .add(opportunity)
                    .addOnSuccessListener {
                        organizationName = ""
                        location = ""
                        requiredSkills = ""
                        firestore.collection("opportunities").get().addOnSuccessListener { snapshot ->
                            opportunities = snapshot.documents.mapNotNull { document ->
                                document.toObject(Opportunity::class.java)?.copy(id = document.id)
                            }
                        }
                    }
                    .addOnFailureListener {
                        // Handle failure
                    }
                    .addOnCompleteListener {
                        isLoading = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Add Opportunity")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Opportunity List
        Text(
            text = "Manage Opportunities",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(opportunities) { opportunity ->
                OpportunityCard(
                    opportunity = opportunity,
                    onDelete = {
                        firestore.collection("opportunities").document(opportunity.id).delete()
                            .addOnSuccessListener {
                                opportunities = opportunities.filter { it.id != opportunity.id }
                            }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onDelete() }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Organization: ${opportunity.organizationName}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Location: ${opportunity.location}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Required Skills: ${opportunity.requiredSkills}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
