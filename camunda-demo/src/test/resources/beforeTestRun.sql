CREATE TABLE process_milestones(
	proc_def_key TEXT primary key,
	milestones TEXT[]
);

CREATE TABLE milestones(
	milestone_id TEXT PRIMARY KEY,
	milestone_name TEXT,
	tasks TEXT[]
);

INSERT INTO process_milestones
	VALUES('Process_05r67sz','{"1","2"}');
	
INSERT INTO milestones
	VALUES('1','Input','{"Task_0w2jxuh"}'),('2','Show Result','{"Task_1ppfanf"}');

INSERT INTO process_milestones
	VALUES('Process_08rb4ct','{"3","4"}');

INSERT INTO milestones
	VALUES('3','Movimentos','{"Task_1wayfyf"}'),('4','Referência Multibanco','{"Task_19epn2w"}');