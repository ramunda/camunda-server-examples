package org.camunda.demo.dto;

import java.io.Serializable;

public class MovementDTO implements Serializable{

	private String amount;
	private String balance;
	private String saftDocument;
	private String paymentLimitDate;
	private String status;
	private String payableAmount;
	private InvoiceDTO invoice;

	
	public MovementDTO() {}

	public MovementDTO(String amount, String balance, String saftDocument, String paymentLimitDate, String status,
			String payableAmount, InvoiceDTO invoice) {
		this.amount = amount;
		this.balance = balance;
		this.saftDocument = saftDocument;
		this.paymentLimitDate = paymentLimitDate;
		this.status = status;
		this.payableAmount = payableAmount;
		this.invoice = invoice;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getSaftDocument() {
		return saftDocument;
	}

	public void setSaftDocument(String saftDocument) {
		this.saftDocument = saftDocument;
	}

	public String getPaymentLimitDate() {
		return paymentLimitDate;
	}

	public void setPaymentLimitDate(String paymentLimitDate) {
		this.paymentLimitDate = paymentLimitDate;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPayableAmount() {
		return payableAmount;
	}

	public void setPayableAmount(String payableAmount) {
		this.payableAmount = payableAmount;
	}

	public InvoiceDTO getInvoice() {
		return invoice;
	}

	public void setInvoice(InvoiceDTO invoice) {
		this.invoice = invoice;
	}

	@Override
	public boolean equals(Object obj) {
		MovementDTO m = (MovementDTO)obj;
		return this.amount.equals(m.getAmount()) && 
				this.balance.equals(m.getBalance()) &&
				this.saftDocument.equals(m.getSaftDocument()) &&
				this.paymentLimitDate.equals(m.getPaymentLimitDate()) &&
				this.invoice.getId().equals(m.getInvoice().getId());
	}	
}

