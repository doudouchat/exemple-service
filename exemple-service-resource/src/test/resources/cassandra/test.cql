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
	loyalty_card text,
	enabled boolean,
	creation_date timestamp,
	preferences list<frozen<tuple<text, text, int, timestamp>>>
);

CREATE TABLE test.subscription (
	email text PRIMARY KEY,
	update_date timestamp
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
	local_date date,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((id), date)
);

CREATE TABLE test.subscription_event ( 
	email text,
	date timestamp,
	local_date date,
	data text,
	event_type text,
	application text,
	version text,
	PRIMARY KEY ((email), date)
);

CREATE TABLE test.subscription_history ( 
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

CREATE TABLE test.resource_schema ( 
	version text,
	resource text,
	profile text,
	content text,
	patch set<text>,
	PRIMARY KEY ((resource), version, profile)
);
CREATE INDEX account_email
	ON test.account ( email );

CREATE INDEX account_loyalty_card
	ON test.account ( loyalty_card );

CREATE INDEX account_event_local_date
	ON test.account_event ( local_date );
	
CREATE INDEX subscription_event_local_date
	ON test.subscription_event ( local_date );

CREATE TABLE test.stock ( 
	store text,
	product text,
	quantity counter,
	PRIMARY KEY ((store), product)
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