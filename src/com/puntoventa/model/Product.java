package com.puntoventa.model;

import java.math.BigDecimal;

public class Product {
    private int id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private int stock;
    private boolean active;
    private Category category;
    private boolean hasIva;  // Field to track if IVA applies

    public Product() {}

    public Product(int id, String code, String name, String description, BigDecimal price, int stock, boolean active, Category category) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.active = active;
        this.category = category;
        this.hasIva = true;  // By default, products have IVA
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public boolean hasIva() { return hasIva; }
    public void setHasIva(boolean hasIva) { this.hasIva = hasIva; }

    @Override
    public String toString() {
        return name + " (" + code + ")";
    }
}