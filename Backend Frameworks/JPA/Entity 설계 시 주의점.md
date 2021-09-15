# Entity 설계 시 주의점

### Entity Class에는 가급적이면 Setter 사용 지양

- Setter를 모두 열어두면 클래스 필드의 변경 포인트가 너무 많아  
  추후 유지보수하기에 어렵다.

### 모든 연관 관계는 지연 로딩 사용하기

- 즉시 로딩(`FetchType.EAGER`)은 예측이 어렵고, 어떤 쿼리문이 실행될지 추적하기 어렵다.  
  특히 JPQL 실행 시 N+1 문제가 자주 발생한다.
- 실무에서 모든 연관 관계는 `FetchType.LAZY`를 사용하자.
- 연관된 엔티티를 DB에서 조회해야 한다면 FETCH JOIN 또는 Entity Graph를 사용하자.
- `@XToOne`(ex. `@OneToOne`, `@ManyToOne`) 관계는 기본 설정 값이 `FetchType.EAGER`이므로  
  이를 직접 지연 로딩으로 설정해주자.

### 컬렉션은 필드에서 초기화하자.

- 컬렉션은 필드에서 바로 초기화하는게 안전하다.
  - null 문제에서 안전함.
  - Hibernate는 Entity를 영속화할때, 컬렉션을 감싸서 Hibernate가 제공하는 내장  
    컽렉션으로 바꾼다. 만약 임의의 메소드에서 컬렉션을 잘못 생성하면 Hibernate의  
    내부 메커니즘에서 문제가 발생할 수 있다. 따라서 필드 레벨에서 생성하는 것이  
    가장 안전하고, 코드도 간결하다.

```java
Member member = new Member();
System.out.println(member.getOrders().getClass();  // class java.util.ArrayList

entityManager.persist(member);
System.out.println(member.getOrders().getClass();  // class org.hibernate.collection.internal.PersistentBag
```

<hr/>
