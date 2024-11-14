// File: app/src/main/java/com/example/impactnow/ui/MockData.kt

package com.example.impactnow.ui

data class Opportunity(
    val id: String,
    val roleDescription: String,
    val location: String,
    val requiredSkills: List<String>,
    val organizationName: String
)
