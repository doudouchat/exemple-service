CREATE KEYSPACE IF NOT EXISTS other_keyspace 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 1
};

USE other_keyspace;

CREATE TABLE IF NOT EXISTS resource_schema ( 
	version text,
	resource text,
	profile text,
	content text,
	patch set<text>,
	PRIMARY KEY ((resource), version, profile)
);