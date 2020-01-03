USE test;

CREATE TYPE address (
	street text,
	city text,
	zip text,
	floor int,
	enable boolean,
	note text
);

CREATE TYPE child (
	birthday date
);

CREATE TYPE cgu (
	code text,
	version text,
	note text
);

CREATE TABLE account ( 
	id UUID PRIMARY KEY,
	email text,
	status text,
	lastname text, 
	firstname text, 
	birthday date,
	address address,	
	addresses map<text, frozen<address>>,
	children map<int, frozen<child>>,
	phones map<text, text>,
	notes map<timestamp, text>,
	profils set<text>,
	cgus set<frozen<cgu>>,
	age int,
	subscription_1 boolean,
	creation_date timestamp,
	preferences list<frozen<tuple<text, text, int, timestamp>>>,
	phones2 map<text, text>,
	cgus2 set<text>,
	content blob
);

CREATE TABLE login (
	id UUID,
	login text PRIMARY KEY,
	password text,
	enable boolean,
	note text
);

CREATE TABLE subscription (
	email text PRIMARY KEY
);

CREATE TABLE account_history ( 
	id UUID,
	field text,
	date timestamp,
	value text,
	PRIMARY KEY ((id), field)
);

CREATE TYPE schema_label_type (
	type text,
	description text,
	labels map<text, text>
);

CREATE TABLE parameter ( 
	app text PRIMARY KEY,
	histories map<text, boolean>
);

CREATE TABLE resource_schema ( 
	app text,
	version text,
	resource text,
	content blob,
	filter set<text>,
	rule map<text, frozen<set<text>>>,
	PRIMARY KEY ((app), resource, version)
);

CREATE TABLE account_schema ( 
	app text PRIMARY KEY,
	labels map<text, frozen<schema_label_type>>
);

CREATE INDEX IF NOT EXISTS account_status
	ON account ( status );
	
CREATE INDEX IF NOT EXISTS login_id
	ON login ( id );
	
CREATE TABLE stock ( 
	store text,
	product text,
	quantity counter,
	PRIMARY KEY ((store, product))
);