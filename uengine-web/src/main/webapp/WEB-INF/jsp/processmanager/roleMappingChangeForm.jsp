<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<%@ include
	file="/WEB-INF/include/include-flowchart_header_resource.jspf"%>
<title>Process Manager</title>
</head>
<body onload="init()">
<form name="mainForm">
	<script type="text/javascript" src="<c:url value='/resources/js/tabs.js'/>"></script>
	<script type="text/javascript">
		var tmp = new Array(
				"Select User"
		);
		createTabs(tmp);
	</script>
	<br>
	<c:import url="/WEB-INF/jsp/organization/popup/organizationChart.jsp"></c:import>
</form>

<form name="roleMappingChangeForm" action="roleMappingChange.jsp" method="POST">
	<input type="hidden" name="endpoint" value="">
	<input type="hidden" value="${instanceId }" name="instanceId">
	<input type="hidden" value="${roleName }" name="roleName">
	<input type="hidden" value="" name="orgRoleName">
	<div align="center">
	<br /><br />
	<table>
	    <tr>
			<td class="gBtn">
	            <a onclick="changeRoleMapping();">
	            	<%-- <span><%=GlobalContext.getMessageForWeb("Ok", loggedUserLocale) %></span> --%>
	            	<span>ok</span>
	            </a>
	            <a onclick="window.close();" >
	            	<%-- <span><%=GlobalContext.getMessageForWeb("Cancel", loggedUserLocale) %></span> --%>
	            	<span>Cancle</span>
	            </a>
			</td>
	    </tr>
	</table>
	</div>
</form>
<br><br>
<script>
	<%-- var WEB_ROOT = "<%=org.uengine.kernel.GlobalContext.WEB_CONTEXT_ROOT%>"; --%>
	
	function changeRoleMapping() {
		var selectedUserList = document.mainForm.selectUserList;
		var returnStr = "";
	
		var endpoint = document.getElementsByName("endpoint")[0];
		var roleName = document.getElementsByName("roleName")[0];
		
		if (selectedUserList != undefined && selectedUserList.options.length != 0) {
			for (i=0; i<selectedUserList.options.length; i++) {
				var userInfo = selectedUserList.options[i].value;
	
				var userInfoArray = userInfo.split(';');		
				var userId = userInfoArray[0].split('=');
				
				returnStr += userId[1];
				if (i != selectedUserList.options.length -1)
					returnStr += ";";
			}
			
			endpoint.value = returnStr;
		} else {
			if (selectedOrganizationList.length > 0) {
	            endpoint.value = selectedOrganizationList[0].id;
	        }
		}
		
		if (endpoint.value == "") {
			alert("Select User");
		} else {
			if (roleName.value.match(/TRCTAG\[[0-9]{1,}\]:.*/gi) != null) {
				var orgRoleName = roleName.value.replace(/TRCTAG\[[0-9]{1,}\]:/gi, "");
				if (confirm("Change " + orgRoleName + " role, too?")) {
					document.getElementsByName("orgRoleName")[0].value = orgRoleName;
				}
			} 
			document.roleMappingChangeForm.submit();
		}
	}
	
	function init() {
		var roleName = document.getElementsByName("roleName")[0];
		if (roleName.value.toLowerCase() === "initiator") {
			alert("Can't change initiator");
			window.close();
		}
	}
	
	function onOk(selectedOrganizationList, opnerInputName) {
	}
</script>
</body>
</html>