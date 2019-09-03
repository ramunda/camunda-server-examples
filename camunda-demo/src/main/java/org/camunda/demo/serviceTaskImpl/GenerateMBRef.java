package org.camunda.demo.serviceTaskImpl;

import java.io.File;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.demo.PRDataOutput;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GenerateMBRef implements JavaDelegate{
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		ObjectMapper objectMapper = new ObjectMapper();
		PRDataOutput dataOutput = objectMapper.readValue(new File("src/main/java/org/camunda/demo/getPaymentReference.json"), PRDataOutput.class);
		
		if(dataOutput != null) {
			execution.setVariable("entity", dataOutput.getEntity());
			execution.setVariable("reference", dataOutput.getReference());
			execution.setVariable("amount", dataOutput.getAmount());
		}
	}

}