package org.camunda.demo.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.demo.formatters.ProcessFormatter;
import org.camunda.demo.ProcessDefinition;
import org.camunda.demo.dto.ParameterDTO;
import org.camunda.demo.exception.exceptions.BasicException;
import org.camunda.demo.exception.exceptions.CamundaException;
import org.camunda.demo.exception.exceptions.DataNotFoundException;
import org.camunda.demo.exception.exceptions.InvalidParametersException;
import org.camunda.demo.model.ParameterModel;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.camunda.demo.repo.MilestoneRepository;
import org.camunda.demo.repo.Milestones;
import org.camunda.demo.repo.ProcessMilestones;
import org.camunda.demo.repo.ProcessRepository;
import org.camunda.demo.utils.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Class that communicates directly with CAMUNDA BPM using the CAMUNDA services API and 
 * POSTGRES database using JpaRepository extended interfaces.
 * 
 */
@Service
public class ProcessService {

	@Autowired
	private ProcessRepository processRepository; 
	
	@Autowired
	private MilestoneRepository milestoneRepository;
	
	@Autowired
    private RuntimeService runtimeService;
	
	@Autowired
    private TaskService taskService;
	
	@Autowired
    private HistoryService historyService;
	
	@Autowired
    private ProcessFormatter processFormatter;

	public ProcessModel startProcessInstance(ProcessModel proc) throws BasicException{
		Map<String, Object> variables = new HashMap<String, Object>();
		String procDefKey = proc.getProcDefKey();
		
		if(!processFormatter.formatter.containsKey(procDefKey))
			throw new InvalidParametersException("Don't exist any formatter defined for Process Definition Key: " + procDefKey);
			
		List<String> procVars = processFormatter.formatter.get(procDefKey).getInVariables();
		List<ParameterModel> inVars = proc.getVariables().collect(Collectors.toList());
		boolean found;

		for(String s : procVars) {
			found = false;
			for(ParameterModel p : inVars) {
				if(p.getKey().equals(s)) {
					variables.put(p.getKey(), p.getValue());
					found = true;
				}	
			}
			if(!found)
				throw new InvalidParametersException("Invalid Parameter " + s);
		}
        
		ProcessInstance pi;
		try {
			pi = runtimeService.startProcessInstanceByKey(procDefKey,variables);
		}catch(ProcessEngineException e) {
			throw new CamundaException( e.getMessage() );
		}
		
		Optional<ProcessMilestones> pms = processRepository.findById(procDefKey);
		
		TaskModel currTask = getCurrentTask(pi.getId(),false);
		Stream<ParameterModel> outputVariables = getProcessVariables(pi.getId(),procDefKey);
		
		if(!pms.isPresent()) {
			return new ProcessModel(procDefKey,pi.getId(),currTask,outputVariables,Stream.empty());
		}

		List<String> milestoneIds = Arrays.asList(pms.get().getMilestones());
		Stream<Milestones> milestones = milestoneRepository.findAllById(milestoneIds).stream();
				
		return new ProcessModel(procDefKey,pi.getId(),currTask,outputVariables,milestones);
	}

	public TaskModel nextTask(String processId, TaskModel taskModel, boolean advance) throws BasicException {
		Task task = taskService.createTaskQuery()
				.taskId(taskModel.getId())
				.singleResult();
		
		if(task == null)
			throw new DataNotFoundException( String.format("No task found with id '%s'", taskModel.getId())); 
			
		String taskId = task.getId();

		taskService.claim(taskId,task.getOwner());
		
		setTaskVariables(taskId,taskModel,advance);
		
		taskService.complete(taskId);

		return getCurrentTask(processId,false);
	}
	
	/**
	 * 
	 * Method that sets in BPM the parameters/variables of a specific task.
	 * 
	 * @param taskId the id of the task passed. 
	 * @param task internal domain object representing the current task.
	 * @param advance flag to set in BPM to move forward or backward in work flow.
	 * 
	 */
	private void setTaskVariables(String taskId, TaskModel task, boolean advance) {
		
		task.getInParameters().forEach(
				p -> taskService.setVariable(taskId, p.getKey(), p.getValue())
		);
		
		taskService.setVariable(taskId,"next",advance);
	}
	

	/**
	 * 
	 * Method that get the process's current task in BPM and returns it as an 
	 * internal domain object (TaskModel). Can be called as utility method by other methods
	 * of this class or by DAL depending on throwError parameter.
	 * 
	 * @param processId the id of the process we want the current task.
	 * @param throwError if called by DAL should be true, false if we want to call it
	 * 			as a utility method for other methods in this class.
	 * @return TaskModel instance that represents the current task of the process with id
	 * 			passed in parameter processId. 
	 * @throws BasicException thrown when method is called from outside and doesn't exist any 
	 * 			active task for the process with id passed in parameter processId.
	 *
	 */
	public TaskModel getCurrentTask(String processId, boolean throwError) throws BasicException {

		Task currTask = taskService.createTaskQuery()
				.processInstanceId(processId)
				.singleResult();

		if(currTask == null && throwError)
			throw new DataNotFoundException("Don't exist any active task in this process");
		else if(currTask == null && !throwError)
			return null;
			
		String procDefKey = currTask.getProcessDefinitionId().split(":")[0];
		String milestone = milestoneRepository.getMilestoneOfTask(currTask.getTaskDefinitionKey(), procDefKey);

		ArrayList<ParameterDTO> outputParamsDTO = (ArrayList<ParameterDTO>) taskService.getVariableLocal(currTask.getId(), "outputParameters");
		ArrayList<ParameterDTO> inputParamsDTO = (ArrayList<ParameterDTO>) taskService.getVariableLocal(currTask.getId(), "inputParameters");
		
		return new TaskModel(
				currTask.getId(),
				currTask.getTaskDefinitionKey(),
				milestone,
				Parser.parseParamDtoToParam(inputParamsDTO),
				Parser.parseParamDtoToParam(outputParamsDTO)
		);
	}

	public boolean cancelProcInstance(String processId) throws BasicException {
		try {
			runtimeService.deleteProcessInstance(processId,null);
		}catch(BadUserRequestException e) {
			throw new DataNotFoundException( e.getMessage() );
		}
		
		return runtimeService.createProcessInstanceQuery()
				.processInstanceId(processId).singleResult() == null;
	}

	public Stream<ProcessModel> procHistory(String procDefKey, String filterName, String filterValue) throws BasicException {
		
		if(!processFormatter.formatter.containsKey(procDefKey))
			throw new InvalidParametersException("Don't exist any formatter defined for Process Definition Key: " + procDefKey);
		
		List<HistoricProcessInstance> hpis = null;
		
		if(filterName == null && filterValue == null) {
			hpis = historyService.createHistoricProcessInstanceQuery()
					.processDefinitionKey(procDefKey)
					.orderByProcessInstanceStartTime()
					.desc()
					.list();
		}
		else {
			hpis = historyService.createHistoricProcessInstanceQuery()
					.processDefinitionKey(procDefKey)
					.variableValueEquals(filterName, filterValue)
					.orderByProcessInstanceStartTime()
					.desc()
					.list();
		}
		
		return hpis.stream().map(this::parseHPI);
	}
	
	public ProcessModel getProcessInfo(String procInstId) throws BasicException {
		
		HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery()
				.processInstanceId(procInstId).singleResult();
		
		if(pi == null)
			throw new DataNotFoundException("Don't exist any process with instance id " + procInstId);
			
		String procDefKey = pi.getProcessDefinitionKey();

		if(!processFormatter.formatter.containsKey(procDefKey))
			throw new InvalidParametersException("Don't exist any formatter defined for Process Definition Key: " + procDefKey);
		
		Stream<ParameterModel> outputVariables = getProcessVariables(pi.getId(),procDefKey);

		Optional<ProcessMilestones> pms = processRepository.findById(procDefKey);
		TaskModel currTask = getCurrentTask(pi.getId(),false);
				
		if(!pms.isPresent()) {
			return new ProcessModel(procDefKey,pi.getId(),pi.getStartTime(),
								pi.getEndTime(),pi.getState(),currTask,outputVariables,Stream.empty()
			); 
		}

		List<String> milestoneIds = Arrays.asList(pms.get().getMilestones());
		Stream<Milestones> milestones = milestoneRepository.findAllById(milestoneIds).stream();
				
		return new ProcessModel(procDefKey,pi.getId(),
				pi.getStartTime(),pi.getEndTime(),pi.getState(),currTask,
				outputVariables,milestones
			);
	}
	
	/**
	 * Parse a CAMUNDA BPM model to an internal domain object (ProcessModel).
	 * 
	 * @param hpi CAMUNDA BPM model that represents a process instance terminated or not.
	 * @return ProcessModel instance representing the process passed as parameter.
	 */
	private ProcessModel parseHPI(HistoricProcessInstance hpi) {

		TaskModel task = null;
		String procDefKey = hpi.getProcessDefinitionKey();
		
		if (hpi.getEndTime() == null){
			try {
				task = getCurrentTask(hpi.getId(),false);
			} catch (BasicException ignored) {}
		}
	
		Stream<ParameterModel> outputVariables = getProcessVariables(hpi.getId(),hpi.getProcessDefinitionKey());
		
		Optional<ProcessMilestones> pms = processRepository.findById(procDefKey);
				
		if(!pms.isPresent()) {
			return new ProcessModel(procDefKey,hpi.getId(),
					hpi.getStartTime(),hpi.getEndTime(),hpi.getState(),task,
					outputVariables,Stream.empty()
				);
		}

		List<String> milestoneIds = Arrays.asList(pms.get().getMilestones());
		Stream<Milestones> milestones = milestoneRepository.findAllById(milestoneIds).stream();
		
		return new ProcessModel(procDefKey,hpi.getId(),
					hpi.getStartTime(),hpi.getEndTime(),hpi.getState(),task,
					outputVariables,milestones
				);
	}
	
	/**
	 * 
	 * Utility method for get the output variables of a specific process.
	 * 
	 * @param procId process's id 
	 * @param procDefKey process definition key of the process. Used to access the map
	 * 			which the value is the variables that the process should return.
	 * @return a stream containing all output variables for the process with id passed in
	 * 			procId parameter.
	 */
	private Stream<ParameterModel> getProcessVariables(String procId,String procDefKey) {
		
		Stream<ParameterModel> variables = historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(procId).list()
				.stream()
				.filter( v -> v.getTaskId() == null)
				.map( v -> new ParameterModel(v.getName(),v.getValue()));
		
		ProcessDefinition pd = processFormatter.formatter.get(procDefKey);
		
		List<String> outputVariables = new ArrayList<String>();
		outputVariables.addAll(pd.getInVariables());
		outputVariables.addAll(pd.getOutVariables());
		
		return variables.filter( v -> outputVariables.contains(v.getKey()));
	}
}
