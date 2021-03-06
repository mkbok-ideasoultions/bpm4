<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<script>
	var orgTreeData;
	var definitionObjectArray;
	$(document).ready(function() {
		getDefinitionObjectList();
		$("#flowchartFrame").css('height', $(window).height()-265);
		$(window).resize(function(){
			$("#flowchartFrame").css('height', $(window).height()-265);
		});
		$("#version").change(function(){
			for ( var i = 0; i < definitionObjectArray.length; i++ ) {
				var obj = definitionObjectArray[i];
	    		if ( obj.version == $(this).val().replace("*","") ) {
	    			setDefintionInfo(obj);
	    		}
			}
		});
		
		$("#definitionChangeBtn").click(function(){
			$("#definitionForm").attr("action","<c:url value='/processmanager/processDesigner.jnlp' />");
	        $("#definitionForm").submit();
		});
		
		$("#versionChange").click(function(){
			fn_updateProcessVersion($("#definitionForm").serializeArray());
		});
	});

	var getDefinitionObjectList = function(){
		$.ajax({
	        url: "<c:url value='/processmanager/ajax/selectProcessDefinitionObjectListByDefId.do' />",
	        type: "POST",
	        cache : false,
			data : $("#definitionForm").serialize(),
	    }).then(function(data) {
	    	definitionObjectArray =  eval("("+data+")");
	    	
	    	if ( window.console ) {
	    		console.log(definitionObjectArray);
	    	}
	    	
	    	$("#version").empty();
	    	for ( var i = 0; i < definitionObjectArray.length; i++ ) {
	    		var obj = definitionObjectArray[i];
	    		var option = $("<option></option>").attr("vaule", obj.version).text(obj.version);
	    		if ( obj.prodVer == obj.version ) {
	    			option = $("<option></option>").attr("vaule", obj.version).css("color", "red").attr("selected", true).text("*"+obj.version);
	    			setDefintionInfo(obj);
	    		}
	    		$("#version").append(option);
	    	}
	    	
	    });
	};
	
	var setDefintionInfo = function(obj){
		//?????????
		var defTitle = "Process Definition - ";
		if ( obj.defName ) {
			defTitle += obj.defName;
		}
		defTitle += " ( ??????-";
		if ( obj.version ) {
			defTitle += obj.version;
		}
		defTitle += " | ?????????-";
		if ( obj.authorName ) {
			defTitle += obj.authorName;
		}
		defTitle += "/";
		if ( obj.author ) {
			defTitle += obj.author;
		}
		defTitle += " | ?????????-";
		if ( obj.modDate ) {
			defTitle += obj.modDate;
		}
		defTitle += " )";
		$("#defTitle").html(defTitle);
		
		//??????
		defTitle = "";
		if ( obj.defName ) {
			defTitle += obj.defName;
		}
		defTitle += " ( ?????????-";
		if ( obj.defId ) {
			defTitle += obj.defId;
		}
		defTitle += " | ???????????? ??????-";
		if ( obj.alias ) {
			defTitle += obj.alias;
		}
		defTitle += " | ???????????? ?????????-";
		if ( obj.programId ) {
			defTitle += obj.programId;
		}
		defTitle += " )";
		$("#defInfo").html(defTitle);
		
		//??????
		defTitle = "";
		if ( obj.defName ) {
			defTitle += obj.defName;
		}
		defTitle += " ( ?????????-";
		if ( obj.defVerId ) {
			defTitle += obj.defVerId;
		}
		defTitle += " | ?????????-";
		if ( obj.authorName ) {
			defTitle += obj.authorName;
		}
		defTitle += "/";
		if ( obj.author ) {
			defTitle += obj.author;
		}
		defTitle += " | ?????????-";
		if ( obj.modDate ) {
			defTitle += obj.modDate;
		}
		defTitle += " )";
		$("#defVersionInfo").html(defTitle);
		
		//??????
		if ( obj.description ) {
			$("#defDescription").html(obj.description);
		}
		
		$("#defVerId").val(obj.defVerId);
		$("#folderId").val(obj.folderId);
		
        $("#definitionForm").attr("action","<c:url value='/processmanager/viewProcessDefinitionFlowChart.do' />");
        $("#definitionForm").submit();
	};
	
	
</script>

<title>Process Manager</title>
</head>
<body style="overflow:hidden">
	<form:form name="definitionForm" id="definitionForm" action="" method="post" target="flowchartFrame">
		<form:input path="comCode" type="hidden" id="comCode" name="comCode" value="${sessionScope.loggedUser.comCode}" /> 
		<form:input path="defId" type="hidden" id="defId" name="defId" value="${defId}" />
		<form:input path="defVerId" type="hidden" id="defVerId" name="defVerId" value="" />
		<form:input path="folderId" type="hidden" id="folderId" name="folderId" value="" />
	</form:form>
	<!-- Page Content -->
	<table width="100%" class="table table-reflow">
		<tr>
			<td colspan="2">
				<span id="defTitle" class="glyphicon glyphicon-duplicate"></span>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-bullhorn">???????????? ??????</span>
			</th>
			<td>
				<button id="definitionChangeBtn" type="button" class="btn btn-info">
					???????????? ??????
				</button>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-play">??????</span>
			</th>
			<td>
				<div id="defInfo"></div>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-play">??????</span>
			</th>
			<td>
				<table>
					<tr>
						<td><select style="width: auto; " class="form-control" id="version"></select></td>
						<td><button id="versionChange" class="btn btn-info" >????????????</button></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-play">????????????</span>
			</th>
			<td>
				<table>
					<tr>
						<td><div id="defVersionInfo"></div></td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<th>
				<span class="glyphicon glyphicon-play">??????</span>
			</th>
			<td>
				<div id="defDescription"></div>
			</td>
		</tr>
		<tr>
			<td colspan="2">
				<div id="flowchartArea"><iframe frameborder="0" id="flowchartFrame" name="flowchartFrame" style="width:100%; overflow-x: hidden;"></iframe></div>
			</td>
		</tr>
	</table>
		
</body>
</html>