[![Build Status](https://travis-ci.com/doudouchat/exemple-service.svg?branch=master)](https://travis-ci.org/doudouchat/exemple-service) 

# exemple-service

## maven

<p>execute with cargo and cassandra <code>mvn clean verify -Pservice,it</code></p>

<p>execute without cargo and cassandra <code>mvn clean verify -Pit -Dapplication.port=8080</code></p>

## Docker

<ol>
<li>docker build -t exemple-service-api exemple-service-api</li>
<li>docker build -t exemple-service-db exemple-service-api-integration</li>
</ol>

<ol>
<li>docker-compose up -d zookeeper</li>
<li>docker-compose up -d cassandra</li>
<li>docker container exec exemple-service-db cqlsh --debug -f /usr/local/tmp/cassandra/schema.cql</li>
<li>docker container exec exemple-service-db cqlsh --debug -f /usr/local/tmp/cassandra/exec.cql</li>
<li>docker-compose up -d api</li>
</ol>

docker-compose exec api cat logs/localhost.2018-08-24.log

## Certificate

keytool -genkeypair -alias mytest -keyalg RSA -keypass mypass -keystore mytest.jks -storepass mypass
