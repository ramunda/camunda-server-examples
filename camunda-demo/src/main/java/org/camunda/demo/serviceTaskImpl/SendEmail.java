package org.camunda.demo.serviceTaskImpl;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class SendEmail implements JavaDelegate{

    // Need to set this variables
    private String username = ""; // email of sender
    private String password = ""; // password of sender
    private String emailFrom = ""; // email of sender

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		
		String amount = (String) execution.getVariable("amount");
		String entity = (String) execution.getVariable("entity");
		String reference = (String) execution.getVariable("reference");
		String emailTo = (String) execution.getVariable("email");

		Properties prop = System.getProperties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");

        Session session = Session.getInstance(prop,
        	      new javax.mail.Authenticator() {
        	        protected PasswordAuthentication getPasswordAuthentication() {
        	            return new PasswordAuthentication(username,password);
        	        }
        	      });
        
       try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(emailTo));
            message.setSubject("Detalhes pagamento");
            message.setText("Os dados para pagamento são: \n Entidade: " + entity +"\nReferência: " + reference + "\nValor: " + amount); 
            Transport.send(message);
        } catch (MessagingException e) {
        	e.printStackTrace();
        }
       
			
	}

}
