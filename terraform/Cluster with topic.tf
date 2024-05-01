# Kafka Cluster with topic
provider "confluent" {
  kafka_id            = var.kafka_id                   
  kafka_rest_endpoint = var.kafka_rest_endpoint        
  kafka_api_key       = var.kafka_api_key              
  kafka_api_secret    = var.kafka_api_secret           
}

resource "confluent_kafka_topic" "clickstream" {
  topic_name         = "clickstream_topic"

  lifecycle {
    prevent_destroy = true
  }
}

