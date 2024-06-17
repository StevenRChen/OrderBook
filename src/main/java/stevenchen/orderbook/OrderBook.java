package stevenchen.orderbook;

import stevenchen.orderbook.action.*;
import stevenchen.orderbook.model.Order;
import stevenchen.orderbook.model.OrderEntry;
import stevenchen.orderbook.model.OrderLevelBucket;
import stevenchen.orderbook.model.Side;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OrderBook {
    private static final Logger LOGGER = Logger.getLogger(OrderBook.class.getName());

    private final Map<Long, OrderEntry> orderLookup;
    private final BlockingQueue<OrderAction> orderActionQueue;
    private final ConcurrentNavigableMap<Integer, OrderLevelBucket> bidLevelMap;
    private final ConcurrentNavigableMap<Integer, OrderLevelBucket> offerLevelMap;

    public OrderBook() {
        orderLookup = new HashMap<>();
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
        //orderLookup.remove(orderId);
    }

    public void modifyOrder(long orderId, long newSize) {
        orderActionQueue.add(new ModifyAction(orderId, newSize));
        OrderEntry oldOrderEntry = orderLookup.get(orderId);
        Order order = oldOrderEntry.getOrder();
        Order newOrder = new Order(order.getId(), order.getPrice(), order.getSide(), newSize);
        OrderEntry newOrderEntry = new OrderEntry(newOrder, oldOrderEntry.getOrderTime());
    }

    public void processOrderActions() {
        while (!orderActionQueue.isEmpty()) {
            synchronized (this) {
                OrderAction orderAction = orderActionQueue.poll();
                switch (orderAction) {
                    case AddAction addAction -> {
                        LOGGER.info("Processing AddAction");
                        processAddAction(addAction);
                        break;
                    }
                    case RemoveAction removeAction -> {
                        LOGGER.info("Processing RemoveAction");
                        processRemoveAction(removeAction);
                        break;
                    }
                    case ModifyAction modifyAction -> {
                        LOGGER.info("Processing ModifyAction");
                        processModifyAction(modifyAction);
                    }
                    case null, default -> LOGGER.warning("Unknown OrderAction type");
                }
            }

        }
    }

    private ConcurrentNavigableMap<Integer, OrderLevelBucket> getOrderLevelMap(Side side) {
        return side == Side.BID ? bidLevelMap : offerLevelMap;
    }

    private void processModifyAction(ModifyAction modifyAction) {
        OrderEntry orderEntry = orderLookup.get(modifyAction.getOrderId());
        Order oldOrder = orderEntry.getOrder();
        Order newOrder = new Order(oldOrder.getId(), oldOrder.getPrice(), oldOrder.getSide(), modifyAction.getNewSize());
        orderEntry.setOrder(newOrder);
    }

    private void processRemoveAction(RemoveAction removeAction) {
        long orderId = removeAction.getOrderId();
        OrderEntry orderEntry = orderLookup.get(orderId);
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(orderEntry.getSide());
        OrderLevelBucket orderLevelBucket = orderLevelMap.get(orderEntry.getPrice());
        orderLevelBucket.removeOrderEntry(orderEntry.getOrder().getId());
        if (orderLevelBucket.isEmpty()) {
            orderLevelMap.remove(orderEntry.getPrice());
        }
        orderLookup.remove(orderEntry.getOrder().getId());
    }

    private void processAddAction(AddAction addAction) {
        OrderEntry orderEntry = addAction.getOrderEntry();
        orderLookup.put(orderEntry.getOrder().getId(), orderEntry);
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(orderEntry.getSide());
        OrderLevelBucket orderLevelBucket = orderLevelMap.computeIfAbsent(orderEntry.getPrice(), p -> new OrderLevelBucket(p, orderEntry.getSide()));
        orderLevelBucket.addOrderEntry(orderEntry);
    }

    /**
     * Get the price of a specific level in the order book.
      * @param side
     * @param level
     * @return price of the level
     */
    public synchronized long getLevelPrice(Side side, int level) {
        ConcurrentNavigableMap<Integer, OrderLevelBucket> orderLevelMap = getOrderLevelMap(side);
        if (level < 0 || level >= orderLevelMap.size()) {
            throw new IllegalArgumentException("Invalid level " + level);
        }
        Integer key = null;
        Iterator<Integer> iterator = orderLevelMap.keySet().iterator();
        for (int i = 0; i <= level; i++) {
            key = iterator.next();
        }
        return orderLevelMap.get(key).getPrice();
    }

    /**
     * Get the price of a specific level in the order book, taking a char as the side.
     * @param side
     * @param level
     * @return price of the level
     */
    public long getLevelPrice(char side, int level) {
        return getLevelPrice(Side.fromChar(side), level);
    }

    /**
     * Get the total size of a specific level in the order book.
     * @param side
     * @param level
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
     * @param side
     * @param level
     * @return total size of the level
     */
    public long getLevelTotalSize(char side, int level) {
        return getLevelTotalSize(Side.fromChar(side), level);
    }

    /**
     * Get all orders on a specific side of the order book, in level then time order.
     * @param side
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
     * @param side
     * @return list of orders
     */
    public List<Order> getAllOrdersOnSide(char side) {
        return getAllOrdersOnSide(Side.fromChar(side));
    }

    /**
     * Get the total order count in the order book.
     * @return
     */
    int getOrdersCount() {
        return orderLookup.size();
    }
}
