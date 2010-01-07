package net.ripe.ipresource;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * Immutable value object for Autonomous System Numbers.
 */
public class Asn extends UniqueIpResource {

	private static final long serialVersionUID = 1L;

	private static final Pattern ASN_TEXT_PATTERN = Pattern.compile("AS(\\d+)(\\.(\\d+))?", Pattern.CASE_INSENSITIVE);

    private static BigInteger ASN_MIN_VALUE = BigInteger.ZERO;
    private static BigInteger ASN16_MAX_VALUE = BigInteger.ONE.shiftLeft(16).subtract(BigInteger.ONE);
    private static BigInteger ASN32_MAX_VALUE = BigInteger.ONE.shiftLeft(32).subtract(BigInteger.ONE);

    public Asn(BigInteger value) {
        super(IpResourceType.ASN, value);
        Validate.isTrue(value.compareTo(ASN_MIN_VALUE) >= 0);
        Validate.isTrue(value.compareTo(ASN32_MAX_VALUE) <= 0);
    }

    public static Asn parse(String text) {
        if (text == null) {
            return null;
        }

        Matcher matcher = ASN_TEXT_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("not a legal ASN: " + text);
        }

        BigInteger high = BigInteger.ZERO;
        BigInteger low;

        if (matcher.group(3) != null) {
            low = new BigInteger(matcher.group(3));
            high = new BigInteger(matcher.group(1));
            checkRange(high, ASN16_MAX_VALUE);
            checkRange(low, ASN16_MAX_VALUE);
        } else {
            low = new BigInteger(matcher.group(1));
            checkRange(low, ASN32_MAX_VALUE);
        }

        return new Asn(high.shiftLeft(16).or(low));
    }

    private static void checkRange(BigInteger value, BigInteger Max) {
        Validate.isTrue(value.compareTo(BigInteger.ZERO) >= 0);
        Validate.isTrue(value.compareTo(Max) <= 0);
    }

    @Override
    public String toString() {
        return "AS" + value;
    }

    @Override
    public int getCommonPrefixLength(UniqueIpResource other) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress lowerBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

    @Override
    public IpAddress upperBoundForPrefix(int prefixLength) {
        throw new UnsupportedOperationException("prefix notation not supported for ASN resources");
    }

}
