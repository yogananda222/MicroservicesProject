<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.revshop.revshopClientApp.dto.Product" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="java.util.List" %>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<title>Product Details</title>
	<link rel="stylesheet" href="ProductDetails.css">
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
	<link href="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.1/css/bootstrap.min.css" rel="stylesheet" id="bootstrap-css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.1.1/js/bootstrap.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
<!------ Include the above in your HEAD tag ---------->

<link href="https://fonts.googleapis.com/css?family=Poppins:300,400,500,600,700,800&display=swap" rel="stylesheet">
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.12.1/css/all.min.css">
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/OwlCarousel2/2.3.4/assets/owl.carousel.min.css">
	<link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/OwlCarousel2/2.3.4/assets/owl.theme.default.min.css">
</head>
<body>
<div class="pd-wrap">
    <div class="container">
        <div class="heading-section">
            <h2>Product Details</h2>
        </div>
        <div class="row">
            <div class="col-md-6">
                <div id="slider" class="owl-carousel product-slider">
                    <div class="item">
                        <img src="${pageContext.request.contextPath}/IMAGES/${product.image}" />
                    </div>
                </div>
            </div>
            <div class="col-md-6">
                <div class="product-dtl">
                    <div class="product-info">
                        <div class="product-name">${product.productName}</div>
                        <div class="reviews-counter">
                            <!-- You can add the reviews count here if needed -->
                        </div>
                        <div class="product-price-discount"><span>₹${product.price}</span></div>
                    </div>
                    <p>${product.description}</p>
                    <div class="product-count">
                        <label for="size">Quantity</label>
                        <form id="addToCartForm" method="POST" action="${pageContext.request.contextPath}/addToCart" class="product-quantity-form">
                            <div class="quantity-box">
                                <button type="button" class="qty-control qtyminus">-</button>
                                <input type="text" id="quantity" name="quantity" value="1" class="qty" required>
                                <button type="button" class="qty-control qtyplus">+</button>
                            </div>
                            <input type="hidden" name="productId" value="${product.productId}">
                            <button type="submit" class="round-black-btn add-to-cart-btn">Add to Cart</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

	        
<div class="product-info-tabs">
    <ul class="nav nav-tabs" id="myTab" role="tablist">
        <li class="nav-item">
            <a class="nav-link active" id="description-tab" data-toggle="tab" href="#description" role="tab" aria-controls="description" aria-selected="true">Description</a>
        </li>
        <li class="nav-item">
            <a class="nav-link" id="review-tab" data-toggle="tab" href="#review" role="tab" aria-controls="review" aria-selected="false">Reviews (${product.reviews.size()})</a>
        </li>
    </ul>
    <div class="tab-content" id="myTabContent">
        <div class="tab-pane fade show active" id="description" role="tabpanel" aria-labelledby="description-tab">
            ${product.description}.
        </div>
        <div class="tab-pane fade" id="review" role="tabpanel" aria-labelledby="review-tab">
            <div class="review-heading">REVIEWS</div>
            <c:if test="${not empty product.reviews}">
                <ul style="list-style-type: none; padding-left: 0;">
                    <c:forEach var="review" items="${product.reviews}">
                        <li>
                            <strong><i class='fas fa-user-circle'></i> </strong>${review.buyerName}
                        </li>
                        <li>
                            <strong>Rating: </strong>
                            <span class="review-rating">
                                <c:forEach var="star" begin="1" end="5">
                                    <c:choose>
                                        <c:when test="${star <= review.rating}">
                                            <i class="fa fa-star star" style="color: Gold;"></i> <!-- Filled star -->
                                        </c:when>
                                        <c:otherwise>
                                            <i class="fa fa-star-o star"></i> <!-- Empty star -->
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                            </span>
                        </li>
                        <li>
                            <strong>Review: </strong>${review.reviewText}
                        </li>
                        <hr>
                    </c:forEach>
                </ul>
            </c:if>

            <c:if test="${empty product.reviews}">
                <li class="list-group-item">No reviews yet.</li>
            </c:if>

            <!-- Form for submitting a new review -->
            <form action="${pageContext.request.contextPath}/addProductReview" method="POST" class="review-form">
                <ul style="list-style-type: none; padding-left: 0;">
                    <li>
                        <input type="hidden" name="productId" value="${product.productId}" />
                        <div class="form-group">
                            <label>Your rating</label>
                            <div class="reviews-counter">
                                <div class="rate">
                                    <input type="radio" id="star5" name="rating" value="5" />
                                    <label for="star5" title="5 stars">5 stars</label>
                                    <input type="radio" id="star4" name="rating" value="4" />
                                    <label for="star4" title="4 stars">4 stars</label>
                                    <input type="radio" id="star3" name="rating" value="3" />
                                    <label for="star3" title="3 stars">3 stars</label>
                                    <input type="radio" id="star2" name="rating" value="2" />
                                    <label for="star2" title="2 stars">2 stars</label>
                                    <input type="radio" id="star1" name="rating" value="1" />
                                    <label for="star1" title="1 star">1 star</label>
                                </div>
                            </div>
                        </div>
                    </li>
                    <li>
                        <div class="form-group">
                            <label>Your message</label>
                            <textarea class="form-control" name="reviewText" rows="5"></textarea>
                        </div>
                    </li>
                    <li>
                        <button type="submit" class="round-black-btn">Submit Review</button>
                    </li>
                </ul>
            </form>
            
            <!-- Form for submitting a new review -->
 
        </div>
    </div>
</div>


			
		</div>
	</div>
	<script src="https://cdnjs.cloudflare.com/ajax/libs/OwlCarousel2/2.3.4/owl.carousel.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js" integrity="sha384-ApNbgh9B+Y1QKtv3Rn7W3mgPxhU9K/ScQsAP7hUibX39j7fakFPskvXusvfa0b4Q" crossorigin="anonymous"></script>
	<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js" integrity="	sha384-JZR6Spejh4U02d8jOt6vLEHfe/JQGiRRSQQxSfFWpi1MquVdAyjUar5+76PVCmYl" crossorigin="anonymous"></script>

	<!-- Include the RevShop Client App -->
	<script src="https://cdn.jsdelivr.net/npm/revshop-client-app@1.0.0"></script>

<!-- Include Bootstrap JS and Popper for Bootstrap components -->
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.min.js"></script>
<script src="ProductDetails.js"></script>
</body>
</html>
