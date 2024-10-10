package com.revshop.buyerservice.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.revshop.buyerservice.dto.CartItemDTO;
import com.revshop.buyerservice.dto.ProductDTO;
import com.revshop.buyerservice.dto.ProductReviewDto;
import com.revshop.buyerservice.entity.Buyer;
import com.revshop.buyerservice.entity.Cart;
import com.revshop.buyerservice.entity.CartItem;
import com.revshop.buyerservice.entity.FavoriteProduct;
import com.revshop.buyerservice.entity.Order;
import com.revshop.buyerservice.entity.Product;
import com.revshop.buyerservice.entity.ProductReview;
import com.revshop.buyerservice.entity.Retailer;
import com.revshop.buyerservice.exception.ReviewNotFoundException;
import com.revshop.buyerservice.service.BuyerService;

import jakarta.transaction.Transactional;

@RestController
@CrossOrigin("http://localhost:8084/")
@RequestMapping("/api/buyers")
@Transactional
public class BuyerController {

    @Autowired
    private BuyerService buyerService;
    
    @PostMapping("/register")
    public ResponseEntity<String> registerBuyer(@RequestBody Buyer buyer) {
        int result = buyerService.registerBuyer(buyer);

        if (result == 1) {
            return ResponseEntity.status(HttpStatus.CREATED).body("Buyer registered successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Buyer> loginBuyer(@RequestBody Buyer buyer) {
        try {
            Buyer loggedInBuyer = buyerService.loginBuyer(buyer);
            return ResponseEntity.ok(loggedInBuyer);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); 
        }
    }

    @GetMapping("/AllProducts")
    public ResponseEntity<List<ProductDTO>> buyerHomepage() {
        List<Product> products = buyerService.getAllProducts();

        if (products.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<ProductDTO> response = products.stream().map(product -> {
            ProductDTO simplifiedProduct = new ProductDTO();
            simplifiedProduct.setProductId(product.getProductId());
            simplifiedProduct.setImage(product.getImage());
            simplifiedProduct.setProductName(product.getProductName());
            simplifiedProduct.setDescription(product.getDescription());
            simplifiedProduct.setPrice(product.getPrice());

            List<ProductReviewDto> reviewDTOs = product.getReviews().stream().map(review -> {
                ProductReviewDto reviewDTO = new ProductReviewDto();
                reviewDTO.setReviewId(review.getReviewId());
                reviewDTO.setReviewText(review.getReviewText());
                reviewDTO.setRating(review.getRating());
                reviewDTO.setBuyerName(review.getBuyer().getName());
                return reviewDTO;
            }).collect(Collectors.toList());

            simplifiedProduct.setReviews(reviewDTOs);
            return simplifiedProduct;
        }).collect(Collectors.toList());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        Product product = buyerService.getProductById(productId);

        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(product.getProductId());
        productDTO.setImage(product.getImage());
        productDTO.setProductName(product.getProductName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());

        List<ProductReviewDto> reviewDTOs = product.getReviews().stream().map(review -> {
            ProductReviewDto reviewDTO = new ProductReviewDto();
            reviewDTO.setReviewId(review.getReviewId());
            reviewDTO.setReviewText(review.getReviewText());
            reviewDTO.setRating(review.getRating());

 
            if (review.getBuyer() != null) {
                reviewDTO.setBuyerName(review.getBuyer().getName());
            } else {
                reviewDTO.setBuyerName("Anonymous"); 
            }

            if (review.getRetailer() != null) {
                reviewDTO.setRetailerId(review.getRetailer().getRetailerId());
            } else {
                reviewDTO.setRetailerId(null); 
            }

            return reviewDTO;
        }).collect(Collectors.toList());

        productDTO.setReviews(reviewDTOs);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
    
    
    @PostMapping("/add-to-cart")
    public ResponseEntity<String> addToCart(@RequestParam("productId") long productId,
                                            @RequestParam("quantity") int quantity,
                                            @RequestParam("buyerId") Long buyerId) {

        if (quantity <= 0) {
            return new ResponseEntity<>("Quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        Product product = buyerService.getProductById(productId);
        if (product == null) {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }

        Buyer buyer = buyerService.getBuyerById(buyerId);
        if (buyer == null) {
            return new ResponseEntity<>("Buyer not found", HttpStatus.NOT_FOUND);
        }

        buyerService.addToCart(product, quantity, buyerId);
        return new ResponseEntity<>("Item added to cart successfully", HttpStatus.OK);
    }


    @GetMapping("/cart")
    public ResponseEntity<List<CartItemDTO>> viewCart(@RequestParam("buyerId") Long buyerId) {
        Cart cart = buyerService.getCartByBuyerId(buyerId);

        if (cart == null || cart.getCartItems().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<CartItemDTO> cartItemDTOList = cart.getCartItems().stream().map(cartItem -> new CartItemDTO(
            cartItem.getCartItemId(),
            cartItem.getProduct().getProductName(),
            cartItem.getQuantity(),
            cartItem.getProduct().getPrice()
        )).collect(Collectors.toList());

        return new ResponseEntity<>(cartItemDTOList, HttpStatus.OK);
    }

    @PostMapping("/place-order")
    public ResponseEntity<Order> placeOrder(@RequestParam("buyerId") Long buyerId) {
        Cart cart = buyerService.getCartByBuyerId(buyerId);

        if (cart != null && !cart.getCartItems().isEmpty()) {
            Order order = buyerService.placeOrder(cart);
            return new ResponseEntity<>(order, HttpStatus.CREATED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/remove-from-cart")
    public ResponseEntity<String> removeFromCart(@RequestParam("cartItemId") Long cartItemId,
                                                 @RequestParam("buyerId") Long buyerId) {

        Cart cart = buyerService.getCartByBuyerId(buyerId);
        if (cart == null) {
            return new ResponseEntity<>("Cart not found for buyer", HttpStatus.NOT_FOUND);
        }

        Optional<CartItem> cartItemToRemove = cart.getCartItems().stream()
                .filter(item -> item.getCartItemId().equals(cartItemId))
                .findFirst();

        if (!cartItemToRemove.isPresent()) {
            return new ResponseEntity<>("CartItem not found in cart", HttpStatus.NOT_FOUND);
        }

        cart.getCartItems().remove(cartItemToRemove.get());
        buyerService.deleteCartItemById(cartItemId);

        if (cart.getCartItems().isEmpty()) {
            buyerService.deleteCart(cart);
        } else {
            buyerService.saveCart(cart);
        }

        return new ResponseEntity<>("Item removed from cart", HttpStatus.OK);
    }

    @PostMapping("/update-cart")
    public ResponseEntity<String> updateCart(@RequestParam("cartItemId") Long cartItemId, 
                                             @RequestParam("quantity") int quantity, 
                                             @RequestParam("buyerId") Long buyerId) {
        if (quantity <= 0) {
            return new ResponseEntity<>("Quantity must be greater than zero", HttpStatus.BAD_REQUEST);
        }

        Cart cart = buyerService.getCartByBuyerId(buyerId);
        if (cart == null || cart.getCartItems().isEmpty()) {
            return new ResponseEntity<>("Cart is empty or not found", HttpStatus.NOT_FOUND);
        }

        cart.getCartItems().stream()
            .filter(item -> item.getCartItemId().equals(cartItemId))
            .findFirst()
            .ifPresentOrElse(item -> {
                item.setQuantity(quantity);
                buyerService.saveCart(cart);
            }, () -> {
                throw new RuntimeException("Item not found in cart");
            });

        return new ResponseEntity<>("Cart updated successfully", HttpStatus.OK);
    }

    @PostMapping("/add-review")
    public ResponseEntity<String> addProductReview(
            @RequestParam("buyerId") Long buyerId,
            @RequestParam("productId") Long productId,
            @RequestBody ProductReview productReview) {

        Buyer buyer = new Buyer();
        buyer.setBuyerId(buyerId);  
        productReview.setBuyer(buyer); 
        
        Product product = new Product();
        product.setProductId(productId);  
        productReview.setProduct(product);  

        ProductReview savedReview = buyerService.addProductReview(productReview);
        if (savedReview != null) {
            return new ResponseEntity<>("Product review added successfully", HttpStatus.CREATED);
        } else {
        	 throw new ReviewNotFoundException("Failed to add review: Review could not be saved.");
        }
    }


    @PostMapping("/addToFavorites")
    public ResponseEntity<FavoriteProduct> addFavoriteProduct(@RequestParam Long buyerId, @RequestParam Long productId) {
        FavoriteProduct favoriteProduct = buyerService.addFavoriteProduct(buyerId, productId);
        return ResponseEntity.ok(favoriteProduct);
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity<List<FavoriteProduct>> getFavoritesByBuyer(@PathVariable Long buyerId) {
        List<FavoriteProduct> favorites = buyerService.getFavoritesByBuyer(buyerId);
        return ResponseEntity.ok(favorites);
    }
    
    @GetMapping("/past-orders")
    public ResponseEntity<List<Order>> getPastOrders(@RequestParam("buyerId") Long buyerId) {
        List<Order> orders = buyerService.getPastOrdersByBuyerId(buyerId);

        if (orders.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

}
