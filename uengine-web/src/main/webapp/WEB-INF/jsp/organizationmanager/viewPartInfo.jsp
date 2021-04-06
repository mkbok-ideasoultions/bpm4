<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<script type="text/javascript">
	$(document).ready(function() {
		var code = $("#code").val();
		var parent_Partcode = $("#parent_Partcode").val();
		
		fn_getPartInfo();		//부서정보 함수 호출
		fn_getChildPartList();	//부서정보 함수 호출
		fn_getEmpInfo();		//사원정보 함수 호출
		fn_setChildPartGrid();	//부서정보 jqGrid 함수 호출
		fn_setEmpGrid();		//사원정보 jqGrid 함수 호출
	});

	/*	부서정보 조회 함수  */
	var fn_getPartInfo = function() {
		$.ajax({
	        url: "<c:url value='/organization/ajax/getPartDetailInfo.do' />",
	        type: "POST",
	        dataType : "json",
	        cache : false,
			data : { code : $("#code").val() },
	    }).then(function(data) {
	    	var array = "";
	    	for(var i = 0; i < data.length; i++) {
	    		$("#pdName").html(data[i].partName);
	    		$("#pdType").html(data[i].partName);
	    		$("#pdId").html(data[i].partCode);
	    		$("#pdDescription").html(data[i].description);
	    		$("#titleOfPart").html(" " + data[i].partName + " Information");
	    		$("#titleOfChildPart").html(" Children groups of " + data[i].partName);
	    		$("#titleOfChildEmp").html(" children users of " + data[i].partName);
	    	}
	    });
	}
	
	/*	사원목록 리스트를 가져와 jqGrid 로 그리는 함수	*/
	var fn_getEmpInfo = function() {
		$.ajax({
			type : "POST",
			url: "<c:url value='/organization/ajax/getEmpInfo.do' />",
			cache : false,			
			dataType : "JSON",
			data : { code : $("#code").val() },
			success : function(data) {
				$("#empTable").jqGrid('clearGridData'
				).jqGrid('setGridParam',
					{
						datatype: "local",
						data : data,
						loadComplete : function(data){
							if(data.rows.length == 0) {
								$("#empTable > tbody").append("<tr><td colspan='5'>검색된 사원 정보가 없습니다.</td></tr>");
							}
						},
					}
				).trigger("reloadGrid",[{current:true}]);
			},
			error : function(data, msg) {
				if ( window.console )	
					console.log(data);
			}
		});
	}

	function setNoImage(imgElement) {
		imgElement.src = WEB_CONTEXT_ROOT + "/images/page/logo/demo_logo.gif"
	}
	
	function changeGroupImage() {
		var imgLogo = document.getElementById("idImgLogo");
		detailInformation.imgSrc = imgLogo.src;
		var options= "dialogWidth:300px; dialogHeight:350px; scrollbar:no; status:no; help:no;";
		var umodal = window.showModalDialog("group/modalGroupLogo.jsp", detailInformation, options);
		
		imgLogo.src = WEB_CONTEXT_ROOT + "/images/portrait/groups/" + detailInformation.code + "_logo.gif";
	}
	
	
	/*	자식부서목록 리스트를 가져와 jqGrid 로 그리는 함수	*/
	var fn_getChildPartList = function() {
		$.ajax({
			type : "POST",
			url: "<c:url value='/organization/ajax/getPartInfo.do' />",
			cache : false,			
			dataType : "JSON",
			data : { code : $("#code").val() },
			success : function(data) {
				$("#partTable").jqGrid('clearGridData'
				).jqGrid('setGridParam',
					{
						datatype: "local",
						data : data,
						loadComplete : function(data){
							if(data.rows.length == 0) {
								$("#partTable > tbody").append("<tr><td colspan='4'>검색된 부서 정보가 없습니다.</td></tr>");
							}
						},
					}
				).trigger("reloadGrid",[{current:true}]);
			},
			error : function(data, msg) {
				if ( window.console )	
					console.log(data);
			}
		});
	}
	
	/*	자식부서목록 jqGrid 구현 함수		*/
	var fn_setChildPartGrid = function() {
		$("#partTable").jqGrid({
			colNames : ["그룹이름",
			            "그룹코드",
			            "그룹설명"],
			colModel : [ {
					name : "partName",
					sorttype : "String",
					align : "center"
				},  {
					name : "partCode",
					sorttype : "String",
					align : "center"
				}, {
					name : "description",
					sorttype : "String",
					align : "center"
				}
			],
			jsonReader : {
				repeatitems : false
			},
			loadonce: true,
			viewrecords : true,
			autowidth : true,
			height : 'auto',
			rownumbers: true,
			rowNum: rowCount,
			pager: "#pager",
			scrollOffset: 0,
			editurl: 'server.php',
			ondblClickRow: function(rowId){
				if ( window.console){
					console.log($(this).getRowData(rowId));
				}
				$("#code").val($(this).getRowData(rowId).partCode);
				console.log($("#code").val($(this).getRowData(rowId).partCode));
				$("#organizationForm").attr("action","<c:url value='/organization/viewPartInfo.do' />");
				$("#organizationForm").submit();
			}
		});
	}
	
	/*	사원목록 jqGrid 구현 함수  */
	var fn_setEmpGrid = function(){
		$("#empTable").jqGrid({
			colNames : ["이름",
			            "부서",
			            "회사",
			            "전자우편",
			            "사원번호"],
			colModel : [ {
					name : "empName",
					sorttype : "String",
					align : "center"
				},  {
					name : "partName",
					sorttype : "String",
					align : "center"
				}, {
					name : "globalCom",
					sorttype : "String",
					align : "center"
				}, {
					name : "email",
					sorttype : "String",
					align : "center"
				}, {
					name : "empCode",
					hidden : true,
					align : "center"
				}
			],
			jsonReader : {
				repeatitems : false
			},
			loadonce: true,
			viewrecords : true,
			autowidth : true,
			height : 'auto',
			rownumbers: true,
			rowNum: rowCount,
			pager: "#pager",
			scrollOffset: 0,
			editurl: 'server.php',
			ondblClickRow: function(rowId){
				if ( window.console){
					console.log($(this).getRowData(rowId));
				}
				$("#code").val($(this).getRowData(rowId).empCode);
				console.log($("#code").val($(this).getRowData(rowId).empCode));
				$("#organizationForm").attr("action","<c:url value='/organization/viewEmpDetailInfo.do' />");
				$("#organizationForm").submit();
			}
		});
	}
</script>
</head>
<body>
<div class="container-fluid">
<form:form name="organizationForm" id="organizationForm" action="" method="post" target="contentsFrame">
	<form:input path="code" type="hidden" id="code" name="code" value="${code}" />
	<form:input path="parent_Partcode" type="hidden" id="parent_Partcode" name="parent_Partcode" value="${parent_Partcode}" /> 
</form:form>
<div id="content-middle" class="content-middle" style="width : 100%;">
	<div id="" class="panel-group">
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfPart" name="titleOfPart" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table class="table table-reflow" id="partInfoTable" name="partInfoTable" width="100%" style="border-bottom: 1px solid #ddd;">
					<tr>
						<td class="active">이름</td>
						<td id="pdName" name="pdName"></td>
						<td class="active">아이디</td>
						<td id="pdId" name="pdId"></td>
						<td class="active">유형</td>
						<td id="pdType" name="pdType"></td>
					</tr>
					<tr>
						<td class="active">설명</td>
						<td colspan="5" id="pdDescription" name="pdDescription"></td>
					</tr>
				</table>
			</div>
		</div>
		<div style="padding-top: 30px;">
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfChildPart" name="titleOfChildPart" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table id="partTable" name="partTable" width="100%" style="border:0px; text-align: center;">
				</table>
			</div>
		</div>
		<div style="padding-top: 30px;">
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfChildEmp" name="titleOfChildEmp" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table id="empTable" width="100%" style="border:0px; text-align: center;">		
				</table>			
			</div>
		</div>
	</div>
</div>
</div>
</body>
</html>