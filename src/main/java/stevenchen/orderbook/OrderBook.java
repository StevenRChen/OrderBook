package stevenchen.orderbook;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class OrderBook {
    private Map<String, OrderEntry> orderLookup;
    private PriorityBlockingQueue<OrderEntry> bidQueue;
    private PriorityBlockingQueue<OrderEntry> offerQueue;

    public OrderBook() {
        orderLookup = new HashMap<>();
        bidQueue = new PriorityBlockingQueue<>(1000, new OrderEntryComparator(Side.BID));
        offerQueue = new PriorityBlockingQueue<>(1000, new OrderEntryComparator(Side.OFFER));
    }


}
