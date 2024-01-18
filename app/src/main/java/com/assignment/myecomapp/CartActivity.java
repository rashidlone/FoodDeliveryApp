package com.assignment.myecomapp;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements PaymentDialog.PaymentDialogListener{

    private ListView cartListView;
    private static List<CartItem> cartItemList;
    private CartItemAdapter cartAdapter;
    private static DatabaseReference userCartRef;
    private static Button checkout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Initialize UI components
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        cartListView = findViewById(R.id.cartListView);
        checkout = findViewById(R.id.checkout);
        checkout.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SuspiciousIndentation")
            @Override
            public void onClick(View v) {

                if(getTotalPrice() <= 0.0)
                    Toast.makeText(CartActivity.this, "Add items first!", Toast.LENGTH_SHORT).show();
                else
                PaymentDialog.showPaymentDialog(CartActivity.this, currentUser.getUid(),CartActivity.this);
            }
        });

        // Initialize cart item list
        cartItemList = new ArrayList<>();
        cartAdapter = new CartItemAdapter(this, R.layout.list_item_cart, cartItemList);
        cartListView.setAdapter(cartAdapter);

        // Fetch cart items from Firebase

        if (currentUser != null) {
            String userId = currentUser.getUid();
            userCartRef = FirebaseDatabase.getInstance().getReference("Data/Users/" + userId + "/CartItems");

            userCartRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    cartItemList.clear();

                    for (DataSnapshot cartItemSnapshot : dataSnapshot.getChildren()) {
                        CartItem cartItem = cartItemSnapshot.getValue(CartItem.class);

                        if (cartItem != null) {
                            cartItemList.add(cartItem);
                        }
                    }

                    // Update the list view
                    cartAdapter.notifyDataSetChanged();
                    cartAdapter.updateTotalPrice();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors
                }
            });
        }
    }

    private static class CartItemAdapter extends ArrayAdapter<CartItem> {

        public CartItemAdapter(CartActivity context, int resource, List<CartItem> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_item_cart, null);
            }

            CartItem cartItem = getItem(position);
            if (cartItem != null) {
                ImageView productImage = convertView.findViewById(R.id.imageProduct);
                TextView productName = convertView.findViewById(R.id.textProductName);
                TextView productPrice = convertView.findViewById(R.id.textProductPrice);
                TextView productQuantity = convertView.findViewById(R.id.textProductQuantity);
                Button deleteButton = convertView.findViewById(R.id.btnDelete);

                productName.setText(cartItem.getItemName());
                productPrice.setText(String.format("$%s", cartItem.getItemPrice()));
                productQuantity.setText(String.format("Quantity: %s", cartItem.getQuantity()));

                deleteButton.setOnClickListener(v -> {
                    // Remove the cart item from Firebase
                    userCartRef.child(cartItem.getItemId()).removeValue()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Item removed successfully from Firebase, now update the list
                                    cartItemList.remove(cartItem);
                                    notifyDataSetChanged();
                                    updateTotalPrice();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Handle failure
                                    Toast.makeText(getContext(), "Failed to delete item. Please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }

            return convertView;
        }

        // Method to update the total price display
        private void updateTotalPrice() {
                checkout.setText(String.format("Checkout: ($%.2f)", getTotalPrice()));
            }
    }

    // Method to get the total sum of all cart items
    private static double getTotalPrice() {
        double totalPrice = 0;

        for (CartItem cartItem : cartItemList) {
            totalPrice += Double.parseDouble(String.valueOf(cartItem.getItemPrice())) * cartItem.getQuantity();
        }

        return totalPrice;
    }
}
