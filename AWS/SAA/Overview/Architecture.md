# Architecture Overview

## Design Patterns

- Multi-Layer Architecture

  - Benefits: Can decouple layers so they can be independently scaled to meet damend.

- Serverless Design Patterns

  - API Gateway, AWS Lambda

- Microservice Design Patterns

- Design a high-performance service that can record a lot of transactions and events as fast as possible

  - Answer: Multi-Layer Architecture

- Burst unpredictable traffic where you will also need to be able to loop up transactions or events

  - Answer: Consider using DynamoDB with GSIs. + Auto-scaling on the table.

- Design a high-performance machine learning solution

  - Amazon FSx for Lustre - Provides the best performance for an internal high-performance file share.

- Systems needs to communicate on UDP

  - Use a NLB(Layer 4 - Transport)
  - ALB has more features and works on Layer 7 - Application.

---

## Re-Architecting Scenarios

- Credentials stored in code

  - Use AWS Secrets Manager
    - Lambda function or similar is needed to retrieve credentials from Secrets Manager.
    - That function will need to run using an IAM Role.

> KMS is a service designed for storing encryption keys.
