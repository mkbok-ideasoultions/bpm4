package org.uengine.web.service.service;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.annotation.Resource;
import javax.ejb.RemoveException;
import javax.servlet.http.HttpServletRequest;

import kr.go.nyj.components.activity.EventHumanActivity;
import kr.go.nyj.util.CommonUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.uengine.kernel.Activity;
import org.uengine.kernel.ActivityReference;
import org.uengine.kernel.AllActivity;
import org.uengine.kernel.ComplexActivity;
import org.uengine.kernel.EndActivity;
import org.uengine.kernel.GlobalContext;
import org.uengine.kernel.HumanActivity;
import org.uengine.kernel.KeyedParameter;
import org.uengine.kernel.ProcessDefinition;
import org.uengine.kernel.ProcessInstance;
import org.uengine.kernel.ResultPayload;
import org.uengine.kernel.Role;
import org.uengine.kernel.RoleMapping;
import org.uengine.kernel.SubProcessActivity;
import org.uengine.kernel.SystemActivity;
import org.uengine.kernel.UEngineException;
import org.uengine.kernel.viewer.ViewerOptions;
import org.uengine.persistence.dao.DAOFactory;
import org.uengine.processmanager.ProcessManagerBean;
import org.uengine.processmanager.ProcessTransactionContext;
import org.uengine.processmanager.TransactionContext;
import org.uengine.util.ActivityForLoop;
import org.uengine.util.UEngineUtil;
import org.uengine.util.dao.ConnectiveDAO;
import org.uengine.util.dao.DefaultConnectionFactory;
import org.uengine.util.dao.IDAO;
import org.uengine.web.service.dao.BpmServiceDAO;
import org.uengine.web.service.vo.AuthorityVO;
import org.uengine.web.service.vo.CommentVO;
import org.uengine.web.service.vo.CurrentActivityVO;
import org.uengine.web.service.vo.FlowchartVO;
import org.uengine.web.service.vo.ProcessRole;
import org.uengine.web.service.vo.ProcessRoleId;
import org.uengine.web.service.vo.ProcessVariable;
import org.uengine.web.service.vo.ProcessVariableValue;
import org.uengine.web.service.vo.ServiceVO;
import org.uengine.web.service.vo.WorkListVO;
import org.uengine.web.service.vo.WorkVO;
import org.uengine.web.worklist.vo.MyWorkVO;

import com.google.gson.Gson;
import com.kriss.activity.KrissHumanActivity;

@Service("bpmService")
public class BpmServiceImpl implements BpmService {

	private Logger log = Logger.getLogger(this.getClass());

	@Resource(name = "bpmServiceDAO")
	private BpmServiceDAO bpmServiceDAO;

	/* ????????? ?????? ?????? ???????????? */
	private static final String ACTION_CODE_COMELETE = "01"; // 01: ??????
	private static final String ACTION_CODE_SAVEONLY = "02"; // 02: ??????
	private static final String ACTION_CODE_COLLECTION = "03"; // 03: ??????
	private static final String ACTION_CODE_REJECT = "04"; // 04: ??????
	private static final String ACTION_CODE_DELEGATE = "05"; // 05: ??????

	/* ???????????? ?????? ???????????? */
	private static final String ACTION_CODE_PROCESS_DELETE = "06"; // 06: ???????????? ??????
	private static final String ACTION_CODE_PROCESS_COMPLETE = "07"; // 07: ???????????? ??????
	private static final String ACTION_CODE_PROCESS_STOP = "08"; // 08: ???????????? ??????
	private static final String ACTION_CODE_PROCESS_CANCEL = "09"; // 09: ???????????? ??????
	private static final String ACTION_CODE_PROCESS_RESTART = "10"; // 10: ???????????? ?????????
	private static final String ACTION_CODE_PROCESS_CHANGE_DUEDATE = "11"; // 11: ???????????? ???????????? ?????? ??????

	/* ????????? ?????? ?????? ???????????? */
	private static final String ACTION_CODE_ACTIVITY_COMPENSATE = "12"; // 12: ????????????
	private static final String ACTION_CODE_ACTIVITY_SUSPEND = "13"; // 13: ????????????
	private static final String ACTION_CODE_ACTIVITY_RESUME = "14"; // 14: ??????
	private static final String ACTION_CODE_ACTIVITY_SKIP = "15"; // 15: ????????????

	public static final String STATUS_READY_UC = Activity.STATUS_READY.toUpperCase();
	public static final String STATUS_COMPLETED_UC = Activity.STATUS_COMPLETED.toUpperCase();
	public static final String STATUS_FAULT_UC = Activity.STATUS_FAULT.toUpperCase();
	public static final String STATUS_RETRYING_UC = Activity.STATUS_RETRYING.toUpperCase();
	public static final String STATUS_RUNNING_UC = Activity.STATUS_RUNNING.toUpperCase();
	public static final String STATUS_SUSPENDED_UC = Activity.STATUS_SUSPENDED.toUpperCase();
	public static final String STATUS_SKIPPED_UC = Activity.STATUS_SKIPPED.toUpperCase();
	public static final String STATUS_STOPPED_UC = Activity.STATUS_STOPPED.toUpperCase();
	public static final String STATUS_TIMEOUT_UC = Activity.STATUS_TIMEOUT.toUpperCase();
	public static final String STATUS_CANCELLED_UC = Activity.STATUS_CANCELLED.toUpperCase();
	public static final String STATUS_CONFIRMED_UC = Activity.STATUS_CONFIRMED.toUpperCase();

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : getFlowChartByPdVer
	 * @date : 2016. 7. 14.
	 * @author : mkbok_Enki
	 * @history :
	 * 
	 * @see org.uengine.web.service.service.BpmService#getFlowChartByPdVer(org.uengine.web.service.vo.FlowchartVO)
	 * @param flowchartVO
	 * @return
	 * @throws Exception
	 */
	@Override
	public void getFlowChartByPdVer(FlowchartVO flowchartVO) throws Exception {
		ViewerOptions options = new ViewerOptions();
		options.setViewType(ViewerOptions.VERTICAL, ViewerOptions.VERTICAL);
		options.put("imagePathRoot", "../resources/images/flowchart/");
		options.put("flowControl", new Boolean(true));
		options.put("decorated", new Boolean(true));
		options.put("show hidden activity", new Boolean(true));
		options.put("ShowAllComplexActivities", new Boolean(true));
		options.put("align", "center");
		options.put("locale", GlobalContext.getDefaultLocale());
		options.put("enableUserEvent_viewFormDefinition_org.uengine.kernel.FormActivity", "View Form Definition");
		options.put("enableUserEvent_drillInto_org.uengine.kernel.SubProcessActivity", "Drill Into");
		ProcessManagerBean processManagerBean = new ProcessManagerBean();
		try {
			flowchartVO
					.setChart(processManagerBean.viewProcessDefinitionFlowChart(flowchartVO.getPdVersionId(), options));
			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			e.printStackTrace();
			throw e;
		} finally {
			processManagerBean.remove();
		}
	}

	@Override
	public void getFlowChartByInstanceId(FlowchartVO flowchartVO) throws Exception {
		ViewerOptions options = new ViewerOptions();
		options.setViewType(ViewerOptions.VERTICAL, ViewerOptions.VERTICAL);
		options.put("imagePathRoot", "../resources/images/flowchart/");
		options.put("flowControl", new Boolean(true));
		options.put("decorated", new Boolean(true));
		options.put("show hidden activity", new Boolean(true));
		options.put("ShowAllComplexActivities", new Boolean(true));
		options.put("align", "center");
		options.put("locale", GlobalContext.getDefaultLocale());
		options.put("enableUserEvent_viewFormDefinition_org.uengine.kernel.FormActivity", "View Form Definition");
		options.put("enableUserEvent_drillInto_org.uengine.kernel.SubProcessActivity", "Drill Into");
		ProcessManagerBean processManagerBean = new ProcessManagerBean();
		try {
			flowchartVO.setChart(processManagerBean.viewProcessInstanceFlowChart(flowchartVO.getInstanceId(), options));
			processManagerBean.applyChanges();
		} catch (Exception e) {
			processManagerBean.cancelChanges();
			e.printStackTrace();
			throw e;
		} finally {
			processManagerBean.remove();
		}
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : executeService
	 * @date : 2016. 7. 21.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 7. 21. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @see org.uengine.web.service.service.BpmService#executeService(org.uengine.web.service.vo.ServiceVO)
	 * @param serviceVO
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@Override
	public ServiceVO executeService(ServiceVO serviceVO, HttpServletRequest request) throws UEngineException {
		ProcessManagerBean processManagerBean = new ProcessManagerBean();

		try {
			String bizKey = serviceVO.getBizKey(); // ??????????????????
			String actionCode = serviceVO.getActionCode(); // ????????????????????????
			String processCode = serviceVO.getProcessCode(); // ??????????????????
			String userId = serviceVO.getLoginUserId(); // ????????? ID
			String statusCode = serviceVO.getStatusCode(); // ??????????????????
			List<ProcessRole> processRoles = serviceVO.getProcessRoles(); // ?????????????????????
			List<ProcessVariable> processVariables = serviceVO.getProcessVariables(); // ??????????????????
			String delegateRoleCode = serviceVO.getDelegateRoleCode(); // ?????????????????????
			List<ProcessRoleId> delegateIds = serviceVO.getDelegateIds(); // ????????? ID
			boolean isSystemCall = serviceVO.getIsSystemCall(); // ?????????????????????
			String dueDate = serviceVO.getDueDate(); // ??????????????????
			String comment = serviceVO.getComment(); // ????????????
			boolean isSubProcess = serviceVO.getIsSubProcess(); // ????????????????????????

			checkMandatory(actionCode, processManagerBean, bizKey, processCode, userId, statusCode, delegateIds,
					dueDate); // ??????????????????
			String instanceId = getInstanceId(processManagerBean, bizKey, actionCode, processCode, userId,
					isSubProcess); // ???????????? ID ??????

			ProcessInstance instance = null;
			HumanActivity workHumanActivity = null;
			SystemActivity systemActivity = null;
			boolean isSystemActivity = false;
			boolean isNew = false; // ????????????????????????

			Map<String, Object> genericContext = new HashMap<String, Object>();
			genericContext.put("request", request);
			RoleMapping loggedRm = null;

			if (UEngineUtil.isNotEmpty(userId)) {
				loggedRm = RoleMapping.create();
				loggedRm.beforeFirst();
				loggedRm.setEndpoint(userId);
				genericContext.put(HumanActivity.GENERICCONTEXT_CURR_LOGGED_ROLEMAPPING, loggedRm);
			}
			processManagerBean.setGenericContext(genericContext);

			if (actionCode.equals(ACTION_CODE_COMELETE) || actionCode.equals(ACTION_CODE_SAVEONLY)
					|| actionCode.equals(ACTION_CODE_COLLECTION) || actionCode.equals(ACTION_CODE_REJECT)
					|| actionCode.equals(ACTION_CODE_DELEGATE)) {

				if (instanceId == null) { // ???????????? ??? ??????
					if (isSubProcess) { // ?????????????????? ??? ??????
						throw new UEngineException(UEngineException.ERR_CODE_INSTANCE_ID_IS_NULL,
								getErrorMessagefByErrorCode(UEngineException.ERR_CODE_INSTANCE_ID_IS_NULL,
										processManagerBean));
					} else {
						checkDuplicateBizKey(processManagerBean, bizKey); // ?????????????????? ????????????
						String defVerId = processManagerBean.getProcessDefinitionProductionVersionByAlias(processCode); // ????????????????????????
																														// ID
						instanceId = processManagerBean.initializeProcess(defVerId); // ???????????? ID ??????
						instance = processManagerBean.getProcessInstance(instanceId); // ???????????? ??????
						instance.setName(bizKey); // ???????????? ??? ??????
						isNew = true;
					}
				}
			} else if (actionCode.equals(ACTION_CODE_PROCESS_DELETE) || actionCode.equals(ACTION_CODE_PROCESS_COMPLETE)
					|| actionCode.equals(ACTION_CODE_PROCESS_STOP) || actionCode.equals(ACTION_CODE_PROCESS_CANCEL)
					|| actionCode.equals(ACTION_CODE_PROCESS_RESTART)
					|| actionCode.equals(ACTION_CODE_PROCESS_CHANGE_DUEDATE)
					|| actionCode.equals(ACTION_CODE_ACTIVITY_COMPENSATE)
					|| actionCode.equals(ACTION_CODE_ACTIVITY_SUSPEND) || actionCode.equals(ACTION_CODE_ACTIVITY_RESUME)
					|| actionCode.equals(ACTION_CODE_ACTIVITY_SKIP)) {

				if (instanceId == null) {
					throw new UEngineException(UEngineException.ERR_CODE_INSTANCE_ID_IS_NULL,
							getErrorMessagefByErrorCode(UEngineException.ERR_CODE_INSTANCE_ID_IS_NULL,
									processManagerBean));
				}
			}

			if (instance == null) {
				instance = processManagerBean.getProcessInstance(instanceId); // ???????????? ??????
			}

			setProcessRoles(instance, processRoles); // ????????????????????? ??????
			setProcessVariables(instance, processVariables); // ?????????????????? ??????

			int iActionCode = Integer.parseInt(actionCode);

			/*
			 * ?????????: 2018-06-29 ?????????: ????????? ????????????: BPMUtil??? ????????? SystemActivity ??????????????? ??????
			 */

			// 2018-06-29 ??????
			List<Activity> divisionActivities = instance.getCurrentRunningActivities();
			Iterator<Activity> divisionActivitiesIterator = divisionActivities.iterator();
			while (divisionActivitiesIterator.hasNext()) {
				Activity divisionActivity = divisionActivitiesIterator.next();
				if (divisionActivity instanceof SystemActivity) {
					SystemActivity tempSystemActivity = (SystemActivity) divisionActivity;
					if (tempSystemActivity.getStatusCode().equals(statusCode)) {
						systemActivity = tempSystemActivity;
						isSystemActivity = true;
					}
				}
			}
			if (!isSystemActivity) {
				if (iActionCode == Integer.parseInt(ACTION_CODE_COMELETE)) { // 01: ??????
					workHumanActivity = completeWorkItem(instance, isNew, userId, statusCode, loggedRm);
					changeWorkItem(instance, userId, processVariables);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_SAVEONLY)) { // 02: ??????
					workHumanActivity = saveWorkItem(instance, isNew, userId, statusCode, processVariables);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_COLLECTION)) { // 03: ??????
					workHumanActivity = collectWorkItem(instance, userId, statusCode);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_REJECT)) { // 04: ??????
					workHumanActivity = rejectWorkItem(instance, userId);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_DELEGATE)) { // 05: ??????
					workHumanActivity = delegateWorkItem(instance, userId, statusCode, delegateRoleCode, delegateIds,
							isSystemCall);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_DELETE)) { // 06: ???????????? ??????
					deleteInstance(instance);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_COMPLETE)) { // 07: ???????????? ??????
					completeInstance(instance);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_STOP)) { // 08: ???????????? ??????
					stopInstance(instance);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_CANCEL)) { // 09: ???????????? ??????
					cancelInstance(instance);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_RESTART)) { // 10: ???????????? ?????????
					resumeInstance(instance);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_PROCESS_CHANGE_DUEDATE)) { // 11: ???????????? ???????????? ?????? ??????
					changeDueDateInstance(instance, dueDate);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_ACTIVITY_COMPENSATE)) { // 12: ????????????
					compensateActivity(instance, userId, statusCode);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_ACTIVITY_SUSPEND)) { // 13: ????????????
					suspendActivity(instance, userId, statusCode);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_ACTIVITY_RESUME)) { // 14: ??????
					resumeActivity(instance, userId, statusCode);
				} else if (iActionCode == Integer.parseInt(ACTION_CODE_ACTIVITY_SKIP)) { // 15: ????????????
					skipActivity(instance, userId, statusCode);
				}
			} else {
				systemActivity.fireComplete(instance);
			}
			// 2018-06-29 ??????
			// 2018-06-29 saveComment ??????
			// saveComment(instance, actionCode, userId, comment, workHumanActivity); //????????????
			// ??????
			serviceVO.setCurrentActivities(getCurrentActivities(instance)); // ???????????????????????? ??????
			processManagerBean.applyChanges();
			serviceVO.setMessage(getErrorMessagefByErrorCode(UEngineException.SUCCESS_CODE, processManagerBean)); // ?????????????????????
																													// ??????

			return serviceVO;
		} catch (UEngineException e) {
			try {
				if (e.getErrorCode() == UEngineException.ERR_CODE_SYSTEM_ERROR) {
					e.printStackTrace();
				}
				processManagerBean.cancelChanges();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			throw e;
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR, processManagerBean));
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR, processManagerBean));
		} finally {
			try {
				processManagerBean.remove();
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (RemoveException e) {
				e.printStackTrace();
			}
		}
	}

	private void saveComment(ProcessInstance instance, String actionCode, String userId, String comment,
			HumanActivity workHumanActivity) throws Exception {
		workHumanActivity = (HumanActivity) ((workHumanActivity == null)
				? instance.getProcessDefinition().getInitiatorHumanActivityReference(instance).getActivity()
				: workHumanActivity);
		String commentType = null;
		if (actionCode.equals(ACTION_CODE_COMELETE)) {
			commentType = "approve";
		} else if (actionCode.equals(ACTION_CODE_REJECT)) {
			commentType = "reject";
		}

		if (commentType != null && UEngineUtil.isNotEmpty(comment)) {
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT                                 \n");
			sql.append("INTO DOC_COMMENTS                      \n");
			sql.append("  (                                    \n");
			sql.append("    ID,                                \n");
			sql.append("    INSTANCE_ID,                       \n");
			sql.append("    CONTENTS,                          \n");
			sql.append("    OPT_TYPE,                          \n");
			sql.append("    EMPNO,                             \n");
			sql.append("    EMPNAME,                           \n");
			sql.append("    EMPTITLE,                          \n");
			sql.append("    TRACINGTAG,                        \n");
			sql.append("    CREATED_DATE,                      \n");
			sql.append("    CREATED_BY,                        \n");
			sql.append("    UPDATED_DATE,                      \n");
			sql.append("    UPDATED_BY,                      \n");
			sql.append("    APPRTITLE                        \n");
			sql.append("  )                                    \n");
			sql.append("  VALUES                               \n");
			sql.append("  (                                    \n");

			// sql.append(" seq_doc_comments.nextval, \n");
			if (DAOFactory.getInstance(null).getDBMSProductName().equals("Cubrid")) {
				sql.append("    seq_doc_comments.NEXT_VALUE,          \n"); // freshka 2016-10-20
			} else {
				sql.append("    seq_doc_comments.nextval,          \n");
			}

			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");
			sql.append("    ?,                               \n");

			// sql.append(" SYSDATE, \n");
			if (DAOFactory.getInstance(null).getDBMSProductName().equals("Cubrid")) {
				sql.append("    CURRENT_TIMESTAMP,                           \n"); // freshka 2016-10-20
			} else {
				sql.append("    SYSDATE,                           \n");
			}

			sql.append("    ?,                           \n");

			// sql.append(" SYSDATE, \n");
			if (DAOFactory.getInstance(null).getDBMSProductName().equals("Cubrid")) {
				sql.append("    CURRENT_TIMESTAMP,                           \n"); // freshka 2016-10-20
			} else {
				sql.append("    SYSDATE,                           \n");
			}

			sql.append("    ?,                           \n");
			sql.append("    ?                           \n");
			sql.append("  )                                   \n");

			Connection conn = instance.getProcessTransactionContext().getConnection();
			PreparedStatement psmt = null;
			RoleMapping rm = workHumanActivity.getActualMapping(instance);
			try {
				psmt = conn.prepareStatement(sql.toString());
				psmt.setString(1, instance.getInstanceId());
				psmt.setString(2, comment);
				psmt.setString(3, commentType);
				psmt.setString(4, userId);
				psmt.setString(5, rm.getResourceName());
				psmt.setString(6, rm.getTitle());
				psmt.setString(7, workHumanActivity.getTracingTag());
				psmt.setString(8, userId);
				psmt.setString(9, userId);
				psmt.setString(10, workHumanActivity.getName().getText());
				psmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
				throw e;
			} finally {
				if (psmt != null)
					psmt.close();
			}

		}

	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? BPM ????????? ??????????????? ?????? ??? executeService() ????????? return ?????? ??? "serviceVO" ??? ??????(??????) Activity ????????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : serviceVO ??? Activity Code ??? Activity Status ????????? ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : getCurrentActivities
	 * @date : 2017. 1. 10.
	 * @author : chonk_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 1. 10. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	public List<CurrentActivityVO> getCurrentActivities(ProcessInstance instance) throws Exception {
		List<CurrentActivityVO> result = new ArrayList<CurrentActivityVO>();
		List<Activity> activities = instance.getCurrentRunningActivities();

		for (int i = 0; i < activities.size(); i++) {
			CurrentActivityVO currentActivity = new CurrentActivityVO();
			Activity currAct = activities.get(i);
			HumanActivity currHumanAct = null;
			SubProcessActivity currSubAct = null;

			if (currAct instanceof HumanActivity) {
				currHumanAct = (HumanActivity) currAct;
				currentActivity.setActivityCode(currHumanAct.getExtValue1()); // activity_id
				currentActivity.setActivityStatus(currHumanAct.getStatus(instance)); // status
				result.add(currentActivity);
			} else if (currAct instanceof SubProcessActivity) {
				currSubAct = (SubProcessActivity) currAct;
				currentActivity.setActivityCode(currSubAct.getStatusCode()); // activity_id
				currentActivity.setActivityStatus(currSubAct.getStatus(instance)); // status
				result.add(currentActivity);
			}
		}

		return result;
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : setProcessVariables
	 * @date : 2016. 8. 4.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 4. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private void setProcessVariables(ProcessInstance instance, List<ProcessVariable> processVariables)
			throws Exception {
		if (processVariables == null)
			return;
		Iterator<ProcessVariable> procVars = processVariables.iterator();
		while (procVars.hasNext()) {
			ProcessVariable procVar = procVars.next();
			String varName = procVar.getVarName();
			if (instance.getProcessDefinition().getProcessVariable(varName) != null) {
				Iterator<ProcessVariableValue> varValues = procVar.getVarValues().iterator();
				org.uengine.kernel.ProcessVariableValue pvv = new org.uengine.kernel.ProcessVariableValue();
				pvv.setName(varName);
				while (varValues.hasNext()) {
					if (pvv.getValue() != null) {
						pvv.moveToAdd();
					}
					Object value = varValues.next().getVarValue();
					if (value instanceof String && ((String) value).equalsIgnoreCase("[null]"))
						continue;
					pvv.setValue(value);
				}
				pvv.beforeFirst();
//				pvv.remove();
//				if ( pvv != null )
				instance.set("", pvv);
			}
		}
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : setProcessRoles
	 * @date : 2016. 8. 4.
	 * @author : mkbok_Enki
	 * @param instance
	 * @throws Exception
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 4. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 */
	private void setProcessRoles(ProcessInstance instance, List<ProcessRole> processRoles) throws Exception {
		if (processRoles == null)
			return;
		Iterator<ProcessRole> roles = processRoles.iterator();
		while (roles.hasNext()) {
			ProcessRole role = roles.next();
			String roleName = role.getRoleName();
			if (instance.getProcessDefinition().getRole(roleName) != null) {
				Iterator<ProcessRoleId> roleIds = role.getRoleIds().iterator();
				RoleMapping rm = RoleMapping.create();
				rm.setName(roleName);
				rm.beforeFirst();
				while (roleIds.hasNext()) {
					if (UEngineUtil.isNotEmpty(rm.getEndpoint())) {
						rm.moveToAdd();
					}
					rm.setEndpoint(roleIds.next().getRoleId());
					rm.fill(instance);
				}
				instance.putRoleMapping(roleName, rm);
			}
		}
	}

	private HumanActivity undoWorkItem(final ProcessInstance instance, final ServiceVO serviceVO,
			final String realUndoReqTaskId) throws Exception {
		// ???????????? undo???????????? task_history??? ???????????? ???. ??? backActivity??? ????????? undo ??? case??? ??????????????? ???.
		instance.getProcessTransactionContext().setSharedContext("_user_compensated", true);
		// 2017-05-24 mkbok
		// undo ?????? instid ??????
		TransactionContext tc = instance.getProcessTransactionContext();
		ProcessManagerBean bean = instance.getProcessTransactionContext().getProcessManager();
		String reqInstId = null;
		return null;
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : compensateActivity
	 * @date : 2016. 12. 26.
	 * @author : freshka
	 * @history :
	 *          -----------------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------------------------------
	 *          -----------------------------------------------------------------------
	 *          2016. 12. 26. freshka ?????? ??????
	 *          -----------------------------------------------------------------------
	 * 
	 * @param instance
	 * @throws UEngineException, Exception
	 */
	private void compensateActivity(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException, Exception {
		// getCompletedHumanActivity(instance).compensateToThis(instance);
		// //"Completed+Skipped+Running"
		// !(status.equals(Activity.STATUS_SKIPPED) ||
		// status.equals(Activity.STATUS_READY) ||
		// status.equals(Activity.STATUS_CANCELLED) ||
		// status.equals(Activity.STATUS_COMPLETED))
		// getHumanActivity(instance,
		// Activity.STATUS_SKIPPED+Activity.STATUS_READY+Activity.STATUS_CANCELLED+Activity.STATUS_COMPLETED,
		// false).compensateToThis(instance);
		getHumanActivity(instance,
				Activity.STATUS_RUNNING + Activity.STATUS_TIMEOUT + Activity.STATUS_SKIPPED + Activity.STATUS_COMPLETED,
				true, userId, statusCode).compensateToThis(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : suspendActivity
	 * @date : 2016. 12. 26.
	 * @author : freshka
	 * @history :
	 *          -----------------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------------------------------
	 *          -----------------------------------------------------------------------
	 *          2016. 12. 26. freshka ?????? ??????
	 *          -----------------------------------------------------------------------
	 * 
	 * @param instance
	 * @throws UEngineException, Exception
	 */
	private void suspendActivity(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException, Exception {
		// getWorkHumanActivity(instance).suspend(instance); //??????,???????????? ???????????? ??????
		// (status.equals(Activity.STATUS_RUNNING) ||
		// status.equals(Activity.STATUS_TIMEOUT))
		getHumanActivity(instance, Activity.STATUS_RUNNING + Activity.STATUS_TIMEOUT, true, userId, statusCode)
				.suspend(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : resumeActivity
	 * @date : 2016. 12. 26.
	 * @author : freshka
	 * @history :
	 *          -----------------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------------------------------
	 *          -----------------------------------------------------------------------
	 *          2016. 12. 26. freshka ?????? ??????
	 *          -----------------------------------------------------------------------
	 * 
	 * @param instance
	 * @throws UEngineException, Exception
	 */
	private void resumeActivity(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException, Exception {
		// getSuspendedHumanActivity(instance).resume(instance); //????????????+?????? ???????????? ??????
		// (status.equals(Activity.STATUS_SUSPENDED) ||
		// status.equals(Activity.STATUS_FAULT))
		getHumanActivity(instance, Activity.STATUS_SUSPENDED + Activity.STATUS_FAULT, true, userId, statusCode)
				.resume(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : skipActivity
	 * @date : 2016. 12. 26.
	 * @author : freshka
	 * @history :
	 *          -----------------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------------------------------
	 *          -----------------------------------------------------------------------
	 *          2016. 12. 26. freshka ?????? ??????
	 *          -----------------------------------------------------------------------
	 * 
	 * @param instance
	 * @throws UEngineException, Exception
	 */
	private void skipActivity(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException, Exception {
		// getWorkHumanActivity(instance).skip(instance); //??????,???????????? ???????????? ??????
		// getHumanActivity(instance,
		// Activity.STATUS_RUNNING+Activity.STATUS_TIMEOUT).skip(instance);
		// !(status.equals(Activity.STATUS_SKIPPED) ||
		// status.equals(Activity.STATUS_READY) ||
		// status.equals(Activity.STATUS_CANCELLED) ||
		// status.equals(Activity.STATUS_COMPLETED))
		getHumanActivity(instance,
				Activity.STATUS_SKIPPED + Activity.STATUS_READY + Activity.STATUS_CANCELLED + Activity.STATUS_COMPLETED,
				false, userId, statusCode).skip(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : resumeInstance
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws RemoteException
	 */
	private void resumeInstance(ProcessInstance instance) throws RemoteException {
		instance.getProcessTransactionContext().getProcessManager().executeProcess(instance.getInstanceId());
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? ?????? ????????? "??????" ??? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : Process ????????? BPM_PROCINST ???????????? STATUS ?????? ?????? "Cancelled" ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : cancelInstance
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private void cancelInstance(ProcessInstance instance) throws Exception {
		EndActivity endActivity = new EndActivity();
		endActivity.setStatus(EndActivity.STATUS_CANCELLED);
		endActivity.executeActivity(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : stopInstance
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private void stopInstance(ProcessInstance instance) throws Exception {
		instance.stop();
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? Process ?????? ????????? "??????" ??? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : Process ????????? BPM_PROCINST ???????????? STATUS ?????? ?????? "Completed" ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : completeInstance
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private void completeInstance(ProcessInstance instance) throws Exception {
		EndActivity endActivity = new EndActivity();
		endActivity.setStatus(EndActivity.STATUS_COMPLETED);
		endActivity.executeActivity(instance);
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : deleteInstance
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private void deleteInstance(ProcessInstance instance) throws Exception {
		instance.remove();
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ?????? ?????? ??????????????? ????????? "??????" ?????? ??? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. ????????? ???????????? ??? ?????? ?????? ????????? ?????? ??????("delegateRoleCode")??? ?????? ?????? ??????
	 * 				 	1-1. ?????? ????????? ?????? getHumanActivitiesByRoleCode() ???????????? Activity ????????? ??????
	 * 				 	1-2. ????????? Activity ????????? delegate() ??????
	 * 				 	1-3. ????????? ???????????? ??? ?????? ?????? ????????? ?????? ??????("isSystemCall")??? ?????? "false" ??? ????????? sendStoryZoneMessageToNYJ() ??????
	 * 				 2. ????????? ???????????? ??? ?????? ?????? ????????? ?????? ??????("delegateRoleCode")??? ?????? ?????? ??????
	 * 				 	2-1. getWorkHumanActivity() ???????????? Activity ????????? ??????
	 *  			 	2-2. Activity Status ??? ??????("Completed") ??? ????????? ?????? ??? ??????
	 * 	 			 	2-3. ????????? ????????? Activity ??? delegate() ??????
	 * 				 	2-4. ????????? ???????????? ??? ?????? ?????? ????????? ?????? ??????("isSystemCall")??? ?????? "false" ??? ????????? sendStoryZoneMessageToNYJ() ??????
	 * </pre>
	 * 
	 * @Method Name : delegateWorkItem
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 24. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param userId
	 * @param statusCode
	 * @param delegateRoleCode
	 * @param delegateIds
	 * @param isSystemCall
	 * @return
	 * @throws UEngineException
	 */
	private HumanActivity delegateWorkItem(ProcessInstance instance, String userId, String statusCode,
			String delegateRoleCode, List<ProcessRoleId> delegateIds, boolean isSystemCall)
			throws UEngineException, Exception {
		HumanActivity workHumanActivity = null;
		Iterator<HumanActivity> workHumanActivities = null;

		if (UEngineUtil.isNotEmpty(delegateRoleCode)) {
			workHumanActivities = getHumanActivitiesByRoleCode(instance.getProcessDefinition(), userId,
					delegateRoleCode, instance).iterator();
		} else {
			workHumanActivity = getWorkHumanActivity(instance, userId, statusCode);
		}

		if (workHumanActivities != null && workHumanActivities.hasNext()) {
			while (workHumanActivities.hasNext()) {
				workHumanActivity = workHumanActivities.next();

				RoleMapping newRm = RoleMapping.create();
				newRm.setName(workHumanActivity.getRole().getName());
				newRm.beforeFirst();

				for (int i = 0; i < delegateIds.size(); i++) {
					if (i > 0) {
						newRm.moveToAdd();
					}
					ProcessRoleId roleId = delegateIds.get(i);
					newRm.setEndpoint(roleId.getRoleId());
					newRm.fill(instance);
				}
				workHumanActivity.delegate(instance, newRm);

				// ????????? ????????? ?????? ?????? ?????? ?????? ?????? ???????????? ???????????? ??????
				if (!isSystemCall) {
					CommonUtil.sendStoryZoneMessageToNYJ(workHumanActivity, instance);
				}
			}
		} else if (workHumanActivity != null) {
			if (!(workHumanActivity.getStatus(instance).equals(Activity.STATUS_COMPLETED))) {
				RoleMapping newRm = RoleMapping.create();
				newRm.setName(workHumanActivity.getRole().getName());
				newRm.beforeFirst();

				for (int i = 0; i < delegateIds.size(); i++) {
					if (i > 0) {
						newRm.moveToAdd();
					}
					ProcessRoleId roleId = delegateIds.get(i);
					newRm.setEndpoint(roleId.getRoleId());
					newRm.fill(instance);
				}
				workHumanActivity.delegate(instance, newRm);

				// ????????? ????????? ?????? ?????? ?????? ?????? ?????? ???????????? ???????????? ??????
				if (!isSystemCall) {
					CommonUtil.sendStoryZoneMessageToNYJ(workHumanActivity, instance);
				}
			} else {
				throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_ALREADY_COMPLETED,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_ALREADY_COMPLETED,
								instance.getProcessTransactionContext().getProcessManager()));
			}
		} else {
			throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
							instance.getProcessTransactionContext().getProcessManager()));
		}

		return workHumanActivity;
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : rejectWorkItem
	 * @date : 2016. 8. 3.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 8. 3. mkbok_Enki ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @throws Exception
	 */
	private HumanActivity rejectWorkItem(ProcessInstance instance, String userId) throws UEngineException, Exception {
		// ????????? ?????? ??????????????? ?????????.
		List<HumanActivity> runningOrCompletedHumanActivities = instance
				.getAllRunningOrCompletedHumanActivities(instance.getProcessDefinition());

		HumanActivity workAct = runningOrCompletedHumanActivities.get(0);

		if (workAct.getStatus(instance).equals(Activity.STATUS_COMPLETED)) {
			workAct.compensateToThis(instance, true);

			KeyedParameter parameter = new KeyedParameter("extValue10", "1");

			KeyedParameter[] parameters = { parameter };

			instance.getWorkList().updateWorkItem(workAct.getTaskIds(instance)[0], userId, parameters,
					instance.getProcessTransactionContext());

			/*
			 * 
			 * final String taskId = runningOrCompletedHumanActivities.get(0)
			 * .getTaskIds(instance)[0]; final TransactionContext tc = processManagerBean
			 * .getTransactionContext();
			 * 
			 * // ???????????? DB ????????? ?????? TransactionListener tl = new TransactionListener() {
			 * 
			 * @Override public void beforeRollback(TransactionContext tx) throws Exception
			 * {
			 * 
			 * }
			 * 
			 * @Override public void beforeCommit(TransactionContext tx) throws Exception {
			 * Connection conn = null; PreparedStatement psmt = null; try { conn =
			 * tc.getConnection(); String sql =
			 * "update bpm_worklist set ext10='1' where taskid=?"; psmt =
			 * conn.prepareStatement(sql); psmt.setString(1, taskId); psmt.executeUpdate();
			 * } catch (Exception e) { e.printStackTrace(); throw e; } finally { if (psmt !=
			 * null) psmt.close(); } }
			 * 
			 * @Override public void afterRollback(TransactionContext tx) throws Exception {
			 * 
			 * }
			 * 
			 * @Override public void afterCommit(TransactionContext tx) throws Exception {
			 * 
			 * } };
			 * 
			 * instance.getProcessTransactionContext().addTransactionListener(tl);
			 */

		} else {
			throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
							instance.getProcessTransactionContext().getProcessManager()));
		}

		return workAct;

	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : collectWorkItem
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 7. 22. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param serviceVO
	 * @param instance
	 */
	private HumanActivity collectWorkItem(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException {
		HumanActivity workAct = null;
		try {
			Iterator<HumanActivity> acts = getHumanActivitiesByStatusCode(instance.getProcessDefinition(), statusCode,
					instance).iterator();
			while (acts.hasNext()) {
				HumanActivity humanActivity = acts.next();
				if (!humanActivity.getStatus(instance).equals(Activity.STATUS_COMPLETED))
					continue;
				RoleMapping rm = humanActivity.getRole().getMapping(instance);
				rm.beforeFirst();
				do {
					if (rm.getEndpoint().equals(userId)) {
						workAct = humanActivity;
						break;
					}
				} while (rm.next());
				if (workAct != null) {
					break;
				}
			}
			if (workAct == null) {
				throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
								instance.getProcessTransactionContext().getProcessManager()));
			}
			// ????????????
			// ????????? ?????? ??????????????? ?????????.
			List<HumanActivity> allHumanActivities = instance
					.getAllRunningOrCompletedHumanActivities(instance.getProcessDefinition());
			HumanActivity nextHumAct = null;
			for (int i = 0; i < allHumanActivities.size(); i++) {
				if (allHumanActivities.get(i).getTracingTag().equals(workAct.getTracingTag())
						&& !(i == (allHumanActivities.size() - 1))) {
					nextHumAct = allHumanActivities.get(i + 1);
				}
			}

			if (nextHumAct == null) {
				throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
								instance.getProcessTransactionContext().getProcessManager()));
			} else {
				workAct.compensateToThis(instance, true);

				KeyedParameter parameter = new KeyedParameter("extValue9", "2");

				KeyedParameter[] parameters = { parameter };

				instance.getWorkList().updateWorkItem(workAct.getTaskIds(instance)[0], userId, parameters,
						instance.getProcessTransactionContext());
				/*
				 * 
				 * final String taskId = workAct.getTaskIds(instance)[0]; final
				 * TransactionContext tc = processManagerBean .getTransactionContext();
				 * 
				 * // ???????????? DB ????????? ?????? TransactionListener tl = new TransactionListener() {
				 * 
				 * @Override public void beforeRollback(TransactionContext tx) throws Exception
				 * {
				 * 
				 * }
				 * 
				 * @Override public void beforeCommit(TransactionContext tx) throws Exception {
				 * Connection conn = null; PreparedStatement psmt = null; try { conn =
				 * tc.getConnection(); String sql =
				 * "update bpm_worklist set ext9='1' where taskid=?"; psmt =
				 * conn.prepareStatement(sql); psmt.setString(1, taskId); psmt.executeUpdate();
				 * } catch (Exception e) { e.printStackTrace(); throw e; } finally { if (psmt !=
				 * null) psmt.close(); } }
				 * 
				 * @Override public void afterRollback(TransactionContext tx) throws Exception {
				 * 
				 * }
				 * 
				 * @Override public void afterCommit(TransactionContext tx) throws Exception {
				 * 
				 * } };
				 * 
				 * instance.getProcessTransactionContext().addTransactionListener( tl);
				 */
			}

		} catch (UEngineException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR,
							instance.getProcessTransactionContext().getProcessManager()));
		}
		return workAct;
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????("statusCode") ?????? ???????????? Activity ????????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. getAllHumanActivities() ???????????? ?????? Activity ????????? ??????
	 * 				 2. ????????? ???????????? ??? ?????? ?????? ?????? ??????("statusCode") ?????? Activity ?????? ??????1("ExtValue1") ?????? ???????????? Activity ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : getHumanActivitiesByStatusCode
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 7. 22. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param parent
	 * @param statusCode
	 * @return
	 * @throws Exception
	 */
	private List<HumanActivity> getHumanActivitiesByStatusCode(ComplexActivity parent, String statusCode,
			ProcessInstance instance) throws Exception {
		List<HumanActivity> returnActs = new ArrayList<HumanActivity>();
		Iterator<ActivityReference> allHumanActivities = parent
				.getAllHumanActivities(instance, instance.getProcessTransactionContext()).iterator();
		while (allHumanActivities.hasNext()) {
			HumanActivity humAct = (HumanActivity) allHumanActivities.next().getActivity();
			if (statusCode.equals(humAct.getExtValue1())) {
				returnActs.add(humAct);
			}
		}
		return returnActs;
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : saveWorkItem
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 7. 22. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param serviceVO
	 * @param instance
	 * @throws Exception
	 */
	private HumanActivity saveWorkItem(ProcessInstance instance, boolean isNew, String userId, String statusCode,
			List<ProcessVariable> processVariables) throws Exception {
		HumanActivity workHumanActivity = null;
		try {
			ProcessManagerBean processManagerBean = instance.getProcessTransactionContext().getProcessManager();
			if (isNew) {
				processManagerBean.executeProcess(instance.getInstanceId());
			}
			// ????????? ????????? 'CONFIRMED' ??? ??????
			workHumanActivity = getWorkHumanActivity(instance, userId, statusCode);
			workHumanActivity.saveActivity(instance);

			if (workHumanActivity.getStatus(instance).equals(Activity.STATUS_RUNNING)
					|| workHumanActivity.getStatus(instance).equals(Activity.STATUS_TIMEOUT)) {
				KeyedParameter parameter = new KeyedParameter();
				parameter.setKey(KeyedParameter.DEFAULT_STATUS);
				parameter.setValue(Activity.STATUS_CONFIRMED.toUpperCase());
				KeyedParameter[] params = { parameter };

				instance.getWorkList().updateWorkItem(workHumanActivity.getTaskIds(instance)[0], null, params,
						instance.getProcessTransactionContext());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR,
							instance.getProcessTransactionContext().getProcessManager()));
		}
		return workHumanActivity;
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? Activity ?????? ????????? "??????" ??? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. ????????? ???????????? ??? ?????? ?????? ?????? ???????????? ??????("isNew")??? ?????? "true" ??? ??????
	 * 				 	1-1. ??? ?????? ?????? ??????("HumanActivity") ????????? ??????
	 * 				 	1-2. ????????? ???????????? ??? ?????? ?????? ?????? ??????("statusCode") ?????? Activity ?????? ??????1("ExtValue1") ?????? ????????? ??????
	 * 				 		1-2-1. executeProcessByWorkitem() ?????? ??????
	 * 				 	1-3. ?????? ??????("statusCode") ?????? ????????? ?????? ??????("statusCode") ?????? Activity ?????? ??????1("ExtValue1") ?????? ???????????? ??????
	 * 				 		1-3-1. ????????? ????????? ?????? ????????? ?????? ?????? ????????? ??????
	 * 				 2. ????????? ???????????? ??? ?????? ?????? ?????? ???????????? ??????("isNew")??? ?????? "false" ??? ??????
	 * 				 	2-1. getWorkHumanActivity() ???????????? Activity ????????? ??????
	 *				 	2-2. Activity ????????? ?????? ?????? 
	 *				 		2-2-1. Activity ??? EventHumanActivity ??????????????? Activity??? ????????? AllActivity ?????? ??? ??????
	 *							2-2-1-1. attatchSubProcess() ?????? ??????
	 *						2-2-2. ?????? ??????
	 *							2-2-2-1. Activity Status ??? ?????????("Running") ????????? ??????("TimeOut") ??? ??????
	 *								2-2-2-1-1. completeWorkitem() ?????? ??????
	 *							2-2-2-2. Activity Status ??? ??????("Completed") ??? ??????
	 *								2-2-2-2-1. ?????? ?????? ????????? ?????? ?????? ????????? ??????
	 * 		         	2-3. Activity ????????? ?????? ??????
	 * 				 		2-3-1. ????????? ????????? ?????? ????????? ?????? ?????? ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : completeWorkItem
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 24. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param isNew
	 * @param userId
	 * @param statusCode
	 * @param loggedRm
	 * @throws Exception
	 * 
	 */
	private HumanActivity completeWorkItem(ProcessInstance instance, boolean isNew, String userId, String statusCode,
			RoleMapping loggedRm) throws Exception {
		ProcessManagerBean processManagerBean = instance.getProcessTransactionContext().getProcessManager();
		HumanActivity firstWorkHumanActivity = null;
		HumanActivity workHumanActivity = null;

		if (isNew) {
			try {
				// ??? ?????? HumanActivity ????????? ?????????, 2017-03-20 chonk
				firstWorkHumanActivity = (HumanActivity) instance.getProcessDefinition()
						.getInitiatorHumanActivityReference(instance).getActivity();
			} catch (Exception e) {
				e.printStackTrace();
				throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR, processManagerBean));
			}

			// FirstWorkHumanActivity??? StatusCode(ActivityCode)??? ????????? ??????, 2017-03-20 chonk
			if (UEngineUtil.isNotEmpty(statusCode) && statusCode.equals(firstWorkHumanActivity.getExtValue1())) {
				try {
					processManagerBean.executeProcessByWorkitem(instance.getInstanceId(), new ResultPayload());
				} catch (RemoteException e) {
					e.printStackTrace();
					throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
							getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR, processManagerBean));
				}
			} else {
				throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
								processManagerBean));
			}
		} else {
			workHumanActivity = getWorkHumanActivity(instance, userId, statusCode);

			if (workHumanActivity != null) {
				String taskId = workHumanActivity.getTaskIds(instance)[0]; // 2016-12-01 freshka

				if (!UEngineUtil.isNotEmpty(taskId)) {
					return workHumanActivity;
				}
				// workHumanActivity??? EventHumanActivity ??????????????? workHumanActivity??? ?????????
				// AllActivity ?????? ??? ?????? attatchSubProcess() ??????
				if (workHumanActivity instanceof EventHumanActivity
						&& workHumanActivity.getParentActivity() instanceof AllActivity) {
					try {
						((EventHumanActivity) workHumanActivity).attatchSubProcess(instance);
					} catch (Exception e) {
						e.printStackTrace();
						throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR, getErrorMessagefByErrorCode(
								UEngineException.ERR_CODE_SYSTEM_ERROR, processManagerBean));
					}
				} else {
					// humanActivity ????????? ????????? ?????? ??????
					if (!(workHumanActivity.getStatus(instance).equals(Activity.STATUS_COMPLETED))) {
						try {
							processManagerBean.completeWorkitem(instance.getInstanceId(),
									workHumanActivity.getTracingTag(), taskId, new ResultPayload());
						} catch (Exception e) {
							e.printStackTrace();
							throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
									getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR,
											processManagerBean));
						}
					} else {
						throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_ALREADY_COMPLETED,
								getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_ALREADY_COMPLETED,
										processManagerBean));
					}
				}
			} else {
				throw new UEngineException(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
						getErrorMessagefByErrorCode(UEngineException.ERR_CODE_WORKITEM_NO_AUTHORTY_OR_COMPLETED,
								processManagerBean));
			}
		}

		return workHumanActivity;
	}

	/**
	 * <pre>
	 * 1. ?????? : Activity ????????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. getHumanActivitiesByStatusCode() ???????????? Activity ????????? ??????
	 * 				 2. Activity ??? Status ??? ?????????("Running") ????????? ??????("TimeOut") ?????? ?????? ??? ??????
	 * 				 3. ????????? ???????????? ??? ?????? ?????? ????????? ID("userId") ?????? Activity ??? ????????? ID ?????? ???????????? Activity ????????? ?????? ?????? ??????
	 * 				 4. ?????? Activity ??? EventHumanActivity ????????? ???????????? Activity Status ??? ??????("Completed") ??? ?????? Activity ?????? ??????
	 * </pre>
	 * 
	 * @Method Name : getWorkHumanActivity
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 24. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param userId
	 * @param statusCode
	 * @return
	 */
	private HumanActivity getWorkHumanActivity(ProcessInstance instance, String userId, String statusCode)
			throws UEngineException {
		HumanActivity returnHumAct = null;
		Iterator<HumanActivity> workHumanActivities;

		try {
			workHumanActivities = getHumanActivitiesByStatusCode(instance.getProcessDefinition(), statusCode, instance)
					.iterator();
			while (workHumanActivities.hasNext()) {
				HumanActivity humanActivity = workHumanActivities.next();

				if (humanActivity.getStatus(instance).equals(Activity.STATUS_RUNNING)
						|| humanActivity.getStatus(instance).equals(Activity.STATUS_TIMEOUT)) {
					RoleMapping rm = humanActivity.getRole().getMapping(instance);
					rm.beforeFirst();

					do {
						if (rm.getEndpoint().equals(userId)) {
							returnHumAct = humanActivity;
							break;
						}
					} while (rm.next());
				}
				// humanActivity??? EventHumanActivity ????????? ???????????? humanActivity ????????? ?????? ??? ??????
				if (!(humanActivity instanceof EventHumanActivity)
						&& humanActivity.getStatus(instance).equals(Activity.STATUS_COMPLETED)) {
					returnHumAct = humanActivity;
				}

				if (returnHumAct != null) {
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR,
							instance.getProcessTransactionContext().getProcessManager()));
		}

		return returnHumAct;
	}

	/**
	 * <pre>
	 * 1. ?????? :
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : getHumanActivity
	 * @date : 2016. 12. 27.
	 * @author : freshka
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 12. 27. freshka ?????? ??????
	 *          ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param filters    (ex. ??????????????? ???????????? ?????? Completed+Running+Skipped)
	 * @param isIncluded ?????? ?????? ??????. true:?????? ?????? ??????. false:?????? ????????? ????????? ????????? ?????????.
	 * @param userId
	 * @param statusCode
	 * @return
	 */
	private HumanActivity getHumanActivity(ProcessInstance instance, String filters, boolean isIncluded, String userId,
			String statusCode) throws UEngineException {
		HumanActivity returnHumAct = null;
		// statusCode ??? ???????????? filters ????????? ??????????????? ?????? (ex. Completed+Running+Skipped)
		Iterator<HumanActivity> workHumanActivities;

		try {
			workHumanActivities = getHumanActivitiesByStatusCode(instance.getProcessDefinition(), statusCode, instance)
					.iterator();
			while (workHumanActivities.hasNext()) {
				HumanActivity humanActivity = workHumanActivities.next();
				if (!isIncluded) {
					if (!(filters == null || filters.indexOf(humanActivity.getStatus(instance)) > -1)) {
						RoleMapping rm = humanActivity.getRole().getMapping(instance);
						rm.beforeFirst();
						do {
							if (rm.getEndpoint().equals(userId)) {
								returnHumAct = humanActivity;
								break;
							}
						} while (rm.next());
					}
				} else {
					if (filters == null || filters.indexOf(humanActivity.getStatus(instance)) > -1) {
						RoleMapping rm = humanActivity.getRole().getMapping(instance);
						rm.beforeFirst();
						do {
							if (rm.getEndpoint().equals(userId)) {
								returnHumAct = humanActivity;
								break;
							}
						} while (rm.next());
					}

				}
				if (returnHumAct != null)
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException(UEngineException.ERR_CODE_SYSTEM_ERROR,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_SYSTEM_ERROR,
							instance.getProcessTransactionContext().getProcessManager()));
		}

		return returnHumAct;
	}

	/**
	 * <pre>
	 * 1. ?????? : 
	 * 2. ???????????? :
	 * </pre>
	 * 
	 * @Method Name : getInstanceIdByBizKey
	 * @date : 2016. 7. 22.
	 * @author : mkbok_Enki
	 * @param processCode
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2016. 7. 22. mkbok_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instanceId
	 * @return
	 */
	private String getInstanceId(ProcessManagerBean processManagerBean, String bizKey, String actionCode,
			String processCode, String userId, boolean isSubProcess) throws UEngineException {
		String instanceId = null;
		ProcessTransactionContext tc = processManagerBean.getTransactionContext();
		StringBuffer sql = new StringBuffer();

		if (isSubProcess) {
			// ?????????????????????(??????????????????)??? ?????? ????????? ?????? ?????? //2016-11-28 freshka
			sql.append("SELECT INST.INSTID											\n");
			sql.append("	FROM BPM_PROCINST INST,								\n");
			sql.append("		 BPM_PROCDEF DEF,								\n");
			sql.append("		 BPM_ROLEMAPPING RM								\n");
			sql.append("WHERE INST.NAME=?										\n");
			// sql.append("WHERE INST.NAME='").append(bizKey).append("' \n");
			sql.append("  AND INST.DEFID=DEF.DEFID								\n");
			sql.append("  AND INST.ISDELETED=0								\n");
			sql.append("  AND DEF.ALIAS=?										\n");
			// sql.append(" AND DEF.ALIAS='").append(processCode).append("' \n");
			sql.append("  AND INST.INSTID=RM.INSTID										\n");
			sql.append("  AND RM.ENDPOINT=?										\n");
			// sql.append(" AND RM.ENDPOINT='").append(userId).append("' \n");
		} else {
			sql.append("SELECT INST.INSTID											\n");
			sql.append("	FROM BPM_PROCINST INST,								\n");
			sql.append("		 BPM_PROCDEF DEF								\n");
			sql.append("WHERE INST.NAME=?										\n");
			// sql.append("WHERE INST.NAME='").append(bizKey).append("' \n");
			sql.append("  AND INST.DEFID=DEF.DEFID								\n");
			sql.append("  AND INST.ISDELETED=0								\n");
			sql.append("  AND DEF.ALIAS=?										\n");
			// sql.append(" AND DEF.ALIAS='").append(processCode).append("' \n");
		}

		if (!UEngineUtil.isNotEmpty(actionCode)) {
			// sql.append(" AND INST.STATUS IN ('Running','Completed') \n");
			sql.append("  AND UPPER(INST.STATUS) IN (");
			sql.append("'").append(STATUS_RUNNING_UC).append("'").append(",");
			sql.append("'").append(STATUS_COMPLETED_UC).append("'").append(",");
			sql.append("'").append(STATUS_STOPPED_UC).append("'").append(",");
			sql.append("'").append(STATUS_CANCELLED_UC).append("'").append(",");
			sql.append("'").append(STATUS_FAULT_UC).append("'").append(",");
			sql.append("'").append(STATUS_SKIPPED_UC).append("'");
			sql.append(")	\n");
		} else if (actionCode.equals(ACTION_CODE_COMELETE) || actionCode.equals(ACTION_CODE_SAVEONLY)
				|| actionCode.equals(ACTION_CODE_COLLECTION) || actionCode.equals(ACTION_CODE_REJECT)
				|| actionCode.equals(ACTION_CODE_DELEGATE) || actionCode.equals(ACTION_CODE_PROCESS_DELETE)
				|| actionCode.equals(ACTION_CODE_PROCESS_COMPLETE) || actionCode.equals(ACTION_CODE_PROCESS_STOP)
				|| actionCode.equals(ACTION_CODE_PROCESS_CANCEL)
				|| actionCode.equals(ACTION_CODE_PROCESS_CHANGE_DUEDATE)
				|| actionCode.equals(ACTION_CODE_ACTIVITY_SUSPEND)) {
			// sql.append(" AND INST.STATUS IN ('Running') \n");
			sql.append("  AND UPPER(INST.STATUS) IN (");
			sql.append("'").append(STATUS_RUNNING_UC).append("'");
			sql.append(")	\n");
		} else if (actionCode.equals(ACTION_CODE_PROCESS_RESTART)) {
			// sql.append(" AND INST.STATUS IN ('Stopped') \n");
			sql.append("  AND UPPER(INST.STATUS) IN (");
			sql.append("'").append(STATUS_RUNNING_UC).append("'").append(",");
			sql.append("'").append(STATUS_COMPLETED_UC).append("'").append(",");
			sql.append("'").append(STATUS_STOPPED_UC).append("'").append(",");
			sql.append("'").append(STATUS_CANCELLED_UC).append("'").append(",");
			sql.append("'").append(STATUS_FAULT_UC).append("'");
			sql.append(")	\n");
		} else if (actionCode.equals(ACTION_CODE_ACTIVITY_COMPENSATE)) {
			// !(status.equals(Activity.STATUS_SKIPPED) ||
			// status.equals(Activity.STATUS_READY) ||
			// status.equals(Activity.STATUS_CANCELLED) ||
			// status.equals(Activity.STATUS_COMPLETED));
			// sql.append(" AND UPPER(INST.STATUS) IN
			// ('"+STATUS_RUNNING_UC+"','"+STATUS_COMPLETED_UC+"','"+STATUS_SKIPPED_UC+"')
			// \n");
			sql.append("  AND UPPER(INST.STATUS) IN (");
			sql.append("'").append(STATUS_RUNNING_UC).append("'").append(",");
			sql.append("'").append(STATUS_SKIPPED_UC).append("'").append(",");
			sql.append("'").append(STATUS_COMPLETED_UC).append("'");
			sql.append("'").append(STATUS_STOPPED_UC).append("'").append(",");
			sql.append("'").append(STATUS_CANCELLED_UC).append("'").append(",");
			sql.append("'").append(STATUS_FAULT_UC).append("'");
			sql.append(")	\n");
		} else if (actionCode.equals(ACTION_CODE_ACTIVITY_RESUME)) {
			// !(status.equals(Activity.STATUS_SKIPPED) ||
			// status.equals(Activity.STATUS_READY) ||
			// status.equals(Activity.STATUS_CANCELLED) ||
			// status.equals(Activity.STATUS_COMPLETED));
			// sql.append(" AND UPPER(INST.STATUS) IN
			// ('"+STATUS_RUNNING_UC+"','"+STATUS_COMPLETED_UC+"','"+STATUS_SKIPPED_UC+"')
			// \n");
			sql.append("  AND UPPER(INST.STATUS) IN (");
			sql.append("'").append(STATUS_RUNNING_UC).append("'").append(",");
			sql.append("'").append(STATUS_SUSPENDED_UC).append("'").append(",");
			sql.append("'").append(STATUS_FAULT_UC).append("'");
			sql.append(")	\n");
		} else if (actionCode.equals(ACTION_CODE_ACTIVITY_SKIP)) {
			// !(status.equals(Activity.STATUS_SKIPPED) ||
			// status.equals(Activity.STATUS_READY) ||
			// status.equals(Activity.STATUS_CANCELLED) ||
			// status.equals(Activity.STATUS_COMPLETED));
			// sql.append(" AND UPPER(INST.STATUS) IN
			// ('"+STATUS_RUNNING_UC+"','"+STATUS_COMPLETED_UC+"','"+STATUS_SKIPPED_UC+"')
			// \n");
			sql.append("  AND UPPER(INST.STATUS) NOT IN ("); // ****** NOT IN ******
			sql.append("'").append(STATUS_SKIPPED_UC).append("'").append(",");
			sql.append("'").append(STATUS_READY_UC).append("'").append(",");
			sql.append("'").append(STATUS_CANCELLED_UC).append("'").append(",");
			sql.append("'").append(STATUS_COMPLETED_UC).append("'");
			sql.append(")	\n");
		}

		PreparedStatement psmt = null;
		ResultSet rs = null;
		try {
			psmt = tc.getConnection().prepareStatement(sql.toString());
			psmt.setString(1, bizKey);
			psmt.setString(2, processCode);
			if (isSubProcess) {
				psmt.setString(3, userId); // ?????????????????????(??????????????????)??? ?????? ????????? ?????? ?????? //2016-11-28 freshka
			}
			rs = psmt.executeQuery();
			if (rs.next()) {
				instanceId = rs.getString("INSTID");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new UEngineException();
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			if (psmt != null)
				try {
					psmt.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
		}

		return instanceId;
	}

	public String getErrorMessagefByErrorCode(int errCode, ProcessManagerBean processManagerBean) {
		String message = null;
		ProcessTransactionContext tc = processManagerBean.getTransactionContext();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MESSAGE_KO				\n");
		sql.append("	FROM BPM_ERR_CODE			\n");
		sql.append("WHERE ERRORCODE=?ERRORCODE		\n");

		try {
			IDAO idao = ConnectiveDAO.createDAOImpl(tc, sql.toString(), IDAO.class);
			idao.set("ERRORCODE", errCode);
			idao.select();
			if (idao.next()) {
				message = idao.getString("MESSAGE_KO");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return message;

	}

	private void checkMandatory(String actionCode, ProcessManagerBean processManagerBean, String bizKey,
			String processCode, String userId, String statusCode, List<ProcessRoleId> delegateIds, String dueDate)
			throws UEngineException {
		// mandatory validation
		if (!UEngineUtil.isNotEmpty(actionCode)) {
			throw new UEngineException(UEngineException.ERR_CODE_ACTION_CODE_IS_NULL,
					getErrorMessagefByErrorCode(104, processManagerBean));
		}

		if (actionCode.equals(ACTION_CODE_PROCESS_DELETE) || actionCode.equals(ACTION_CODE_PROCESS_COMPLETE)
				|| actionCode.equals(ACTION_CODE_PROCESS_STOP) || actionCode.equals(ACTION_CODE_PROCESS_CANCEL)
				|| actionCode.equals(ACTION_CODE_PROCESS_RESTART)) {
			// 2017-03-06 chonk
		} else if (actionCode.equals(ACTION_CODE_PROCESS_CHANGE_DUEDATE)) {
			if (!UEngineUtil.isNotEmpty(dueDate)) {
				throw new UEngineException(UEngineException.ERR_CODE_DUE_DATE_IS_NULL,
						getErrorMessagefByErrorCode(109, processManagerBean));
			}
		} else {
			if (!UEngineUtil.isNotEmpty(statusCode)) {
				throw new UEngineException(UEngineException.ERR_CODE_STATUS_CODE_IS_NULL,
						getErrorMessagefByErrorCode(103, processManagerBean));
			}
			if (actionCode.equals(ServiceVO.ACTION_CODE_DELEGATE) && (delegateIds == null || delegateIds.size() == 0)) {
				throw new UEngineException(UEngineException.ERR_CODE_DELEGATE_IDS_IS_NULL,
						getErrorMessagefByErrorCode(106, processManagerBean));
			}
		}

		if (!UEngineUtil.isNotEmpty(processCode)) {
			throw new UEngineException(UEngineException.ERR_CODE_PROCESS_CODE_IS_NULL,
					getErrorMessagefByErrorCode(101, processManagerBean));
		}
		if (!UEngineUtil.isNotEmpty(bizKey)) {
			throw new UEngineException(UEngineException.ERR_CODE_BIZ_KEY_IS_NULL,
					getErrorMessagefByErrorCode(102, processManagerBean));
		}
		if (!UEngineUtil.isNotEmpty(userId)) {
			throw new UEngineException(UEngineException.ERR_CODE_USER_ID_IS_NULL,
					getErrorMessagefByErrorCode(105, processManagerBean));
		}
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? ?????????????????? ?????? ?????? ?????? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. ?????? ???????????? ???????????? ??? getInstanceId() ??????????????? ???????????? ID("instanceId") ?????? null ??? ?????? 
	 * 				 	1-1. ????????? ???????????? ??? ?????? ?????? ??????????????????("bizKey")??? ????????? Process ????????? BPM_PROCINST ???????????? NAME ?????? ?????? ??????????????? ??????
	 * 				 	1-2. ?????????????????? ????????? ?????? ?????? ?????? ?????? ?????? ?????? ?????? ????????? ??????
	 * </pre>
	 *
	 * @Method Name : checkDuplicateBizKey
	 * @date : 2017. 3. 30.
	 * @author : chonk_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 30. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param processManagerBean
	 * @param bizKey
	 * @return
	 * @throws Exception
	 */
	private void checkDuplicateBizKey(ProcessManagerBean processManagerBean, String bizKey) throws UEngineException {
		boolean isDuplicateBizKey = false; // ??????????????????????????????
		ProcessTransactionContext tc = processManagerBean.getTransactionContext();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT name FROM bpm_procinst WHERE name = ?name AND isdeleted = 0");

		try {
			IDAO idao = ConnectiveDAO.createDAOImpl(tc, sql.toString(), IDAO.class);
			idao.set("name", bizKey);
			idao.select();
			if (idao.size() > 0) {
				isDuplicateBizKey = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (isDuplicateBizKey) {
			throw new UEngineException(UEngineException.ERR_CODE_BIZ_KEY_IS_DUPLICATED,
					getErrorMessagefByErrorCode(UEngineException.ERR_CODE_BIZ_KEY_IS_DUPLICATED, processManagerBean));
		}
	}

	private String getProcessVarValue(String processVarName, List<ProcessVariable> processVariables) throws Exception {
		// 2017-02-10 freshka
		StringBuffer varValueBuffer = new StringBuffer();
		if (processVariables != null && processVariables.size() > 0) {
			for (int i = 0; i < processVariables.size(); i++) {
				ProcessVariable processVariable = processVariables.get(i);
				if (processVarName.equals(processVariable.getVarName())) {
					List<ProcessVariableValue> varValues = processVariable.getVarValues();
					if (varValues != null && varValues.size() > 0) {
						for (int j = 0; j < varValues.size(); j++) {
							if (varValueBuffer.length() > 0)
								varValueBuffer.append(",");
							varValueBuffer.append((String) varValues.get(j).getVarValue());
						}
					}
					break;
				}
			}
		}
		return varValueBuffer.toString();
	}

	@Override
	public WorkListVO getWorkListByUserId(String userId, int pageSize, String minRegDate) throws Exception {
		ProcessManagerBean processManagerBean = null;

		WorkListVO workListVO = new WorkListVO();

		PreparedStatement psmt = null;
		ResultSet rs = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

			StringBuffer sql = new StringBuffer();

			sql.append("SELECT def.alias,                                               				  \n");
			sql.append("    inst.instid,                                                                  \n");
			sql.append("    defver.defname,                                                    			  \n");
			sql.append("    inst.name,                                                                    \n");
			sql.append("    inst.info,                                                                    \n");
			sql.append("    inst.initep,                                                                  \n");
			sql.append("    inst.initrsnm,                                                                \n");
			sql.append("    initwl.ENDDATE,                                                               \n");
			sql.append("    inst.CURREP,                                                                  \n");
			sql.append("    inst.CURRRSNM,                                                                \n");
			sql.append("    wl.STARTDATE,                                                                 \n");
			sql.append("    wl.ext1,                                                                      \n");
			sql.append("    wl.ext2,                                                                      \n");
			sql.append("    wl.title                                                                      \n");
			sql.append("  FROM bpm_worklist wl                                                            \n");
			sql.append("  INNER JOIN bpm_procinst inst                                                    \n");
			sql.append("  ON wl.instid      =inst.instid                                                  \n");
			sql.append("  AND inst.isdeleted='0'                                                          \n");
			sql.append("  AND inst.status   ='Running'                                                    \n");
			sql.append("  INNER JOIN bpm_worklist initwl                                                  \n");
			sql.append("  ON inst.inittaskid=initwl.taskid                                                \n");
			sql.append("  INNER JOIN bpm_procdefver defver                                                \n");
			sql.append("  ON wl.defid            =defver.defverid                                         \n");
			sql.append("  AND (defver.ISDELETED IS NULL                                                   \n");
			sql.append("  OR defver.isdeleted    ='0')                                                    \n");
			sql.append("  INNER JOIN bpm_procdef def                                                      \n");
			sql.append("  ON defver.defid     =def.defid                                                  \n");
			sql.append("  AND (def.ISDELETED IS NULL                                                      \n");
			sql.append("  OR def.isdeleted    ='0')                                                       \n");
			sql.append("  INNER JOIN bpm_rolemapping rm                                                   \n");
			sql.append("  ON inst.instid   =rm.instid                                                     \n");
			sql.append("  AND wl.rolename  =rm.rolename                                                   \n");
			sql.append("  WHERE wl.status IN ('NEW','CONFIRMED','DRAFT')                                  \n");
			sql.append(
					"  AND (wl.endpoint = ?  -- ????????? ??????                                                                                                	  	  \n");
			sql.append(
					"  OR rm.endpoint   = ?) -- ????????? ??????                                                                                    		  	  \n");
			if (UEngineUtil.isNotEmpty(minRegDate))
				sql.append("  AND TO_CHAR(wl.STARTDATE, 'YYYY/MM/DD HH:MI') >= ?                 		  \n");
			sql.append("  ORDER BY wl.STARTDATE DESC                                                      \n");

			String titleLink = null;
			String title = null;
			String regDate = null;
			String status = null;
			String link = null;
			String statusClz = null;

			processManagerBean = new ProcessManagerBean();
			Connection conn = processManagerBean.getTransactionContext().getConnection();

			psmt = conn.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			psmt.setString(1, userId);
			psmt.setString(2, userId);
			if (UEngineUtil.isNotEmpty(minRegDate))
				psmt.setString(3, minRegDate);
			rs = psmt.executeQuery();

			List<WorkVO> works = new ArrayList<WorkVO>();

			int i = 0;

			while (rs.next()) {
				i++;

				titleLink = "javascript:retrieveWorkItemForUengine('" + rs.getString("alias") + "', '"
						+ rs.getString("name") + "', '" + rs.getString("ext1") + "', '" + rs.getString("ext2") + "');";
				title = rs.getString("defname") + " - " + rs.getString("info");

				if (rs.getDate("STARTDATE") != null)
					regDate = sdf.format(new Date(rs.getTimestamp("STARTDATE").getTime()));

				status = "?????????";
				link = "goProcessInstanceDetailForUdngine('" + rs.getString("alias") + "', '" + rs.getString("name")
						+ "')";
				statusClz = "step_2";

				WorkVO work = new WorkVO();

				work.setTitleLink(titleLink);
				work.setTitle(title);
				work.setRegDate(regDate);
				work.setStatus(status);
				work.setLink(link);
				work.setStatusClz(statusClz);

				works.add(work);
				if (i == pageSize)
					break;
			}

			rs.last();
			int totalCount = rs.getRow();

			workListVO.setTotalCount(Integer.toString(totalCount));
			workListVO.setWorkList(works);

			// processManagerBean.applyChanges();
		} catch (Exception e) {
			e.printStackTrace();
			// processManagerBean.cancelChanges();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (psmt != null)
					psmt.close();
			} catch (Exception e) {
			}
			processManagerBean.remove();
		}

		return workListVO;
	}

	@Override
	public WorkListVO getInstanceListByUserId(String userId, int pageSize, String minRegDate) throws Exception {
		ProcessManagerBean processManagerBean = null;
		WorkListVO workListVO = new WorkListVO();

		PreparedStatement psmt = null;
		ResultSet rs = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

			StringBuffer sql = new StringBuffer();

			sql.append(
					"SELECT                                                				                 			  \n");
			sql.append("    distinct inst.instid,               \n");
			sql.append("    inst.CURRSTATUSNAMES,               \n");
			sql.append("    def.alias,   			  \n");
			sql.append(
					"    defver.defname,                                                    			                               \n");
			sql.append(
					"    defver.PROGRAMID,                                                    			                               \n");
			sql.append(
					"    inst.name,                                                                                                 \n");
			sql.append(
					"    inst.info,                                                                                                 \n");
			sql.append(
					"    inst.initep,                                                                                               \n");
			sql.append(
					"    inst.initrsnm,                                                                                             \n");
			sql.append(
					"    initwl.ENDDATE,                                                                                            \n");
			sql.append(
					"    inst.CURREP,                                                                                               \n");
			sql.append(
					"    inst.CURRRSNM,                                                                                             \n");
			sql.append(
					"    wl.STARTDATE,                                                                                              \n");
			sql.append(
					"    wl.ext1,                                                                                                   \n");
			sql.append(
					"    wl.ext2,                                                                                                   \n");
			sql.append(
					"    wl.title                                                                                                   \n");
			sql.append(
					"  FROM bpm_procinst inst                                                                                     \n");
			sql.append(
					"  INNER JOIN bpm_worklist wl                                                                                 \n");
			sql.append("  ON wl.instid      =inst.instid               \n");
			sql.append("  inner join bpm_worklist initwl              \n");
			sql.append(
					"  on initwl.taskid=inst.inittaskid                                                                           \n");
			sql.append(
					"  INNER JOIN bpm_procdefver defver                                                                             \n");
			sql.append(
					"  ON wl.defid            =defver.defverid                                                                      \n");
			sql.append(
					"  AND (defver.ISDELETED IS NULL                                                                                \n");
			sql.append(
					"  OR defver.isdeleted    ='0')                                                                                 \n");
			sql.append(
					"  INNER JOIN bpm_procdef def                                                                                   \n");
			sql.append(
					"  ON defver.defid     =def.defid                                                                               \n");
			sql.append(
					"  AND (def.ISDELETED IS NULL                                                                                   \n");
			sql.append(
					"  OR def.isdeleted    ='0')                                                                                   \n");
			sql.append("  WHERE 1=1              \n");
			sql.append(
					"  AND inst.isdeleted='0'                                                                                       \n");
			sql.append(
					"  AND inst.status   ='Running'                                                                                 \n");
			sql.append("  and wl.status ='COMPLETED'                                                               \n");
			sql.append("  AND wl.endpoint = ?  -- ????????? ??????               \n");
			if (UEngineUtil.isNotEmpty(minRegDate))
				sql.append("  AND TO_CHAR(initwl.ENDDATE, 'YYYY/MM/DD HH:MI') >= ?                 		  \n");
			sql.append("  ORDER BY initwl.ENDDATE DESC                                                      \n");

			String titleLink = "";
			String title = "";
			String regDate = "";
			String status = "";
			String link = "";
			String name = "";

			processManagerBean = new ProcessManagerBean();
			Connection conn = processManagerBean.getTransactionContext().getConnection();

			psmt = conn.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			psmt.setString(1, userId);
			if (UEngineUtil.isNotEmpty(minRegDate))
				psmt.setString(2, minRegDate);
			rs = psmt.executeQuery();

			List<WorkVO> works = new ArrayList<WorkVO>();

			int i = 0;

			while (rs.next()) {
				i++;

				titleLink = "javascript:retrieveInstanceItemForUengine('" + rs.getString("alias") + "', '"
						+ rs.getString("name") + "', '" + rs.getString("programid") + "');";
				title = rs.getString("defname") + " - " + rs.getString("info");

				if (rs.getDate("STARTDATE") != null)
					regDate = sdf.format(new Date(rs.getTimestamp("enddate").getTime()));

				String[] statuses;
				if (UEngineUtil.isNotEmpty(rs.getString("CURRSTATUSNAMES"))) {
					statuses = rs.getString("CURRSTATUSNAMES").split(";");
					if (statuses.length > 1) {
						status = statuses[0] + " ??? " + Integer.toString(statuses.length - 1);
					} else if (statuses.length == 1) {
						status = statuses[0];
					}
				} else {
					status = "";
				}

				String[] names;
				if (UEngineUtil.isNotEmpty(rs.getString("CURRRSNM"))) {
					names = rs.getString("CURRRSNM").split(";");
					if (names.length > 1) {
						name = names[0] + " ??? " + Integer.toString(names.length - 1);
					} else if (names.length == 1) {
						name = names[0];
					}
				} else {
					status = "";
				}

				link = "goProcessInstanceDetailForUdngine('" + rs.getString("alias") + "', '" + rs.getString("name")
						+ "')";

				WorkVO work = new WorkVO();

				work.setTitleLink(titleLink);
				work.setTitle(title);
				work.setRegDate(regDate);
				work.setStatus(status);
				work.setLink(link);
				work.setName(name);

				works.add(work);
				if (i == pageSize)
					break;
			}

			rs.last();
			int totalCount = rs.getRow();

			workListVO.setTotalCount(Integer.toString(totalCount));
			workListVO.setWorkList(works);

			// processManagerBean.applyChanges();
		} catch (Exception e) {
			e.printStackTrace();
			// processManagerBean.cancelChanges();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (psmt != null)
					psmt.close();
			} catch (Exception e) {
			}
			processManagerBean.remove();
		}

		return workListVO;
	}

	@Override
	public List<MyWorkVO> getMyWorkListByUserId(MyWorkVO vo) throws Exception {
		return bpmServiceDAO.selectMyWorkListByUserId(vo);
	}

	@Override
	public WorkListVO getMyInstanceListByUserId(String userId) throws Exception {
		ProcessManagerBean processManagerBean = null;
		WorkListVO workListVO = new WorkListVO();

		PreparedStatement psmt = null;
		ResultSet rs = null;

		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");

			StringBuffer sql = new StringBuffer();

			sql.append(
					"SELECT                                                				                 			  \n");
			sql.append("    distinct inst.instid,               \n");
			sql.append("    inst.CURRSTATUSNAMES,               \n");
			sql.append("    def.alias,   			  \n");
			sql.append(
					"    defver.defname,                                                    			                               \n");
			sql.append(
					"    defver.PROGRAMID,                                                    			                               \n");
			sql.append(
					"    inst.name,                                                                                                 \n");
			sql.append(
					"    inst.info,                                                                                                 \n");
			sql.append(
					"    inst.initep,                                                                                               \n");
			sql.append(
					"    inst.initrsnm,                                                                                             \n");
			sql.append(
					"    initwl.ENDDATE,                                                                                            \n");
			sql.append(
					"    inst.CURREP,                                                                                               \n");
			sql.append(
					"    inst.CURRRSNM,                                                                                             \n");
			sql.append(
					"    wl.STARTDATE,                                                                                              \n");
			sql.append(
					"    wl.ext1,                                                                                                   \n");
			sql.append(
					"    wl.ext2,                                                                                                   \n");
			sql.append(
					"    wl.title                                                                                                   \n");
			sql.append(
					"  FROM bpm_procinst inst                                                                                     \n");
			sql.append(
					"  INNER JOIN bpm_worklist wl                                                                                 \n");
			sql.append("  ON wl.instid      =inst.instid               \n");
			sql.append("  inner join bpm_worklist initwl              \n");
			sql.append(
					"  on initwl.taskid=inst.inittaskid                                                                           \n");
			sql.append(
					"  INNER JOIN bpm_procdefver defver                                                                             \n");
			sql.append(
					"  ON wl.defid            =defver.defverid                                                                      \n");
			sql.append(
					"  AND (defver.ISDELETED IS NULL                                                                                \n");
			sql.append(
					"  OR defver.isdeleted    ='0')                                                                                 \n");
			sql.append(
					"  INNER JOIN bpm_procdef def                                                                                   \n");
			sql.append(
					"  ON defver.defid     =def.defid                                                                               \n");
			sql.append(
					"  AND (def.ISDELETED IS NULL                                                                                   \n");
			sql.append(
					"  OR def.isdeleted    ='0')                                                                                   \n");
			sql.append("  WHERE 1=1              \n");
			sql.append(
					"  AND inst.isdeleted='0'                                                                                       \n");
			sql.append(
					"  AND inst.status   ='Running'                                                                                 \n");
			sql.append("  and wl.status ='COMPLETED'                                                               \n");
			sql.append("  AND wl.endpoint = ?  -- ????????? ??????               \n");
			sql.append("  ORDER BY initwl.ENDDATE DESC                                                      \n");

			String title = "";
			String regDate = "";
			String status = "";
			String name = "";

			processManagerBean = new ProcessManagerBean();
			Connection conn = processManagerBean.getTransactionContext().getConnection();

			psmt = conn.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			psmt.setString(1, userId);
			rs = psmt.executeQuery();

			List<WorkVO> works = new ArrayList<WorkVO>();

			while (rs.next()) {
				title = rs.getString("defname") + " - " + rs.getString("info");

				if (rs.getDate("STARTDATE") != null)
					regDate = sdf.format(new Date(rs.getTimestamp("enddate").getTime()));

				String[] statuses;
				if (UEngineUtil.isNotEmpty(rs.getString("CURRSTATUSNAMES"))) {
					statuses = rs.getString("CURRSTATUSNAMES").split(";");
					if (statuses.length > 1) {
						status = statuses[0] + " ??? " + Integer.toString(statuses.length - 1);
					} else if (statuses.length == 1) {
						status = statuses[0];
					}
				} else {
					status = "";
				}

				String[] names;
				if (UEngineUtil.isNotEmpty(rs.getString("CURRRSNM"))) {
					names = rs.getString("CURRRSNM").split(";");
					if (names.length > 1) {
						name = names[0] + " ??? " + Integer.toString(names.length - 1);
					} else if (names.length == 1) {
						name = names[0];
					}
				} else {
					status = "";
				}

				WorkVO work = new WorkVO();

				work.setTitle(title);
				work.setRegDate(regDate);
				work.setStatus(status);
				work.setName(name);

				works.add(work);
			}

			rs.last();
			int totalCount = rs.getRow();

			workListVO.setTotalCount(Integer.toString(totalCount));
			workListVO.setWorkList(works);

			// processManagerBean.applyChanges();
		} catch (Exception e) {
			e.printStackTrace();
			// processManagerBean.cancelChanges();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (Exception e) {
			}
			try {
				if (psmt != null)
					psmt.close();
			} catch (Exception e) {
			}
			processManagerBean.remove();
		}

		return workListVO;
	}

	@Override
	public List<CommentVO> getCommentList(String processCode, String bizKey) throws Exception {

		StringBuffer sql = new StringBuffer();

		sql.append("SELECT DC.CONTENTS,                                            \n");
		sql.append("  DC.OPT_TYPE,                                                 \n");
		sql.append("  DC.EMPNO,                                                    \n");
		sql.append("  DC.EMPNAME,                                                  \n");
		sql.append("  DC.EMPTITLE,                                                 \n");
		sql.append("  TO_CHAR(DC.CREATED_DATE, 'yyyy-mm-dd hh24:mi') as CREATEDATE,              \n");
		sql.append("  (                                                            \n");
		sql.append("  CASE                                                         \n");
		sql.append("    WHEN OPT_TYPE='reject'                                     \n");
		sql.append("    THEN '??????'                                                \n");
		sql.append("    ELSE '??????'                                                \n");
		sql.append("  END) AS APPROVETYPE_KO,                                                        \n");
		sql.append("  DC.OPT_TYPE,                                                 \n");
		sql.append("  DC.APPRTITLE                                                 \n");
		sql.append("FROM BPM_PROCINST INST                                         \n");
		sql.append("INNER JOIN DOC_COMMENTS DC                                     \n");
		sql.append("ON DC.INSTANCE_ID = INST.INSTID                                \n");
		sql.append("INNER JOIN BPM_PROCDEF DEF                                     \n");
		sql.append("ON INST.DEFID     =DEF.DEFID                                   \n");
		sql.append("WHERE INST.NAME   =?                       \n");
		sql.append("AND INST.STATUS  IN ('Running', 'Completed')                   \n");
		sql.append("AND INST.ISDELETED=0                                           \n");
		sql.append("AND DEF.ALIAS     =?                                       \n");

		List<CommentVO> result = new ArrayList<CommentVO>();

		Connection conn = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;

		try {
			conn = DefaultConnectionFactory.create().getConnection();
			psmt = conn.prepareStatement(sql.toString());
			psmt.setString(1, bizKey);
			psmt.setString(2, processCode);

			rs = psmt.executeQuery();
			while (rs.next()) {
				CommentVO commentVO = new CommentVO();
				commentVO.setActivityName(rs.getString("APPRTITLE"));
				commentVO.setComment(rs.getString("CONTENTS"));
				commentVO.setCommentType(rs.getString("OPT_TYPE"));
				commentVO.setCommentType_en(rs.getString("OPT_TYPE"));
				commentVO.setCommentType_ko(rs.getString("APPROVETYPE_KO"));
				commentVO.setCreatedDate(rs.getString("CREATEDATE"));
				commentVO.setJikCode(rs.getString("EMPTITLE"));
				commentVO.setUserId(rs.getString("EMPNO"));
				commentVO.setUserName(rs.getString("EMPNAME"));

				result.add(commentVO);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (rs != null)
				try {
					rs.close();
				} catch (Exception e2) {
				}
			if (psmt != null)
				try {
					psmt.close();
				} catch (Exception e2) {
				}
			if (conn != null)
				try {
					conn.close();
				} catch (Exception e2) {
				}
		}

		return result;
	}

	@Override
	public AuthorityVO getAuthority(String processCode, String bizKey, String statusCode, String userId)
			throws Exception {

		AuthorityVO authority = new AuthorityVO();

		ProcessManagerBean processManagerBean = null;
		HumanActivity workHumanActivity = null;

		try {
			processManagerBean = new ProcessManagerBean();
			String instanceId = getInstanceId(processManagerBean, bizKey, null, processCode, userId, false);
			if (instanceId == null) {
				// ??????????????? ????????? ????????? ??????????????? ?????? ??????(????????? ?????? ??????)
				String defVerId = processManagerBean.getProcessDefinitionProductionVersionByAlias(processCode);
				ProcessDefinition pd = processManagerBean.getProcessDefinition(defVerId);
				HumanActivity humAct = (HumanActivity) pd.getInitiatorHumanActivityReference(null).getActivity();
				workHumanActivity = statusCode.equals(humAct.getExtValue1()) ? humAct : null;
			} else {
				ProcessInstance instance = processManagerBean.getProcessInstance(instanceId);
				for (Iterator<HumanActivity> i = instance
						.getAllRunningOrCompletedHumanActivities(instance.getProcessDefinition()).iterator(); i
								.hasNext();) {
					HumanActivity humAct = i.next();
					if (statusCode.equals(humAct.getExtValue1())
							&& (humAct.getStatus(instance).equals(Activity.STATUS_RUNNING)
									|| humAct.getStatus(instance).equals(Activity.STATUS_TIMEOUT))) {
						RoleMapping rm = humAct.getRole().getMapping(instance);
						rm.beforeFirst();
						do {
							workHumanActivity = rm.getEndpoint().equals(userId) ? humAct : null;
						} while (rm.next() && workHumanActivity == null);
						if (workHumanActivity != null)
							break;
					}
				}
			}

			if (workHumanActivity != null) {
				if (workHumanActivity instanceof KrissHumanActivity)
					authority.setApprove(((KrissHumanActivity) workHumanActivity).isApprove());
				authority.setSave(((KrissHumanActivity) workHumanActivity).isSave());
				authority.setDelegate(((KrissHumanActivity) workHumanActivity).isDelegate());
				authority.setCollect(((KrissHumanActivity) workHumanActivity).isCollect());
				authority.setReject(((KrissHumanActivity) workHumanActivity).isReject());
				authority.setComplete(((KrissHumanActivity) workHumanActivity).isComplete());
				authority.setDelete(((KrissHumanActivity) workHumanActivity).isDelete());
			}

		} catch (Exception e) {
			throw e;
		} finally {
			processManagerBean.remove();
		}

		return authority;
	}

	@Override
	public List<MyWorkVO> getMyInstanceListByUserIdAndStatus(MyWorkVO myWorkVO) throws Exception {
		return bpmServiceDAO.selectMyInstanceListByUserId(myWorkVO);
	}

	@Override
	public List<MyWorkVO> getMyWorkListCountByUserId(String userId) throws Exception {
		return bpmServiceDAO.selectMyWorkListCountByUserId(userId);
	}

	@Override
	public List<MyWorkVO> getMyInstanceListCountByUserId(String userId) throws Exception {
		return bpmServiceDAO.selectMyInstanceListCountByUserId(userId);
	}

	@Override
	public ProcessDefinition getProdVersionInfoByDefId(String defId) throws Exception {
		ProcessDefinition def = null;
		ProcessManagerBean processManagerBean = null;
		try {
			processManagerBean = new ProcessManagerBean();
			String prodVerId = processManagerBean.getProcessDefinitionProductionVersion(defId);
			def = processManagerBean.getProcessDefinition(prodVerId);

		} catch (Exception e) {
			throw e;
		} finally {
			processManagerBean.remove();
		}

		return def;
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? ?????? ?????? ???????????? ?????? ?????? ?????? ?????? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. ?????? ???????????? ???????????? ??? ?????? checkMandatory() ??????????????? ???????????? ??????("dueDate") ??? Validation ??????
	 * 				 2. ?????? ?????? ?????? ?????? ????????? ?????? ?????????
	 * 				 3. ????????? ???????????? ??? ?????? ?????? ???????????? ??????("dueDate")??? ????????? Process ????????? BPM_PROCINST ???????????? DUEDATE ?????? ??? ??????
	 * </pre>
	 * 
	 * @Method Name : changeDueDateInstance
	 * @date : 2017. 3. 06.
	 * @author : chonk_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 06. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param dueDate
	 * @throws Exception
	 */
	private void changeDueDateInstance(ProcessInstance instance, String dueDate) throws Exception {
		dueDate = dueDate + "235959";
		SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
		Date date = sdf.parse(dueDate);
		Calendar changeDueDate = Calendar.getInstance();
		changeDueDate.setTime(date);

		instance.getProcessDefinition().setDueDate(instance, changeDueDate);
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????????????? ?????? ?????? ?????? ????????? ???????????? ?????? ?????? ?????? ?????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. Activity ??? ?????? ?????? ???, ???????????? ??????("ChangeEndDate")??? ??????????????????("yyyyMMddHHmmss")??? ?????? ?????? ??????
	 *				 	1-1. Activity ????????? BPM_WORKLIST ???????????? ENDDATE ?????? ??? ??????
	 * 				 	1-2. FlowChart ????????? ?????? ????????? Activity ????????? ????????? ??? ??????
	 * 				 2. ???????????? ??????("ChangeEndDate")??? ??????????????????("yyyyMMddHHmmss")??? ?????? ?????? ??????
	 * 				 	2-1. ????????? ??????("SYSDATE") ????????? ?????? ??????
	 * </pre>
	 * 
	 * @Method Name : changeWorkItem
	 * @date : 2017. 3. 22.
	 * @author : chonk_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 22. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param instance
	 * @param userId
	 * @param processVariables
	 * @throws Exception
	 */
	private void changeWorkItem(ProcessInstance instance, String userId, List<ProcessVariable> processVariables)
			throws UEngineException, Exception {
		String changeEndDateStr = getProcessVarValue("ChangeEndDate", processVariables);

		if (UEngineUtil.isNotEmpty(changeEndDateStr)) {
			SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
			Date changeEndDate = sdf.parse(changeEndDateStr);

			KeyedParameter parameter = new KeyedParameter();
			parameter.setKey("endDate");
			parameter.setValue(changeEndDate);
			KeyedParameter[] parameters = { parameter };

			List<HumanActivity> runningOrCompletedHumanActivities = instance
					.getAllRunningOrCompletedHumanActivities(instance.getProcessDefinition());
			HumanActivity workAct = runningOrCompletedHumanActivities.get(0);

			Calendar changeEndDateCal = Calendar.getInstance();
			changeEndDateCal.setTime(changeEndDate);
			workAct.setEndTime(instance, changeEndDateCal);
			instance.getWorkList().updateWorkItem(workAct.getTaskIds(instance)[0], userId, parameters,
					instance.getProcessTransactionContext());
		}
	}

	/**
	 * <pre>
	 * 1. ?????? : ?????? ??????("roleCode") ?????? ???????????? Activity ????????? ?????? ??????????????? ?????? ??????
	 * 2. ???????????? : 1. getAllHumanActivities() ???????????? ?????? Activity ????????? ??????
	 * 				 2. ?????? Activity ??? Status ??? ?????????("Running") ????????? ??????("TimeOut") ?????? ?????? ??? ??????
	 * 				 3. ????????? ???????????? ??? ?????? ?????? ?????? ??????("roleCode") ?????? Activity ??? ????????? ?????? ?????? ?????? ??????????????? ??????
	 * 				 4. ????????? ???????????? ??? ?????? ?????? ????????? ID("userId") ?????? Activity ??? ????????? ID ?????? ???????????? Activity ????????? ??????
	 * </pre>
	 * 
	 * @Method Name : getHumanActivitiesByRoleCode
	 * @date : 2017. 3. 24.
	 * @author : chonk_Enki
	 * @history : ----------------------------------------------------------------
	 *          ------- ????????? ????????? ???????????? ----------- -------------------
	 *          --------------------------------------- 2017. 3. 24. chonk_Enki ??????
	 *          ?????? ------------------------------------------------------------
	 *          -----------
	 * 
	 * @param parent
	 * @param userId
	 * @param roleCode
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	private List<HumanActivity> getHumanActivitiesByRoleCode(ComplexActivity parent, String userId, String roleCode,
			ProcessInstance instance) throws Exception {
		List<HumanActivity> returnActs = new ArrayList<HumanActivity>();
		Iterator<ActivityReference> allHumanActivities = parent
				.getAllHumanActivities(instance, instance.getProcessTransactionContext()).iterator();

		while (allHumanActivities.hasNext()) {
			HumanActivity humAct = (HumanActivity) allHumanActivities.next().getActivity();

			if (humAct.getStatus(instance).equals(Activity.STATUS_RUNNING)
					|| humAct.getStatus(instance).equals(Activity.STATUS_TIMEOUT)) {
				if (roleCode.equals(humAct.getRole().getName())) {
					RoleMapping rm = humAct.getRole().getMapping(instance);
					rm.beforeFirst();

					do {
						if (rm.getEndpoint().equals(userId)) {
							returnActs.add(humAct);
							break;
						}
					} while (rm.next());
				}
			}
		}

		return returnActs;
	}

	@Override
	public void approve(String userId, String processCode, String bizKey, Map paramMap, HttpServletRequest request)
			throws Exception {

		ProcessManagerBean processManagerBean = new ProcessManagerBean();

		try {

			String defVerId = processManagerBean.getProcessDefinitionProductionVersionByAlias(processCode);

			String instanceId = processManagerBean.initializeProcess(defVerId); // ???????????? ID ??????
			ProcessInstance instance = processManagerBean.getProcessInstance(instanceId); // ???????????? ??????
			instance.setName(bizKey); // ???????????? ??? ??????

			Gson gson = new Gson();
			String data = gson.toJson(paramMap);

			// ?????? ????????? ??????
			instance.setProperty("", "DATA", data);

			// ??? ??????
			Map<String, Object> genericContext = new HashMap<String, Object>();
			genericContext.put("request", request);
			RoleMapping loggedRm = null;

			if (UEngineUtil.isNotEmpty(userId)) {
				loggedRm = RoleMapping.create();
				loggedRm.beforeFirst();
				loggedRm.setEndpoint(userId);
				genericContext.put(HumanActivity.GENERICCONTEXT_CURR_LOGGED_ROLEMAPPING, loggedRm);
			}
			processManagerBean.setGenericContext(genericContext);

			RoleMapping rm = RoleMapping.create();
			rm.setName("Initiator");
			rm.beforeFirst();
			rm.setEndpoint(userId);
			rm.fill(instance);
			instance.putRoleMapping("Initiator", rm);

			// ???????????? ?????? ??????
			Iterator<String> keys = paramMap.keySet().iterator();

			while (keys.hasNext()) {
				String varName = keys.next();

				if (instance.getProcessDefinition().getProcessVariable(varName) != null) {
					org.uengine.kernel.ProcessVariableValue pvv = new org.uengine.kernel.ProcessVariableValue();
					pvv.beforeFirst();
					pvv.setName(varName);

					Object value = paramMap.get(varName);
					pvv.setValue(value);
					pvv.beforeFirst();

					instance.set("", pvv);

				}
			}

			// ??? ?????? HumanActivity ????????? ?????????, 2017-03-20 chonk
			HumanActivity firstWorkHumanActivity = (HumanActivity) instance.getProcessDefinition()
					.getInitiatorHumanActivityReference(instance).getActivity();

			processManagerBean.executeProcessByWorkitem(instance.getInstanceId(), new ResultPayload());

			processManagerBean.applyChanges();

		} catch (Exception e) {
			e.printStackTrace();
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (RemoteException | RemoveException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public List<Map<String, Object>> selectWorklistByUserId(String userId) throws Exception {
		return bpmServiceDAO.selectWorklistByUserId(userId);
	}

	@Override
	public Map<String, Object> getWorkData(String instId) throws Exception {
		ProcessManagerBean processManagerBean = new ProcessManagerBean();

		Map<String, Object> resultMap = new HashMap<String,Object>();
		try {
			ProcessInstance instance = processManagerBean.getProcessInstance(instId);
			String data = (String) instance.getProperty("", "DATA");
			resultMap.put("DATA", data);
		} catch (Exception e) {
			e.printStackTrace();
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (RemoteException | RemoveException e) {
				e.printStackTrace();
			}
		}
		return resultMap;
	}

	@Override
	public void completeWork(String userId, String instId, final String taskId, HttpServletRequest request)
			throws Exception {
		ProcessManagerBean processManagerBean = new ProcessManagerBean();

		try {


			ProcessInstance instance = processManagerBean.getProcessInstance(instId); // ???????????? ??????
			final ProcessInstance thisInstance = instance;
			ActivityForLoop forLoop = new ActivityForLoop() {
				
				@Override
				public void logic(Activity activity) {
					if ( activity instanceof HumanActivity) {
						String[] taskIds;
						try {
							taskIds = ((HumanActivity)activity).getTaskIds(thisInstance);
							if ( taskIds != null && taskIds.length > 0 ) {
								String thisTaskId = taskIds[0];
								if ( taskId.equals(thisTaskId) ) {
									stop(activity);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						
					}
					
				}
			};
			
			forLoop.run(instance.getProcessDefinition());
			
			HumanActivity humanActivity = (HumanActivity) forLoop.getReturnValue();
			
			
			Map<String, Object> genericContext = new HashMap<String, Object>();
			genericContext.put("request", request);
			RoleMapping loggedRm = null;

			if (UEngineUtil.isNotEmpty(userId)) {
				loggedRm = RoleMapping.create();
				loggedRm.beforeFirst();
				loggedRm.setEndpoint(userId);
				genericContext.put(HumanActivity.GENERICCONTEXT_CURR_LOGGED_ROLEMAPPING, loggedRm);
			}
			processManagerBean.setGenericContext(genericContext);

			processManagerBean.completeWorkitem(instance.getInstanceId(),
					humanActivity.getTracingTag(), taskId, new ResultPayload());
			
			processManagerBean.applyChanges();

		} catch (Exception e) {
			e.printStackTrace();
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (RemoteException | RemoveException e) {
				e.printStackTrace();
			}
		}

		
	}

	@Override
	public void rejectWork(String userId, String instId, String taskId, HttpServletRequest request) throws Exception {
		
		ProcessManagerBean processManagerBean = new ProcessManagerBean();

		try {


			ProcessInstance instance = processManagerBean.getProcessInstance(instId); // ???????????? ??????
			
			HumanActivity initiateHumanActivity = (HumanActivity) instance.getProcessDefinition().getInitiatorHumanActivityReference(instance).getActivity();
			
			initiateHumanActivity.compensateToThis(instance);
			
			processManagerBean.applyChanges();

		} catch (Exception e) {
			e.printStackTrace();
			processManagerBean.cancelChanges();
			throw e;
		} finally {
			try {
				processManagerBean.remove();
			} catch (RemoteException | RemoveException e) {
				e.printStackTrace();
			}
		}
	}

}
