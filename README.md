# Public-Api

[![CircleCI](https://circleci.com/gh/whalebone/public-api.svg?style=svg)](https://circleci.com/gh/whalebone/public-api)

### env vars

- ELASTIC_HOST=http://localhost:9200

**OR**

- ELASTIC_HOST_1=http://node1:9200
- ELASTIC_HOST_2=http://node2:9200
- ELASTIC_HOST_3=http://node3:9200

...

- JWT_SECRET=test
- AES_KEY=testtesttesttest
- AES_IV=1234567890123456

### usage

see https://app.swaggerhub.com/apis/whalebone/whalebone-api/1-oas3/

http port: 8080

### testing

mvn integration-test -Parq-wildfly-managed
assumes elastic running on ELASTIC_HOST