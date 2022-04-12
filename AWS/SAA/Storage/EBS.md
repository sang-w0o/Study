# Amazon EBS(Elastic Block Storage)

- Provides storage to your EC2 instances via EBS volumes, which offer different benetifs to that of  
  instance store volumes used with some EC2 instances.

- Provides persistent and durable block level storage
- EBS volumes offer far more flexibility with regards to managing the data when compared to data stored  
  on instance store volumes.

## Overview

- EBS volumes are independent of the EC2 instance.
- They are logically attached to the instance, instead of directly attached like instance store volumes.
- From a connectivity perspective, each EBS volume can only be attached to a single EC2 instance at any time.  
  However, multiple EBS volumes can be attached to the same EC2 instance.
- Provides snapshot to backup data. (Snapshots are stored in S3)
  - Also able to copy a snapshot from one region to another region.
- Every write to an EBS volume is replicated multiple times within the same AZ of your region to help prevent  
  the complete loss of data. -> EBS volume is only available in a single AZ.
- EBS volumes can only be accessed by one instance at a time.

---

## EBS Volume Types

### SSD

- Suited for work with smaller blocks.
- As boot volumes for EC2 instances.

### HDD

- Suited for workloads that require higher throughput/large blocks of data (big data, logging information etc.)

---

## EBS Security

- Uses AES-256 encryption along with AWS KMS.
