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
	<form:form name="definitionForm" id="definitionForm" action="" method="post" target="procVarsFrame">
		<form:input path="comCode" type="hidden" id="comCode" name="comCode" value="${sessionScope.loggedUser.comCode}" /> 
		<form:input path="defId" type="hidden" id="defId" name="defId" value="${command.defId}" />
		<form:input path="instId" type="hidden" id="instId" name="instId" value="${command.instId}" />
		<form:input path="defVerId" type="hidden" id="defVerId" name="defVerId" value="${command.defVerId}" />
	</form:form>
<div id="processVariablesArea" style="vertical-align: middle;">
	<table width="100%" border="0" cellspacing="0" cellpadding="0">
		<tr height="30" >
            <td width="5" align="left"></td>
            <td width="*" align="left"  class="padding15"><table width="100%" border="0" cellspacing="0" cellpadding="0" class="tableborder">
        <tr height="20" >
            <td width="200" align="center" bgcolor="#DAE9F9">Name/Type</td>
            <td width="*" align="left" bgcolor="#DAE9F9">Value</td>
        </tr>
        <tr height="1">
            <td colspan="3" height="1" class="bgcolor_head_underline"></td>
        </tr>
        <c:forEach items="${processVariables}" var="processVariable">
        	<tr height="20" >
				<td width="200" align="center">${processVariable.varDisplayName}/${processVariable.varType}</td>
            	<td width="*" align="left">
                	<input type="button" value="XML" onclick="viewProcessVariableInXML('${processVariable.varName}')">
                	<input type="button" value="change" onclick="showProcessVariableChangeForm('${processVariable.varName}' ,'${processVariable.varType eq null ? '' : processVariable.varType}')">
            		<c:choose>
            			<c:when test="${processVariable.varFormContextFilePath ne 'null'}">
            				<a id="showFormInstanceBtn" href="javascript:showFormInstance('${processVariable.varName}', '${processVariable.varFormContextFilePath }');" target="_blank">${processVariable.varFormContextFilePath }</a>
            			</c:when>
            			<c:otherwise>
            				<c:set var="data" value="${processVariable.data }" />
            				<c:out value="${data }" escapeXml="false" />
            			</c:otherwise>
            		</c:choose>
                </td>
        	</tr>
		</c:forEach>
	</table>
</div>

<form name="showFormInstanceForm" id="showFormInstanceForm" action="" method="post">
	<input type="hidden" id="instanceId" name="instanceId" value="${command.instId}" />
	<input type="hidden" id="variableName" name="variableName" value="" />
	<input type="hidden" id="filePath" name="filePath" value="" /> 
</form>

<script>
	function viewProcessVariableInXML(variableName){
		window.open("viewProcessVariableInXML/${command.instId}/" + variableName, "processVariable", "width=700,height=500,scrollbars=yes,resizable=yes").focus();
	}
	
	function showProcessVariableChangeForm(variableName, variableType){
		window.open("processVariableChangeForm/${command.instId}/" + variableName + "/" + variableType, "processVariable", "width=700,height=500,scrollbars=yes,resizable=yes").focus();
	}
	
	function showFormInstance(processVariable, filePath) {
		$("#variableName").attr("value", processVariable);
		$("#filePath").attr("value", filePath);
		$("#showFormInstanceForm").attr("target","_blank");
		$("#showFormInstanceForm").attr("action","<c:url value='/processmanager/showFormInstance.do' />");
		$("#showFormInstanceForm").submit();
	}
</script>
</body>
</html>