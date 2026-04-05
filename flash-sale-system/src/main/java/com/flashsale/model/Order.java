package com.flashsale.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String userId;
    private int quantity;
    private double pricePaid;
    private LocalDateTime orderedAt;

    public Order() {}

    public Order(Long productId, String userId, int quantity, double pricePaid) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
        this.pricePaid = pricePaid;
        this.orderedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getUserId() { return userId; }
    public int getQuantity() { return quantity; }
    public double getPricePaid() { return pricePaid; }
    public LocalDateTime getOrderedAt() { return orderedAt; }

    public void setId(Long id) { this.id = id; }
    public void setProductId(Long productId) { this.productId = productId; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPricePaid(double pricePaid) { this.pricePaid = pricePaid; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }
}