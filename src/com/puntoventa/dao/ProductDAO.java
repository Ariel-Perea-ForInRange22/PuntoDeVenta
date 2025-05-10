package com.puntoventa.dao;

import com.puntoventa.model.Product;
import java.util.List;

public interface ProductDAO {
    void insert(Product product) throws Exception;
    void update(Product product) throws Exception;
    void delete(int id) throws Exception;
    Product findById(int id) throws Exception;
    Product findByCode(String code) throws Exception;
    List<Product> findAll() throws Exception;
    List<Product> findByName(String name) throws Exception;
    void updateStock(int productId, int newStock) throws Exception;
    boolean hasProductsInCategory(int categoryId) throws Exception;
    boolean isProductInSales(int productId) throws Exception;
}