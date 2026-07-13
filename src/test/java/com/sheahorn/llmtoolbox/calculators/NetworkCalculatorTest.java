package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.NetworkCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NetworkCalculatorTest {

    private NetworkCalculator calc;

    @BeforeEach
    void setUp() {
        calc = new NetworkCalculator();
    }

    // ── cidr ─────────────────────────────────────────────────────────────

    @Test
    void cidr_slash24() {
        String result = calc.cidr("192.168.1.0/24");
        assertTrue(result.contains("Network: 192.168.1.0/24"));
        assertTrue(result.contains("Netmask: 255.255.255.0"));
        assertTrue(result.contains("Broadcast: 192.168.1.255"));
        assertTrue(result.contains("First host: 192.168.1.1"));
        assertTrue(result.contains("Last host: 192.168.1.254"));
        assertTrue(result.contains("Usable hosts: 254"));
    }

    @Test
    void cidr_slash32() {
        String result = calc.cidr("10.0.0.1/32");
        assertTrue(result.contains("Network: 10.0.0.1/32"));
        assertTrue(result.contains("Netmask: 255.255.255.255"));
        assertTrue(result.contains("First host: 10.0.0.1"));
        assertTrue(result.contains("Last host: 10.0.0.1"));
        assertTrue(result.contains("Usable hosts: 1"));
    }

    @Test
    void cidr_slash31() {
        String result = calc.cidr("192.168.1.0/31");
        assertTrue(result.contains("Usable hosts: 2"));
        assertTrue(result.contains("First host: 192.168.1.0"));
        assertTrue(result.contains("Last host: 192.168.1.1"));
    }

    @Test
    void cidr_slash0() {
        String result = calc.cidr("0.0.0.0/0");
        assertTrue(result.contains("Network: 0.0.0.0/0"));
        assertTrue(result.contains("Netmask: 0.0.0.0"));
        assertTrue(result.contains("Broadcast: 255.255.255.255"));
        // 2^32 - 2 = 4294967294 (excludes network and broadcast)
        assertTrue(result.contains("Usable hosts: 4294967294"));
    }

    @Test
    void cidr_slash16() {
        String result = calc.cidr("10.0.0.0/16");
        assertTrue(result.contains("Netmask: 255.255.0.0"));
        assertTrue(result.contains("Broadcast: 10.0.255.255"));
        assertTrue(result.contains("Usable hosts: 65534"));
    }

    @Test
    void cidr_slash8() {
        String result = calc.cidr("10.0.0.0/8");
        assertTrue(result.contains("Netmask: 255.0.0.0"));
        assertTrue(result.contains("Usable hosts: 16777214"));
    }

    @Test
    void cidr_invalidFormat_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidr("192.168.1.0"));
    }

    @Test
    void cidr_invalidPrefix_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidr("192.168.1.0/33"));
    }

    @Test
    void cidr_negativePrefix_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidr("192.168.1.0/-1"));
    }

    @Test
    void cidr_nonZeroHostBits() {
        // 192.168.1.5/24 — the .5 is host bits, should be masked to .0
        String result = calc.cidr("192.168.1.5/24");
        assertTrue(result.contains("Network: 192.168.1.0/24"));
    }

    // ── subnetInfo ───────────────────────────────────────────────────────

    @Test
    void subnetInfo_dottedMask() {
        String result = calc.subnetInfo("192.168.1.50", "255.255.255.0");
        assertTrue(result.contains("Network: 192.168.1.0/24"));
        assertTrue(result.contains("Netmask: 255.255.255.0"));
        assertTrue(result.contains("Usable hosts: 254"));
    }

    @Test
    void subnetInfo_prefixSlash() {
        String result = calc.subnetInfo("10.0.0.50", "/16");
        assertTrue(result.contains("Network: 10.0.0.0/16"));
        assertTrue(result.contains("Netmask: 255.255.0.0"));
    }

    @Test
    void subnetInfo_integerPrefix() {
        String result = calc.subnetInfo("172.16.5.50", "24");
        assertTrue(result.contains("Network: 172.16.5.0/24"));
    }

    @Test
    void subnetInfo_multicast() {
        // 224.0.0.0/4 is multicast range
        String result = calc.subnetInfo("224.0.0.1", "255.255.255.0");
        assertTrue(result.contains("Multicast: true"));
    }

    @Test
    void subnetInfo_nonMulticast() {
        String result = calc.subnetInfo("192.168.1.1", "255.255.255.0");
        assertTrue(result.contains("Multicast: false"));
    }

    @Test
    void subnetInfo_totalAddresses() {
        String result = calc.subnetInfo("10.0.0.0", "255.255.255.0");
        assertTrue(result.contains("Total addresses: 256"));
    }

    @Test
    void subnetInfo_invalidMask_throws() {
        // 255.255.0.255 is not a valid contiguous mask
        assertThrows(IllegalArgumentException.class,
                () -> calc.subnetInfo("192.168.1.1", "255.255.0.255"));
    }

    @Test
    void subnetInfo_slash32() {
        String result = calc.subnetInfo("10.0.0.1", "255.255.255.255");
        assertTrue(result.contains("Usable hosts: 1"));
        assertTrue(result.contains("Total addresses: 1"));
    }

    @Test
    void subnetInfo_slash31() {
        String result = calc.subnetInfo("192.168.1.0", "255.255.255.254");
        assertTrue(result.contains("Usable hosts: 2"));
        assertTrue(result.contains("Total addresses: 2"));
    }

    // ── ipInCidr ─────────────────────────────────────────────────────────

    @Test
    void ipInCidr_true() {
        assertTrue(calc.ipInCidr("192.168.1.50", "192.168.1.0/24"));
    }

    @Test
    void ipInCidr_false() {
        assertFalse(calc.ipInCidr("192.168.2.1", "192.168.1.0/24"));
    }

    @Test
    void ipInCidr_networkAddress() {
        assertTrue(calc.ipInCidr("192.168.1.0", "192.168.1.0/24"));
    }

    @Test
    void ipInCidr_broadcastAddress() {
        assertTrue(calc.ipInCidr("192.168.1.255", "192.168.1.0/24"));
    }

    @Test
    void ipInCidr_slash0() {
        // everything is in 0.0.0.0/0
        assertTrue(calc.ipInCidr("8.8.8.8", "0.0.0.0/0"));
    }

    @Test
    void ipInCidr_slash32() {
        assertTrue(calc.ipInCidr("10.0.0.1", "10.0.0.1/32"));
        assertFalse(calc.ipInCidr("10.0.0.2", "10.0.0.1/32"));
    }

    @Test
    void ipInCidr_invalidCidr_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.ipInCidr("10.0.0.1", "not-a-cidr"));
    }

    // ── subnetSplit ──────────────────────────────────────────────────────

    @Test
    void subnetSplit_24to25() {
        String result = calc.subnetSplit("192.168.1.0/24", 25);
        // should produce 2 subnets: 192.168.1.0/25 and 192.168.1.128/25
        assertTrue(result.contains("192.168.1.0/25"));
        assertTrue(result.contains("192.168.1.128/25"));
    }

    @Test
    void subnetSplit_24to26() {
        String result = calc.subnetSplit("10.0.0.0/24", 26);
        // 4 subnets: .0/26, .64/26, .128/26, .192/26
        assertTrue(result.contains("10.0.0.0/26"));
        assertTrue(result.contains("10.0.0.64/26"));
        assertTrue(result.contains("10.0.0.128/26"));
        assertTrue(result.contains("10.0.0.192/26"));
    }

    @Test
    void subnetSplit_16to24() {
        String result = calc.subnetSplit("172.16.0.0/16", 24);
        // 256 subnets, just check first and last
        assertTrue(result.contains("172.16.0.0/24"));
        assertTrue(result.contains("172.16.255.0/24"));
    }

    @Test
    void subnetSplit_newPrefixNotGreater_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.subnetSplit("192.168.1.0/24", 24));
    }

    @Test
    void subnetSplit_newPrefixSmaller_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.subnetSplit("192.168.1.0/24", 16));
    }

    @Test
    void subnetSplit_newPrefixTooHigh_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.subnetSplit("192.168.1.0/24", 33));
    }

    // ── ipToLongStr ──────────────────────────────────────────────────────

    @Test
    void ipToLongStr_zero() {
        assertEquals("0", calc.ipToLongStr("0.0.0.0"));
    }

    @Test
    void ipToLongStr_max() {
        assertEquals("4294967295", calc.ipToLongStr("255.255.255.255"));
    }

    @Test
    void ipToLongStr_knownValue() {
        // 192.168.1.1 = 0xC0A80101 = 3232235777
        assertEquals("3232235777", calc.ipToLongStr("192.168.1.1"));
    }

    @Test
    void ipToLongStr_invalidOctet_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.ipToLongStr("256.0.0.0"));
    }

    // ── longToIpStr ──────────────────────────────────────────────────────

    @Test
    void longToIpStr_zero() {
        assertEquals("0.0.0.0", calc.longToIpStr(0));
    }

    @Test
    void longToIpStr_max() {
        assertEquals("255.255.255.255", calc.longToIpStr(0xFFFFFFFFL));
    }

    @Test
    void longToIpStr_knownValue() {
        assertEquals("192.168.1.1", calc.longToIpStr(0xC0A80101L));
    }

    @Test
    void longToIpStr_roundtrip() {
        String ip = "10.20.30.40";
        long val = Long.parseLong(calc.ipToLongStr(ip));
        assertEquals(ip, calc.longToIpStr(val));
    }

    @Test
    void longToIpStr_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.longToIpStr(-1));
    }

    @Test
    void longToIpStr_tooLarge_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.longToIpStr(0x100000000L));
    }

    // ── ipValidate ───────────────────────────────────────────────────────

    @Test
    void ipValidate_valid() {
        assertTrue(calc.ipValidate("192.168.1.1"));
    }

    @Test
    void ipValidate_allZeros() {
        assertTrue(calc.ipValidate("0.0.0.0"));
    }

    @Test
    void ipValidate_all255() {
        assertTrue(calc.ipValidate("255.255.255.255"));
    }

    @Test
    void ipValidate_invalidOctet() {
        assertFalse(calc.ipValidate("192.168.1.256"));
    }

    @Test
    void ipValidate_negativeOctet() {
        assertFalse(calc.ipValidate("192.168.-1.1"));
    }

    @Test
    void ipValidate_tooFewOctets() {
        assertFalse(calc.ipValidate("192.168.1"));
    }

    @Test
    void ipValidate_tooManyOctets() {
        assertFalse(calc.ipValidate("192.168.1.1.1"));
    }

    @Test
    void ipValidate_nonNumeric() {
        assertFalse(calc.ipValidate("abc.def.ghi.jkl"));
    }

    // ── cidrSummarize ────────────────────────────────────────────────────

    @Test
    void cidrSummarize_twoAdjacent24s() {
        String result = calc.cidrSummarize(new String[]{"192.168.0.0/24", "192.168.1.0/24"});
        assertEquals("192.168.0.0/23", result);
    }

    @Test
    void cidrSummarize_fourAdjacent24s() {
        String result = calc.cidrSummarize(new String[]{
                "10.0.0.0/24", "10.0.1.0/24", "10.0.2.0/24", "10.0.3.0/24"});
        assertEquals("10.0.0.0/22", result);
    }

    @Test
    void cidrSummarize_singleCidr() {
        String result = calc.cidrSummarize(new String[]{"192.168.1.0/24"});
        assertEquals("192.168.1.0/24", result);
    }

    @Test
    void cidrSummarize_nonAdjacent() {
        // 192.168.0.0/24 and 192.168.2.0/24 are not contiguous (gap of 192.168.1.0/24)
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidrSummarize(new String[]{"192.168.0.0/24", "192.168.2.0/24"}));
    }

    @Test
    void cidrSummarize_empty_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidrSummarize(new String[]{}));
    }

    @Test
    void cidrSummarize_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidrSummarize(null));
    }

    @Test
    void cidrSummarize_invalidCidr_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidrSummarize(new String[]{"192.168.1.0/24", "not-cidr"}));
    }

    @Test
    void cidrSummarize_unsortedInput() {
        // Input in reverse order should still work (sorted internally)
        String result = calc.cidrSummarize(new String[]{"192.168.1.0/24", "192.168.0.0/24"});
        assertEquals("192.168.0.0/23", result);
    }

    @Test
    void cidrSummarize_overlapping() {
        // Overlapping CIDRs are not contiguous (broadcast+1 != next network)
        assertThrows(IllegalArgumentException.class,
                () -> calc.cidrSummarize(new String[]{"10.0.0.0/23", "10.0.0.0/24"}));
    }
}
