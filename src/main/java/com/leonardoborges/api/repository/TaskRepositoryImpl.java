package com.leonardoborges.api.repository;

import com.leonardoborges.api.dto.TaskFilterRequest;
import com.leonardoborges.api.model.Task;
import com.leonardoborges.api.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TaskRepositoryImpl implements TaskRepositoryCustom {
    
    @PersistenceContext
    private final EntityManager entityManager;
    
    @Override
    public Page<Task> findTasksWithFilters(User user, TaskFilterRequest filters, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Task> query = cb.createQuery(Task.class);
        Root<Task> root = query.from(Task.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        if (user != null) {
            predicates.add(cb.equal(root.get("user"), user));
        }
        
        if (filters != null) {
            if (filters.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filters.getStatus()));
            }
            
            if (filters.getMinPriority() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("priority"), filters.getMinPriority()));
            }
            
            if (filters.getMaxPriority() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("priority"), filters.getMaxPriority()));
            }
            
            if (filters.getTitleContains() != null && !filters.getTitleContains().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("title")), 
                    "%" + filters.getTitleContains().toLowerCase() + "%"));
            }
            
            if (filters.getDescriptionContains() != null && !filters.getDescriptionContains().trim().isEmpty()) {
                predicates.add(cb.like(
                    cb.lower(root.get("description")), 
                    "%" + filters.getDescriptionContains().toLowerCase() + "%"));
            }
            
            if (filters.getCreatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), filters.getCreatedAfter()));
            }
            
            if (filters.getCreatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), filters.getCreatedBefore()));
            }
            
            if (filters.getUpdatedAfter() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedAt"), filters.getUpdatedAfter()));
            }
            
            if (filters.getUpdatedBefore() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("updatedAt"), filters.getUpdatedBefore()));
            }
            
            if (filters.getUserId() != null && user == null) {
                predicates.add(cb.equal(root.get("user").get("id"), filters.getUserId()));
            }
            
            if (filters.getIncludeDeleted() == null || !filters.getIncludeDeleted()) {
                predicates.add(cb.equal(root.get("deleted"), false));
            }
        } else {
            predicates.add(cb.equal(root.get("deleted"), false));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        
        if (pageable.getSort().isSorted()) {
            pageable.getSort().forEach(order -> {
                if (order.isAscending()) {
                    query.orderBy(cb.asc(root.get(order.getProperty())));
                } else {
                    query.orderBy(cb.desc(root.get(order.getProperty())));
                }
            });
        } else {
            query.orderBy(cb.desc(root.get("createdAt")));
        }
        
        TypedQuery<Task> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<Task> tasks = typedQuery.getResultList();
        
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Task> countRoot = countQuery.from(Task.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        Long total = entityManager.createQuery(countQuery).getSingleResult();
        
        return new PageImpl<>(tasks, pageable, total);
    }
}
