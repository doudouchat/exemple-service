CREATE KEYSPACE IF NOT EXISTS test_keyspace 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 1
};

USE test_keyspace;

CREATE TYPE IF NOT EXISTS address (
	street text,
	city text,
	zip text,
	floor int,
	enable boolean
);

CREATE TYPE IF NOT EXISTS cgu (
	code text,
	version text
);

CREATE TABLE IF NOT EXISTS account ( 
	id UUID PRIMARY KEY,
	email text,
	status text,
	civility text, 
	lastname text, 
	firstname text, 
	birthday date,
	address address,	
	addresses map<text, frozen<address>>,
	profils set<text>,
	cgus set<frozen<cgu>>,
	age int,
	optin_mobile boolean,
	mobile text,
	creation_date timestamp,
	update_date timestamp,
	preferences list<frozen<map<text, text>>>
);

CREATE TABLE IF NOT EXISTS subscription (
	email text PRIMARY KEY,
	subscription_date timestamp,
);

CREATE TABLE IF NOT EXISTS account_history ( 
	id UUID,
	field text,
	date timestamp,
	value text,
	application text,
	version text,
	user text,
	previous_value text,
	PRIMARY KEY ((id), field)
);

CREATE TABLE IF NOT EXISTS account_event ( 
	id UUID,
	date timestamp,
	user text,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((id), date)
) WITH cdc=true;

CREATE TABLE account_username ( 
	id UUID,
	username text,
	field text,
	PRIMARY KEY ((username, field))
);

CREATE TABLE IF NOT EXISTS subscription_event ( 
	email text,
	date timestamp,
	user text,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((email), date)
) WITH cdc=true;

CREATE TABLE IF NOT EXISTS subscription_history ( 
	email text,
	field text,
	date timestamp,
	value text,
	application text,
	version text,
	user text,
	previous_value text,
	PRIMARY KEY ((email), field)
);

CREATE TABLE IF NOT EXISTS resource_schema ( 
	version text,
	resource text,
	profile text,
	content text,
	patch set<text>,
	PRIMARY KEY ((resource), version, profile)
);
	
CREATE TABLE IF NOT EXISTS stock ( 
	store text,
	product text,
	quantity counter,
	PRIMARY KEY ((store, product))
);

CREATE TABLE IF NOT EXISTS stock_history ( 
	store text,
	product text,
	date timestamp,
	user text,
	application text,
	quantity counter,
	PRIMARY KEY ((store, product), date, user, application)
) WITH cdc=true;