AOP (Aspect Oriented Programming)
======

* AOP 를 구현하기 위해서는 다음의 `Dependency`를 추가해야 한다.
```xml
<dependency>
	<groupId>org.aspectj</groupId>
  	<artifactId>aspectjweaver</artifactId>
	<version>1.8.13</version>
</dependency>
```
* Spring Framework의 AOP 기능은 `spring-aop` 모듈이 제공하는데, `spring-context` 모듈을   
  의존 대상에 추가하면 `spring-aop` 모듈도 함께 의존 대상에 포함된다.   
  `aspectjweaver` 모듈은 AOP를 설정하는데 필요한 어노테이션을 제공한다.
```java
package chap07;

public interface Calculator {
	public long factorial(long num);
}
```
* 위 interface는 계승을 구하기 위한 것이다.

```java
package chap07;

public class ImpeCalculator implements Calculator {
	
	@Override
	public long factorial(long num) {
		long result = 1;
		for(long i = 1; i <= num; i++) {
			result *= i;
		}
		return result;
	}
}
```
* 위 `ImpeCalculator`는 for문을 이용하여 계승 값을 구했다.

```java
package chap07;

public class RecCalculator implements Calculator {
	
	@Override
	public long factorial(long num) {
		if(num == 0)
			return 1;
		else
			return num * factorial(num - 1);
	}
}
```
* 위의 `RecCalculator`는 재귀 호출을 통하여 계승 값을 구했다.
<hr/>

<h2>Proxy와 AOP</h2>

* 위에서 구현한 클래스들의 실행 시간을 출력하려면 `System.currentTimeMillis()` 를 사용하면 되지만,   
  `RegCalculator` 와 같이 재귀호출을 하는 경우에는 실행 시간을 측정하기가 매우 까다롭다.   
  또한 기존 코드를 수정해야 하며 코드의 중복이 발생할 수 있는데, 이를 해결해주는 것이 `Proxy` 객체이다.

```java
package chap07;

public class ExeTimeCalculator implements Calculator {
	
	private Calculator delegate;
	
	public ExeTimeCalculator(Calculator delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public long factorial(long num) {
		long start = System.nanoTime();
		long result = delegate.factorial(num);
		long end = System.nanoTime();
		System.out.printf("%s.factorial(%d) 실행시간 : %d\n", delegate.getClass().getSimpleName(), 
				num, (end-start));
		
		return result;
	}
}
```
* 위 코드는 기존 코드를 변경하지 않고 실행 시간을 출력할 수 있으며, 실행 시간을 구하는 코드의   
  중복도 없다. 이것이 가능한 이유는 `ExeTimeCalculator`가 `factorial()` 기능을 직접 구현하기 보다는   
  다른 객체에 `factorial()`의 실행을 위임했기 때문이다. 또한 계산 기능 외에 __실행 시간 측정__ 이라는   
  부가적인 기능을 실행하도록 했다.

* 이렇게 __핵심 기능의 실행은 다른 객체에 위임하고 부가적인 기능을 제공하는 객체를__ `Proxy` 라고 부른다.   
  `Proxy` 객체의 특징은 __핵심 기능은 구현하지 않는다는 것__ 이다. 대신, __여러 객체에 공통으로 적용할 수__   
  __있는 기능을 구현한다__.
<hr/>

<h3>AOP</h3>

* AOP는 Aspect Oriented Programming의 약자로, __여러 객체에 공통으로 적용할 수 있는 기능을 분리해서__   
  __재사용성을 높여주는 프로그래밍 기법__ 이다. AOP는 __핵심 기능과 공통 기능의 구현을 분리함으로써__   
  __코드의 수정 없이 공통 기능을 적용__ 할 수 있게 만들어준다.

* AOP의 기본 개념은 __핵심 기능에 공통 기능을 삽입__ 하는 것인데, 이를 구현하는 방법은 3가지가 있다.
  * 컴파일 시점에 코드에 공통 기능을 삽입하는 방법
  * 클래스 로딩 시점에 Byte코드에 공통 기능을 삽입하는 방법
  * 런타임에 Proxy 객체를 생성해서 공통 기능을 삽입하는 방법
* Spring이 제공하는 AOP 방식은 __Proxy를 이용한 방식__ 이다. Spring AOP는 Proxy 객체를 자동으로 만들어준다.

* AOP에서 공통 기능을 __Aspect__ 라 하는데, Aspect외에 알아두어야 할 용어들은 다음과 같다.
<table>
    <tr>
        <td>Advice</td>
        <td>언제 공통 관심 기능을 핵심 logic에 적용할 지를 정의하고 있다.</td>
    </tr>
    <tr>
        <td>Joinpoint</td>
        <td>Advice를 적용 가능한 지점을 의미한다. 메소드의 호출, 필드 값 변경 등이 Joinpoint에 해당한다. Spring은 Proxy를 이용하여
            AOP를 구현하기 때문에 메소드 호출에 대한 Joinpoint만 지원한다.</td>
    </tr>
    <tr>
        <td>Pointcut</td>
        <td>Joinpoint의 부분집합으로서 실제 Advice가 적용되는 Joinpoint를 나타낸다. Spring에서는 Regex나 AspectJ 문법을 이용하여
            Pointcut을 정의할 수 있다.</td>
    </tr>
    <tr>
        <td>Weaving</td>
        <td>Advice를 핵심 logic 코드에 적용하는 것을 의미한다.</td>
    </tr>
    <tr>
        <td>Aspect</td>
        <td>여러 객체에 공통적으로 적용되는 기능을 의미한다.</td>
    </tr>
</table>

<hr/>

<h3>Advice의 종류</h3>

* Spring은 Proxy를 이용하여 메소드 호출 시점에 Aspect를 적용하기에 구현 가능한 Advice의 종류는 다음과 같다.
<table>
    <tr>
        <td>Before Advice</td>
        <td>대상 객체의 메소드 호출 전에 공통 기능을 실행한다.</td>
    </tr>
    <tr>
        <td>After Returning Advice</td>
        <td>대상 객체의 메소드가 Exception 없이 실행된 이후에 공통 기능을 실행한다.</td>
    </tr>
    <tr>
        <td>After Throwing Advice</td>
        <td>대상 객체의 메소드를 실행하는 도중 Exception이 발생한 경우에 공통 기능을 실행한다.</td>
    </tr>
    <tr>
        <td>After Advice</td>
        <td>Exception의 발생여부와 관계없이 객체의 메소드 실행 후 공통 기능을 실행한다.</td>
    </tr>
    <tr>
        <td>Around Advice</td>
        <td>대상 객체의 메소드 실행 전, 후 또는 Exception 발생 시점에 공통 기능을 실행한다.</td>
    </tr>
</table>

* 위 중 널리 사용되는 것은 __Around Advice__ 로, 다양한 시점에 원하는 기능을 삽입할 수 있기 때문이다.   
  Cache, Performance Monitoring 등의 기능과 같은 Aspect를 구현할 때에는 Around Advice를 주로 이용한다.
<hr/>

<h2>Spring AOP 구현</h2>

* Spring AOP를 이용하여 공통 기능을 구현하고 적용하는 순서는 다음과 같다.
  * (1) Aspect로 사용할 클래스에 __@Aspect__ 어노테이션을 붙인다.
  * (2) __@Pointcut__ 어노테이션으로 공통 기능을 적용할 Pointcut을 정의한다.
  * (3) 공통 기능을 구현한 메소드에 __@Around__ 어노테이션을 적용한다.
<hr/>

<h3>@Aspect, @Pointcut, @Around를 이용한 AOP의 구현</h3>

* 개발자는 공통 기능을 제공하는 __Aspect 구현 클래스__ 를 만들고, Java 설정을 이용하여   
  Aspect를 어디에 적용할지 설정하면 된다. Aspect는 __@Aspect__ 어노테이션을 이용하여 구현하며,   
  `Proxy`는 Spring Framework가 알아서 만들어준다.

```java
@Aspect
public class ExeTimeAspect {
	
	@Pointcut("execution(public * chap07..*(..))")
	public void publicTarget() {
		
	}
	
	@Around("publicTarget()")
	public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.nanoTime();
		try {
			Object result = joinPoint.proceed();
			return result;
		} finally {
			long finish = System.nanoTime();
			Signature sig = joinPoint.getSignature();
			System.out.printf("%s.%s(%s) 실행시간 : %d ns\n", 
				joinPoint.getTarget().getClass().getSimpleName(),
				sig.getName(), Arrays.toString(joinPoint.getArgs()),
				(finish - start));
				
		}
	}
}
```
* __@Aspect__ 어노테이션을 적용한 클래스는 __Advice, Pointcut__ 을 함께 제공한다.
* __@Pointcut__ 은 공통 기능을 적용할 대상을 설정한다.
* __@Around__ 어노테이션은 Around Advice를 설정한다. 위 코드에서는 값이 `publicTarget()`인데,   
  이는 `publicTarget()` 메소드에 정의한 Pointcut에 공통 기능을 적용함을 의미한다. `publicTarget()`   
  메소드는 chap07 패키지와 그 하위에 있는 `public` 메소드를 Pointcut으로 설정하고 있으므로, chap07   
  패키지 또는 그 하위 패키지에 속한 Bean 객체의 `public` 메소드에 __@Around__ 가 붙은 `measure()`   
  메소드를 적용한다.
* `measure()` 메소드의 `ProceedingJoinPoint` 타입 파라미터는 __Proxy 대상 객체의 메소드를 호출할 때 사용__ 한다.   
  위 코드처럼 `proceed()` 메소드를 사용해서 __실제 대상 객체의 메소드를 호출__ 한다.

```java
@Configuration
@EnableAspectJAutoProxy
public class AppCtx {
	
	@Bean
	public ExeTimeAspect exeTimeAspect() {
		return new ExeTimeAspect();
	}
	
	@Bean
	public Calculator calculator() {
		return new RecCalculator();
	}
}
```
* 위는 Spring 설정 클래스인데, __@Aspect__ 어노테이션을 붙인 클래스를 공통 기능으로 사용하려면   
  __@EnableAspectJAutoProxy__ 어노테이션을 설정 클래스에 붙여야 한다. 이 어노테이션을 추가하면   
  Spring은 __@Aspect__ 어노테이션이 붙은 Bean 객체를 찾아서 Bean 객체의 __@Pointcut__ 설정과 __@Around__ 설정을 사용한다.

```java
@PointCut("execution(public * chap07..*(..))")
private void publicTarget() {

}

@Around("publicTarget()")
public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {

    //..

}
```
* __@Around__ 어노테이션은 PointCut으로 `publicTarget()` 메소드를 설정했다. `publicTarget()` 메소드의   
  __@Pointcut__ 은 chap07 패키지나 그 하위 패키지에 속한 Bean객체의 public 메소드를 설정한다.
<hr/>

<h3>ProceedingJoinPoint 의 메소드</h3>

* Around Advice에서 사용할 공통 기능 메소드는 대부분 파라미터로 잔달받은 `ProceedingJoinPoint`의 `proceed()`   
  메소드만 호출하면 된다. 
* 호출되는 대상 객체에 대한 정보, 실행되는 메소드에 대한 정보, 메소드를 호출 할 때 전달된 인자에 대한 정보는   
  `ProceedingJoinPoint` 인터페이스가 제공하는 다음 메소드를 사용하여 알 수 있다.
  * `Signature getSignature()` : 호출되는 메소드에 대한 정보를 구한다.
  * `Object getTarget()` : 대상 객체를 구한다.
  * `Object[] getArgs()` : 파라미터 목록을 구한다.
* `org.aspectj.lang.Signature` 인터페이스는 다음 메소드들을 제공한다.
  * `String getName()` : 호출되는 메소드의 이름을 구한다.
  * `String toLongString()` : 호출되는 메소드를 완전하게 표현한 문장을 구한다. (리턴 타입, 파라미터 타입 모두 표시)
  * `String toShortString()` : 호출되는 메소드를 축약해서 표현한 문장을 구한다.(메소드의 이름만 표시)
<hr/>

<h2>Proxy의 생성 방식</h2>

* p.167