package com.sprint.redisjava.notification;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationSubscriber implements MessageListener {

    @Override
    public void onMessage(
            Message message,
            byte[] pattern
    ){
        String channel = new String(
                message.getChannel()
        );

        String body = new String(
                message.getBody()
        );

        System.out.println(
                "Received on " +
                        channel +
                        ": " +
                        body
        );
    }
}
