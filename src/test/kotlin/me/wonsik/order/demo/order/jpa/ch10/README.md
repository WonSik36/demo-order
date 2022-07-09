# 객체지향 쿼리 언어
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 조건식

### 타입 표현
| 종류      | 설명                                                                                   | 예시                                                                   |
|---------|--------------------------------------------------------------------------------------|----------------------------------------------------------------------|
| 문자      | 작은 따옴표<br>작은 따옴표를 표현하고 싶을시, 따옴표 연속 두개 사용                                             | 'Hello' 'She''s'                                                     |
| 숫자      | L(Long), D(Double), F(Float)                                                         | 10L, 10D, 10F                                                        |
| 날짜      | Date {d 'yyyy-m-dd'}<br>TIME {t 'hh-mm-ss'}<br>DATETIME {ts 'yyyy-mm-dd hh-mm-ss.f'} | {d '2012-03-04'}<br>{t '12-03-04'}<br>{ts '2012-03-04 10-11-11.123'} |
| Boolean | TRUE, FALSE                                                                          |                                                                      |
| Enum    | 패키지명을 포함한 전체 이름 사용                                                                   |                                                                      |
| 엔티티 타입  | 엔티티 타입을 표현. 주로 상속과 관련해서 사용                                                           | TYPE(m) = Member                                                     |

### 연산자 우선 순위
1. 경로 탐색 연산: .
2. 수학 연산: +, -, *, /
3. 비교 연산: =, >, <
4. 논리 연산: NOT, AND, OR

### BETWEEN, IN, LIKE, NULL 비교
* BETWEEN 식
  * X [NOT] BETWEEN A AND B
* IN 식
  * X [NOT] IN (예제|서브쿼리)
* LIKE 식
  * 문자표현식 [NOT] LIKE 패턴값 [ESCAPE 이스케이프문자]
  * %: 아무 값들이 입력되어도 됨 (없어도 됨)
  * _: 아무 값들이 입력되어도 됨 (한 글자는 있어야 함)
* NULL 비교식
  * { 단일값 경로 | 입력 파라미터 } IS [NOT] NULL

### 컬렉션 식
* 빈 컬렉션 비교식
  * { 컬렉션 값 연관 경로 } IS [NOT] EMPTY
* 컬렉션의 멤버 식
  * { 엔티티 | 값 } [NOT] MEMBER [OF] { 컬렉션 값 연관 경로 }

### 스칼라식
#### 문자함수
| 함수                                                   | 설명                                                  | 예제                                |
|------------------------------------------------------|-----------------------------------------------------|-----------------------------------|
| CONCAT(문자1,문자2,...)                                  | 문자를 합친다                                             | CONCAT('A','B') = 'AB'            |
| SUBSTRING(문자, 위치, [길이])                              | 위치부터 시작해 길이만큼 문자를 구한다                               | SUBSTRING('ABCDEF', 2, 3) = 'BCD' |
| TRIM([[LEADING or TRAILING or BOTH] [트림문자] FROM] 문자) | 트림 문자 양쪽(BOTH)에서 제거<br>LEADING: 왼쪽<br>TRAILING: 오른쪽 | TRIM(' ABC ') = 'ABC'             |
| LOWER(문자)                                            | 소문자로 변경                                             |                                   |
| UPPER(문자)                                            | 대문자로 변경                                             |                                   |
| LENGTH(문자)                                           | 길이                                                  |                                   |
| LOCATE(찾을 문자, 원본 문자, [검색 시작 위치])                     | 검색 위치부터 문자를 검색<br>1부터 시작, 못 찾으면 0 반환                | LOCATE('DE', 'ABCDEFG') = 4       |

#### 수학함수

| 함수                 | 설명          | 예제              |
|--------------------|-------------|-----------------|
| ABS(수학식)           | 절대값         |                 |
| SQRT(수학식)          | 제곱근         |                 |
| MOD(수학식, 나눌 수)     | 나머지         | MOD(4,3) = 1    |
| SIZE(컬렉션 값 연관 경로식) | 컬렉션 크기를 구한다 | SIZE(t.members) |


#### 날짜함수

* CURRENT_DATE: 현재 날짜
* CURRENT_TIME: 현재 시간
* CURRENT_TIMESTAMP: 현재 날짜 시간

하이버네이트에서 제공하는 기능
* YEAR(날짜 타입)
* MONTH(날짜 타입)
* DAY(날짜 타입)
* HOUR(날짜 타입)
* MINUTE(날짜 타입)
* SECOND(날짜 타입)

## 다형성 쿼리

### TYPE
* 조회 대상을 특정 자식 타입으로 한정할때 주로 사용
#### JPQL
```
select i from Item i where type(i) in (Album, Book)
```

#### SQL
```sql
select
    item0_.item_id as item_id2_2_,
    item0_.name as name3_2_,
    item0_.price as price4_2_,
    item0_1_.artist as artist1_0_,
    item0_2_.author as author1_1_,
    item0_.dtype as dtype1_2_ 
from
    item item0_ 
left outer join
    album item0_1_ 
        on item0_.item_id=item0_1_.item_id 
left outer join
    book item0_2_ 
        on item0_.item_id=item0_2_.item_id 
where
    item0_.dtype in (
        'A' , 'B'
    )
```

### TREAT
* 부모 타입을 특정 자식 타입으로 다룰때 사용
* **자식 타입**에 대해 쿼리하는게 나은듯

#### JPQL (TREAT)

```
select i from Item i where treat(i as Album).artist = 'artist' and i.name = 'album'
```

#### SQL (TREAT)

```sql
select
    item0_.item_id as item_id2_2_,
    item0_.name as name3_2_,
    item0_.price as price4_2_,
    item0_1_.artist as artist1_0_,
    item0_2_.author as author1_1_,
    item0_.dtype as dtype1_2_ 
from
    item item0_ 
inner join
    album item0_1_ 
        on item0_.item_id=item0_1_.item_id 
left outer join
    book item0_2_ 
        on item0_.item_id=item0_2_.item_id 
where
    item0_1_.artist='artist' 
    and item0_.name='album'
```

#### JPQL (자식 타입)

```
select a from Album a where a.artist = 'artist' and a.name = 'album'
```

#### SQL (자식 타입)

```sql
select
    album0_.item_id as item_id2_2_,
    album0_1_.name as name3_2_,
    album0_1_.price as price4_2_,
    album0_.artist as artist1_0_ 
from
    album album0_ 
inner join
    item album0_1_ 
        on album0_.item_id=album0_1_.item_id 
where
    album0_.artist='artist' 
    and album0_1_.name='album'
```