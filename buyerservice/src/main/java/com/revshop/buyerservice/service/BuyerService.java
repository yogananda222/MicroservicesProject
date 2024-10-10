package com.revshop.buyerservice.service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.revshop.buyerservice.dao.BuyerRepository;
import com.revshop.buyerservice.dao.CartItemRepository;
import com.revshop.buyerservice.dao.CartRepository;
import com.revshop.buyerservice.dao.FavoriteProductRepository;
import com.revshop.buyerservice.dao.OrderItemRepository;
import com.revshop.buyerservice.dao.OrderRepository;
import com.revshop.buyerservice.dao.ProductDAO;
import com.revshop.buyerservice.dao.ProductRepository;
import com.revshop.buyerservice.dao.ProductReviewRepository;
import com.revshop.buyerservice.entity.Buyer;
import com.revshop.buyerservice.entity.Cart;
import com.revshop.buyerservice.entity.CartItem;
import com.revshop.buyerservice.entity.FavoriteProduct;
import com.revshop.buyerservice.entity.Order;
import com.revshop.buyerservice.entity.OrderItem;
import com.revshop.buyerservice.entity.Product;
import com.revshop.buyerservice.entity.ProductReview;
import com.revshop.buyerservice.entity.Retailer;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BuyerService implements BuyerServiceInterface {
	
	 @Autowired
	    private CartItemRepository cartItemRepository;
	@Autowired
    private BuyerRepository buyerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

  
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductReviewRepository productReviewRepo;
    
    @Autowired
    private FavoriteProductRepository favoriteProductRepo;
    
    @Autowired
    private OrderItemRepository orderItemRepo;
	@Override
	public int registerBuyer(Buyer buyer) {

		if (buyerRepository.existsByEmail(buyer.getEmail())) {
			return 0; 
		}
		buyerRepository.save(buyer); 
		return 1; 
	}

	@Override
	public Buyer loginBuyer(Buyer buyer) {

	    Buyer existingBuyerOptional = buyerRepository.findByEmail(buyer.getEmail());

	   
	        if (existingBuyerOptional.getPassword().equals(buyer.getPassword())) {

	            if (existingBuyerOptional.isBlocked()) {
	                
	                throw new RuntimeException("Account is blocked.");
	            } else {
	               
	                return existingBuyerOptional;
	            }
	        } else {
	           
	            throw new RuntimeException("Invalid email or password.");
	        }
	    } 
	    
	
	
    @Override
    public List<Product> getAllProducts() {
    	
    	List<Product> products = productRepository.findAll();

        for (Product product : products) {
            String fullPath = product.getImage(); 
            String imageName = new File(fullPath).getName(); 
            product.setImage(imageName); 
        }

        return products;
    }
    

    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }
    @Override
    public Product getProductById(long productId) {
        return productRepository.findById(productId).orElse(null);
        
    }

    @Override
    public Buyer getBuyerByEmail(String email) {
        return buyerRepository.findByEmail(email);
    }

    @Override
    public Cart getCartByBuyerId(Long buyerId) {
        Buyer buyer = buyerRepository.findById(buyerId).orElse(null);
        return buyer != null ? cartRepository.findByBuyer(buyer) : null;
    }

    @Override
    public Buyer getBuyerById(Long buyerId) {
        return buyerRepository.findById(buyerId).orElse(null);
    }
    @Override
    public void addToCart(Product product, int quantity, Long buyerId) {

        Cart cart = cartRepository.findByBuyerBuyerId(buyerId);
        
        if (cart == null) {

            cart = new Cart();
            cart.setBuyer(buyerRepository.findById(buyerId).orElseThrow(() -> new RuntimeException("Buyer not found")));
            cart.setCartItems(new ArrayList<>());
        }


        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock available for the product.");
        }


        Optional<CartItem> existingItemOpt = cart.getCartItems().stream()
            .filter(item -> item.getProduct().getProductId().equals(product.getProductId()))
            .findFirst();

        if (existingItemOpt.isPresent()) {
           
            CartItem existingItem = existingItemOpt.get();
           
            if ((existingItem.getQuantity() + quantity) > product.getStockQuantity()) {
                throw new RuntimeException("Cannot add more items than available in stock.");
            }
            existingItem.setQuantity(existingItem.getQuantity() + quantity); 
        } else {

            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setCart(cart);
            cart.getCartItems().add(newItem);
        }

         cartRepository.save(cart); 
    }



    
    @Override
    @Transactional
    public void deleteCartItemById(Long cartItemId) {

        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);


        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            Cart cart = cartItem.getCart();


            cart.getCartItems().remove(cartItem);


            cartRepository.save(cart);


            cartItemRepository.delete(cartItem);


            if (cart.getCartItems().isEmpty()) {
               
                cartRepository.delete(cart);
            }
        } else {
            throw new RuntimeException("CartItem not found with ID: " + cartItemId);
        }
    }
    @Override
    public void deleteCart(Cart cart) {
        cartRepository.delete(cart);  
    }
    
    public Order placeOrder(Cart cart) {
     
        Order order = new Order();

     
        order.setBuyer(cart.getBuyer());

        if (!cart.getCartItems().isEmpty()) {
            Retailer retailer = cart.getCartItems().get(0).getProduct().getRetailer(); 
            order.setRetailer(retailer); 
        }


        order.setOrderStatus("Pending");
        order.setShippingAddress(cart.getBuyer().getCity()); 
        order.setBillingAddress(cart.getBuyer().getCity());  
        order.setTotalAmount(calculateTotalAmount(cart));   

        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);  
            orderItems.add(orderItem);  
            
            // Reducing the stock of the product after placing an order
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity()); 
            productRepository.save(product); 
        }


        order.setOrderItems(orderItems);  


        Order savedOrder = orderRepository.save(order);

  
        for (OrderItem orderItem : orderItems) {
            orderItemRepo.save(orderItem); 
        }

        cart.getCartItems().clear();
        cartRepository.save(cart); 

        return savedOrder;
    }


    @Override
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }



    private double calculateTotalAmount(Cart cart) {
        double total = 0.0;
        for (CartItem item : cart.getCartItems()) {
            total += item.getProduct().getPrice() * item.getQuantity(); 
        }
        return total;
    }





    @Override
    public String getAddressById(Long buyerId) {
      
        return null;
    }

	@Override
	public ProductReview addProductReview(ProductReview productReview) {
		 return productReviewRepo.save(productReview);
	}

	@Override
	public FavoriteProduct addFavoriteProduct(Long buyerId, Long productId) {
		  Buyer buyer = buyerRepository.findById(buyerId).orElseThrow(() -> new IllegalArgumentException("Buyer not found"));
	        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("Product not found"));

	        FavoriteProduct favoriteProduct = new FavoriteProduct();
	        favoriteProduct.setBuyer(buyer);
	        favoriteProduct.setProduct(product);

	        return favoriteProductRepo.save(favoriteProduct);
	}

	@Override
	public List<FavoriteProduct> getFavoritesByBuyer(Long buyerId) {
		return favoriteProductRepo.findByBuyerBuyerId(buyerId);
	}

	public List<Order> getPastOrdersByBuyerId(Long buyerId) {
	    Buyer buyer = buyerRepository.findById(buyerId).orElse(null);
	    if (buyer == null) {
	        throw new RuntimeException("Buyer not found");
	    }
	    
	    return buyer.getOrders();
	}

}
