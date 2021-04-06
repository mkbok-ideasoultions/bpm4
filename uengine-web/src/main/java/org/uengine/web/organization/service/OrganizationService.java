package org.uengine.web.organization.service;

import java.util.List;

import org.uengine.kernel.RoleMapping;
import org.uengine.web.organization.vo.OrganizationVO;
import org.uengine.web.organization.vo.UserVO;
import org.w3c.dom.Document;

import com.defaultcompany.organization.web.chartpicker.Company;

public interface OrganizationService {
	public List<OrganizationVO> selectOrganizationTreeByComCode(String partCode) throws Exception;
	public List<UserVO> selectUserListByPartCode(String partCode) throws Exception;
	public Document createOrgChartXmlDocument(String comCode) throws Exception;
	public Document createGroupChartXmlDocument(String comCode) throws Exception;
	public Document createRoleChartXmlDocument(String comCode) throws Exception;
	public List<RoleMapping> getPartListByComCode(String comCode) throws Exception;
	public List<OrganizationVO> selectOrganizationRootNode(String comCode) throws Exception;
	public List<Integer> checkUserListexist(String partCode) throws Exception;
	public List<OrganizationVO> getCompanyInfo(String partCode) throws Exception;
	public List<OrganizationVO> getPartInfo(String partCode) throws Exception;
	public List<OrganizationVO> getEmpInfo(String partCode) throws Exception;
	public List<OrganizationVO> getEmpDetailInfo(String code) throws Exception;
	public List<OrganizationVO> getPartDatailInfo(String code)throws Exception;
	public int updateEmpInfo(OrganizationVO organizationVO) throws Exception;

}
