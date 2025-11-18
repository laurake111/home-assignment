package ee.tuum.assignment.integration;

import ee.tuum.assignment.dto.event.AccountCreationActionEvent;
import ee.tuum.assignment.rabbitConfig.RabbitConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.rabbitmq.listener.direct.auto-startup=false")
@AutoConfigureMockMvc
public class RabbitMQTest {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	RabbitAdmin rabbitAdmin;

	@Autowired
	private SimpleMessageListenerContainer listener;

	@BeforeEach
	public void setup() {
		listener.stop();
	}

	// todo this test only works if i have no other consumer listening to the queue
	@Test
	void messageArrivesInAccountEventsQueue() {
		System.out.println("Queue props: " +
				rabbitAdmin.getQueueProperties(RabbitConfig.ACCOUNT_EVENTS_QUEUE));

		rabbitAdmin.purgeQueue(RabbitConfig.ACCOUNT_EVENTS_QUEUE, true);

		AccountCreationActionEvent event = new AccountCreationActionEvent(
				123L,
				"EE"
		);

		rabbitTemplate.convertAndSend(
				RabbitConfig.ASSIGNMENT_EXCHANGE,
				RabbitConfig.ACCOUNT_CREATED_KEY,
				event
		);

		Message message = rabbitTemplate.receive(RabbitConfig.ACCOUNT_EVENTS_QUEUE, 5000);
		assertThat(message).isNotNull();
	}
}
