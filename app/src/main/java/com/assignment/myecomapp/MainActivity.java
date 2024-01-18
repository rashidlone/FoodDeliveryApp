package com.assignment.myecomapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Product> productsList;
    private List<Product> filteredProductsList;
    private ProductAdapter adapter;
    private SearchView searchView;

    private static FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize Firebase Realtime Database reference
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("Data/Products");

        // Initialize UI components
        ListView productList = findViewById(R.id.productList);
        searchView = findViewById(R.id.searchView);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        productsList = new ArrayList<>();
        filteredProductsList = new ArrayList<>();
        adapter = new ProductAdapter(this, R.layout.list_item_products, filteredProductsList);
        productList.setAdapter(adapter);



        // Retrieve data from Firebase
        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productsList.clear();

                for (DataSnapshot productSnapshot : dataSnapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);

                    if (product != null) {
                        productsList.add(product);
                    }
                }

                // Initially, display all products
                filteredProductsList.addAll(productsList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors

            }
        });

        // Set up search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return true;
            }
        });

        // Set up bottom navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if(item.getItemId() == R.id.action_profile) {
                    navigateToProfile();
                    return true;

            }
            return false;
        });
    }

    private void filterProducts(String query) {
        filteredProductsList.clear();

        for (Product product : productsList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredProductsList.add(product);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void navigateToProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // User is logged in, open ProfileActivity
            Intent intent = new Intent(MainActivity.this, EditProfile.class);
            startActivity(intent);
        } else {
            // User is not logged in, open LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private static class ProductAdapter extends ArrayAdapter<Product> {

        public ProductAdapter(MainActivity context, int resource, List<Product> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.list_item_products, null);
            }

            Product product = getItem(position);
            if (product != null) {
                ImageView productImage = convertView.findViewById(R.id.imageProduct);
                TextView productName = convertView.findViewById(R.id.textProductName);
                TextView productShortDescription = convertView.findViewById(R.id.textProductDescription);
                TextView productPrice = convertView.findViewById(R.id.textProductPrice);
                Button addToCartButton = convertView.findViewById(R.id.btnAddToCart);
                // Get references to quantity-related views
                ImageView decrease = convertView.findViewById(R.id.btnDecrease);
                ImageView increase = convertView.findViewById(R.id.btnIncrease);
                TextView quantity = convertView.findViewById(R.id.textQuantity);

                // Set initial quantity value
                int initialQuantity = 1;
                quantity.setText(String.valueOf(initialQuantity));

                // Set onClickListeners for decrease and increase buttons
                decrease.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Decrease quantity
                        int currentQuantity = Integer.parseInt(quantity.getText().toString());
                        if (currentQuantity > 1) {
                            currentQuantity--;
                            quantity.setText(String.valueOf(currentQuantity));
                        }
                    }
                });

                increase.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Increase quantity
                        int currentQuantity = Integer.parseInt(quantity.getText().toString());
                        currentQuantity++;
                        quantity.setText(String.valueOf(currentQuantity));
                    }
                });
                // Set values based on the Product object
                productName.setText(product.getName());
                productShortDescription.setText(product.getDescription());
                productPrice.setText(String.format("$%s", product.getPrice())); // Convert price to String

                addToCartButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Check if the user is logged in
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (currentUser != null) {
                            // User is logged in, allow adding to cart
                            Toast.makeText(getContext(), "Added to Cart: " + product.getName(), Toast.LENGTH_SHORT).show();

                            // Add the item to the user's cart in Firebase
                            addToCart(currentUser.getUid(), product, quantity.getText().toString());
                        } else {
                            // User is not logged in, prompt to log in
                            Toast.makeText(getContext(), "Please log in first to add to cart!", Toast.LENGTH_SHORT).show();
                            // TODO: Redirect the user to the login activity
                        }
                    }
                });
            }

            return convertView;
        }

        private void addToCart(String userId, Product product, String quantity) {
            // Get reference to the user's cart in Firebase
            DatabaseReference userCartRef = FirebaseDatabase.getInstance().getReference("Data/Users/" + userId + "/CartItems");

            // Generate a unique key for the cart item
            String cartItemId = userCartRef.push().getKey();

            // Create a CartItem object
            CartItem cartItem = new CartItem(cartItemId, product.getName(), product.getPrice(), Integer.parseInt(quantity));

            // Add the cart item to the user's cart
            userCartRef.child(cartItemId).setValue(cartItem);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Set up the search view
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle toolbar item clicks
        if (item.getItemId() == R.id.actionCart) {

            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null) {
                // User is logged in, open CartActivity
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            } else {
                // User is not logged in, open LoginActivity
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
