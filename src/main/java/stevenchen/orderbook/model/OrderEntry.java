package stevenchen.orderbook.model;

import stevenchen.orderbook.Side;

import java.time.Instant;

public class OrderEntry {
    private Order order;
    private Instant orderTime;
    private Side side;
    private int price;

    public OrderEntry(Order order, Instant orderTime) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        this.order = order;
        this.orderTime = orderTime;
        this.price = (int)(Math.round(order.getPrice() * 100));  // Assuming price precision is 2 decimal places
        this.side = Side.fromChar(order.getSide());
    }

    public Order getOrder() {
        return order;
    }
    public void setOrder(Order order) {
        this.order = order;
    }

    public Instant getOrderTime() {
        return orderTime;
    }

    public int getPrice() {
        return price;
    }

    public Side getSide() {
        return side;
    }
}
