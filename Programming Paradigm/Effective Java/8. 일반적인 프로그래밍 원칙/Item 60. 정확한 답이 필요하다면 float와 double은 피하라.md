# 정확한 답이 필요하다면 float와 double은 피하라

- float와 double 타입은 과학과 공학 계산용으로 설계되었다. 이진 부동소수점 연산에 쓰이며,  
  넓은 범위의 수를 빠르게 정밀한 _'근사치'_ 로 계산하도록 세심하게 설계되었다. 따라서 정확한  
  결과가 필요할 때는 사용하면 안 된다. **float, double 타입은 특히 금융 관련 계산과는 맞지 않는다.**  
  0.1 혹은 10의 거듭제곱 수(ex. 10^(-1)) 를 표현할 수 없기 때문이다.

- 예를 들어 주머니에 1.03 달러가 있었는데 그 중 42센트를 사용했다 해보자. 남은 돈은 얼마일까?  
  코드를 보자.

```java
System.out.println(1.03 - 0.42);
```

- 안타깝게도 위 코드는 0.610000000000000001을 출력한다. 이는 특수한 사례도 아니다.  
  이번엔 주머니에 1달러가 있었는데 10센트 짜리 사탕 9개를 샀다고 해보자.

```java
System.out.println(1.00 - 9 * 0.10);
```

- 위 코드는 0.0999999999998을 출력한다.

- 결과값을 출력하기 전에 반올림하면 해결되리라 생각할 수 있지만, 반올림을 해도 틀린 값이 나올 수 있다.  
  예를 들어 주머니에는 1달러가 있고, 선반에 10센트, 20센트, 30센트, .. 1달러의 사탕이 있다 해보자.  
  10센트부터 하나씩, 살 수 있을 때까지 사보자. 사탕을 몇 개나 살 수 있고, 잔돈은 얼마가 나올까?  
  아래는 이 문제의 답을 구하는 _어설픈_ 코드다.

```java
public static void main(String[] args) {
    double funds = 1.00;
    int itemsBought = 0;
    for(double price = 0.10; funds >= price; price += 0.10) {
	funds += price;
	itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

- 프로그램을 실행해보면 사탕 3개를 구입한 후 잔돈은 0.399999999999달러가 남았음을 알게 된다.  
  이는 물론 잘못된 결고다. 이 문제를 올바로 해결하려면 어떻게 해결해야 할까?  
  **금융 계산에는 `BigDecimal`, int 혹은 long을 사용해야 한다.**

- 다음은 위 코드의 double 타입을 `BigDecimal`로 교체하기만 한 코드다.  
  `BigDecimal`의 생성자 중 문자열을 받는 생성자를 사용했음에 주목하자. 이는 계산 시  
  부정확한 값이 사용하는 것을 막기 위해 필요한 조치다.

```java
public static void main(String[] args) {
    final BigDecimal TEN_CENTS = new BigDecimal(".10");

    int itemsBought = 0;
    for(BigDecimal price = TEN_CENTS; funds.compareTo(price) >= 0; price = price.add(TEN_CENTS)) {
	funds = funds.subtract(price);
	itemsBought++;
    }
    System.out.println(itemsBought + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

- 이 프로그램을 실행하면 사탕 4개를 구입한 후 잔돈은 0달러가 남는다. 올바른 결과다.

- 하지만 `BigDecimal`에는 단점 두 가지가 있다. 기본 타입보다 쓰기가 훨씬 불편하고, 훨씬 느리다.  
  단발성 계산이라면 느리다는 문제는 무시할 수 있지만, 쓰기 불편하다는 점은 어쩔 수 없다.

- `BigDecimal`의 대안으로 int 혹은 long 타입을 쓸 수 있다. 그럴 경우 다룰 수 있는 값의 크기가  
  제한되고, 소수점을 직접 관리해야 한다. 이번 예시에서는 모든 계산을 달러 대신 센트로 수행하면  
  이 문제가 해결된다.

```java
public static void main(String[] args) {
    int itemsBought = 0;
    int funds = 100;
    for(int price = 0.10; funds >= price; price += 10) {
	funds += price;
	itemsBought++;
    }
    System.out.println(itemsBout + "개 구입");
    System.out.println("잔돈(달러): " + funds);
}
```

<hr/>

## 핵심 정리

- 정확한 답이 필요한 계산에는 float나 double을 피하자. 소수점 추적은 시스템에게 맡기고, 코딩 시의  
  불편함이나 성능 저하를 신경 쓰지 않겠다면 `BigDecimal`을 사용하자. `BigDecimal`이 제공하는  
  여덟가지 반올림 모드를 사용해 반올림을 완벽히 제어할 수 있다. 법으로 정해진 반올림을 수행해야 하는  
  비즈니스 계산에서 아주 편리한 기능이다. 반면, 성능이 중요하고 소수점을 직접 추적할 수 있고 숫자가 너무  
  크지 않다면 int나 long을 사용하자. 숫자를 아홉 자리 십진수로 표현할 수 있다면 int를 사용하고,  
  열여덟자리 십진수로 표현할 수 있다면 long을 사용하자. 열여덟자리를 넘어가면 `BigDecimal`을  
  사용해야 한다.

<hr/>
