package com.example.impactnow.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.impactnow.ui.navigation.Screen

@Composable
fun PostOpportunityScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var organizationName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredSkills by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Post an Internship Opportunity",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = { navController.popBackStack() }) {
                Text(text = "Cancel")
            }
            Button(
                onClick = {
                    isLoading = true
                    firestore.collection("opportunities").add(
                        mapOf(
                            "organizationName" to organizationName,
                            "location" to location,
                            "requiredSkills" to requiredSkills
                        )
                    ).addOnSuccessListener {
                        isLoading = false
                        navController.navigate(Screen.AdminPosted.route) // Navigate to the posted opportunities screen
                    }.addOnFailureListener {
                        isLoading = false
                    }
                },
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text(text = "Post")
                }
            }
        }
    }
}
