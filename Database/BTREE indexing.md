# BTREE indexing

- BTREE index는 큰 data block을 처리하는 방식으로, 검색 작업을 더욱 쉽고 빠르게 할 수 있게 해준다.  
  데이터베이스의 BTREE index는 B+tree 자료구조를 사용해 구현된다.  
  B+tree는 각 node가 데이터를 ascending order로 정렬된 형태로 저장한다.  
  여기서 tree의 node는 1개 이상의 key를 가진다.
  각 key는 2개의 자식 node에 대한 참조를 가지며, 왼쪽 자식 node는 자신보다 작은 key를, 오른쪽 자식 node는 자신보다 큰 key를 가진다.

---

## B+tree의 주요 특징

- B+tree는 데이터를 leaf node에만 저장한다는 것이다.  
  아래는 B+tree의 예시이다.

  ![picture 1](/images/DB_BTREE_INDEXING_1.png)

- 이렇게 record는 leaf node에만 저장되고, 각 leaf node는 다음 record에 대한 참조를 가지게 된다.  
  따라서 데이터베이스는 BTREE index를 통해 binary search를 수행하거나, leaf node만을 탐색하면서  
  순차 탐색을 수행할 수 있다.

---

- [참고 문서](https://builtin.com/data-science/b-tree-index)
