package org.camunda.demo;

import java.util.List;

public class ProcessDefinition {
	private List<String> inVariables;
	private List<String> outVariables;
	
	public ProcessDefinition(List<String> inVariables, List<String> outVariables) {
		this.inVariables = inVariables;
		this.outVariables = outVariables;
	}
	public List<String> getInVariables() {
		return inVariables;
	}
	public void setInVariables(List<String> inVariables) {
		this.inVariables = inVariables;
	}
	public List<String> getOutVariables() {
		return outVariables;
	}
	public void setOutVariables(List<String> outVariables) {
		this.outVariables = outVariables;
	}
}
