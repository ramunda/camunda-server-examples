package org.camunda.demo.listeners;

import java.util.ArrayList;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.demo.dto.ParameterDTO;

public class ShowFinancialMovement implements TaskListener{

	@Override
	public void notify(DelegateTask delegateTask) {
		Map<String, Object> variables = delegateTask.getVariables();
		
		ArrayList<ParameterDTO> outputParameters = new ArrayList<>();
		outputParameters.add(new ParameterDTO("movements",variables.get("movements")));
		delegateTask.setVariableLocal("outputParameters", outputParameters);
	}
}
