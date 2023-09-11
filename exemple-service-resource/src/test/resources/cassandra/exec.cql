SELECT * FROM test.account;

INSERT INTO test.resource_schema (app, version, resource, profile, content) VALUES (
	'app1',
	'v1',
	'account',
	'example',
	'{
		"$schema": "https://json-schema.org/draft/2020-12/schema",
		"properties": {
			"id": {
				"type": "number",
				"readOnly": true
			}
		}
	}'
);
