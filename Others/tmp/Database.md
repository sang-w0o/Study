## 5. Indices

### 1. 기본 개념

- Index는 데이터 검색 속도를 높이기 위해 사용된다.

- Index file(index entry)은 `<search-key, pointer>`의 형태로 이뤄진다.
- 2 가지의 인덱스 종류가 존재한다.

  - Ordered index: Search key가 정렬되어 있다.
  - Hash index: Search key들이 정렬된 것이 아니라, bucket들에 분산되어 있다.

- Index를 사용한 검색은 동등 연산 검색(exact match query), 범위 검색(range query)을 지원한다.

- Ordered index

  - Ordered index에서 index entry들은 search key의 값을 기준으로 정렬되어 있다.
  - Primary index(clustering index): Search key가 ordered index의 정렬 순서와 동일하게 data record를 정렬
  - Secondary index(non-clustering index): Search key가 ordered index의 정렬 순서와 다르게 data record를 정렬

- Clustering index이든 non-clustering index이든 index 자체는 정렬되어 있다.  
  단지 clustering index는 data record가 정렬된 기준이 ordered index와 동일할 뿐이다.

- Dense index: Index record가 file 내의 모든 search key 값에 대해 존재한다.  
  즉, 모든 record에 대한 pointer를 가진다.

- Sparse index: Dense index와 달리 search key 값들의 일부에 대해서만 index record를 가진다.  
  특히 record들이 search key를 기준으로 순차적으로 정렬되어 있을 때만 사용된다.  
  다시 말해 sparse index는 정렬되어 있는 data file에 대해서만 적용 가능하다.

- Multilevel index: 쉽게 말해 index에 대한 index이다.

  - Disk에 있는 primary index를 순차적인 file로 취급해 해당 index에 sparse index를 만든 것
  - Outer index: Primary index file에 대한 sparse index
  - Innder index: Primary index file

- Secondary index

  - Secondary index는 dense index여야만 한다.(spare index가 될 수 없다.)

- Primary vs secondary index

  - Index를 사용하면 데이터 검색을 빠르게 처리할 수 있지만, index를 update하는 것은 추가적인 overhead를 유발한다.
  - Primary index를 사용한 순차적 scan은 효율적이지만, secondary index를 사용한 순차적 scan은 효율적이지 않다.

### 2. B+ Tree index

- B+ Tree:

  - B+ tree:

    - Root 에서 모든 각각의 leaf까지의 거리는 항상 동일하다.(balanced)
    - Root 또는 leaf가 아닌 node는 `n ~ [2/n]`개의 child를 가질 수 있다.
    - Leaf node는 `[(n-1)/2] ~ n-1`개의 data record를 가질 수 있다.
    - B+ tree의 node 안에 있는 search key들은 정렬되어 있다.
    - Nonleaf node들은 spare index, leaf node는 dense index이다.

      ![picture 15](../../images/TMP_DB_1.png)

  ![picture 16](../../images/TMP_DB_2.png)
  ![picture 17](../../images/TMP_DB_3.png)
  ![picture 18](../../images/TMP_DB_4.png)
  ![picture 19](../../images/TMP_DB_5.png)
  ![picture 20](../../images/TMP_DB_6.png)

- Non-unique search keys

  - Non-unique search key: 동일한 key 값을 가지는 tuple이 2개 이상 존재하는 경우
  - Non-unique search key의 삭제는 비싸다.

### 3. B+ Tree variations

- B+ tree file organization

  - B+ tree file organization에서 leaf node들은 pointer가 아닌 실제 record를 가진다.

    ![picture 21](../../images/TMP_DB_7.png)

  - B+ tree index와 동일하게 삽입, 삭제 연산이 이뤄진다.

- Bulk loading

  - 대량의 entry를 한 번에 B+ tree에 삽입하는 것은 비효율적이다. 아래의 대안들이 있다.
    - Entry들을 먼저 정렬하고 정렬된 순서대로 insert
    - Entry들을 먼저 정렬하고 leaf level부터 layer-by-layer로 tree 생성(bottom-up)

- Indices on mutiple attributes

  - Composite search key

### 4. Static hashing

- Hash function

  - Bucket은 하나 이상의 record를 저장하는 기본 단위이다.
  - Hash function `h`는 모든 search-key들의 집합인 `k`에 대해 `B`개의 bucket들의 집합에 대응시킨다.

- Bucket overflow

  - Bucket overflow는 아래의 원인들로 발생할 수 있다.
    - Bucket 개수의 부족함
    - Skew in distribution of records
  - Bucket overflow의 발생 빈도를 줄일 수도 있어도, 완전히 없앨 수는 없다.
  - Overflow bucket들을 활용해 대처할 수 있다.

- Overflow chaining: Overflow bucket들이 linked list에 함께 chaining되어 있다.
- Open hashing: Overflow bucket을 사용하지 않으며, database에는 적합하지 않다.

- Hash indices

  - Hash index는 항상 secondary index이다.  
    왜냐하면 hash value의 순서가 실제 record의 순서와 아무런 관계가 없기 때문이다.

- Static hashing

  - Hash function `h`는 모든 search-key들의 집합인 `k`에 대해 `B`개의 bucket들의 집합에 대응시킨다.
  - Database의 크기는 커지고 작아지므로 bucket underflow, overflow가 많이 발생한다.
  - Bucket 개수가 동적으로 변경될 수 있도록 하는 것이 좋다.

### 5. Dynamic hashing

- Dynamic hashing

  - 크기가 커지고 작아지는 database 등에 적합하다.
  - Hash function 자체도 동적으로 수정될 수 있다.

- Extendable hashing

  - Hash function이 매우 큰 범위의 값들을 생성한다.
  - 항상 bucket 주소를 찾기 위해 prefix 만을 사용한다.
    - Prefix가 `i` bit라고 하면 database 크기에 따라 `i`가 동적으로 변경된다.
  - Bucket의 수도 bucket의 coalesing, splitting에 의해 동적으로 변경된다.

    ![picture 22](../../images/TMP_DB_8.png)

  - Search key `K`가 담길 bucket을 찾기 위해 아래의 과정이 발생한다.
    - (1) `h(K) = X` 계산
    - (2) X의 앞 `i` bit를 prefix로 가지는 bucket address를 찾아 해당 bucket에 저장한다.

- Insert 과정: p.168~172

- Extendable hashing의 장단점

  - 장점
    - Hash performance는 시간이 지나도 저하되지 않는다.
    - Minimal space overhead
  - 단점
    - Extra level of indirection to find the desired record
    - Bucket address table 자체가 매우 커질 수 있다.

- Ordered indexing vs hashing

  - Hashing: 일반적으로 key의 특정 값을 검색할 때 빠르다.
  - Ordered index: Range query에 빠르다.

### 6. Bitmap indices

- Bitmap index는 여러 개의 key들에 대한 query를 효율적으로 수행하기 위해 사용된다.
- Bitmap은 모든 record에 대해 유일한 번호를 설정하고, 각 bit에 해당 recrod의 적용 여부를 표시한다.

  ![picture 23](../../images/TMP_DB_9.png)

- 여러 개의 속성들에 대한 query를 수행할 때 효율적이다.
- 원본 데이터의 크기에 비해 bitmap의 크기는 매우 작다.

---

## 6. Query Processing

- Steps in query processing

  - Parsing and translation
  - Optimization
  - Evaluation

- Query optimization

  - 하나의 query는 여러 개의 relational algebra expression으로 변환될 수 있다.
  - 각 relational algebra expression들은 다르게 평가될 수 있다.
  - 따라서 연산의 비용을 계산하고, 가장 적은 비용을 가지는 expression을 선택한다.

- Measurements of query cost

  - Query 비용은 대부분 disk access 횟수에 의해 좌우된다.
  - 아래 용어를 사용하자.
    - $t_T$: Time to transfer 1 block
    - $t_s$: Time for 1 seek
    - B block의 transfer와 S번의 seek 비용: $B * t_T + S * t_s$

- Selection operation

  - A1(linear search)

    - Scan each file block and test all records to see whether thy satisfy the selection condition
    - Cost estimate: $b_r * t_T + t_S$
      - 모든 block들이 물리적으로 인접하다고 가정
      - $b_r$ : Number of blocks of relation $r$
    - 만약 selection이 key attribute에 대한 것이라면 cost는 $1/2 * b_r * t_T + t_S$가 된다.
    - Linear search는 언제든지 적용 가능하다.

  - A2(primary index, equality on key)

    - 동등 조건을 만족하는 하나의 record 반환
    - Cost: $(h_i + 1) * (t_T + t_s)$
      - $h_i$ : Index의 height

  - A3(primary index, equality on non-key)

    - 동등 조건을 만족하는 모든 record 반환
    - Cost: $h_i * (t_T + t_s) + t_s + t_T * b$
      - $b$ : 조건을 만족시키는 record들을 포함한 block 개수

  - A4(secondary index, equality on non-key)

    - search key가 candidate key이면 단일 record 반환
      - cost: $(h_i + 1) * (t_T + t_s)$
    - search key가 candidate key가 아니면 여러 개의 record 반환
      - cost: $(h_i + n) * (t_T + t_s)$
      - $n$ : 조건을 만족시키는 record의 개수

  - A5(primary index, comparison)

    - 값 크기 비교 연산
    - 조건을 만족하는 tuple을 찾고, 해당 tuple부터 linear scan
    - Index를 사용한다면 index 기준으로 linear scan
      - Linear file scan이 index를 사용하는 것보다 더 빠를 수 있다.

### 3. Sorting

- External sort-merge

  -
