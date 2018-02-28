<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html lang="es">
    <body>
    Select a bucket: 
    <form name="bucket" method="get" action="call_analytics">
    <select name="bucket"> 
        <c:forEach items="${bucketList}" var="bucket">
             <option value="${bucket.name}">${bucket.name}</option>
        </c:forEach>
        </select>
        <input type="submit">
        </form>
    </body>
</html>