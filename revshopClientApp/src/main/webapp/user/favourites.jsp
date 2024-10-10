<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Favorites</title>
    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-5">
        <h2>Your Favorite Products</h2>

        <c:if test="${not empty favorites}">
            <ul class="list-group">
                <c:forEach var="favorite" items="${favorites}">
                    <li class="list-group-item">
                        ${favorite.product.name} - 
                        <fmt:formatNumber value="${favorite.product.price}" type="currency" />
                        <a href="/productDetails?productId=${favorite.product.productId}" class="btn btn-info btn-sm float-right">View Product</a>
                    </li>
                </c:forEach>
            </ul>
        </c:if>

        <c:if test="${empty favorites}">
            <p>You don't have any favorite products yet.</p>
        </c:if>
    </div>

    <script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@4.5.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
