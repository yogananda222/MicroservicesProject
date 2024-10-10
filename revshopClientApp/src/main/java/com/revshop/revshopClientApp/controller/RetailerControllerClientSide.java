package com.revshop.revshopClientApp.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revshop.revshopClientApp.EmailService.EmailService;
import com.revshop.revshopClientApp.dto.Order;
import com.revshop.revshopClientApp.dto.Product;
import com.revshop.revshopClientApp.dto.ProductReview;
import com.revshop.revshopClientApp.dto.Retailer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RetailerControllerClientSide {

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private EmailService emailService;

	private static final String UPLOAD_DIR = "C:/Users/dell/OneDrive/Desktop/revshopClientApp/revshopClientApp/src/main/webapp/IMAGES";

	@RequestMapping("register")
	public ModelAndView register(@RequestParam("contactNo") String contactNo, @RequestParam("email") String email,
			@RequestParam("businessName") String businessName, @RequestParam("password") String password,
			HttpServletRequest request) {

		ModelAndView mv = new ModelAndView();

		Retailer retailer = new Retailer();
		retailer.setContactNo(contactNo);
		retailer.setEmail(email);
		retailer.setBusinessName(businessName);
		retailer.setPassword(password);

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");

		if (instances == null || instances.isEmpty()) {
			mv.setViewName("redirect:/seller/createAccount.jsp");
			request.getSession().setAttribute("message", "Service unavailable. Please try again later.");
			return mv;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/register";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Retailer> entity = new HttpEntity<>(retailer, headers);

		try {
			ResponseEntity<String> result = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
			String responseBody = result.getBody();


			if (responseBody != null && responseBody.contains("success")) {
				request.getSession().setAttribute("message", "Retailer registered successfully");

				String subject = "Thank you for your Registration!";
				String body = "Dear " + businessName + ",\n\n"
						+ "Thank you for registering your business with Revshop.\n"
						+ "Our team will get in touch with you shortly.\n\n" + "Best regards,\nThe Revshop Team";

				emailService.sendSimpleEmail(email, subject, body);

				mv.setViewName("redirect:/seller/mainpage.jsp"); 
			} else {
				request.getSession().setAttribute("message", "Retailer registration failed");
				mv.setViewName("redirect:/seller/createAccount.jsp");
			}
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An error occurred during registration");
			mv.setViewName("redirect:/seller/createAccount.jsp"); 
		}

		return mv;
	}

	@RequestMapping("login")
	public ModelAndView login(@RequestParam("email") String email, 
	                          @RequestParam("password") String password,
	                          HttpServletRequest request) {
	    
	    HttpSession session = request.getSession();

	    Retailer retailer = new Retailer();
	    retailer.setEmail(email);
	    retailer.setPassword(password);

	    List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");

	    ModelAndView modelAndView = new ModelAndView();

	    if (instances == null || instances.isEmpty()) {
	        session.setAttribute("message", "Service unavailable. Please try again later.");
	        modelAndView.setViewName("redirect:/seller/login.jsp");
	        return modelAndView;
	    }

	    ServiceInstance serviceInstance = instances.get(0);
	    String baseUrl = "http://localhost:8181/retailer/login";

	    RestTemplate restTemplate = new RestTemplate();
	    HttpHeaders headers = new HttpHeaders();
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	    HttpEntity<Retailer> entity = new HttpEntity<>(retailer, headers);

	    try {

	        ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
	        String responseBody = response.getBody();


	        if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
	            ObjectMapper objectMapper = new ObjectMapper();
	            Retailer loggedInRetailer = objectMapper.readValue(responseBody, Retailer.class);


	            session.setAttribute("retailerId", loggedInRetailer.getRetailerId());
	            session.setAttribute("businessName", loggedInRetailer.getBusinessName());
	            session.setAttribute("email", loggedInRetailer.getEmail());
	            session.setAttribute("contactNo", loggedInRetailer.getContactNo());
	            session.setAttribute("isApproved", loggedInRetailer.isApproved());
	            session.setAttribute("isBlocked", loggedInRetailer.isBlocked());
	            session.setAttribute("message", "Login successful!");

	            modelAndView.setViewName("redirect:/seller/dashboard");
	        } else {

	            if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
	                session.setAttribute("message", "Account is blocked or not approved.");
	            } else if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
	                session.setAttribute("message", "Invalid email or password.");
	            } else {
	                session.setAttribute("message", "Login failed: " + responseBody);
	            }
	            modelAndView.setViewName("redirect:/seller/login.jsp");
	        }
	    } catch (HttpClientErrorException e) {
	        session.setAttribute("message", " " + e.getResponseBodyAsString());
	        modelAndView.setViewName("redirect:/seller/login.jsp");
	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("message", "An error occurred during login. Please try again.");
	        modelAndView.setViewName("redirect:/seller/login.jsp");
	    }
	    return modelAndView;
	}

	@RequestMapping("/seller/dashboard")
	public ModelAndView dashboard(HttpServletRequest request) {

	    HttpSession session = request.getSession();
	    Long retailerId = (Long) session.getAttribute("retailerId");
	    System.out.println(retailerId);

	    ModelAndView modelAndView = new ModelAndView("Dashboard.jsp");

	    if (retailerId == null) {
	        modelAndView.setViewName("redirect:/seller/login.jsp");
	        session.setAttribute("message", "Please log in first.");
	        return modelAndView;
	    }

	    List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");

	    if (instances == null || instances.isEmpty()) {
	        session.setAttribute("message", "Service unavailable. Please try again later.");
	        modelAndView.setViewName("redirect:/seller/login.jsp");
	        return modelAndView;
	    }

	    ServiceInstance serviceInstance = instances.get(0);
	    RestTemplate restTemplate = new RestTemplate();

	    try {
	
	        String salesCountUrl = "http://localhost:8181/retailer/" + retailerId + "/orders/count";
	        ResponseEntity<Integer> salesCountResponse = restTemplate.getForEntity(salesCountUrl, Integer.class);
	        Integer salesCount = salesCountResponse.getStatusCode() == HttpStatus.OK ? 
	                              salesCountResponse.getBody() : 0;
	        modelAndView.addObject("salesCount", salesCount);


	        String productReviewsCountUrl = "http://localhost:8181/retailer/productReviewsCount/" + retailerId;
	        ResponseEntity<Integer> productReviewsCountResponse = restTemplate.getForEntity(productReviewsCountUrl, Integer.class);
	        Integer productReviewsCount = productReviewsCountResponse.getStatusCode() == HttpStatus.OK ? 
	                                      productReviewsCountResponse.getBody() : 0;
	        modelAndView.addObject("productReviewsCount", productReviewsCount);

	        String productsCountUrl = "http://localhost:8181/retailer/" + retailerId + "/products/count";
	        ResponseEntity<Integer> productsCountResponse = restTemplate.getForEntity(productsCountUrl, Integer.class);
	        Integer productsCount = productsCountResponse.getStatusCode() == HttpStatus.OK ? 
	                                productsCountResponse.getBody() : 0;
	        modelAndView.addObject("productsCount", productsCount);

	    } catch (Exception e) {
	        e.printStackTrace();
	        session.setAttribute("message", "An error occurred while fetching dashboard details.");
	    }

	    return modelAndView;
	}


	@RequestMapping("reviews")
	public ModelAndView getAllReviews(HttpServletRequest request) {
		Long retailerId = (Long) request.getSession().getAttribute("retailerId");
		ModelAndView modelAndView = new ModelAndView();


		if (retailerId == null) {
			modelAndView.addObject("message", "Retailer ID not found in session");
			System.out.println("Retailer ID not found in session.");
			modelAndView.setViewName("redirect:/seller/Dashboard.jsp"); 
			return modelAndView;
		}

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			modelAndView.addObject("message", "Retailer service is currently unavailable.");
			modelAndView.setViewName("redirect:/seller/dashboard"); 
			return modelAndView;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/" + retailerId + "/reviews";


		System.out.println("Requesting URL: " + baseUrl);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		try {
			ResponseEntity<List<ProductReview>> response = restTemplate.exchange(baseUrl, HttpMethod.GET,
					new HttpEntity<>(headers), 
					new ParameterizedTypeReference<List<ProductReview>>() {
					});

			System.out.println("Response Body: " + response.getBody());

			if (response.getStatusCode() == HttpStatus.OK) {
				List<ProductReview> reviews = response.getBody();
				modelAndView.addObject("reviews", reviews); 
				modelAndView.setViewName("/seller/ProductReviews.jsp"); 
			} else {
				request.getSession().setAttribute("message", "No reviews found for this retailer.");
				modelAndView.setViewName("redirect:/seller/ProductReviews.jsp"); 

			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "Error fetching reviews: " + e.getMessage());
			modelAndView.setViewName("redirect:/seller/ProductReviews.jsp"); 
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An unexpected error occurred.");
			modelAndView.setViewName("redirect:/seller/ProductReviews.jsp"); 
		}

		return modelAndView; 
	}

	@RequestMapping("deleteReview")
	public ModelAndView deleteReview(@RequestParam("id") Long reviewId, HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();

		Long retailerId = (Long) request.getSession().getAttribute("retailerId");
		
		if (retailerId == null) {
			request.getSession().setAttribute("message", "Retailer ID not found in session");
			mv.setViewName("redirect:/seller/dashboard");
			return mv;
		}

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			request.getSession().setAttribute("message", "Retailer service is currently unavailable.");
			mv.setViewName("redirect:/seller/dashboard");
			return mv;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/" + retailerId + "/reviews/" + reviewId;

		RestTemplate restTemplate = new RestTemplate();

		try {

			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.DELETE, null, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				request.getSession().setAttribute("message", "Review deleted successfully.");
				mv.setViewName("redirect:/seller/dashboard"); 
			} else {
				request.getSession().setAttribute("message", "Failed to delete review: " + response.getBody());
				mv.setViewName("redirect:/seller/dashboard"); 
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace(); 
			request.getSession().setAttribute("message", "Error deleting review: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An unexpected error occurred while deleting the review.");
		}

		mv.setViewName("redirect:/reviews"); 
		return mv;
	}

	@RequestMapping("/orders")
	public ModelAndView getAllOrders(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();

		Long retailerId = (Long) request.getSession().getAttribute("retailerId");

		if (retailerId == null) {
			modelAndView.addObject("message", "Retailer ID not found in session");
			System.out.println("Retailer ID not found in session.");
			modelAndView.setViewName("redirect:/seller/dashboard"); 
			return modelAndView;
		}

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			modelAndView.addObject("message", "Retailer service is currently unavailable.");
			modelAndView.setViewName("redirect:/seller/dashboard"); 
			return modelAndView;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/orders/" + retailerId;


		System.out.println("Requesting URL: " + baseUrl);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		try {
			ResponseEntity<List<Order>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Order>>() {
					});

			System.out.println("Response Body: " + response.getBody());

			if (response.getStatusCode() == HttpStatus.OK) {
				List<Order> orders = response.getBody();
				modelAndView.addObject("orders", orders); 
				modelAndView.setViewName("/seller/Orders.jsp"); 
			} else {
				request.getSession().setAttribute("message", "No orders found for this retailer.");
				modelAndView.setViewName("redirect:/seller/dashboard"); 
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "Error fetching orders: " + e.getMessage());
			modelAndView.setViewName("redirect:/seller/Orders.jsp"); 
			} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An unexpected error occurred.");
			modelAndView.setViewName("redirect:/seller/Orders.jsp"); 
		}

		return modelAndView; 
	}

	@RequestMapping("updateOrderStatus")
	public ModelAndView updateOrderStatus(@RequestParam("orderId") Long orderId,
			@RequestParam("newStatus") String newStatus, HttpServletRequest request) {
		ModelAndView mv = new ModelAndView();
		Long retailerId = (Long) request.getSession().getAttribute("retailerId");

		
		if (retailerId == null) {
			request.getSession().setAttribute("message", "Retailer ID not found in session");
			mv.setViewName("redirect:/seller/dashboard");
			return mv;
		}


		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			request.getSession().setAttribute("message", "Retailer service is currently unavailable.");
			mv.setViewName("redirect:/seller/dashboard");
			return mv;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/orders/" + orderId + "/status";

		String jsonBody = newStatus; 

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

		try {

			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				request.getSession().setAttribute("message", "Order status updated successfully.");
			} else {
				request.getSession().setAttribute("message", "Failed to update order status: " + response.getBody());
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "Error updating order status: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message",
					"An unexpected error occurred while updating the order status.");
		}

		mv.setViewName("redirect:/orders"); 
		return mv;
	}

	@RequestMapping("/products")
	public ModelAndView getAllProducts(HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();
		Long retailerId = (Long) request.getSession().getAttribute("retailerId");

		if (retailerId == null) {
			modelAndView.addObject("message", "Retailer ID not found in session");
			System.out.println("Retailer ID not found in session.");
			modelAndView.setViewName("redirect:/seller/dashboard"); 
			return modelAndView;
		}

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			modelAndView.addObject("message", "Retailer service is currently unavailable.");
			modelAndView.setViewName("redirect:/seller/dashboard"); 
			return modelAndView;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/retailerproducts/" + retailerId;


		System.out.println("Requesting URL: " + baseUrl);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

		try {
			ResponseEntity<List<Product>> response = restTemplate.exchange(baseUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Product>>() {
					});

			System.out.println("Response Body: " + response.getBody());

			if (response.getStatusCode() == HttpStatus.OK) {
				List<Product> products = response.getBody();
				modelAndView.addObject("products", products); 
				modelAndView.setViewName("seller/MyProducts.jsp"); 
			} else {
				request.getSession().setAttribute("message", "No products found for this retailer.");
				modelAndView.setViewName("redirect:/seller/dashboard"); 
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "Error fetching products: " + e.getMessage());
			modelAndView.setViewName("redirect:/seller/MyProducts.jsp");
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An unexpected error occurred.");
			modelAndView.setViewName("redirect:/seller/MyProducts.jsp"); 
		}

		return modelAndView; 
		
	}

	@RequestMapping("/deleteProduct")
	public ModelAndView deleteProduct(@RequestParam("productId") Long productId, HttpServletRequest request) {
		ModelAndView modelAndView = new ModelAndView();

		Long retailerId = (Long) request.getSession().getAttribute("retailerId");

		if (retailerId == null) {
			request.getSession().setAttribute("message", "Retailer ID not found in session");
			modelAndView.setViewName("redirect:/seller/dashboard");
			return modelAndView;
		}


		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		if (instances.isEmpty()) {
			request.getSession().setAttribute("message", "Retailer service is currently unavailable.");
			modelAndView.setViewName("redirect:/seller/dashboard");
			return modelAndView;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/delete/" + productId;

		RestTemplate restTemplate = new RestTemplate();

		try {

			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.DELETE, null, String.class);

			if (response.getStatusCode() == HttpStatus.OK) {
				request.getSession().setAttribute("message", "Product deleted successfully.");
				modelAndView.setViewName("redirect:/seller/dashboard");
			} else {
				request.getSession().setAttribute("message", "Failed to delete product: " + response.getBody());
				modelAndView.setViewName("redirect:/seller/dashboard"); 
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "Error deleting product: " + e.getResponseBodyAsString());
		} catch (Exception e) {
			e.printStackTrace();
			request.getSession().setAttribute("message", "An unexpected error occurred while deleting the product.");
		}

		modelAndView.setViewName("redirect:/products"); 
		return modelAndView;
	}

	@RequestMapping(value = "/addProduct", method = RequestMethod.POST)
	public ModelAndView addProduct(@RequestParam("productName") String productName,
			@RequestParam("description") String description, @RequestParam("price") double price,
			@RequestParam("category") String category, @RequestParam("stockQuantity") int stockQuantity,
			@RequestParam("imageFile") MultipartFile imageFile, HttpServletRequest request) {

		System.out.println("Product Name: " + productName);
		System.out.println("Description: " + description);
		System.out.println("Price: " + price);
		System.out.println("Stock Quantity: " + stockQuantity);
		System.out.println("Image File: " + imageFile.getOriginalFilename());

		ModelAndView mv = new ModelAndView();

		HttpSession session = request.getSession();
		Long retailerid = (Long) session.getAttribute("retailerId");

		if (retailerid == null) {
			mv.setViewName("redirect:/seller/login.jsp"); 
			session.setAttribute("message", "Please log in to add a product.");
			return mv;
		}

		Product product = new Product();
		product.setProductName(productName);
		product.setDescription(description);
		product.setPrice(price);
		product.setStockQuantity(stockQuantity);
		product.setCategory(category);
		product.setRetailerId(retailerid); 


		String fileName = imageFile.getOriginalFilename();
		try {

			String filePath = UPLOAD_DIR + "/" + fileName;
			imageFile.transferTo(new File(filePath));


			product.setImage(fileName);

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", "Error uploading image. Please try again.");
			mv.setViewName("redirect:/seller/AddNewProduct.jsp");
			return mv;
		}


		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");

		if (instances == null || instances.isEmpty()) {
			mv.setViewName("redirect:/seller/AddNewProduct.jsp");
			session.setAttribute("message", "Service unavailable. Please try again later.");
			return mv;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/" + retailerid + "/addproducts";

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Product> entity = new HttpEntity<>(product, headers);

		try {

			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.POST, entity, String.class);
			String responseBody = response.getBody();

			System.out.println("Response Body: " + response.getBody());


			if (response.getStatusCode() == HttpStatus.OK && responseBody.contains("Product added successfully")) {
				session.setAttribute("message", "Product added successfully!");
				mv.setViewName("redirect:/seller/dashboard"); 
			} else {
				session.setAttribute("message", "Failed to add the product. Please try again.");
				mv.setViewName("redirect:/seller/AddNewProduct.jsp");
			}

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", "An error occurred while adding the product.");
			mv.setViewName("redirect:/seller/AddNewProduct.jsp");
		}

		return mv;
	}

	@RequestMapping(value = "/updateProduct", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView updateProduct(@RequestParam("productId") Long productId,
			@RequestParam("name") String productName, @RequestParam("description") String description,
			@RequestParam("price") double price, @RequestParam("category") String category,
			@RequestParam("stockQuantity") int stockQuantity,
			@RequestParam(value = "imageFile", required = false) MultipartFile imageFile, HttpServletRequest request) {

		if (imageFile != null) {
			System.out.println("Image File: " + imageFile.getOriginalFilename());
		}

		ModelAndView mv = new ModelAndView();

		HttpSession session = request.getSession();
		Long retailerId = (Long) session.getAttribute("retailerId");

		if (retailerId == null) {
			mv.setViewName("redirect:/seller/login.jsp"); 
			session.setAttribute("message", "Please log in to update the product.");
			return mv;
		}


		Product product = new Product();
		product.setProductName(productName);
		product.setDescription(description);
		product.setPrice(price);
		product.setStockQuantity(stockQuantity);
		product.setCategory(category);
		product.setRetailerId(retailerId); 


		if (imageFile != null && !imageFile.isEmpty()) {
			String fileName = imageFile.getOriginalFilename();
			try {
		
				String filePath = UPLOAD_DIR + "/" + fileName;
				imageFile.transferTo(new File(filePath));
				product.setImage(fileName);

			} catch (Exception e) {
				e.printStackTrace();
				session.setAttribute("message", "Error uploading image. Please try again.");
				mv.setViewName("redirect:/seller/EditProduct.jsp");
				return mv;
			}
		}

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");

		if (instances == null || instances.isEmpty()) {
			mv.setViewName("redirect:/seller/EditProduct.jsp");
			session.setAttribute("message", "Service unavailable. Please try again later.");
			return mv;
		}

		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/" + retailerId + "/updateProduct/"
				+ productId;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		HttpEntity<Product> entity = new HttpEntity<>(product, headers);

		try {

			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String.class);
			String responseBody = response.getBody();

			System.out.println("Response Body: " + responseBody);


			if (response.getStatusCode() == HttpStatus.OK && responseBody.contains("Product updated successfully")) {
				session.setAttribute("message", "Product updated successfully!");
				mv.setViewName("redirect:/products"); 
			} else {
				session.setAttribute("message", "Failed to update the product. Please try again.");
				mv.setViewName("redirect:/seller/EditProduct.jsp");
			}

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", "An error occurred while updating the product.");
			mv.setViewName("redirect:/seller/EditProduct.jsp");
		}

		return mv;
	}

	
	@RequestMapping(value = "/manage-inventory", method = { RequestMethod.POST, RequestMethod.GET })
	public ModelAndView manageInventory(@RequestParam("productId") Long productId,
			@RequestParam("newStockQuantity") int newStockQuantity, HttpServletRequest request) {

		List<ServiceInstance> instances = discoveryClient.getInstances("RETAILERSERVICE");
		ServiceInstance serviceInstance = instances.get(0);
		String baseUrl = "http://localhost:8181/retailer/manage-inventory/" + productId;

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Integer> entity = new HttpEntity<>(newStockQuantity, headers);

		ModelAndView modelAndView = new ModelAndView();

		try {
			ResponseEntity<String> response = restTemplate.exchange(baseUrl, HttpMethod.PUT, entity, String.class);

			System.out.println("Response Body: " + response.getBody());

			if (response.getStatusCode() == HttpStatus.OK) {
				modelAndView.addObject("message", "Inventory managed successfully.");
			} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
				modelAndView.addObject("message", "Product not found.");
			} else {
				modelAndView.addObject("message", "Failed to manage inventory.");
			}
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			modelAndView.addObject("message", "An error occurred while managing inventory: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			modelAndView.addObject("message", "An unexpected error occurred.");
		}
		modelAndView.setViewName("redirect:/products");
		return modelAndView;
	}

	@RequestMapping("logout")
	public String logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false); 
		if (session != null) {
			session.invalidate(); 
		}
		return "redirect:/seller/mainpage.jsp"; 
	}
}