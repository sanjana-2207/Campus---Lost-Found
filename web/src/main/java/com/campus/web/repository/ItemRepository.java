package com.campus.web.repository;

import com.campus.web.model.Item;
import com.campus.web.model.ItemStatus;
import com.campus.web.model.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByTypeAndStatusInOrderByCreatedAtDesc(ItemType type,
                                                         Collection<ItemStatus> statuses);

    List<Item> findByTypeOrderByCreatedAtDesc(ItemType type);

    List<Item> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    long countByType(ItemType type);

    long countByStatus(ItemStatus status);

    long countByStatusIn(Collection<ItemStatus> statuses);

    @Query("select i.status, count(i) from Item i group by i.status")
    List<Object[]> countGroupedByStatus();

    @Query("select i.category.name, count(i) from Item i "
            + "group by i.category.name order by count(i) desc, i.category.name")
    List<Object[]> countGroupedByCategory();

    @Query("select year(i.eventDate), month(i.eventDate), i.type, count(i) "
            + "from Item i group by year(i.eventDate), month(i.eventDate), i.type "
            + "order by year(i.eventDate), month(i.eventDate)")
    List<Object[]> countGroupedByMonthAndType();
}
