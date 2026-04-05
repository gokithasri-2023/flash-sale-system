package com.flashsale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private double originalPrice;
    private double salePrice;
    private int stock;
    private boolean saleActive;
    private LocalDateTime saleStart;
    private LocalDateTime saleEnd;

    public Product() {}

    public Product(String name, double originalPrice, double salePrice,
                   int stock, LocalDateTime saleStart, LocalDateTime saleEnd) {
        this.name = name;
        this.originalPrice = originalPrice;
        this.salePrice = salePrice;
        this.stock = stock;
        this.saleActive = true;
        this.saleStart = saleStart;
        this.saleEnd = saleEnd;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public double getOriginalPrice() { return originalPrice; }
    public double getSalePrice() { return salePrice; }
    public int getStock() { return stock; }
    public boolean isSaleActive() { return saleActive; }
    public LocalDateTime getSaleStart() { return saleStart; }
    public LocalDateTime getSaleEnd() { return saleEnd; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setOriginalPrice(double p) { this.originalPrice = p; }
    public void setSalePrice(double p) { this.salePrice = p; }
    public void setStock(int stock) { this.stock = stock; }
    public void setSaleActive(boolean b) { this.saleActive = b; }
    public void setSaleStart(LocalDateTime t) { this.saleStart = t; }
    public void setSaleEnd(LocalDateTime t) { this.saleEnd = t; }
}