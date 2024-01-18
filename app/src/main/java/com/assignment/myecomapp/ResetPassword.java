package com.assignment.myecomapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class ResetPassword extends AppCompatActivity {

    private EditText emailEditText;
    private Button resetPasswordButton;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        emailEditText = findViewById(R.id.editTextEmail);
        resetPasswordButton = findViewById(R.id.btnResetPassword);

        resetPasswordButton.setOnClickListener(view -> checkUserAndResetPassword());
    }

    private void checkUserAndResetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter your registered email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if the user exists
        firebaseAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            // User exists, send password reset email
                            sendPasswordResetEmail(email);
                        } else {
                            // User does not exist
                            Toast.makeText(ResetPassword.this, "No account found with this email address", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Error occurred while checking user existence
                        Toast.makeText(ResetPassword.this, "Failed to check user existence. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPasswordResetEmail(String email) {
        // Send password reset email
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ResetPassword.this, "Password reset email sent. Check your email.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetPassword.this, "Failed to send password reset email. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
