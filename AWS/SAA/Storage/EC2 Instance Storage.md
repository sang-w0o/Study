# EC2 Instance Storage

## Instance Store Volumes

- Instance Store Volumes provide ephemoral storage (temporary)
- Not recommended for critical or valuable data, since it could be lost.
- If your instance is stopped or terminated, your data is lost.
- If your instance is rebooted, your data will remain in tact.
- Instance Store Volumes are not available for all instances.
- The capacity of Instance Store Volumes increases with the size of the EC2 instance.
- Instance Store Volumes have the same security mechanisms provided by EC2.
- Instance Store Volumes should not be used for:
  - Data that needs to remain persistent.
  - Data that needs to be accessed and shared by multiple entities.

### Benefits

- No additional cost for storage, since it is included in the price of the instance.
- Offers very high I/O speed.
- I3 instance family:
  - 3.0 million random read IOPS
  - 1.4 million write IOPS
- Instance store volumes are ideal as cache or buffer for rapidly changing data without the need for retention.
- Often used within a load balancing group, where data is replicated and pooled between the fleet.
