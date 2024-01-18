package com.assignment.myecomapp;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword, editTextRetypePassword,
            editTextFullName, editTextDOB, editTextPhone,
            editTextCardHolderName, editTextCardNumber, editTextExpiryMonth,
            editTextExpiryYear, editTextCVV;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button btnRegister;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersRef;

    // Progress Dialog
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database reference
        usersRef = FirebaseDatabase.getInstance().getReference("Data/Users");

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextRetypePassword = findViewById(R.id.editTextRetypePassword);
        editTextFullName = findViewById(R.id.editTextFullName);
        editTextDOB = findViewById(R.id.editTextDOB);
        editTextPhone = findViewById(R.id.editTextPhone);
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioButtonMale = findViewById(R.id.radioButtonMale);
        radioButtonFemale = findViewById(R.id.radioButtonFemale);
        editTextCardHolderName = findViewById(R.id.editTextCardHolderName);
        editTextCardNumber = findViewById(R.id.editTextCardNumber);
        editTextExpiryMonth = findViewById(R.id.editTextExpiryMonth);
        editTextExpiryYear = findViewById(R.id.editTextExpiryYear);
        editTextCVV = findViewById(R.id.editTextCVV);
        btnRegister = findViewById(R.id.btnRegister);

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Registering");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        // Set onClickListener for the register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        // Set onClickListener for the DOB field
        editTextDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
    }

    private void registerUser() {
        String email = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String retypePassword = editTextRetypePassword.getText().toString().trim();
        String fullName = editTextFullName.getText().toString().trim();
        String dob = editTextDOB.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String cardHolderName = editTextCardHolderName.getText().toString().trim();
        String cardNumber = editTextCardNumber.getText().toString().trim();
        String expiryMonth = editTextExpiryMonth.getText().toString().trim();
        String expiryYear = editTextExpiryYear.getText().toString().trim();
        String cvv = editTextCVV.getText().toString().trim();

        int selectedGenderId = radioGroupGender.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = (selectedGender != null) ? selectedGender.getText().toString() : "";

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) ||
                TextUtils.isEmpty(retypePassword) || TextUtils.isEmpty(fullName) ||
                TextUtils.isEmpty(dob) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(cardHolderName) || TextUtils.isEmpty(cardNumber) ||
                TextUtils.isEmpty(expiryMonth) || TextUtils.isEmpty(expiryYear) || TextUtils.isEmpty(cvv) ||
                TextUtils.isEmpty(gender)) {
            Toast.makeText(this, "All fields must be filled", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(retypePassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show ProgressDialog
        progressDialog.show();

        // Perform registration using Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Dismiss ProgressDialog
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Registration successful
                            // Save user data to Firebase Realtime Database
                            saveUserDataToDatabase(email, fullName, dob, phone, gender,
                                    cardHolderName, cardNumber, expiryMonth, expiryYear, cvv);

                            Toast.makeText(RegisterActivity.this, "Registration Successful! You can login now.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // Registration failed
                            Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveUserDataToDatabase(String email, String fullName, String dob, String phone, String gender,
                                        String cardHolderName, String cardNumber, String expiryMonth, String expiryYear, String cvv) {
        String userId = firebaseAuth.getCurrentUser().getUid();

        // Create a map to store user data
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("fullName", fullName);
        userData.put("dob", dob);
        userData.put("phone", phone);
        userData.put("gender", gender);
        userData.put("cardHolderName", cardHolderName);
        userData.put("cardNumber", cardNumber);
        userData.put("expiryMonth", expiryMonth);
        userData.put("expiryYear", expiryYear);
        userData.put("cvv", cvv);

        // Save user data to the database under "Data/Users/userId"
        usersRef.child(userId).setValue(userData);
    }

    private void showDatePickerDialog() {
        // Get current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDayOfMonth) {
                        // Set the selected date to the EditText
                        editTextDOB.setText(selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDayOfMonth);
                    }
                }, year, month, day);

        // Show the DatePickerDialog
        datePickerDialog.show();
    }
}
