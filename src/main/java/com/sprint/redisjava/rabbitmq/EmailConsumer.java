package com.sprint.redisjava.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {
    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    public void processEmail(
            EmailProducerController.EmailJob job
    ) throws InterruptedException {
        System.out.println(
                "Processing email job: " + job
        );

        Thread.sleep(1500);

        System.out.println(
                "Email job completed: " + job
        );
    }
}