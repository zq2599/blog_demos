<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>kafka启动订阅</title>
<style>
.importentspan{
    color:#00F;
}
</style>
</head>
<body>

<jsp:include page="common.jsp" flush="true"/>

<div>
    <form method="post" action="${pageContext.request.contextPath}/poststart">
        <p>
            Topic&nbsp;&nbsp;:&nbsp;<input type="text" name="topic" maxlength="999">
        </p>

        <input type="Submit" value="提交">
        &nbsp;
        <input type="Reset" value="重置">
    </form>
</div>

</body>
</html>