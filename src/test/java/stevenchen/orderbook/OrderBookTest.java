package stevenchen.orderbook;

import org.junit.jupiter.api.Test;
import stevenchen.orderbook.model.Order;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderBookTest {

    @Test
    void addOrderTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        orderBook.processOrderActions();
        assertEquals(1, orderBook.getOrdersCount());

        order = new Order(101, 101.0, 'O', 100);
        orderBook.addOrder(order);
        orderBook.processOrderActions();
        assertEquals(2, orderBook.getOrdersCount());
    }

    @Test
    void addAndRemoveOrderTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        orderBook.processOrderActions();
        assertEquals(1, orderBook.getOrdersCount());

        orderBook.removeOrder(100);
        orderBook.processOrderActions();
        assertEquals(0, orderBook.getOrdersCount());
    }

    @Test
    void getLevelPriceTest() {
        OrderBook orderBook = new OrderBook();
        Order order = new Order(100, 100.0, 'B', 100);
        orderBook.addOrder(order);
        orderBook.processOrderActions();
        assertEquals(100.0, orderBook.getLevelPrice('B', 0));
        assertEquals(100.0, orderBook.getLevelPrice(Side.BID, 0));
    }



}
