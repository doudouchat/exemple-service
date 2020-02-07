CREATE TYPE test.address (
	street text,
	city text,
	zip text,
	floor int,
	enable boolean,
	note text
);

CREATE TYPE test.child (
	birthday date
);

CREATE TYPE test.cgu (
	code text,
	version text,
	note text
);

CREATE TABLE test.account ( 
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

CREATE TABLE test.login (
	id UUID,
	login text PRIMARY KEY,
	password text,
	enable boolean,
	note text
);

CREATE TABLE test.subscription (
	email text PRIMARY KEY
);

CREATE TABLE test.account_history ( 
	id UUID,
	field text,
	date timestamp,
	value text,
	PRIMARY KEY ((id), field)
);

CREATE TYPE test.schema_label_type (
	type text,
	description text,
	labels map<text, text>
);

CREATE TABLE test.parameter ( 
	app text PRIMARY KEY,
	histories map<text, boolean>
);

CREATE TABLE test.resource_schema ( 
	app text,
	version text,
	resource text,
	content blob,
	filter set<text>,
	rule map<text, frozen<set<text>>>,
	PRIMARY KEY ((app), resource, version)
);

CREATE TABLE test.account_schema ( 
	app text PRIMARY KEY,
	labels map<text, frozen<schema_label_type>>
);

CREATE INDEX account_status
	ON test.account ( status );
	
CREATE INDEX login_id
	ON test.login ( id );
	
CREATE TABLE test.stock ( 
	store text,
	product text,
	quantity counter,
	PRIMARY KEY ((store, product))
);