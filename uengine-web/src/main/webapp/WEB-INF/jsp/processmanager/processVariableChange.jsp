<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>

<title>Process Manager</title>
<script>
	top.opener.document.location.reload();
</script>
</head>
<body>
	<p>changed value = ${value }</p><br>
	<p>Value has been successfully changed.</p>
</body>
</html>