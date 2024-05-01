import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced

public class POCStream {

    public static void main(String[] args) throws IOException {
        Properties streamsProps = new Properties();
        try (FileInputStream fis = new FileInputStream("src/main/resources/streams.properties")) {
            streamsProps.load(fis);
        }
        streamsProps.put(StreamsConfig.APPLICATION_ID_CONFIG, "basic-streams");

        StreamsBuilder builder = new StreamsBuilder();  
        final String clickStreamTopic = streamsProps.getProperty("basic.input.topic");

        final String clickStreamStart = "ClickStreamID-";
        //using serdes to serialize and deserialize as events are stored in bytes in Kafka topics
        KStream<String, String> clickStream = builder.stream(clickStreamTopic, Consumed.with(Serdes.String(), Serdes.String()))

        // Display the records on the console
        clickStream.peek((key, value) -> System.out.println("Incoming record - key " +key +" value " + value))

        // storing in Ktable
        KTable<String, String> clickStreamKTable = builder.table(clickStreamTopic, Materialized.with(Serdes.String(), Serdes.String()));

        System.exit(0);

    }

}