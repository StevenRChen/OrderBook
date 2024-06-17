package stevenchen.orderbook;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SideEnumTest {
    @Test
    void testSideEnum() {
        assertEquals('B', Side.BID.toChar());
        assertEquals(Side.BID, Side.fromChar('B'));
        assertEquals('O', Side.OFFER.toChar());
        assertEquals(Side.OFFER, Side.fromChar('O'));
    }
}
