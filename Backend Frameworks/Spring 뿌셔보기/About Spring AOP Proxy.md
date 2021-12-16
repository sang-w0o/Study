# About Spring AOP Proxy

- <a href="http://www.yes24.com/Product/Goods/62268795">최범균님의 스프링 5 프로그래밍 입문</a>에서 AOP를 설명할 때 Proxy를 아래처럼 정의한다.

> - Proxy: 핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체

- 조금 다른 표현으로 정의하자면, Proxy는 **타겟(대상)을 감싸서 타겟의 요청을 대신 받아주는 Wrapper 객체** 라 할 수 있다.  
  여기서 타겟은 우리가 작성한 클래스 등이 된다.

- Spring에서 Proxy를 만드는 방식에는 크게 두 가지가 있다. 바로 JDK Dynamic Proxy와 CGLib 을 사용하는 방법이다.

## JDK Dynamic Proxy

-

<hr/>

## CGLib
