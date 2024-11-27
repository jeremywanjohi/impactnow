package com.example.impactnow.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.example.impactnow.ui.navigation.Screen
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBarDefaults
import com.example.impactnow.ui.navigation.Screen.AdminApplications.route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostOpportunityScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // State variables for form fields
    var organizationName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var requiredSkills by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // State for validation errors
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Opportunity") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Screen Title
                    Text(
                        text = "Post an Internship Opportunity",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Organization Name Field
                    OutlinedTextField(
                        value = organizationName,
                        onValueChange = { organizationName = it },
                        label = { Text("Organization Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Location Field
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = true
                    )

                    // Required Skills Field
                    OutlinedTextField(
                        value = requiredSkills,
                        onValueChange = { requiredSkills = it },
                        label = { Text("Required Skills (Comma Separated)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = false,
                        maxLines = 3
                    )

                    // Description Field
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        singleLine = false,
                        maxLines = 5
                    )

                    // Image URL Field
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL (Optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        singleLine = true
                    )

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Cancel")
                        }
                        Button(
                            onClick = {
                                // Input Validation
                                when {
                                    organizationName.isBlank() -> {
                                        errorMessage = "Organization Name is required."
                                    }
                                    location.isBlank() -> {
                                        errorMessage = "Location is required."
                                    }
                                    requiredSkills.isBlank() -> {
                                        errorMessage = "At least one Required Skill is required."
                                    }
                                    description.isBlank() -> {
                                        errorMessage = "Description is required."
                                    }
                                    else -> {
                                        // All validations passed
                                        isLoading = true
                                        errorMessage = null

                                        // Prepare data
                                        val opportunityData = mapOf(
                                            "organizationName" to organizationName.trim(),
                                            "location" to location.trim(),
                                            "requiredSkills" to requiredSkills.split(",").map { it.trim() },
                                            "description" to description.trim(),
                                            "imageUrl" to imageUrl.trim(),
                                            "createdAt" to com.google.firebase.Timestamp.now()
                                        )

                                        // Submit to Firestore
                                        firestore.collection("opportunities")
                                            .add(opportunityData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(context, "Opportunity posted successfully!", Toast.LENGTH_SHORT).show()
                                                navController.navigate(Screen.AdminPosted.route) {
                                                    popUpTo(Screen.AdminPosted.route) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                isLoading = false
                                                Toast.makeText(context, "Failed to post opportunity: ${exception.message}", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                }

                                // Show error message if any
                                errorMessage?.let { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Posting...")
                            } else {
                                Text(text = "Post")
                            }
                        }
                    }
                }
            }
        }
    )
}
