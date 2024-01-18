package com.assignment.myecomapp;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EditProfile extends AppCompatActivity {

    private EditText editTextName, editTextDOB, editTextPassword, editTextConfirmPassword,
            editTextPhone, editTextCardHolderName, editTextCardNumber,
            editTextExpiryMonth, editTextExpiryYear, editTextCVV;

    private TextView editTextEmail;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonMale, radioButtonFemale;
    private Button btnSaveChanges;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);

        // Initialize Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userRef = FirebaseDatabase.getInstance().getReference("Data/Users/" + userId);

            // Initialize UI components
            editTextName = findViewById(R.id.editTextName);
            editTextDOB = findViewById(R.id.editTextDOB);
            editTextDOB.setOnClickListener(v -> showDatePickerDialog());
            editTextPassword = findViewById(R.id.editTextPassword);
            editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
            editTextPhone = findViewById(R.id.editTextPhone);
            editTextEmail = findViewById(R.id.editTextEmail);
            radioGroupGender = findViewById(R.id.radioGroupGender);
            radioButtonMale = findViewById(R.id.radioButtonMale);
            radioButtonFemale = findViewById(R.id.radioButtonFemale);
            editTextCardHolderName = findViewById(R.id.editTextCardHolderName);
            editTextCardNumber = findViewById(R.id.editTextCardNumber);
            editTextExpiryMonth = findViewById(R.id.editTextExpiryMonth);
            editTextExpiryYear = findViewById(R.id.editTextExpiryYear);
            editTextCVV = findViewById(R.id.editTextCVV);
            btnSaveChanges = findViewById(R.id.btnSaveChanges);

            // Fetch user data from Firebase
            fetchUserData();

            // Set onClickListener for Save Changes button
            btnSaveChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    saveChanges();
                }
            });
        } else {
            finish();
        }
    }

    private void fetchUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                UserData userData = task.getResult().getValue(UserData.class);

                if (userData != null) {
                    // Populate UI with fetched data
                    editTextName.setText(userData.getName());
                    editTextDOB.setText(userData.getDob());
                    editTextPhone.setText(userData.getPhone());
                    editTextEmail.setText(userData.getEmail());

                    // Set gender radio button
                    if (userData.getGender().equals("Male")) {
                        radioButtonMale.setChecked(true);
                    } else if (userData.getGender().equals("Female")) {
                        radioButtonFemale.setChecked(true);
                    }

                    // Set payment details
                    editTextCardHolderName.setText(userData.getCardHolderName());
                    editTextCardNumber.setText(userData.getCardNumber());
                    editTextExpiryMonth.setText(userData.getExpiryMonth());
                    editTextExpiryYear.setText(userData.getExpiryYear());
                    editTextCVV.setText(userData.getCvv());
                }
            }
        });
    }

    private void saveChanges() {
        // Get data from UI fields
        String name = editTextName.getText().toString().trim();
        String dob = editTextDOB.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String cardHolderName = editTextCardHolderName.getText().toString().trim();
        String cardNumber = editTextCardNumber.getText().toString().trim();
        String expiryMonth = editTextExpiryMonth.getText().toString().trim();
        String expiryYear = editTextExpiryYear.getText().toString().trim();
        String cvv = editTextCVV.getText().toString().trim();

        // Validate data
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dob) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!TextUtils.isEmpty(password) || !TextUtils.isEmpty(confirmPassword)) {
            // Password fields are not empty, check for password match
            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Get selected gender
        String gender = "";
        int selectedRadioButtonId = radioGroupGender.getCheckedRadioButtonId();
        if (selectedRadioButtonId == R.id.radioButtonMale) {
            gender = "Male";
        } else if (selectedRadioButtonId == R.id.radioButtonFemale) {
            gender = "Female";
        }

        // Create UserData object
        UserData userData = new UserData(name, dob, gender, phone, email,
                cardHolderName, cardNumber, expiryMonth, expiryYear, cvv);

        // Update user data in Firebase
        userRef.setValue(userData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                    }
                });
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
