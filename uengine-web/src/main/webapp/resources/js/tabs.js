/*
 * createTabs(strIds, strDefaultId);
 *  - strIds = id1;id2;id3;id4....
 *  - strDefaultId = Default View
 * div id = divTabItem_<id>
 * 
 */

var contentPageName = "";
var allElements = new Array();

var createdCount = 0;
	String.prototype.trim = function() {
	 return this.replace(/(^\s*)|(\s*$)/gi, "");
}

function changeDisplay(strShowId){
    for(i=0 ; i < allElements.length ; i++){
        var objTemp= document.getElementById("divTabItem_" + allElements[i]);
		if (objTemp) objTemp.style.display = 'NONE';
    }
    
    document.getElementById("divTabItem_" + strShowId).style.display='';
    setContentPageName(strShowId);
}

function changeTabStyle(objName) {
    for(var i=0 ; i < allElements.length ; i++) {
		var objMenu = document.getElementById("menuItem_" + allElements[i]);
		if (objMenu) objMenu.className = "";
    }
    
    document.getElementById("menuItem_" + objName).className = "current";
    setContentPageName(objName);
}

function setContentPageName(objName) {
	contentPageName = objName;
}

var changeDisplayMenuItem = function(objName) {
	if (objName == contentPageName) {
		return;
	}
	
	var isOnReady = true;
	if (!objName) {
		objName = allElements[0];
	}
	
	changeTabStyle(objName);
	changeDisplay(objName);
	setContentPageName(objName);
}

function createTabs(arr) {
	var tabId = "tabMenu" + createdCount;
	createdCount++;
	
	document.write("<ul class=\"tabs\" id=\"" + tabId + "\"></ul>");
	var tab = document.getElementById(tabId);
	
	var isFirst = true;
	
	for ( var int = 0; int < arr.length; int++) {
		var tempData = arr[int];
		var li = document.createElement("li");
		var tempTitle = null;
		
		if (tempData.title) {
			tempTitle = tempData.title;
		} else {
			tempTitle = tempData;
		}
		
		li.id = "menuItem_" + tempTitle;
		
		if (isFirst) {
			li.className = "current";
			setContentPageName(tempTitle);
			isFirst = false;
		}
		
		tab.appendChild(li);
		var a = document.createElement("a");
		
		var tempHref = "javascript: changeDisplayMenuItem('" + tempTitle + "');";
		if (tempData.onclick) {
			tempHref += tempData.onclick;
		}
		
		a.href = tempHref;
		a.innerHTML = tempTitle;
		li.appendChild(a);

		allElements[allElements.length] = tempTitle;
	}
}