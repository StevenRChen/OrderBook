package stevenchen.orderbook.action;

public class ModifyAction implements OrderAction {
    private final long orderId;
    private final long newSize;

    public ModifyAction(long orderId, long newSize) {
        this.orderId = orderId;
        this.newSize = newSize;
    }

    public long getOrderId() {
        return orderId;
    }

    public long getNewSize() {
        return newSize;
    }
}
