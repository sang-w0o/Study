# 스트림은 주의해서 사용하라

- Stream API는 다량의 데이터 처리 작업(순차적, 병렬적 모두)을 돕고자 Java8에 추가되었다.  
  이 API가 제공하는 추상 개념 중 핵심은 두 가지다.

  - Stream은 데이터 원소의 유한 혹은 무한 시퀀스를 뜻한다.
  - Stream Pipeline은 이 원소들로 수행하는 연산 단계를 표현하는 개념이다.

- Stream의 원소들은 어디로부터든 올 수 있다. 대표적으로는 컬렉션, 배열, 파일, 정규표현식 패턴 matcher,  
  난수 생성기, 혹은 다른 스트림이 있다. 스트림 안의 데이터 원소들은 객체 참조나 기본 타입 값이다.  
  기본 타입 값으로는 int, long, double 이렇게 세 가지를 지원한다.

- 스트림 파이프라인은 Source Stream에서 시작해 종단 연산(Terminal Operation)으로 끝나며,  
  그 사이에 하나 이상의 중간 연산(Intermediate Operation)이 있을 수 있다.  
  각 중간 연산은 스트림을 어떠한 방식으로 변환(transform)한다. 예를 들어, 각 원소에 함수를 적용하거나  
  특정 조건을 만족 못하는 원소를 걸러낼 수 있다. 중간 연산들은 모두 한 스트림을 다른 스트림으로 변환하는데,  
  변환된 스트림의 원소 타입은 변환 전 스트림의 원소 타입과 같을 수도 있고 다를 수도 있다. 종단 연산은 마지막  
  중간 연산이 내놓은 스트림에 최후의 연산을 가한다. 원소를 정렬해 컬렉션에 담거나, 특정 원소 하나를 선택하거나,  
  모든 원소를 출력하는 식이다.

- 스트림 파이프라인은 지연 평가(Lazy Evaluation)된다. 평가는 종단 연산이 호출될 때 이뤄지며, 종단 연산에  
  쓰이지 않는 데이터 원소는 계산에 쓰이지 않는다. 이러한 지연 평가가 무한 스트림을 다룰 수 있게 해주는 열쇠다.  
  종단 연산이 없는 스트림 파이프라인은 아무 일도 하지 않는 명령어인 `no-op`와 같으니, 종단 연산을  
  빼먹는 일이 절대 없도록 하자.

- Stream API는 메소드 체이닝을 지원하는 Fluent API이다. 즉, 파이프라인 하나를 구성하는 모든 호출을  
  연결하여 단 하나의 표현식으로 완성할 수 있다. 파이프라인 여러 개를 연결해 하나로 만들 수도 있다.

- 기본적으로 Stream Pipeline은 순차적으로 수행된다. 파이프라인을 병렬로 실행하려면 파이프라인을 구성하는  
  스트림 중 하나에서 `parallel()` 메소드를 호출해주기만 하면 되나, 실제로 효과를 볼 수 있는 상황은  
  많지 않다.

- Stream API는 다재다능하여 사실상 어떠한 계산이라도 해낼 수 있다. 하지만 _할 수 있다_ 는 뜻이지,  
  _해야 한다_ 는 뜻은 아니다. 스트림을 제대로 사용하면 프로그램이 짧고 깔끔해지지만, 잘못 사용하면  
  읽기 어렵고 유지보수도 힘들어진다. 스트림을 언제 써야 하는지를 규정하는 확고부동한 규칙은 없지만,  
  참고할 만한 노하우는 있다.

- 아래 코드를 보자. 이 프로그램은 사전 파일에서 단어를 읽어 사용자가 지정한 문턱값보다 원소 수가 많은  
  아나그램(anagram) 그룹을 출력한다. 아나그램이란 철자를 구성하는 알파벳이 같고 순서만 다른 단어를 말한다.  
  이 프로그램은 사용자가 명시한 사전 파일에서 각 단어를 읽어 Map에 저장한다. Map의 key는 그 단어를  
  구성하는 철자들을 알파벳 순으로 정렬한 값이다. 즉, "staple"의 key는 "aelpst"가 되고,  
  "pastel"의 key 또한 "aelpst"가 되는 것이다. 따라서 이 두 단어는 아나그램이고, 아나그램끼리는  
  같은 key를 공유한다. Map의 값은 같은 key를 공유하는 단어들을 담은 집합이다. 사전 하나를 모두  
  처리하고 나면 각 집합은 사전에 등재된 아나그램들을 모두 담은 상태가 된다. 마지막으로 이 프로그램은  
  Map의 `values()` 메소드로 아나그램 집합들을 얻어 원소 수가 문턱값보다 많은 집합들을 출력한다.

```java
public class Anagrams {
    public static void main(String[] args) {
	File dictionary = new File(args[0]);
	int minGroupSize = Integer.parseInt(args[1]);

	Map<String, Set<String>> groups = new HashMap<>();
	try(Scanner s = new Scanner(dictionary)) {
	    while(s.hasNext()) {
		String word = s.next();
		groups.computeIfAbsent(alphabetize(word), (unused) -> new TreeSet<>()).add(word); // (1)
	    }
	}
	for(Set<String> group : groups.values()) {
	    if(group.size() > minGroupSize) {
		System.out.println(group.size() + ": " + group);
	    }
	}
    }

    private static String alphabetize(String word) {
	char[] chars = word.toCharArray();
	Arrays.sort(a);
	return new String(a);
    }
}
```

- 이 프로그램의 첫 번째 단계(`(1)`)에 주목하자. Map에 각 단어를 추가할 때 Java8에서 추가된  
  `computeIfAbsent()` 메소드를 사용했다. 이 메소드는 Map 안에 key가 있는지 찾은 다음, 있으면  
  단순히 그 key에 매핑된 value를 반환한다. key가 없다면 건네진 함수 객체를 key에 적용해 value를  
  계산해낸 다음 그 key와 value를 매핑해 놓고, 계산된 값을 반환한다. 이처럼 `computeIfAbsent()`를  
  활용하면 각 key에 다수의 value를 매핑하는 Map을 쉽게 구현할 수 있다.

- 다르게 구현한 프로그램을 살펴보자. 앞의 코드와 같은 일을 하지만 스트림을 과하게 활용한다.  
  사전 파일을 여는 부분만 제외하면 프로그램 전체가 단 하나의 표현식으로 처리된다.  
  사전을 여는 작업을 분리한 이유는 그저 try-with-resources 문을 사용해 사전 파일을 제대로 닫기 위함이다.

```java
public class Anagrams {
    public static void main(String[] args) {
	Path dictionary = Paths.get(args[0]);
	int minGroupSize = Integer.parseInt(args[1]);

	try(Stream<String> words = Files.lines(dictionary)) {
	    words.collect(
		groupingBy(word -> word.chars().sorted()
				.collect(StringBuilder::new,
				    (sb, c) -> sb.append((char) c),
				    StringBuilder::append).toString()))
	    .values().stream()
	    .filter(group -> group.size() >= minGroupSize)
	    .map(group -> group.size() + ": " + group)
	    .forEach(System.out::println);
	}
    }
}
```

- 코드를 이해하기 당연히 어려울 것이다. 이 코드는 확실히 짧지만 읽기는 어렵다. 특히 스트림에 익숙치 않은  
  프로그래머라면 더욱 그럴 것이다. 이처럼 **스트림을 과용하면 프로그램이 읽거나 유지보수하기 어려워진다.**

- 다행이 절충 지점이 있다. 아래 프로그램에서도 앞서의 두 프로그램과 기능은 같지만, 스트림을 적당히 사용했다.  
  그 결과 원래 코드보다 짧을 뿐만 아니라 명확하기까지 하다.

```java
public class Anagrams {
    public static void main(String[] args) {
	Path dictionary = Paths.get(args[0]);
	int minGroupSize = Integer.parseInt(args[1]);

	try(Stream<String> words = Files.lines(dictionary)) {
	    words.collect(groupingBy(word -> alphabetize(word)))
	        .values().stream()
		.filter(group -> group.size() >= minGroupSize)
		.forEach(group -> System.out.println(group.size() + ": " + g));
	}
    }

    // alphabetize()
}
```

- 스트림을 본 적이 없더라도 위 코드는 이해하기 쉬울 것이다. try-with-resources 블록에서  
  사전 파일을 열고, 파일의 모든 라인으로 구성된 스트림을 얻는다. 스트림 변수명을 words로 지어  
  스트림 안의 각 원소가 단어(word)임을 명확히 했다. 이 스트림 파이프라인에는 중간 연산은 없으며,  
  종단 연산에서는 모든 단어를 수집해 Map으로 모은다. 이 Map은 단어들을 아나그램끼리 묶어놓은  
  것으로, 앞선 두 프로그램이 생성한 Map과 실질적으로 같다. 그 다음으로 이 Map의 `values()`가  
  반환한 값으로부터 새로운 `Stream<List<String>>` 스트림을 연다. 이 스트림의 원소는 물론  
  아나그램의 리스트다. 그 리스트들 중 원소 개수가 minGroupSize보다 적은 것들은 필터링되어 무시된다.  
  마지막으로, 종단 연산인 `forEach()`는 살아남은 리스트를 출력한다.

> 람다 매개변수명은 주의해서 결정하자. **람다에서는 타입 이름을 자주 생략하므로 매개변수명을 잘**  
> **지어야 스트림 파이프라인의 가독성이 유지된다.**  
> 한편, 단어의 철자를 알파벳 순으로 정렬하는 로직은 별도 메소드인 `alphabetize()`로 분리했다.  
> 연산에 적절한 이름을 지어주고 세부 구현을 주 프로그램 로직 밖으로 빼내 전체적인 가독성을 높인 것이다.  
> **Helper 메소드를 적절히 활용하는 일의 중요성은 일반 반복 코드에서보다는 스트림 파이프라인에서 훨씬 크다.**  
> 파이프라인에서는 타입 정보가 명시되지 않거나 임시 변수를 자주 사용하기 때문이다.

- `alphabetize()` 메소드도 스트림을 사용해 다르게 구현할 수 있다. 하지만 그렇게 하면 명확성이  
  떨어지고, 잘못 구현할 가능성이 커진다. 심지어 느려질 수도 있다. Java가 기본 타입인 char용  
  스트림을 지원하지 않기 때문이다. char값들을 스트림으로 처리하는 코드를 보자.

```java
"Hello World!".chars().forEach(System.out::print);
```

- 위 코드의 결과는 "Hello World!"를 출력하는 것이 아니라, 숫자를 출력한다.  
  `"Hello World!".chars()`가 반환하는 스트림의 원소가 char가 아닌 int이기 때문이다.  
  따라서 정수값을 출력하는 `print()`가 호출된 것이다. 이처럼 이름이 chars인데 int 원소로 구성된  
  스트림을 반환하면 헷갈릴 수 있다. 올바른 값을 출력하게 하려면 명시적으로 형변환해줘야 한다.

```java
"Hello World!".chars().forEach(x -> System.out.println((char) x));
```

- 하지만 **char 값들을 처리할 때는 스트림을 사용하지 않는 편이 낫다.**

- 스트림을 처음 쓰기 시작하면 모든 반복문을 스트림으로 바꾸고 싶은 유혹에 빠지겠지만,  
  서두르지 않는게 좋다. 스트림으로 바꾸는게 가능할지라도 코드 가독성과 유지보수 측면에서는  
  손해볼 수도 있기 때문이다. 중간 정도 복잡한 작업에도(위의 아나그램 프로그램처럼) 스트림과  
  반복문을 적절히 조합하는 것이 최선이다. 그러니 **기존 코드는 스트림을 사용하도록 리팩토링하되,**  
  **새로운 코드가 더 나아 보일 때만 반영하자.**

- 이번 아이템에서 본 프로그램에서처럼 스트림 파이프라인은 되풀이되는 계산을 함수 객체(람다나 메소드 참조)로  
  표현한다. 반면 반복 코드에서는 코드 블록을 사용해 표현한다. 그런데 함수 객체로는 할 수 없지만, 코드  
  블록으로는 할 수 있는 일들이 있으니, 아래가 그 예시이다.

  - 코드 블록에서는 범위 안의 지역변수를 읽고 수정할 수 있다. 하지만 람다에서는 final이거나 사실상 final인  
    변수만 읽을 수 있고, 지역 변수를 수정하는 것은 불가능하다.

  - 코드 블록에서는 return문을 사용해 메소드에서 빠져나가거나, break나 continue문으로 블록 바깥의  
    반복문을 종료하거나 반복을 한번 더 건너뛸 수 있다. 또한 메소드 선언에 명시된 검사 예외를 던질 수 있다.  
    하지만 람다로는 이 중 어떤 것도 할 수 없다.

- 계산 로직에서 이상의 일들을 수행해야 한다면 스트림과는 맞지 않는 것이다.  
  반대로 아래 일들에는 스트림이 아주 안성맞춤이다.

  - 원소들의 시퀀스를 일관되게 변환한다.
  - 원소들의 시퀀스를 필터링한다.
  - 원소들의 시퀀스를 하나의 연산을 사용해 결합한다.(더하기, 연결하기, 최소값 구하기 등)
  - 원소들의 시퀀스를 컬렉션에 모은다.
  - 원소들의 시퀀스에서 특정 조건을 만족하는 원소를 찾는다.

- 이러한 일들 중 하나를 수행하는 로직이라면 스트림을 적용하기에 좋은 후보다.

- 한편, 스트림으로 처리하기 어려운 일도 있다. 대포적인 예로, 한 데이터가 파이프라인의 여러 단계(state)를  
  통과할 때 이 데이터의 각 단계에서의 값들에 동시에 접근하기는 어려운 경우다. 스트림 파이프라인은 일단 한  
  값을 다른 값에 매핑하고 나면 원래의 값은 잃는 구조이기 때문이다. 원래 값과 새로운 값의 쌍을 저장하는  
  객체를 사용해 매핑하는 우회 방법도 있겠지만, 그리 만족스러운 해법은 아닐 것이다. 매핑 객체가 필요한  
  단계가 여러 곳이라면 특히 더 그렇다. 이런 방식은 코드량도 많고 지저분하여 스트림을 쓰는 주목적에서  
  완전히 벗어난다. 가능한 경우라면, 앞 단계의 값이 필요할 때 매핑을 거꾸로 수행하는 방법이 나을 것이다.

- 예를 들어 처음 20개의 메르센 소수(Mersenne Prime)를 출력하는 프로그램을 작성해보자.  
  메르센 수는 `2^p - 1` 형태의 수다. 여기서 p가 소수이면 해당 메르센 수도 소수일 수 있는데,  
  이때의 수를 메르센 소수라 한다. 이 파이프라인의 첫 스트림으로는 모든 소수를 사용할 것이다.  
  아래 코드는 무한 스트림을 반환하는 메소드이다.

```java
static Stream<BigInteger> primes() {
    return Stream.iterate(BigInteger.TWO, BigInteger::nextProbablePrime);
}
```

- 메소드명 `primes()`는 스트림의 원소가 소수들로 구성됨을 알려준다. 스트림을 반환하는 메소드명은  
  이처럼 원소의 정체를 알려주는 복수 명사로 쓰자. 스트림 파이프라인의 가독성이 크게 좋아질 것이다.  
  이 메소드가 이용하는 `Stream.iterate()`라는 정적 팩토리는 매개변수를 2개 받는다.  
  첫 번째 매개변수는 스트림의 첫 번째 원소이고, 두 번째 매개변수는 스트림에서 다음 원소를 생성해주는 함수다.  
  이제 처음 20개의 메르센 소수를 출력하는 프로그램을 보자.

```java
public static void main(String[] args) {
    primes().map(p -> TWO.pow(p.intValueExact()).subtract(ONE))
        .filter(mersenne -> mersenne.isProbablePrime(50))
        .limit(20)
        .forEach(System.out::println);
}
```

- 위 코드는 앞서의 설명을 정직하게 구현했다. 소수들을 사용해 메르센 수를 계싼하고, 결과값이 소수인 경우에만  
  남긴 다음, 결과 스트림의 원소 수를 20개로 제한해놓고, 작업이 끝나면 결과를 출력한다.

- 이제 우리가 각 메르센 소수 앞에 지수(p)를 출력하길 원한다고 해보자.  
  이 값은 초기 스트림에만 나타나므로 결과를 출력하는 종단 연산에서는 접근할 수 없다.  
  하지만 다행이 첫 번째 중간 연산에서 수행한 매핑을 거꾸로 수행해 메르센 수의 지수를 쉽게 계산해낼 수 있다.  
  지수는 단순히 숫자를 이진수로 표현한 다음 몇 비트인지를 세면 나오므로, 종단 연산을 다음처럼 작성하면  
  원하는 결과를 얻을 수 있다.

```java
///..
    .forEach(mp -> System.out.println(mp.bitLength() + ": " + mp));
```

- 스트림과 반복 중 어느 쪽을 써야 할지 바로 알기 어려울 때도 많다. 카드 덱(Card Deck)을 초기화하는  
  작업을 생각해보자. 카드는 숫자(rank)와 무늬(suit)를 묶은 불변 값 클래스이고, 숫자와 무늬는 모두  
  열거 타입이라 하자. 이 작업은 두 집합의 원소들로 만들 수 있는 가능한 모든 조합을 계산하는 문제다.  
  수학자들은 이를 두 집합의 데카르트 곱이라 부른다. 아래는 for-each 반복문을 중첩해서 구현한 코드로,  
  스트림에 익숙치 않은 사람들에게 친숙한 방식일 것이다.

```java
private static List<Card> newDeck() {
    List<Card> result = new ArrayList<>();
    for(Suit suit : Suit.values()) {
	for(Rank rank : Rank.values()) {
	    result.add(new Card(suit, rank));
	}
    }
    return result;
}
```

- 아래는 위와 동일한 기능을 스트림으로 구현한 코드다. 중간 연산으로 사용한 `flatMap()`은 스트림의  
  원소 각각을 하나의 스트림으로 매핑한 다음, 그 스트림들을 다시 하나의 스트림으로 합친다.  
  이를 평탄화(flattening)라고도 한다. 이 구현에서는 중첩된 람다를 사용했음에 주의하자.

```java
private static List<Card> newDeck() {
    return Stream.of(Suit.values())
        .flatMap(suit ->
	    Stream.of(Rank.values())
	        .map(rank -> new Card(suit, rank)))
	.collect(toList());
}
```

- 결국 어느 코드가 더 좋아보이는지는 개인 취향과 프로그래밍 환경의 문제다.

<hr/>

## 핵심 정리

- 스트림을 사용해야 멋지게 처리할 수 있는 일이 있고, 반복 방식이 더 알맞은 일도 있다.  
  그리고 수많은 작업이 이 둘을 적절히 조합했을 때 가장 멋지게 해결된다. 어느 쪽을 선택하는  
  확고부동한 규칙은 없지만, 참고할만한 지침 정도는 있다. 어느 쪽이 더 나은지가 확연히  
  드러나는 경우가 많겠지만, 아니더라도 방법은 있다.

- **스트림과 반복 중 어느 쪽이 더 나은지 확신하기 어렵다면 둘 다 해보고 더 나은쪽을 선택하자.**

<hr/>
