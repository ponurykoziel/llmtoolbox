package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.CryptCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptCalculatorTest {

    private CryptCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new CryptCalculator();
    }

    // ── crypt: hash ─────────────────────────────────────────────────────

    @Test
    void crypt_sha256_knownVector() {
        // SHA-256 of empty string
        String result = calc.crypt("", "sha256");
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result);
    }

    @Test
    void crypt_sha256_hello() {
        // SHA-256 of "hello"
        String result = calc.crypt("hello", "sha256");
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", result);
    }

    @Test
    void crypt_md5_knownVector() {
        // MD5 of empty string
        String result = calc.crypt("", "md5");
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    void crypt_md5_hello() {
        String result = calc.crypt("hello", "md5");
        assertEquals("5d41402abc4b2a76b9719d911017c592", result);
    }

    @Test
    void crypt_sha1_knownVector() {
        // SHA-1 of empty string
        String result = calc.crypt("", "sha1");
        assertEquals("da39a3ee5e6b4b0d3255bfef95601890afd80709", result);
    }

    @Test
    void crypt_sha512_knownVector() {
        // SHA-512 of empty string
        String result = calc.crypt("", "sha512");
        assertEquals("cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e", result);
    }

    @Test
    void crypt_sha3_256_knownVector() {
        // SHA3-256 of empty string
        String result = calc.crypt("", "sha3_256");
        assertEquals("a7ffc6f8bf1ed76651c14756a061d662f580ff4de43b49fa82d80a4b80f8434a", result);
    }

    @Test
    void crypt_sha224_knownVector() {
        // SHA-224 of empty string
        String result = calc.crypt("", "sha224");
        assertEquals("d14a028c2a3a2bc9476102bb288234c415a2b01f828ea62ac5b3e42f", result);
    }

    @Test
    void crypt_sha384_knownVector() {
        // SHA-384 of empty string
        String result = calc.crypt("", "sha384");
        assertEquals("38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b", result);
    }

    @Test
    void crypt_sha512_224_knownVector() {
        // SHA-512/224 of empty string
        String result = calc.crypt("", "sha512_224");
        assertEquals("6ed0dd02806fa89e25de060c19d3ac86cabb87d6a0ddd05c333b84f4", result);
    }

    @Test
    void crypt_sha512_256_knownVector() {
        // SHA-512/256 of empty string
        String result = calc.crypt("", "sha512_256");
        assertEquals("c672b8d1ef56ed28ab87c3622c5114069bdd3ad7b8f9737498d0c01ecef0967a", result);
    }

    @Test
    void crypt_sha3_224_knownVector() {
        // SHA3-224 of empty string
        String result = calc.crypt("", "sha3_224");
        assertEquals("6b4e03423667dbb73b6e15454f0eb1abd4597f9a1b078e3f5b5a6bc7", result);
    }

    @Test
    void crypt_sha3_384_knownVector() {
        // SHA3-384 of empty string
        String result = calc.crypt("", "sha3_384");
        assertEquals("0c63a75b845e4f7d01107d852e4c2485c51a50aaaa94fc61995e71bbee983a2ac3713831264adb47fb6bd1e058d5f004", result);
    }

    @Test
    void crypt_sha3_512_knownVector() {
        // SHA3-512 of empty string
        String result = calc.crypt("", "sha3_512");
        assertEquals("a69f73cca23a9ac5c8b567dc185a756e97c982164fe25859e0d1dcc1475c80a615b2123af1f5f94c11e3e9402c3ac558f500199d95b6d3e301758586281dcd26", result);
    }

    // ── crypt: base64 ───────────────────────────────────────────────────

    @Test
    void crypt_base64_encode() {
        String result = calc.crypt("hello", "base64_encode");
        assertEquals("aGVsbG8=", result);
    }

    @Test
    void crypt_base64_decode() {
        String result = calc.crypt("aGVsbG8=", "base64_decode");
        assertEquals("hello", result);
    }

    @Test
    void crypt_base64_roundtrip() {
        String original = "Hello, World! 123";
        String encoded = calc.crypt(original, "base64_encode");
        String decoded = calc.crypt(encoded, "base64_decode");
        assertEquals(original, decoded);
    }

    @Test
    void crypt_base64url_encode() {
        String result = calc.crypt("hello?world", "base64url_encode");
        // standard base64 would have '+' and '/', URL-safe uses '-' and '_'
        assertFalse(result.contains("+"));
        assertFalse(result.contains("/"));
    }

    @Test
    void crypt_base64url_decode() {
        String encoded = calc.crypt("hello?world", "base64url_encode");
        String decoded = calc.crypt(encoded, "base64url_decode");
        assertEquals("hello?world", decoded);
    }

    @Test
    void crypt_base64url_roundtrip() {
        String original = "test/data?key=value&x=1";
        String encoded = calc.crypt(original, "base64url_encode");
        String decoded = calc.crypt(encoded, "base64url_decode");
        assertEquals(original, decoded);
    }

    // ── crypt: invalid algo ─────────────────────────────────────────────

    @Test
    void crypt_invalidAlgo_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.crypt("hello", "nonexistent"));
    }

    @Test
    void crypt_caseInsensitive() {
        String lower = calc.crypt("hello", "sha256");
        String upper = calc.crypt("hello", "SHA256");
        assertEquals(lower, upper);
    }

    // ── hmac ────────────────────────────────────────────────────────────

    @Test
    void hmac_sha256_knownVector() {
        // HMAC-SHA256 with key="key", data="The quick brown fox jumps over the lazy dog"
        String result = calc.hmac("The quick brown fox jumps over the lazy dog", "key", "hmac_sha256");
        assertEquals("f7bc83f430538424b13298e6aa6fb143ef4d59a14946175997479dbc2d1a3cd8", result);
    }

    @Test
    void hmac_sha256_emptyKey() {
        // Empty key throws RuntimeException (wrapping IllegalArgumentException from SecretKeySpec)
        assertThrows(RuntimeException.class,
                () -> calc.hmac("hello", "", "hmac_sha256"));
    }

    @Test
    void hmac_sha1_knownVector() {
        String result = calc.hmac("hello", "secret", "hmac_sha1");
        assertNotNull(result);
        assertEquals(40, result.length()); // SHA-1 produces 20 bytes = 40 hex chars
    }

    @Test
    void hmac_sha512_knownVector() {
        String result = calc.hmac("hello", "secret", "hmac_sha512");
        assertNotNull(result);
        assertEquals(128, result.length()); // SHA-512 produces 64 bytes = 128 hex chars
    }

    @Test
    void hmac_invalidAlgo_throws() {
        // The IllegalArgumentException is wrapped in RuntimeException
        assertThrows(RuntimeException.class,
                () -> calc.hmac("hello", "key", "hmac_nonexistent"));
    }

    @Test
    void hmac_differentKeysDifferentResults() {
        String r1 = calc.hmac("hello", "key1", "hmac_sha256");
        String r2 = calc.hmac("hello", "key2", "hmac_sha256");
        assertNotEquals(r1, r2);
    }

    @Test
    void hmac_md5_knownVector() {
        String result = calc.hmac("hello", "secret", "hmac_md5");
        assertNotNull(result);
        assertEquals(32, result.length()); // MD5 produces 16 bytes = 32 hex chars
    }

    @Test
    void hmac_sha224_knownVector() {
        String result = calc.hmac("hello", "secret", "hmac_sha224");
        assertNotNull(result);
        assertEquals(56, result.length()); // SHA-224 produces 28 bytes = 56 hex chars
    }

    @Test
    void hmac_sha384_knownVector() {
        String result = calc.hmac("hello", "secret", "hmac_sha384");
        assertNotNull(result);
        assertEquals(96, result.length()); // SHA-384 produces 48 bytes = 96 hex chars
    }

    // ── checksum ────────────────────────────────────────────────────────

    @Test
    void checksum_crc32_knownValue() {
        // CRC32 of "123456789" = 0xcbf43926
        String result = calc.checksum("123456789", "crc32");
        assertEquals("cbf43926", result);
    }

    @Test
    void checksum_crc32_empty() {
        String result = calc.checksum("", "crc32");
        assertEquals("00000000", result);
    }

    @Test
    void checksum_crc32c() {
        String result = calc.checksum("hello", "crc32c");
        assertNotNull(result);
        assertEquals(8, result.length());
    }

    @Test
    void checksum_adler32() {
        String result = calc.checksum("hello", "adler32");
        assertNotNull(result);
        assertEquals(8, result.length());
    }

    @Test
    void checksum_fnv1a_32() {
        String result = calc.checksum("hello", "fnv1a_32");
        assertNotNull(result);
        assertEquals(8, result.length());
    }

    @Test
    void checksum_joaat() {
        String result = calc.checksum("hello", "joaat");
        assertNotNull(result);
        assertEquals(8, result.length());
    }

    @Test
    void checksum_differentInputsDifferentResults() {
        String r1 = calc.checksum("hello", "crc32");
        String r2 = calc.checksum("world", "crc32");
        assertNotEquals(r1, r2);
    }

    @Test
    void checksum_invalidAlgo_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.checksum("hello", "nonexistent"));
    }

    // ── hexEncode / hexDecode ───────────────────────────────────────────

    @Test
    void hexEncode_hello() {
        String result = calc.hexEncode("hello");
        assertEquals("68656c6c6f", result);
    }

    @Test
    void hexEncode_empty() {
        assertEquals("", calc.hexEncode(""));
    }

    @Test
    void hexDecode_hello() {
        String result = calc.hexDecode("68656c6c6f");
        assertEquals("hello", result);
    }

    @Test
    void hexDecode_empty() {
        assertEquals("", calc.hexDecode(""));
    }

    @Test
    void hex_roundtrip() {
        String original = "Hello, World! 123";
        String encoded = calc.hexEncode(original);
        String decoded = calc.hexDecode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hex_roundtrip_binary() {
        // bytes 0x00, 0xFF, 0xAB
        String original = "\u0000\u00FF\u00AB";
        String encoded = calc.hexEncode(original);
        String decoded = calc.hexDecode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void hexDecode_oddLength_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.hexDecode("abc"));
    }

    @Test
    void hexDecode_invalidHex_throws() {
        assertThrows(NumberFormatException.class,
                () -> calc.hexDecode("gg"));
    }
}
