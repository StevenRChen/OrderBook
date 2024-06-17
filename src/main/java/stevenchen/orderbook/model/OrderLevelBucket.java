package stevenchen.orderbook.model;

import java.util.Collection;
import java.util.LinkedHashMap;

public class OrderLevelBucket {
    private final long price;
    private final Side side;
    private final LinkedHashMap<Long, OrderEntry> orderEntryMap;

    public OrderLevelBucket(long price, Side side) {
        this.price = price;
        this.side = side;
        this.orderEntryMap = new LinkedHashMap<>();
    }

    public long getPrice() {
        return price;
    }

    public double getOriginalPrice() {
        return price / 100.0;
    }

    public Side getSide() {
        return side;
    }

    public void addOrderEntry(OrderEntry orderEntry) {
        if(orderEntry == null) {
            throw new IllegalArgumentException("OrderEntry cannot be null");
        }
        if(orderEntry.getPrice() != price) {
            throw new IllegalArgumentException("OrderEntry price does not match bucket price");
        }
        if(orderEntry.getSide() != side) {
            throw new IllegalArgumentException("OrderEntry side does not match bucket side");
        }
        orderEntryMap.put(orderEntry.getOrder().getId(), orderEntry);
    }

    public void removeOrderEntry(long orderId) {
        OrderEntry entry = orderEntryMap.remove(orderId);
        if (entry == null) {
            throw new IllegalArgumentException("Order id " + orderId + " not found in bucket");
        }
    }

    public boolean isEmpty() {
        return orderEntryMap.isEmpty();
    }

    /**
     * Generate an Iterable of OrderEntry objects in time order.
     * This utilise the LinkedHashMap to maintain the insertion order.
     *
     * @return
     */
    public Collection<OrderEntry> getOrderEntries() {
        return orderEntryMap.values();
    }
}

