package com.example.impactnow.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SavedOrganizationsScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var savedOrganizations by remember { mutableStateOf(listOf<Opportunity>()) }

    // Fetch saved organizations from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("savedOrganizations").get().addOnSuccessListener { snapshot ->
            savedOrganizations = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Opportunity::class.java)?.copy(id = doc.id)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Your Saved Organizations",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(savedOrganizations) { organization ->
                OpportunityCard(
                    opportunity = organization,
                    onApply = {
                        navController.navigate(Screen.Apply.createRoute(organization.id))
                    },
                    onSave = {}
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
