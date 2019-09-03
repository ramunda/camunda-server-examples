package org.camunda.demo.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.demo.model.ParameterModel;
import org.camunda.demo.model.TaskModel;

public class TaskDTO {
	
	private String id;
	private List<ParameterDTO> inParameters;
	private List<ParameterDTO> outParameters;
	private String milestone;
	
	public TaskDTO() {}
	
	public TaskDTO(TaskModel task) {
		if(task != null) {
			this.id = task.getId();
			this.milestone = task.getMilestone();
			this.inParameters = parseToDto( task.getInParameters() );
			this.outParameters = parseToDto( task.getOutParameters() );
		} 
	}
	
	private List<ParameterDTO> parseToDto(Stream<ParameterModel> params) {
		return params != null ? params
				.map( p -> new ParameterDTO(p.getKey(), p.getValue() ) )
 				.collect(Collectors.toList()) : null;
	}
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<ParameterDTO> getInParameters() {
		return inParameters;
	}

	public void setInParameters(List<ParameterDTO> inParameters) {
		this.inParameters = inParameters;
	}

	public List<ParameterDTO> getOutParameters() {
		return outParameters;
	}

	public void setOutParameters(List<ParameterDTO> outParameters) {
		this.outParameters = outParameters;
	}

	public String getMilestone() {
		return milestone;
	}

	public void setMilestone(String milestone) {
		this.milestone = milestone;
	}
}
