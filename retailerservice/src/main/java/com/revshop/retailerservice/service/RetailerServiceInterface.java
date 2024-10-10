package com.revshop.retailerservice.service;

import java.util.List;

import com.revshop.retailerservice.entity.Order;
import com.revshop.retailerservice.entity.Product;
import com.revshop.retailerservice.entity.ProductReview;
//import java.util.List;
//import java.util.Optional;
//
//import com.revshop.retailerservice.entity.Category;
//import com.revshop.retailerservice.entity.Order;
//import com.revshop.retailerservice.entity.Product;
//import com.revshop.retailerservice.entity.ProductReview;
import com.revshop.retailerservice.entity.Retailer;

public interface RetailerServiceInterface {


	int registerRetailer(Retailer retailer);

	Retailer loginRetailer(Retailer retailer);

	Retailer getRetailerByEmail(String email);

	boolean isEmailAlreadyUsed(String email);	
	
	Retailer addProductToRetailer(Long retailerId, Product product);

	int deleteProductById(Long productId); 
	
	List<Product> getProductsByRetailerId(Long retailerId);

	int ManageInventoryByProductId(Long productId, int newStockQuantity);
	
    List<Order> getOrdersByRetailerId(Long retailerId);

    int updateOrderStatus(Long orderId, String newStatus);
  
    List<ProductReview> getReviewsByProductId(Long productId);

    int deleteReviewById(Long reviewId);

    List<ProductReview> getReviewsByRetailerId(Long retailerId);
    
    int getTotalOrderCountByRetailerId(Long retailerId);
    
    int getTotalProductReviewCountByRetailerId(Long retailerId);

	int getTotalProductCountByRetailerId(Long retailerId);

	int updateProduct(Long retailerId, Product product);

	Product getProductById(Long retailerId, Long productId);

	Retailer getRetailerById(Long retailerId); 

}
