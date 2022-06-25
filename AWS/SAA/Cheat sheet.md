# Cheat sheet

## EFS(Elastic File System)

## AWS Backuo

## Reserved, spot, on-demand instances

## AWS File gateway, tape gateway, volume gateway, dataSync

- AWS Storage Gateway: A service that allows you to create a gateway between your on-premises storage systems and  
  Amazon's S3 and Glacier. There are three gateway services:

  - AWS File Gateway: File Gateway is a service used to extend your on-premises file storage to the AWS cloud  
    without needing to update existing applications that use these files. With file gateway, you can map on-premises  
    drives to S3 so that users and applications can access these files as they usually would. Once the files are in  
    S3, you can access them in the cloud from any applications deployed there or continue using these files from  
    on-premises. File Gateway provides access to files in S3 based SMB or NSF protocols.

  - AWS Volume Gateway: Volume Gateway provides primary storage or backup storage for your on-premises iSCSI  
    block storage devices. There are two configuration modes when you use volume gateway:

    - Cache mode: System keeps the primary copy of your data in S3, and a local on-premises cache provides low  
      latency access to the most frequently accessed data.
    - Stored mode: Primary copy of the data is on-premises, and the system periodically saves a backup to S3.

  - AWS Tape Gateway: Tape Gateway(Virtual Tape Library) allows you to back up your on-premises data to S3 from  
    your corporate data center. With Tape Gateway, you can leverage the Glacier storage classes for data archiving  
    at a lower cost than S3. The Tape Gateway is essentially a cloud-based tape backup solution that replaces  
    physical components with virtual ones.

- AWS DataSync: AWS DataSync is used to automatically transfer data from one storage location to another.  
  For example, it can be used to migrate data from on-premises to an AWS storage service. DataSync is not just  
  for migrating on-premises data: It can also move data from one AWS storage service to another from witin AWS.

## Route 53 active-active, active-passive failover

- Active-active failover: This failover configuration is used when you want all of your resources to be available  
  the majority of the time. When a resource becomes unavailable, Route53 can detect that it's unhealthy and stop  
  including it when responding to queries.

- Active-passive failover: This failover configuration is used when you want a primary group of resources to be  
  available the majority of the time, and you want a secondary group of resources to be on standby in case all of  
  the primary resources become unavailable. When responding to queries, Route53 includes only the health primary  
  resources. If all of the primary resources are unhealthy, Route53 begins to include only the healthy secondary  
  resources in response to DNS queries. Active-passive has several configuration options based on the number of  
  resources, and at what point you want Route53 to failover based on the health of your resources.FLSI

## VPN, Transit gateway, Direct Connect, etc

## S3 Multipart upload & Pre-signed URLs
