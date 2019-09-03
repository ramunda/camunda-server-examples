package org.camunda.demo.formatters;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import javax.annotation.PostConstruct;

import org.camunda.demo.ProcessDefinition;
import org.springframework.stereotype.Component;

import static java.util.Collections.emptyList;

/*
 * Should define here the input and output variables of a process.
 * Key: Process Definition Key, Value: ProcessDefinition instance with input and output variables respectively 
 */
@Component
public class OutputFormatter {
	
	public HashMap<String,ProcessDefinition> formatter;
	
	@PostConstruct
	public void init() {
		formatter = new HashMap<>();
	
		formatter.put("Process_08rb4ct", new ProcessDefinition(
				Arrays.asList("client","clientDetails","user"),
				Arrays.asList("reference","amount","entity")
		));
		
		formatter.put("Process_05r67sz", new ProcessDefinition(
				emptyList(),
				Arrays.asList("n1","n2","toSum","result")
		));
		
		formatter.put("Process_02jaj7t", new ProcessDefinition(
				emptyList(),
				Arrays.asList("reference","amount","entity")
		));
		
	}
}
