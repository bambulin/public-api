# Public-Api

[![CircleCI](https://circleci.com/gh/whalebone/public-api.svg?style=svg)](https://circleci.com/gh/whalebone/public-api)

### Environment variables:

**ELASTIC_HOST**=http://localhost:9200  
URL of ElasticSearch rest api. If there's more than one node you can define multiple URLs using **ELASTIC_HOST_1**, **ELASTIC_HOST_2**, etc.  

**ELASTIC_QUERY_MAX_SIZE**=10000  
Max number of records that can be returned from single search.
If not defined then 10000 is used by default.  

**JWT_SECRET**=test  
Secret key used in JWT tokens

**AES_KEY**=testtesttesttest  
128 bits AES key for client id decryption  

**AES_IV**=1234567890123456  
128 bits AES initial vector for client id decryption

### usage

see https://app.swaggerhub.com/apis/whalebone/whalebone-api/1-oas3/

http port: 8080

### testing

mvn integration-test -Parq-wildfly-managed
assumes elastic running on ELASTIC_HOST