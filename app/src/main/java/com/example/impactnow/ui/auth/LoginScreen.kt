// File: com/example/impactnow/ui/auth/LoginScreen.kt

package com.example.impactnow.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.impactnow.ui.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavHostController) {
    // State variables for form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    // Regular expression pattern for email validation
    val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "IMPACTNOW",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Green
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() }, // Trim spaces
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.trim() }, // Trim spaces
            label = { Text("Enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                // Input Validation
                when {
                    email.isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Please enter your email.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    !email.matches(emailPattern.toRegex()) -> {
                        Toast.makeText(
                            context,
                            "Please enter a valid email address.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    password.isEmpty() -> {
                        Toast.makeText(
                            context,
                            "Please enter your password.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        // Proceed with Firebase Authentication
                        isLoading = true
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        firestore.collection("users")
                                            .document(userId)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                val role = document.getString("role")
                                                android.util.Log.d("LoginDebug", "Role fetched: $role")
                                                when (role) {
                                                    "Admin", "Student" -> {
                                                        // Navigate to mainFlow instead of directly to role-specific screens
                                                        navController.navigate("mainFlow") {
                                                            // Clear the back stack to prevent navigating back to the login screen
                                                            popUpTo("login") { inclusive = true }
                                                        }
                                                    }
                                                    null -> {
                                                        Toast.makeText(
                                                            context,
                                                            "Role not set. Please contact support.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    else -> {
                                                        android.util.Log.e("LoginDebug", "Unknown role: $role")
                                                        Toast.makeText(
                                                            context,
                                                            "Unknown role: $role. Please contact support.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                android.util.Log.e("LoginDebug", "Error fetching user details: ${exception.localizedMessage}")
                                                Toast.makeText(
                                                    context,
                                                    "Error fetching user details: ${exception.localizedMessage}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    } else {
                                        android.util.Log.e("LoginDebug", "Authentication failed, userId is null.")
                                        Toast.makeText(
                                            context,
                                            "Authentication failed. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    // Handle different types of authentication exceptions
                                    val errorMessage = when (task.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email or password."
                                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                                        else -> task.exception?.localizedMessage ?: "Login failed."
                                    }
                                    android.util.Log.e("LoginDebug", "Authentication error: $errorMessage")
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White
                )
            } else {
                Text(text = "SIGN IN")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // **New:** Sign-Up Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account? ")
            TextButton(onClick = {
                navController.navigate("signup") {
                    // Optional: Clear back stack to prevent returning to login via back button
                    popUpTo("login") { inclusive = false }
                }
            }) {
                Text(text = "Sign Up")
            }
        }
    }
}
