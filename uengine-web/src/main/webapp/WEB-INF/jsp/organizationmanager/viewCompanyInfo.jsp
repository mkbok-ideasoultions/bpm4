<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<script type="text/javascript">
	$(document).ready(function() {
		fn_getCompanyInfo();	//�쉶�궗�젙蹂� �븿�닔 �샇異�
		fn_getPartInfo();		//�옄�떇遺��꽌 紐⑸줉 �븿�닔 �샇異�
		fn_setChildPartGrid();	//�옄�떇遺��꽌 紐⑸줉 jqGrid �븿�닔 �샇異�
		fn_getEmpInfo();		//�옄�떇�궗�썝 紐⑸줉 �븿�닔 �샇異�
		fn_setChildEmpGrid();	//�옄�떇�궗�썝 紐⑸줉 jqGird �븿�닔 �샇異�
	});
	
	/*	�쉶�궗�젙蹂� 議고쉶 �븿�닔  */
	var fn_getCompanyInfo = function() {
		$.ajax({
	        url: "<c:url value='/organization/ajax/getCompanyInfo.do' />",
	        type: "POST",
	        dataType : "json",
	        cache : false,
			data : { code : $("#code").val() },
	    }).then(function(data) {	    	
	    	for(var i=0; i < data.length; i++) {
	    		$("#comCode").html(data[i].comCode);
	    		$("#comName").html(data[i].comName);
	    		$("#description").html(data[i].description);
	    		$("#titleOfCompany").html(data[i].comName + " Information");
	    		$("#titleOfChildPart").html("Child groups of " + data[i].comName);
	    		$("#titleOfChildEmp").html("Child users of " + data[i].comName);
	    	}
	    });
	}
	
	/*	�옄�떇臾댁꽌紐⑸줉 由ъ뒪�듃瑜� 媛��졇�� jqGird 濡� 洹몃━�뒗  �븿�닔  */
	var fn_getPartInfo = function() {
		$.ajax({
			url: "<c:url value='/organization/ajax/getPartInfo.do' />",
	        type: "POST",
	        dataType : "json",
	        cache : false,
			data : { code : $("#code").val() },
			success : function(data){
				$("#partListTable").jqGrid('clearGridData'
				).jqGrid('setGridParam',
					{
						datatype: "local",
						data : data,
						loadComplete : function(data){
							if(data.rows.length == 0) {
								$("#partListTable > tbody").append("<tr><td colspan='5'>검색된 부서 정보가 없습니다.</td></tr>");
							}
						},
					}
				).trigger("reloadGrid",[{current:true}]);
			},			
			error : function(data, msg){
				
			}
		});
	    
	}
	
	/*	�궗�썝紐⑸줉 由ъ뒪�듃瑜� 媛��졇�� jqGrid 濡� 洹몃━�뒗 �븿�닔	*/
	var fn_getEmpInfo = function() {
		$.ajax({
			type : "POST",
			url: "<c:url value='/organization/ajax/getEmpInfo.do' />",
			cache : false,			
			dataType : "JSON",
			data : { code : $("#code").val() },
			success : function(data) {
				$("#empListTable").jqGrid('clearGridData'
				).jqGrid('setGridParam',
					{
						datatype: "local",
						data : data,
						loadComplete : function(data){
							$("#nodata").remove();
							if(data.rows.length == 0) {
								$("#empListTable > tbody").append("<tr><td colspan='5'>검색된 부서 정보가 없습니다.</td></tr>");
							}
						},
					}
				).trigger("reloadGrid",[{current:true}]);
			}
			,error : function(data, msg) {
				if ( window.console )	
					console.log(data);
			}
		});
	}
	
	/*	�옄�떇遺��꽌紐⑸줉 jqGrid 援ы쁽 �븿�닔		*/
	var fn_setChildPartGrid = function() {
		$("#partListTable").jqGrid({
			colNames : ["그룹 이름",
			            "그룹 코드",
			            "그룹 설명"],
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
	
	/*	�옄�떇�궗�썝紐⑸줉 jqGrid 援ы쁽 �븿�닔  */
	var fn_setChildEmpGrid = function(){
		$("#empListTable").jqGrid({
			colNames : ["이름",
			            "부서",
			            "회사",
			            "전자우편",
			            "사번"],
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
<form:form name="organizationForm" id="organizationForm" action="" method="post" target="contentsFrame">
	<form:input path="code" type="hidden" id="code" name="code" value="${code}" />
	<form:input path="parent_Partcode" type="hidden" id="parent_Partcode" name="parent_Partcode" value="${parent_Partcode}" /> 
</form:form>
<form name="imgForm" id="imgForm" action="" method="post">
        <input type="hidden" id="imgData" name="imgData">
 </form>
<div id="content-middle" class="content-middle" style="width : 100%;">
	<div id="" class="panel-group">
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfCompany" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body" id="body">
				<table class="table table-reflow" width="100%" style="border-bottom: 1px solid #ddd; ">
					<tr style="border-bottom: 1px solid #ddd">
						<td class="active">로고</td>
						<td colspan="7">
							<img src="<c:url value='/resources/images/page/logo/demo_logo.png'/>" width="137" height="49" />
						</td>
					</tr>
					<tr>
						<td class="active">이름</td>
						<td><span id="comName"></span></td>
						<td class="active">아이디</td>
						<td><span id="comCode"></span></td>
						<td class="active">유형</td>
						<td></td>
					</tr>
					<tr>
						<td class="active">설명</td>
						<td colspan="7"><span id="description"></span></td>
					</tr>
				</table>
			</div>
		</div>
		<div style="padding-top: 30px;"/>
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfChildPart" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table id="partListTable" name="partListTable" width="100%" style="text-align: center; border:0px;"></table>
			</div>
		</div>
		<div style="padding-top: 30px;"/>
		<div class="panel panel-primary">
			<div class="panel-heading">
				<span id="titleOfChildEmp" class="glyphicon glyphicon-edit"><span>
			</div>
			<div class="panel-body">
				<table id="empListTable" name="empListTable" width="100%" style="text-align: center; border:0px;"></table>
			</div>
		</div>				
	</div>
</div>
</body>
</html>