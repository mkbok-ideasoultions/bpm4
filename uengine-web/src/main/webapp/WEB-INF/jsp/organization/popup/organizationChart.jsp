<%@ page contentType="text/html; charset=UTF-8" language="java"  %>
<%-- <%@include file="header.jsp"%> --%>
<%-- <%@include file="getLoggedUser.jsp"%> --%>
<%@include file="styleHeader.jsp"%>
<%
	boolean isMultiple = true;
	boolean isApproval = false;
	//String sMultiple = request.getParameter("multiple");
	
	try {
		isMultiple = UEngineUtil.isTrue(request.getParameter("multiple"));
		isApproval = UEngineUtil.isTrue(request.getParameter("isApproval"));
	} catch (Exception e) {}
	
	String inputType = null;
	String dblClickScript = null;
	if (isMultiple) {
		dblClickScript = "deleteApproval();";
	} else {
		dblClickScript = "";
	}
%>
<%@page import="org.uengine.util.UEngineUtil"%>
<script type="text/javascript">
		var WEB_ROOT = "<%=GlobalContext.WEB_CONTEXT_ROOT%>";
</script>
<c:import url="/WEB-INF/jsp/organization/importCommonScripts.jsp"></c:import>
<%@ include file="../scripts/" %>
<script type="text/javascript" src="<%=GlobalContext.WEB_CONTEXT_ROOT %>/scripts/ajax/ajaxCommon.js"></script>
<script type="text/javascript" src="<%=GlobalContext.WEB_CONTEXT_ROOT %>/scripts/crossBrowser/elementControl.js"></script>
<script type="text/javascript" src="<%=GlobalContext.WEB_CONTEXT_ROOT %>/scripts/ajax/userList.js"></script>
<style type="text/css">
	@import "/resources/libs/dojo/dojo/resources/dojo.css";
	@import "/resources/libs/dojo/dijit/tests/css/dijitTests.css";
</style>
<script type="text/javascript" src="/resources/libs/dojo/dojo/dojo.js" djConfig="parseOnLoad: true, isDebug: false"></script>
<script type="text/javascript" src="/resources/libs/dojo/dijit/tests/_testCommon.js"></script>
<script type="text/javascript">
		dojo.require("dojo.data.ItemFileWriteStore");
		dojo.require("dijit.Tree");
		dojo.require("dijit.ColorPalette");
		dojo.require("dijit.Menu");
		dojo.require("dojo.parser");	// scan page for widgets and instantiate them
		var global_tn = '';
				
		var IS_MULTIPLE = <%=isMultiple%>;
		function SelectedInfo() {
				var type = '';
				var id = '';
				var name = '';
				var title = '';
				var birthday;
				var isMale = true;
				var jikName = '';
		}
		
		
		function getSelectedUsers() {
			if (IS_MULTIPLE) {
				selectedOrganizationList = new Array();
	
				var selectList = document.mainForm.selectUserList;
				var j = 0;
				for (var i=0; i<selectList.options.length; i++) {
					var user = new SelectedInfo();
					var userInfo = selectList.options[i].value;
		
					var userInfoArray = userInfo.split(';');
					var userId = userInfoArray[0].split('=');
					var userName = userInfoArray[1].split('=')[1];
					var jikName = userInfoArray[1].split('=')[2];
					user.type 		= "";
					user.name 	= userName.replace('_',' ');
					user.id 		= userId[1];
					user.isMale	= "1";
					user.birthday	= "";
					
					user.jikName = jikName;
					selectedOrganizationList [j++] = user;
				}
			}
			
			return selectedOrganizationList;
		}

		var userNameList;
		function setUser(chkBoxUser) {
			var userInfo = null;
			var user = new SelectedInfo();
			
			if (chkBoxUser.checked) {
				userInfo = chkBoxUser.value;
				
				var userInfoArray = userInfo.split(';');
				var userId = userInfoArray[0].split('=');
				var userName = userInfoArray[1].split('=')[1].replace('_',' ');
				var jikName = userInfoArray[2].split('=')[1].replace('_',' ');
				
				user.type 		= "";
				user.name 		= userName;
				user.id 		= userId[1];
				user.isMale		= "1";
				user.birthday	= "";
				user.jikName 	= jikName;

				if (IS_MULTIPLE) {
					// 2011.04.05 sung-yeol jung modify
					/*
					var newOption = document.createElement("option");
					var realTargetTag = document.mainForm.selectUserList;
					
					newOption.value = chkBoxUser.value;
					newOption.text = " " + userName
					
					realTargetTag.options.add(newOption);
					*/
					
					var flag      = false;
		            var selectList = document.mainForm.selectUserList;

		            for (var i=0; i<selectList.options.length; i++) {
		                var refUserInfo       = selectList.options[i].value;
		                var refUserInfoArray  = refUserInfo.split(';');
		                var refUserId         = refUserInfoArray[0].split('=');
		                
		                if(userId[1] == refUserId[1]){
		                    flag = true;
		                    break;
		                }
		            }
		            
		            if(flag == false){
		                var newOption     = document.createElement("option");
		                var realTargetTag = document.mainForm.selectUserList;
		                newOption.value   = chkBoxUser.value;
		                newOption.text    = " " + userName
		                realTargetTag.options.add(newOption);
		                
		            } else {
		                userNameList += ((userNameList == "")?user.name:", " + user.name);
		            }
				} else {
					selectedOrganizationList = [user];
				}
			}
		}
		
		function addUser() {
			// 2011.04.05 sung-yeol jung modify
			var userList = $("input[name='chkUser']");
		    
		    var userInfo = null;
		    var user = new SelectedInfo();
		    
		    if (userList != null) {
		        $("input[name='chkUser']").each(function(index, chkbox) {
		            if (chkbox.checked) {
		                userInfo = $(this).val();
		                
		                var userInfoArray = userInfo.split(';');
		                var userId = userInfoArray[0].split('=');
		                var userName = userInfoArray[1].split('=')[1].replace('_',' ');
		                var jikName = userInfoArray[2].split('=')[1].replace('_',' ');
		                
		                user.type       = "";
		                user.name       = userName;
		                user.id         = userId[1];
		                user.isMale     = "1";
		                user.birthday   = "";
		                user.jikName    = jikName;

		                if (IS_MULTIPLE) {
		                    var flag       = false;
		                    
		                    for (var i=0; i < $("#selectUserList option").size(); i++) {
		                        var refUserInfo       = $("#selectUserList option:eq("+i+")").val();
		                        var refUserInfoArray  = refUserInfo.split(';');
		                        var refUserId         = refUserInfoArray[0].split('=');
		                        
		                        if(userId[1] == refUserId[1]){
		                            flag = true;
		                            break;
		                        }
		                    }
		                    
		                    if(flag == false){
		                        $("#selectUserList").append("<option value=\""+userInfo+"\">"+user.name+"</option>");
		                    } else {
		                        userNameList += ((userNameList == "")?user.name:", " + user.name);
		                    }
		                } else {
		                    selectedOrganizationList = [user];
		                }
		            }
		        });
		        
		        if (IS_MULTIPLE) {
		            getSelectedUsers(userList);
		            
		            // 12.22 add
		            if(userNameList != ""){
		                //alert("해당 유저(들)는 이미 포함되어 있습니다. " + userNameList);
		            }
		        }
		    }
		    
			//var userList = document.mainForm.chkUser;
			
			//if (userList != null) {
			//	if (userList.length) {
			//		for (var ii = 0; ii < userList.length; ii++) {
			//			setUser(userList[ii])
			//		}
			//	} else {
			//		setUser(userList)
			//	}
				
			//	if (IS_MULTIPLE) {
			//		getSelectedUsers(userList);
			//	}
			//}
		}
		
		function deleteApproval() {
			var selectedUser = document.mainForm.selectUserList;

			for (var ii = 0; ii < selectedUser.options.length; ii++) {
				var option = selectedUser.options[ii];
				if (option.selected) {
					selectedUser.options[ii] = null;
					ii--;
				}
			}

			getSelectedUsers();
		}
		function searchUser() {
			var mainForm = document.mainForm;
			getUserList(mainForm.column.value, mainForm.key.value, "tableUserList", <%=isMultiple%>); 
			try {
				document.getElementById("chkBoxAllUsers").checked = false;
			} catch (e) {}
		}

		function chkEnter(e) {
			var code = (window.event) ? event.keyCode : e.which;
			if (code == 13) {
				searchUser();
				if (window.event) {
					window.event.returnValue = false;
				} else {
					e.returnValue = false;
				}
			}
		}

		var appendChildNodeToTree = function(httpReq, item) {
			if (httpReq.readyState == 4) {
				if (httpReq.status == 200) {
					var responseText = httpReq.responseText;
					var arrayOfResult = new Array();
					eval(responseText);
					
					for (var i = 0; i < arrayOfResult.length; i++) {
						var result = arrayOfResult[i];
						
						if (result.discription) {
							result.discription = decodeURIComponent(result.discription);
						}
						if (result.name) {
							result.name = decodeURIComponent(result.name);
						}
						
						var newItem = continentStore.newItem(result, {parent : item, attribute : "children"});
						var tempChild = {code : "temp" + result.code, name : "tempChildrenForItem", type : "temp"};
						continentStore.newItem(tempChild, {parent : newItem, attribute : "children"});
					}
				} else {
					alert(httpReq.status);
				}
				
				httpReq = null;
			}
		}

		function getDepartmentList(item) {
			var firstChild = item.children[0];
			if ("tempChildrenForItem" == firstChild.name) {
				continentStore.deleteItem(firstChild);
				var url = WEB_ROOT + "/organizationmanager/json/childrenOfPartJson.jsp?";
				
				if (item.type == "company") {
					url += "comCode=" + item.code;
				} else if(item.type == "department") {
					url += "parentCode=" + item.code;
				}

				var httpReq = getXMLHttp();
				submitAjaxWithNewHTTP(
						httpReq,
						url,
						"Get",
						function () {
							appendChildNodeToTree(httpReq, item);
						},
						null
				);
			}
		}
		
		var selectedOrganizationList = new Array();
		var inpuName = "";
	</script>
<style type="text/css">
BODY {
	background:url(images/empty.gif) #ffffff;
	margin: 0px;
	padding: 0px;
}
</style>
<script type="text/javascript" src="<%=GlobalContext.WEB_CONTEXT_ROOT %>/resources/js/tabs.js"></script>


<form name="mainForm">
   
    <table width="95%"  align="center">
        <tr>
        <td valign="top">
        
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
            <tr>
                <td>
                	<select name="column">
                        <option value="empname"><%=GlobalContext.getMessageForWeb("User Name", loggedUserLocale) %></option>
                        <option value="empcode"><%=GlobalContext.getMessageForWeb("User ID", loggedUserLocale) %></option>
                        <option value="partname"><%=GlobalContext.getMessageForWeb("Department Name", loggedUserLocale) %></option>
                        <option value="partcode"><%=GlobalContext.getMessageForWeb("Department Code", loggedUserLocale) %></option>
                    </select>
                    <input type="text" name="key" onKeyPress="chkEnter(event);" style="height:19px; width:105px;" />
                    <input type="button" onClick="searchUser();" value="<%=GlobalContext.getMessageForWeb("Search", loggedUserLocale) %>" style="height:19px;"/></td>
            </tr>
            <tr> 
                <td height="10"></td>
            </tr>
        </table>
        <div style="overflow: auto; height: 155px; border:1px solid #CCC;">
                	<div dojoType="dojo.data.ItemFileWriteStore" jsId="continentStore" url="<%=GlobalContext.WEB_CONTEXT_ROOT %>/organizationmanager/json/jsonConfig.json"></div>
                    
                    <div dojoType="dijit.tree.ForestStoreModel" jsId="continentModel" store="continentStore" 
					rootId="continentRoot" rootLabel="Organization" childrenAttrs="children" ></div>
                    
                    <div dojoType="dijit.Tree" id="tree2" model="continentModel" showRoot="false" openOnClick="false" >
                <script type="dojo/method" event="getIconClass" args="item, opened">
					var ObjType="";
					
					if (item == this.model.root) {
						ObjType = "organization";
					} else if (continentStore.getValue(item, "type") == "root") {
						ObjType="organization";
					} else {
						ObjType = continentStore.getValue(item, "type");
					}
					
					if (ObjType == "folder") {
						ObjType = opened ? "customFolderOpenedIcon" : "customFolderClosedIcon";
					} else if (ObjType == "company") {
						ObjType = "comIcon";
					} else if (ObjType == "department") {
						ObjType = "deptIcon";
					} else {
                        ObjType = "organizationIcon";
                    }
					
					return ObjType;
				</script>
                <script type="dojo/method" event="onClick" args="item">
					if (item.type == "user") {
						var id = item.code;
						var name = item.name;	
						var jikname= item.jikname;	
						
						adduser(id, name, jikname);
					} else {
						if (item.type == "department") {
							getUserList("partcode", item.code, "tableUserList", <%=isMultiple%>, <%= isApproval%>);
							try {
								document.getElementById("chkBoxAllUsers").checked = false;
							} catch (e) {}
						}
					}
				</script>
				<script type="dojo/method" event="onOpen" args="item, node">
					getDepartmentList(item);
				</script>
                </div>
        </div>
        <br>
        <hr>
        <table width="100%" class="tableborder" >
            <tr>
                <td><table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr class="tabletitle" align="center" height="26">
                            <td width="30" align="center">
                           		<% if (isMultiple) { %>
                                <input type="checkbox" id="chkBoxAllUsers" onClick="formatCheckBox('chkUser', this.checked);" />
                                <% } else { %>
                                &nbsp;
                                <% } %>
                            </td>
                            <td>CODE</td>
                            <td>NAME</td>
                            <td>PART</td>
                            <td>POSITION</td>
                        </tr>
                    </table>
                </td>
            </tr>
            <tr>            
	            <td>
		            <div style="width:100%; height:110px; overflow:auto;">
		                <table width="100%" border="0" cellspacing="0" cellpadding="0">
		                	<col span="1" width="30" align="center" />
		                    <col span="1" width="30" align="center" />
		                    <col span="1" align="center"/>
		                    <col span="1" align="center"/>
		                    <col span="1" align="center"/>
		                    <tbody id="tableUserList">
		                    </tbody>    
		                </table>
		            </div>
	            </td>            
            </tr>            
        </table>
        </td>
        
        <% if (isMultiple) { %>
        <script type="text/javascript">
			window.resizeTo(600, 580);
		</script>
        <td width="40" align="center"><img src="../images/Common/b_addR.gif" width="25" height="25" onClick="addUser()" style="cursor:pointer"><br />
                <br />
                <img src="../images/Common/b_addL.gif" width="25" height="25" onClick="deleteApproval()" style="cursor:pointer"></td>
            <td width="200"  valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <td><img src="../images/Common/I_info.gif" width="11" height="11"></td>
                        <td height="30" style="font-size:11px; letter-spacing:-1; text-align:center">double click = remove selected user</td>
                    </tr>
                    <tr>
                        <td colspan="2"><select multiple="multiple" name="selectUserList" id="selectUserList" size="22" style="width:100% " ondblclick="deleteApproval();" style="background:#F7F7F7"></select></td>
                    </tr>
                </table></td>
            <% } %>
        </tr>
    </table>
    <br>
    <br>
    <table width="95%" border="0" cellspacing="0" cellpadding="0" align="center">
        <tr>
            <td  class="formheadline"></td>
        </tr>
    </table>
</form>