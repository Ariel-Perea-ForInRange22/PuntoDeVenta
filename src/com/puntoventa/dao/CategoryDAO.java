package com.puntoventa.dao;

import com.puntoventa.model.Category;
import java.util.List;

public interface CategoryDAO {
    void insert(Category category) throws Exception;
    void update(Category category) throws Exception;
    void delete(int id) throws Exception;
    Category findById(int id) throws Exception;
    List<Category> findAll() throws Exception;
}