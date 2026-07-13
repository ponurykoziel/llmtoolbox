package com.sheahorn.llmtoolbox.calculators;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;
import java.util.zip.Adler32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class CryptCalculator implements Calculator {

    // FNV-1a 32-bit constants
    private static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    private static final int FNV_PRIME = 0x01000193;

    public String crypt(String input, String algo) {
        switch (algo.toLowerCase()) {
            case "md2":
                return hash(input, "MD2");
            case "md5":
                return hash(input, "MD5");
            case "sha1":
                return hash(input, "SHA-1");
            case "sha224":
                return hash(input, "SHA-224");
            case "sha256":
                return hash(input, "SHA-256");
            case "sha384":
                return hash(input, "SHA-384");
            case "sha512":
                return hash(input, "SHA-512");
            case "sha512_224":
                return hash(input, "SHA-512/224");
            case "sha512_256":
                return hash(input, "SHA-512/256");
            case "sha3_224":
                return hash(input, "SHA3-224");
            case "sha3_256":
                return hash(input, "SHA3-256");
            case "sha3_384":
                return hash(input, "SHA3-384");
            case "sha3_512":
                return hash(input, "SHA3-512");
            case "base64_encode":
                return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
            case "base64_decode":
                return new String(Base64.getDecoder().decode(input), StandardCharsets.UTF_8);
            case "base64url_encode":
                return Base64.getUrlEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
            case "base64url_decode":
                return new String(Base64.getUrlDecoder().decode(input), StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("Unknown algo: " + algo);
        }
    }

    public String hmac(String input, String key, String algo) {
        try {
            String jcaAlgo;
            switch (algo.toLowerCase()) {
                case "hmac_md5":
                    jcaAlgo = "HmacMD5";
                    break;
                case "hmac_sha1":
                    jcaAlgo = "HmacSHA1";
                    break;
                case "hmac_sha224":
                    jcaAlgo = "HmacSHA224";
                    break;
                case "hmac_sha256":
                    jcaAlgo = "HmacSHA256";
                    break;
                case "hmac_sha384":
                    jcaAlgo = "HmacSHA384";
                    break;
                case "hmac_sha512":
                    jcaAlgo = "HmacSHA512";
                    break;
                case "hmac_sha512_224":
                    jcaAlgo = "HmacSHA512/224";
                    break;
                case "hmac_sha512_256":
                    jcaAlgo = "HmacSHA512/256";
                    break;
                default:
                    throw new IllegalArgumentException("Unknown HMAC algo: " + algo);
            }

            Mac mac = Mac.getInstance(jcaAlgo);
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), jcaAlgo);
            mac.init(keySpec);
            byte[] result = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC error: " + e.getMessage(), e);
        }
    }

    public String checksum(String input, String algo) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        switch (algo.toLowerCase()) {
            case "crc32": {
                CRC32 crc = new CRC32();
                crc.update(bytes);
                return String.format("%08x", crc.getValue());
            }
            case "crc32c": {
                CRC32C crc = new CRC32C();
                crc.update(bytes);
                return String.format("%08x", crc.getValue());
            }
            case "adler32": {
                Adler32 adler = new Adler32();
                adler.update(bytes);
                return String.format("%08x", adler.getValue());
            }
            case "fnv1a_32": {
                int hash = FNV_OFFSET_BASIS;
                for (byte b : bytes) {
                    hash ^= (b & 0xFF);
                    hash *= FNV_PRIME;
                }
                return String.format("%08x", hash);
            }
            case "joaat": {
                int hash = 0;
                for (byte b : bytes) {
                    hash += (b & 0xFF);
                    hash += (hash << 10);
                    hash ^= (hash >>> 6);
                }
                hash += (hash << 3);
                hash ^= (hash >>> 11);
                hash += (hash << 15);
                return String.format("%08x", hash);
            }
            default:
                throw new IllegalArgumentException("Unknown checksum algo: " + algo + ". Use: crc32, crc32c, adler32, fnv1a_32, joaat");
        }
    }

    public String hexEncode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public String hexDecode(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private String hash(String input, String algorithm) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hash error: " + e.getMessage(), e);
        }
    }
}
