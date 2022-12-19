package com.sanket.localstacksqs.localsqs.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.config.QueueMessageHandlerFactory;
import org.springframework.cloud.aws.messaging.config.SimpleMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.PayloadArgumentResolver;

import static java.util.Collections.singletonList;

@Configuration
public class AWSConfiguration {

  @Value("${cloud.aws.region.static}")
  private String region;

  @Value("${cloud.aws.credentials.access-key}")
  private String awsAccesskey;

  @Value("${cloud.aws.credentials.secret-key}")
  private String awsSecretKey;

  @Value("${cloud.aws.sqs.polling-timeout}")
  private Integer pollingTimeout;

  @Bean
  public QueueMessageHandlerFactory queueMessageHandlerFactory(MessageConverter messageConverter) {

    var factory = new QueueMessageHandlerFactory();
    factory.setArgumentResolvers(singletonList(new PayloadArgumentResolver(messageConverter)));
    return factory;
  }

  @Bean
  protected MessageConverter messageConverter(ObjectMapper objectMapper) {

    var converter = new MappingJackson2MessageConverter();
    converter.setObjectMapper(objectMapper);
    // Serialization support:
    converter.setSerializedPayloadClass(String.class);
    // Deserialization support: (suppress "contentType=application/json" header requirement)
    converter.setStrictContentTypeMatch(false);
    return converter;
  }

  @Bean
  public AwsClientBuilder.EndpointConfiguration endpointConfiguration(){
    return new AwsClientBuilder.EndpointConfiguration("http://localhost:4576", region);
  }

  @Bean
  @Primary
  public AmazonSQSAsync amazonSQSAsync(final AwsClientBuilder.EndpointConfiguration endpointConfiguration){
    AmazonSQSAsync amazonSQSAsync = AmazonSQSAsyncClientBuilder
        .standard()
        .withEndpointConfiguration(endpointConfiguration)
        .withCredentials(new AWSStaticCredentialsProvider(
            new BasicAWSCredentials(awsAccesskey, awsSecretKey)
        ))
        .build();
    createQueues(amazonSQSAsync, "incoming-queue");
    createQueues(amazonSQSAsync, "outgoing-queue");
    return amazonSQSAsync;
  }

  private void createQueues(final AmazonSQSAsync amazonSQSAsync,
                            final String queueName){
    amazonSQSAsync.createQueue(queueName);
    var queueUrl = amazonSQSAsync.getQueueUrl(queueName).getQueueUrl();
    amazonSQSAsync.purgeQueueAsync(new PurgeQueueRequest(queueUrl));
  }

  //This is to make tests run faster, as explained here: https://github.com/spring-attic/spring-cloud-aws/issues/504
  @Bean
  public SimpleMessageListenerContainerFactory simpleMessageListenerContainerFactory(AmazonSQSAsync amazonSQSAsync) {
    SimpleMessageListenerContainerFactory factory = new SimpleMessageListenerContainerFactory();
    factory.setAmazonSqs(amazonSQSAsync);
    factory.setWaitTimeOut(pollingTimeout); // less than 10 sec when testing
    return factory;
  }
}
