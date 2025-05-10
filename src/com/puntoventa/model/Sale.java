package com.puntoventa.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Sale {
    private int id;
    private Date saleDate;
    private BigDecimal total;
    private User user;
    private List<SaleDetail> details;

    public Sale() {}

    public Sale(int id, Date saleDate, BigDecimal total, User user, List<SaleDetail> details) {
        this.id = id;
        this.saleDate = saleDate;
        this.total = total;
        this.user = user;
        this.details = details;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Date getSaleDate() { return saleDate; }
    public void setSaleDate(Date saleDate) { this.saleDate = saleDate; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<SaleDetail> getDetails() { return details; }
    public void setDetails(List<SaleDetail> details) { this.details = details; }
}