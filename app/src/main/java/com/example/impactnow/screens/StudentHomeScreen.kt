// File: com/example/impactnow/screens/StudentHomeScreen.kt

package com.example.impactnow.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Work
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.impactnow.ui.navigation.Screen
import com.example.impactnow.R
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Ensure the Opportunity data class is correctly imported or defined
// File: com/example/impactnow/screens/Opportunity.kt

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
fun StudentHomeScreen(navController: NavHostController) {
    val firestore = FirebaseFirestore.getInstance()
    var opportunities by remember { mutableStateOf(listOf<Opportunity>()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Snackbar Host State
    val snackbarHostState = remember { SnackbarHostState() }

    // Coroutine Scope for Snackbar
    val coroutineScope = rememberCoroutineScope()

    // Fetch opportunities from Firestore
    LaunchedEffect(Unit) {
        try {
            val snapshot = firestore.collection("opportunities").get().await()
            opportunities = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Opportunity::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Failed to load opportunities: ${e.message}")
            }
            Log.e("StudentHomeScreen", "Error fetching opportunities", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                    opportunities.isEmpty() -> {
                        Text(
                            text = "No opportunities available at the moment.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(opportunities) { opportunity ->
                                OpportunityCard(
                                    opportunity = opportunity,
                                    onApply = {
                                        // Navigate to ApplyScreen with the opportunity ID
                                        navController.navigate(Screen.Apply.createRoute(opportunity.id))
                                    },
                                    onSave = {
                                        firestore.collection("savedOrganizations")
                                            .add(
                                                mapOf(
                                                    "organizationName" to opportunity.organizationName,
                                                    "location" to opportunity.location,
                                                    "requiredSkills" to opportunity.requiredSkills,
                                                    "description" to opportunity.description,
                                                    "imageUrl" to opportunity.imageUrl,
                                                    "deadline" to opportunity.deadline
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(context, "Failed to save: ${exception.message}", Toast.LENGTH_SHORT).show()
                                                Log.e("StudentHomeScreen", "Error saving opportunity", exception)
                                            }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunityCard(
    opportunity: Opportunity,
    onApply: () -> Unit,
    onSave: () -> Unit
) {
    // State to control dialog visibility
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            // Organization Name
            Text(
                text = opportunity.organizationName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))

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
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Skills Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = opportunity.requiredSkills.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onSave,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Save Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
                }
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Apply Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply")
                }
            }

            // Popup Dialog for Opportunity Details
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            showDialog = false
                            onApply()
                        }) {
                            Text("Apply")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    },
                    title = {
                        Text(text = opportunity.title)
                    },
                    text = {
                        Column {
                            // Opportunity Image
                            if (opportunity.imageUrl.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(opportunity.imageUrl),
                                    contentDescription = "Opportunity Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .padding(bottom = 8.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {

                            }
                            // Opportunity Description
                            Text(
                                text = opportunity.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                )
            }
        }
    }
}
