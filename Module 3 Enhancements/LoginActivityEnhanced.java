package com.example.cs350inventoryappprojectzacharychan;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * LoginActivity handles user authentication and registration.
 * Enhancement: improved validation, cleaner method structure, and clearer feedback.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton, registerButton;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.editTextUsername);
        passwordEditText = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);

        databaseHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(v -> handleLogin());
        registerButton.setOnClickListener(v -> handleRegistration());
    }

    /**
     * Enhancement: extracted login logic into a separate method
     * to improve readability and maintainability.
     */
    private void handleLogin() {
        String username = getTrimmedText(usernameEditText);
        String password = getTrimmedText(passwordEditText);

        if (!validateCredentials(username, password)) {
            return;
        }

        boolean userExists = databaseHelper.checkUser(username, password);
        if (userExists) {
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enhancement: extracted registration logic into a separate method
     * for better software design and reuse of validation logic.
     */
    private void handleRegistration() {
        String username = getTrimmedText(usernameEditText);
        String password = getTrimmedText(passwordEditText);

        if (!validateCredentials(username, password)) {
            return;
        }

        boolean success = databaseHelper.insertUser(username, password);
        if (success) {
            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
            clearInputFields();
        } else {
            Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Enhancement: centralized input retrieval.
     */
    private String getTrimmedText(EditText editText) {
        return editText.getText().toString().trim();
    }

    /**
     * Enhancement: centralized validation improves maintainability
     * and prevents duplicated code.
     */
    private boolean validateCredentials(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * Enhancement: clears fields after successful registration.
     */
    private void clearInputFields() {
        usernameEditText.setText("");
        passwordEditText.setText("");
    }
}
