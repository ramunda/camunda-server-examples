package org.camunda.demo.serviceTaskImpl;

import java.io.File;
import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.demo.FMDataOutput;
import org.camunda.demo.dto.MovementDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FinancialMovement implements JavaDelegate{
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {

		ObjectMapper objectMapper = new ObjectMapper();
		FMDataOutput dataOutput = objectMapper.readValue(new File("src/main/java/org/camunda/demo/getFinancialMovement.json"), FMDataOutput.class);

		if(dataOutput != null) {
			List<MovementDTO> movements = dataOutput.getMovement();
			execution.setVariable("movements", movements);
		}

	}

}
