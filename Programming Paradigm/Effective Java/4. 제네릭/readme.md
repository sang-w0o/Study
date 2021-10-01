# Generic

- 제네릭(Generic)은 Java5 부터 사용할 수 있다. 제네릭을 지원하기 전에는 컬렉션에서  
  객체를 꺼낼 때마다 형변환을 해야 했다. 그래서 누군가 실수로 엉뚱한 타입의 객체를  
  넣어두면 런타임에 형변환 오류가 나곤 했다. 반면, 제네릭을 사용하면 컬렉션이 담을 수 있는  
  타입을 컴파일러에 알려주게 된다. 그래서 컴파일러는 알아서 형변환 코드를 추가할 수 있고,  
  엉뚱한 타입의 객체를 넣으려는 시도를 컴파일 과정에서 차단하여 더 안전하고 명확한  
  프로그램을 만들어준다. 꼭 컬렉션이 아니더라도 이러한 이점을 누릴 수 있으나, 코드가  
  복잡해진다는 단점이 따라온다. 이번에는 제네릭의 이점을 최대로 살리고, 단점을 최소화하는  
  방법을 알아보자.

<hr/>

- 자주 사용되는 용어들

| 한글 용어                | 영문 용어               | 예시                      | 아이템 |
| :----------------------- | :---------------------- | :------------------------ | :----- |
| 매개변수화 타입          | parameterized type      | `List<String>`            | 26     |
| 실제 타입 매개변수       | actual type parameter   | `String`                  | 26     |
| 제네릭 타입              | generic type            | `List<E>`                 | 26, 29 |
| 정규 타입 매개변수       | formal type parameter   | `E`                       | 26     |
| 비한정적 와일드카드 타입 | unbounded wildcard type | `List<?>`                 | 26     |
| raw 타입                 | raw type                | `List`                    | 26     |
| 한정적 타입 매개변수     | bounded type parameter  | `List<E extends Number>`  | 29     |
| 재귀적 타입 한정         | recursive type bound    | `List<E extends List<E>>` | 31     |
| 한정적 와일드카드 타입   | bounded wildcard type   | `List<? extends Number>`  | 31     |
| 제네릭 메소드            | generic method          | `List<E>.add(E)`          | 30     |
| 타입 토큰                | type token              | `String.class`            | 33     |

<hr/>
