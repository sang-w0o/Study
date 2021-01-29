<h1>@Component, @Service, @Controller ,@Respository의 차이</h1>

<h2>들어가며</h2>

* `@Component`, `@Service`, `@Controller` `@Repository`의 차이를 알아보기 전에   
  우선적으로 Spring에서 `@Component`의 역할을 파악하는 것이 우선이다.

* Spring의 초기 배포 당시에는 모든 Bean 객체들을 XML 파일에 직접 등록해줘야 했다.   
  프로젝트의 규모가 점점 커지면 이 과정은 상당히 신경을 써야하는 부분이 되며,   
  이를 해결하기 위해 Spring에서 내놓은 것이 어노테이션 기반 방식으로 이를 해결하는 것이다.

* Spring 2.5 버전부터 어노테이션으로 DI(Dependency Injection)를 수행할 수 있게 되었다.   
  여기서 `@Component`가 등장했으며, `@Component`가 선언된 클래스는 Spring Bean으로   
  자동적으로 등록된다.

* 이는 곧 개발자가 기존 방식처럼 Bean 객체를 `<bean>` 태그에 직접 명시하여 의존성을   
  추가하지 않아도 됨을 의미한다.
<hr/>

<h2>