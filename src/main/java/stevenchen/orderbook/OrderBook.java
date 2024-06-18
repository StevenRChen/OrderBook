package stevenchen.orderbook;

import stevenchen.orderbook.action.*;
import stevenchen.orderbook.model.Order;
import stevenchen.orderbook.model.OrderEntry;
import stevenchen.orderbook.model.OrderLevelBucket;
import stevenchen.orderbook.model.Side;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

import java.util.stream.Collectors;

public class OrderBook {
    private static final Logger LOGGER = Logger.getLogger(OrderBook.class.getName());

    private final Map<Long, OrderEntry> orderLookup;
    private final BlockingQueue<OrderAction> orderActionQueue;
    private final ConcurrentNavigableMap<Integer, OrderLevelBucket> bidLevelMap;
    private final ConcurrentNavigableMap<Integer, OrderLevelBucket> offerLevelMap;

    public OrderBook() {
        orderLookup = new ConcurrentHashMap<>();
        orderActionQueue = new LinkedBlockingQueue<>();
        bidLevelMap = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
        offerLevelMap = new ConcurrentSkipListMap<>();
    }

    public void addOrder(Order order) {
        OrderEntry orderEntry = new OrderEntry(order, Instant.now());
        orderActionQueue.add(new AddAction(orderEntry));
    }

    public void removeOrder(long orderId) {
        orderActionQueue.add(new RemoveAction(orderId));
    }

    public void modifyOrder(long orderId, long newSize) {
        orderActionQueue.add(new ModifyAction(orderId, newSize));
    }

    private void processOrderAction(OrderAction orderAction) {
        switch (orderAction) {
            case AddAction addAction -> processAddAction(addAction);
            case RemoveAction removeAction -> processRemoveAction(removeAction);
            case ModifyAction modifyAction -> processModifyAction(modifyAction);
            case null, default -> LOGGER.warning("Unknown OrderAction type");
        }
    }

    public void drainAndProcessOrderActions() {
        while (!orderActionQueue.isEmpty()) {
            synchronized (this) {
                OrderAction orderAction = orderActionQueue.poll();
                processOrderAction(orderAction);
            }
        }
    }

    public void continuousProcessOrderActions() throws InterruptedException {
        while (true) {
            synchronized (this) {
                OrderAction orderAction = orderActionQueue.take();
                processOrderAction(orderAction);
            }
        }
    }

    private ConcurrentNavigableMap<Integer, OrderLevelBucket> getOrderLevelMap(Side side) {
        return side == Side.BID ? bidLevelMap : offerLevelMap;
    }

    private void processAddAction(AddAction addAction) {
        OrderEntry orderEntry = addAction.getOrderEntry();
        if(orderLookup.containsKey(orderEntry.getOrder().getId())) {
            throw new IllegalArgumentException("Order id " + orderEntry.getOrder().getId() + " already exists");
        }
        orderLookup.put(orderEntry.getOrder().getId(), orderEntry);
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(orderEntry.getSide());
        OrderLevelBucket orderLevelBucket = orderLevelMap.computeIfAbsent(orderEntry.getPrice(), p -> new OrderLevelBucket(p, orderEntry.getSide()));
        orderLevelBucket.addOrderEntry(orderEntry);
    }

    private void processRemoveAction(RemoveAction removeAction) {
        long orderId = removeAction.getOrderId();
        OrderEntry orderEntry = orderLookup.get(orderId);
        if (orderEntry == null) {
            return;
        }
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(orderEntry.getSide());
        OrderLevelBucket orderLevelBucket = orderLevelMap.get(orderEntry.getPrice());
        orderLevelBucket.removeOrderEntry(orderEntry.getOrder().getId());
        if (orderLevelBucket.isEmpty()) {
            orderLevelMap.remove(orderEntry.getPrice());
        }
        orderLookup.remove(orderEntry.getOrder().getId());
    }

    private void processModifyAction(ModifyAction modifyAction) {
        OrderEntry orderEntry = orderLookup.get(modifyAction.getOrderId());
        Order oldOrder = orderEntry.getOrder();
        Order newOrder = new Order(oldOrder.getId(), oldOrder.getPrice(), oldOrder.getSide(), modifyAction.getNewSize());
        orderEntry.setOrder(newOrder);
    }

    /**
     * Get the price of a specific level in the order book.
     * @return price of the level
     */
    public synchronized double getLevelPrice(Side side, int level) {
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(side);
        if (level < 0 || level >= orderLevelMap.size()) {
            throw new IllegalArgumentException("Invalid level " + level);
        }
        Integer key = null;
        Iterator<Integer> iterator = orderLevelMap.keySet().iterator();
        for (int i = 0; i <= level; i++) {
            key = iterator.next();
        }
        return orderLevelMap.get(key).getOriginalPrice();
    }

    /**
     * Get the price of a specific level in the order book, taking a char as the side.
     * @return price of the level
     */
    public double getLevelPrice(char side, int level) {
        return getLevelPrice(Side.fromChar(side), level);
    }

    /**
     * Get the total size of a specific level in the order book.
     * @return total size of the level
     */
    public synchronized long getLevelTotalSize(Side side, int level) {
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(side);
        if (level < 0 || level >= orderLevelMap.size()) {
            throw new IllegalArgumentException("Invalid level " + level);
        }
        Iterator<Integer> iterator = orderLevelMap.keySet().iterator();
        Integer key = null;
        for (int i = 0; i <= level; i++) {
            key = iterator.next();
        }
        OrderLevelBucket orderLevelBucket = orderLevelMap.get(key);
        return orderLevelBucket.getOrderEntries().stream().map(OrderEntry::getOrder).mapToLong(Order::getSize).sum();
    }

    /**
     * Get the total size of a specific level in the order book, taking a char as the side.
     * @return total size of the level
     */
    public long getLevelTotalSize(char side, int level) {
        return getLevelTotalSize(Side.fromChar(side), level);
    }

    /**
     * Get all orders on a specific side of the order book, in level then time order.
     * @return list of orders
     */
    public synchronized List<Order> getAllOrdersOnSide(Side side) {
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(side);
        return orderLevelMap.keySet().stream()
                .map(orderLevelMap::get)
                .flatMap(bucket -> bucket.getOrderEntries().stream().map(OrderEntry::getOrder))
                .collect(Collectors.toList());
    }

    /**
     * Get all orders on a specific side of the order book, in level then time order, taking a char as the side.
     * @return list of orders
     */
    public List<Order> getAllOrdersOnSide(char side) {
        return getAllOrdersOnSide(Side.fromChar(side));
    }

    /**
     * Get the total order count in the order book.
     * @return return the total order count
     */
    int getOrdersCount() {
        return orderLookup.size();
    }
}
