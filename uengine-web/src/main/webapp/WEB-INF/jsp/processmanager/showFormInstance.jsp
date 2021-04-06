<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<%@ include file="/WEB-INF/include/include-flowchart_header_resource.jspf"%>

<title>Process Manager</title>
</head>
<body style="overflow-x: hidden;">
	<form id="formInstanceChangeForm" action="/processmanager/formInstanceChange.do" method="POST" target="_blank">
	<textarea id="contents" name="contents" cols="300" rows="50">${value }</textarea>
	<input id="instanceId" name="instanceId" value="${instanceId }">
	<input id="variableName" name="variableName" value="${variableName }">
	</form>
	<button id="formInstanceChangeBtn" name="formInstanceChangeBtn">제출</button>
	<script>
		$("#formInstanceChangeBtn").click(function() {
			$("#formInstanceChangeForm").attr("action","<c:url value='/processmanager/formInstanceChange.do' />");
			$("#formInstanceChangeForm").submit();
		});
	</script>
</body>
</html>