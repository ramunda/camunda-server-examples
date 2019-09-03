package org.camunda.demo.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestones, String>{
	
	@Query(value = "Select milestone_name From milestones where tasks @> CAST(ARRAY[?1] as TEXT[]) and ARRAY[milestone_id] <@ (SELECT milestones from process_milestones where proc_def_key=?2)",nativeQuery=true)
	String getMilestoneOfTask(String taskName, String procDefKey);
}
