SELECT * FROM test.account;

INSERT INTO test.login (username, id) VALUES ('jean.dupond@gmail.com', e2b0fdc7-bd69-410d-b684-207c8cbf7598);

INSERT INTO test.resource_schema (app, version, resource, profile, content) VALUES (
	'app1',
	'v1',
	'account',
	'example',
	'{
		"$schema": "http://json-schema.org/draft-07/schema",
		"properties": {
			"id": {
				"type": "number",
				"readOnly": true
			}
		}
	}'
);
