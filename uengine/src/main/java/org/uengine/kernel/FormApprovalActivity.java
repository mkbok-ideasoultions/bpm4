package org.uengine.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.uengine.contexts.TextContext;
import org.uengine.persistence.dao.DAOFactory;
import org.uengine.webservices.worklist.DefaultWorkList;

/**
 * 
 * @author <a href="mailto:bigmahler@users.sourceforge.net">Jong-Uk Jeong</a>
 * @version $Id: FormApprovalActivity.java,v 1.17 2011/07/22 07:33:14 curonide Exp $
 * @version $Revision: 1.17 $
 */
public class FormApprovalActivity extends FormActivity {
	
	private static final long serialVersionUID = GlobalContext.SERIALIZATION_UID;
	
	public final static String KEY_APPR_STATUS = "KEY_APPR_STATUS";
	public final static String SIGN_DRAFT = "SIGN_DRAFT";
	public final static String SIGN_APPROVED = "SIGN_APPROVED";
	public final static String SIGN_ARBITRARY_APPROVED = "SIGN_ARBITRARY_APPROVED";
	public final static String SIGN_REJECT = "SIGN_REJECT";
	
	public final static String APPROVALTYPE_POST_APPROVAL = "POST_APPROVAL";
	public final static String APPROVALTYPE_ARBITRARY_APPROVAL = "ARBITRARY_APPROVAL";
	public final static String APPROVALTYPE_APPROVAL = "APPROVAL";
	public final static String APPROVALTYPE_CONSENT = "CONSENT";
	
	public FormApprovalActivity(){
		setName("FormApproval");
	}
	
	public String getTool() {
		//return super.getTool();
		return "formApprovalHandler";
    }

	public Map createParameter(ProcessInstance instance) throws Exception {
		Map parameterMap = super.createParameter(instance);
		String trcTag="";
		Activity parent = getParentActivity();
		
		while(true){
			if(parent instanceof FormApprovalLineActivity ){
				trcTag = parent.getTracingTag();
				break;
			}else{
				parent = parent.getParentActivity();
				if(parent == null) break;
			}
		}

		
		parameterMap.put("approvalLineActivityTT",trcTag );
				
		return parameterMap;
	}

	public RoleMapping getActualMapping(ProcessInstance instance) throws Exception {
		if(getApprover(instance)==null) return super.getActualMapping(instance);
		
		return getApprover(instance);
	}
	
	protected String createApproverRoleName(){
		if(role!=null) return role.getName();
		return "approver_"+getApprovalLineActivity().getTracingTag()+"_"+getTracingTag();
	}
	
	RoleMapping approver;
		public RoleMapping getApprover(ProcessInstance instance) {
			RoleMapping rm;
			try {
				rm = instance.getRoleMapping(createApproverRoleName());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			
			if(rm != null)
				return rm;
			else
				return approver;
		}
		public void setApprover(ProcessInstance instance, RoleMapping approver) {
			this.approver = approver;
			try {
				instance.putRoleMapping(createApproverRoleName(), approver);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

	public Role getRole() {
		return approver==null ? super.getRole() : new Role(){
			
			public String getName() {
				return getDisplayName().getText();
			}

			public TextContext getDisplayName() {
				TextContext tc = TextContext.createInstance();
				
				tc.setText(createApproverRoleName());
				return tc;
			}

			public RoleMapping getMapping(ProcessInstance inst, String tracingTag) throws Exception {
				
				return getApprover(inst);
			}
			
		};
	}
	boolean isViewMode=false;
		public boolean isViewMode() {
			return isViewMode;
		}
	
		public void setViewMode(boolean isViewMode) {
			this.isViewMode = isViewMode;
		}

	public void arbitraryApprove(ProcessInstance instance) throws Exception{
		setApprovalStatus(instance, SIGN_ARBITRARY_APPROVED);
		
		String taskId = getTaskIds(instance)[0];		
		KeyedParameter[] parameters = new KeyedParameter[]{};		
		instance.getWorkList().completeWorkItem(taskId, parameters, instance.getProcessTransactionContext());
		setEndTime(instance, GlobalContext.getNow(instance.getProcessTransactionContext()));
		
		getApprovalLineActivity().fireComplete(instance);
		
		EventMessagePayload emp = new EventMessagePayload();
		emp.setTriggerTracingTag(getTracingTag());
		getApprovalLineActivity().fireEventHandlers(instance, EventHandler.TRIGGERING_BY_ARBITRARYFINISHED, emp);
	}
	
	public void rejectApprove(ProcessInstance instance) throws Exception{
		setApprovalStatus(instance, SIGN_REJECT);
		
		int loopingOption = getApprovalLineActivity().getLoopingOption();
		
		String taskId = getTaskIds(instance)[0];		
		KeyedParameter[] parameters = new KeyedParameter[]{};		
		instance.getWorkList().completeWorkItem(taskId, parameters, instance.getProcessTransactionContext());

		if(loopingOption == FormApprovalLineActivity.LOOPINGOPTION_AUTO || loopingOption == FormApprovalLineActivity.LOOPINGOPTION_REPEATONREJECT){
			getApprovalLineActivity().getDraftActivity().compensateToThis(instance);
		}else {
			setEndTime(instance, GlobalContext.getNow(instance.getProcessTransactionContext()));
			
//			????????? ?????? ????????? ????????? ????????? ??????
			if (this.getParentActivity().getClass() == AllActivity.class) {
				List<Activity> faActs = ((AllActivity)this.getParentActivity()).getChildActivities();
				for (Activity faAct : faActs) {
					if (Activity.STATUS_RUNNING.equals(faAct.getStatus(instance)) && !this.getTracingTag().equals(faAct.getTracingTag())) {
						((FormApprovalActivity)faAct).cancelWorkItem(instance, Activity.STATUS_CANCELLED);
						faAct.setStatus(instance, Activity.STATUS_CANCELLED);
					}
				}
				this.getParentActivity().fireComplete(instance);
			} else {
				getApprovalLineActivity().fireComplete(instance);
			}
			
//			?????? ??????
//			getApprovalLineActivity().fireComplete(instance);			
		}

		EventMessagePayload emp = new EventMessagePayload();
		emp.setTriggerTracingTag(getTracingTag());
		getApprovalLineActivity().fireEventHandlers(instance, EventHandler.TRIGGERING_BY_REJECTED, emp);
	}
	
	public void setApprovalStatus(ProcessInstance instance, String status) throws Exception{
		instance.setProperty(getTracingTag(), KEY_APPR_STATUS, status);
		getApprovalLineActivity().setApprovalLineStatus(instance, status);
	}
	
	public String getApprovalStatus(ProcessInstance instance) throws Exception{
		if(instance==null) return null;
		
		return (String)instance.getProperty(getTracingTag(), KEY_APPR_STATUS);
	}
	
	FormApprovalLineActivity formApprovalLineActivity;
	public FormApprovalLineActivity getApprovalLineActivity() {
		if (formApprovalLineActivity != null) {
			//return formApprovalLineActivity;
		}

		Activity tracing = this;

		do {
			if (FormApprovalLineActivity.class.isAssignableFrom(tracing.getClass())) {
				formApprovalLineActivity = (FormApprovalLineActivity) tracing;

				return formApprovalLineActivity;
			}

			tracing = tracing.getParentActivity();
		} while (tracing != null);

		return null;
	}

	protected void afterComplete(ProcessInstance instance) throws Exception {
		//is Draft
		if(this == getApprovalLineActivity().getDraftActivity()){
			setApprovalStatus(instance, SIGN_DRAFT);
		//is approve
		}else{
			if(!isNotificationWorkitem())
				setApprovalStatus(instance, SIGN_APPROVED);
		}
		super.afterComplete(instance);
		
		EventMessagePayload emp = new EventMessagePayload();
		emp.setTriggerTracingTag(getTracingTag());

		if(this == getApprovalLineActivity().getDraftActivity()){
			getApprovalLineActivity().fireEventHandlers(instance, EventHandler.TRIGGERING_BY_DRAFTED, emp);
		}else{
			getApprovalLineActivity().fireEventHandlers(instance, EventHandler.TRIGGERING_BY_APPROVED, emp);
		}
	}

	protected void onSave(ProcessInstance instance, Map parameterMap_)
			throws Exception {
		if(!isViewMode()){
			super.onSave(instance, parameterMap_);
		}
	}

	boolean isArbitraryApprovable;
		public boolean isArbitraryApprovable() {
			return isArbitraryApprovable;
		}
		public void setArbitraryApprovable(boolean isArbitraryApprovable) {
			this.isArbitraryApprovable = isArbitraryApprovable;
		}

}
