package com.craftsmanship.tfm.persistence;

import java.util.List;

import com.craftsmanship.tfm.exceptions.ItemDoesNotExist;
import com.craftsmanship.tfm.exceptions.ItemWithNoStockAvailable;
import com.craftsmanship.tfm.exceptions.OrderDoesNotExist;
import com.craftsmanship.tfm.models.Order;

public interface OrderPersistence {

    public Order create(Order order) throws ItemDoesNotExist, ItemWithNoStockAvailable;

    public List<Order> list();

    public Order get(Long id) throws OrderDoesNotExist;

    public Order update(Long id, Order order) throws OrderDoesNotExist, ItemDoesNotExist, ItemWithNoStockAvailable;

    public Order delete(Long id) throws OrderDoesNotExist;
}