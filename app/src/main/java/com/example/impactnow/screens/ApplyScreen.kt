// File: com/example/impactnow/screens/ApplyScreen.kt

@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.impactnow.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.tasks.await

@Composable
fun ApplyScreen(opportunityId: String?, navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch current user information
    val currentUser = auth.currentUser
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                // Fetch user details from 'users' collection
                val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
                if (userDoc.exists()) {
                    val firstName = userDoc.getString("firstName") ?: ""
                    val lastName = userDoc.getString("lastName") ?: ""
                    fullName = "$firstName $lastName"
                    email = userDoc.getString("email") ?: ""
                } else {
                    Toast.makeText(context, "User details not found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching user details: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User not authenticated.", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Login.route)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Apply for an Opportunity") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Apply for an Opportunity",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Full Name Field (Read-only)
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // Disable editing as it's fetched from user profile
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Email Field (Read-only)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false, // Disable editing as it's fetched from user profile
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Message Field (Optional)
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Message (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Submit and Cancel Buttons
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (opportunityId.isNullOrEmpty()) {
                                Toast.makeText(context, "Invalid Opportunity.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // Basic Validation
                            if (fullName.isBlank() || email.isBlank()) {
                                Toast.makeText(context, "Please ensure your name and email are filled.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isLoading = true

                            // Prepare application data
                            val applicationData = hashMapOf(
                                "studentId" to (currentUser?.uid ?: ""),
                                "studentName" to fullName,
                                "email" to email,
                                "appliedOpportunityId" to opportunityId,
                                "status" to "Pending", // Initial status
                                "applicationDate" to Timestamp.now(),
                                "message" to message
                            )

                            // Submit application to Firestore
                            firestore.collection("applications")
                                .add(applicationData)
                                .addOnSuccessListener {
                                    isLoading = false
                                    Toast.makeText(context, "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                                    navController.navigate(Screen.Applications.route) {
                                        popUpTo(Screen.Apply.route) { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    isLoading = false
                                    Toast.makeText(context, "Failed to submit application: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Submitting...")
                        } else {
                            Text("Submit")
                        }
                    }
                }
            }
        }
    }
}
