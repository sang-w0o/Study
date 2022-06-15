# IoC Container, DI

## IoC Container: Bean Factory, Application Context

- Spring application에서는 객체의 생성과 관계 설정, 사용, 제거 등의 작업을 애플리케이션 코드 대신 독립된 컨테이너가 담당한다.  
  이를 컨테이너가 코드 대신 객체에 대한 제어권을 갖고 있다고 해서 IoC라 한다. 따라서 스프링에서 사용되는 스프링 컨테이너도 자연스럽게  
  IoC 컨테이너라고도 하게 된다.

- 스프링에서는 IoC를 담당하는 컨테이너를 Bean Factory 또는 Application Context라고 부르기도 한다.  
  객체의 생성과 객체 사이의 런타임 관계를 설정하는 DI의 관점에서 봤을 때는 컨테이너를 Bean Factory라고 한다. 하지만 스프링 컨테이너는  
  단순한 DI 작업보다 더 많은 일을 한다. DI를 위한 Bean Factory에 엔터프라이즈 애플리케이션을 개발하는 데 필요한 여러 가지 컨테이너의  
  기능을 추가한 것을 Application Context라 한다. 즉 Application Context는 그 자체로 IoC와 DI를 위한 Bean Factory이면서,  
  그 이상의 기능을 가진 것이다.

- 스프링의 IoC 컨테이너는 일반적으로 Application Context를 말한다. Bean Factory와 Application Context는 각각의 기능을 대표하는  
  `BeanFactory`, `ApplicationContext`라는 두 개의 인터페이스로 정의되어 있다.

---
