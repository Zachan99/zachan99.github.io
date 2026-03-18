package com.example.cs350inventoryappprojectzacharychan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity handles user authentication and registration.
 * It connects to the DatabaseHelper to verify login credentials or create new users.
 */
public class LoginActivity extends AppCompatActivity {

    // UI components for user input and buttons
    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;

    // Helper class to interact with SQLite database
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Bind UI elements from layout
        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        // Initialize the database helper
        databaseHelper = new DatabaseHelper(this);

        // Handle login button click
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate input
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if user exists in database
                boolean userExists = databaseHelper.checkUser(username, password);
                if (userExists) {
                    // Login successful
                    Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                    // Navigate to inventory screen
                    Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
                    startActivity(intent);
                    finish(); // Finish login activity so it's not in back stack
                } else {
                    // Invalid credentials
                    Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Handle register button click
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate input
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Try to insert new user
                boolean success = databaseHelper.insertUser(username, password);
                if (success) {
                    Toast.makeText(LoginActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    // Username already exists (due to UNIQUE constraint)
                    Toast.makeText(LoginActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
