package com.assignment.myecomapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentSuccess extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);

        // Set up UI components
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        // Set click listener for the "Back to Home" button
        btnBackToHome.setOnClickListener(v -> finish());
    }
}
