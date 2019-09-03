package org.camunda.demo.utils;

import java.util.List;
import java.util.stream.Stream;

import org.camunda.demo.dto.ParameterDTO;
import org.camunda.demo.dto.ProcessDTO;
import org.camunda.demo.dto.TaskDTO;
import org.camunda.demo.exception.exceptions.InvalidParametersException;
import org.camunda.demo.model.ParameterModel;
import org.camunda.demo.model.ProcessModel;
import org.camunda.demo.model.TaskModel;

/**
 * Utility class for parse external domain objects in internal domain objects.
 */
public class Parser {
	
	public static TaskModel parseTaskDtoToTask ( TaskDTO taskDto) {
		return 	new TaskModel( 
				taskDto.getId(),
				taskDto.getMilestone(),
				parseParamDtoToParam(taskDto.getInParameters()),
				parseParamDtoToParam(taskDto.getOutParameters())
		);
	}
	
	public static Stream<ParameterModel> parseParamDtoToParam ( List<ParameterDTO> parameterDto) {
		return parameterDto != null ? parameterDto
				.stream()
				.map( (pdto) -> new ParameterModel(pdto.getKey(),pdto.getValue()) ) : Stream.empty();
	}


	public static ProcessModel parseProcessDtoToProcess(ProcessDTO procDTO) throws InvalidParametersException {
		
		if(procDTO.getProcDefKey() == null)
			throw new InvalidParametersException("Invalid Process Definition Key");
			
		Stream<ParameterModel> variables = parseParamDtoToParam(procDTO.getVariables());
		
		return new ProcessModel(procDTO.getProcDefKey(),variables); 
	}
}
