package com.example.finalyearproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;

    private EditText emailEditText;
    private EditText passwordEditText;

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private void OpenSignUpActivity() {
        Intent intent = new Intent(this, CreateAccountActivity.class);
        startActivity(intent);
    }

    private void SuccessfullyLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // Return if currentUser is null
        if (currentUser == null) return;

        Intent intent = new Intent(getApplicationContext(), TaskActivity.class);
        startActivity(intent);
        finish();
    }

    private void CheckLoginDetails(Task<?> task) {
        if (!task.isSuccessful()) {
            Log.d(TaskActivity.TAG, "signInWithCredential:failure", task.getException());
            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
        }

        SuccessfullyLoggedIn();
        Log.d(TaskActivity.TAG, "signInWithEmailAndPassword:success");
        Toast.makeText(getApplicationContext(), "Logged in Successfully!", Toast.LENGTH_LONG).show();
    }

    private void SignInWithGoogleListener() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void SignInWithEmailListener() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        // Skip if Email and Password are empty
        if (email.length() == 0 || password.length() == 0) return;

        // Else Sign in with Email and Password
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this::CheckLoginDetails);
    }

    @Override
    public void onStart() {
        super.onStart();
        SuccessfullyLoggedIn();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle("");
        setContentView(R.layout.activity_login);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        SignInButton googleSignInButton = findViewById(R.id.googleSignInButton);
        TextView signUpTextView = findViewById(R.id.signUpTextView);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);

        // Opens Sign Up page Listener
        signUpTextView.setOnClickListener(v -> OpenSignUpActivity());
        // Sign in with Email and Password Listener
        loginButton.setOnClickListener(v -> SignInWithEmailListener());
        // Sign in with Google Listener
        googleSignInButton.setOnClickListener(v -> SignInWithGoogleListener());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                AuthCredential credential = GoogleAuthProvider.getCredential(Objects.requireNonNull(account).getIdToken(), null);
                mAuth.signInWithCredential(credential).addOnCompleteListener(this, this::CheckLoginDetails);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.d(TaskActivity.TAG, "Google sign in failed", e);
            }
        }
    }

    // Do nothing when Back Button pressed
    @Override
    public void onBackPressed() {
    }
}