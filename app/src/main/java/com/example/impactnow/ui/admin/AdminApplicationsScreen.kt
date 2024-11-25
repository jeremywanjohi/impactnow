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

data class StudentApplication(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val email: String = "",
    val appliedOpportunityId: String = "",
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
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    applications = snapshot.documents.mapNotNull { doc ->
                        try {
                            doc.toObject(StudentApplication::class.java)?.copy(
                                id = doc.id,
                                applicationDate = when (val date = doc.get("applicationDate")) {
                                    is Timestamp -> date
                                    is String -> {
                                        // Parse the String into Timestamp if necessary
                                        Timestamp(java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(date))
                                    }
                                    else -> null
                                }
                            )
                        } catch (e: Exception) {
                            Log.e("AdminApplications", "Error parsing application data", e)
                            null
                        }
                    }
                    isLoading = false
                }
            }

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Applications") }
            )
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${application.studentName}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Email: ${application.email}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Applied Opportunity ID: ${application.appliedOpportunityId}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Status: ${application.status}", style = MaterialTheme.typography.bodyMedium)

            val formattedDate = application.applicationDate?.toDate()?.toString() ?: "N/A"
            Text(text = "Applied On: $formattedDate", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (application.status == "Pending") {
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onReject) {
                        Text("Reject", color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onAccept) {
                        Text("Accept", color = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Text(
                    text = "This application has been ${application.status}.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

suspend fun updateApplicationStatus(applicationId: String, newStatus: String) {
    val firestore = FirebaseFirestore.getInstance()
    firestore.collection("applications").document(applicationId)
        .update("status", newStatus)
        .addOnSuccessListener {
            // Optionally notify success
        }
        .addOnFailureListener { exception ->
            // Optionally handle failure
            Log.e("UpdateStatus", "Failed to update status", exception)
        }
}
