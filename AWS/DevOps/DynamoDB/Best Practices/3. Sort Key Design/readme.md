# Best Practices for Using Sort Keys to Organize Data

- 잘 설계된 Sort Key를 사용하면, 아래의 2가지 대표적인 장점이 생긴다.

  - 쿼리를 효율적으로 할 수 있도록 연관된 데이터를 한 곳에 모아둘 수 있다.  
    잘 설계한다면 `begins_with`, `between`, `<`, `>` 등 연관된 item들을 범위를 정해 쿼리할 수도 있다.

  - Composite Sort Key는 one-to-many 연관 관계의 데이터들을 원하는 대로 쿼리할 수 있다.  
    예를 들어 지리적 위치를 나열하는 table에서 Sort Key를 아래처럼 설계할 수도 있을 것이다.

  ```
  [country]#[region]#[state]#[county]#[city]#[neighborhood]
  ```

  - 이렇게 설계된 Sort Key를 사용하면 `country`부터 `neighborhood`까지 어느 단계에서든 효율적으로 쿼리할 수 있다.

## Using Sort Keys for Version Control

- 많은 애플리케이션에서는 다양한 이유로 item 단위로 변경 내역을 추적해야 할 경우가 많다.  
  이를 Sort Key의 prefix로 구현하는 효과적인 설계 패턴을 보자.

  - 각 item에 대해 2개의 복사본을 만든다. 하나의 복사본은 `v0_`과 같이 첫 번째 version number를 포함한 값을 Sort Key의 prefix로 가지며,  
    다른 하나의 복사본은 그 다음 `v1_`과 같이 version number를 포함한 값을 Sort Key의 prefix로 가진다.

  - item이 업데이트될 때마다 업데이트된 item을 다음 version number를 포함한 값을 Sort Key의 prefix로 갖도록 생성한다.  
    그리고 업데이트된 item을 첫 번째 item(`v0_`으로 시작하는 Sort Key)으로 복사한다.  
    이렇게 하면 가장 최근의 item은 항상 `v0_` prefix로 찾을 수 있게 된다.

- 예를 들어, 부품 제조 공장은 아래의 스키마를 사용할 수도 있을 것이다.

![picture 9](/images/SORT_KEY_VERSION_CONTROL.png)

- 위 예시 스키마의 데이터를 보면 `Equipment_1` item은 다양한 Auditor들에 의해 수정된다.  
  수정된 결과는 새로운 item `v1`부터 하나씩 증가하는 version number를 갖고 있다.

- 수정이 완료되면 애플리케이션 단에서는 zero-version item(`v0_Audit`)의 내용을 최신 상태로 바꾼다.

- 이렇게 해서 만약 가장 최근의 item 상태를 필요로 한다면, Sort Key로 prefix `v0_`으로 쿼리하면 된다.

- 만약 애플리케이션에서 item의 전체 수정 기록을 파악해야 한다면, Partition Key와 함께 모든 item을 조회하되,  
  Sort Key value가 `v0_` prefix를 갖는 item은 제외하도록 하면 된다.

---
