package org.uengine.web.processmanager.vo;

import java.io.Serializable;
import java.util.List;

import org.uengine.kernel.RoleMapping;

public class ProcessInstanceRoleVO implements Serializable {

	private static final long serialVersionUID = 1964024470837101238L;
	
	private String displayName;
	private String name;
	private String endPoint;
	private RoleMapping roleMapping;
	
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEndPoint() {
		return endPoint;
	}
	public void setEndPoint(String endPoint) {
		this.endPoint = endPoint;
	}
	public RoleMapping getRoleMapping() {
		return roleMapping;
	}
	public void setRoleMapping(RoleMapping roleMapping) {
		this.roleMapping = roleMapping;
	}
}