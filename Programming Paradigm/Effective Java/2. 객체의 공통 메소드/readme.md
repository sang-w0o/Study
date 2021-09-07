# 객체의 공통 메소드

- `Object`는 객체를 만들 수 있는 구체 클래스이지만 기본적으로는 상속해서 사용하도록 설계되었다.  
  `Object`에서 final이 아닌 메소드(`equals()`, `hashCode()`, `toString()`, `clone()`, `finalize()`)는  
  모두 재정의(overriding)를 염두에 두고 설계된 것이라 재정의 시 지켜야할 일반 규칙이  
  명확히 정의되어 있다. 그래서 `Object`를 상속하는 클래스, 즉 모든 클래스는 이 메소드들을 일반적인  
  규약에 맞게 재정의해야 한다. 메소드를 잘못 구현하면 대상 클래스가 이 규약을 준수한다고 가정하는  
  클래스(`HashMap`, `HashSet` 등)를 오동작하게 만들 수 있다.

- 이번 장에서는 final이 아닌 `Object`의 메소드들을 언제, 어떻게 재정의해야 하는지를 다룬다.  
  그중 `finalize()` 메소드는 <a href="https://github.com/sang-w0o/Study/blob/master/Programming%20Paradigm/Effective%20Java/1.%20%EA%B0%9D%EC%B2%B4%20%EC%83%9D%EC%84%B1%EA%B3%BC%20%ED%8C%8C%EA%B4%B4/Item%208.%20finalizer%EC%99%80%20cleaner%20%EC%82%AC%EC%9A%A9%EC%9D%84%20%ED%94%BC%ED%95%98%EB%9D%BC.md">아이템 8</a>에서 다뤘으니 더 이상 언급하지 않는다.  
  `Comparable.compareTo`의 경우에는 `Object`의 메소드는 아니지만 성격이 비슷하여 함께 다룬다.

<hr/>
