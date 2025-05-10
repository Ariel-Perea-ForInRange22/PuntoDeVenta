package com.puntoventa.controller;

import com.puntoventa.dao.CategoryDAO;
import com.puntoventa.dao.CategoryDAOImpl;
import com.puntoventa.dao.ProductDAO;
import com.puntoventa.dao.ProductDAOImpl;
import com.puntoventa.model.Category;
import java.util.List;

public class CategoryController {
    private CategoryDAO categoryDAO;
    private ProductDAO productDAO;

    public CategoryController() {
        this.categoryDAO = new CategoryDAOImpl();
        this.productDAO = new ProductDAOImpl();
    }

    public void addCategory(Category category) throws Exception {
        validateCategory(category);
        categoryDAO.insert(category);
    }

    public void updateCategory(Category category) throws Exception {
        validateCategory(category);
        categoryDAO.update(category);
    }

    public void deleteCategory(int id) throws Exception {
        if (productDAO.hasProductsInCategory(id)) {
            throw new Exception("No se puede eliminar la categoría porque tiene productos asociados. Por favor, primero reasigne o elimine los productos de esta categoría.");
        }
        categoryDAO.delete(id);
    }

    public Category getCategoryById(int id) throws Exception {
        return categoryDAO.findById(id);
    }

    public List<Category> getAllCategories() throws Exception {
        return categoryDAO.findAll();
    }

    private void validateCategory(Category category) throws Exception {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new Exception("El nombre de la categoría es requerido");
        }
    }
}