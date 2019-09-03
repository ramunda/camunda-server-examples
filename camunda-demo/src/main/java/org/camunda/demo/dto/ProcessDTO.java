package org.camunda.demo.dto;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.demo.model.ParameterModel;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.camunda.demo.repo.Milestones;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
  
@JsonInclude(Include.NON_NULL)
public class ProcessDTO {
	
	private String procDefKey;
	private String procInstId;
	private Date startDate;
	private Date endDate;
	private List<ParameterDTO> variables;
	private List<String> milestones;
	private TaskDTO activeTask;
	private String state;
	
	public ProcessDTO() {}
	
	public ProcessDTO(ProcessModel proc) {
		this.procDefKey = proc.getProcDefKey();
		this.procInstId = proc.getProcInstId();
		this.startDate = proc.getStartDate();
		this.endDate = proc.getEndDate();
		this.variables = parseToParamDto(proc.getVariables());
		this.milestones = parseMilestones(proc.getMilestones());
		this.activeTask = proc.getActiveTask() != null ? new TaskDTO(proc.getActiveTask()) : null; //TODO: ERRO ?
		this.state = proc.getState();
	}

	private List<ParameterDTO> parseToParamDto(Stream<ParameterModel> params) {
		
		return params != null ? params
				.map( p -> new ParameterDTO(p.getKey(),p.getValue()) )
				.collect(Collectors.toList())
				: null;
	}
	
	private List<String> parseMilestones(Stream<Milestones> milestones) {
		
		return milestones != null ? milestones
				.map(m -> m.getMilestoneName()) 
				.collect(Collectors.toList())
				: null;
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

	public List<ParameterDTO> getVariables() {
		return variables;
	}

	public void setVariables(List<ParameterDTO> variables) {
		this.variables = variables;
	}

	public List<String> getMilestones() {
		return milestones;
	}

	public void setMilestones(List<String> milestones) {
		this.milestones = milestones;
	}

	public TaskDTO getActiveTask() {
		return activeTask;
	}

	public void setActiveTask(TaskDTO activeTask) {
		this.activeTask = activeTask;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
