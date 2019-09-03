package org.camunda.demo.dal;

import java.util.stream.Stream;

import org.camunda.demo.exception.exceptions.BasicException;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.camunda.demo.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessDAL {

	@Autowired
	private ProcessService processService;
	
	public ProcessModel startProcessInstance(ProcessModel proc) throws BasicException{
		return processService.startProcessInstance(proc);
	}

	public TaskModel nextTask(String processId, TaskModel task, boolean advance) throws BasicException{
		return processService.nextTask(processId,task,advance);
	}

	public boolean cancelProcInstance(String processId) throws BasicException{
		return processService.cancelProcInstance(processId);
	}

	public TaskModel getActiveTask(String processId) throws BasicException{
		return processService.getCurrentTask(processId,true);
	}

	public Stream<ProcessModel> procHistory(String procDefKey,String filterName, String filterValue) throws BasicException {
		return processService.procHistory(procDefKey,filterName,filterValue);
	}
	
	public ProcessModel getProcessInfo(String procInstId) throws BasicException {
		return processService.getProcessInfo(procInstId);
	}
}
