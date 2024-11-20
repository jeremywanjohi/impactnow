package com.example.impactnow.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SignupScreen(navController: NavHostController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Register Here",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))

        // First Name Input
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Last Name Input
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Email Input
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() }, // Trim spaces
            label = { Text("Enter Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password Input
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password Input
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Re-enter Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Register Button
        Button(
            onClick = {
                val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+" // Regex for email validation

                when {
                    email.isEmpty() || !email.matches(emailPattern.toRegex()) -> {
                        Toast.makeText(
                            context,
                            "Please enter a valid email address.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    password != confirmPassword -> {
                        Toast.makeText(
                            context,
                            "Passwords do not match.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    val user = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "email" to email,
                                        "role" to "Student" // Default role is set to "Student"
                                    )
                                    if (userId != null) {
                                        firestore.collection("users")
                                            .document(userId)
                                            .set(user)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    context,
                                                    "Registration successful!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                navController.navigate("home")
                                            }
                                            .addOnFailureListener { exception ->
                                                Toast.makeText(
                                                    context,
                                                    "Error saving user details: ${exception.localizedMessage}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    val errorMessage = when (task.exception) {
                                        is FirebaseAuthInvalidCredentialsException -> "Invalid email format."
                                        is FirebaseAuthUserCollisionException -> "This email is already registered."
                                        else -> task.exception?.localizedMessage ?: "Signup failed."
                                    }
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "REGISTER")
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Redirect to Login Screen
        TextButton(onClick = { navController.navigate("login") }) {
            Text(text = "Already have an account? Login")
        }
    }
}
