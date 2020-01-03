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

INSERT INTO login (login) VALUES (
	'jean.dupond@gmail.com'
);
