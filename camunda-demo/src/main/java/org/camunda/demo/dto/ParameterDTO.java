package org.camunda.demo.dto;

import java.io.Serializable;

import org.camunda.demo.model.ParameterModel;

public class ParameterDTO implements Serializable {
	
	private String key;
	private Object value;
	
	public ParameterDTO() {}
	
	public ParameterDTO(String key, Object value) {
		this.key = key;
		this.value = value;
	}
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
		ParameterDTO p = (ParameterDTO) obj;
		
		if(p.getValue() == null && this.value == null)
			return p.getKey().equals(this.key);
		
		return p.getKey().equals(this.key) && p.getValue().equals(this.value);
	}
}
