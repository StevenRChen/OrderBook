package stevenchen.orderbook.model;

public enum Side {
    BID('B'), OFFER('O');

    private final char sideChar;

    Side(char sideChar) {
        this.sideChar = sideChar;
    }

    public char toChar() {
        return this.sideChar;
    }

    public static Side fromChar(char c) {
        for (Side side : Side.values()) {
            if (side.sideChar == Character.toUpperCase(c)) {
                return side;
            }
        }
        throw new IllegalArgumentException("Invalid character for Side: " + c);
    }
}
