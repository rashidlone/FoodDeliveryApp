package com.assignment.myecomapp;
public class Product {

    private String Name;
    private String ShortDescription;
    private String productId;

    private String Description;
    private double Price;  // Assuming price is a numeric value

    public Product() {
    }
    public Product(String name, String shortDescription, String longDescription, double price) {
        this.Name = name;
        this.ShortDescription = shortDescription;
        this.Description = longDescription;
        this.Price = price;
    }

    public String getName() {
        return Name;
    }

    public String getDescription() {
        return Description;
    }

    public double getPrice() {
        return Price;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
