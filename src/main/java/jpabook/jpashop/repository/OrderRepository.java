package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager em;

    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {

        String jpql = "select o from Order o join o.member m";

        return em.createQuery(jpql, Order.class)
                .setMaxResults(1000)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d ",
                Order.class
        ).getResultList();
    }

    public List<OrderSimpleQueryDto> findOrderDto() {
        //new 명령어를 사용해서 일반적인 SQL을 사용할 떄 처럼 원하는 값을 선택해서 조회
        //JPQL의 결과를 DTO 즉시 전환
        //네트워크 용량 최적화(생각보다 성능 차이 미비)
        //리포지토리 재사용성 떨어짐, API 스펙에 맞춘 코드가 리포지토리에 들어가는 단점
        return em.createQuery(
                        "select new jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto(o.id, m.name, o.orderDate, o.status, d.address)" +
                                " from Order o" +
                                " join o.member m" +
                                " join o.delivery d", OrderSimpleQueryDto.class)
                .getResultList();
    }

    public List<Order> findAllWithDelivery(int offset, int limit) {
        return em.createQuery(
                        "select o from Order o " +
                                "join fetch o.member m " +
                                "join fetch o.delivery d ", // N + 1 XToOne 관계는 데이터 로우 수가 증가하지 않으므로 페이징 자체는 문제 없음
                        Order.class
                ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }

    public List<Order> findAllWithItem() {
        return em.createQuery(
                "select o from Order o " +
                        "join fetch o.member m " +
                        "join fetch o.delivery d " + //XToOne 관계는 데이터 로우 수가 증가하지 않으므로 페이징 자체는 문제 없음
                        "join fetch o.orderItems oi " +// 지연 로딩 성능 최적화를 위해 hibernate.default_batch_fetch_size , @BatchSize 를 적용한다.
                        "join fetch oi.item i ",
                Order.class
        ).getResultList();
    }
}
