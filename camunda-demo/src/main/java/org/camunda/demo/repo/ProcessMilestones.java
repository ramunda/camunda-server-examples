package org.camunda.demo.repo;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import com.vladmihalcea.hibernate.type.array.StringArrayType;

@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@Entity
public class ProcessMilestones {
	@Id
	private String procDefKey;
	@Type(type = "string-array")
	@Column(name= "milestones", columnDefinition = "text[]")
	private String[] milestones;
	
	
	public String getProcDefKey() {
		return procDefKey;
	}
	public void setProcDefKey(String procDefKey) {
		this.procDefKey = procDefKey;
	}
	public String[] getMilestones() {
		return milestones;
	}
	public void setMilestones(String[] milestones) {
		this.milestones = milestones;
	}
	
}
