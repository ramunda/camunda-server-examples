package org.camunda.demo.repo;

import java.util.Arrays;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.vladmihalcea.hibernate.type.array.StringArrayType;

@TypeDef(name = "string-array", typeClass = StringArrayType.class)
@Entity
public class Milestones {
	@Id
	private String milestoneId;
	private String milestoneName;
	@Type(type = "string-array")
	@Column(name= "tasks", columnDefinition = "text[]")
	private String[] tasks;
	
	
	public String getMilestoneId() {
		return milestoneId;
	}
	public void setMilestoneId(String milestoneId) {
		this.milestoneId = milestoneId;
	}
	public String[] getTasks() {
		return tasks;
	}
	public void setTasks(String[] tasks) {
		this.tasks = tasks;
	}
	public String getMilestoneName() {
		return milestoneName;
	}
	public void setMilestoneName(String milestoneName) {
		this.milestoneName = milestoneName;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;

		Milestones m = (Milestones) obj;
		return this.milestoneId.equals(m.getMilestoneId()) &&
				this.milestoneName.equals(m.getMilestoneName()) &&
				Arrays.equals(this.tasks, m.getTasks());
	}
}
