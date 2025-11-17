package ee.tuum.assignment.rabbitConfig;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
@Log4j2
public class Receiver {

	private final CountDownLatch latch = new CountDownLatch(1);

	public void receiveMessage(Object event) {
		log.info("Received event: {}", event);
		latch.countDown();
	}

	public CountDownLatch getLatch() {
		return latch;
	}
}
