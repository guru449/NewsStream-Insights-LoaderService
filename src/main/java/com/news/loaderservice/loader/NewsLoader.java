package com.news.loaderservice.loader;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;

import java.util.Collections;
import java.util.Properties;

@SpringBootApplication
public class NewsLoader {

    @Value("${app.kafka.processed-topic}")
    private String processedTopic;

    @Value("${app.kafka.broker}")
    private String kafkaBroker;

    @Value("${spring.data.mongodb.database}")
    private String mongoDatabase;

    @Value("${spring.data.mongodb.collection}")
    private String mongoCollection;

    public static void main(String[] args) {
        SpringApplication.run(NewsLoader.class, args);
    }

    @Bean
    public CommandLineRunner run(KafkaConsumer<String, String> consumer, MongoClient mongoClient) {
        return args -> {
            consumer.subscribe(Collections.singletonList(processedTopic));
            loadNews(consumer, mongoClient);
        };
    }

    public void loadNews(KafkaConsumer<String, String> consumer, MongoClient mongoClient) {
        MongoDatabase database = mongoClient.getDatabase(mongoDatabase);
        MongoCollection<Document> collection = database.getCollection(mongoCollection);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                Document doc = Document.parse(record.value());
                collection.insertOne(doc);
            }
        }
    }

    @Bean
    public KafkaConsumer<String, String> kafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafkaBroker);
        props.put("group.id", "loader-service-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        return new KafkaConsumer<>(props);
    }

    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "loader-service");
    }
}
