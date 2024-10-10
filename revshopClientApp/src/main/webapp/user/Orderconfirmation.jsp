<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Order Confirmation</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-5">
  <!--       <h2>Order Confirmation</h2>

        <c:if test="${not empty order}">
            <div class="alert alert-success">
                <p>Thank you for your order! Your order ID is <strong>${order.orderId}</strong>.</p>
                <p>Order Date: <strong>${order.orderDate}</strong></p>
                <p>Total Price: <strong>${order.totalAmount}</strong></p>
            </div> --> 

  <!--           <h3>Order Details:</h3>
            <table class="table">
                <thead>
                    <tr>
                        <th>Product Name</th>
                        <th>Quantity</th>
                        <th>Price</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${order.orderItems}">
                        <tr>
                            <td>${item.productName}</td>
                            <td>${item.quantity}</td>
                            <td>${item.product.price}</td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table> --> 

            <!-- Razorpay Payment Button -->
            <form>
                <script src="https://checkout.razorpay.com/v1/payment-button.js" 
                        data-payment_button_id="pl_P6C9prLtlRt6EX" async></script>
            </form>

        </c:if>

        <c:if test="${empty order}">
            <div class="alert alert-danger">
                <p>Sorry, your order could not be processed. Please try again later.</p>
            </div>
        </c:if>

        <div class="mt-4">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/viewCart">Go to Cart</a>
            <a href="user/Homepage.jsp" class="btn btn-secondary">Continue Shopping</a>
        </div>
    </div>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
