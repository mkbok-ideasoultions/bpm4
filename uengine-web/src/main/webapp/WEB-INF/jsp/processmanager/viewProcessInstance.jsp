<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<script>
	var resizeFrameHeight = function() {
		window.parent.parent.resizeFrames();
	}
	
	var orgTreeData;
	var definitionObjectArray;
	$(document).ready(function(){
		
		//검색창 숨기기
		parent.$("#content-middle").hide();
		
		//프로세스 인스턴스 start, stop후 뒤로가기 방지
		if("${statusChanged}" === "true") {
			var curInstStatus = "stopped";
			if("${command.status}".toLowerCase() === "running") {
				curInstStatus = "started";
			}
			alert("Process instance \"${command.instId}\" has been successfully " + curInstStatus);
			history.pushState(null, null, location.href);
			window.onpopstate = function(event) {
			    history.go(1);
			}
		}
		
		$("#tabs").tabs();
		
		$("#instanceForm").submit();
		
		$("#flowchartFrame").load(resizeFrameHeight);
	});
	
	
</script>
<title>Process Manager</title>
</head>
<body style="overflow: hidden;">
	<form:form name="instanceForm" id="instanceForm" action="/bpm/processmanager/viewProcessInstanceFlowChart.do" method="post" target="flowchartFrame">
		<form:input path="comCode" type="hidden" id="comCode" name="comCode" value="${sessionScope.loggedUser.comCode}" /> 
		<form:input path="defId" type="hidden" id="defId" name="defId" value="${command.defId}" />
		<form:input path="instId" type="hidden" id="instId" name="instId" value="${command.instId}" />
		<form:input path="defVerId" type="hidden" id="defVerId" name="defVerId" value="${command.defVerId}" />
	<!-- Page Content -->
	<table width="100%" class="table table-reflow">
		<tr>
			<td colspan="2">
				<span id="defTitle" class="glyphicon glyphicon-duplicate"></span>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-bullhorn">인스턴스 정보</span>
			</th>
			<!-- 20190220 skk : 인스턴스 목록가기 버튼 msg.pror추가 확인 -->
			<td>
				<div style="float: right; ">
					<!-- 20190222 skk : 시작, 정지 버튼추가 -->
					<button id="instancePlayBtn" type="button" class="btn btn-info"></button>
				</div>
				<div style="float: right; margin-right: 10px; ">
					<button id="instanceListBtn" type="button" class="btn btn-info">
						인스턴스 목록
					</button>
				</div>
				<div style="float: right; margin-right: 10px; ">
					<button id="definitionChangeBtn" type="button" class="btn btn-info">
						인스턴스 변경
					</button>
				</div>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<div id="tabs">
					<ul>
						<li><a id="flowchartA" href="#flowchartArea"><spring:message code="process.flowchart.label" /></a></li>
						<li><a id="procVarsA" href="#procVarsArea"><spring:message code="process.procvars.label" /></a></li>
						<li><a id="rolesA" href="#rolesArea"><spring:message code="process.roles.label" /></a></li>
					</ul>
					<div id="flowchartArea"><iframe frameborder="0" id="flowchartFrame" name="flowchartFrame" style="width:100%; overflow-x: hidden;"></iframe></div>
					<div id="procVarsArea"><iframe frameborder="0" id="procVarsFrame" name="procVarsFrame" style="width:100%; overflow-x: hidden;"></iframe></div>
					<div id="rolesArea"><iframe frameborder="0" id="rolesFrame" name="rolesFrame" style="width:100%; overflow-x: hidden;"></iframe></div>
				</div>
			</td>
		</tr>
	</table>
		
	</form:form>
	
	<form:form name="instanceListForm" id="instanceListForm" action="" method="post">
		<form:input path="comCode" type="hidden" id="comCode" name="comCode" value="${sessionScope.loggedUser.comCode}" /> 
	</form:form>
	
<script>
	// 20190222 skk
	var setBtnName = function(btnId){
		return function(btnName){
			$(btnId).html(btnName);
		}
	};
	
	// instance를 stop, start할지 결정할 버튼의 이름 구하기
	var instancePlayBtnName = function(){
		var name = "start";
		if("${command.status}".toLowerCase() === "running") {
			name = "stop";
		}
		return name;
	}();
	
	setBtnName("#instancePlayBtn")(instancePlayBtnName);
</script>

<!-- event용 scripts -->
<script>
	$("#definitionChangeBtn").click(function(){
		$("#instanceForm").attr("action","<c:url value='/processmanager/processDesignerInstance.jnlp' />");
	    $("#instanceForm").submit();
	});
	
	$("#instanceListBtn").click(function(){
		$("#instanceListForm").attr("target", "contentsFrame");
		$("#instanceListForm").attr("action","<c:url value='/processmanager/viewInstanceList' />");
		$("#instanceListForm").serialize();
		$("#instanceListForm").submit();
	});
	
	// 20190222 skk
	$("#instancePlayBtn").click(function(){ //name = stop, start
		$("#instanceForm").attr("target", "_self");
		$("#instanceForm").attr("action","<c:url value='/processmanager/put/instance/" + instancePlayBtnName + "' />");
		$("#instanceForm").submit();
	});
	
	$("#flowChartA").click(function(){
		$("#instanceForm").attr("action","<c:url value='/processmanager/viewProcessInstanceFlowChart.do' />");
		$("#instanceForm").attr("target","flowChartFrame");
		$("#instanceForm").submit();
	});
	
	$("#procVarsA").click(function(){
		$("#instanceForm").attr("action","<c:url value='/processmanager/viewProcessInstanceProcVars.do' />");
		$("#instanceForm").attr("target","procVarsFrame");
		$("#instanceForm").submit();
	});
	
	$("#rolesA").click(function(){
		$("#instanceForm").attr("action","<c:url value='/processmanager/viewProcessInstanceRoles.do' />");
		$("#instanceForm").attr("target","rolesFrame");
		$("#instanceForm").submit();
	});
</script>
</body>
</html>