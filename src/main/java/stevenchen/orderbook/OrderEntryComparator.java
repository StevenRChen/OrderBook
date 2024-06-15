package stevenchen.orderbook;

import java.util.Comparator;

public class OrderEntryComparator implements Comparator<OrderEntry> {

    private final Side side;

    public OrderEntryComparator(Side side) {
        this.side = side;
    }

    @Override
    public int compare(OrderEntry o1, OrderEntry o2) {
        int result = 0;
        if (o1.getOrder().getPrice() < o2.getOrder().getPrice()) {
            result = 1;
        } else if (o1.getOrder().getPrice() > o2.getOrder().getPrice()) {
            result = -1;
        }
        if(side == Side.OFFER) {
            result *= -1;
        }
        if (result != 0) {
            return result;
        }
        if (o1.getOrderTime().isBefore(o2.getOrderTime())) {
            return 1;
        } else if (o1.getOrderTime().isAfter(o2.getOrderTime())) {
            return -1;
        }
        return 0;
    }
}
