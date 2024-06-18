package stevenchen.orderbook;

import org.junit.jupiter.api.Test;
import stevenchen.orderbook.model.Order;
import stevenchen.orderbook.model.Side;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookTest {

    @Test
    void addOrderTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(1, orderBook.getOrdersCount());

        order = new Order(101, 101.0, 'O', 100);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(2, orderBook.getOrdersCount());
    }

    @Test
    void addAndRemoveOrderTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(1, orderBook.getOrdersCount());

        orderBook.removeOrder(100);
        orderBook.drainAndProcessOrderActions();
        assertEquals(0, orderBook.getOrdersCount());
    }

    @Test
    void getLevelPriceTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(102, 102.0, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(101, 101.0, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(103, 103.0, 'O', 100);
        orderBook.addOrder(order);
        // two offer orders at price 103
        order = new Order(104, 103.0, 'O', 100);
        orderBook.addOrder(order);
        order = new Order(106, 106.0, 'O', 100);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(102.0, orderBook.getLevelPrice('B', 0));
        assertEquals(102.0, orderBook.getLevelPrice(Side.BID, 0));
        assertEquals(101.0, orderBook.getLevelPrice(Side.BID, 1));
        assertEquals(100.0, orderBook.getLevelPrice(Side.BID, 2));
        assertEquals(103.0, orderBook.getLevelPrice('O', 0));
        assertEquals(103.0, orderBook.getLevelPrice(Side.OFFER, 0));
        assertEquals(106.0, orderBook.getLevelPrice(Side.OFFER, 1));
    }

    @Test
    void getLevelPriceWithDecimalPlaceTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.1, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(102, 102.1, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(101, 101.1, 'B', 100);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(102.1, orderBook.getLevelPrice('B', 0));
        assertEquals(102.1, orderBook.getLevelPrice(Side.BID, 0));
        assertEquals(101.1, orderBook.getLevelPrice(Side.BID, 1));
        assertEquals(100.1, orderBook.getLevelPrice(Side.BID, 2));
    }

    @Test
    void getLevelTotalSizeTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(102, 102.0, 'B', 200);
        orderBook.addOrder(order);
        order = new Order(101, 101.0, 'B', 300);
        orderBook.addOrder(order);
        order = new Order(103, 103.0, 'O', 400);
        orderBook.addOrder(order);
        // two offer orders at price 103
        order = new Order(104, 103.0, 'O', 500);
        orderBook.addOrder(order);
        order = new Order(106, 106.0, 'O', 600);
        orderBook.addOrder(order);
        orderBook.drainAndProcessOrderActions();
        assertEquals(200, orderBook.getLevelTotalSize('B', 0));
        assertEquals(200, orderBook.getLevelTotalSize(Side.BID, 0));
        assertEquals(300, orderBook.getLevelTotalSize(Side.BID, 1));
        assertEquals(100, orderBook.getLevelTotalSize(Side.BID, 2));
        assertEquals(900, orderBook.getLevelTotalSize('O', 0));
        assertEquals(900, orderBook.getLevelTotalSize(Side.OFFER, 0));
        assertEquals(600, orderBook.getLevelTotalSize(Side.OFFER, 1));
    }

    @Test
    void getAllOrdersOnSideTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        order = new Order(102, 102.0, 'B', 200);
        orderBook.addOrder(order);;
        order = new Order(101, 101.0, 'B', 300);
        orderBook.addOrder(order);;
        order = new Order(103, 103.0, 'O', 400);
        orderBook.addOrder(order);;
        // two offer orders at price 103
        order = new Order(104, 103.0, 'O', 500);
        orderBook.addOrder(order);;
        order = new Order(106, 106.0, 'O', 600);
        orderBook.addOrder(order);;
        orderBook.drainAndProcessOrderActions();

        List<Order> bidOrders = orderBook.getAllOrdersOnSide(Side.BID);
        List<Order> offerOrders = orderBook.getAllOrdersOnSide('o');
        bidOrders.forEach(o -> {
            assertEquals('B', o.getSide());
        });
        offerOrders.forEach(o -> {
            assertEquals('O', o.getSide());
        });
        assertArrayEquals(bidOrders.stream().mapToLong(Order::getId).toArray(), new long[]{102, 101, 100});
        assertArrayEquals(offerOrders.stream().mapToLong(Order::getId).toArray(), new long[]{103, 104, 106});
    }
}
