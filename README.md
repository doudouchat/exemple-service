[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=doudouchat_exemple-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=doudouchat_exemple-service)
[![build](https://github.com/doudouchat/exemple-service/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-service/actions)
[![codecov](https://codecov.io/gh/doudouchat/exemple-service/graph/badge.svg)](https://codecov.io/gh/doudouchat/exemple-service) 

# exemple-service

## maven

<p>execute with docker and cassandra <code>mvn clean verify -Pservice,it</code></p>

## Docker

<p>build image <code>docker build -t exemple-service --build-arg VERSION_TOMCAT=@Tag .</code></p>

<p>exemple build image <code>docker build -t exemple-service --build-arg VERSION_TOMCAT=10.1.25-jdk21 .</code>
