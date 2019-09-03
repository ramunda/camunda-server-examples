package org.camunda.demo.bl;

import java.util.stream.Stream;

import org.camunda.demo.dal.ProcessDAL;
import org.camunda.demo.exception.exceptions.BasicException;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProcessBL {
	
	@Autowired
	private ProcessDAL processDAL;

	public ProcessModel startProcessInstance(ProcessModel proc) throws BasicException{
		return processDAL.startProcessInstance(proc);
	}

	public TaskModel nextTask(String processId, TaskModel task, boolean advance) throws BasicException{
		return processDAL.nextTask(processId,task,advance);		
	}

	public TaskModel getActiveTask(String processId) throws BasicException{
		return processDAL.getActiveTask(processId);
	}
	
	public boolean cancelProcInstance(String processId) throws BasicException{
		return processDAL.cancelProcInstance(processId);
	}
	
	public Stream<ProcessModel> procHistory(String procDefKey,String filterName, String filterValue) throws BasicException{
		return processDAL.procHistory(procDefKey,filterName,filterValue);
	}
	
	public ProcessModel getProcessInfo(String procInstId) throws BasicException {
		return processDAL.getProcessInfo(procInstId);
	}
}
