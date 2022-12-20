# spring-boot-localstack

A sample app to show how to use localstack for local developing. It receives messages coming from an incoming queue, updates their internal state, and forwards them to an outgoing queue.

You should have Docker, Docker Compose and awscli installed and working for the app to work properly.

## How to test:

Just run `mvn test -Dspring.profiles.actve=test`.

## How to run:

- Start localstack locally: `docker-compose up`. This will start localstack at `http://localhost:4576` with the fake credentials provided in the config files.
- Start the app: `mvn spring-boot:run`. This will create 2 queues: `incoming-queue` and `outgoing-queue`.
- Send a message to `incoming-queue`:
```
aws --endpoint-url=http://localhost:4576 sqs send-message --queue-url http://localhost:4576/queue/incoming-queue --message-body "{\"data\": {\"nane\": \"foo\"}}"
```
- The app should be generating some logs like the following:
```
  2022-12-19 18:45:42.231  INFO 68793 --- [enerContainer-2] c.s.localstacksqs.localsqs.Controller    : Received message on incoming queue: SampleEvent(eventId=null, version=null, type=null, eventTime=null, data=EventData(name=foo, age=0, description=null, eventType=null))
  2022-12-19 18:45:42.279  INFO 68793 --- [enerContainer-2] c.s.localstacksqs.localsqs.Controller    : Forwarded message SampleEvent(eventId=null, version=null, type=null, eventTime=2022-12-19T18:45:42.256117-03:00[America/Sao_Paulo], data=EventData(name=foo, age=0, description=null, eventType=PROCESSED)) to outgoing queue
```

## References:
- https://github.com/spring-attic/spring-cloud-aws/issues/504
- https://stackoverflow.com/questions/45818092/spring-boot-startup-error-for-aws-application-there-is-not-ec2-meta-data-avail
- https://advancedweb.hu/how-to-use-the-aws-sqs-cli-to-receive-messages/
- https://lobster1234.github.io/2017/04/05/working-with-localstack-command-line/
- https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/examples-sqs-messages.html
- https://sg.wantedly.com/companies/bebit/post_articles/317492
