package tools.data;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

import tools.HexTool;

/**
 * Writes a maplestory-packet little-endian stream of bytes.
 *
 * @author Frz
 */
public class MaplePacketLittleEndianWriter {

    private final ByteArrayOutputStream baos;
    private static final Charset ASCII = Charset.forName("US-ASCII"); // US-ASCII, ISO-8859-1, UTF-8, MS949

    /**
     * Constructor - initializes this stream with a default size.
     */
    public MaplePacketLittleEndianWriter() {
        this(32);
    }

    /**
     * Constructor - initializes this stream with size <code>size</code>.
     *
     * @param size The size of the underlying stream.
     */
    public MaplePacketLittleEndianWriter(final int size) {
        this.baos = new ByteArrayOutputStream(size);
    }

    /**
     * Gets a <code>MaplePacket</code> instance representing this sequence of
     * bytes.
     *
     * @return A <code>MaplePacket</code> with the bytes in this stream.
     */
    public final byte[] getPacket() {
        return baos.toByteArray();
    }

    /**
     * Changes this packet into a human-readable hexadecimal stream of bytes.
     *
     * @return This packet as hex digits.
     */
    @Override
    public final String toString() {
        return HexTool.toString(baos.toByteArray());
    }

    /**
     * Write the number of zero bytes
     *
     * @param i
     */
    public final void write0(final int i) {
        for (int x = 0; x < i; x++) {
            baos.write((byte) 0);
        }
    }

    /**
     * Write an array of bytes to the stream.
     *
     * @param b The bytes to write.
     */
    public final void write(final byte[] b) {
        for (int x = 0; x < b.length; x++) {
            baos.write(b[x]);
        }
    }

    /**
     * Write a byte to the stream.
     *
     * @param b The byte to write.
     */
    public final void write(final byte b) {
        baos.write(b);
    }

    public final void write(final int b) {
        baos.write((byte) b);
    }

    public final void write(final boolean b) {
        baos.write(b ? 1 : 0);
    }

    /**
     * Write a short integer to the stream.
     *
     * @param i The short integer to write.
     */
    public final void writeShort(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
    }

    /**
     * Writes an integer to the stream.
     *
     * @param i The integer to write.
     */
    public final void writeInt(final int i) {
        baos.write((byte) (i & 0xFF));
        baos.write((byte) ((i >>> 8) & 0xFF));
        baos.write((byte) ((i >>> 16) & 0xFF));
        baos.write((byte) ((i >>> 24) & 0xFF));
    }

    /**
     * Writes an ASCII string the the stream.
     *
     * @param s The ASCII string to write.
     */
    public final void writeAsciiString(final String s) {
        write(s.getBytes(ASCII));
    }

    public final void writeAsciiString(String s, final int max) {
        if (s.length() > max) {
            s = s.substring(0, max);
        }
        write(s.getBytes(ASCII));
        for (int i = s.length(); i < max; i++) {
            write(0);
        }
    }

    /**
     * Writes a maple-convention ASCII string to the stream.
     *
     * @param s The ASCII string to use maple-convention to write.
     */
    public final void writeMapleAsciiString(final String s) {
        writeShort((short) s.length());
        writeAsciiString(s);
    }

    /**
     * Writes a boolean to the packet as a byte.
     * @param b The boolean to write.
     */
    public final void writeBoolean(boolean b) {
        write(b ? 1 : 0);
    }

    /**
     * Writes a 2D 4 byte position information
     *
     * @param s The Point position to write.
     */
    public final void writePos(final Point s) {
        writeShort(s.x);
        writeShort(s.y);
    }

    public final void writeRect(final Rectangle s) {
        writeInt(s.x);
        writeInt(s.y);
        writeInt(s.x + s.width);
        writeInt(s.y + s.height);
    }

    /**
     * Write a long integer to the stream.
     *
     * @param l The long integer to write.
     */
    public final void writeLong(final long l) {
        baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
    }

    public final void writeReversedLong(final long l) {
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
        baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
    }

    public final void writeDouble(final double d) {
    	long l = Double.doubleToLongBits(d);
    	baos.write((byte) (l & 0xFF));
        baos.write((byte) ((l >>> 8) & 0xFF));
        baos.write((byte) ((l >>> 16) & 0xFF));
        baos.write((byte) ((l >>> 24) & 0xFF));
        baos.write((byte) ((l >>> 32) & 0xFF));
        baos.write((byte) ((l >>> 40) & 0xFF));
        baos.write((byte) ((l >>> 48) & 0xFF));
        baos.write((byte) ((l >>> 56) & 0xFF));
    	
    	/*baos.write((byte) ((l >>> 56) & 0xFF));
    	baos.write((byte) ((l >>> 48) & 0xFF));
    	baos.write((byte) ((l >>> 40) & 0xFF));
    	baos.write((byte) ((l >>> 32) & 0xFF));
    	baos.write((byte) ((l >>> 24) & 0xFF));
    	baos.write((byte) ((l >>> 16) & 0xFF));
    	baos.write((byte) ((l >>> 8) & 0xFF));
    	baos.write((byte) (l & 0xFF));*/
    }
}
