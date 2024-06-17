package stevenchen.orderbook.action;

public class RemoveAction implements OrderAction{
    private final long orderId;
    public RemoveAction(long orderId) {
        this.orderId = orderId;
    }
    public long getOrderId() {
        return orderId;
    }
}
