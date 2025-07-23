package com.example.backend.dao;

import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class GenericRepositoryImpl<T, ID> implements GenericRepository<T, ID> {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<T> findAll(Class<T> clazz) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(clazz);
        Root<T> root = cq.from(clazz);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    @Override
    public T find(Class<T> clazz, ID id) {
        return em.find(clazz, id);
    }

    @Override
    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public T update(T entity) {
        return em.merge(entity);
    }

    @Override
    public void delete(Class<T> clazz, ID id) {
        T existing = em.find(clazz, id);
        if (existing != null) {
            em.remove(existing);
        }
    }


}
