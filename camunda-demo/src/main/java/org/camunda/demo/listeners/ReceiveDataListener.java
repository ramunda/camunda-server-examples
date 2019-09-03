package org.camunda.demo.listeners;

import java.util.ArrayList;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.demo.dto.ParameterDTO;

public class ReceiveDataListener implements TaskListener {

	@Override
	public void notify(DelegateTask delegateTask) {
		ArrayList<ParameterDTO> inputParameters = new ArrayList<>();
		inputParameters.add(new ParameterDTO("n1",null));
		inputParameters.add(new ParameterDTO("n2",null));
		inputParameters.add(new ParameterDTO("toSum",null));
		delegateTask.setVariableLocal("inputParameters",inputParameters);
	}

}
