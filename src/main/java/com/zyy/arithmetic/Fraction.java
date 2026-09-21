package com.zyy.arithmetic;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 不可变、自动约分的分数模型。
 */
public final class Fraction implements Comparable<Fraction> {
    public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);
    public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);

    private final BigInteger numerator;
    private final BigInteger denominator;

    private Fraction(BigInteger numerator, BigInteger denominator) {
        if (denominator.signum() == 0) {
            throw new ArithmeticException("分母不能为 0");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        this.numerator = numerator.divide(gcd);
        this.denominator = denominator.divide(gcd);
    }

    public static Fraction of(BigInteger numerator, BigInteger denominator) {
        return new Fraction(numerator, denominator);
    }

    public static Fraction of(long numerator, long denominator) {
        return new Fraction(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    public static Fraction integer(long value) {
        return of(value, 1);
    }

    public BigInteger numerator() {
        return numerator;
    }

    public BigInteger denominator() {
        return denominator;
    }

    public Fraction add(Fraction other) {
        return of(numerator.multiply(other.denominator).add(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator));
    }

    public Fraction subtract(Fraction other) {
        return of(numerator.multiply(other.denominator).subtract(other.numerator.multiply(denominator)),
                denominator.multiply(other.denominator));
    }

    public Fraction multiply(Fraction other) {
        return of(numerator.multiply(other.numerator), denominator.multiply(other.denominator));
    }

    public Fraction divide(Fraction other) {
        if (other.numerator.signum() == 0) {
            throw new ArithmeticException("除数不能为 0");
        }
        return of(numerator.multiply(other.denominator), denominator.multiply(other.numerator));
    }

    public boolean isZero() {
        return numerator.signum() == 0;
    }

    public boolean isNonNegative() {
        return numerator.signum() >= 0;
    }

    public boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    public boolean isProper() {
        return numerator.signum() > 0 && numerator.compareTo(denominator) < 0;
    }

    public String canonical() {
        return numerator + "/" + denominator;
    }

    public String toDisplay() {
        if (numerator.signum() == 0) {
            return "0";
        }
        String sign = numerator.signum() < 0 ? "-" : "";
        BigInteger absolute = numerator.abs();
        if (denominator.equals(BigInteger.ONE)) {
            return sign + absolute;
        }
        if (absolute.compareTo(denominator) < 0) {
            return sign + absolute + "/" + denominator;
        }
        BigInteger whole = absolute.divide(denominator);
        BigInteger remainder = absolute.remainder(denominator);
        if (remainder.signum() == 0) {
            return sign + whole;
        }
        return sign + whole + "’" + remainder + "/" + denominator;
    }

    @Override
    public int compareTo(Fraction other) {
        return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Fraction other)) {
            return false;
        }
        return numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numerator, denominator);
    }

    @Override
    public String toString() {
        return toDisplay();
    }
}
