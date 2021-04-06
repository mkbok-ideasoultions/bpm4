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
<body style="overflow-x: hidden;">
	<div id="processRolesArea" style="vertical-align: middle;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr height="30">
				<td width="5" align="left"></td>
				<td width="*" align="left" class="padding15">
					<table width="100%" border="0" cellspacing="0" cellpadding="0"
						class="tableborder">
						<col span="1" width="200" align="center" />
						<col span="1" width="*" align="left" />
						<tr height="20">
							<td bgcolor="#DAE9F9">Name</td>
							<td bgcolor="#DAE9F9">Binding</td>
						</tr>
						<tr height="1">
							<td align="center" colspan="2" height="1" class="bgcolor_head_underline"></td>
						</tr>
						<c:forEach items="${processRoles}" var="processRole">
							<tr height="20">
								<td>${processRole.displayName }</td>
								<td>
									<c:choose>
            							<c:when test="${command.instId ne null }">
	            							<input type="button" value="change" onclick="showRoleMappingChangeForm('${processRole.name }', '${processRole.endPoint }')">
		                        			${processRole.roleMapping }
            							</c:when>
            						</c:choose>
	                    		</td>
							</tr>
							<tr height="1">
								<td colspan="2" height="1" class="bgcolor_head_underline"></td>
							</tr>
						</c:forEach>
						<c:if test="${empty processRoles } ">
							<tr height="20">
								<td align="left" colspan="2">No Roles declared.</td>
							</tr>
							<tr height="1">
								<td colspan="2" height="1" class="bgcolor_head_underline"></td>
							</tr>
						</c:if>
					</table>
				</td>
			</tr>
		</table>
	</div>
<script>

	function showRoleMappingChangeForm(roleName, oldValue){
	    window.open("roleMappingChangeForm?multiple=true&instanceId=${command.instId }&roleName=" + roleName + "&oldValue=" + oldValue, "roleMapping", "width=700,height=500,scrollbars=yes,resizable=yes");
	}
</script>
</body>
</html>