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
    val id: String = "",
    val organizationName: String = "",
    val location: String = "",
    val requiredSkills: String = ""
)

@Composable
fun OrganizationsPostedScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var opportunities by remember { mutableStateOf(listOf<Opportunity>()) }

    LaunchedEffect(Unit) {
        firestore.collection("opportunities").get().addOnSuccessListener { snapshot ->
            opportunities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Opportunity::class.java)?.copy(id = doc.id)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Organizations Posted",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(opportunities) { opportunity ->
                OpportunityCard(opportunity = opportunity, onDelete = {
                    firestore.collection("opportunities").document(opportunity.id).delete()
                        .addOnSuccessListener {
                            opportunities = opportunities.filter { it.id != opportunity.id }
                        }
                })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { showDeleteDialog = true }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Organization: ${opportunity.organizationName}")
            Text(text = "Location: ${opportunity.location}")
            Text(text = "Required Skills: ${opportunity.requiredSkills}")

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Confirmation") },
                    text = { Text("Are you sure you want to delete this opportunity?") },
                    confirmButton = {
                        TextButton(onClick = {
                            onDelete()
                            showDeleteDialog = false
                        }) {
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
