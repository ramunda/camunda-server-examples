package org.camunda.demo;

import java.util.List;

import org.camunda.demo.dto.MovementDTO;

public class FMDataOutput {
	protected List<MovementDTO> movement;

	public List<MovementDTO> getMovement() {
		return movement;
	}

	public void setMovement(List<MovementDTO> movement) {
		this.movement = movement;
	}
	
	
}
