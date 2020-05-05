USE test;

INSERT INTO parameter (app, histories) VALUES (
	'default',  
	{ 	'lastname' : false,
		'firstname' : false,
  		'address' : false,
  		'addresses' : true,
  		'cgus' : true,
  		'phones' : true }
);

INSERT INTO login (username) VALUES (
	'jean.dupond@gmail.com'
);
