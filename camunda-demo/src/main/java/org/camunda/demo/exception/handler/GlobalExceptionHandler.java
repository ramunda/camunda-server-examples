package org.camunda.demo.exception.handler;


import org.camunda.demo.exception.exceptions.CamundaException;
import org.camunda.demo.exception.exceptions.DataNotFoundException;
import org.camunda.demo.exception.exceptions.InvalidParametersException;
import org.camunda.demo.exception.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler{

	@ExceptionHandler
	public ResponseEntity<ErrorModel> handleDataNotFoundException(DataNotFoundException ex){
		ErrorModel error = new ErrorModel("DataNotFound",ex.getMessage());
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(error);
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorModel> handleCamundaException(CamundaException ex){
		ErrorModel error = new ErrorModel("CamundaException",ex.getMessage());
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(error);
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorModel> handleInvalidParametersException(InvalidParametersException ex){
		ErrorModel error = new ErrorModel("InvalidParametersException",ex.getMessage());
		
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(error);
	}
}
