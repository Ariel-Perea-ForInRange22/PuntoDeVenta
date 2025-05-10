package com.puntoventa.controller;

import com.puntoventa.dao.ProductDAO;
import com.puntoventa.dao.ProductDAOImpl;
import com.puntoventa.model.Product;
import java.util.List;

public class ProductController {
    private ProductDAO productDAO;

    public ProductController() {
        this.productDAO = new ProductDAOImpl();
    }

    public void addProduct(Product product) throws Exception {
        validateProduct(product);
        productDAO.insert(product);
    }

    public void updateProduct(Product product) throws Exception {
        validateProduct(product);
        productDAO.update(product);
    }

    public void deleteProduct(int id) throws Exception {
        Product product = productDAO.findById(id);
        if (product == null) {
            throw new Exception("Producto no encontrado");
        }
        
        if (productDAO.isProductInSales(id)) {
            throw new Exception("No se puede eliminar el producto '" + product.getName() + "' porque está asociado a ventas existentes. Para mantener el historial de ventas, considere desactivar el producto en lugar de eliminarlo.");
        }
        
        productDAO.delete(id);
    }

    public Product getProductById(int id) throws Exception {
        return productDAO.findById(id);
    }

    public Product getProductByCode(String code) throws Exception {
        return productDAO.findByCode(code);
    }

    public List<Product> getAllProducts() throws Exception {
        return productDAO.findAll();
    }

    public List<Product> searchProducts(String query) throws Exception {
        return productDAO.findByName(query);
    }    public void updateProductStock(int productId, int newStock) throws Exception {
        if (newStock < 0) {
            throw new Exception("El stock no puede ser negativo");
        }
        productDAO.updateStock(productId, newStock);
    }

    private void validateProduct(Product product) throws Exception {
        if (product.getCode() == null || product.getCode().trim().isEmpty()) {
            throw new Exception("El código del producto es requerido");
        }
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new Exception("El nombre del producto es requerido");
        }
        if (product.getPrice() == null || product.getPrice().doubleValue() < 0) {
            throw new Exception("El precio debe ser mayor o igual a 0");
        }
        if (product.getStock() < 0) {
            throw new Exception("El stock no puede ser negativo");
        }
    }
}