package com.revshop.buyerservice.entity;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String orderStatus;
    private String shippingAddress;
    private String billingAddress;
    private double totalAmount;
    private LocalDate orderDate;

    // Getters and setters for the order date
    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate localDate) {
        this.orderDate = localDate;
    }

    // Many-to-one relationship with Buyer, prevent circular reference with @JsonManagedReference
    @ManyToOne
    @JoinColumn(name = "buyer_id")
    @JsonIgnore
    @JsonManagedReference
    private Buyer buyer;

    // Many-to-one relationship with Retailer, ignore in JSON output to prevent recursion
    @ManyToOne
    @JoinColumn(name = "retailer_id")
    @JsonIgnore // Avoid serialization of retailer
    private Retailer retailer;

    // One-to-many relationship with OrderItem, use @JsonIgnore if not needed in JSON response
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnore // Prevent recursive serialization of orderItems
    private List<OrderItem> orderItems;

    // One-to-one relationship with Invoice, use @JsonIgnore if not needed in JSON response
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    @JsonIgnore // Prevent recursive serialization of invoice
    private Invoice invoice;

    // Getters and setters for Order fields
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(String billingAddress) {
        this.billingAddress = billingAddress;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Buyer getBuyer() {
        return buyer;
    }

    public void setBuyer(Buyer buyer) {
        this.buyer = buyer;
    }

    public Retailer getRetailer() {
        return retailer;
    }

    public void setRetailer(Retailer retailer) {
        this.retailer = retailer;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
