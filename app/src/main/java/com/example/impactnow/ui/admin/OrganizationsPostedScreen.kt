// File: com/example/impactnow/ui/admin/OrganizationsPostedScreen.kt

package com.example.impactnow.ui.admin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.impactnow.R
import com.example.impactnow.screens.EmptyStateView
import com.example.impactnow.screens.ErrorView
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

import com.google.firebase.Timestamp

data class Opportunity(
    val id: String = "",
    val organizationName: String = "",
    val title: String = "",
    val location: String = "",
    val requiredSkills: List<String> = emptyList(),
    val description: String = "",
    val imageUrl: String = "",
    val deadline: Timestamp? = null
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationsPostedScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var opportunities by remember { mutableStateOf(listOf<Opportunity>()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch opportunities from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("opportunities")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                val fetchedOpportunities = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Opportunity::class.java)?.copy(id = doc.id)
                }
                opportunities = fetchedOpportunities
                isLoading = false
            }
            .addOnFailureListener { exception ->
                isLoading = false
                errorMessage = "Failed to load opportunities: ${exception.message}"
                Log.e("OrganizationsPosted", "Error fetching opportunities", exception)
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organizations Posted") },
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    errorMessage != null -> {
                        ErrorView(message = errorMessage!!)
                    }
                    opportunities.isEmpty() -> {
                        EmptyStateView()
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(opportunities) { opportunity ->
                                OpportunityCard(opportunity = opportunity, onDelete = {
                                    coroutineScope.launch {
                                        firestore.collection("opportunities")
                                            .document(opportunity.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                opportunities = opportunities.filter { it.id != opportunity.id }
                                                Toast.makeText(context, "Opportunity deleted successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(context, "Failed to delete: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                Log.e("OrganizationsPosted", "Error deleting opportunity", exception)
                                            }
                                    }
                                })
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Optional: Handle card click if needed */ },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showDeleteDialog = true }
                    )
                }
        ) {
            // Display Image
            if (opportunity.imageUrl.isNotBlank()) {
                val painter = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(opportunity.imageUrl)
                        .crossfade(true)
                        .error(R.drawable.warning) // Placeholder on error
                        .placeholder(R.drawable.warning) // Placeholder while loading
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "${opportunity.organizationName} Logo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Organization Name
            Text(
                text = opportunity.organizationName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Title (if available)
            if (opportunity.title.isNotBlank()) {
                Text(
                    text = opportunity.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Location with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Work,
                    contentDescription = "Location Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = opportunity.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Required Skills with Icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Required Skills Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Skills: ${opportunity.requiredSkills.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Description
            if (opportunity.description.isNotBlank()) {
                Text(
                    text = opportunity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Deadline
            if (opportunity.deadline != null) {
                Text(
                    text = "Deadline: ${opportunity.deadline.toDate().toString().substring(0, 16)}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons (Optional: e.g., Edit)
            /*
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { /* Handle Edit */ },
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Edit")
                }
                Button(
                    onClick = { /* Handle Delete */ }
                ) {
                    Text("Delete")
                }
            }
            */
        }

        // Delete Confirmation Dialog
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
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
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
                contentDescription = "No Opportunities",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Opportunities Found",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "There are no posted opportunities at the moment.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }

    }
}
