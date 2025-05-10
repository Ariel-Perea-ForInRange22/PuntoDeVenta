package com.puntoventa.dao;

import com.puntoventa.model.Sale;
import java.util.Date;
import java.util.List;

public interface SaleDAO {
    void insert(Sale sale) throws Exception;
    Sale findById(int id) throws Exception;
    List<Sale> findAll() throws Exception;
    List<Sale> findByDateRange(Date startDate, Date endDate) throws Exception;
    List<Sale> getSalesByDateRange(Date startDate, Date endDate) throws Exception;
}