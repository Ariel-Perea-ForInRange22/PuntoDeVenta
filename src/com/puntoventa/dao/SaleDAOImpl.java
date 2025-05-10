package com.puntoventa.dao;

import com.puntoventa.model.*;
import com.puntoventa.util.DatabaseConfig;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaleDAOImpl implements SaleDAO {
    
    @Override
    public void insert(Sale sale) throws Exception {
        String sql = "INSERT INTO Sales (saleDate, total, userId) VALUES (?, ?, ?)";
        String detailSql = "INSERT INTO SaleDetails (saleId, productId, quantity, unitPrice, subtotal) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = DatabaseConfig.getConnection();
            conn.setAutoCommit(false);
            
            // Insert sale
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setTimestamp(1, new Timestamp(sale.getSaleDate().getTime()));
            stmt.setBigDecimal(2, sale.getTotal());
            stmt.setInt(3, sale.getUser().getId());
            stmt.executeUpdate();
            
            // Get generated sale ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                sale.setId(rs.getInt(1));
            }
            
            // Insert sale details
            PreparedStatement detailStmt = conn.prepareStatement(detailSql);
            for (SaleDetail detail : sale.getDetails()) {
                detailStmt.setInt(1, sale.getId());
                detailStmt.setInt(2, detail.getProduct().getId());
                detailStmt.setInt(3, detail.getQuantity());
                detailStmt.setBigDecimal(4, detail.getUnitPrice());
                detailStmt.setBigDecimal(5, detail.getSubtotal());
                detailStmt.executeUpdate();
            }
            
            conn.commit();
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new Exception("Error al hacer rollback: " + ex.getMessage());
                }
            }
            throw new Exception("Error al insertar venta: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    throw new Exception("Error al cerrar la conexión: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public Sale findById(int id) throws Exception {
        String sql = "SELECT s.id, s.saleDate, s.total, s.userId, u.username " +
                    "FROM Sales s " +
                    "LEFT JOIN Users u ON s.userId = u.id " +
                    "WHERE s.id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToSale(rs, true);
            }
            return null;
        } catch (SQLException e) {
            throw new Exception("Error al buscar venta por ID: " + e.getMessage());
        }
    }

    @Override
    public List<Sale> findAll() throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.id, s.saleDate, s.total, s.userId, u.username " +
                    "FROM Sales s " +
                    "LEFT JOIN Users u ON s.userId = u.id " +
                    "ORDER BY s.saleDate DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs, false));
            }
            return sales;
        } catch (SQLException e) {
            throw new Exception("Error al obtener todas las ventas: " + e.getMessage());
        }
    }

    @Override
    public List<Sale> findByDateRange(Date startDate, Date endDate) throws Exception {
        List<Sale> sales = new ArrayList<>();
        String sql = "SELECT s.id, s.saleDate, s.total, s.userId, u.username " +
                    "FROM Sales s " +
                    "LEFT JOIN Users u ON s.userId = u.id " +
                    "WHERE s.saleDate BETWEEN ? AND ? " +
                    "ORDER BY s.saleDate DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                sales.add(mapResultSetToSale(rs, true)); // Changed to true to load details
            }
            return sales;
        } catch (SQLException e) {
            throw new Exception("Error al buscar ventas por rango de fecha: " + e.getMessage());
        }
    }

    @Override
    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) throws Exception {
        List<Sale> sales = new ArrayList<>();
        String query = "SELECT * FROM Sales WHERE saleDate BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setTimestamp(1, new Timestamp(startDate.getTime()));
            stmt.setTimestamp(2, new Timestamp(endDate.getTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = new Sale();
                    sale.setId(rs.getInt("id"));
                    sale.setSaleDate(rs.getTimestamp("saleDate"));
                    sale.setTotal(rs.getBigDecimal("total"));
                    
                    // Cargar los detalles de la venta
                    String detailQuery = "SELECT sd.*, p.* FROM SaleDetails sd " +
                                       "JOIN Products p ON sd.productId = p.id " +
                                       "WHERE sd.saleId = ?";
                    
                    try (PreparedStatement detailStmt = conn.prepareStatement(detailQuery)) {
                        detailStmt.setInt(1, sale.getId());
                        try (ResultSet detailRs = detailStmt.executeQuery()) {
                            List<SaleDetail> details = new ArrayList<>();
                            while (detailRs.next()) {
                                SaleDetail detail = new SaleDetail();                                detail.setId(detailRs.getInt("id"));
                                detail.setQuantity(detailRs.getInt("quantity"));
                                detail.setUnitPrice(detailRs.getBigDecimal("unitPrice"));
                                
                                Product product = new Product();
                                product.setId(detailRs.getInt("productId"));
                                product.setCode(detailRs.getString("code"));
                                product.setName(detailRs.getString("name"));
                                product.setPrice(detailRs.getBigDecimal("price"));
                                product.setStock(detailRs.getInt("stock"));
                                product.setActive(detailRs.getBoolean("active"));
                                
                                detail.setProduct(product);
                                details.add(detail);
                            }
                            sale.setDetails(details);
                        }
                    }
                    sales.add(sale);
                }
            }
        }
        
        return sales;
    }

    private Sale mapResultSetToSale(ResultSet rs, boolean loadDetails) throws SQLException {
        try {
            Sale sale = new Sale();
            sale.setId(rs.getInt("id"));
            sale.setSaleDate(new Date(rs.getTimestamp("saleDate").getTime()));
            sale.setTotal(rs.getBigDecimal("total"));
            
            User user = new User();
            user.setId(rs.getInt("userId"));
            String username = rs.getString("username");
            user.setUsername(username != null ? username : "Usuario Desconocido");
            sale.setUser(user);
            
            if (loadDetails) {
                sale.setDetails(loadSaleDetails(sale.getId()));
            }
            
            return sale;
        } catch (SQLException e) {
            throw new SQLException("Error al mapear resultados de venta: " + e.getMessage());
        }
    }

    private List<SaleDetail> loadSaleDetails(int saleId) throws SQLException {
        List<SaleDetail> details = new ArrayList<>();
        String sql = "SELECT sd.id, sd.productId, sd.quantity, sd.unitPrice, " +
                    "p.code, p.name, p.price " +
                    "FROM SaleDetails sd " +
                    "LEFT JOIN Products p ON sd.productId = p.id " +
                    "WHERE sd.saleId = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, saleId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SaleDetail detail = new SaleDetail();
                detail.setId(rs.getInt("id"));
                
                Product product = new Product();
                product.setId(rs.getInt("productId"));
                product.setCode(rs.getString("code"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getBigDecimal("price"));
                detail.setProduct(product);
                
                detail.setQuantity(rs.getInt("quantity"));
                detail.setUnitPrice(rs.getBigDecimal("unitPrice"));
                details.add(detail);
            }
            return details;
        } catch (SQLException e) {
            throw new SQLException("Error al cargar detalles de venta: " + e.getMessage());
        }
    }
}