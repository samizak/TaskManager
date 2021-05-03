package com.example.finalyearproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

/**
 * The Activity for Creating a new Account
 */
public class CreateAccountActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;

    private FirebaseAuth mAuth;

    /**
     * Opens the tasks activity when successfully logged in
     */
    private void SuccessfullyLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Return if currentUser is null
        if (currentUser == null) return;

        Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Checks the login details
     * @param task A Firebase task
     */
    private void CheckLoginDetails(Task<?> task) {
        if (!task.isSuccessful()) {
            Log.d(TaskActivity.TAG, "signInWithCredential:failure", task.getException());
            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
        }

        SuccessfullyLoggedIn();
        Log.d(TaskActivity.TAG, "signInWithEmailAndPassword:success");
        Toast.makeText(getApplicationContext(), "Account Created!", Toast.LENGTH_LONG).show();
    }

    /**
     * Create new Account Button Listener
     */
    private void CreateAccountButtonListener() {
        boolean isEmailValid = IsValidEmail(emailEditText.getText());
        boolean isPasswordValid = IsPasswordValid(passwordEditText.getText());

        if (!isEmailValid) {
            emailEditText.setError("Invalid Email Address!");
            Toast.makeText(getApplicationContext(), "Invalid Email Address!", Toast.LENGTH_LONG).show();
        }

        if (!isPasswordValid) {
            passwordEditText.setError("Invalid Password!");
            Toast.makeText(getApplicationContext(), "Password must be greater than 5 characters!", Toast.LENGTH_LONG).show();
        }

        // If both email and password are valid
        if (isEmailValid && isPasswordValid) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this::CheckLoginDetails);
        }
    }

    /**
     * Checks if the Email is in a Valid format
     * @param email The Email
     * @return true if the Email is in a valid format
     */
    private boolean IsValidEmail(CharSequence email) {
        return (email.toString() != null && !email.toString().trim().isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    /**
     * Checks if the Password is valid
     * @param password The Password
     * @return true if the password is in a valid format
     */
    private boolean IsPasswordValid(CharSequence password) {
        return password.length() > 5;
    }

    @Override
    public void onStart() {
        super.onStart();
        SuccessfullyLoggedIn();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("");
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button createAccountButton = findViewById(R.id.createAccountButton);

        // Create a new account Listener
        createAccountButton.setOnClickListener(v -> CreateAccountButtonListener());
    }
}