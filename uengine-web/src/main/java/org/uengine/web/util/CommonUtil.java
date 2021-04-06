/**
 * 
 */
/*
 * Copyright yysvip.tistory.com.,LTD.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of yysvip.tistory.com.,LTD. ("Confidential Information").
 */
package org.uengine.web.util; 

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.omg.CORBA.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.uengine.util.dao.DefaultConnectionFactory;
import org.uengine.web.organization.controller.OrganizationController;
import org.uengine.web.organization.dao.OrganizationDAO;
import org.uengine.web.organization.service.OrganizationService;
import org.uengine.web.organization.service.OrganizationServiceImpl;
import org.uengine.web.organization.vo.OrganizationVO;
import org.uengine.web.organization.vo.UserVO;

/**
 * <pre>
 * org.uengine.util.util 
 *    |_ CommonUtil.java
 * 
 * </pre>
 * @date : 2016. 6. 20. 오후 3:14:10
 * @version : 
 * @author : next3
 */
/**
 * <pre>
 * org.uengine.util.util 
 * CommonUtil.java
 * 
 * </pre>
 * @date : 2016. 6. 20. 오후 3:14:10
 * @version : 
 * @author : next3
 */
public class CommonUtil {
	private Logger log = Logger.getLogger(this.getClass());
	
	public static List<Map> getTreeObjectByListForBootstrap(List levelTree, String levelFieldName, String idFieldName, String textFieldName, int startLevel) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		List<Map> treeList = new ArrayList();
		
		for ( int i = 0; i < levelTree.size(); i++ ) {
			Object thisObject = levelTree.get(i);
			int thisLevel = Integer.parseInt((String) getFieldValue(thisObject, levelFieldName));
			
			if ( startLevel != thisLevel )
				continue;
			
			Map<String, Object> thisMap = new HashMap<String, Object>();
			thisMap.put("id", getFieldValue(thisObject, idFieldName));
			thisMap.put("text", getFieldValue(thisObject, textFieldName));
			
			if ( i < levelTree.size()-1 ) {
				Object nextObject = levelTree.get(i+1);
				Field nextLevelField = nextObject.getClass().getDeclaredField(levelFieldName);
				nextLevelField.setAccessible(true);
				int nextLevel = Integer.parseInt((String) nextLevelField.get(nextObject));
				if ( nextLevel == (thisLevel+1) ) {
					List childList = new ArrayList();
					for ( int j = i+1; j < levelTree.size(); j++ ) {
						Object childObject = levelTree.get(j);
						int childLevel = Integer.parseInt((String) getFieldValue(childObject, levelFieldName));
						if ( childLevel == thisLevel)	break;
						childList.add(levelTree.get(j));
					}
					thisMap.put("nodes", getTreeObjectByListForBootstrap(childList, levelFieldName, idFieldName, textFieldName, nextLevel));
				}
			}
			
			treeList.add(thisMap);
			
		}
		
		return treeList;
		
	}
	
	//public static List<Map> getTreeByListForJSTree(List nodeList, String partCode, String parent_Partcode, String partName) throws Exception {
	//	
	//	List<Map> treeList = new ArrayList();
	//	//for(int i = 0; i < nodeList.size(); i++) {
	//	for(int i = 0; i<nodeList.size(); i++) {
	//		Object thisObject = nodeList.get(i);
	//		String id = (String) getFieldValue(thisObject, partCode);
	//		String name = (String) getFieldValue(thisObject, partName);
	//		String parentId = (String) getFieldValue(thisObject, parent_Partcode);
	//		int chkVal = 0;			
	//		try {
	//			chkVal = checkUserExistList(id);
	//		} catch (Exception e) {
	//			e.printStackTrace();
	//		}
	//		
	//		Map<String, Object> thisMap = new HashMap<String, Object>();
	//		
	//		thisMap.put("id", id);
	//		thisMap.put("text", name);			
	//		thisMap.put("type", "default");
	//		
	//		if ( !parentId.equals("#") ) {
	//			thisMap.put("parent", parentId);
	//		}
	//		
	//		if((i+1) == nodeList.size()) {
	//			System.out.println("no more nodes");
	//		} else {
	//			List childList = new ArrayList();
	//			List<UserVO> userList = new ArrayList();
	//			for (int j = i+1; j < nodeList.size(); j++) {
	//				Object childObject = nodeList.get(j);
	//				String childParentId = (String) getFieldValue(childObject, parent_Partcode);
	//				if( childParentId.equals(id)) {			
	//					childList.add(nodeList.get(j));
	//				} else if(!childParentId.equals(id) && chkVal > 1){
	//					//partcode 정보를 가져와서 emptable 조회후 
	//					//empCode , empname 로 노드 생성
	//					userList = selectUserListByPartCode(id);
	//				} else {
	//					break;
	//				}
	//			}
	//			thisMap.put("children", getTreeByListForJSTree(childList, partCode, parent_Partcode, partName));
	//			
	//			if(chkVal > 1) {
	//				thisMap.put("children", getTreeByUserForJSTree(userList, id));
	//			}
	//		}			
	//		treeList.add(thisMap);
	//	}		
	//	return treeList;
	//}
	
	public static List<Map> getTreeByListForJSTree(List partList, List<UserVO>userList, String partCode, String parent_Partcode, String partName, boolean chkChildren) throws Exception {
		List<Map> treeList = new ArrayList();
		//List<UserVO> userList = new ArrayList();
		int chkVal = 0;
		
		for(int i = 0; i < partList.size(); i++) {
			Object thisObject = partList.get(i);
			String id = (String) getFieldValue(thisObject, partCode);
			String name = (String) getFieldValue(thisObject, partName);
			String parentId = (String) getFieldValue(thisObject, parent_Partcode);
			
			Map<String, Object> thisMap = new HashMap<String, Object>();
			thisMap.put("id", id);
			thisMap.put("text", name);
			thisMap.put("type", "default");
			
			if ( !parentId.equals("#") ) {
				thisMap.put("parent", parentId);
			}
			if(userList.size() > 0 || chkChildren == true) {
				thisMap.put("children", true);
			}
			treeList.add(thisMap);
			
			}
		if(userList.size() > 0) {
			for(int j=0; j < userList.size(); j++) {					
				String userId = userList.get(j).getEmpCode();
				String userName = userList.get(j).getEmpName();
				String userCode = userList.get(j).getPartCode();
				
				Map<String, Object> userMap = new HashMap<String, Object>();
				userMap.put("id", userId);
				userMap.put("text", userName);
				userMap.put("type", "process");
				userMap.put("parent", userCode);
				treeList.add(userMap);
			}
		}	
		return treeList;
	}
	
	public static List<Map> getTreeByUserForJSTree(List<UserVO> userList, String partCode) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		List<Map> treeList = new ArrayList();
		for(int i = 0; i < userList.size(); i++){
			String id = userList.get(i).getEmpCode();
			String name = userList.get(i).getEmpName();
			String code = userList.get(i).getPartCode();
			
			Map<String, Object> thisMap = new HashMap<String, Object>();
			
			thisMap.put("id", id);
			thisMap.put("text", name);			
			thisMap.put("type", "default");
			thisMap.put("parent", code);
			treeList.add(thisMap);
		}
		return treeList;
	}
	

	public static List<Map> getTreeObjectByListForJSTree(List levelTree, String levelFieldName, String idFieldName, String textFieldName, int startLevel, String parentId, String typeFieldName, int cnt) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		
		List<Map> treeList = new ArrayList();
		
		for ( int i = 0; i < levelTree.size(); i++ ) {
			Object thisObject = levelTree.get(i);
			int thisLevel = Integer.parseInt((String) getFieldValue(thisObject, levelFieldName));
			String thisId = (String) getFieldValue(thisObject, idFieldName);
			String thisText = (String) getFieldValue(thisObject, textFieldName);
			String thisType = (String) getFieldValue(thisObject, typeFieldName);											
			
			if ( startLevel != thisLevel )
				continue;
			
			Map<String, Object> thisMap = new HashMap<String, Object>();
			
			thisMap.put("id", thisId);
			thisMap.put("text", thisText);
			thisMap.put("type", thisType);

			if ( !parentId.equals("#") )
				thisMap.put("parent", parentId);
		
				if ( i < levelTree.size()-1 ) {
					Object nextObject = levelTree.get(i+1);
					int nextLevel = Integer.parseInt((String) getFieldValue(nextObject, levelFieldName));
					if ( nextLevel == (thisLevel+1) ) {
						List childList = new ArrayList();
						for ( int j = i+1; j < levelTree.size(); j++ ) {
							Object childObject = levelTree.get(j);
							int childLevel = Integer.parseInt((String) getFieldValue(childObject, levelFieldName));
							if ( childLevel == thisLevel)	break;
							childList.add(levelTree.get(j));
						}
						thisMap.put("children", getTreeObjectByListForJSTree(childList, levelFieldName, idFieldName, textFieldName, nextLevel, thisId, typeFieldName));
					}
				}
			treeList.add(thisMap);
		}
		
		return treeList;
		
	}
	public static List<Map> getTreeObjectByListForJSTree(List levelTree, String levelFieldName, String idFieldName, String textFieldName, int startLevel, String parentId, String typeFieldName) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {		
		return getTreeObjectByListForJSTree(levelTree, levelFieldName, idFieldName, textFieldName, startLevel, parentId, typeFieldName);
	}
	
	public static Object getFieldValue(Object o, String fieldName) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException{
		Field field = o.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		return field.get(o);
	}
	
	public static int checkUserExistList(String partCode) throws Exception {
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT COUNT(GLOBALCOM) CNT FROM EMPTABLE ");
		sql.append(" WHERE PARTCODE = '" + partCode + "'");
		sql.append(" GROUP BY GLOBALCOM");
		
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		int cnt = 0;
		try {
			conn = DefaultConnectionFactory.create().getConnection();
			psmt = conn.prepareStatement(sql.toString());
			rs = psmt.executeQuery();
			
			while ( rs.next() ){
				OrganizationVO vo = new OrganizationVO();
				vo.setCnt(rs.getInt("cnt"));
				cnt = vo.getCnt();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			if(conn != null) {	conn.close();}
			if(psmt != null) {	psmt.close();}
			if(rs != null) {	rs.close();}
		}
		return cnt;
	}
	
	public static List<UserVO> selectUserListByPartCode(String partCode) throws Exception {
    	
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT ");
		sql.append(" EMPNAME, EMPCODE, JIKNAME, PARTCODE ");
		sql.append(" FROM EMPTABLE ");
		sql.append(" WHERE PARTCODE = '" + partCode + "'");
		
		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;

		List<UserVO> userList = new ArrayList<UserVO>();
		
		try {
			conn = DefaultConnectionFactory.create().getConnection();
			psmt = conn.prepareStatement(sql.toString());
			rs = psmt.executeQuery();
			
			while(rs.next()) {
				UserVO vo = new UserVO();
				vo.setEmpCode(rs.getString("empCode"));
				vo.setEmpName(rs.getString("empName"));
				vo.setPartCode(rs.getString("partCode"));
				vo.setJikName(rs.getString("jikName"));
				userList.add(vo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(conn != null) {	conn.close();}
			if(psmt != null) {	psmt.close();}
			if(rs != null) {	rs.close();}
		}
		return userList;
	}
		
}
