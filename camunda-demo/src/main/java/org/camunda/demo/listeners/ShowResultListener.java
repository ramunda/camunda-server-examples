package org.camunda.demo.listeners;

import java.util.ArrayList;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.demo.dto.ParameterDTO;

public class ShowResultListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		Map<String, Object> variables = delegateTask.getVariables();
		
		ArrayList<ParameterDTO> outputParameters = new ArrayList<>();
		outputParameters.add(new ParameterDTO("n1", variables.get("n1") ));
		outputParameters.add(new ParameterDTO("n2", variables.get("n2") ));
		outputParameters.add(new ParameterDTO("toSum", variables.get("toSum")));
		outputParameters.add(new ParameterDTO("result", variables.get("result")));
		delegateTask.setVariableLocal("outputParameters", outputParameters);
	}

}
