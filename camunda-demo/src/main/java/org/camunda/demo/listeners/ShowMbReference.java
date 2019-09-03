package org.camunda.demo.listeners;

import java.util.ArrayList;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.demo.dto.ParameterDTO;

public class ShowMbReference implements TaskListener{
	@Override
	public void notify(DelegateTask delegateTask) {
		Map<String, Object> variables = delegateTask.getVariables();
		
		ArrayList<ParameterDTO> outputParameters = new ArrayList<>();
		outputParameters.add(new ParameterDTO("entity", variables.get("entity") ));
		outputParameters.add(new ParameterDTO("reference", variables.get("reference") ));
		outputParameters.add(new ParameterDTO("amount", variables.get("amount")));
		delegateTask.setVariableLocal("outputParameters", outputParameters);

	}
}
