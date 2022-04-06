# Serverless Compute - Lambda

## Event Sources

- Event Source is an AWS service that produces the event that your Lambda function responses to by invoking it.

### Push-based Event Sources

- AWS Services other than Poll-based serivces.

- Serivces using this model publish events in addition to actually invoking a Lambda function.

### Poll-based Event Sources

- Amazon Kinesis, Amazon DynamoDB, Amazon SQS

- Lambda polls the service looking for particular events and invokes the associated function when a matching event is found.

---

## Event Source Mapping

- Event source mapping is the configuration that links your event source to your Lambda function.  
  It's what links the events generated from your event source to invoke your Lamba function.

### Push-based services

- The mapping is maintained within the Event Source.
- By using the appropriate API calls for the event source service, you are able to create and configure the relevant mappings.

> ex) Using the API for S3 Bucket notifications, you can specify which events to publish within that bucket.

- This requires specific access to allow your event source to invoke the Lambda function.

- Permissions are kept in Function Policy.

### Poll-based services

- The configurations of the mappings are held within your Lambda function.
- With the `CreateEventSourceMapping` API, you can set up the relevant event source mapping for your poll-based service.
- The permissions are required in the Execution Role Policy.

---

## Synchronous VS Asynchronous invocation

- When you manually invoke a Lambda function or when your custom built application invokes it,  
  you have the ability to use the _'invoke'_ option which allows you to specify if the function  
  should be invoked synchronously or asynchronously.

- When a function is invoked synchronously, it enables you to assess the result before moving onto the next operation required.
- If you want to control the flow of your functions, then synchronous invocations can help you maintain an order.
- Asynchronous invocations can be used when there is no need to maintain an order of function execution.

- When Event Sources are used to call and invoke your Lambda function, the invocation type is dependent on the service.

  - For poll-based services, the invocation type is always synchronous.
  - For push-based services, it varies on the service.

---

## Others

### Function Policy

- Function Policy defines which AWS resources are allowed to invoke your function.

### Role Execution Policy

- Role Execution Policy determines what resources the function role has access to when the function is being run.

### Dead Letter Queue(DLQ)

- Dead Letter Queue is used to receive payloads that were not processed due to a failed execution

- Filed asynchronous functions would automatically retry the event a further two more times.
- Synchronous invocations do not automatically retry failed attempts.

---
