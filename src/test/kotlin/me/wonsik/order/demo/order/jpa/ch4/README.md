# 엔티티 매핑
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## `@Entity`


* 기본 생성자가 있어야함 (`public` or `protected`)
* `final` 클래스, `enum`, `interface`, `inner` 클래스에 사용할 수 없다. 
* 저장할 필드에 `final` 을 사용해서는 안된다.

## `@Table`
| 속성                | 설명                     | 기본값       |
|-------------------|------------------------|-----------|
| name              | 매핑할 테이블 이름             | 엔티티 이름 사용 |
| uniqueConstraints | DDL 생성 시에 유니크 제약 조건 생성 |           |
| indexes           | DDL 생성 시에 인덱스 생성       |           |

## `hibernate.ddl-auto` 속성
| 옵션          | 설명                                                 |
|-------------|----------------------------------------------------|
| create      | 기존 테이블을 삭제하고 새로 생성한다. DROP + CREATE                |
| create-drop | 기존 테이블을 삭제하고 새로 생성한뒤, 종료시에 테이블을 제거한다               |
| update      | 데이터베이스 테이블과 엔티티 매핑정보를 비교해서 변경사항만 수정                |
| validate    | 데이터베이스 테이블과 엔티티 매핑정보를 비교해서 차이가 있으면 경고를 남기고 실행하지 않음 |
| none        | 유효하지 않은 옵션을 주어, ddl-auto 사용하지 않도록 함                |

## 기본 키 매핑
### 직접 할당
* 기본 키를 애플리케이션에서 직접 할당
* `@GeneratedValue` 어노테이션을 주지 않는 경우
* 자바 기본형보다 **자바 래퍼형**을 추천

### 자동 생성

#### `IDENTITY` 
* 기본 키 생성을 데이터베이스에 위임
* 기본 키 값을 얻어오기 위해 `em.persist()` 를 호출하는 즉시 INSERT 쿼리가 실행된다.
* ex) MySQL, PostgreSQL, SQL Server

#### `SEQUENCE`
* 데이터베이스 시퀀스를 사용해서 기본 키 할당
* ex) Oracle, PostgreSQL, H2

1. DB 시퀀스를 사용해서 식별자 조회 
2. 해당 식별자 엔티티에 저장 
3. 트랜잭션 커밋시, DB 에 저장 

`@SequenceGenerator`

| 속성             | 설명                                | 기본값                |
|----------------|-----------------------------------|--------------------|
| name           | 식별자 생성기 이름                        | 필수                 |
| sequenceName   | 데이터베이스에 등록되어 있는 시퀀스 이름            | hibernate_sequence |
| initialValue   | DDL 생성시에만 사용. 시퀀스 DDL 을 생성할때, 초기값 | 1                  |
| allocationSize | 시퀀스 한번 호출에 증가하는 수                 | 50                 |


#### `TABLE`
* 키 생성 테이블 사용
* 모든 데이터베이스에 적용 가능

`@TableGenerator`

| 속성              | 설명                | 기본값                 |
|-----------------|-------------------|---------------------|
| name            | 식별자 생성기 이름        | 필수                  |
| table           | 키 생성 테이블 명        | hibernate_sequences |
| pkColumnName    | 시퀀스 컬럼명           | sequence_name       |
| valueColumnName | 시퀀스 값 컬럼명         | next_val            |
| pkColumnValue   | 키로 사용할 값 이름       | 엔티티 이름              |
| initialValue    | 초기값               | 0                   |
| allocationSize  | 시퀀스 한번 호출에 증가하는 수 | 50                  |

### 권장하는 식별자 선택 전략
자연 키 보다 대리 키를 사용할것을 권장

## 필드와 컬럼 매핑

`@Column`

| 속성                    | 설명                           | 기본값                  |
|-----------------------|------------------------------|----------------------|
| name                  | 필드와 매핑할 테이블의 컬럼 이름           | 객체의 필드 이름            |
| insertable            | 엔티티 저장시 false 인 경우, 저장하지 않음  | true                 |
| updatable             | 엔티티 수정시 false 인 경우, 수정하지 않음  | true                 |
| table                 | 하나의 엔티티를 두개 이상의 테이블에 매핑할때 사용 | 현재 클래스가 매핑된 테이블      |
| nullable(DDL)         | DDL 생성시 사용                   | true                 |
| unique(DDL)           | 한개 컬럼에 unique 제약 조건을 걸때 사용   |                      |
| columnDefinition(DDL) | 데이터베이스 컬럼 정보 직접 전달           |                      |
| length(DDL)           | 문자 길이 제약 조건                  | 255                  |
| precision, scale(DDL) | BigDecimal 타입에서 사용           | precision=0, scale=0 |

`@Enumerated`

| 속성    | 설명                                                                      | 기본값               |
|-------|-------------------------------------------------------------------------|-------------------|
| value | EnumType.ORDINAL: enum 순서를 DB 에 저장<br>EnumType.STRING: enum 이름을 DB 에 저장 | EnumType.ORDINAL  |

`@Lob`
* 필드 타입이 문자면 CLOB 으로 매핑
* 아닌 경우, BLOB 으로 매핑

`@Transient`
* 해당 필드는 매핑하지 않음

`@Access`

| 속성    | 설명                                                         | 기본값               |
|-------|------------------------------------------------------------|-------------------|
| value | AccessType.FIELD: 필드에 직접 접근<br>AccessType.PROPERTY: 접근자 사용 | @Id 의 위치를 기준으로 결정 |