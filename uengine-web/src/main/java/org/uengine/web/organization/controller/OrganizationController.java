package org.uengine.web.organization.controller; 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.uengine.web.login.vo.LoginVO;
import org.uengine.web.organization.dao.OrganizationDAO;
import org.uengine.web.organization.service.OrganizationService;
import org.uengine.web.organization.vo.OrganizationVO;
import org.uengine.web.organization.vo.UserVO;
import org.uengine.web.processmanager.vo.ProcessDefinitionVO;
import org.uengine.web.processmanager.vo.ProcessInstanceVO;
import org.uengine.web.util.CommonUtil;
import org.w3c.dom.Document;

import com.defaultcompany.organization.web.chartpicker.Company;
import com.google.gson.Gson;

  @Controller 
  public class OrganizationController {

	private Logger log = Logger.getLogger(this.getClass());
	
	@Resource(name = "organizationService")
	private OrganizationService organizationService;
	
	private static Gson gson = new Gson();
	
	@RequestMapping(value = "/organizationmanager/")
	public ModelAndView approvalLineEditor(LoginVO sessionVO) throws Exception {
		ModelAndView mv = new ModelAndView("/organizationmanager/index", "command", new OrganizationVO());
		return mv;
	}
	
	@RequestMapping(value="/organization/popup/approvalLineEditor.do")
	public ModelAndView approvalLineEditor(LoginVO sessionVO, HttpServletRequest request) throws Exception{
        ModelAndView mv = new ModelAndView("/organization/popup/approvalLineEditor");
        
        return mv;
    }
	
	//최상위 노드 생성 로직
	@RequestMapping(value="/organization/ajax/selectOrganizationRootNode.do")
	public void selectOrganizationRootNode(LoginVO sessionVO, HttpServletRequest request, HttpServletResponse response )throws Exception{
		String comCode = sessionVO.getComCode();
		List<OrganizationVO> companyList = organizationService.selectOrganizationRootNode(comCode);
		
		Map<String, Object> thisMap = new HashMap<String, Object>();
		List<Map> treeList = new ArrayList();

		for(int i = 0; i < companyList.size(); i++) {
			String thisComCode = companyList.get(i).getComCode();
			String thisComName = companyList.get(i).getComName();
			String thisDescription = companyList.get(i).getDescription();
			int thisCnt = companyList.get(i).getCnt();
			
			thisMap.put("id", thisComCode);
			thisMap.put("text", thisComName);
			thisMap.put("type", "root");
			thisMap.put("parent", "#");
			thisMap.put("children", true);
			treeList.add(thisMap);
		}
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().print(gson.toJson(treeList));
	}
	
	//partcode 로 조직도 구성 로직
	@RequestMapping(value="/organization/ajax/selectOrganizationTreeByComCode.do")
	public void selectOrganizationChart(LoginVO sessionVO, HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String partCode = request.getParameter("partcode");
		List<OrganizationVO> organizationPartList = organizationService.selectOrganizationTreeByComCode(partCode);
		
		boolean chkChildren = false;
		if(organizationPartList != null && organizationPartList.size() > 0) {
			for(int i=0; i < organizationPartList.size(); i++) {
				String chkPartCode = organizationPartList.get(i).getPartCode();
				List<OrganizationVO> chkList = organizationService.selectOrganizationTreeByComCode(chkPartCode);
				if(chkList != null && chkList.size() > 0) {
					chkChildren = true;
				}
			}
		}
		
		//현재 선택된 노드의 PARTCODE 를 받아와 해당 PARTCODE 에 속해있는 사원 정보를 EMPTABLE 에서 가져온다(EMPTABLE)		
		List<UserVO>  organizationUserList = organizationService.selectUserListByPartCode(partCode);
		/*
		 * 현재 선택된 노드의 PARTCODE 를 받아와 현재의 PARTCODE 를 PARENT_PARTCODE 로 사용하는 바로 한단계 아래의 자식
		 * 부서를 가져온다.(PARTTABLE)
		 */
		List treeList = CommonUtil.getTreeByListForJSTree(organizationPartList, organizationUserList, "partCode", "parent_Partcode", "partName", chkChildren);
		
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(treeList));
    }
	
	/**
	 * @ Method Name	: viewCompanyInfo
	 * @ 작성일			: 2019. 3. 19.
	 * @ 작성자			: taejin
	 * @ 변경이력			:
	 * @ Method 설명		: 회사정보 보여주는 페이지 이동 
	 */
	@RequestMapping(value="/organization/viewCompanyInfo.do")
	public ModelAndView viewCompanyInfo(@ModelAttribute OrganizationVO organizationVO) throws Exception {
		ModelAndView mv = new ModelAndView("/organizationmanager/viewCompanyInfo", "command", organizationVO);
		return mv;
	}
	
	@RequestMapping(value="/organization/viewPartInfo.do")
	public ModelAndView viewPartInfo(@ModelAttribute OrganizationVO organizationVO) throws Exception {
		ModelAndView mv = new ModelAndView("/organizationmanager/viewPartInfo", "command", organizationVO);
		return mv;
	}
	
	@RequestMapping(value="/organization/viewEmpDetailInfo.do")
	public ModelAndView viewEmpInfo(@ModelAttribute OrganizationVO organizationVO) throws Exception {
		ModelAndView mv = new ModelAndView("/organizationmanager/viewEmpInfo", "command", organizationVO);
		return mv;
	}
	
	
	@RequestMapping(value="/organization/ajax/getCompanyInfo.do")
	public void getCompanyInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		List<OrganizationVO> companyInfoList = organizationService.getCompanyInfo(code);
		
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(companyInfoList));
	}
	
	@RequestMapping(value="/organization/ajax/getPartInfo.do")
	public void getPartInfo (LoginVO sessionVO,HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		
		List<OrganizationVO> partInfoList = organizationService.getPartInfo(code);		
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(partInfoList));                
	}
	
	@RequestMapping(value="/organization/ajax/getEmpInfo.do")
	public void getEmpInfo (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		
		List<OrganizationVO> empInfoList = organizationService.getEmpInfo(code);
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(empInfoList));
	}
	
	@RequestMapping(value="/organization/ajax/getPartDetailInfo.do")
	public void getPartDetailInfo (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		
		List<OrganizationVO> partDetailInfoList = organizationService.getPartDatailInfo(code);
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(partDetailInfoList));
	}
	
	@RequestMapping(value="/organization/ajax/getEmpDetailInfo.do")
	public void getEmpDetailInfo (HttpServletRequest request, HttpServletResponse response) throws Exception {
		String code = request.getParameter("code");
		
		List<OrganizationVO> empDetailInfoList = organizationService.getEmpDetailInfo(code);
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(empDetailInfoList));
	}
	
	@RequestMapping(value="/organization/ajax/updateEmpInfo.do")
	public void updateEmpInfo(@ModelAttribute OrganizationVO organizationVO, HttpServletRequest request, HttpServletResponse response) throws Exception {
		boolean chkValue = false;
		int cnt = organizationService.updateEmpInfo(organizationVO);
		if(cnt > 0) {
			chkValue = true;
		}		
		response.setContentType("text/html;charset=UTF-8");
        response.getWriter().print(gson.toJson(chkValue));
	}
	
	@RequestMapping(value="/organization/ajax/selectUserListByPartCode.do")
	public void selectUserListByPartCode(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		String partCode = request.getParameter("partCode");		
		List<UserVO> userList = organizationService.selectUserListByPartCode(partCode);
		
		for ( int i = 0; i < userList.size() && i % 2 == 0; i++) {
			userList.get(i).setAccessable(true);
		}		
		response.setContentType("text/html;charset=UTF-8");
		response.getWriter().print(gson.toJson(userList));		
	}
	
	@RequestMapping(value = "/organization/get/xml/organization/chart/{comCode}")
	public void getXmlOrganizationChart(HttpServletResponse response, @PathVariable String comCode) throws Exception {
		
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
		
		Document xmlDoc = organizationService.createOrgChartXmlDocument(comCode);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
		
		String result = writer.getBuffer().toString();
		System.out.println(result);
		response.getWriter().write(result);
	}
	
	@RequestMapping(value = "/organization/get/xml/group/chart/{comCode}")
	public void getXmlGroupChart(HttpServletResponse response, @PathVariable String comCode) throws Exception {
		
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
		
		Document xmlDoc = organizationService.createGroupChartXmlDocument(comCode);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
		
		String result = writer.getBuffer().toString();
		System.out.println(result);
		response.getWriter().write(result);
	}
	
	@RequestMapping(value = "/organization/get/xml/role/chart/{comCode}")
	public void getXmlRoleChart(HttpServletResponse response, @PathVariable String comCode) throws Exception {
		
		response.setContentType("text/xml; charset=UTF-8");
		response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
		response.setHeader("Pragma","no-cache"); //HTTP 1.0
		response.setDateHeader ("Expires", 0); //prevents caching at the proxy server
		
		Document xmlDoc = organizationService.createRoleChartXmlDocument(comCode);
		
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
		
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(xmlDoc), new StreamResult(writer));
		
		String result = writer.getBuffer().toString();
		System.out.println(result);
		response.getWriter().write(result);
	}
	
	@RequestMapping(value="/organization/testLink/download.do")
	public void download(ModelMap modelMap, HttpServletRequest request, HttpServletResponse response) {
        try {
            String imgData = request.getParameter("imgData");
            imgData = imgData.replaceAll("data:image/png;base64,", "");
 
            byte[] file = Base64.decodeBase64(imgData);
            ByteArrayInputStream is = new ByteArrayInputStream(file);
 
            response.setContentType("image/png");
            response.setHeader("Content-Disposition", "attachment; filename=report.png");
 
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 
    }
}
