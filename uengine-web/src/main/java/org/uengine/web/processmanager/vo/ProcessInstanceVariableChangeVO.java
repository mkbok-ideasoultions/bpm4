package org.uengine.web.processmanager.vo;

import java.io.Serializable;
import java.util.List;

public class ProcessInstanceVariableChangeVO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String inputterHtml;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInputterHtml() {
		return inputterHtml;
	}
	public void setInputterHtml(String inputterHtml) {
		this.inputterHtml = inputterHtml;
	}
	
}
