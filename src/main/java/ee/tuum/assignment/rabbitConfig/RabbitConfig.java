package ee.tuum.assignment.rabbitConfig;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitConfig {
	public static final String ACCOUNT_EVENTS_QUEUE = "account-events";
	public static final String ASSIGNMENT_EXCHANGE = "spring-assignment-exchange";

	public static final String ACCOUNT_CREATED_KEY = "account.created";
	public static final String BALANCE_CREATED_KEY = "balance.created";
	public static final String BALANCE_UPDATED_KEY = "balance.updated";
	public static final String TRANSACTION_CREATED_KEY = "transaction.created";

	@Bean
	public Queue accountEventsQueue() {
		return new Queue(ACCOUNT_EVENTS_QUEUE, true);
	}

	@Bean
	public TopicExchange exchange() {
		return new TopicExchange(ASSIGNMENT_EXCHANGE);
	}

	@Bean
	public Binding binding(Queue accountEventsQueue, TopicExchange exchange) {
		// "#" catches everything from this exchange
		return BindingBuilder.bind(accountEventsQueue)
				.to(exchange)
				.with("#");
	}


	@Bean
	MessageListenerAdapter listenerAdapter(Receiver receiver, MessageConverter messageConverter) {
		MessageListenerAdapter adapter = new MessageListenerAdapter(receiver, "receiveMessage");
		adapter.setMessageConverter(messageConverter);
		return adapter;
	}

	@Bean
	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
											 MessageListenerAdapter listenerAdapter) {
		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
		container.setConnectionFactory(connectionFactory);
		container.setQueueNames(ACCOUNT_EVENTS_QUEUE);
		container.setMessageListener(listenerAdapter);
		return container;
	}

	@Bean
	public MessageConverter messageConverter() {
		return new Jackson2JsonMessageConverter();
	}

	@Bean
	@Primary
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
		RabbitTemplate template = new RabbitTemplate(connectionFactory);
		template.setMessageConverter(messageConverter);
		return template;
	}
}
