USE test;

INSERT INTO account (id, lastname, firstname) VALUES (6ab09bec-e68e-48d9-a5f8-97e6fb4c9b47, 'KRUIKSWIJK','Steven') USING TTL 86400 AND TIMESTAMP 123456789;

SELECT * FROM account;

INSERT INTO parameter (app, histories) VALUES (
	'default',  
	{ 	'lastname' : true,
  		'address' : false,
  		'cgus' : true,
  		'profils' : true,
  		'addresses' : true,
  		'phones' : true,
  		'phones2' : true,
  		'cgus2' : true 
  	}
);

INSERT INTO resource_schema (app, version, resource, content) VALUES (
	'app1',
	'v1',
	'account',textAsBlob(
	'{
		"$schema": "http://json-schema.org/draft-07/schema",
		"properties": {
			"id": {
				"type": "number",
				"readOnly": true
			}
		}
	}')
);
