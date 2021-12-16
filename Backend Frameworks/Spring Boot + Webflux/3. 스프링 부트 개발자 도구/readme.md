# 스프링 부트 개발자 도구

<h2>리액터 개발자 도구</h2>

- Reactor Flow(여러 가지로 조합된 리액터의 처리 과정) 중에서 무언가 잘못된다면  
  어떻게 대응할 것인가? 스택 트레이스를 찾아보면 될까?  
  아쉽지만 리액터 처리 과정은 일반적으로 여러 스레드에 걸쳐 수행될 수 있기 때문에  
  스택 트레이스를 통해 쉽게 확인할 수 없다. 리액터는 나중에 **구독**에 의해  
  실행되는 작업 흐름을 **조립**하는 비동기, 논블로킹 연산을 사용한다.

- 그렇다면 StackTrace를 출력하면 어떤 내용이 나올까? 작업 흐름 조립에 대한  
  내용이 나올까, 아니면 구독에 대한 내용이 나올까?

- 애플리케이션에서 리액터로 작성하는 일련의 연산은 앞으로 어떤 작업이 수행될지  
  기록해 놓은 조리법이라고 생각할 수도 있다. Spring 레퍼런스 문서에서는 이를  
  **조립**이라 부르며, 구체적으로는 람다 함수나 메소드 레퍼런스를 사용해서 작성한  
  명령 객체를 합쳐 놓은 것이라고 볼 수 있다. 조리법에 포함된 모든 연산은  
  하나의 스레드에서 처리될 수도 있다.

- 하지만 누군가가 구독하기 전까지는 아무런 일도 발생하지 않는다는 점을 잊지 말아야 한다.  
  조리법에 적힌 내용은 구독이 돼야 실행되기 시작하며, 리액터 플로우를 조립하는 데 사용된  
  스레드와 각 단계를 실제 수행하는 스레드가 동일하다는 보장은 없다.

- 한 가지 골치 아픈 사실이 있는데, Java StackTrace는 동일한 스레드 내에서만 이어지며,  
  스레드 경계를 넘지 못한다는 점이다. 멀티 스레드를 사용하는 환경에서 예외를 잡아서  
  스레드 경계를 넘어 전달하려면 특별한 조치를 취해줘야 한다. 이 문제는 **구독**하는  
  시점에 시작돼서 작업 흐름의 각 단계가 여러 스레드를 거쳐서 수행될 수 있는 리액티브 코드를  
  작성할 때는 훨씬 더 심각해진다.

- 리액터 프로젝트 리드인 Simon Basie가 작성한 코드를 보자.

```java
static class SimpleExample {
    public static void main(String[] args) {
	ExecutorService executor = Executors.newSingleThreadExecutor();

	List<Integer> source;
	if(new Random().nextBoolean()) {
	    source = IntStream.range(1, 11).boxed().collect(Collectors.toList());
	} else {
	    source = Arrays.asList(1, 2, 3, 4);
	}

	try {
	    executor.submit(() -> source.get(5)).get();
	} catch(InterruptedException | ExecutionException e) {
	    e.printStackTrace();
	} finally {
	    executor.shutdown();
	}
    }
}
```

- 리액터 기반이 아닌 이 명령형 코드는 `ExecutorService`를 생성하고, 긴 `List`와  
  짧은 `List`를 임의로 생성한 후에, `List`를 생성한 스레드가 아닌 다른 스레드에서  
  람다식을 통해 리스트의 5번째 원소를 추출한다.

- 위 코드는 성공 또는 실패의 두 가지 경로로 실행된다. 10개의 원소를 가진 `List`가  
  생성되면 5번째 원소를 추출하는 데 아무런 문제가 없을 것이고, 4개의 원소를 가진  
  `List`가 만들어진다면 `ArrayIndexOutOfBoundsException`가 발생할 것이다.

- 실패하는 경우의 StackTrace를 살펴보면, 우선 먼저 `ArrayIndexOutOfBoundsException`으로  
  시작하지만, 그 뒤의 내용들은 `java.util.concurrent` 호출로 가득하지만 그다지 의미있는  
  정보가 아니다. 바로 이 점이 문제이다.

- 스택 트레이스를 보면 `FutureTask`에서 `ExecutionException`이 발생했고, 원인은  
  `ArrayIndexOutOfBoundsException`이라 나와있다. 계속 내리다 보면 이 스레드를 시작한  
  지점에서 끝이 나고, 새 스레드 시작 전에 **어느 경로를 타고 리스트가 생성되었는지는 나오지 않는다.**  
  리스트에서 5번째 원소를 가져오는 스레드와 리스트를 생성한 스레드가 다르기 때문이다.

- 명령행 코드가 아니라 리액터 코드로 바꿔서 실행하면 어떻게 될까?

```java
static class ReactorExample {
    public static void main(String[] args) {
	Mono<Integer> source;
	if(new Random().nextBoolean()) {
	    source = Flux.range(1, 10).elementAt(5);
	} else {
	    source = Flux.just(1, 2, 3, 4).elementAt(5);
	}

	source
	    .subscribeOn(Schedulers.parallel())
	    .block();
    }
}
```

- 리액터로 작성된 코드는 랜덤하게 10개 또는 4개짜리 `Flux`를 생성하고,  
  `Flux.elementAt(5)`를 호출해서 5번째 원소를 포함하는 `Mono`를  
  반환한 후에 `subscribeOn(Schedulers.parallel())`를 호출하여 리액터 플로우가  
  여러 스레드에서 병렬 실행된다.

- 위 코드는 명령형 코드와 내용상 동일하며, 스택 트레이스가 스레드 경계를 넘지  
  못한다는 것을 보여주기 위한 코드이다. 실행해보면 `IndexOutOfBoundsException`이 발생한다.

- 리액터로 작성한 코드를 시행해도 스택 트레이스에 많은 내용이 출력되지만,  
  **최초의 문제 발생 지점인 `Flux` 생성 지점까지 출력하지 못하므로** 큰 의미가 없는 정보다.

- 이 문제는 리액터가 아니라, Java의 스택 트레이스 처리에서 기인하는 문제다.  
  리액터는 스택 트레이스를 통해 가능한 한 가장 먼 곳까지 따라가지만 다른 스레드의 내용까지  
  쫓아가지는 못한다. **이 한계를 극복해주는 것이 바로 리액터의 `Hooks.onOperatorDebug()`이다.**

```java
static class ReactorExample {
    public static void main(String[] args) {

	Hooks.onOperatorDebug()

	Mono<Integer> source;
	if(new Random().nextBoolean()) {
	    source = Flux.range(1, 10).elementAt(5);
	} else {
	    source = Flux.just(1, 2, 3, 4).elementAt(5);
	}

	source
	    .subscribeOn(Schedulers.parallel())
	    .block();
    }
}
```

- `Hooks.onOperatorDebug()`를 호출해서 리액터의 BackTracing을  
  활성화한 것 외에는 기존 코드와 같다.

- 실행해보면 `Exception in thread "main"` 부분까지는 기존과 마찬가지이지만,  
  그 이후에 오류 관련 핵심 정보가 출력된다. 이번 스택 트레이스에서는 아래의 정보까지  
  확인할 수 있다.

  - (1) `Flux.elementAt(5)`에서 발생한 첫 번째 오류는 `Flux.just(1,2,3,4)`와 연결돼 있다.
  - (2) 후속 오류는 `Mono.subscribeOn(..)`에서 발생했다.

- `Hooks.onOperatorDebug()`만 호출했을 뿐인데 스택 트레이스에서  
  의미 있는 정보가 출력되므로 오류를 찾기가 훨씬 쉬워졌다.

- 이 기능은 정말 혁신적이라 할 수 있다. 프로젝트 리액터는 오류 관련 핵심 정보를 스레드  
  경계를 넘어서 전달할 수 있는 방법을 만들어냈다. 리액터 자체로는 JVM이 지원하지 않으므로  
  스레드 경계를 넘을 수 없지만, `Hooks.onOperatorDebug()`를 호출하면 리액터가  
  처리 흐름 조립 시점에서의 호출부 세부정보를 수집하고 구독해서 실행하는 시점에  
  세부 정보를 넘겨준다.

- 프로젝트 리액터는 완전한 비동기, 논블로킹을 위한 도구다. 리액터 플로우에 있는 모든  
  연산은 다른 스레드에서 실행될 수도 있다. 리액터 개발자 도구 없이 개발자 스스로가  
  리액터 플로우 처리 정보를 스레드마다 넘겨주도록 구현하려면 엄청난 비용이 들 것이다.  
  리액터는 확장성 있는 애플리케이션을 만들 수 있는 수단을 제공함과 동기에 개발자의  
  디버깅을 돕는 도구도 함께 제공한다.

> 리액터가 스레드별 스택 세부정보를 스레드 경계를 넘어서 전달하는 과정에는 굉장히 많은  
> 비용이 든다. 아마도 이런 비용 이슈가 Java에서 스레드 경계를 넘어 정보를 전달하는  
> 것을 기본적으로 허용하지 않는 첫 번째 이유일 것이다. 따라서 성능 문제를 일으킬 수  
> 있으므로 실제 운영환경 또는 실제 벤치마크에서는 `Hooks.onOperatorDebug()`를  
> 절대로 호출해서는 안된다. 버그를 추적할 다른 방법이 없어서 운영 환경에서  
> `Hooks.onOperatorDebug()`를 꼭 호출해야 한다면, 반드시 적절한 조건을 사용해서  
> `Hooks.onOperatorDebug()`가 해당 조건을 만족할 때만 실행되게 해야 한다.  
> 리액터 개발자 도구에 운영환경에 영향을 가장 적게 미치는 도구가 있는지 지속적으로  
> 살펴보는 것도 중요하다.

<hr/>

<h2>리액터 플로우 로깅</h2>

- 리액터에서는 실행 후에 디버깅하는 것 외에 실행될 때 로그를 남길 수도 있다.  
  리액터 코드에 `log.debug()`를 사용해보면 전통적인 명령행 코드와는 달리  
  원하는 대로 출력하는게 쉽지 않다는 것을 알 수 있다. 이런 이슈는 리액티브 스트림이  
  비동기라는 특성 때문이 아니라, 리액터가 적용한 함수형 프로그래밍 기법에서 비롯된  
  문제다. 람다 함수나 메소드 레퍼런스를 사용하면 `log.debug()`문을 사용할 수 있는  
  위치에 제한을 받는다. 아래 코드를 보자.

```java
return itemRepository.findById(id)
    .map(Item::getPrice);
```

- 위 코드에서 리액터의 `map()` 연산은 `Item::getPrice`라는 메소드 레퍼런스를  
  이용해서 `Item` 객체에서 price 값을 얻는다.

- Java8을 사용하면 위와 같은 코드는 아주 보편적으로 사용된다.  
  뭔가 심오한 내용을 알아야만 쓸 수 있는 특별한 코드가 아니며, 많은 개발자들에 의해  
  사용되는 표준 Java 코드이다.

- Java8의 메소드 레퍼런스를 이용하면 코드를 간결하게 줄일 수 있으며,  
  많은 라이브러리에서 메소드 레퍼런스를 점점 더 활발히 사용해서 개발자도 단순하고  
  간결한 코드를 작성할 수 있게 하고 있다. 하지만 로그를 찍으려면 아래와 같이 작성해야  
  하므로 간결함을 잃게 된다.

```java
return itemRepository.findById(id)
    .map(item -> {
	log.debug("Found Item");
	return item.getPrice();
    });
```

- 메소드 레퍼런스를 썼다면 훨씬 간결했을 코드가 여러 행의 람다식을 사용하는  
  장황한 코드로 바뀌었다. 로그를 찍게 하려면 이렇게 하는 수 밖에 없다.

- 짧고 가독성 좋은 코드를 장황한 코드로 바꾸면, 코드를 읽는 데 많은 비용이 든다.  
  가독성에 대한 견해는 주관적일 수 있지만, Java8의 Stream API, 프로젝트 리액터  
  플로우나 Optional을 사용해보면 공감할 수 있을 것이다. 사용하면 할수록 메소드 레퍼런스와  
  반환값이 없는 람다를 사용해서 더 가볍고 좋은 Java 코드를 작성하는 새로운 방식을  
  점점 더 좋아하게 된다.

- 그런데 그저 로깅 때문에 간결한 코드를 쓸 수 없다는건 여러모로 심각한 문제다.  
  다행히 리액터는 이 문제에 대한 해법을 제시한다. 리액터 플로우 중에  
  어느 단계에 있는지 알고 싶다면 아래의 코드를 추가하면 된다.

```java
Mono<Cart> addItemToCart(String cartId, String itemId) {
    return this.cartRepository.findById(cartId)
        .log("Found Cart")
	.defaultIfEmpty(new Cart(cartId))
	.log("Empty Cart")
	.flatMap(cart -> cart.getCartItems().stream()
	    .filter(cartItem -> cartItem.getItem().getId().equals(itemId))
	    .findAny()
	    .map(cartItem -> {
		cartItem.increment();
		return Mono.just(cart).log("New Cart Item");
	    })
	    .orElseGet(() -> {
		return this.itemRepository.findById(itemId)
		    .log("Fetched Item")
		    .map(item -> new CartItem(item))
		    .log("cartItem")
		    .map(cartItem -> {
			cart.getCartItems().add(cartItem);
			return cart;
		    }).log("Added Cart Item");
	    }))
	.log("Cart with another item")
	.flatMap(cart -> this.cartRepository.save(cart))
	.log("Saved Cart");
}
```

- 위 코드에서는 여러 리액터 연산 사이에 `log(..)`문이 여러 개 있는 것이  
  눈에 띄는데, 각 `log(..)`는 각기 다른 문자열을 포함하고 있다.

- 위 메소드를 실행해보면 `log()`에 인자로 넘겨진 문자열 토큰과 함께  
  리액티브 스트림 시그널이 모두 로그에 출력된다.

- 여러 번 강조했지만, **구독하기 전까지는 아무것도 실행되지 않는다.**  
  구독은 리액터 플로우에서 가장 마지막에 발생하는 일이지만, 신기하게도 로그에서는 맨 위에  
  `savedCart : | onSubscribe()`와 같이 표시된다.

- 로그를 계속 보면 리액터 플로우는 대체로 소스 코드상 맨 아래에 있는 것부터 시작해서  
  위로 올라가면서 실행되고, 소스 코드에서 위에 있던 내용이 로그에서는 아래에 출력된다.

- 이는 모든 요청과 구독 흐름은 아래에서 시작되어서 위로 흘러가기 때문이다.  
  그리고 데이터가 준비되면 이번에는 반대로 `onNext()`와 `onComplete()` 시그널이  
  위에서 아래로 발생한다. 그래서 결국 마지막에는 `savedCart : | onComplete()`로 끝난다.

> `log()`의 연산에 의해 출력되는 로그 수준의 기본값은 `INFO`이다. 하지만  
> 이 메소드의 두 번째 인자로 `LEVEL`을 넘겨주면, 원하는 로그 수준으로 출력할 수 있다.  
> 세 번째 인자로 리액티브 스트림의 Signal을 넘겨주면, 특정 신호에 대한 로그만 출력할 수도 있다.

<hr/>

<h2>블록하운드를 이용한 블로킹 코드 검출</h2>

- 리액티브 프로그래밍은 블로킹 API를 한 번 호출하면 아무 소용이 없게 된다.  
  단 한 코드로 시스템이 걷잡을 수 없도록 느려지는 위험을 이대로 방치해도 될까?

- 절대 안되며, 블로킹 코드가 소스코드에 어디에도 없고, 관련 설정도 적절하다는 것을  
  알아야한다. 이를 위해 `BlockHound`를 사용할 수 있다.

- 블록하운드는 리액터 개발팀에 소속된 Java 챔피언인 Sergei Egorov에 의해 개발되었다.  
  이 라이브러리는 개발자가 직접 작성한 코드 뿐만 아니라 서드파티 라이브러리에 사용된  
  블로킹 메소드 호출을 모두 찾아내서 알려주는 Java Agent이다. 블록하운드는 JDK 자체에서  
  호출되는 블로킹 코드까지도 찾아낸다.

- 블록하운드를 사용하기 위해서는 다음 의존성을 추가해주면 된다.

```gradle
//..

dependencies {
    //..
    implementation("io.projectreactor.tools:blockhound:1.0.3.RELEASE")
}
```

- 블록하운드는 그 자체로는 아무 일도 하지 않는다. 하지만 애플리케이션에 적절하게  
  설정되면 Java Agent API를 이용해서 블로킹 메소드를 검출하고, 해당 스레드가  
  블로킹 메소드 호출을 허용하는지 검사할 수 있다. Spring Boot의 생명주기에  
  블록하운드를 등록해보자.

```java
public class Application {

    public static void main(String[] args) {
        BlockHound.install();
        SpringApplication.run(Application.class, args);
    }
}
```

- `BlockHound.install()`이 `SpringApplication.run()`보다 앞에  
  위치하고 있는 것을 눈여겨 보자. 이렇게 하면 Spring Boot 애플리케이션을  
  시작할 때 블록하운드가 바이트코드를 조작(instrument)할 수 있게 된다.

- 블록하운드에는 여러 옵션이 있다. 특정 블로킹 호출을 문제로 인식하지 않도록  
  허용 리스트에 등록할 수도 있고, 특정 부분을 블로킹으로 인지되도록 금지 리스트에  
  등록할 수도 있다. 예를 들어, Thymeleaf 템플릿을 블로킹 방식으로 읽는 것은  
  수용 가능하다고 판단되면 이 부분을 허용 리스트에 추가해서 블록하운드가  
  이 부분을 무시하게 할 수 있다. 그렇다면 구체적으로 무엇을 허용해야 할까?

- 예를 들어, `FileInputStream.readBytes()` 메소드는 JDK 소스 코드를 보면 일부가  
  C언어로 구현된 네이티브 메소드임을 알 수 있다. 이 메소드를 허용 리스트에 추가할 수 있지만,  
  이렇게 너무 저수준의 메소드를 허용하는 것은 좋지 않다. JDK에 포함되어 있는 이 메소드를  
  어디에서 호출하는지 전부 파악하지 않은 상태에서 이 메소드 호출을 허용 리스트에 추가하면,  
  누군가 무책임하게 이 메소드를 호출하는 부분을 검출해낼 수 없게 되며, 결국 시스템의  
  위험 요소로 남게 된다.

- 범용적으로 사용되는 JDK 메소드를 허용해서 무분별하게 블로킹 코드가 사용되는 위험을  
  감수하지 말고, 허용 범위를 좁혀서 좀 더 구체적인 일부 지점만 허용하는 것이 안전하다.

```java
public class Application {

    public static void main(String[] args) {
	// Thymeleaf 템플릿 읽는 부분을 허용
	BlockHound.builder()
	    .allowBlockingCallsInside(
		TemplateEngine.class.getCanonicalName(), "process"
	    ).install();
        SpringApplication.run(Application.class, args);
    }
}
```

<hr/>
