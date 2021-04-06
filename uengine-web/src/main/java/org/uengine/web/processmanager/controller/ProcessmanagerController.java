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
package org.uengine.web.processmanager.controller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.metaworks.ObjectInstance;
import org.metaworks.ObjectType;
import org.metaworks.web.WebUtil;
import org.springframework.context.support.DelegatingMessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.uengine.kernel.Activity;
import org.uengine.kernel.GlobalContext;
import org.uengine.web.login.vo.LoginVO;
import org.uengine.web.process.vo.TreeVO;
import org.uengine.web.processmanager.service.ProcessmanagerService;
import org.uengine.processmanager.ProcessManagerFactoryBean;
import org.uengine.processmanager.ProcessManagerRemote;
import org.uengine.web.processmanager.vo.ProcessDefinitionVO;
import org.uengine.web.processmanager.vo.ProcessInstanceRoleVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableChangeVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVariableVO;
import org.w3c.dom.Document;

@Controller
public class ProcessmanagerController {



	private Logger log = Logger.getLogger(this.getClass());
	
	@Resource(name = "messageSource")
	private DelegatingMessageSource messageSource;

	@Resource(name = "processmanagerService")
	private ProcessmanagerService processmanagerService;
	
	private static ObjectMapper mapper = new ObjectMapper();


	@RequestMapping(value = "/processmanager/")
	public ModelAndView approvalLineEditor(LoginVO sessionVO) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/index", "command", new ProcessDefinitionVO());
		return mv;
	}

	@RequestMapping(value = "/processmanager/viewInstanceList")
	public ModelAndView viewInstanceList(LoginVO sessionVO) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/viewInstanceList", "command", new ProcessInstanceVO());
		
		//status list
		String a = null;
		String[] args = {};
		Locale locale = new Locale(sessionVO.getLocale());
		Map<String, String> statusMap = new LinkedHashMap<String, String>();
		statusMap.put("all",						messageSource.getMessage("instance.status.all", args, locale) 	);
		statusMap.put(Activity.STATUS_RUNNING,		messageSource.getMessage("instance.status.running", args, locale) 	);
		statusMap.put(Activity.STATUS_COMPLETED,	messageSource.getMessage("instance.status.completed", args, locale) );
		statusMap.put(Activity.STATUS_STOPPED,		messageSource.getMessage("instance.status.stopped", args, locale) 	);
		statusMap.put(Activity.STATUS_CANCELLED,	messageSource.getMessage("instance.status.cancelled", args, locale) );
		statusMap.put(Activity.STATUS_FAULT,		messageSource.getMessage("instance.status.failed", args, locale) 	);
				
				mv.addObject("statusOptions", statusMap);
		return mv;
	}

	@RequestMapping(value = "/processmanager/instanceList.do")
	public ModelAndView getInstanceListPage(@ModelAttribute ProcessInstanceVO instance) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/instanceList", "command", instance);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/put/instance/remove/{instanceId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> removeInstance(@PathVariable String instanceId) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		try {
			processmanagerService.removeProcessInstance(instanceId);
			result.put("status", "success");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return result;
	}

	@RequestMapping(value = "/processmanager/list/instanceList", method = RequestMethod.POST)
	@ResponseBody
	public List<ProcessInstanceVO> getInstanceList(@RequestBody String body) throws Exception {
		System.out.println("body: " + body);
		List<Map<String,String>> params = mapper.readValue(body, List.class);
		return processmanagerService.getProcessInstanceListByComCode(mapper.readValue(body, List.class));
	}
	
	@RequestMapping(value = "/processmanager/processVersionChange", method = RequestMethod.POST)
	@ResponseBody
	public boolean processVersionChange(@RequestBody String body) throws Exception {
		List<Map<String,String>> params = mapper.readValue(body, List.class);
		processmanagerService.processVersionChange(params);
		return true;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessDefinition.do")
	public ModelAndView viewProcessDefinition(@ModelAttribute ProcessDefinitionVO pd) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessDefinition", "command", pd);
		return mv;
	}

	@RequestMapping(value = "/processmanager/viewProcessDefinitionFlowChart.do")
	public ModelAndView viewProcessDefinitionFlowChart(@ModelAttribute ProcessDefinitionVO pd, @RequestBody String body) throws Exception {
		System.out.println("body22: " + body);

		System.out.println("ProcessDefinitionVO :" + pd);
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessDefinitionFlowChart", "command", pd);
		return mv;
	}

	@RequestMapping(value = "/processmanager/viewProcessInstance.do")
	public ModelAndView viewProcessInstance(@ModelAttribute ProcessInstanceVO instanceVO) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstance", "command", instanceVO);
		return mv;
	}
	
	/**
	 * 상세페이지에서 stop인지를 판단하기 위해 status를 변화시킨 instanceVo를 가지고 리턴한다.
	 * 뒤로가기로 상세페이지를 들어온 경우, instanceId가 null이기 때문에 인스턴스 목록 페이지로 돌아가도록 한다.
	 * @param instanceVO : 현재 진행중인 프로세스 인스턴스의 VO
	 * @author skk
	 * @return ModelAndView : 액티비티를 멈춘뒤, status가 변했다는 값("statusChanged", 뒤로가기 방지 스크립트 작동 트리거)과 함께 상세페이지로 돌아간다. 
	 * @throws Exception
	 */
	@RequestMapping(value = "/processmanager/put/instance/stop")
	public ModelAndView stopProcessInstance(@ModelAttribute ProcessInstanceVO instanceVO) throws Exception {
		
		String pi = instanceVO.getInstId();
		if(pi == null) {
			return getInstanceListPage(instanceVO);
		}
		
		processmanagerService.stopProcessInstance(pi);
		
		instanceVO.setStatus(Activity.STATUS_STOPPED);
		
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstance", "command", instanceVO);
		mv.addObject("statusChanged", true);
		return mv;
	}
	
	/**
	 * 상세페이지에서 start인지를 판단하기 위해 status를 변화시킨 instanceVo를 가지고 리턴한다.
	 * 뒤로가기로 상세페이지를 들어온 경우, instanceId가 null이기 때문에 인스턴스 목록 페이지로 돌아가도록 한다.
	 * @param instanceVO : 현재 진행중인 프로세스 인스턴스의 VO
	 * @author skk
	 * @return ModelAndView : 액티비티 재시작 하고, status가 변했다는 값("statusChanged", 뒤로가기 방지 스크립트 작동 트리거)과 함께 상세페이지로 돌아간다. 
	 * @throws Exception
	 */
	@RequestMapping(value = "/processmanager/put/instance/start" )
	public ModelAndView startProcessInstance(@ModelAttribute ProcessInstanceVO instanceVO) throws Exception {
		
		String pi = instanceVO.getInstId();
		if(pi == null) {
			return getInstanceListPage(instanceVO);
		}
		
		processmanagerService.executeProcess(pi);
		
		instanceVO.setStatus(Activity.STATUS_RUNNING); // 정확한 문자 확인 필요
		
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstance", "command", instanceVO);
		mv.addObject("statusChanged", true);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessFlowChart.do")
	public ModelAndView viewProcessFlowChart(@RequestBody String body, HttpServletRequest request) throws Exception {
		// 2018
		Map<String, String> map = processmanagerService.StringToMap(body);
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessFlowChart", "command", body);
		processmanagerService.forwordMapIntoPage(request, map);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessInstanceFlowChart.do")
	public ModelAndView viewProcessInstanceFlowChart(@ModelAttribute ProcessInstanceVO instanceVO) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstanceFlowChart", "command", instanceVO);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessInstanceProcVars.do")
	public ModelAndView viewProcessInstanceProcVars(@ModelAttribute ProcessInstanceVO instanceVO, LoginVO sessionVO) throws Exception {
		
		String pi = instanceVO.getInstId();
		Locale locale = new Locale(sessionVO.getLocale());
		//
		List<ProcessInstanceVariableVO> processVariables = processmanagerService.getProcessInstanceVariables(pi, locale.toString());
		
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstanceProcVars", "processVariables", processVariables);
		mv.addObject("command", instanceVO);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/createFormDefinition.do")
	public ModelAndView createFormDefinition(@ModelAttribute ProcessDefinitionVO pd) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/createFormDefinition", "command", pd);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/formDefinitionEditor.do")
	public ModelAndView formDefinitionEditor(@ModelAttribute ProcessDefinitionVO pd) throws Exception {
		ModelAndView mv = new ModelAndView("/processmanager/formDefinitionEditor", "command", pd);
		return mv;
	}

	@RequestMapping(value = "/processmanager/processDesigner.jnlp")
	public void executeProcessDesigner(LoginVO sessionVO, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("application/x-java-jnlp-file; charset=UTF-8");
		response.setHeader("Cache-Control", "public");
		response.setHeader("Expires", "0");
		
       	Document xmlDoc = processmanagerService.createJnlpXmlDocument(sessionVO, request);
       	
       	TransformerFactory tf = TransformerFactory.newInstance();
       	Transformer transformer = tf.newTransformer();
       	transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
       	
       	StringWriter writer = new StringWriter();
       	transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
       	
       	String result = writer.getBuffer().toString();
       	response.getWriter().write(result);
	}
	
	@RequestMapping(value = "/processmanager/processDesignerInstance.jnlp")
	public void executeProcessDesignerByInstanceId(LoginVO sessionVO, HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		response.setContentType("application/x-java-jnlp-file; charset=UTF-8");
		response.setHeader("Cache-Control", "public");
		response.setHeader("Expires", "0");
		
		Document xmlDoc = processmanagerService.createJnlpXmlDocumentByInstanceId(sessionVO, request);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
		
		String result = writer.getBuffer().toString();
		response.getWriter().write(result);
	}
	
	@RequestMapping(value = "/processmanager/processtree/list/{comCode}", method = RequestMethod.POST)
	@ResponseBody
	public List<TreeVO> getProcessTreeBycomCode(@PathVariable String comCode) throws Exception {
		return processmanagerService.getProcessTreeBycomCode(comCode);
	}

	@RequestMapping(value = "/processmanager/ajax/selectProcessDefinitionObjectListByDefId.do")
	public void selectProcessDefinitionObjectListByDefId(LoginVO sessionVO, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
		String defId = request.getParameter("defId");
		
		List<ProcessDefinitionVO> definitionObjectList = processmanagerService.selectProcessDefinitionObjectListByDefId(defId);
		
		response.setContentType("text/html;charset=UTF-8");
		log.info("selectProcessDefinitionObjectListByDefId");
		log.info(definitionObjectList);
		response.getWriter().print(mapper.writeValueAsString(definitionObjectList));
	}
	
	@RequestMapping(value = "/processmanager/get/definition/xpd/version/{version}", method = RequestMethod.POST)
	public void getProcessDefinitionXPDStringWithVersion(@PathVariable String version, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");	
		response.getWriter().write(processmanagerService.getProcessDefinitionXPDStringWithVersion(version));
	}
	
	@RequestMapping(value = "/processmanager/get/definition/xpd/{defId}", method = RequestMethod.POST)
	public void getProcessDefinitionXPDString(@PathVariable String defId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");	
		response.getWriter().write(processmanagerService.getProcessDefinitionXPDString(defId));
	}
	
	@RequestMapping(value = "/processmanager/get/instance/xpd/{instanceId}", method = RequestMethod.POST)
	public void getProcessDefinitionXPDStringWithInstanceId(@PathVariable String instanceId, HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setContentType("text/html;charset=UTF-8");	
		response.getWriter().write(processmanagerService.getProcessDefinitionXPDStringWithInstanceId(instanceId));
	}

	@RequestMapping(value = "/processmanager/put/definition", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,String> saveProcessDefinition(@RequestBody String body) {
		
		Map<String, String> parameterMap = null;
		try {
			parameterMap = mapper.readValue(body, Map.class);
			String defVerId = processmanagerService.saveProcessDefinition(parameterMap);
			parameterMap.put("defVerId", defVerId);
			parameterMap.put("status", "success");
		} catch (Exception e) {
			parameterMap.put("status", "fail");
			parameterMap.put("message", e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return parameterMap;
		
	}
	
	@RequestMapping(value = "/processmanager/put/instance/definition", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,String> saveProcessInstanceDefinition(@RequestBody String body) {
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		try {
			Map<String, String> paramMap = mapper.readValue(body, Map.class);
			processmanagerService.saveProcessInstanceDefinition(paramMap);
			parameterMap.put("status", "success");
		} catch (Exception e) {
			parameterMap.put("status", "fail");
			parameterMap.put("message", e.getLocalizedMessage());
			e.printStackTrace();
		}
		
		return parameterMap;
		
	}
	
	@RequestMapping(value = "/processmanager/put/folder", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> addFolder(@RequestBody String body) throws Exception {
		Map<String, String> parameterMap = null;
		try {
			parameterMap = mapper.readValue(body, Map.class);
			String defVerId = processmanagerService.addFolder(parameterMap);
			parameterMap.put("id", defVerId);
			parameterMap.put("text", parameterMap.get("folderName"));
			parameterMap.put("status", "success");
		} catch (Exception e) {
			throw e;
		}
		
		return parameterMap;
	}

	@RequestMapping(value = "/processmanager/put/folder/move/{parent}/{object}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> moveFolder(@PathVariable String parent, @PathVariable String object) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		try {
			processmanagerService.moveFolder(parent, object);
			result.put("status", "success");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return result;
	}
	
	@RequestMapping(value = "/processmanager/put/folder/remove/{defId}", method = RequestMethod.POST)
	@ResponseBody
	public Map<String, String> removeItem(@PathVariable String defId) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		try {
			processmanagerService.removeDefinition(defId);
			result.put("status", "success");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return result;
	}
	
	@RequestMapping(value = "/processmanager/get/list/xml/definition/process/")
	public void getXmlRoleChart(HttpServletResponse response) throws Exception {
		
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
		
		Document xmlDoc = processmanagerService.createDefinitionListXmlDocument();
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
		
		String result = writer.getBuffer().toString();
		response.getWriter().write(result);
	}
	
	@RequestMapping(value = "/processmanager/processDefinitionListXML")
	public ModelAndView processDefinitionListXML() throws Exception {
		System.out.println("processDefinitionListXML printOut");
		ModelAndView mv = new ModelAndView("/processmanager/processDefinitionListXML");
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessVariableInXML/{instId}/{variableName}")
	public ModelAndView viewProcessVariableInXML(@PathVariable String instId, @PathVariable String variableName) throws Exception {
		String value = processmanagerService.getProcessVariableInXML(instId, "", variableName);
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessVariableInXML", "value", value);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/processVariableChangeForm/{instId}/{variableName}/{dataClassName:.+}")
	public ModelAndView processVariableChangeForm(@PathVariable String instId, @PathVariable String variableName, @PathVariable("dataClassName") String dataClassName) throws Exception {
		List<ProcessInstanceVariableChangeVO> changeVOs = processmanagerService.getProcessVariableForChange(instId, "", variableName, dataClassName);
		ModelAndView mv = new ModelAndView("/processmanager/processVariableChangeForm", "command", changeVOs);
		mv.addObject("instanceId", instId);
		mv.addObject("variableName", variableName);
		mv.addObject("dataClassName", dataClassName);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/processVariableChange")
	public ModelAndView processVariableChange(HttpServletRequest request) throws Exception {
		request.setCharacterEncoding("UTF-8");
		String instanceId = request.getParameter("instanceId");
		String variableName = request.getParameter("variableName");
		String clsName = request.getParameter("dataClassName");
		
		Class outputCls = GlobalContext.getComponentClass(clsName);
		ObjectInstance rec = (ObjectInstance)WebUtil.createInstance(new ObjectType(outputCls), request.getParameterMap(), "", null);
		Object value = rec.getObject();
		
		processmanagerService.changeProcessVariable(instanceId, variableName, value);
		ModelAndView mv = new ModelAndView("/processmanager/processVariableChange", "value", value);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/showFormInstance.do")
	public ModelAndView showFormInstance(@RequestParam String instanceId, @RequestParam String variableName, @RequestParam String filePath, HttpServletRequest request) throws Exception {
		String value = processmanagerService.showFormInstance(filePath);
		ModelAndView mv = new ModelAndView("/processmanager/showFormInstance")
				.addObject("value", value)
				.addObject("instanceId", instanceId)
				.addObject("variableName", variableName);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/formInstanceChange.do")
	public ModelAndView formInstanceChange(@RequestParam String instanceId, @RequestParam String variableName, @RequestParam String contents) throws Exception {
		String filePath = processmanagerService.changeFormInstance(contents, instanceId, variableName);
		ModelAndView mv = new ModelAndView("/processmanager/formInstanceChange", "filePath", filePath);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/viewProcessInstanceRoles.do")
	public ModelAndView viewProcessInstanceRoles(@ModelAttribute ProcessInstanceVO instanceVO, LoginVO sessionVO) throws Exception {
		String instanceId = instanceVO.getInstId();
		Locale locale = new Locale(sessionVO.getLocale());
		List<ProcessInstanceRoleVO> processRoles = processmanagerService.getProcessRolesForChange(instanceId, locale.toString());
		ModelAndView mv = new ModelAndView("/processmanager/viewProcessInstanceRoles", "processRoles", processRoles);
		mv.addObject("command", instanceVO);
		return mv;
	}
	
	@RequestMapping(value = "/processmanager/roleMappingChangeForm")
	public ModelAndView roleMappingChangeForm(@RequestParam String multiple, @RequestParam String instanceId, @RequestParam String roleName, @RequestParam String oldValue) throws Exception {
		//String filePath = processmanagerService.changeFormInstance(contents, instanceId, variableName);
		ModelAndView mv = new ModelAndView("/processmanager/roleMappingChangeForm")
				.addObject("instanceId", instanceId)
				.addObject("roleName", roleName)
				.addObject("oldValue", oldValue);
		return mv;
	}
}
