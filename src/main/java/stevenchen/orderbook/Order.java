package stevenchen.orderbook;

public class Order{
    private long id; // i d o f o r d e r
    private double price;
    private char side; // B "Bid " o r O " O f f e r "
    private long size;
    public Order (long id, double price, char side, long size) {
        this. id=id ;
        this. price=price ;
        this. size=size ;
        this. side=side ;
    }
    public long getId () {return id;}
    public double getPrice() {return price;}
    public long getSize() {return size;}
    public char getSide() {return side;}
}