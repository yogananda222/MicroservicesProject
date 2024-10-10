package com.revshop.revshopClientApp.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.revshopClientApp.EmailService.EmailService;
import com.revshop.revshopClientApp.dto.Buyer;
import com.revshop.revshopClientApp.dto.CartItemDTO;
import com.revshop.revshopClientApp.dto.FavoriteProduct;
import com.revshop.revshopClientApp.dto.Order;
import com.revshop.revshopClientApp.dto.Product;
import com.revshop.revshopClientApp.dto.ProductReview;
import com.revshop.revshopClientApp.dto.ProductReviewDto;
import com.revshop.revshopClientApp.dto.Retailer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class BuyerControllerClientSide {

    @Autowired
    private DiscoveryClient discoveryClient;
    
    @Autowired
    private EmailService emailService;

    @RequestMapping(value = "/registerBuyer", method = RequestMethod.POST)  // Specify POST method
    public ModelAndView registerBuyer(@RequestParam("name") String name,
                                      @RequestParam("email") String email,
                                      @RequestParam("contactNo") String contactNo,
                                      @RequestParam("password") String password,
                                      @RequestParam("city") String city,
                                      HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView();

        Buyer buyer = new Buyer();
        buyer.setName(name);
        buyer.setEmail(email);
        buyer.setContactNo(contactNo);
        buyer.setPassword(password);
        buyer.setCity(city);

        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");

        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage"); 
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/register";
        System.out.println(baseUrl);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Buyer> entity = new HttpEntity<>(buyer, headers);

        try {
            ResponseEntity<String> result = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
            String responseBody = result.getBody();
            System.out.println("Response Body: " + responseBody);

            // Check if the registration is successful
            if (responseBody != null && responseBody.contains("success")) {
            	request.getSession().setAttribute("alertMessage", "Buyer registered successfully");
                modelAndView.setViewName("redirect:/user/authentication.jsp"); 
//                modelAndView.addObject("alertMessage", "Buyer registered successfully");
            } else {
                request.getSession().setAttribute("alertMessage", "Buyer registration failed");
                modelAndView.setViewName("redirect:/user/error.jsp"); 
//                modelAndView.addObject("alertMessage", "Buyer registration failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("alertMessage", "An error occurred during registration");
            modelAndView.setViewName("redirect:/user/error.jsp");
//            modelAndView.addObject("alertMessage", "An error occurred during registration");
        }

        return modelAndView;
    }
    
    
    @RequestMapping("/loginBuyer")
    public ModelAndView loginBuyer(@RequestParam("email") String email, 
                                    @RequestParam("password") String password, 
                                    HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        HttpSession session = request.getSession();
        
        Buyer buyer = new Buyer();
        buyer.setEmail(email);
        buyer.setPassword(password);
        
        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
        
        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/login";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<Buyer> entity = new HttpEntity<>(buyer, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();
            
            // Log the response for debugging
            System.out.println("Response Body: " + responseBody); 
            
            // Check response status and login result
            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                // Deserialize the response into a Retailer object
                ObjectMapper objectMapper = new ObjectMapper();
                Buyer loggedInBuyer = objectMapper.readValue(responseBody, Buyer.class);
                
                session.setAttribute("email", loggedInBuyer.getEmail());
                session.setAttribute("buyerId", loggedInBuyer.getBuyerId());
                session.setAttribute("name", loggedInBuyer.getName());
                session.setAttribute("password", loggedInBuyer.getPassword());
                session.setAttribute("contactNo", loggedInBuyer.getContactNo()); 
                session.setAttribute("city", loggedInBuyer.getCity());
                session.setAttribute("loginMessage", "Buyer logged in successfully");
                
                // Fetch all products after successful login
                List<ServiceInstance> productInstances = discoveryClient.getInstances("BUYERSERVICE");
                ServiceInstance productServiceInstance = productInstances.get(0);
                String productBaseUrl = productServiceInstance.getUri().toString() + "/api/buyers/AllProducts";

                ResponseEntity<List<Product>> productResponse = restTemplate.exchange(
                    productBaseUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Product>>() {}
                );

                if (productResponse.getStatusCode() == HttpStatus.OK) {
                    List<Product> products = productResponse.getBody();
                    // Store the products in session to access them in the Homepage.jsp
                    session.setAttribute("products", products); // Pass products to the session
                } else {
                    modelAndView.addObject("alertMessage", "Error fetching products after login.");
                }

                // Redirect to Homepage.jsp
                modelAndView.setViewName("redirect:/user/Homepage.jsp"); 
            } else {
            	request.getSession().setAttribute("alertMessage", "Buyer login failed");
                modelAndView.setViewName("redirect: /user/authentication.jsp"); 
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("alertMessage", "Invalid Credentails");
            modelAndView.setViewName("redirect:/user/authentication.jsp");

        }
        return modelAndView;
    }
    
	@RequestMapping("buyer/logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false); // Get the current session, if it exists
		if (session != null) {
			session.invalidate(); // Invalidate the session
		}
		return "redirect:/user/authentication.jsp"; // Redirect to the main page or a logout confirmation page
	}
    
    
    @RequestMapping("/productDetails")
    public ModelAndView getProductById(@RequestParam("productId") Long productId, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("redirect:/user/ProductDetails.jsp");
        HttpSession session = request.getSession();

        // Fetch buyer service instances
        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");

        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        // Build the URL for the product endpoint
        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/product/" + productId;
System.out.println(baseUrl);
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Fetch product details from the product service
            ResponseEntity<Product> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null, Product.class);
            Product product = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && product != null) {
                session.setAttribute("product", product);
                // Store the product details in session
            } else {
                session.setAttribute("errorMessage", "Product not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "An error occurred while fetching product details.");
        }

        return modelAndView;
    }

    @RequestMapping("/addToCart")
    public ModelAndView addToCart(@RequestParam("productId") Long productId,
                                   @RequestParam("quantity") int quantity,
                                   HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView();
        HttpSession session = request.getSession();

        // Fetch buyer ID from the session
        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer is not logged in.");
            return modelAndView;
        }

        // Get the service instance of the BuyerService
        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        // Build the base URL dynamically
        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/add-to-cart?buyerId=" + buyerId + "&productId=" + productId + "&quantity=" + quantity;

        RestTemplate restTemplate = new RestTemplate();

        // Call the add-to-cart API
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, null, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                modelAndView.addObject("message", "Item added to cart successfully.");
            } else {
                modelAndView.addObject("message", "Failed to add item to cart. Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Error adding item to cart.");
            return modelAndView;
        }

        // Redirect to the viewCart URL after adding the item
        modelAndView.setViewName("redirect:/viewCart");
        return modelAndView;
    }


    @RequestMapping("/viewCart")
    public ModelAndView viewCart(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("/user/cart.jsp");
        HttpSession session = request.getSession();

        Long buyerId = (Long) session.getAttribute("buyerId");
        if (buyerId == null) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer is not logged in.");
            return modelAndView;
        }

        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/cart?buyerId=" + buyerId;

        RestTemplate restTemplate = new RestTemplate();

        try {
            ResponseEntity<List<CartItemDTO>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<CartItemDTO>>() {});
            List<CartItemDTO> cartItems = response.getBody();

            if (response.getStatusCode() == HttpStatus.OK && cartItems != null) {
                request.setAttribute("cartItems", cartItems);
            } else {
                request.setAttribute("message", "No items in the cart.");
            }
        } catch (HttpClientErrorException e) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Error fetching cart items.");
        }

        return modelAndView;
    }


	
    @RequestMapping("/place-order")
    public ModelAndView placeOrder(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("/user/Orderconfirmation.jsp");
        HttpSession session = request.getSession();
        Long buyerId = (Long) session.getAttribute("buyerId");

        if (buyerId == null) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer is not logged in.");
            return modelAndView;
        }

        // Fetch the buyer service instances
        List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
        if (instances.isEmpty()) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Buyer service is unavailable.");
            return modelAndView;
        }

        // Get the first available instance
        ServiceInstance serviceInstance = instances.get(0);
        String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/place-order?buyerId=" + buyerId;

        RestTemplate restTemplate = new RestTemplate();
        try {
            // Call the buyer service to place an order
            ResponseEntity<Order> response = restTemplate.exchange(baseUrl, HttpMethod.POST, null, Order.class);
            Order order = response.getBody();

            if (response.getStatusCode() == HttpStatus.CREATED && order != null) {
                modelAndView.addObject("order", order);  
                modelAndView.setViewName("/user/Orderconfirmation.jsp");  
                
                // Send email confirmation
                String buyerEmail = (String) session.getAttribute("buyerEmail");  
                if (buyerEmail != null) {
                    String subject = "Order Confirmation - Order ID: " + order.getOrderId();
                    String body = "Thank you for your order! Your order ID is " + order.getOrderId() +
                            ".\nOrder Date: " + order.getOrderDate() +
                            "\nTotal Amount: " + order.getTotalAmount() + 
                            "\n Order Status: " + order.getOrderStatus() +
                            "\n Products: " + order.getOrderItems() +
                            "\nWe will notify you when your order is shipped.";
                    emailService.sendSimpleEmail(buyerEmail, subject, body);
                }
            } else {
                modelAndView.addObject("message", "Failed to place order.");
                modelAndView.setViewName("errorPage");
            }
        } catch (HttpClientErrorException e) {
            modelAndView.setViewName("errorPage");
            modelAndView.addObject("message", "Error placing order: " + e.getMessage());
        }

        return modelAndView;
    }
    
    

    
	@RequestMapping("/removefromcart")
	public ModelAndView removeFromCart(@RequestParam("cartItemId") Long cartItemId, HttpServletRequest request) {
	    ModelAndView modelAndView = new ModelAndView("cart");

	    // Retrieve buyerId from session
	    HttpSession session = request.getSession();
	    Long buyerId = (Long) session.getAttribute("buyerId");

	    if (buyerId == null) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer is not logged in.");
	        return modelAndView;
	    }

	    // Fetch the Buyer service instance from Eureka Discovery
	    List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
	    if (instances.isEmpty()) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer service is unavailable.");
	        return modelAndView;
	    }

	    // Construct the base URL for BUYERSERVICE
	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/remove-from-cart?buyerId=" + buyerId + "&cartItemId=" + cartItemId;
	    System.out.println(baseUrl);

	    RestTemplate restTemplate = new RestTemplate();
	    try {
	        // Send request to remove item from cart
	        ResponseEntity<Void> response = restTemplate.exchange(baseUrl, HttpMethod.POST, null, Void.class);

	        // Check the response status
	        if (response.getStatusCode() == HttpStatus.OK) {
	            modelAndView.addObject("message", "Item removed from cart successfully.");
	        } else {
	            modelAndView.addObject("message", "Failed to remove item from cart.");
	        }
	    } catch (HttpClientErrorException e) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Error removing item from cart.");
	    }

	    // Refresh the cart after removing an item
	    return viewCart(request); // Assuming this method fetches the updated cart
	}
	
	

	@RequestMapping("/update-cart")
	public ModelAndView updateCart(@RequestParam("cartItemId") Long cartItemId, 
	                               @RequestParam("quantity") int quantity, 
	                               HttpServletRequest request) {
	    ModelAndView modelAndView = new ModelAndView("cart");
	    HttpSession session = request.getSession();
	    Long buyerId = (Long) session.getAttribute("buyerId");

	    // Check if buyer is logged in
	    if (buyerId == null) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer is not logged in.");
	        return modelAndView;
	    }

	    // Fetch the Buyer service instance using Eureka Discovery
	    List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
	    if (instances.isEmpty()) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer service is unavailable.");
	        return modelAndView;
	    }

	    // Construct the base URL to update cart item quantity
	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/update-cart?buyerId=" + buyerId + "&cartItemId=" + cartItemId + "&quantity=" + quantity;
	    System.out.println(baseUrl);
	    RestTemplate restTemplate = new RestTemplate();

	    try {
	        // Send POST request to update the cart item
	        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, null, String.class);

	        if (response.getStatusCode() == HttpStatus.OK) {
	            modelAndView.addObject("message", "Cart updated successfully.");
	        } else {
	            modelAndView.addObject("message", "Failed to update cart. Status: " + response.getStatusCode());
	        }
	    } catch (HttpClientErrorException e) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Error updating cart item.");
	    }

	    // Refresh the cart after updating the item
	    return viewCart(request); // Assuming this method fetches the updated cart
	}
	
	
	@RequestMapping("/view-past-orders")
	public ModelAndView viewPastOrders(HttpServletRequest request) {
	    ModelAndView modelAndView = new ModelAndView("user/pastOrders.jsp"); // assuming you have or will create a JSP page for displaying past orders

	    // Retrieve buyerId from session
	    HttpSession session = request.getSession();
	    Long buyerId = (Long) session.getAttribute("buyerId");

	    if (buyerId == null) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer is not logged in.");
	        return modelAndView;
	    }

	    // Fetch the Buyer service instance from Eureka Discovery
	    List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
	    if (instances.isEmpty()) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer service is unavailable.");
	        return modelAndView;
	    }

	    // Construct the URL for fetching past orders
	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/past-orders?buyerId=" + buyerId;
	    System.out.println(baseUrl);

	    RestTemplate restTemplate = new RestTemplate();
	    try {
	        // Send request to get past orders
	        ResponseEntity<List<Order>> response = restTemplate.exchange(
	                baseUrl, HttpMethod.GET, null,
	                new ParameterizedTypeReference<List<Order>>() {});

	        // Check the response status
	        if (response.getStatusCode() == HttpStatus.OK) {
	            modelAndView.addObject("orders", response.getBody()); // Adding the list of past orders to the model
	        } else {
	            modelAndView.addObject("message", "Failed to retrieve past orders.");
	        }
	    } catch (HttpClientErrorException e) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Error retrieving past orders.");
	    }

	    return modelAndView;
	}
	
	
	@RequestMapping(value = "/addProductReview", method = RequestMethod.POST)
	public ModelAndView addProductReview(
	        @RequestParam("productId") Long productId,
	        @RequestParam("reviewText") String reviewText,
	        @RequestParam("rating") int rating,
	        HttpServletRequest request) {

	    ModelAndView modelAndView = new ModelAndView();

	    // Get buyerId from the session
	    HttpSession session = request.getSession();
	    Long buyerId = (Long) session.getAttribute("buyerId");

	    if (buyerId == null) {
	        modelAndView.setViewName("redirect:/user/login.jsp");
	        request.getSession().setAttribute("alertMessage", "Please log in to add a review");
	        return modelAndView;
	    }

	    // Create Buyer object
	    Buyer buyer = new Buyer();
	    buyer.setBuyerId(buyerId);

	    // Create ProductReviewDto object
	    ProductReviewDto productReviewDto = new ProductReviewDto();
	    productReviewDto.setBuyer(buyer); 
	    productReviewDto.setReviewText(reviewText);
	    productReviewDto.setRating(rating);
	    productReviewDto.setBuyerName(buyer.getName());


	    // Get BuyerService instance through service discovery
	    List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
	    if (instances.isEmpty()) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer service is unavailable.");
	        return modelAndView;
	    }

	    // Prepare request URL for the BuyerService API
	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/add-review";

	    // Make HTTP POST request to add the review
	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

	    // Create HttpEntity with ProductReviewDto
	    HttpEntity<ProductReviewDto> entity = new HttpEntity<>(productReviewDto, headers);

	    try {
	        ResponseEntity<String> result = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
	        String responseBody = result.getBody();
	        if (responseBody != null && result.getStatusCode() == HttpStatus.CREATED) {
	            request.getSession().setAttribute("alertMessage", "Product review added successfully");
	            modelAndView.setViewName("redirect:/user/ProductDetails.jsp");
	        } else {
	            request.getSession().setAttribute("alertMessage", "Failed to add product review");
	            modelAndView.setViewName("redirect:/user/error.jsp");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        request.getSession().setAttribute("alertMessage", "An error occurred while adding the review");
	        modelAndView.setViewName("redirect:/user/error.jsp");
	    }

	    return modelAndView;
	}

	

	@RequestMapping(value = "/addFavoriteProduct", method = RequestMethod.POST)
	public ModelAndView addFavoriteProduct(@RequestParam("productId") Long productId,
	                                       HttpServletRequest request) {

	    ModelAndView modelAndView = new ModelAndView();

	    // Get buyerId from the session
	    HttpSession session = request.getSession();
	    Long buyerId = (Long) session.getAttribute("buyerId");

	    if (buyerId == null) {
	        modelAndView.setViewName("redirect:/user/login.jsp");
	        request.getSession().setAttribute("alertMessage", "Please log in to add a product to favorites");
	        return modelAndView;
	    }

	    // Get BuyerService instance
	    List<ServiceInstance> instances = discoveryClient.getInstances("BUYERSERVICE");
	    if (instances.isEmpty()) {
	        modelAndView.setViewName("errorPage");
	        modelAndView.addObject("message", "Buyer service is unavailable.");
	        return modelAndView;
	    }

	    // Prepare request URL
	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = serviceInstance.getUri().toString() + "/api/buyers/addToFavorites";
	    System.out.println(baseUrl);

	    // Make HTTP POST request
	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

	    HttpEntity<String> entity = new HttpEntity<>(headers);

	    try {
	        ResponseEntity<FavoriteProduct> result = restTemplate.exchange(
	                baseUrl + "?buyerId=" + buyerId + "&productId=" + productId,
	                HttpMethod.POST,
	                entity,
	                FavoriteProduct.class);

	        if (result.getStatusCode() == HttpStatus.OK) {
	            request.getSession().setAttribute("alertMessage", "Product added to favorites successfully");
	            modelAndView.setViewName("redirect:/user/Homepage.jsp");
	        } else {
	            request.getSession().setAttribute("alertMessage", "Failed to add product to favorites");
	            modelAndView.setViewName("redirect:/user/error.jsp");
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        request.getSession().setAttribute("alertMessage", "An error occurred while adding the product to favorites");
	        modelAndView.setViewName("redirect:/user/error.jsp");
	    }

	    return modelAndView;
	}

	
}