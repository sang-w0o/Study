Component Scan
======

* __컴포넌트 스캔__ 은 Spring이 직접 클래스를 검색해서 Bean으로 등록해주는 기능이다.   
  이를 사용하면 설정 클래스에 Bean으로 등록하지 않아도 원하는 클래스를 Bean으로 등록할 수 있으므로   
  설정 코드가 크게 줄어든다.
<hr/>

<h2>@Component 어노테이션으로 스캔 대상 지정하기</h2>

* Spring이 검색해서 Bean으로 등록하게 하려면 클래스에 __@Component__ 어노테이션을 붙여야 한다.
* __@Component__ 어노테이션은 해당 클래스를 __스캔 대상__ 으로 표시한다.
```java
@Component
public class MemberDao {
    //..
}
```

```java
@Component("infoPrinter")
public class MemberInfoPrinter {
    //..
}
```
* 위의 `MemberInfoPrinter` 에는 __@Component__ 에 속성값을 주었는데, 이 속성값은 __Bean으로 등록할 때 사용할 이름__ 이다.
* 만약 속성값을 주지 않으면, __클래스명의 첫 글자를 소문자로 바꾼 이름을 Bean이름으로 사용__ 한다.
<hr/>

<h2>@ComponentScan 어노테이션으로 스캔 설정하기</h2>

* __@Component__ 어노테이션을 붙여 Spring Bean으로 등록하려면, __설정 클래스__ 에 __@ComponentScan__   
  어노테이션을 적용해야 한다.
```java

@Configuration
@ComponentScan(basePackages={"spring"})
public class AppCtx {
    //...
    // MemberInfoPrinter와 MemberDao는 @Component 어노테이션을
    // 적용했기에 설정 클래스에 작성하지 않아도 된다.
}
```
* __@ComponentScan__ 어노테이션의 __baskPackages 속성은 스캔 대상 패키지 목록을 지정__ 한다.
* 위 경우에는 `spring` 패키지와 그 하위 패키지에 속한 클래스를 스캔 대상으로 설정한다.   
  즉, 그 패키지 내에서 __@Component__ 어노테이션이 붙은 클래의 객체를 생성해 Bean으로 등록한다.
<hr/>

<h2>스캔 대상에서 제외하거나 포함하기</h2>

* __@ComponentScan__ 의 __executeFilters__ 속성을 사용하면 스캔할 때 특정 대상을 자동 등록 대상에서   
  제외할 수 있다.
```java
@Configuration
@ComponentScan(basePackages= {"spring"}, excludeFilters=@Filter(type=FilterType.REGEX, pattern="spring\\..*Dao"))
public class AppCtxWithExclude {
	
	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	@Qualifier("printer")
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
}
```
* 위 코드는 __@Filter__ 어노테이션의 __type__ 속성값으로 __FilterType.REGEX__ 를 주었다. 이는 정규표현식을   
  사용하여 제외 대상을 지정한다는 것을 의미한다. __pattern__ 속성은 __FilterType__ 에 적용할 값을 설정한다.   
  위의 경우는 __spring.__ 으로 시작하고 __Dao__ 로 끝나는 정규 표현식을 지정한 것이다.   
  (이에는 spring.MemberDao 클래스가 해당된다.)

```java
@Configuration
@ComponentScan(basePackages= {"spring"}, excludeFilters=@Filter(type=FilterType.ASPECTJ, pattern="spring.*Dao"))
public class AppCtxWithExclude {
	
	@Bean
	public MemberDao memberDao() {
		return new MemberDao();
	}
	
	@Bean
	@Qualifier("printer")
	public MemberPrinter memberPrinter1() {
		return new MemberPrinter();
	}
}
```
* 위 코드는 AspectJ 패턴을 사용하여 대상을 지정한 것이다.
* AspectJ 패턴을 사용하려면 의존 대상에 __aspectjweaver__ 모듈을 추가해야 한다.
```xml
<dependencies>
    <!--  기존 dependency -->
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
        <version>1.8.13</version>
</dependencies>
```
* __patterns__ 속성은 String[] 타입이므로 배열을 이용하여 여러 개의 패턴을 지정할 수 있다.

* 특정 어노테이션을 붙인 타입을 컴포넌트 대상에서 제외할 수도 있다.
* __@NoProduct__ 나 __@ManualBean__ 어노테이션을 붙인 클래스를 스캔 대상에서 제외하고 싶다고 하자.
```java
@Retention(RUNTIME)
@Target(TYPE)
public @interface NoProduct {

}

@Retention(RUNTIME)
@Target(TYPE)
public @interface ManualBean {

}
```
* 이 두 어노테이션을 붙인 클래스를 컴포넌트 스캔 대상에서 제외하려면 다음과 같이 __excludeFilters__ 속성을 지정한다.
```java
@Configuration
@ComponentScan(basePackages={"spring"}, 
    excludeFilters=@Filter(type=FilterType.ANNOTATION, classes = {NoProduct.class, ManualBean.class}))
public class AppCtxWithExclude {

    //..

}
```
* __type__ 속성값으로 __FilterType.ANNOTATION__ 을 사용하면 __classes__ 속성에 필터로 사용할 어노테이션 타입을 값으로 준다.

* 특정 타입이나 그 하위 타입을 컴포넌트 스캔 대상에서 제외하려면 __ASSIGNABLE_TYPE__ 을 __FilterType__ 으로 사용한다.
```java
@Configuration
@ComponentScan(basePackages={"spring"},
    excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MemberDao.class ))
public class AppCtxWithExclude {

    //...

}
```
* __classes__ 속성에는 제외할 타입의 목록을 지정한다. 두 개 이상일 경우에는 배열 표기를 하면 된다.

* 설정할 필터가 2개 이상이면 __@ComponentScan__ 의 __excludeFilters__ 속성에 배열을 사용해서 __@Filter__ 목록을 전달하면 된다.
```java
@Configuration
@ComponentScan(basePackages={"spring"},
    excludeFilters = {
        @Filter(type=FilterType.ANNOTATION, classes = ManualBean.class),
        @Filter(type=FilterType.REGEX, pattern = "spring2\\..*")
    })
public class AppCtxWithExclude {

    //..

}
```
<hr/>

<h3>기본 스캔 대상</h3>

* __@Component__ 어노테이션을 붙인 클래스만 컴포넌트 스캔 대상에 포함되는 것은 아니다.   
  다음 어노테이션을 붙인 클래스들도 컴포넌트 스캔 대상에 포함된다.
  * __@Component__ 
  * __@Controller__
  * __@Service__
  * __@Repository__
  * __@Aspect__
  * __@Configuration__
<hr/>

<h2>컴포넌트 스캔에 따른 충돌 처리</h2>

* 컴포넌트 스캔 기능을 사용해서 자동으로 Bean을 등록할 때는 충돌에 주의해야 한다.   
  크게 Bean이름 충돌과 수동 등록에 따른 충돌이 발생할 수 있다.
<hr/>

<h3>Bean 이름 충돌</h3>

* spring 패키지와 spring2 패키지에 모두 `MemberRegisterService`클래스가 존재하고,   
  두 클래스 모두 __@Component__ 어노테이션을 붙였다고 하자. 이 상황에서 __@ComponentScan__   
  어노테이션을 사용하면 __ConflictingBeanDefinitionException__ 이 발생한다.
* 이와 같은 경우의 충돌이 발생하면, __둘 중 하나에 명시적으로 Bean 이름을 지정해서__ 이름 충돌을 피해야 한다.
<hr/>

<h3>수동 등록한 Bean과의 충돌</h3>

```java
@Component
public class MemberDao {

    //..

}
```
* 위 코드에서 `MemberDao` 클래스는 __@Component__ 어노테이션이 있으므로 컴포넌트 스캔 대상이다.
```java
@Configuration
@ComponentScan(basePackages={"spring"})
public class AppCtx {

    @Bean
    public MemberDao memberDao() {
        return new MemberDao();
    }
}
```
* 하지만 위처럼 설정 클래스에 직접 `MemberDao` 클래스를 __memberDao__ 라는 이름의 Bean으로 등록하면 충돌이 발생한다.

* 스캔할 때 사용하는 Bean 이름과 수동 등록한 Bean이름이 같은 경우에는 __수동 등록한 Bean이 우선__ 이다.   
  즉, `MemberDao` 타입의 Bean은 `AppCtx`에서 정의한 한 개만 존재한다.

```java
@Configuration
@ComponentScan(basePackages={"spring"})
public class AppCtx {

    @Bean
    public MemberDao memberDao2() {
        return new MemberDao();
    }
}
```
* 위의 경우는 스캔을 통해 등록한 `memberDao` Bean과 수동 등록한 `memberDao2` Bean이 모두 존재한다.   
  `MemberDao` 타입의 Bean이 2 개가 생성되므로 자동 주입하는 코드는 __@Qualifier__ 어노테이션을   
  사용해서 알맞은 Bean을 선택해야 한다.
<hr/>