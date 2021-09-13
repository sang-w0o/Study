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
