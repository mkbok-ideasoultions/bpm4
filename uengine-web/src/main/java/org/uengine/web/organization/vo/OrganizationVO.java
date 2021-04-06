package org.uengine.web.organization.vo;

import java.io.Serializable;

/**
 * <pre>
 * org.uengine.web.organization.vo 
 * OrganizationVO.java
 * 
 * </pre>
 * @date : 2016. 6. 20. 오전 10:55:06
 * @version : 
 * @author : next3
 */
public class OrganizationVO implements Serializable {

	private static final long serialVersionUID = 342794486746189970L;
	private String level;
	private String partCode;
    private String partName;
    private String description;
    private boolean isLeaf;
    private int lft, rgt;
    private boolean expanded = false;
    private String objType = "folder";
    
    //추가 항목들
    private String empCode;
    private String isAdmin;
    private String password;
    private String mobileNo;
    private String nateOn;
    private String msn;
	private String locale;
	private String empName;
    private String jikName;    
    private String globalCom;
    private String email;
    private String code;
    private String comCode;
    private String comName;
    private String type;
    private int cnt;
    private String parent_Partcode;
    
    public String getEmpCode() {
		return empCode;
	}
	public void setEmpCode(String empCode) {
		this.empCode = empCode;
	}
	public String getEmpName() {
		return empName;
	}
	public void setEmpName(String empName) {
		this.empName = empName;
	}
	public String getJikName() {
		return jikName;
	}
	public void setJikName(String jikName) {
		this.jikName = jikName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getGlobalCom() {
		return globalCom;
	}
	public void setGlobalCom(String globalCom) {
		this.globalCom = globalCom;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public String getIsAdmin() {
		return isAdmin;
	}
	public void setIsAdmin(String isAdmin) {
		this.isAdmin = isAdmin;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMobileNo() {
		return mobileNo;
	}
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	public String getNateOn() {
		return nateOn;
	}
	public void setNateOn(String nateOn) {
		this.nateOn = nateOn;
	}
	public String getMsn() {
		return msn;
	}
	public void setMsn(String msn) {
		this.msn = msn;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}


    
	/**
	 * @return the objType
	 */
	public String getObjType() {
		return objType;
	}
	/**
	 * @param objType the objType to set
	 */
	public void setObjType(String objType) {
		this.objType = objType;
	}
	/**
	 * @return the expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}
	/**
	 * @param expanded the expanded to set
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}
	/**
	 * @return the lft
	 */
	public int getLft() {
		return lft;
	}
	/**
	 * @param lft the lft to set
	 */
	public void setLft(int lft) {
		this.lft = lft;
	}
	/**
	 * @return the rgt
	 */
	public int getRgt() {
		return rgt;
	}
	/**
	 * @param rgt the rgt to set
	 */
	public void setRgt(int rgt) {
		this.rgt = rgt;
	}
	/**
	 * @return the isLeaf
	 */
	public boolean isLeaf() {
		return isLeaf;
	}
	/**
	 * @param isLeaf the isLeaf to set
	 */
	public void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}
	/**
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}
	/**
	 * @param level the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}
	/**
	 * @return the partCode
	 */
	public String getPartCode() {
		return partCode;
	}
	/**
	 * @param partCode the partCode to set
	 */
	public void setPartCode(String partCode) {
		this.partCode = partCode;
	}
	/**
	 * @return the partName
	 */
	public String getPartName() {
		return partName;
	}
	/**
	 * @param partName the partName to set
	 */
	public void setPartName(String partName) {
		this.partName = partName;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	public int getCnt() {
		return cnt;
	}
	public void setCnt(int cnt) {
		this.cnt = cnt;
	}
	public String getComCode() {
		return comCode;
	}
	public void setComCode(String comCode) {
		this.comCode = comCode;
	}
	public String getComName() {
		return comName;
	}
	public void setComName(String comName) {
		this.comName = comName;
	}
	public String getParent_Partcode() {
		return parent_Partcode;
	}
	public void setParent_Partcode(String parent_Partcode) {
		this.parent_Partcode = parent_Partcode;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

}
