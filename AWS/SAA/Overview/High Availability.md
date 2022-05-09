# High Availability Overview

## Migrating

- When migrating large amount of objects, consider using **AWS DataSync** if you can use Direct Connect.  
  Otherwise you can use **AWS Snowball**.

- If migrating data from on-premise DB to RDS, consider using **AWS Database Migration Service**.

- Storage

  - No persistent requirements - Use **EC2 Instance Ephemeral Storage**.
  - Persistent requirements - Use **EBS Persistent Storage**.
  - Object storage - Use **Amazon S3**
  - Archives - Use **Amazon Glacier**

- Getting your data in and out of AWS

  - Direct Connect, VPN, Internet Connection, AWS Snowball, AWS Snowmobile, and AWS Storage Gateway

    - If data takes longer than a WEEK to transport, usually condiser **AWS Snowball.**
    - If you need to shift more than 50TB or data, usually the best option is **AWS Snowball Device**.

![picture 2](/images/AWS_SAA_HA_OVERVIEW_1.png)
![picture 3](/images/AWS_SAA_HA_OVERVIEW_2.png)
![picture 4](/images/AWS_SAA_HA_OVERVIEW_3.png)
![picture 5](/images/AWS_SAA_HA_OVERVIEW_4.png)
![picture 6](/images/AWS_SAA_HA_OVERVIEW_5.png)
![picture 7](/images/AWS_SAA_HA_OVERVIEW_6.png)
![picture 8](/images/AWS_SAA_HA_OVERVIEW_7.png)
![picture 9](/images/AWS_SAA_HA_OVERVIEW_8.png)
![picture 10](/images/AWS_SAA_HA_OVERVIEW_9.png)
![picture 11](/images/AWS_SAA_HA_OVERVIEW_10.png)

---
