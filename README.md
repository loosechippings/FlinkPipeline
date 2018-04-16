# Flink Pipeline

This project requires:
* A Kafka broker
* Schema Registry
* Kafka Connect
    
All dependencies can be started easily with [Confluent docker images](https://github.com/confluentinc/cp-docker-images) and the following docker-compose.yml:

```yml
version: '2'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    network_mode: "host"
    environment:
      ZOOKEEPER_CLIENT_PORT: 32181
      ZOOKEEPER_TICK_TIME: 2000
    extra_hosts:
      - "moby:127.0.0.1"

  kafka:
    image: confluentinc/cp-enterprise-kafka:latest
    network_mode: "host"
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: localhost:32181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:29092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    extra_hosts:
      - "moby:127.0.0.1"

  schema_registry:
    image: confluentinc/cp-schema-registry
    network_mode: "host"
    depends_on:
      - zookeeper
      - kafka
    ports:
      - "8081:8081"
    environment:
      SCHEMA_REGISTRY_HOST_NAME: lappy
      SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL: 'localhost:32181'

  connect:
    image: confluentinc/cp-kafka-connect
    network_mode: "host"
    depends_on:
      - zookeeper
      - kafka
      - schema_registry
    ports:
      - "8083:8083"
    environment:
      CONNECT_BOOTSTRAP_SERVERS: 'localhost:29092'
      CONNECT_REST_ADVERTISED_HOST_NAME: localhost
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: compose-connect-group
      CONNECT_CONFIG_STORAGE_TOPIC: docker-connect-configs
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_OFFSET_FLUSH_INTERVAL_MS: 10000
      CONNECT_OFFSET_STORAGE_TOPIC: docker-connect-offsets
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_STATUS_STORAGE_TOPIC: docker-connect-status
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: 1
      CONNECT_KEY_CONVERTER: io.confluent.connect.avro.AvroConverter
      CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL: 'http://localhost:8081'
      CONNECT_VALUE_CONVERTER: io.confluent.connect.avro.AvroConverter
      CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL: 'http://localhost:8081'
      CONNECT_INTERNAL_KEY_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_INTERNAL_VALUE_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_ZOOKEEPER_CONNECT: 'localhost:2181'
``` 

**FixAvroProducerPipeline** writes naive fix messages to a Kafka topic using the *WrappedFixMessage* Avro schema and Schema Registry.

**FixConsumerPipeline** reads *WrappedFixMessage* Avro messages from a Kafka topic does a trivial transformation to the *FixMessage* topic and prints to stdout.

To consume the Avro encoded Kafka topic start a Kafka Connect File Stream Sink with the following command:

```bash
curl -X POST -H "Content-Type: application/json" --data '{"name": "local-file-sink", "config": {"connector.class":"FileStreamSinkConnector", "tasks.max":"1", "file":"test.sink.txt", "topics":"fix-messages" }}' http://localhost:8083/connectors
```
