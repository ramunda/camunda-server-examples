package org.camunda.demo.routing;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.demo.bl.ProcessBL;
import org.camunda.demo.dto.ProcessDTO;
import org.camunda.demo.dto.TaskDTO;
import org.camunda.demo.exception.exceptions.BasicException;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;
import org.camunda.demo.utils.Parser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Class with all endpoints that front-end can use to communicate with CAMUNDA BPM.
 */
@CrossOrigin
@RestController
@RequestMapping("/camunda")
public class ProcessController {
	
	@Autowired
	private ProcessBL processBL;
	
	@RequestMapping(value="/process", method= RequestMethod.POST)
    public @ResponseBody ProcessDTO startProcessInstance(@RequestBody ProcessDTO processDTO) throws BasicException{
		
		ProcessModel proc = Parser.parseProcessDtoToProcess(processDTO);
		
		return new ProcessDTO( processBL.startProcessInstance(proc) );
    }
	
	@RequestMapping(value="/process/{processId}/next", method= RequestMethod.POST)
    public @ResponseBody TaskDTO nextTask(@PathVariable String processId,@RequestBody TaskDTO taskDTO, @RequestParam(defaultValue = "true") boolean advance) throws BasicException{
		TaskModel task = Parser.parseTaskDtoToTask(taskDTO);
		
		return new TaskDTO( processBL.nextTask(processId,task,advance) );
    }
	
	@RequestMapping(value="/process/{processId}/task", method= RequestMethod.GET)
    public @ResponseBody TaskDTO getActiveTask(@PathVariable String processId) throws BasicException{
		return new TaskDTO( processBL.getActiveTask(processId) );
    }
	
	@RequestMapping(value="/process/{processId}/cancel", method= RequestMethod.GET)
    public @ResponseBody boolean cancelProcInstance(@PathVariable String processId) throws BasicException{
        return processBL.cancelProcInstance(processId);
    }
	
	@RequestMapping(value="/process/{procDefKey}/history", method= RequestMethod.GET)
    public @ResponseBody List<ProcessDTO> procHistory(@PathVariable String procDefKey,@RequestParam(required=false) String filterName,@RequestParam(required=false) String filterValue) throws BasicException{
		
        return processBL.procHistory(procDefKey,filterName,filterValue)
				.map( ProcessDTO::new )
				.collect(Collectors.toList());
    }
	
	@RequestMapping(value="/process/{procInstId}/info", method= RequestMethod.GET)
    public @ResponseBody ProcessDTO getProcessInfo(@PathVariable String procInstId) throws BasicException{
		
        return new ProcessDTO( processBL.getProcessInfo(procInstId) );
    }
}
