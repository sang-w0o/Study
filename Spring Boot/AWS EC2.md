AWS EC2
======

* `AWS(Amazon Web Service)` 클라우드 서비스를 통해 서버 배포를 한다.
* 외부에서 웹 서비스에서 접근하려면 24시간 작동하는 서버가 필요한데, 이에는 3가지 선택지가 있다.
  1. PC의 24시간 구동
  2. 호스팅 서비스 이용
  3. 클라우드 서비스 이용

* 일반적으로 비용은 호스팅 서비스나 PC의 구동이 저렴하나, 특정 시간에만 트래픽이 몰리는 경우에는 __유동적으로 사양을 늘릴__   
  __수 있는 클라우드가 유리__ 하다. 클라우드 서비스는 쉽게 말해 클라우드(인터넷)를 통해 서버, Storage, DB, Network,   
  Software, Monitoring 등의 computing service를 제공하는 것이다. 예를 들어 `AWS-EC2`는 서버 장비를 대여하는 것이지만, 실제로는   
  그 안의 log관리, 모니터링, 하드웨어 교체, 네트워크 관리 등을 기본적으로 지원한다. 즉, 개발자가 직접 해야할 일을 AWS가 지원한다.   
  이러한 클라우드에는 몇 가지 형태가 있다.
  * `Infrastructure as a Service(IaaS)` : 기존 물리 장비를 middleware와 함께 묶어둔 추상화 서비스로, 가상 머신, storage,   
    network, OS 등의 IT Infra를 대여해주는 서비스이다. AWS-EC2, S3 등이 있다.
  * `Platform as a Service(PaaS)` : `IaaS`를 한번 더 추상화한 서비스로, 많은 기능들이 자동화되어 있다. AWS-Beanstalk, Heroku 등이 있다.
  * `Software as a Service(SaaS)` : 소프트웨어 서비스로, Google Drive, DropBox 등이 있다.

* AWS 서비스는 보통 `IaaS`를 사용한다. AWS의 `PaaS`인 `Beanstalk`를 사용하면 대부분의 작업이 간소화되지만, 프리미어이기 때문에   
  무중단 배포가 불가하다.
<hr/>

