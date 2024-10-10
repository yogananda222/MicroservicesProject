<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="java.util.List" %>
<%@ page import="com.revshop.revshopClientApp.dto.CartItemDTO" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Shopping Cart</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-5">
        <h2>Your Shopping Cart</h2>

        <!-- Display message if any -->
        <%
            String message = (String) request.getAttribute("message");
            if (message != null) {
        %>
            <div class="alert alert-info">
                <%= message %>
            </div>
        <%
            }
        %>

        <!-- Cart Table -->
        <%
            List<CartItemDTO> cartItems = (List<CartItemDTO>) request.getAttribute("cartItems");
            if (cartItems != null && !cartItems.isEmpty()) {
        %>
            <table class="table table-bordered">
                <thead>
                    <tr>
                        <th>Product Name</th>
                        <th>Price</th>
                        <th>Quantity</th>
                        <th>Total</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (CartItemDTO item : cartItems) {
                    %>
                    <tr>
                        <!-- Product Name -->
                        <td><%= item.getProductName() %></td>

                        <!-- Product Price -->
                        <td><fmt:formatNumber value="<%= item.getPrice() %>" type="currency" /></td>

                        <!-- Quantity with Update Form -->
                        <td>
                            <form action="${pageContext.request.contextPath}/update-cart" method="post" class="form-inline">
                                <input type="hidden" name="cartItemId" value="<%= item.getCartItemId() %>">
                                <input type="number" name="quantity" value="<%= item.getQuantity() %>" min="1" class="form-control">
                                <input type="hidden" name="buyerId" value="<%= session.getAttribute("buyerId") %>">
                                <button type="submit" class="btn btn-primary ml-2">Update</button>
                            </form>
                        </td>

                        <!-- Total Price for the Cart Item -->
                        <td><fmt:formatNumber value="<%= item.getQuantity() * item.getPrice() %>" type="currency" /></td>

                        <!-- Remove Item Form -->
                        <td>
                            <form action="${pageContext.request.contextPath}/removefromcart" method="post">
                                <input type="hidden" name="cartItemId" value="<%= item.getCartItemId() %>">
                                <input type="hidden" name="buyerId" value="<%= session.getAttribute("buyerId") %>">
                                <button type="submit" class="btn btn-danger">Remove</button>
                            </form>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        <%
            } else {
        %>
            <p>No items in the cart.</p>
        <%
            }
        %>
        
        
        <a href="${pageContext.request.contextPath}/user/checkout.jsp"><button type="button" class="btn btn-primary">Continue Shopping</button></a>
        

        <!-- Place Order Button -->
        <%
            if (cartItems != null && !cartItems.isEmpty()) {
        %>
            <form action="${pageContext.request.contextPath}/place-order" method="post">
                <input type="hidden" name="buyerId" value="<%= session.getAttribute("buyerId") %>">
                <button type="submit" class="btn btn-success">Place Order</button>
            </form>
        <%
            }
        %>
    </div>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
