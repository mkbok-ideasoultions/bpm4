<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<script type="text/javascript">
	$(document).ready(function() {
		var code = $("#code").val();
		getEmpDetailInfo();
	});
	
	//사원 상세 정보 조회 함수
	getEmpDetailInfo = function() {
		$.ajax({
	        url: "<c:url value='/organization/ajax/getEmpDetailInfo.do?code=' />" +$("#code").val(),
	        type: "POST",
	        dataType : "json",
	        cache : false
			//data : { code : $("#code").val() },
	    }).then(function(data) {
	    	for(var i=0; i < data.length; i++) {
	    		$("#userCode").html(data[i].empCode);
	    		$("#empCode").val(data[i].empCode);
	    		$("#isAdmin").val(data[i].isAdmin);
	    		$("#empName").val(data[i].empName); 
	    		$("#pw").val(data[i].password);
	    		$("#email").val(data[i].email);
	    		$("#jikName").val(data[i].jikName);
	    		$("#mobileNo").val(data[i].mobileNo);
	    		$("#globalCom").val(data[i].globalCom);
	    		$("#nateOn").val(data[i].nateOn);
	    		$("#msn").val(data[i].msn);
	    		$("#locale").val(data[i].locale);
	    		if( data[i].isAdmin != '0'){
	    			$("#adminChk").attr("checked",true);	
	    		}
	    		if( data[i].locale == 'ko') {
	    			$("#ko").attr("selected",true);
	    		} else {
	    			$("#en").attr("selected",true);
	    		}
	    		$("#titleOfEmp").html(" " + data[i].empName + " Information");
	    	}
	    });
	}
	
	//사원 정보 변경 함수
	updateEmpInfo = function() {
		$.ajax({
	        url: "<c:url value='/organization/ajax/updateEmpInfo.do' />",
	        type: "POST",
	        dataType : "json",
	        cache : false,
			data : $("#empInfoForm").serialize(),
	    }).then(function(data) {
	    	if(data == true) {
	    		alert("수정이 성공하였습니다.");
	    		location.href("<c:url value='/organization/viewEmpDetailInfo.do?code=' />" + $("#code").val());
	    	} else {
	    		alert("수정이 실패 하였습니다.");
	    	}
	    });
	}
	
	changeChkVal = function(){
		if($("#adminChk").is(":checked")){
			$("#isAdmin").val(1);
		} else {
			$("#isAdmin").val(0);
		}
	}
</script>
</head>
<body>
<form:form name="organizationForm" id="organizationForm" action="" method="post" target="contentsFrame">
	<form:input path="code" type="hidden" id="code" name="code" value="${code}" />
	<form:input path="parent_Partcode" type="hidden" id="parent_Partcode" name="parent_Partcode" value="${parent_Partcode}" /> 
</form:form>
<form id="empInfoForm" name="empInfoForm" action="" method="post">
<input type="hidden" id="empCode" name="empCode" value="">
<input type="hidden" id="isAdmin" name="isAdmin" value="">
<div id="content-middle" class="content-middle" style="width : 100%;">
	<div id="" class="panel-group">
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfEmp" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table class="table table-reflow" width="100%" style="border-bottom: 1px solid #ddd; margin: 0px;">
					<tr>
						<td rowspan="6" style="width: 150px;">
							<img src="<c:url value='/resources/images/portraits/unknown_user.gif'/>" width="150" height="200" />
						</td>
						<td class="active">아이디</td>
						<td id="userCode" name="userCode"></td>
						<td class="active">관리자</td>
						<td>
							<input type="checkbox" id="adminChk" name="adminChk" value="" onchange="changeChkVal();"> 
						</td>
					</tr>
					<tr>
						<td class="active">이름</td>
						<td >
							<input type="text" id="empName" name="empName" value="">
						</td>
						<td class="active">암호</td>
						<td>
							<input type="password" id="pw" name="pw" value="">
						</td>
					</tr>
					<tr>
						<td class="active">전자우편</td>
						<td>
							<input type="text" id="email" name="email" value="">
						</td>
						<td class="active">직급</td>
						<td>
							<input type="text" id="jikName" name="jikName" value="">
						</td>
					</tr>
					<tr>
						<td class="active">Mobile</td>
						<td>
							<input type="text" id="mobileNo" name="mobileNo" value="">
						</td>
						<td class="active">회사/부서</td>
						<td>
							<input type="text" id="globalCom" name="globalCom" value="">
						</td>
					</tr>
					<tr>
						<td class="active">네이트온</td>
						<td>
							<input type="text" id="nateOn" name="nateOn" value="">
						</td>
						<td class="active">MSN</td>
						<td>
							<input type="text" id="msn" name="msn" value="">
						</td>
					</tr>
					<tr>
						<td class="active">언어</td>
						<td colspan="3">
							<select id="locale" name="locale">
								<option id="ko" value="ko">한국어</option>
								<option id="en" value="en">영어</option>
							</select>
						</td>
					</tr>
				</table>
			</div>
			<div style="text-align: center; margin-bottom: 10px;">
				<a style="cursor: pointer;" onclick="updateEmpInfo();">
					<img src="<c:url value='/resources/images/icons/btn2_06.gif'/>"/>
				</a>
			</div>
		</div>
	</div>
</div>
</form>
</body>
</html>