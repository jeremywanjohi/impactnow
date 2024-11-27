package com.example.impactnow.screens

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.filled.ExitToApp

// File: com/example/impactnow/screens/ProfileScreen.kt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val logoutButtonColor = Color.Red // Distinct color for Logout button


    var interests by remember { mutableStateOf("") }
    var skills by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var timeCommitment by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currentUser = auth.currentUser

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            try {
                // Fetch profile data from 'profiles' collection using user UID
                val profileDoc = firestore.collection("profiles").document(currentUser.uid).get().await()
                if (profileDoc.exists()) {
                    interests = profileDoc.getString("interests") ?: ""
                    skills = profileDoc.getString("skills") ?: ""
                    location = profileDoc.getString("location") ?: ""
                    timeCommitment = profileDoc.getString("timeCommitment") ?: ""
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Error fetching profile data", e)
                Toast.makeText(context, "Error fetching profile data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "User not authenticated.", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Login.route)
        }
    }

    // Define the color palette
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(backgroundColor)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Your Profile",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Interests Field
                OutlinedTextField(
                    value = interests,
                    onValueChange = { interests = it },
                    label = { Text("Interests") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Interests") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Skills Field
                OutlinedTextField(
                    value = skills,
                    onValueChange = { skills = it },
                    label = { Text("Skills") },
                    leadingIcon = { Icon(Icons.Default.Build, contentDescription = "Skills") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Location Field
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Time Commitment Field
                OutlinedTextField(
                    value = timeCommitment,
                    onValueChange = { timeCommitment = it },
                    label = { Text("Preferred Time Commitment") },
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = "Time Commitment") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Save Profile Button
                Button(
                    onClick = {
                        coroutineScope.launch {
                            if (currentUser != null) {
                                saveProfile(
                                    firestore = firestore,
                                    userId = currentUser.uid,
                                    interests = interests,
                                    skills = skills,
                                    location = location,
                                    timeCommitment = timeCommitment,
                                    context = context,
                                    navController = navController
                                )
                            }
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save Profile")
                    }
                }


            }
        }
    }

    // Function to save profile data
    suspend fun saveProfile(
        firestore: FirebaseFirestore,
        userId: String,
        interests: String,
        skills: String,
        location: String,
        timeCommitment: String,
        context: android.content.Context,
        navController: NavHostController
    ) {
        try {
            firestore.collection("profiles").document(userId)
                .set(
                    mapOf(
                        "interests" to interests,
                        "skills" to skills,
                        "location" to location,
                        "timeCommitment" to timeCommitment
                    )
                )
                .await()
            Toast.makeText(context, "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            navController.navigate(Screen.Home.route) { // Navigate to Home or desired screen
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error saving profile", e)
            Toast.makeText(context, "Failed to save profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

fun saveProfile(firestore: FirebaseFirestore, userId: String, interests: String, skills: String, location: String, timeCommitment: String, context: Context, navController: NavHostController) {

}
