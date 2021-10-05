# 타입 안전 이종 컨테이너를 고려하라

- 제네릭은 `Set<E>`, `Map<K, V>` 등의 컬렉션과 `ThreadLocal<T>`, `AtomicReference<T>` 등의  
  단일 원소 컨테이너에도 흔히 쓰인다. 이런 모든 쓰임에서 매개변수화되는 대상은 원소가 아닌 컨테이너 자신이다.  
  따라서 하나의 컨테이너에서 매개변수화할 수 있는 타입의 수가 제한된다. 컨테이너의 일반적인 용도에 맞게  
  설계된 것이니 문제될 것은 없다. 예를 들어 `Set`에는 원소의 타입을 뜻하는 단 하나의 타입 매개변수만  
  있으면 되며, `Map`에는 key와 value의 타입을 뜻하는 2개만 필요한 식이다.

- 하지만 더 유연한 수단이 필요할 때도 종종 있다. 예를 들어, 데이터베이스의 행(row)는 임의 개수의 열(column)을  
  가질 수 있는데, 모두 열을 타입 안전하게 이용할 수 있다면 멋질 것이다. 다행이 쉬운 해법이 있다.  
  컨테이너 대신 key를 매개변수화한 다음, 컨테이너에 값을 넣거나 뺄 때 매개변수화한 key를 함께  
  제공하면 된다. 이렇게 하면 제네릭 타입 시스템이 값의 타입이 key와 같음을 보장해줄 것이다. 이러한 설계 방식을  
  **타입 안전 이종 컨테이너 패턴(Type safe Heterogeneous Container Pattern)** 이라고 한다.

- 간단한 예시로 타입벼로 즐겨 찾는 인스턴스를 저장하고 검색할 수 있는 `Favorites`클래스를 생각해보자.  
  각 타입의 `Class` 객체를 매개변수화한 key 역할로 사용하면 되는데, 이 방식이 동작하는 이유는 class의  
  클래스가 제네릭이기 때문이다. class 리터럴의 타입은 `Class`가 아닌 `Class<T>`이다.  
  예를 들어, `String.class`의 타입은 `Class<String>`이고, `Integer.class`의 타입은  
  `Class<Integer>`인 식이다. 한펴너, 컴파일타임 타입 정보와 런타임 타입 정보를 알아내기 위해  
  메소드들이 주고받는 class 리터럴을 **타입 토큰(Type Token)** 이라 한다.

- 아래 코드는 `Favorites` 클래스의 API로, 아주 단순하다.  
  key가 매개변수화 되었다는 점만 빼면 일반 `Map`처럼 보일 정도다.  
  클라이언트는 즐겨찾기를 저장하거나 얻어올 때 `Class` 객체를 알려주면 된다.

```java
public class Favorites {
    public <T> void putFavorite(Class<T> type, T instance);
    public <T> T getFavorite(Class<T> type);
}
```

- 그리고 다음은 위 `Favorites`를 사용하는 예시이다.  
  즐겨찾는 `String`, `Integer`, `Class` 인스턴스를 저장, 검색, 출력하고 있다.

```java
public class Client {
    public static void main(String[] args) {
	Favorites favorites = new Favorites();

	favorites.putFavorite(String.class, "Java");
	favorites.putFavorite(Integer.class, 0xcafebabe);
	favorites.putFavorite(Class.class, Favorites.class);

	String favoriteString = favorites.getFavorite(String.class);
	int favoriteInteger = favorites.getFavorite(Integer.class);
	Class<?> favoriteClass = favorites.getFavorite(Class.class);

	System.out.printf("%s %x %s%n", favoriteString, favoriteInteger, favoriteClass.getName());
	// Java cafebabe Favorites
    }
}
```

- `Favorites` 인스턴스는 타입 안전하다. `String`을 요청했는데 `Integer`를 반환하는 일은 절대 없다.  
  또한 모든 key의 타입이 제각각이라, 일반적인 `Map`과 달리 여러 가지 타입의 원소를 담을 수 있다.  
  따라서 `Favorites`는 타입 안전 이종 컨테이너라 할만하다.

- `Favorites`의 구현은 놀랍도록 간단한데, 아래 코드가 전부다.

```java
public class Favorites {
    private Map<Class<?>, Object> favorites = new HashMap<>();

    public <T> void putFavorite(Class<T> type, T instance) {
	favorites.put(Objects.requireNonNull(type), instance);
    }

    public <T> T getFavorite(Class<T> type) {
	return type.cast(favorites.get(type));
    }
}
```

- 위 코드에서는 미묘한 일들이 일어나고 있다. `Favorites`가 사용하는 private `Map` 변수인  
  favorites의 타입은 `Map<Class<?>, Object>`이다. 비한정적 와일드카드 타입이라 이 맵 안에  
  아무것도 넣을 수 없다 생각할 수 있지만, 사실은 그 반대다. **와일드카드 타입이 중첩되었다는 점**을  
  깨달아야 한다. 맵이 아니라 key가 와일드카드 타입인 것이다. 이는 모든 key가 서로 다른 매개변수화  
  타입일 수 있다는 뜻으로, 첫 번째는 `Class<String>`, 두 번째는 `Class<Integer>` 등이 될  
  수 있다. 다양한 타입을 지원하는 힘이 여기서 나온다.

- 그 다음으로 알아둘 점은 favorites 맵의 value 타입이 단순히 `Object`라는 것이다.  
  **이 맵은 key와 value 사이의 타입 관계를 보증하지 않는다**는 말이다. 즉, 모든 값이 key로  
  명시한 타입일 것이라 보장하지 않는다. 사실 Java의 타입 시스템에서는 이를 명시할 방법이 없다.  
  하지만 클라이언트는 이 관계가 성립함을 알고, 즐겨찾기를 검색할 때 그 이점을 누리게 된다.

- `putFavorite()`의 구현은 아주 쉽다. 주어진 `Class` 객체와 즐겨찾기 인스턴스를 favorites에  
  추가해 관계를 지으면 끝이다. Key와 value 사이의 _타입 링크(Type linkage)_ 정보는 버려진다.  
  즉, 그 value가 그 key 타입의 인스턴스라는 정보가 사라진다. 하지만 `getFavorite()`에서  
  이 관계를 되살릴 수 있으니 괜찮다.

- `getFavorite()`는 먼저 주어진 `Class` 객체에 해당하는 값을 favorites 맵에서 꺼낸다.  
  이 객체가 바로 반환해야할 객체가 맞지만, 잘못된 컴파일타임 타입을 가지고 있다. 이 객체의 타입은  
  favorites 맵의 value 타입인 `Object`이나, 우리는 이를 `T`로 바꿔 반환해야 한다.

- 따라서 `getFavorite()` 구현은 `Class#cast()`를 사용해 이 객체 참조를 `Class`객체가  
  가리키는 타입으로 동적 형변환한다.

- `cast()`는 형변환 연산자의 동적 버전이다. 이 메소드는 단순히 주어진 인수가 `Class`객체가 알려주는  
  타입의 인스턴스인지를 검사한 다음, 맞다면 그 인수를 그대로 반환하고, 아니면 `ClassCastException`을  
  던진다. 클라이언트 코드가 깔끔히 컴파일된다면 `getFavorite()`이 호출하는 `cast()`는  
  `ClassCastException`을 던지지 않을 것임을 우리는 알고 있다. 다시 말해 favorites 맵 안의 값은  
  해당 key의 타입과 항상 일치함을 알고 있다.

- 그런데 `cast()` 메소드가 단지 인수를 그대로 반환하기만 한다면 굳이 왜 사용하는 것일까?  
  그 이유는 `cast()` 메소드의 시그니처가 `Class` 클래스가 제네릭이라는 이점을 완벽하게 활용하기  
  때문이다. 아래 코드에서 보듯, `cast()`의 반환 타입은 `Class`객체의 타입 매개변수와 같다.

```java
public class Class<T> {
    //..
    T cast(Object obj);
}
```

- 이것이 정확히 `getFavorite()`에 필요한 기능으로, `T`로 비검사 형변환하는 손실 없이도 `Favorites`를  
  타입 안전하게 만드는 비결이다.

- 지금의 `Favorites` 클래스에는 알아두어야 할 제약이 두 가지 있다.  
  첫 번째, 악의적인 클라이언트가 `Class`객체를 제네릭이 아닌 raw 타입으로 넘기면 `Favorites` 인스턴스의  
  타입 안전성이 쉽게 깨진다. 하지만 이렇게 짜여진 클라이언트 코드에서는 컴파일할 때 비검사 경고가 뜰 것이다.  
  `HashSet`과 `HashMap`의 일반 컬렉션 구현체에도 똑같은 문제가 있다. 예를 들어, `HashSet`의  
  raw 타입을 사용하면 `HashSet<Integer>`에 `String`을 넣는 건 아주 쉬운 일이다.  
  그렇기는 하지만, 이 정도의 문제를 감수하겠다면 런타임 타입 안전성을 얻을 수 있다. `Favorites`가  
  타입 불변식을 어기는 일이 없도록 보장하려면 `putFavorite()` 메소드에서 인수로 넘어온 instance의  
  타입이 type으로 명시한 타입과 같은지 확인하면 된다. 그냥 동적 형변환을 쓰면 된다.

```java
public class Favorites {
    public <T> void putFavorite(Class<T> type, T instance) {
	favorites.put(Objects.requireNonNull(type), type.cast(instance));
    }
}
```

- `java.util.Collections`에는 `checkedSet()`, `checkedList()`, `checkedMap()` 등의  
  메소드들이 있는데, 바로 위 방식을 적용한 컬렉션 wrapper들이다. 이 정적 팩토리 메소드들은 컬렉션 혹은  
  `Map`과 함께 한개 혹은 두개의 `Class` 객체를 받는다. 이 메소드들은 모두 제네릭이라 `Class`객체와 컬렉션의  
  컴파일타임 타입이 같음을 보장한다. 또 이 wrapper들은 내부 컬렉션들을 실체화한다. 예를 들어 런타임에  
  `Coin`을 `Collection<Stamp>`에 넣으려 하면 `ClassCastException`을 던진다.  
  이 wrapper들은 제네릭과 raw 타입을 섞어 사용하는 애플리케이션에서 클라이언트 코드가 컬렉션에 잘못된  
  타입의 원소를 넣지 못하게 추적하는 데 도움을 준다.

- `Favorites` 클래스의 두 번째 제약은 실체화 불가 타입에는 사용할 수 없다는 것이다.  
  다시 말해, 즐겨 찾는 `String`이나 `String[]`은 저장할 수 있어도, 즐겨 찾는 `List<String>`은  
  저장할 수 없다는 뜻이다. `List<String>`을 저장하려는 코드는 컴파일되지 않을 것이다.  
  이는 `List<String>`용 `Class` 객체를 얻을 수 없기 때문이다. `List<String>.class`라고 쓰면  
  문법 오류가 난다. `List<String>`과 `List<Integer>`는 `List.class`라는 같은 `Class` 객체를  
  공유하므로, 만약 `List<String>.class`와 `List<Integer>.class`를 허용해서 둘 다 똑같은 타입의  
  객체 참조를 반환한다면 `Favorites` 객체의 내부는 아수라장이 될 것이다. 이 두번째 제약에 대한 완벽히  
  만족스러운 우회로는 없다.

- `Favorites`가 사용하는 타입 토큰은 비한정적이다. 즉, `getFavorite()`와 `putFavorite()`은  
  어떤 `Class` 객체든 받아들인다. 때로는 이 메소드들이 허용하는 타입을 제한하고 싶을 수 있는데,  
  한정적 타입 토큰을 활용하면 가능하다. 한정적 타입 토큰이란 단순히 한정적 타입 매개변수나 한정적  
  와일드카드를 사용하여 표현 가능한 타입을 제한하는 타입 토큰이다.

- 어노세이션 API는 한정적 타입 토큰을 적극적으로 사용한다. 예를 들어, 아래 코드는 `AnnotatedElement`  
  인터페이스에 선언된 메소드로, 이 메소드는 리플렉션의 대상이 되는 타입들, 즉 `java.lang.Class<T>`,  
  `java.lang.reflect.Method`, `java.lang.reflect.Field`와 같이 프로그램 요소를 표현하는  
  타입들에서 구현한다.

```java
public interface AnnotatedElement {
    //..

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);
}
```

- 여기서 annotationClass 인수는 어노테이션 타입을 뜻하는 한정적 타입 토큰이다.  
  이 메소드는 토큰으로 명시한 타입의 어노테이션이 대상 요소에 달려 있다면 그 어노테이션을 반환하고,  
  없다면 null을 반환한다. 즉, 어노테이션된 요소는 그 key가 어노테이션 타입인  
  타입 안전 이종 컨테이너인 것이다.

- `Class<?>` 타입의 객체가 있고, 이를 `getAnnotation()` 처럼 한정적 타입 토큰을 받는 메소드에  
  넘기려면 어떻게 해야 할까? 객체를 `Class<? extends Annotation>`으로 형변환할 수도 있지만,  
  이 형변환은 비검사이므로 컴파일 시 경고가 뜰 것이다. 운 좋게도 `Class` 클래스는 이런 형변환을  
  안전하게, 그리고 동적으로 수행해주는 인스턴스 메소드를 제공한다. 바로 `asSubclass()` 메소드로  
  호출된 인스턴스 자신의 `Class` 객체를 인수가 명시한 클래스로 형변환해준다.  
  (형변환 된다는 것은 이 클래스가 인수로 명시한 클래스의 하위 클래스라는 뜻이다.)  
  형변환에 성공하면 인수로 받은 클래스 객체를 반환하고, 실패하면 `ClassCastException`을 던진다.

- 아래 코드는 컴파일 시점에서는 타입을 알 수 없는 어노테이션을 `asSubclass()`를 사용해 런타임에서  
  읽어내는 예시이다. 이 메소드는 오류나 경고 없이 컴파일된다.

```java
public class SomeClass {
    static Annotation getAnnotation(AnnotatedElement element, String annotationTypeName) {
	Class<?> annotationType = null;
	try {
	    annotationType = Class.forName(annotationTypeName);
	} catch(Exception ex) {
	    throw new IllegalAgrumentException(ex);
	}
	return element.getAnnotation(annotationType.asSubclass(Annotation.class));
    }
}
```

- 참고로 `asSubclass()` 메소드는 아래처럼 구현되어 있다.

```java
package java.lang;

public final class Class<T> implements /*...*/ {
    //..

    @SuppressWarnings("unchecked")
    public <U> Class<? extends U> asSubclass(Class<U> clazz) {
        if (clazz.isAssignableFrom(this))
            return (Class<? extends U>) this;
        else
            throw new ClassCastException(this.toString());
    }
}
```

<hr/>

## 핵심 정리

- Collection API로 대표되는 일반적인 제네릭 형태에서는 한 컨테이너가 다룰 수 있는 타입 매개변수의 수가  
  고정되어 있다. 하지만 컨테이너 자체가 아닌 key를 타입 매개변수로 바꾸면, 이런 제약이 없는  
  **타입 안전 이종 컨테이너**를 만들 수 있다. 타입 안전 이종 컨테이너는 `Class`를 key로 쓰며,  
  이런 식으로 쓰이는 `Class` 객체를 타입 토큰이라 한다. 또한, 직접 구현한 key 타입도 쓸 수 있다.  
  예를 들어, 데이터베이스의 행(컨테이너)을 표현한 `DatabaseRow` 타입에는 제네릭 타입인  
  `Column<T>`를 key로 사용할 수 있을 것이다.

<hr/>
