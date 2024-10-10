package com.revshop.buyerservice.dto;

import com.revshop.buyerservice.entity.Buyer;
import com.revshop.buyerservice.entity.Retailer;

public class ProductReviewDto {
	
	private Long reviewId;

	private int rating;
	
	private String reviewText;
	
	private Buyer buyer;
	
	private String buyerName; 
	
	private Retailer retailer; 
	
	private Long retailerId; 
	
	
	public Long getReviewId() {
		return reviewId;
	}
	public void setReviewId(Long reviewId) {
		this.reviewId = reviewId;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getReviewText() {
		return reviewText;
	}
	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}
	public Buyer getBuyer() {
		return buyer;
	}
	public void setBuyer(Buyer buyer) {
		this.buyer = buyer;
	}
	public String getBuyerName() {
		return buyerName;
	}
	public void setBuyerName(String buyerName) {
		this.buyerName = buyerName;
	}
	public Retailer getRetailer() {
		return retailer;
	}
	public void setRetailer(Retailer retailer) {
		this.retailer = retailer;
	}
	public Long getRetailerId() {
		return retailerId;
	}
	public void setRetailerId(Long retailerId) {
		this.retailerId = retailerId;
	}

	
	
	
	
	
	

	
}
