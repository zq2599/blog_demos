<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<META HTTP-EQUIV="Expires" CONTENT="0">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Redis追加list</title>
<style>
.importentspan{
    color:#00F;
}
</style>
</head>
<body>
<div>
    机器编号<span class="importentspan">${serviceSource}</span>&nbsp;(${time})
</div>

<div>
    <form method="post" action="${pageContext.request.contextPath}/append_to_list">
        <p>
            KEY&nbsp;&nbsp;:&nbsp;<input type="text" name="key" maxlength="999">
        </p>
        <p>
            VALUE&nbsp;:&nbsp;<input type="text" name="value" maxlength="999">
        </p>
        <input type="Submit" value="提交">
        &nbsp;
        <input type="Reset" value="重置">
    </form>
</div>

</body>
</html>