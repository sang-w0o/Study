<h1>AWS DataSync & S3 Webinar<h1>

<h2>파일 스로티지의 특징</h2>

<h3>NAS Storage</h3>

* NAS Storage의 사용 사례
  * 제조업 설계 디자인 파일
  * 헬스케어 의료 서비스
  * 동영상, 오디오 컨텐츠 서비스
  * 내부 직원용 파일 스토리지

* 즉, 데이터가 시간에 정비례하여 증가한다.   
  이를 `백업`하면 운영 부담 및 비용을 최소화할 수 있다.
<hr/>

<h2>Amazon S3</h2>

* 3 Availibility Zone Model
  * 지리적으로 서로 격리된 환경에서 Availability Zone(AZ) 구성
  * AWS Region 내에 최소 3개의 AZ에 걸쳐 여러 디바이스에 저장한다.
  * 기본적으로 데이터를 올리면 3개의 리전에 데이터가 __중복 저장__ 된다.

* 연간 객체에 대해 99.999999% 내구성 제공
  * 연평균 예상 손실률 0.000000001%
  * 서로 격리된 환경에서 Availablity Zone(AZ) 구성

* 즉, S3를 사용하면 __백업에 대한 고민을 할 필요가 없다.__

* S3 규모
  * Exabyte 규모의 데이터가 수백만 장치에 걸쳐 저장되어 있다.
  * 수 조 개의 오브젝트가 전세계에 걸쳐 저장되어 있다.
  * 보통 성능 피크는 초당 수 백만 요청 규모이다.
  * 하나의 리전에서 하루 최대 60TB/s 처리가 가능하다.
  * 3개 이상의 AZ에 거쳐 235+개의 마이크로 서비스가 요처을 처리한다.
  
* S3 Versioning
  * 데이터를 덮어쓰거나 삭제하는 의도치 않은 사용자의 작업 실수 방지 가능
  * 데이터를 덮어쓰거나 삭제하는 애플리케이션의 코드 오류 처리 가능
  * `Bucket Versioning`을 이용하여 S3에 저장된 모든 객체에 대하 백업 및 복원 가능

* S3 Storage Class

  * S3 Standard : Active, 자주 사용하는 데이터
    * Milliseconds Access
    * >= 3 AZ
    * $0.0210/GB

  * S3 Intelligent-Tiering
    * 접근 패턴이 자주 변동되는 데이터
    * >= 3 AZ
    * 객체 당 모니터링 비율, 최소 저장 기간 요건

  * S3 Standard-IA
    * 자주 접근하지 않는 데이터
    * Milliseconds Access
    * $0.0125/GB
    * 최소 저장 기간 요건, 최소 오브젝트 사이즈 요건

  * __서비스의 성격에 맞는 S3 class를 선택하여 사용하는 것이 중요하다__.

* S3 Security
  * 접근 제어(AWS Identity & Access Management)
  * AWS Key Management Console

* Amazon S3는 데이터 분석의 초석이 될 수 있다.
  * 비정형 데이터부터 정형 데이터까지 다양한 유형의 데이터를 저장할 수 있으므로 모든 데이터를 담고 있는   
    저장소로서의 기능을 수행할 수 있다.(데이터 저장소 = Data Lake)

* S3는 AWS 분석 서비스와 연계가 가능하다.
<hr/>