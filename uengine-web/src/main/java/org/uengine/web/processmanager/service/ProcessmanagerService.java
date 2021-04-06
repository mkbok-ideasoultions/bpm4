package org.uengine.web.processmanager.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.uengine.web.login.vo.LoginVO;
import org.uengine.web.process.vo.TreeVO;
import org.uengine.web.processmanager.vo.ProcessDefinitionVO;
import org.uengine.web.processmanager.vo.ProcessInstanceRoleVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableChangeVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableVO;
import org.w3c.dom.Document;

public interface ProcessmanagerService {
	
	public List<ProcessDefinitionVO> selectProcessDefinitionObjectListByDefId(String defId) throws Exception;

	public Document createJnlpXmlDocument(LoginVO sessionVO, HttpServletRequest request) throws Exception;

	public Document createJnlpXmlDocumentByInstanceId(LoginVO sessionVO, HttpServletRequest request) throws Exception;

	public String getProcessDefinitionXPDStringWithVersion(String version) throws Exception;

	public String getProcessDefinitionXPDString(String defId) throws Exception;

	public String getProcessDefinitionXPDStringWithInstanceId(String instanceId) throws Exception;

	public String saveProcessDefinition(Map<String,String> parameterMap) throws Exception;

	public List<TreeVO> getProcessTreeBycomCode(String comCode) throws Exception;
	
	public List<ProcessInstanceVO> getProcessInstanceListByComCode(List<Map<String,String>> params) throws Exception;

	public String addFolder(Map<String, String> parameterMap) throws Exception;

	public void moveFolder(String parent, String object) throws Exception;

	public void removeDefinition(String defId) throws Exception;

	public void removeProcessInstance(String instanceId) throws Exception;

	public void saveProcessInstanceDefinition(Map<String, String> paramMap) throws Exception;
	public void processVersionChange(List<Map<String,String>> params) throws Exception;
	
	public Document createDefinitionListXmlDocument() throws Exception;
	
	public Map<String,String> StringToMap(String body) throws Exception;
	public void forwordMapIntoPage(HttpServletRequest request, Map<String, String> map) throws Exception;
	
	public void stopProcessInstance(String instanceId) throws Exception;
	public void executeProcess(String instanceId) throws Exception;
	
	public List<ProcessInstanceVariableVO> getProcessInstanceVariables(String instanceId, String locale) throws Exception;
	
	public String getProcessVariableInXML(String instanceId, String scope, String varKey) throws Exception;
	
	public List<ProcessInstanceVariableChangeVO> getProcessVariableForChange(String instanceId, String scope, String varKey, String clsName) throws Exception;

	public void changeProcessVariable(String instanceId, String variableName, Object value) throws Exception;
	
	public String showFormInstance(String filePath) throws Exception;
	
	public String changeFormInstance(String contents, String instanceId, String variableName) throws Exception;
	
	public List<ProcessInstanceRoleVO> getProcessRolesForChange(String instanceId, String locale) throws Exception;
}
