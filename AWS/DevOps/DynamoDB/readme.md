# AWS DynamoDB

- [AWS DynamoDB 공식문서](https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html)를 읽고 정리한 문서 입니다.

## 소개

- Amazon DynamoDB는 AWS에서 full managed 서비스로 제공하는 NoSQL 데이터베이스이다.  
  빠르고 예측 가능한 성능과 끝없는 scalability를 제공하며 자체적으로 암호화 기능도 제공한다.

- DynamoDB로는 데이터베이스 테이블을 만들고 원하는 만큼 데이터를 받아올 수도, 그리고 원하는 만큼 저장시킬 수도 있다.  
  또한 downtime도 없고, 성능 저하도 없이 테이블들을 scale up, scale down 할 수 있다.

### High Availability and Durability

- DynamoDB 내의 데이터는 SSD에 저장되며, 이 SSD는 AWS Region 내의 여러 가용 영역(Availability Zone)으로  
  자동으로 replicate된다. 이로써 고가용성과 데이터의 안전성을 보장하게 된다. 심지어는 다른 AWS Region과 데이터를  
  동기화하도록 설정할 수도 있다.
