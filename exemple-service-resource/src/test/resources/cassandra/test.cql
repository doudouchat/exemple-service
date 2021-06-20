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
	birthday date,
	address address,	
	addresses map<text, frozen<address>>,
	children map<int, frozen<child>>,
	phones map<text, text>,
	notes map<timestamp, text>,
	profils set<text>,
	cgus set<frozen<cgu>>,
	age int,
	enabled boolean,
	creation_date timestamp,
	preferences list<frozen<tuple<text, text, int, timestamp>>>
);

CREATE TABLE test.login (
	id UUID,
	username text PRIMARY KEY
);

CREATE TABLE test.subscription (
	email text PRIMARY KEY
);

CREATE TABLE test.account_history ( 
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

CREATE TABLE test.account_event ( 
	id UUID,
	date timestamp,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((id), date)
);

CREATE TABLE test.subscription_event ( 
	email text,
	date timestamp,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((email), date)
);

CREATE TABLE test.resource_schema ( 
	app text,
	version text,
	resource text,
	profile text,
	content text,
	filter set<text>,
	field set<text>,
	patch set<text>,
	PRIMARY KEY ((app), resource, version, profile)
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

CREATE TABLE test.exemple ( 
	id UUID PRIMARY KEY,
	email text, 
	birthday date,
	address address,	
	addresses map<text, frozen<address>>,
	children map<int, frozen<child>>,
	phones map<text, text>,
	notes map<timestamp, text>,
	profils set<text>,
	cgus set<frozen<cgu>>,
	age int,
	enabled boolean,
	creation_date timestamp,
	preferences list<frozen<tuple<text, text, int, timestamp>>>,
	phones2 map<text, text>,
	content blob
);