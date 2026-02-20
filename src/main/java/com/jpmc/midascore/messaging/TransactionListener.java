package com.jpmc.midascore.messaging;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    @KafkaListener(topics = "${general.kafka-topic:trader-updates}")
    public void onMessage(Transaction transaction) {
    }

}
