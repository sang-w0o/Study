<h1> 현재 테스트코드 문제점</h1>

* `Warehouses` Entity class를 저장하는 `WarehousEntityFactory#create()`
```java
public class WarehouseEntityFactory {
    private Warehouses create(String accessToken, WarehouseStatus status, MainItemType[] mainItemTypes) {
        int userId = JwtTokenUtil.extractUserId(accessToken);

        Warehouses warehouse = Warehouses.builder()
            .userId(userId)
            .name(NAME)
            .space(SPACE)
            .address(ADDRESS)
            .addressDetail(ADDRESS_DETAIL)
            .description(DESCRIPTION)
            .availableWeekdays(AVAILABLE_WEEKDAYS)
            .openAt(OPEN_AT)
            .closeAt(CLOSE_AT)
            .availableTimeDetail(AVAILABLE_TIME_DETAIL)
            .cctvExist(CCTV_EXISTS)
            .doorLockExist(DOOR_LOCK_EXIST)
            .airConditioningType(AIR_CONDITIONING_TYPE)
            .workerExist(WORKER_EXIST)
            .canPark(CAN_PARK)
            .warehouseType(WAREHOUSE_TYPE)
            .minReleasePerMonth(MIN_RELEASE_PER_MONTH)
            .latitude(LATITUDE)
            .longitude(LONGITUDE)
            .status(status)
            .build();

        List<MainItemTypes> m = Arrays.stream(mainItemTypes)
            .map(mainItemType -> new MainItemTypes(mainItemType, warehouse))
            .collect(Collectors.toList());

        warehouse.getMainItemTypes().addAll(m);

        List<Insurances> insurances = Arrays.stream(new String[]{"INSURANCE1", "INSURANCE2", "INSURANCE3"})
                .map(Insurances::new).collect(Collectors.toList());
        warehouse.setInsurances(insurances);

        List<SecurityCompanies> securityCompanies = Arrays.stream(new String[]{"SEC_COMP1", "SEC_COMP2", "SEC_COMP3"})
                .map(SecurityCompanies::new).collect(Collectors.toList());
        warehouse.setSecurityCompanies(securityCompanies);

        return warehousesRepository.save(warehouse);
    }
}
```

* 위 코드에서 `Insurances`와 `SecurityCompanies`를 저장하는 부분은 새로 추가된 부분이다.
* 하지만 위 메소드를 사용하는 테스트 케이스는 모두 실패한다. 아래는 로그.
```
2021-01-05 16:55:34.512 DEBUG 15948 --- [    Test worker] org.hibernate.SQL                        : insert into warehouses (created_at, last_modified_at, address, address_detail, air_conditioning_type, available_time_detail, available_weekdays, can_park, cctv_exist, close_at, description, door_lock_exist, latitude, longitude, min_release_per_month, name, open_at, space, status, user_id, warehouse_type, worker_exist) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
2021-01-05 16:55:34.514 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [TIMESTAMP] - [2021-01-05T16:55:34.472]
2021-01-05 16:55:34.515 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [2] as [TIMESTAMP] - [2021-01-05T16:55:34.472]
2021-01-05 16:55:34.515 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [3] as [VARCHAR] - [ADDRESS]
2021-01-05 16:55:34.516 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [4] as [VARCHAR] - [ADDRESS_DETAIL]
2021-01-05 16:55:34.516 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [5] as [VARCHAR] - [BOTH]
2021-01-05 16:55:34.517 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [6] as [VARCHAR] - [AVAILABLE_TIME_DETAIL]
2021-01-05 16:55:34.518 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [7] as [INTEGER] - [1]
2021-01-05 16:55:34.520 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [8] as [BOOLEAN] - [true]
2021-01-05 16:55:34.520 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [9] as [BOOLEAN] - [true]
2021-01-05 16:55:34.521 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [10] as [VARCHAR] - [18:00]
2021-01-05 16:55:34.521 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [11] as [VARCHAR] - [DESCRIPTION]
2021-01-05 16:55:34.522 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [12] as [BOOLEAN] - [true]
2021-01-05 16:55:34.523 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [13] as [DOUBLE] - [22.2]
2021-01-05 16:55:34.524 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [14] as [DOUBLE] - [22.2]
2021-01-05 16:55:34.524 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [15] as [INTEGER] - [1000]
2021-01-05 16:55:34.525 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [16] as [VARCHAR] - [NAME]
2021-01-05 16:55:34.525 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [17] as [VARCHAR] - [09:00]
2021-01-05 16:55:34.525 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [18] as [INTEGER] - [123]
2021-01-05 16:55:34.526 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [19] as [VARCHAR] - [VIEWABLE]
2021-01-05 16:55:34.526 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [20] as [INTEGER] - [1443]
2021-01-05 16:55:34.526 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [21] as [VARCHAR] - [THREEPL]
2021-01-05 16:55:34.527 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [22] as [BOOLEAN] - [true]
2021-01-05 16:55:34.535 DEBUG 15948 --- [    Test worker] org.hibernate.SQL                        : insert into insurances (name) values (?)
2021-01-05 16:55:34.536 TRACE 15948 --- [    Test worker] o.h.type.descriptor.sql.BasicBinder      : binding parameter [1] as [VARCHAR] - [INSURANCE1]
2021-01-05 16:55:34.564  WARN 15948 --- [    Test worker] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 1364, SQLState: HY000
2021-01-05 16:55:34.564 ERROR 15948 --- [    Test worker] o.h.engine.jdbc.spi.SqlExceptionHelper   : Field 'warehouse_id' doesn't have a default value
```

* 이때, `Insurances`와 `SecurityCompanies`를 추가하는 코드를 제거하면 테스트 케이스는 모두 통과한다.   
  둘 다 `Warehouses`와 같은 관계를 가진다고 생각했는데, 차이점은 아래와 같다.

```java
@Entity
@Table(name = "main_item_types")
public class MainItemTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MainItemType type;

    @ManyToOne
    @JoinColumn(name = "warehouse_id")
    private Warehouses warehouse;

    @Builder
    public MainItemTypes(MainItemType mainItemType, Warehouses warehouse) {
        this.type = mainItemType;
        this.warehouse = warehouse;
    }
}

@Entity
public class Insurances {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @Builder
    public Insurances(String name) {
        this.name = name;
    }
}

@Entity
@Table(name = "security_companies")
public class SecurityCompanies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 50, nullable = false)
    private String name;

    @Builder
    public SecurityCompanies(String name) {
        this.name = name;
    }
}
```

* `SecurityCompanies`도 `Insurances`와 마찬가지로 1:N 양방향 연관에 대한 매핑이 되어 있지 않다.   
  `MainItemTypes`에만 양방향으로 1:N 연관 관계를 설정해 놓은 이유는 다른 관계와 다르게   
  `MainItemTypes`로만 `Warehouses`를 조회해야하는 경우가 있기 때문이다.

* 혹시나 해서 `Insurances`, `SecurityCompanies`를 저장하는 부분을 지우고, `DeliveryTypes`를 저장하는 로직을 추가했다.
```java
List<DeliveryTypes> deliveryTypes = Arrays.stream(new String[]{"DELIVERY1", "DELIVERY2", "DELIVERY3"}).map(DeliveryTypes::new).collect(Collectors.toList());
warehouse.setDeliveryTypes(deliveryTypes);
```

* 참고로 `DeliveryTypes`가 매핑된 것은 아래와 같다. `Insurances`와 마찬가지로 1:N 단방향 연관 관계이다.
```java
@Table(name = "delivery_types")
public class DeliveryTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 30, nullable = false)
    private String name;

    @Builder
    public DeliveryTypes(String name) {
        this.name = name;
    }
}
```

* 상식적으로 생각하면 `DeliveryTypes`를 저장하는 코드도 정상적으로 수행되지 않아야 한다.   
  하지만 테스트는 통과했고 로그를 보니 쿼리도 적절하게 잘 수행되었다.

