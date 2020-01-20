[![Build Status](https://travis-ci.com/doudouchat/exemple-service.svg?branch=master)](https://travis-ci.org/doudouchat/exemple-service) 

# exemple-service

## maven

<p>execute with cargo and cassandra <code>mvn clean verify -Pservice,it</code></p>

<p>execute without cargo and cassandra <code>mvn clean verify -Pit -Dapplication.port=8080</code></p>

## Docker

<ol>
<li>docker build -t exemple-service .</li>
</ol>

<ol>
<li>docker-compose up -d service</li>
</ol>
