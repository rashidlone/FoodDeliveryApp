package com.assignment.myecomapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PaymentDialog {

    private static UserData currentUserData;
    private static DatabaseReference cartRef;

    public static void showPaymentDialog(Context context, String userAuthId, PaymentDialogListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.payment_dialog, null);

        final EditText editTextCardHolderName = view.findViewById(R.id.editTextCardHolderName);
        final EditText editTextCardNumber = view.findViewById(R.id.editTextCardNumber);
        final EditText editTextExpiryMonth = view.findViewById(R.id.editTextExpiryMonth);
        final EditText editTextExpiryYear = view.findViewById(R.id.editTextExpiryYear);
        final EditText editTextCVV = view.findViewById(R.id.editTextCVV);

        fetchUserData(userAuthId, userData -> {
            if (userData != null) {
                currentUserData = userData;
                String fullCardNumber = userData.getCardNumber();
                String maskedCardNumber = maskCardNumber(fullCardNumber);
                editTextCardNumber.setText(maskedCardNumber);
                editTextCardHolderName.setText(userData.getCardHolderName());
            }
        });

        builder.setView(view)
                .setTitle("Confirm Card Details to Buy:")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    String cardHolderName = editTextCardHolderName.getText().toString();
                    String cardNumber = editTextCardNumber.getText().toString();
                    String expiryMonth = editTextExpiryMonth.getText().toString();
                    String expiryYear = editTextExpiryYear.getText().toString();
                    String cvv = editTextCVV.getText().toString();

                    if (currentUserData != null) {

                        if(currentUserData.getExpiryMonth().equals(expiryMonth) && currentUserData.getExpiryYear().equals(expiryYear) && currentUserData.getCvv().equals(cvv))
                        {
                            paymentSuccess((Activity) context);

                        }else{
                            Toast.makeText(context, "Wrong details!", Toast.LENGTH_SHORT).show();
                        }

                    }


                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static void paymentSuccess(Activity context) {
        
        Toast.makeText(context, "Payment Successful!", Toast.LENGTH_SHORT).show();
        //empty cart when payment is successful
        cartRef.removeValue();
        Intent intent = new Intent(context, PaymentSuccess.class);
        context.startActivity(intent);
        context.finish();
    }

    private static void fetchUserData(String userAuthId, UserDataFetchListener listener) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Data/Users/" + userAuthId);
            cartRef = FirebaseDatabase.getInstance().getReference("Data/Users/" + userId + "/CartItems");


            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    UserData userData = dataSnapshot.getValue(UserData.class);
                    listener.onUserDataFetched(userData);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error
                }
            });
        }
    }

    private static String maskCardNumber(String fullCardNumber) {
        if (fullCardNumber != null && fullCardNumber.length() >= 10) {
            String maskedDigits = "******";
            return fullCardNumber.substring(0, 6) + maskedDigits + fullCardNumber.substring(fullCardNumber.length() - 4);
        } else {
            return fullCardNumber;
        }
    }

    public interface PaymentDialogListener {
    }

    public interface UserDataFetchListener {
        void onUserDataFetched(UserData userData);
    }
}
