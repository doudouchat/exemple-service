[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7211d23c538f4b389e447bd02dd8c8a4)](https://app.codacy.com/gh/doudouchat/exemple-service?utm_source=github.com&utm_medium=referral&utm_content=doudouchat/exemple-service&utm_campaign=Badge_Grade)
[![build](https://github.com/doudouchat/exemple-service/workflows/build/badge.svg)](https://github.com/doudouchat/exemple-service/actions)
[![codecov](https://codecov.io/gh/doudouchat/exemple-service/graph/badge.svg)](https://codecov.io/gh/doudouchat/exemple-service) 

# exemple-service

## maven

<p>execute with docker and cassandra <code>mvn clean verify -Pservice,it</code></p>

## Docker

<p>build image <code>docker build -t exemple-service --build-arg VERSION_TOMCAT=@Tag .</code></p>

<p>exemple build image <code>docker build -t exemple-service --build-arg VERSION_TOMCAT=9.0.60-jdk8-openjdk .</code>
