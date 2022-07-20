# 컬렉션과 부가 기능
> 김영한 님의 _자바 ORM 표준 JPA 프로그래밍_ (에이콘출판주식회사, 2020) 을 읽고 정리하였습니다.

## 컬렉션
* 하이버네이트는 엔티티를 영속 상태로 만들때, 컬렉션 필드를 래핑함
  * 원본 컬렉션이 null 이 아니여야함
* `Collection, List` 는 엔티티를 추가할때, 중복된 엔티티가 있는지 비교하지 않음
  * 따라서 엔티티를 추가해도 지연 로딩된 컬렉션을 초기화하지 않음
* `Set` 은 엔티티를 추가할때, 중복된 엔티티가 있는지 비교해야함
  * 따라서 엔티티를 추가할때, 지연로딩된 컬렉션을 초기화함

| 컬렉션 인터페이스           | 내장 컬렉션          | 중복 허용 | 순서 보관 | 권장하는 원본 컬렉션 |
|---------------------|-----------------|-------|-------|-------------|
| Collection, List    | PersistentBag   | O     | X     | ArrayList   |
| Set                 | PersistentSet   | X     | X     | HashSet     |
| List + @OrderColumn | PersistentList  | O     | O     | ArrayList   |

* `@OrderColumn` 의 경우, 단점이 존재하여 순서가 필요한 경우 직접 관리하는것을 권장

## @Converter
* `@Converter` 어노테이션 적용 및 `AttributeConverter` 인터페이스를 구현
* 필드 레벨, 클래스 레벨, 전역 레벨 모두 적용 가능
  * 필드 레벨, 클래스 레벨: `@Convert` 어노테이션 사용
  * 전역 레벨: `@Converter(autoApply = true)` 사용


## 리스너

## 엔티티 그래프

