package com.example.impactnow.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.impactnow.ui.navigation.Screen

data class Opportunity(
    val id: String = "",
    val organizationName: String = "",
    val location: String = "",
    val requiredSkills: String = ""
)

@Composable
fun StudentHomeScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var opportunities by remember { mutableStateOf(listOf<Opportunity>()) }

    // Fetch opportunities from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("opportunities").get().addOnSuccessListener { snapshot ->
            opportunities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Opportunity::class.java)?.copy(id = doc.id)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Available Opportunities",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(opportunities) { opportunity ->
                OpportunityCard(
                    opportunity = opportunity,
                    onApply = {
                        navController.navigate(Screen.Apply.createRoute(opportunity.id))
                    },
                    onSave = {
                        firestore.collection("savedOrganizations")
                            .add(opportunity)
                            .addOnSuccessListener {
                                // Display save success message
                            }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onApply: () -> Unit, onSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Organization: ${opportunity.organizationName}")
            Text(text = "Location: ${opportunity.location}")
            Text(text = "Required Skills: ${opportunity.requiredSkills}")
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = onApply) {
                    Text("Apply")
                }
                Button(onClick = onSave) {
                    Text("Save")
                }
            }
        }
    }
}
