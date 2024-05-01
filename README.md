# StreamingPOC
## Problem Statement: 
Your company is experiencing rapid growth in user activity, generating a constant stream of data from various sources. This includes website clickstream data, user interactions with a mobile app, and real-time sensor readings from your infrastructure.
## Overview
### Architecture
Below is the architecture diagram
![image](https://github.com/mummidishiva/StreamingPOC/assets/29959154/157305cb-62c2-4b15-a83b-c3191733dc71)
### Summary
Assuming that we will have different sources generating events/messages, in this case, we have a mobile app, real-time sensor readings, click stream from the user applications, and some other sources. Each of these sends the events to each Kafka topic as shown below. 
![image](https://github.com/mummidishiva/StreamingPOC/assets/29959154/dbea559c-77ab-4bf3-86ce-0364e6c5aab3)
### Producing events using Datagen
For our use case, assuming the data is generated from the confluent Datagen, this can be configured by installing the connector from the confluent hub into a docker image. Settings should be defined in the docker.yml file and then run the docker-compose.  For our example below are the few parameters defined
Topic name: clickstream
port: http://localhost:8083 (default kafka connector)
Define the JSON as below for each event and save it as clickstream-config.json
  ```
{
    "name": "datagen-clickstream",
    "config": {
      "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
      "kafka.topic": "clickstream_topic",
      "quickstart": "clickstream",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false",
      "max.interval": 1000,
      "iterations": 1000,
      "tasks.max": "1"
    }
  }

```
NOTE: use schema-string to specify the schema setting

Submit the above format as JSON payload to Kafka Connect
```
http POST http://localhost:8083/connectors @clickstream-config.json
```
Docker Compose
```
docker exec -it broker kafka-console-consumer --bootstrap-server localhost:9092 --topic clickstream_json --property print.key=true -- from-beginning
```
The above will show the data generated from the Datagen connector, follow the same process for other event sources.

### Kafka Stream
Now that we have events being generated from the various sources into the Kafka topic, we now connect the Kafka Stream to the topics as shown below
![image](https://github.com/mummidishiva/StreamingPOC/assets/29959154/e03d094c-ab4c-4696-a098-ffcbbd3b7b5a)
Processing the events using the low-level Kafka clients needs more code to write and needs more maintenance. Using Kafka streams, it is very easy to process the events. 

### Confluent Cloud
We need to set the Confluent Cloud, using the Confluent Cloud account, then go to Add Cloud Environment, create a new Cluster, set the default Schema Registry, then choose the language (java, python, etc.)
Now define the streambuilder class and retrieve the input topic as shown below
```
StreamsBuilder builder = new StreamsBuilder();  
final String clickStreamTopic = streamsProps.getProperty("basic.input.topic");

```
Reading the topic into the KStream and displaying on the console
```
final String clickStreamStart = "ClickStreamID-";
//using serdes to serialize and deserialize as events are stored in bytes in Kafka topics
KStream<String, String> clickStream = builder.stream(clickStreamTopic, Consumed.with(Serdes.String(), Serdes.String()))

// Display the records on the console
clickStream.peek((key, value) -> System.out.println("Incoming record - key " +key +" value " + value))

```
Here we can apply any transformations based on the project needs on the events like filtering, Joins, mapping, Windowing, etc., but to do those we need to use the KTable.
```
 StreamsBuilder builder = new StreamsBuilder();
 KTable<String, String> clickStreamKTable = builder.table(clickStreamTopic, Materialized.with(Serdes.String(), Serdes.String()));

```
### Connector to Push the data into Data Lake Storage
We can have different consumers subscribing to the data from Kafka Stream and processing the data as per the business requirements. Once the data is subscribed the data can be pushed into the Data Store using the various connectors available, in this is case I am using Azure Data Lake Storage Gen2 and the Confluent Kafka Connector – Azure Data Lake Storage Gen2 Sink Connector as shown below

![image](https://github.com/mummidishiva/StreamingPOC/assets/29959154/9b95968f-c144-4353-ab4e-157d30aeffae)

Azure Data Lake Storage Gen2 sink Connector periodically polls the data from the Kafka and uploads it into the Azure Data Lake Storage. This connector should be installed on the machine from where we want to consume the data from Kafka, below is the sample configuration, also note that the Azure CLI needs to be installed on the machine.
```
{
  "name": "adls-gen2-sink",
  "config": {
    "name":"adls-gen2-sink",
    "connector.class":"io.confluent.connect.azure.datalake.gen2.AzureDataLakeGen2SinkConnector",
    "tasks.max":"1",
    "topics":"clickstream_topic",
    "flush.size":"3",
    "azure.datalake.gen2.client.id":"client ID",
    "azure.datalake.gen2.client.key":"Client Key",
    "azure.datalake.gen2.account.name":"account Name",
    "azure.datalake.gen2.token.endpoint":"https://login.microsoftonline.com/<tenant-id>/oauth2/token",
    "format.class":"io.confluent.connect.azure.storage.format.avro.AvroFormat",
    "confluent.topic.bootstrap.servers":"localhost:9092",
    "confluent.topic.replication.factor":"1"
  },
  "tasks": []

```
Client ID, Client Key, and Account Name can be obtained from the Azure portal with admin access

### Scalability and Performance
Below are some of the techniques we can use to increase the processing/ performance of the Kafka Streams
* Kafka Stream processes the stream event based on available threads, if the stream events are increasing periodically, we can create multiple instances of the Kafka Stream and process the events.
* Confluent Kafka provides self-managed cloud solutions on Kubernetes, where we can achieve elasticity
* Proper partitions of Kafka topics helps in attaining the proper parallelism.•	Proper partitions of Kafka topics helps in attaining the proper parallelism.
* It is not suggested to perform the transformation logic, joins on the KTable in Kafka Stream as it may add latency in performing the transformation logic.

### Terraform pseudo code
attached in the folder



