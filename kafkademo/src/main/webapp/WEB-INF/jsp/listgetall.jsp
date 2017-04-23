<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Redis读测试</title>
<style>
.importentspan{
    color:#00F;
}
</style>
</head>
<body>键[${key}]对应的值从list中成功取出，第一个值[${value}]，机器编号<span class="importentspan">${serviceSource}</span>${time}

<div>详细数据如下:</div>
<c:forEach var="item" items="${list}">
         <div>${item}</div>
</c:forEach>

</body>
</html>