package org.camunda.demo.model;

import java.util.List;
import java.util.stream.Stream;

public class TaskModel {
	
	private String id;
	private String taskDefKey;
	private Stream<ParameterModel> inParameters;
	private Stream<ParameterModel> outParameters;
	private String milestone;
	
	
	public TaskModel(String id, String name, String milestone, Stream<ParameterModel> inParams,Stream<ParameterModel> outParams) {
		this.id = id;
		this.milestone = milestone;
		this.taskDefKey = name;
		this.inParameters = inParams;
		this.outParameters = outParams;
	}

	public TaskModel(String id,String milestone, Stream<ParameterModel> inParams,Stream<ParameterModel> outParams) {
		this.id = id;
		this.milestone = milestone;
		this.inParameters = inParams;
		this.outParameters = outParams;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Stream<ParameterModel> getInParameters() {
		return inParameters;
	}

	public void setInParameters(Stream<ParameterModel> inParameters) {
		this.inParameters = inParameters;
	}

	public Stream<ParameterModel> getOutParameters() {
		return outParameters;
	}

	public void setOutParameters(Stream<ParameterModel> outParameters) {
		this.outParameters = outParameters;
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}

	public String getTaskDefKey() {
		return taskDefKey;
	}

	public void setTaskDefKey(String name) {
		this.taskDefKey = name;
	}
}
