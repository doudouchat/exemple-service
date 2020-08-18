SELECT * FROM test.account;

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
