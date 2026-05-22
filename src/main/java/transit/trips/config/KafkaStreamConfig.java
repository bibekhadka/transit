package transit.trips.config;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Named;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import transit.trips.core.model.Tap;
import transit.trips.core.model.Trip;
import transit.trips.core.service.TripStateMachine;
import transit.trips.core.service.fare.FarePolicy;
import transit.trips.core.stream.TripStreamProcessor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamConfig {

	private final TripStateMachine stateMachine;
	private final FarePolicy farePolicy;

	@Autowired
	public KafkaStreamConfig(TripStateMachine stateMachine, FarePolicy farePolicy) {
		this.stateMachine = stateMachine;
		this.farePolicy = farePolicy;
	}

	@Value(value = "${spring.kafka.bootstrap-servers}")
	private String bootstrapServers;

	@Value(value = "${spring.kafka.streams.application-id}")
	private String applicationId;

	@Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
	KafkaStreamsConfiguration kStreamsConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		return new KafkaStreamsConfiguration(props);
	}

	@Bean
	public KStream<String, Tap> process(StreamsBuilder builder, StoreBuilder<KeyValueStore<String, Trip>> tripStore) {
		builder.addStateStore(tripStore);
		KStream<String, Tap> taps = builder.stream("taps");
		taps.process(() -> new TripStreamProcessor(stateMachine, farePolicy), Named.as("trip-processor"), "trip-store");
		return taps;
	}

	@Bean
	public Serde<Trip> tripStateSerde() {
		JacksonJsonSerializer<Trip> serializer = new JacksonJsonSerializer<>();
		JacksonJsonDeserializer<Trip> deserializer = new JacksonJsonDeserializer<>(Trip.class);
		return Serdes.serdeFrom(serializer, deserializer);
	}

	@Bean
	public StoreBuilder<KeyValueStore<String, Trip>> tripStore(Serde<Trip> tripStateSerde) {
		return Stores.keyValueStoreBuilder(Stores.persistentKeyValueStore("trip-store"), Serdes.String(),
				tripStateSerde);
	}
}
