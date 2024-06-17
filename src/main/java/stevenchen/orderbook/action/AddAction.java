package stevenchen.orderbook.action;

import stevenchen.orderbook.model.OrderEntry;

public class AddAction implements OrderAction {
    private final OrderEntry orderEntry;

    public AddAction(OrderEntry orderEntry) {
        this.orderEntry = orderEntry;
    }
    public OrderEntry getOrderEntry() {
        return orderEntry;
    }
}
