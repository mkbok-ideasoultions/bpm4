package org.uengine.web.processmanager.vo;

import java.io.Serializable;
import java.util.List;

public class ProcessInstanceVariableVO implements Serializable {

	private static final long serialVersionUID = -820587010141790559L;
	private String varName;
	/**
	 * @return the varName
	 */
	public String getVarName() {
		return varName;
	}
	/**
	 * @param varName the varName to set
	 */
	public void setVarName(String varName) {
		this.varName = varName;
	}
	
	private String varDisplayName;
	
	public String getVarDisplayName() {
		return varDisplayName;
	}
	public void setVarDisplayName(String varDisplayName) {
		this.varDisplayName = varDisplayName;
	}

	private String varType;
	
	public String getVarType() {
		return varType;
	}
	public void setVarType(String varType) {
		this.varType = varType;
	}
	
	private String varFormContextFilePath;
	
	public String getVarFormContextFilePath() {
		return varFormContextFilePath;
	}
	public void setVarFormContextFilePath(String varFormContextFilePath) {
		this.varFormContextFilePath = varFormContextFilePath;
	}
	
	private Serializable data;
	
	public Serializable getData() {
		return data;
	}
	public void setData(Serializable data) {
		this.data = data;
	}
}