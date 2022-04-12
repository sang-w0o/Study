# Amazon EFS

## Overview

- EFS provides simple, scalable file storage for use with Amazon EC2 instances.  
  It provides ability for users to browse cloud network resources

- EC2 instances can be configured to access Amazon EFS instances using configured mount points.  
  Mount points can be created in multiple AZs that attach to multiple EC2 instances.

- EFS is a fully managed, highly available and durable service.
- EFS can easily scale to petabytes in size with low latency access.
- EFS has been designed to maintain a high level of throughput(MB/s).

- As the file system can be accessed by multiple instances, it makes it a very good storage option for applications  
  that scale across multiple instances allowing for parallel access for data.

- EFS is also regional, so any application deployments that span across multiple AZs can all access the same  
  file systems providing a high level of availability of your application storage layer.

---

## Storage Classes and Performance Options

### Storage Class

- EFS offers 2 different storage classes.

  - Standard: default class
    - Charged on the amount of storage used per month.
  - IA(Infrequent Access): Useful if you're storing data on EFS that is rarely accessed.
    - Charged on the amount of storage used per month + Read, Write operations you make to the storage class.

- EFS Lifecycle Management exists, thus automatically moves data between the Standard and IA storage classes.
  - 특정 주기(14, 30, 60, 90일 중 하나) 동안 접근되지 않으면 IA로 이동되고, 접근되면 그 즉시 Standard로 이동된다.
    > 128K 미만의 크기를 가진 파일 및 metadata는 항상 Standard Storage Class에 있다.

### Performance Modes

- General Purpose
  - When using General Purpose, you can monitor I/O metric with CloudWatch and determine weather to move to Max I/O or not.
- Max I/O

![picture 1](/images/AWS_SAA_EFS_1.png)

### Throughput Modes

- Bursting throughput
  - default. The more file you store, the more throughput you get.
- Provisioned throughput

---

## Mounting methods

- EFS offers two methods to connect your Linux-based EC2 instances to your EFS file system.

  - Use standard Linux NFS client to perform the mount.
  - Use EFS mount helper. (recommended)

---

## Managing EFS Security

### Allowing access

- To initially create your EFS file system, you need to ensure that you have _"Allow"_ access for the following services.

```
elasticfilesystem:CreateFileSystem
elasticfilesystem:CreateMountTarget

ec2:DescribeSubnet
ec2:CreateNetworkInterface
ec2:DescribeNetworkInterfaces
```

> EC2 permissions are required to allow actions carried out by the `CreateMountTarget` action to be carried out.

### Encryption

- EFS supports both encryption at rest and in transit.

- At rest: Enable encyption of data at rest by checking the corresponding checkbox in the EFS console.  
  This uses AWS KMS to manage your encryption keys.

- In transit: If you need to ensure your data remains secure between the EFS file system and your end client,  
  then you need to implement encryption in transit.

  - The encryption is enabled through the utilization of the TLS protocol, which is transport layer security,  
    when you perform your mounting of your EFS file system.

---

## Importing Data

- You can use AWS DataSync to move on-premise file server datas to EFS.

- Things that you can do with DataSync

  - Migrate an NFS file system from Amazon EC2 to Amazon EFS within the same AWS region.
  - Replicate an NFS file system from Amazon EC2 in one AWS region to an Amazon EFS file system in a  
    different AWS region for disaster recovery.
  - Migrate an Amazon EFS file system from EFS Standard(no lifecycle management) to an EFS file system with  
    lifecycle management enabled. File systems with lifecycle management enabled will automatically move  
    files to a lower-cost EFS IA Storage class based on a predefined lifecycle policy.
  - Migrate an Amazon EFS file system from one performance mode to another performance mode within the smae AWS region.
  - Replicate an Amazon EFS file system from one AWS region to another Amazon EFS file system in a different AWs region  
    for disaster recovery.

---
