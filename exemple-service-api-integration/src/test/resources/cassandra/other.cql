CREATE KEYSPACE IF NOT EXISTS other 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 1
};

USE other;

CREATE TABLE IF NOT EXISTS resource_schema ( 
	app text,
	version text,
	resource text,
	profile text,
	content text,
	filter set<text>,
	patch set<text>,
	PRIMARY KEY ((app), resource, version, profile)
);