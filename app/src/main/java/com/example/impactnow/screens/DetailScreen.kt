// File: app/src/main/java/com/example/impactnow/ui/DetailScreen.kt

package com.example.impactnow.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen

@Composable
fun DetailScreen(opportunityId: String?, navController: NavHostController) {
    // Dummy data retrieval based on opportunityId
    val opportunity = remember(opportunityId) {
        Opportunity(
            id = "1",
            roleDescription = "Community Clean-Up",
            location = "New York",
            requiredSkills = listOf("Teamwork", "Physical Fitness"),
            organizationName = "Green Earth Org"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = opportunity.roleDescription,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Organization: ${opportunity.organizationName}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Location: ${opportunity.location}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Required Skills: ${opportunity.requiredSkills.joinToString(", ")}",
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate(Screen.Apply.createRoute(opportunity.id)) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Apply Now", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}
