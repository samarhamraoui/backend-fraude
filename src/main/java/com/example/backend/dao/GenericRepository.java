package com.example.backend.dao;

import org.springframework.stereotype.Repository;

import java.util.List;
public interface GenericRepository<T, ID> {
    List<T> findAll(Class<T> clazz);
    T find(Class<T> clazz, ID id);
    T save(T entity);
    T update(T entity);
    void delete(Class<T> clazz, ID id);
}