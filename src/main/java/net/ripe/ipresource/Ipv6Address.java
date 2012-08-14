/**
 * The BSD License
 *
 * Copyright (c) 2010, 2011 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Ipv6 address. This implementation has no support for interfaces.
 */
public class Ipv6Address extends IpAddress {

    private static final long serialVersionUID = 2L;

    /* Pattern to match IPv6 addresses in forms defined in http://www.ietf.org/rfc/rfc4291.txt */
    private static final Pattern IPV6_PATTERN = Pattern.compile("(([0-9A-Fa-f]{1,4}:){7}([0-9A-Fa-f]{1,4}|:))|(([0-9A-Fa-f]{1,4}:){6}(:[0-9A-Fa-f]{1,4}|((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){5}(((:[0-9A-Fa-f]{1,4}){1,2})|:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3})|:))|(([0-9A-Fa-f]{1,4}:){4}(((:[0-9A-Fa-f]{1,4}){1,3})|((:[0-9A-Fa-f]{1,4})?:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){3}(((:[0-9A-Fa-f]{1,4}){1,4})|((:[0-9A-Fa-f]{1,4}){0,2}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){2}(((:[0-9A-Fa-f]{1,4}){1,5})|((:[0-9A-Fa-f]{1,4}){0,3}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(([0-9A-Fa-f]{1,4}:){1}(((:[0-9A-Fa-f]{1,4}){1,6})|((:[0-9A-Fa-f]{1,4}){0,4}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))|(:(((:[0-9A-Fa-f]{1,4}){1,7})|((:[0-9A-Fa-f]{1,4}){0,5}:((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)){3}))|:))");
    private static final int COLON_COUNT_FOR_EMBEDDED_IPV4 = 6;
    private static final int COLON_COUNT_IPV6 = 7;

    /**
     * Mask for 16 bits, which is the length of one part of an IPv6 address.
     */
    private BigInteger PART_MASK = BigInteger.valueOf(0xffff);

    private final BigInteger value;

    public Ipv6Address(BigInteger value) {
        this.value = value;
    }

    @Override
    public IpResourceType getType() {
        return IpResourceType.IPv6;
    }

    @Override
    protected int doHashCode() {
        return value.hashCode();
    }

    @Override
    protected int doCompareTo(IpResource obj) {
        if (obj instanceof Ipv6Address) {
            Ipv6Address that = (Ipv6Address) obj;
            return this.getValue().compareTo(that.getValue());
        } else {
            return super.doCompareTo(obj);
        }
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        Validate.isTrue(getType() == other.getType(), "incompatible resource types");
        BigInteger temp = this.getValue().xor(other.getValue());
        return getType().getBitSize() - temp.bitLength();
    }

    @Override
    public Ipv6Address lowerBoundForPrefix(int prefixLength) {
        BigInteger mask = bitMask(0, getType()).xor(bitMask(prefixLength, getType()));
        return new Ipv6Address(this.getValue().and(mask));
    }

    @Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        return new Ipv6Address(this.getValue().or(bitMask(prefixLength, getType())));
    }

    public static Ipv6Address parse(String ipAddressString) {
        Validate.notNull(ipAddressString);
        ipAddressString = ipAddressString.trim();
        Validate.isTrue(IPV6_PATTERN.matcher(ipAddressString).matches(), "Invalid IPv6 address: " + ipAddressString);

        ipAddressString = expandMissingColons(ipAddressString);
        if (isInIpv4EmbeddedIpv6Format(ipAddressString)) {
            ipAddressString = getIpv6AddressWithIpv4SectionInIpv6Notation(ipAddressString);
        }
        return new Ipv6Address(ipv6StringtoBigInteger(ipAddressString));
    }

    private static String expandMissingColons(String ipAddressString) {
        int colonCount = isInIpv4EmbeddedIpv6Format(ipAddressString) ? COLON_COUNT_FOR_EMBEDDED_IPV4 : COLON_COUNT_IPV6;
        return ipAddressString.replace("::", StringUtils.repeat(":", colonCount - StringUtils.countMatches(ipAddressString, ":") + 2));
    }

    private static boolean isInIpv4EmbeddedIpv6Format(String ipAddressString) {
        return ipAddressString.contains(".");
    }

    private static String getIpv6AddressWithIpv4SectionInIpv6Notation(String ipAddressString) {
        String ipv6Section = StringUtils.substringBeforeLast(ipAddressString, ":");
        String ipv4Section = StringUtils.substringAfterLast(ipAddressString, ":");
        try {
            String ipv4SectionInIpv6Notation = StringUtils.join(new Ipv6Address(Ipv4Address.parse(ipv4Section).getValue()).toString().split(":"), ":", 2, 4);
            return ipv6Section + ":" + ipv4SectionInIpv6Notation;
        } catch(IllegalArgumentException e) {
            throw new IllegalArgumentException("Embedded Ipv4 in IPv6 address is invalid: " + ipAddressString, e);
        }
    }

    /**
     * Converts a fully expanded IPv6 string to a BigInteger
     *
     * @param ipAddressString Fully expanded address (i.e. no '::' shortcut)
     * @return Address as BigInteger
     */
    private static BigInteger ipv6StringtoBigInteger(String ipAddressString) {
        Pattern p = Pattern.compile("([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4}):([0-9a-fA-F]{0,4})");
        Matcher m = p.matcher(ipAddressString);
        m.find();

        String ipv6Number = "";
        for (int i = 1; i <= m.groupCount(); i++) {
            String part = m.group(i);
            String padding = "0000".substring(0, 4 - part.length());
            ipv6Number = ipv6Number + padding + part;
        }

        return new BigInteger(ipv6Number, 16);
    }

    @Override
    public String toString(boolean defaultMissingOctets) {
        long[] list = new long[8];
        int currentZeroLength = 0;
        int maxZeroLength = 0;
        int maxZeroIndex = 0;
        for (int i = 7; i >= 0; i--) {
            list[i] = getValue().shiftRight(i*16).and(PART_MASK).longValue();

            if (list[i] == 0) {
                currentZeroLength ++;
            } else {
                if (currentZeroLength > maxZeroLength) {
                    maxZeroIndex = i + currentZeroLength;
                    maxZeroLength = currentZeroLength;
                }
                currentZeroLength = 0;
            }
        }
        if (currentZeroLength > maxZeroLength) {
            maxZeroIndex = -1 + currentZeroLength;
            maxZeroLength = currentZeroLength;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            if (i == maxZeroIndex && maxZeroLength > 1) {
                if (i == 7) {
                    sb.append(':');
                }
                i -= (maxZeroLength - 1);
            } else {
                sb.append(String.format("%x", list[i]));
            }
            sb.append(':');
        }
        if ( (maxZeroIndex - maxZeroLength + 1) != 0) {
            sb.deleteCharAt(sb.length()-1);
        }

        return sb.toString();
    }


    // -------------------------------------------------------------------------------- HELPERS

    @Override
    public final BigInteger getValue() {
        return value;
    }

    @Override
    public boolean isValidNetmask() {
        int bitLength = value.bitLength();
        if (bitLength < IpResourceType.IPv6.getBitSize()) {
            return false;
        }

        int lowestSetBit = value.getLowestSetBit();
        for (int i = bitLength - 1; i >= lowestSetBit; --i) {
            if (!value.testBit(i)) {
                return false;
            }
        }

        return true;
    }
}
