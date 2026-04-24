package fuzzing.exercise04;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * Exercise 04: A small packet parser with a CRC32 checksum gate.
 *
 * Packet layout (big-endian):
 *
 *   offset 0..3   : CRC32 of everything after byte 3
 *   offset 4      : version byte (must be 0x01)
 *   offset 5      : type byte
 *   offset 6..7   : payload length N (unsigned short)
 *   offset 8..    : payload (N bytes)
 *
 * The CRC gate is a classic fuzz blocker: a coverage-guided fuzzer
 * will almost never guess a valid CRC on its own, so the interesting
 * code paths below the check are never reached.
 *
 * Do not modify this file. Your task in Exercise04Test is to write
 * a fuzz target that gets past the checksum and exercises the logic
 * underneath.
 */
public final class PacketParser {

    private PacketParser() {
        // utility class
    }

    /**
     * Parse a packet. Throws {@link IllegalArgumentException} if the
     * packet is malformed or the CRC does not match.
     *
     * There is a planted bug somewhere below the CRC check. You will
     * not see it unless your fuzz target produces valid CRCs.
     */
    public static byte[] parse(byte[] data) {
        if (data == null || data.length < 8) {
            throw new IllegalArgumentException("packet too short");
        }

        ByteBuffer buf = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN);
        int expectedCrc = buf.getInt();

        CRC32 crc = new CRC32();
        crc.update(data, 4, data.length - 4);
        if ((int) crc.getValue() != expectedCrc) {
            throw new IllegalArgumentException("bad checksum");
        }

        byte version = buf.get();
        if (version != 0x01) {
            throw new IllegalArgumentException("unsupported version");
        }

        byte type = buf.get();
        int length = buf.getShort() & 0xFFFF;

        // Planted bug: trusts the declared length without checking
        // against the actual remaining buffer size.
        byte[] payload = new byte[length];
        buf.get(payload);

        if (type == 0x42) {
            // Deeper branch - further bug, reachable only with the
            // right type byte.
            return decodeSpecial(payload);
        }
        return payload;
    }

    private static byte[] decodeSpecial(byte[] payload) {
        // Planted bug: ArithmeticException on empty payload.
        int stride = payload.length % payload[0];
        byte[] out = new byte[payload.length];
        for (int i = 0; i < payload.length; i++) {
            out[i] = (byte) (payload[i] ^ stride);
        }
        return out;
    }
}
