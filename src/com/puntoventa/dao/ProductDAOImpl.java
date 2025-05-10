package com.puntoventa.dao;

import com.puntoventa.model.Category;
import com.puntoventa.model.Product;
import com.puntoventa.util.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {
    
    @Override
    public void insert(Product product) throws Exception {
        String sql = "INSERT INTO Products (code, name, description, price, stock, active, categoryId) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setBoolean(6, product.isActive());
            if (product.getCategory() != null) {
                stmt.setInt(7, product.getCategory().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    @Override
    public void update(Product product) throws Exception {
        String sql = "UPDATE Products SET code=?, name=?, description=?, price=?, stock=?, active=?, categoryId=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, product.getCode());
            stmt.setString(2, product.getName());
            stmt.setString(3, product.getDescription());
            stmt.setBigDecimal(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setBoolean(6, product.isActive());
            if (product.getCategory() != null) {
                stmt.setInt(7, product.getCategory().getId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }
            stmt.setInt(8, product.getId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws Exception {
        if (isProductInSales(id)) {
            throw new Exception("Cannot delete product as it is associated with sales.");
        }
        String sql = "DELETE FROM Products WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public Product findById(int id) throws Exception {
        String sql = "SELECT p.*, c.id as cat_id, c.name as cat_name, c.description as cat_desc " +
                    "FROM Products p " +
                    "LEFT JOIN Categories c ON p.categoryId = c.id " +
                    "WHERE p.id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Product findByCode(String code) throws Exception {
        String sql = "SELECT p.*, c.id as cat_id, c.name as cat_name, c.description as cat_desc " +
                    "FROM Products p " +
                    "LEFT JOIN Categories c ON p.categoryId = c.id " +
                    "WHERE p.code=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Product> findAll() throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.id as cat_id, c.name as cat_name, c.description as cat_desc " +
                    "FROM Products p " +
                    "LEFT JOIN Categories c ON p.categoryId = c.id";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }

    @Override
    public List<Product> findByName(String name) throws Exception {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, c.id as cat_id, c.name as cat_name, c.description as cat_desc " +
                    "FROM Products p " +
                    "LEFT JOIN Categories c ON p.categoryId = c.id " +
                    "WHERE p.name LIKE ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + name + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }

    @Override
    public void updateStock(int productId, int newStock) throws Exception {
        String sql = "UPDATE Products SET stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newStock);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }

    @Override
    public boolean hasProductsInCategory(int categoryId) throws Exception {
        String sql = "SELECT COUNT(*) FROM Products WHERE categoryId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isProductInSales(int productId) throws Exception {
        String sql = "SELECT COUNT(*) FROM SaleDetails WHERE productId = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCode(rs.getString("code"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setActive(rs.getBoolean("active"));

        // Map category if exists
        int catId = rs.getInt("cat_id");
        if (!rs.wasNull()) {
            Category category = new Category();
            category.setId(catId);
            category.setName(rs.getString("cat_name"));
            category.setDescription(rs.getString("cat_desc"));
            product.setCategory(category);
        }

        return product;
    }
}