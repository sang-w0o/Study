Bean Lifecycle과 범위
======

<h2>컨테이너 초기화와 종료</h2>

* Spring Container는 __초기화__ 와 __종료__ 라는 Lifecycle을 갖는다.
```java

// 1. Container 초기화
AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppContext.class);

// 2. Container에서 Bean 객체를 구해 사용한다.
Greeter g = ctx.getBean("greeter", Greeter.class);
String msg = g.greet("Spring!");
System.out.println(msg);

// 3. Container 종료
ctx.close();
```
* 위 코드를 보면 `AnnotationConfigApplicationContext` 의 생성자를 이용해서 `Context` 객체를 생성하는데,   
  이 시점에 __Spring Container를 초기화__ 한다. Spring Container는 __설정 클래스에서 정보를 읽어와 알맞은__   
  __Bean 객체를 생성하고 각 Bean을 연결(의존 주입) 하는 작업을 수행__ 한다.

* Container의 초기화가 완료되면 `getBean()`와 같은 메소드를 이용하여 Container에 보관된 Bean객체를 구할 수 있다.

* Container의 사용이 끝나면 Container를 종료한다. 종료할 때 사용하는 메소드는 `close()` 메소드이다.   
  `close()` 메소드는 `AbstractApplicationContext` 클래스에 정의되어 있다.   
  Java 설정을 사용하는 `AnnotationConfigApplicationContext` 클래스나 XML 설정을 사용하는 `GenericXmlApplicationContext`   
  클래스는 모두 `AbstractApplicationContext` 클래스를 상속받고 있다.

* Spring Container의 초기화 및 종료 시에는 다음의 작업도 함께 수행된다.
  * 초기화 : __Bean 객체의 생성, 의존 주입, 초기화__
  * 종료 : __Bean 객체의 소멸__
<hr/>

<h2>Spring Bean 객체의 Lifecycle</h2>

* Spring Container는 Bean 객체의 lifecycle을 관리한다. 이 때 Bean 객체의 lifecycle은 다음과 같다.
  * `객체 생성` ==> `의존 설정` ==> `초기화` ==> `소멸`

* Spring Container를 초기화 할 때, Spring Container는 가장 먼저 __Bean 객체를 생성하고 의존을 설정__ 한다.   
  의존 자동 주입을 통한 의존 설정이 이 시점에 수행된다. __모든 의존 설정이 완료되면 Bean객체의 초기화를 수행__ 한다.   
  Bean 객체를 초기화하기 위해 Spring은 Bean객체의 지정된 메소드를 호출한다.

* Spring Container를 종료하면 Spring Container는 __Bean 객체의 소멸을 처리__ 한다. 이때에도 지정된 메소드를 호출한다.
<hr/>

<h3>Bean 객체의 초기화와 소멸 : Spring Interface</h3>

* Spring Container는 Bean 객체를 초기화하고 소멸하기 위해 Bean 객체의 지정한 메소드를 호출한다.
* Spring은 다음의 두 interface에 이 메소드를 정의하고 있다.
  * `org.springframework.beans.factory.InitializingBean`
  * `org.springframework.beans.factory.DisposableBean`
```java
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
    void destroy() throws Exception;
}
```
* Bean 객체가 `InitializingBean` 인터페이스를 구현하면 Spring Container는 초기화 과정에서 Bean 객체의   
  `afterPropertiesSet()` 메소드를 실행한다.
* Spring Container는 Bean 객체가 `DisposableBean` 인터페이스를 구현하면 소멸 과정에서 Bean 객체의   
  `destroy()` 메소드를 실행한다.
<hr/>

<h3>Bean 객체의 초기화와 소멸 : Custom 메소드</h3>

* 직접 구현한 클래스가 아닌, 외부에서 제공받은 클래스를 Spring Bean 클래스로 설정하고 싶을 때가 있다.   
  이 경우 소스코드를 받지 않았다면, `InitializeBean` 와 `DisposableBean` 인터페이스를 구현하도록   
  수정할 수 없다. 
```java
public class Client2 {
    private String host;

    public void setHost(String host) {
        this.host = host;
    }

    public void connect() {
        System.out.println("Client2.connect() called.");
    }

    public void send() {
       System.out.println("Client2.send() to " + host);
    }

    public void close() {
        System.out.println("Client2.close() called.");
    }
}
```
* `Client2` 클래스를 Bean으로 사용하려면 초기화 과정에서 `connect()` 메소드를 호출하고, 소멸 과정에서   
  `close()` 메소드를 실행해야 한다고 하자. 그러면 다음과 같이 __@Bean__ 어노테이션의 __initMethod__ 속성과   
  __destroyMethod__ 속성에 초기화와 소멸 과정에서 사용할 메소드 이름을 지정해주면 된다.
```java
@Bean(initMethod="connect", destroyMethod="close")
public Client2 client2() {
    Client2 client = new Client2();
    client.setHost("host");
    return client;
}
```
* 주의 : __초기화 메소드가 두 번 호출되지 않도록 주의하자.__
```java

// Client.java
public class Client implements InitializingBean, DisposableBean {

    //...

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("Client.afterPropertiesSet() called.");
    }

    @Override
    public void destroy() throws Exception {
        System.out.println("Client.destroy() called.");
    }
}

// AppCtx.java

@Bean
public Client client() {
    Client client = new Client();
    client.setHost("host");
    client.afterPropertiesSet();
    return client;
}
```
* 위 코드는 Bean 설정 메소드에서 `afterPropertiesSet()` 메소드를 호출한다. 그러나 `Client` 클래스는   
  `InitializingBean` 인터페이스를 구현했기 때문에 `afterPropertiesSet()` 메소드를 또 호출한다.   
  이러한 중복 호출을 방지하도록 주의해야 한다.
<hr/>

<h2>Bean 객체의 생성과 관리 범위</h2>

* 기본적으로 __Spring Container는 Bean 객체를 단 1개만 생성하며, 이를 Singleton Scope__ 라 한다고 했다.   
  하지만 __Prototype 범위의 Bean__ 을 설정하면, __Bean 객체를 구할 때 마다 매번 새로운 객체를 생성__ 한다.

* 특정 Bean을 Prototype 범위로 지정하려면 값으로 __prototype__ 을 갖는 __@Scope__ 어노테이션을 사용하면 된다.
```java
@Configuration
public class AppCtxWithPrototype {

    @Bean
    @Scope("prototype")
    public Client client() {
        Client client = new Client();
        client.setHost("host");
        return client;
    }
}
```
* Singleton 범위를 명시적으로 지정하고 싶다면 __@Scope__ 어노테이션의 값으로 __singleton__ 을 지정하면 된다.

* Prototype 범위를 갖는 Bean은 __완전한 lifecycle을 따르지 않는다__.   
  Spring Container는 Prototype의 Bean 객체를 __생성하고 프로퍼티를 설정하고 초기화 작업까지는 수행하지만,__   
  __Container를 종료한다고 해서 Prototype의 Bean 객체의 소멸 메소드는 실행하지 않는다__.
