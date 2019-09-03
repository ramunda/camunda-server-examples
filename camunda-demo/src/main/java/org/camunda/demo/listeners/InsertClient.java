package org.camunda.demo.listeners;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.demo.dto.ParameterDTO;

public class InsertClient implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		ArrayList<ParameterDTO> inputParameters = new ArrayList<>();
		inputParameters.add(new ParameterDTO("name",null));
		inputParameters.add(new ParameterDTO("birthdate",null));
		inputParameters.add(new ParameterDTO("gender",null));
		inputParameters.add(new ParameterDTO("notify",null));
		
		delegateTask.setVariableLocal("inputParameters",inputParameters);
	}

}
