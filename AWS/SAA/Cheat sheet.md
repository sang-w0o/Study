# Cheat sheet

## EFS(Elastic File System) vs EBS(Elastic Block Storage)

- EBS: Provides block-level storage for your EC2 instances for persistent and durable data storage.  
  EBS is an appropriate choice for storing frequently changing data or if you have specific IOPS requirements.  
  It is possible to attach more than one EBS volumes to an EC2 instance; however, multiple EC2 instances cannot  
  share EBS storage.

- EFS: EFS is a file-level storage optimized for low latency access that appears to users like a file manager  
  interface. EFS uses standard file system semantics such as locking files, renaming files, updating files, and  
  uses a hierarchy structure. You can mount EFS storage to multiple EC2 instances to enable concurrent access  
  to the file system.

## AWS Backup, Amazon Data Lifecycle Manager

- AWS Backup is a service that allows you to automate and manage data backups for various amazon services.  
  For example, you can use AWS Backup to protect your data stored on EBS volumes, EFS, or Amazon RDS databases.

- Amazon Data Lifecycle Manager: Helps organizations manage EBS snapshots.

## EBS instance types

- General Purpose SDD(gp2): Used for workloads where IOPS is more critical than throughput, and the cost is more  
  important than performance.

- Provisioned IOPS SSD(io2): Used for workloads when more concerned with IOPS performance and when workloads are  
  primarily samller random I/O operations.

- Throughput Optimized HDD(st1): Used when primary concerned with throughput, and storage pattern aligns with larger  
  sequential I/O operations.

- Cold HDD(sc1): Used when storage is infrequently accessed and when minimizing storage cost is more important  
  than performance.

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

- Direct Connect: Charged for connection to your Amazon VPC. No additional cost for on-premises to access your  
  Amazon EFS.

## S3 Multipart upload & Pre-signed URLs

- Ways to minimize costs of incomplete multipart uploads:

  - Stop unsuccessful multipart uploads.
  - Stop incomplete uploads with a bucket lifecycle policy.

## S3 Object lock

- S3 object lock can prevent objects from modification or deletion.

- Object lock has two retention modes:
  - Compliance mode: Prevents objects from being deleted or updated by users, including the root user.
  - Governance mode: Allows object to be modified, prevents objects from being deleted.

## Amazon GuardDuty, Amazon Inspector, Amazon Macie, Amazon Detective

- Amazon GuardDuty: A threat detection service that continuously monitors AWS accounts and workloads for malicious  
  activity and anomalous behavior. GuardDudy utilizes machine learning to identify the threats and classify them.

- Amazon Inspector: An automated security assessment service that monitors for software vulnerabilities and unintended  
  network exposure.

- Amazon Macie: A fully managed data security and data privacy service that leverages pattern recognition and machine  
  learning to discover and protect sensitive data within your web prescence.

- Amazon Detective: Makes it easy to analyze, investigate and determine the root cause of security assessment findings  
  or suspicious activities. Analysis and investigation data are also presented in the form of graphs, continously refreshed.

## ENI(Elastic Network Interface)

- TODO

## Auto Slacing Policies

- TODO

## S3 Glacier Data Retrieval options

- Expedited:

  - Used for urgent access to a subset of an Archive
  - Less than 250MB
  - Data available within 1~5 minutes.
  - With provisioned capacity: Guaranteed to be available when you need them(Recommended for DR plan)
  - On-demand

- Standard

  - Used to retrieve any of your Archives.
  - Data available within 3~5 hours.
  - Cheaper than Expedited.

- Bulk
  - The cheapest option for data retrieval.
  - Used to retrieve petabytes of data
  - Data available within 5~12 hours.

## EC2 Purchase options

### On-demand instances

- Can be launched at any time.
- Can be used for as long as needed.
- Flat rate determined on the instance type.
- Typically used for short-term uses where workloads can be irregualr

### Reserved instances

- Purchases for a set period of time for reduced cost.
- Best applied for long term, predictable workloads.
- Modifying instance size which is **within the same instance family** is possible.
- You can modify the AZ of the reserved instance, change the scope of the reserved instance from AZ to region.

### Scheduled instances

- You pay for the reservations on a recurring schedule, either daily, weekly or monthly.
- You could set up a scheduled instance to run during that set time frame once a week.
- Note that event if you don't use the instance, you would still be charged.

### Spot instances

- Bid for a unused EC2 compute resources.
- No guarantees for a fixed period of time.
- Must bid higher than the current spot price which is set by AWS.  
  This spot price fluctuates depending on supply and demand of the unused resource.
- As soon as your bid price comes lower than the fluctuating spot price, you will be issued a two-minute warning  
  before the instance automatically terminates and is removed from your AWS environment.
- You can bit for large EC2 instances at a very low cost point, saving a huge amount on cost.
- Since it can be removed at any time, spot instances are only useful for processing data and applications that  
  can be suddenly interrupted.(Batch jobs, background processing of data etc.)

### Tenancy

- Relates to what underlying host your EC2 instance will reside on, so essentially the physical server within  
  an AWS Data Center.

#### Shared tenancy

- EC2 instance is launched on any available host with the required resources.
- The same host may be used by multiple customers.

### Dedicated instances

- Hosted on hardware that no other customer can access.
- May be required to meet compliance.
- Occurs additional charges.

### Dedicated hosts

- Additional visibility and control on the physical host.
- Allows to use the same host for a number of instances.
- May be required to meet compliance.
-

## S3 Transfer Acceleration

- Amazon S3 Transfer Acceleration enables fast, easy, and secure transfers of files over long distances between  
  your client and your Amazon S3 bucket. S3 Transfer Acceleration leverages Amazon CloudFront’s globally distributed  
  AWS Edge Locations. As data arrives at an AWS Edge Location, data is routed to your Amazon S3 bucket over an  
  optimized network path.

## How to establish site-to-site VPN

-

## Cluster placement group

- A cluster placement group is a logical grouping of instances within a single Availability Zone.  
  Placement groups are recommended for applications that benefit from low network latency, high network throughput, or both

---

2. Not ENI?
3. Does using EBS slow down boot time?
