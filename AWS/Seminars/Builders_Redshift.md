# DB보다 먼, 빅데이터보다 가까운 Amazon Redshift 알아보기

> AWS Builders Korea Program

## 데이터 트렌드

- 데이터 분석의 제약사항

  - 다양성: 원천 데이터의 종류가 너무 다양함, 다각도 분석의 필요성, 데이터의 크기 및 속도 등
  - 성능: 느린 성능, 관리의 어려움, 확장의 어려움 등
  - 비용: 예상치 못한 비용의 증가, 도구의 고착화, 보안 이슈 등

- **즉, 매우 빠른 속도로 커지는 데이터를 커버하기가 어렵다.**

### Redshift가 데이터 분석의 어려움을 해결하는 방법

- 다양한 데이터 분석: AWS DW, Data Lake, DB를 통합해 분석할 수 있다.
- 안정적인 성능 보장: 일반적인 DW 대비 최대 3배의 성능을 보장한다.
- 비욜 절감: 필요한 만큼만 on-demand RI로 최대 75% 절감 가능하다.

> DW: Data Warehouse

### 기존 DW 아키텍처의 제약사항

- 확장성

  - 필요 시 쉽게 확장할 수 없다.
  - HW 변경, 업그레이드 시 장기간의 작업 시간이 필요하다.

- 비용

  - 높은 플랫폼 관리 비용
  - Cold/Warm 데이터 보관에 따른 공간 낭비 발생

- 사용성 제약

  - 데이터 포맷의 제약
  - 데이터 Silo 제약
  - 별도의 수집 및 변환(TEL) 아키텍쳐를 구축해야 한다.
  - 사용자 기준의 제약 발생

- 고전적인 구조
  - 규격화된 사이즈 일반화로 인한 제약

### AWS Analytics Portfolio

![picture 52](/images/AWS_REDSHIFT_1.png)

---

## Redshift 아키텍쳐

- Amazon Redshift에서는 단일 클러스터 DW 뿐만 아니라 데이터 Silo를 제거해 폭넓은 데이터 활용이 가능하다.

![picture 54](/images/AWS_REDSHIFT_3.png)

### 일반적인 활용 사례

- on-premise의 DW에 고가용성 및 확장성을 보장하기 위해 migration
- metric 분석
- 빅데이터 분석

### Redshift 기본원리

- 클라우드에 최적화된 MPP 기반의 DW, Columnar 기반의 OLAP DB이다.

![picture 55](/images/AWS_REDSHIFT_4.png)

### Redshift 클러스터 아키텍쳐

![picture 56](/images/AWS_REDSHIFT_5.png)

### Redshift 아키텍쳐의 발전 과정

![picture 58](/images/AWS_REDSHIFT_6.png)

### Compute Node - 다수의 Slice로 구성

![picture 59](/images/AWS_REDSHIFT_7.png)

### Redshift 인스턴스 타입

### AQUA(Advanced Query Accelerator)

![picture 60](/images/AWS_REDSHIFT_8.png)

### Redshift의 보안

![picture 61](/images/AWS_REDSHIFT_9.png)

### 복원성

- 99.9% SLA
- 2nd node로 데이터 자동 복제 가능
- 디스트 및 node 장애에 대한 자동 감지 및 복구
- 데이터 자동 백업
- 백업 데이터를 다른 region으로 복제
- RA3 instance 사용 시 다른 AZ로 이전 가능(AZ Level Failure)

---

## Redshift 현대화 데이터 아키텍쳐

![picture 53](/images/AWS_REDSHIFT_2.png)

---

## 신규 기능

---
