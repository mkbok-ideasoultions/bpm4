<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="/WEB-INF/include/include-header.jspf"%>
<%@ include file="/WEB-INF/include/include-header_resource.jspf"%>
<%@ include file="/WEB-INF/include/include-flowchart_header_resource.jspf"%>
<script>
	var orgTreeData;
	var definitionObjectArray;
	var selectedNode;
	var selectedParent;
	var isTreeClick = false;
	var comCode = "${sessionScope.loggedUser.comCode}";
	
	$(document).ready(function() {
		$("#contentsFrame").css('height', $(window).height()-105);
		$(window).resize(function(){
			$("#contentsFrame").css('height', $(window).height()-105);
		});
		fn_setorganizationTree();
	});
	
	// �쉶�궗�끂�뱶 �겢由� �떆 �쉶�궗�젙蹂� 
	fn_showCompanyInfo = function() {
		$("#organizationForm").attr("action","<c:url value='/organization/viewCompanyInfo.do' />");
		$("#organizationForm").submit();
	}
	
	// 遺��꽌�끂�뱶 �겢由� �떆 遺��꽌, �븯�쐞遺��꽌, 遺��꽌�뿉 �냼�냽�맂 �궗�썝 �젙蹂�
	fn_showPartInfo = function() {
		$("#organizationForm").attr("action","<c:url value='/organization/viewPartInfo.do' />");
		$("#organizationForm").submit();
	}
	
	// �궗�썝�끂�뱶 �겢由� �떆 �궗�썝 �젙蹂�
	fn_showEmpDetailInfo = function() {
		$("#organizationForm").attr("action","<c:url value='/organization/viewEmpDetailInfo.do' />");
		$("#organizationForm").submit();
	}
	
	// �쉶�궗 議곗쭅�룄 �듃由� �깮�꽦 ( jsTree )
	fn_setorganizationTree = function() {
		$('#organizationTree').on('select_node.jstree', function(node, selected, event,data) {
			//�끂�뱶 �꽑�깮�떆 諛쒖깮�븯�뒗 �씠踰ㅽ듃 諛� ajax 泥섎━
			$("#organizationContentsDiv").show();
			$("#code").val(selected.node.id);
			$("#parent_Partcode").val(selected.node.parent);
			selectedNode = selected.node;
			selectedParent = selected.node.parent; 
			isTreeClick = true;
			if (selectedNode.type && isTreeClick){
				if ( selectedNode.type == "default" || selectedNode.type == "process" ) {
					if ( selectedNode.type == "default" ) {
						//遺��꽌 �긽�꽭 �젙蹂� �븿�닔 �샇異�
						fn_showPartInfo();
					} else if ( selected.node.type == "process") {
						//�궗�썝 �긽�꽭 �젙蹂� �븿�닔 �샇異�
						fn_showEmpDetailInfo();
					}
				} else if (selectedNode.type == "root") {
					//�쉶�궗 �긽�꽭 �젙蹂� �븿�닔 �샇異�
					fn_showCompanyInfo();
				}
			}
			isTreeClick = false;
		}).on('loaded.jstree', function(node, selected, event) {
			if(node.id == null) {
				$("#code").val(comCode);
			}
			fn_showCompanyInfo();
		}).on('open_node.jstree', function(node, selected, event) {
		}).on('close_node.jstree', function(node, selected, event) {
		}).jstree({	
			// jsTree 瑜� �씠�슜�븳 湲곕낯�쟻�씤 �끂�뱶 �깮�꽦 諛� �듃由� 援ъ꽦
			'plugins' : [  "state", "types", "wholerow", "contextmenu", "dnd", "sort" ],
			//jsTree �끂�뱶 �뜲�씠�꽣 �삎�떇 (children �쓣 �꽕�젙 �빐以� �븘�슂 �뾾�씠 parent 瑜� �꽕�젙�븯�뿬 遺�紐�,�옄�떇 愿�怨꾨�� 援ъ꽦)		
			//'core' : {'data' : 
			//			[{"id":"60000001", "text":"���몴�씠�궗", "parent" : "#", "type":"root"} ,{"id":"60000002", "text":"媛쒕컻1��", "parent":"60000001", "type":"default"},{"id":"10000004", "text":"媛뺣룎�닔", "parent":"60000002", "type":"process"}]
			//		 },
			
			'core' : {
				'multiple' : true,
				'data' : {
					 "url" : function(node){
						if(node.id === "#"){							
							return "<c:url value='/organization/ajax/selectOrganizationRootNode.do' />";							
						}  else {
							var partcode = node.id;
							return "<c:url value='/organization/ajax/selectOrganizationTreeByComCode.do?partcode=' />" + partcode;
						} 
					},
					"dataType" : "json", // needed only if you do not supply JSON headers
					"method" : "POST",
					"cache" : false 
				} 
			},
			'types' : {
				'#' : {
					'valid_children' : ['root']
				},
				'root' : {
					'icon' : 'glyphicon glyphicon-hdd',
					'valid_children' : ['default']
				},
				'f-open' : {
					'icon' : 'glyphicon glyphicon-folder-open'
				},
				'f-close' : {
					'icon' : 'glyphicon glyphicon-folder-close'
				},
				'process' : {
					'icon' : 'glyphicon glyphicon-forward'
				},
				'form' : {
					'icon' : 'glyphicon glyphicon-edit'
				},
				'default' : {
					'icon' : 'glyphicon glyphicon-folder-close'
				}
			},
			'dnd' : {
				check_while_dragging : true
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
<div class="container-fluid">
  <table width=100%>
    <tr>
      <td width=230px style="vertical-align: top">
        <div id="content-left" class="content-left">
          <div class="list-group">
            <li class="list-group-item disabled"><span class="glyphicon glyphicon-tasks"><spring:message code="menu.organization.label" /></span></li>            
            <li class="list-group-item" style="overflow-x:auto;"id="organizationTree"></li>
          </div>
        </div>
      </td>
      <td width= 100% style="vertical-align: top;">
      	<div id="iframeArea"><iframe id="contentsFrame" src="<c:url value='' />" name="contentsFrame" style="width:100%; height:100%; border: 0px; overflow-y: hidden;"></iframe></div>
      </td>
      </td>
    </tr>
  </table>
</div>
</body>
</html>