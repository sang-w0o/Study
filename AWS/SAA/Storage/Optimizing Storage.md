# Optimizing Storage

## Amazon FSx

- Amazon FSx also focuses on file systems, much like EFS.
- Amazon FSx comes in two forms.

  - Amazon FSx for Windows File
    - Provides a fully managed native Microsoft Windows file system on AWS
    - Easily move and migrate your windows-based workloads requiring file storage
    - The solution is built on Windows Server
    - Operates as shared file storage
    - Full support for:
      - SMB Protocol
      - Windows NTFS
      - AD(Active Directory) integration
      - DFS(Distributed File System)
    - Uses SSD Storage for enhanced performance and throughput providing sub-millisecond latencies.
  - Amazon FSx for Lustre
    - A fully managed file system designed for compute-intensive workloads, for example Machine Learning and HPC.
    - Ability to process massive data sets.
    - Performance can run up to hundreds of GB per second of throughput, millions of IOPS and sub-millisecond latency.
    - Integration with Amazon S3.
    - Supports cloud-bursting workloads from on-premises over Direct Connect and VPN connections.

### Amazon FSx for Windows File Server

- Using data deduplication doesn't add any cost, so it is a great way to minimize storage costs.

---

## AWS Storage Gateway

---

## Amazon Backup

---
