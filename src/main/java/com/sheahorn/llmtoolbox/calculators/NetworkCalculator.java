package com.sheahorn.llmtoolbox.calculators;

import com.sheahorn.llmtoolbox.calculators.common.Calculator;

public class NetworkCalculator implements Calculator {

    public String cidr(String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Use: a.b.c.d/prefix");
        }

        String ipStr = parts[0];
        int prefix = Integer.parseInt(parts[1]);
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("Prefix must be 0-32");
        }

        long ip = ipToLong(ipStr);
        long mask = prefixToMask(prefix);
        long network = ip & mask;
        long broadcast = network | (~mask & 0xFFFFFFFFL);

        long firstHost, lastHost, count;
        if (prefix == 32) {
            firstHost = network;
            lastHost = broadcast;
            count = 1;
        } else if (prefix == 31) {
            firstHost = network;
            lastHost = broadcast;
            count = 2;
        } else {
            firstHost = network + 1;
            lastHost = broadcast - 1;
            count = broadcast - network - 1;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Network: ").append(longToIp(network)).append("/").append(prefix).append("\n");
        sb.append("Netmask: ").append(longToIp(mask)).append("\n");
        sb.append("Wildcard: ").append(longToIp(~mask & 0xFFFFFFFFL)).append("\n");
        sb.append("Broadcast: ").append(longToIp(broadcast)).append("\n");
        sb.append("First host: ").append(longToIp(firstHost)).append("\n");
        sb.append("Last host: ").append(longToIp(lastHost)).append("\n");
        sb.append("Usable hosts: ").append(count);
        return sb.toString();
    }

    public String subnetInfo(String ip, String mask) {
        long ipLong = ipToLong(ip);
        int prefix = parseMask(mask);
        long netmask = prefixToMask(prefix);
        long network = ipLong & netmask;
        long broadcast = network | (~netmask & 0xFFFFFFFFL);

        long firstHost, lastHost, usableHosts, totalAddresses;
        if (prefix == 32) {
            firstHost = network;
            lastHost = broadcast;
            usableHosts = 1;
            totalAddresses = 1;
        } else if (prefix == 31) {
            firstHost = network;
            lastHost = broadcast;
            usableHosts = 2;
            totalAddresses = 2;
        } else {
            firstHost = network + 1;
            lastHost = broadcast - 1;
            usableHosts = broadcast - network - 1;
            totalAddresses = 1L << (32 - prefix);
        }
        boolean isMulticast = (ipLong & 0xF0000000L) == 0xE0000000L;

        StringBuilder sb = new StringBuilder();
        sb.append("Network: ").append(longToIp(network)).append("/").append(prefix).append("\n");
        sb.append("Netmask: ").append(longToIp(netmask)).append("\n");
        sb.append("Wildcard: ").append(longToIp(~netmask & 0xFFFFFFFFL)).append("\n");
        sb.append("Broadcast: ").append(longToIp(broadcast)).append("\n");
        sb.append("First host: ").append(longToIp(firstHost)).append("\n");
        sb.append("Last host: ").append(longToIp(lastHost)).append("\n");
        sb.append("Usable hosts: ").append(usableHosts).append("\n");
        sb.append("Total addresses: ").append(totalAddresses).append("\n");
        sb.append("Multicast: ").append(isMulticast);
        return sb.toString();
    }

    public boolean ipInCidr(String ip, String cidr) {
        long ipLong = ipToLong(ip);
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Use: a.b.c.d/prefix");
        }
        long network = ipToLong(parts[0]);
        int prefix = Integer.parseInt(parts[1]);
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("Prefix must be 0-32");
        }
        long mask = prefixToMask(prefix);
        return (ipLong & mask) == (network & mask);
    }

    public String subnetSplit(String cidr, int newPrefix) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid CIDR format. Use: a.b.c.d/prefix");
        }
        int oldPrefix = Integer.parseInt(parts[1]);
        if (oldPrefix < 0 || oldPrefix > 32) {
            throw new IllegalArgumentException("Prefix must be 0-32");
        }
        if (newPrefix <= oldPrefix) {
            throw new IllegalArgumentException("newPrefix must be greater than current prefix");
        }
        if (newPrefix > 32) {
            throw new IllegalArgumentException("newPrefix must be 0-32");
        }

        long network = ipToLong(parts[0]) & prefixToMask(oldPrefix);
        int subnetCount = 1 << (newPrefix - oldPrefix);
        long step = 1L << (32 - newPrefix);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subnetCount; i++) {
            long subnet = network + i * step;
            if (i > 0) sb.append("\n");
            sb.append(longToIp(subnet)).append("/").append(newPrefix);
        }
        return sb.toString();
    }

    public String ipToLongStr(String ip) {
        return String.valueOf(ipToLong(ip));
    }

    public String longToIpStr(long value) {
        if (value < 0 || value > 0xFFFFFFFFL) {
            throw new IllegalArgumentException("Value must be 0 to 4294967295 (unsigned 32-bit)");
        }
        return longToIp(value);
    }

    public boolean ipValidate(String ip) {
        try {
            ipToLong(ip);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Summarize multiple CIDR ranges into the smallest covering CIDR.
     * Validates that input CIDRs are contiguous (no gaps). Throws if they are not.
     */
    public String cidrSummarize(String[] cidrs) {
        if (cidrs == null || cidrs.length == 0) {
            throw new IllegalArgumentException("At least one CIDR is required");
        }

        // Parse and sort by network address
        record CidrEntry(long network, long broadcast, String original) {}
        CidrEntry[] entries = new CidrEntry[cidrs.length];
        for (int i = 0; i < cidrs.length; i++) {
            String[] parts = cidrs[i].split("/");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid CIDR: " + cidrs[i]);
            }
            long network = ipToLong(parts[0]);
            int prefix = Integer.parseInt(parts[1]);
            long mask = prefixToMask(prefix);
            long broadcast = network | (~mask & 0xFFFFFFFFL);
            entries[i] = new CidrEntry(network, broadcast, cidrs[i]);
        }
        java.util.Arrays.sort(entries, (a, b) -> Long.compare(a.network, b.network));

        // Validate contiguity: each entry's broadcast+1 must equal the next entry's network
        for (int i = 0; i < entries.length - 1; i++) {
            long expectedNext = entries[i].broadcast + 1;
            if (expectedNext != entries[i + 1].network) {
                throw new IllegalArgumentException(
                    "CIDRs are not contiguous: " + entries[i].original + " ends at " +
                    longToIp(entries[i].broadcast) + ", but next CIDR " +
                    entries[i + 1].original + " starts at " +
                    longToIp(entries[i + 1].network) + " (gap of " +
                    (entries[i + 1].network - expectedNext) + " addresses)");
            }
        }

        long min = entries[0].network;
        long max = entries[entries.length - 1].broadcast;

        int prefix = 32;
        while (prefix > 0) {
            long mask = prefixToMask(prefix);
            if ((min & mask) == min && ((min | (~mask & 0xFFFFFFFFL)) >= max)) {
                break;
            }
            prefix--;
        }

        return longToIp(min) + "/" + prefix;
    }

    private int parseMask(String mask) {
        if (mask == null || mask.isBlank()) {
            throw new IllegalArgumentException("mask is required");
        }

        // "/16" or "16"
        String stripped = mask.startsWith("/") ? mask.substring(1) : mask;

        // Try as integer prefix
        try {
            int prefix = Integer.parseInt(stripped);
            if (prefix < 0 || prefix > 32) {
                throw new IllegalArgumentException("Prefix must be 0-32");
            }
            return prefix;
        } catch (NumberFormatException e) {
            // Not an integer, try as dotted netmask
        }

        // Try as dotted netmask e.g. "255.255.0.0"
        long maskLong = ipToLong(stripped);
        // Validate it's a valid contiguous netmask
        long inverted = ~maskLong & 0xFFFFFFFFL;
        if ((inverted & (inverted + 1)) != 0) {
            throw new IllegalArgumentException("Invalid netmask: not a valid contiguous mask");
        }
        return Long.bitCount(maskLong);
    }

    private long prefixToMask(int prefix) {
        return prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
    }

    private long ipToLong(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) throw new IllegalArgumentException("Invalid IP");
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) throw new IllegalArgumentException("Octet out of range");
            result = (result << 8) | octet;
        }
        return result;
    }

    private String longToIp(long value) {
        return ((value >> 24) & 0xFF) + "." +
               ((value >> 16) & 0xFF) + "." +
               ((value >> 8) & 0xFF) + "." +
               (value & 0xFF);
    }
}
