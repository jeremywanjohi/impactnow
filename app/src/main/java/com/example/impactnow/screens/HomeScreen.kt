
package com.example.impactnow.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.example.impactnow.ui.theme.LightGreen200

data class Opportunity(
    val id: String,
    val roleDescription: String,
    val location: String,
    val requiredSkills: List<String>,
    val organizationName: String
)

@Composable
fun HomeScreen(navController: NavHostController) {
    val opportunities = listOf(
        Opportunity(
            id = "1",
            roleDescription = "Community Clean-Up",
            location = "New York",
            requiredSkills = listOf("Teamwork", "Physical Fitness"),
            organizationName = "Green Earth Org"
        ),
        Opportunity(
            id = "2",
            roleDescription = "Tutoring Underprivileged Kids",
            location = "Los Angeles",
            requiredSkills = listOf("Communication", "Patience"),
            organizationName = "Education First"
        ),
        // Add more dummy opportunities as needed
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        items(opportunities) { opportunity ->
            OpportunityCard(opportunity = opportunity) {
                navController.navigate(Screen.Detail.createRoute(opportunity.id))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OpportunityCard(opportunity: Opportunity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = opportunity.roleDescription,
                style = MaterialTheme.typography.titleMedium,
                color = LightGreen200
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Organization: ${opportunity.organizationName}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Location: ${opportunity.location}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Required Skills: ${opportunity.requiredSkills.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}