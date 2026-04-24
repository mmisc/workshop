package fuzzing;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.code_intelligence.jazzer.junit.FuzzTest;

import fuzzing.exercise04.PacketParser;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

/**
 * Exercise 04 - Fuzz blockers.
 *
 * The fuzz target below calls PacketParser.parse() with raw fuzzer
 * bytes. It compiles and runs, but coverage plateaus almost
 * immediately. Run it for 60 seconds and note where `cov` ends up:
 *
 *   JAZZER_FUZZ=1 mvn -Dtest=Exercise04Test test
 *
 * Your task:
 *
 *   1. Read PacketParser.parse() and identify the fuzz blocker.
 *   2. Decide: is it a "real" gate (something a real attacker must
 *      bypass) or an "artificial" one (safe to patch out while
 *      fuzzing)? The answer determines the fix.
 *   3. Rewrite the fuzz target so the fuzzer's mutated input can
 *      reach the code below the gate.
 *
 * There is a hint below in a commented-out method. Try solving it
 * yourself first; look at the hint only if you get stuck.
 */
class Exercise04Test {

    @FuzzTest(maxDuration = "1m")
    void fuzzPacketParserNaive(FuzzedDataProvider data) {
        byte[] input = data.consumeRemainingAsBytes();
        try {
            PacketParser.parse(input);
        } catch (IllegalArgumentException expected) {
            // Malformed packets are expected; ignore them.
        }
    }

    /*
     * HINT (uncomment to try it):
     *
     * Rather than feeding raw fuzzer bytes directly (the CRC will
     * almost never match), treat the fuzzed bytes as the PAYLOAD
     * and compute a valid CRC ourselves. This way the fuzzer's
     * mutations affect the payload, not the checksum.
     *
     * @FuzzTest(maxDuration = "1m")
     * void fuzzPacketParserWithValidCrc(FuzzedDataProvider data) {
     *     byte version = 0x01;
     *     byte type = data.consumeByte();
     *     byte[] payload = data.consumeRemainingAsBytes();
     *
     *     int length = payload.length;
     *     byte[] afterCrc = new byte[4 + payload.length];
     *     ByteBuffer.wrap(afterCrc).order(ByteOrder.BIG_ENDIAN)
     *         .put(version)
     *         .put(type)
     *         .putShort((short) length)
     *         .put(payload);
     *
     *     CRC32 crc = new CRC32();
     *     crc.update(afterCrc);
     *
     *     byte[] packet = new byte[4 + afterCrc.length];
     *     ByteBuffer.wrap(packet).order(ByteOrder.BIG_ENDIAN)
     *         .putInt((int) crc.getValue())
     *         .put(afterCrc);
     *
     *     try {
     *         PacketParser.parse(packet);
     *     } catch (IllegalArgumentException expected) {
     *         // ignore
     *     }
     * }
     */
}
