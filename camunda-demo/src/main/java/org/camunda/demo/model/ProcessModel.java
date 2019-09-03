package org.camunda.demo.model;

import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.camunda.demo.repo.Milestones;

public class ProcessModel {

	private String procDefKey;
	private String procInstId;
	private Date startDate;
	private Date endDate;
	private Stream<ParameterModel> variables;
	private Stream<Milestones> milestones;
	private TaskModel activeTask;
	private String state;


	public ProcessModel() {}
	
	
	public ProcessModel(String procDefKey, String procInstId,Date start,Date end,String state,TaskModel currentTask,Stream<ParameterModel> variables,Stream<Milestones> milestones) {
		this.procDefKey = procDefKey;
		this.procInstId = procInstId;
		this.startDate = start;
		this.endDate = end;
		this.state = state;
		this.activeTask = currentTask;
		this.variables = variables;
		this.milestones = milestones;
	}

	public ProcessModel(String procDefKey, String procInstId, TaskModel currentTask, Stream<ParameterModel> outputVariables,Stream<Milestones> milestones) {
		this.procDefKey = procDefKey;
		this.procInstId = procInstId;
		this.activeTask = currentTask;
		this.milestones = milestones;
		this.variables = outputVariables;
	}


	public ProcessModel(String procDefKey,Stream<ParameterModel> variables) {
		this.procDefKey = procDefKey;
		this.variables = variables;
	}


	public String getProcDefKey() {
		return procDefKey;
	}


	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}


	public String getProcInstId() {
		return procInstId;
	}


	public void setProcInstId(String procInstId) {
		this.procInstId = procInstId;
	}

	public Stream<ParameterModel> getVariables() {
		return variables;
	}


	public void setVariables(Stream<ParameterModel> variables) {
		this.variables = variables;
	}

	public TaskModel getActiveTask() {
		return activeTask;
	}


	public void setActiveTask(TaskModel activeTask) {
		this.activeTask = activeTask;
	}


	public Stream<Milestones> getMilestones() {
		return milestones;
	}


	public void setMilestones(Stream<Milestones> milestones) {
		this.milestones = milestones;
	}


	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
