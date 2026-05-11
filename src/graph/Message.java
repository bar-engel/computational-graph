package graph;

import java.util.Date;

/**
 * Immutable message passed between agents via topics.
 * The String constructor is the canonical one; the others delegate to it.
 */
public class Message {

    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    /** Main constructor — all other constructors delegate here. */
    public Message(String text) {
        this.asText = text;
        this.data = text.getBytes();
        double parsed;
        try {
            parsed = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            parsed = Double.NaN;
        }
        this.asDouble = parsed;
        this.date = new Date();
    }

    public Message(byte[] bytes) {
        this(new String(bytes));
    }

    public Message(double d) {
        this(String.valueOf(d));
    }
}
