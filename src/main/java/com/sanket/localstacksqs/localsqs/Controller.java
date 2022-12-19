package com.sanket.localstacksqs.localsqs;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanket.localstacksqs.localsqs.model.EventData;
import com.sanket.localstacksqs.localsqs.model.SampleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;


@Component
@Slf4j
@RequiredArgsConstructor
public class Controller {

  private final AmazonSQSAsync amazonSQSAsync;

  @Value("${cloud.aws.sqs.outgoing-queue.url}")
  private String outgoingQueueUrl;

  private final ObjectMapper mapper;

  @SqsListener(value = "${cloud.aws.sqs.incoming-queue.url}", deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
  private void consumeFromSQS(SampleEvent sampleEvent) throws JsonProcessingException {
    log.info("Received message on incoming queue: {}", sampleEvent);
    processEvent(sampleEvent);
    amazonSQSAsync.sendMessage(outgoingQueueUrl, mapper.writeValueAsString(sampleEvent));
    log.info("Forwarded message {} to outgoing queue", sampleEvent);
  }

  private void processEvent(SampleEvent event) {
    event.setEventTime(ZonedDateTime.now());
    if (event.getData() != null) {
      event.getData().setEventType(EventData.EventType.PROCESSED);
    }
  }
}
