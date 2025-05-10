package com.puntoventa.controller;

import com.puntoventa.dao.SaleDAO;
import com.puntoventa.dao.SaleDAOImpl;
import com.puntoventa.model.Product;
import com.puntoventa.model.Sale;
import com.puntoventa.model.SaleDetail;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleController {
    private SaleDAO saleDAO;
    
    public SaleController() {
        saleDAO = new SaleDAOImpl();
    }
    
    public void saveSale(Sale sale) throws Exception {
        if (sale == null) {
            throw new IllegalArgumentException("La venta no puede ser nula");
        }
        if (sale.getUser() == null) {
            throw new IllegalArgumentException("La venta debe tener un usuario asociado");
        }
        saleDAO.insert(sale);
    }
    
    public Sale getSaleById(int id) throws Exception {
        if (id <= 0) {
            throw new IllegalArgumentException("El ID de venta debe ser mayor que 0");
        }
        try {
            return saleDAO.findById(id);
        } catch (Exception e) {
            throw new Exception("Error al obtener la venta: " + e.getMessage(), e);
        }
    }
    
    public List<Sale> getAllSales() throws Exception {
        try {
            return saleDAO.findAll();
        } catch (Exception e) {
            System.err.println("Error al obtener las ventas: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Retorna una lista vacía en caso de error
        }
    }
    
    public List<Sale> getSalesByDateRange(Date startDate, Date endDate) throws Exception {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Las fechas no pueden ser nulas");
        }
        if (startDate.after(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la fecha final");
        }
        try {
            return saleDAO.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            throw new Exception("Error al obtener las ventas por rango de fecha: " + e.getMessage(), e);
        }
    }
    
    public Map<Product, Integer> getTopSellingProductOfDay() {
        try {
            // Obtener la fecha actual
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            Date startDate = cal.getTime();
            
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            Date endDate = cal.getTime();              // Obtener las ventas del día
            List<Sale> sales = saleDAO.findByDateRange(startDate, endDate);
            
            // Mapa para contar la cantidad vendida de cada producto
            Map<Product, Integer> productQuantities = new HashMap<>();
            
            // Contar la cantidad vendida de cada producto
            for (Sale sale : sales) {
                if (sale.getDetails() != null) {
                    for (SaleDetail detail : sale.getDetails()) {
                        Product product = detail.getProduct();
                        productQuantities.merge(product, detail.getQuantity(), Integer::sum);
                    }
                }
            }
            
            // Si no hay ventas, retornar null
            if (productQuantities.isEmpty()) {
                return null;
            }
            
            // Encontrar el producto con la mayor cantidad vendida
            Map.Entry<Product, Integer> maxEntry = Collections.max(
                productQuantities.entrySet(),
                Map.Entry.comparingByValue()
            );
            
            // Retornar un mapa con solo el producto más vendido
            Map<Product, Integer> result = new HashMap<>();
            result.put(maxEntry.getKey(), maxEntry.getValue());
            return result;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}