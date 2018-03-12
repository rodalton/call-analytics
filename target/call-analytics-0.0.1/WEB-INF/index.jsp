<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Call Analytics with Watson</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<link rel="stylesheet" href="style.css" />
</head>
<body>
	<table>
		<tr>
			<td style='width: 30%;'><img class="newappIcon"
				src='images/newapp-icon.png'></td>
			<td>
				<h1>Call Analytics with Watson</h1>

				<p class='description'></p> Select an IBM COS bucket: <br>
				<form name="bucket" method="get" action="call_analytics">
					<select name="bucket">
						<c:forEach items="${bucketList}" var="bucket">
							<option value="${bucket.name}">${bucket.name}</option>
						</c:forEach>
					</select>
					<p></p>
					<input type="submit" value="Run call analytics">
				</form>
			</td>
		</tr>
	</table>
</body>
</html>
