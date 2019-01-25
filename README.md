# Public-Api

[![CircleCI](https://circleci.com/gh/whalebone/public-api.svg?style=svg)](https://circleci.com/gh/whalebone/public-api)

### env vars

- ELASTIC_HOST=localhost
- ELASTIC_PORT=9300
- ELASTIC_REST_PORT=9200  - for testing only
- ELASTIC_CLUSTER=archive
- JWT_SECRET=test
- AES_KEY=testtesttesttest
- AES_IV=1234567890123456

### usage

see https://app.swaggerhub.com/apis/whalebone/whalebone-api/1

### testing

mvn integration-test -Parq-wildfly-managed
assumes elastic running on ELASTIC_HOST, ELASTIC_PORT, ELASTIC_REST_PORT
