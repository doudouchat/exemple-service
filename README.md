[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7211d23c538f4b389e447bd02dd8c8a4)](https://app.codacy.com/gh/doudouchat/exemple-service?utm_source=github.com&utm_medium=referral&utm_content=doudouchat/exemple-service&utm_campaign=Badge_Grade)
![build](https://github.com/doudouchat/exemple-service/workflows/build/badge.svg)
[![codecov](https://codecov.io/gh/doudouchat/exemple-service/graph/badge.svg)](https://codecov.io/gh/doudouchat/exemple-service) 

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
