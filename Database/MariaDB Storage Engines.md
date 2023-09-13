# MariaDB Storage Engines

- MariaDB의 주요 storage engine으로는 InnoDB, ARIA, 그리고 MyISAM이 있다.  
  이들을 각자의 장점과 단점이 있으므로, 애플리케이션의 요구 사항에 맞는 적절한 엔진을 선택해야 한다.

## InnoDB

- InnoDB가 제공하는 기능들은 아래와 같다.

  - ACID 원칙을 구현한 트랜잭션 기능
  - Row-level locking: 갱신되는 row만 lock이 걸린다. 이는 동시적으로 다수의 read, write를 수행하는 애플리케이션의  
    성능을 향상시켜줄 수 있다.
  - Crash recovery: 장애가 발생했을 때, 일관성이 보장된 상태로 데이터베이스를 복구할 수 있다.

- InnoDB는 MariaDB에서 가장 많이 사용되는 storage engine으로 대부분의 애플리케이션에 적합하다.

---

## ARIA

- ARIA는 InnoDB와 유사한 트랜잭션 기능을 제공하지만, ACID 원칙을 구현하지 않았고, row-level locking은 되지만  
  crash recovery 기능은 제공하지 않는다. 따라서 InnoDB만큼 안정성이 확보되진 않으며, crash recovery가 필요 없는 애플리케이션에 적합하다.

---

## MyISAM

- MyISAM은 트랜잭션을 지원하지 않는 storage engine으로, 사용하기 쉬우며 빠른 처리 속도를 제공한다.  
  다만 crash recovery 또한 제공하지 않아 InnoDB, ARIA에 비해 안정성이 떨어진다. 이 storage engine은 트랜잭션이 필요 없는 애플리케이션에 적합하다.

|       기능        | InnoDB |  ARIA  | MyISAM |
| :---------------: | :----: | :----: | :----: |
|  ACID 원칙 준수   |   O    |   X    |   X    |
| Row-level locking |   O    |   O    |   X    |
|  Crash recovery   |   O    |   X    |   X    |
|       Speed       |  slow  |  fast  |  fast  |
|    Reliability    |  High  | Medium |  Low   |

---
