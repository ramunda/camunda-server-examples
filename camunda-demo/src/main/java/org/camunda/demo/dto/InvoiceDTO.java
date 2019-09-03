package org.camunda.demo.dto;

import java.io.Serializable;

public class InvoiceDTO implements Serializable{

	private String id;
	
	
	public InvoiceDTO() {}

	public InvoiceDTO(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
