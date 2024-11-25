// File: com/example/impactnow/screens/ApplicationsScreen.kt

package com.example.impactnow.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class Application(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val email: String = "",
    val appliedOpportunityId: String = "",
    val status: String = "Pending",
    val applicationDate: Timestamp? = null,
    val message: String = ""
)

@Composable
fun ApplicationsScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val context = LocalContext.current

    var applications by remember { mutableStateOf(listOf<Application>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            firestore.collection("applications")
                .whereEqualTo("studentId", currentUser.uid)
                .orderBy("applicationDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        errorMessage = "Failed to fetch applications: ${error.message}"
                        Log.e("ApplicationsScreen", "Error fetching applications", error)
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        applications = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Application::class.java)?.copy(id = doc.id)
                        }
                        isLoading = false
                    }
                }
        } else {
            isLoading = false
            errorMessage = "User not authenticated."
            Log.e("ApplicationsScreen", "User not authenticated.")
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Applications.route) { inclusive = true }
            }
        }
    }

    Scaffold(
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                    }
                    errorMessage != null -> {
                        ErrorView(message = errorMessage!!)
                    }
                    applications.isEmpty() -> {
                        EmptyStateView()
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(applications) { application ->
                                ApplicationCard(application = application, navController = navController)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ApplicationCard(application: Application, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("applicationDetails/${application.id}")
            },
        colors = CardDefaults.cardColors(
            containerColor = when (application.status) {
                "Pending" -> MaterialTheme.colorScheme.tertiaryContainer
                "Accepted" -> MaterialTheme.colorScheme.secondaryContainer
                "Rejected" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusIcon(status = application.status)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Opportunity ID: ${application.appliedOpportunityId}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Status: ${application.status}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Applied On: ${application.applicationDate?.toDate()?.toString()?.substring(0, 16) ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (application.message.isNotBlank()) {
                    Text(
                        text = application.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StatusIcon(status: String) {
    val icon = when (status) {
        "Pending" -> Icons.Default.Info
        "Accepted" -> Icons.Default.CheckCircle
        "Rejected" -> Icons.Default.Cancel
        else -> Icons.Default.Info
    }
    val iconTint = when (status) {
        "Pending" -> MaterialTheme.colorScheme.tertiary
        "Accepted" -> MaterialTheme.colorScheme.secondary
        "Rejected" -> MaterialTheme.colorScheme.error
        else -> Color.Gray
    }
    Icon(
        imageVector = icon,
        contentDescription = "$status Icon",
        tint = iconTint,
        modifier = Modifier.size(40.dp)
    )
}

@Composable
fun EmptyStateView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "No Applications",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Applications Found",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You have not applied to any opportunities yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Cancel,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
