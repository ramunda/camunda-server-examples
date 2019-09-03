CREATE TABLE process_milestones(
	proc_def_key TEXT primary key,
	milestones TEXT[]
);

CREATE TABLE milestones(
	milestone_id TEXT PRIMARY KEY,
	milestone_name TEXT,
	tasks TEXT[]
);