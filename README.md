# Public-Api

### env vars

- ELASTIC_HOST=localhost
- ELASTIC_PORT=9300
- ELASTIC_REST_PORT=9200
- ELASTIC_CLUSTER=archive

### usage

see https://app.swaggerhub.com/apis/whalebone/whalebone-api/1

### testing

mvn integration-test -Parq-wildfly-managed
assumes elastic running on ELASTIC_HOST, ELASTIC_PORT, ELASTIC_REST_PORT
