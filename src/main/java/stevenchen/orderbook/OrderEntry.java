package stevenchen.orderbook;

import java.time.Instant;

public class OrderEntry {
    private Order order;
    private Instant orderTime;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Instant getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Instant orderTime) {
        this.orderTime = orderTime;
    }
}
