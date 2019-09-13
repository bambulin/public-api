# Public-Api

[![CircleCI](https://circleci.com/gh/whalebone/public-api.svg?style=svg)](https://circleci.com/gh/whalebone/public-api)

### Environment variables:

**ELASTIC_HOST**=http://localhost:9200  
URL of ElasticSearch rest api. If there's more than one node you can define multiple URLs using **ELASTIC_HOST_1**, **ELASTIC_HOST_2**, etc.  

**IOC_ELASTIC_HOST**=host.to.ioc.elastic:9300** 
 host and the tcp transport port (NO rest port) of elastic with iocs. You can define multiple s as in case of ELASTIC_HOST env prop 

**ELASTIC_QUERY_MAX_SIZE**=10000  
Max number of records that can be returned from single search.
If not defined then 10000 is used by default.  

### usage

see https://app.swaggerhub.com/apis/whalebone/whalebone-api/1-oas3/

http port: 8080

### testing

mvn integration-test -Parq-wildfly-managed
for IT tests there must be IOC_ELASTIC_HOST_REST=http://host.to.ioc.elastic:9200 set so the test IoC data can be initialized