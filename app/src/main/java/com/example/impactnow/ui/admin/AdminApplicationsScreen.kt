package com.example.impactnow.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class StudentApplication(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val email: String = "",
    val appliedOpportunityId: String = "",
    val organizationName: String = "", // New field for organization name
    val status: String = "Pending",
    val applicationDate: Timestamp? = null // Keep as Timestamp for consistent handling
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApplicationsScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var applications by remember { mutableStateOf(listOf<StudentApplication>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Listener to fetch real-time updates
    LaunchedEffect(Unit) {
        val listener: ListenerRegistration = firestore.collection("applications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    errorMessage = "Failed to fetch applications: ${error.message}"
                    Log.e("AdminApplications", "Firestore error", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    // Fetch organization names in bulk to optimize performance
                    val opportunityIds = snapshot.documents.mapNotNull { it.getString("appliedOpportunityId") }.distinct()
                    if (opportunityIds.isNotEmpty()) {
                        firestore.collection("opportunities")
                            .whereIn("id", opportunityIds.take(10)) // Firestore 'in' queries limit to 10 per request
                            .get()
                            .addOnSuccessListener { opportunitiesSnapshot ->
                                val opportunityMap = opportunitiesSnapshot.documents.associateBy(
                                    { it.id },
                                    { it.getString("organizationName") ?: "Unknown" }
                                )

                                applications = snapshot.documents.mapNotNull { doc ->
                                    try {
                                        val application = doc.toObject(StudentApplication::class.java)?.copy(
                                            id = doc.id,
                                            applicationDate = doc.getTimestamp("applicationDate")
                                        )
                                        application?.copy(
                                            organizationName = opportunityMap[application.appliedOpportunityId] ?: "Unknown"
                                        )
                                    } catch (e: Exception) {
                                        Log.e("AdminApplications", "Error parsing application data", e)
                                        null
                                    }
                                }
                                isLoading = false
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                errorMessage = "Failed to fetch opportunities: ${e.message}"
                                Log.e("AdminApplications", "Failed to fetch opportunities", e)
                            }
                    } else {
                        applications = emptyList()
                        isLoading = false
                    }
                }
            }
    }

    Scaffold(
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                errorMessage != null -> Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                applications.isEmpty() -> Text(
                    text = "No applications found.",
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(applications) { application ->
                        ApplicationItem(
                            application = application,
                            onAccept = {
                                coroutineScope.launch {
                                    updateApplicationStatus(application.id, "Accepted")
                                }
                            },
                            onReject = {
                                coroutineScope.launch {
                                    updateApplicationStatus(application.id, "Rejected")
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationItem(
    application: StudentApplication,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Student Name and Organization Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = application.studentName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = application.organizationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Email
            Text(
                text = "Email: ${application.email}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Opportunity ID
            Text(
                text = "Opportunity ID: ${application.appliedOpportunityId}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Application Date
            val formattedDate = application.applicationDate?.toDate()?.toString() ?: "N/A"
            Text(
                text = "Applied On: $formattedDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Status and Actions
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (application.status) {
                    "Pending" -> {
                        TextButton(onClick = onReject) {
                            Text("Reject", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = onAccept) {
                            Text("Accept", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    "Accepted" -> {
                        Text(
                            text = "Accepted",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    "Rejected" -> {
                        Text(
                            text = "Rejected",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    else -> {
                        Text(
                            text = application.status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

suspend fun updateApplicationStatus(applicationId: String, newStatus: String) {
    val firestore = FirebaseFirestore.getInstance()
    try {
        firestore.collection("applications").document(applicationId)
            .update("status", newStatus)
            .await() // Using Kotlin Coroutines extension
        // Optionally notify success, e.g., via Snackbar or Toast
    } catch (e: Exception) {
        // Handle failure, e.g., log error or notify admin
        Log.e("UpdateStatus", "Failed to update status", e)
    }
}