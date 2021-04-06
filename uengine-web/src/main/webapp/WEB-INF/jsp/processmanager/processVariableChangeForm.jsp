<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>

<title>Process Manager</title>
</head>
<body>
<form name="processVariableChangeForm" id="processVariableChangeForm" action="processVariableChange" method="POST">
<input type="hidden" value="${instanceId}" name="instanceId"/>
<input type="hidden" value="${variableName}" name="variableName"/>
<input type="hidden" value="${dataClassName}" name="dataClassName"/>

<table><tr><td bgcolor="black">
<table cellspacing="1">
<c:forEach items="${command}" var="command">
	<tr>
		<td bgcolor=#aaaaff>${command.name} </td>
    	<td bgcolor=white>${command.inputterHtml}</td>
   	</tr>
</c:forEach>

</table>
</td></tr></table>
<button id="changeFormBtn" name="changeFormBtn">제출</button>
</form>
<script>
	$("#changeFormBtn").click(function() {
		$("#processVariableChangeForm").attr("action","<c:url value='/processmanager/processVariableChange' />");
		$("#processVariableChangeForm").submit();
	});
</script>
</body>
</html>