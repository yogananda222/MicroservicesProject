package com.revshop.retailerservice.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.revshop.retailerservice.dao.OrderDAO;
import com.revshop.retailerservice.dao.ProductDAO;
import com.revshop.retailerservice.dao.ProductReviewDAO;
import com.revshop.retailerservice.dao.RetailerDAO;
import com.revshop.retailerservice.entity.Order;
import com.revshop.retailerservice.entity.Product;
import com.revshop.retailerservice.entity.ProductReview;
import com.revshop.retailerservice.entity.Retailer;

import org.springframework.kafka.core.KafkaTemplate;

@Service
@Transactional
public class RetailerService implements RetailerServiceInterface{
	
	@Autowired
	private KafkaTemplate<String, AddProductEvent> kafkaTemplate;


	@Autowired

	private RetailerDAO retailerDAO;

	@Autowired
	private OrderDAO orderDAO; 

	@Autowired
	private ProductDAO productDAO; 

	@Autowired
	private ProductReviewDAO productReviewDAO; 

	@Override
	public int registerRetailer(Retailer retailer) {
		
		if (retailerDAO.existsByEmail(retailer.getEmail())) {
			return 0;
		}
		retailerDAO.save(retailer); 
		return 1; 
	}

	@Override
	public Retailer loginRetailer(Retailer retailer) {
		Optional<Retailer> existingRetailerOptional = retailerDAO.findByEmail(retailer.getEmail());

		if (existingRetailerOptional.isPresent()) {
			Retailer existingRetailer = existingRetailerOptional.get();

			if (existingRetailer.getPassword().equals(retailer.getPassword())) {
				if (!existingRetailer.isApproved()) {
					throw new RuntimeException("Account is not approved.");
				}
				if (existingRetailer.isBlocked()) {
					throw new RuntimeException("Account is blocked.");
				}
				return existingRetailer; 
			} else {
				throw new RuntimeException("Invalid email or password.");
			}
		}
		throw new RuntimeException("Failed to login: retailer not found");
	}

	@Override
	public boolean isEmailAlreadyUsed(String email) {
		return retailerDAO.existsByEmail(email);
	}

	@Override
	public int deleteProductById(Long productId) {
		if (productDAO.existsById(productId)) {
			productDAO.deleteById(productId);
			return 1; 
		}
		return -1; 
	}

	@Override
	public int ManageInventoryByProductId(Long productId, int newStockQuantity) {
		Optional<Product> productOptional = productDAO.findById(productId);
		if (productOptional.isPresent()) {
			Product product = productOptional.get();
			product.setStockQuantity(newStockQuantity);
			productDAO.save(product); 
			return 1; 
		}
		return -1; // If Product not found
	}

	@Override
	public List<Product> getProductsByRetailerId(Long retailerId) {
		return productDAO.findByRetailerId(retailerId);
	}

	@Override
	public List<Order> getOrdersByRetailerId(Long retailerId) {
		return orderDAO.findByRetailerId(retailerId);
	}

	@Override
	public int updateOrderStatus(Long orderId, String newStatus) {
		Order order = orderDAO.findById(orderId).orElse(null);
		if (order != null) {
			order.setOrderStatus(newStatus);
			orderDAO.save(order);
			return 1; // Success
		}
		return -1; // Order not found
	}

	@Override
	public List<ProductReview> getReviewsByProductId(Long productId) {
		return productReviewDAO.findByProductId(productId);
	}

	@Override
	public int deleteReviewById(Long reviewId) {
		if (productReviewDAO.existsById(reviewId)) {
			productReviewDAO.deleteById(reviewId);
			return 1; // Success
		}
		return -1; // Review not found
	}

	@Override
	public List<ProductReview> getReviewsByRetailerId(Long retailerId) {
		return productReviewDAO.findByRetailerId(retailerId);
	}

	@Override
	public int getTotalOrderCountByRetailerId(Long retailerId) {
		return orderDAO.countByRetailerId(retailerId);
	}

	public int getTotalProductReviewCountByRetailerId(Long retailerId) {
		return productReviewDAO.getTotalProductReviewCountByRetailerId(retailerId);
	}

	@Override
	public int getTotalProductCountByRetailerId(Long retailerId) {
		return productDAO.getTotalProductCount(retailerId);
	}

	@Override
	public Retailer getRetailerById(Long retailerId) {
		return retailerDAO.getRetailerById(retailerId);
	}

	@Override
	public Retailer addProductToRetailer(Long retailerId, Product product) {
		Optional<Retailer> retailerOptional = retailerDAO.findById(retailerId);

		if (retailerOptional.isPresent()) {
			Retailer retailer = retailerOptional.get();
			product.setRetailer(retailer);
			productDAO.save(product);
			kafkaTemplate.send("notificationTopic", new AddProductEvent(product.getProductName()));
			return retailer;
		} else {
			throw new RuntimeException("Retailer not found with ID: " + retailerId);
		}
	}

	@Override
	public int updateProduct(Long retailerId, Product product) {
		Optional<Product> existingProductOptional = productDAO.findById(product.getProductId());

		if (existingProductOptional.isPresent()) {
			Product existingProduct = existingProductOptional.get();

			existingProduct.setProductName(product.getProductName());
			existingProduct.setDescription(product.getDescription());
			existingProduct.setPrice(product.getPrice());
			existingProduct.setStockQuantity(product.getStockQuantity());
			existingProduct.setCategory(product.getCategory());

			productDAO.save(existingProduct);
			return 1; // Success
		}
		return -1; // Product not found
	}

	@Override
	public Product getProductById(Long retailerId, Long productId) {
	    Optional<Product> productOptional = productDAO.findById(productId);
	    if (productOptional.isPresent()) {
	        Product product = productOptional.get();

	        if (product.getRetailer().getRetailerId().equals(retailerId)) {
	            return product; 
	        } else {
	            throw new RuntimeException("Product does not belong to retailer with ID: " + retailerId);
	        }
	    } else {
	        throw new RuntimeException("Product not found with ID: " + productId);
	    }
	}

	@Override
	public Retailer getRetailerByEmail(String email) {
	
		return null;
	}


}