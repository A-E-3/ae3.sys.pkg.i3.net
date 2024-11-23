package ru.myx.ae3.net.mobile;

import java.util.Collection;
import java.util.Collections;

import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionHidden;
import ru.myx.ae3.reflect.ReflectionManual;

/** @author myx */
@ReflectionManual
public interface ImeiNumber {

	/**
	 *
	 */
	@ReflectionExplicit
	ImeiNumber NULL_IMEI = new ImeiNumberEmpty(0);

	/** no example range known */
	@ReflectionExplicit
	ImeiNumber EXAMPLE_RANGE = new ImeiNumberRange(0x000000000000L, 0x0000000FFFFFL);

	/** no example range known */
	@ReflectionExplicit
	ImeiNumber EXAMPLE_RANGES = new ImeiNumberSet()//
			.union(new ImeiNumberRange(0x000000000000L, 0x00000000FFFFL))//
			.union(new ImeiNumberRange(0x000000100000L, 0x00000000FFFFL))//
	;

	/**
	 *
	 */
	static final char FMT_COUNT = '/';

	/**
	 *
	 */
	static final char FMT_COLON = '-';

	/**
	 *
	 */
	static final String FMT_COLON_STRING = "-";

	/**
	 *
	 */
	static final char FMT_RANGE = ':';

	/**
	 *
	 */
	static final char FMT_UNION = '+';

	/** No validation, just start of the sequence and count
	 *
	 * @param imeiNumber
	 * @param imeiCount
	 * @return */
	@ReflectionHidden
	static ImeiNumber create(final long imeiNumber, final long imeiCount) {

		return imeiCount == 1
			? new ImeiNumberSingle(imeiNumber)
			: imeiCount == 0
				? new ImeiNumberEmpty(imeiNumber)
				: new ImeiNumberRange(imeiNumber, imeiCount);
	}

	/** Should output: `49-015420-323751-8` 14 decimal digits and one calculated Lunn Checksum
	 *
	 * @param imeiNumber
	 * @param colons
	 *            - checksum is printed only when colons is true
	 * @param checksum
	 * @return */
	@ReflectionExplicit
	static String formatImeiNumberFromNumber(final long imeiNumber, final boolean colons, final boolean checksum) {

		final StringBuilder builder = new StringBuilder(//
				14 + (colons
					? 3 + 1
					: 0)//
		);

		final long x = imeiNumber;
		long div = 10000000000000L;
		/** https://en.wikipedia.org/wiki/Luhn_algorithm */
		int lunnSumm = 0;
		for (int digit = 14; digit > 0; --digit) {
			if (colons) {
				switch (digit) {
					case 12 :
					case 6 :
						builder.append(ImeiNumber.FMT_COLON);
						//$FALL-THROUGH$
					default :
				}
			}
			final int d = (int) (x / div % 10);
			// builder.append(ImeiNumberSingle.BASE10[d]);
			builder.append((char) ('0' + d));
			lunnSumm += (digit & 0x01) == 0
				? d
				: d * 2 % 10 + d * 2 / 10 % 10;
			div /= 10;
		}
		if (checksum) {
			if (colons) {
				builder.append(ImeiNumber.FMT_COLON);
			}
			// builder.append(ImeiNumberSingle.BASE10[(10 - lunnSumm % 10) % 10]);
			builder.append((char) ('0' + (10 - lunnSumm % 10) % 10));
		}

		return builder.toString();
	}

	/** @return */
	@ReflectionExplicit
	static Class<ImeiNumberEmpty> getImeiEmptyClass() {

		return ImeiNumberEmpty.class;
	}

	/** @return */
	@ReflectionExplicit
	static Class<ImeiNumberRange> getImeiRangeClass() {

		return ImeiNumberRange.class;
	}

	/** @return */
	@ReflectionExplicit
	static Class<ImeiNumberSet> getImeiSetClass() {

		return ImeiNumberSet.class;
	}

	/** @return */
	@ReflectionExplicit
	static Class<ImeiNumberSingle> getImeiSingleClass() {

		return ImeiNumberSingle.class;
	}

	/** On input: string of decimals?
	 *
	 * @param imeiBytes
	 *            in network order
	 * @return */
	@ReflectionExplicit
	static long imeiBytesToImeiNumber(final byte[] imeiBytes) {

		if (imeiBytes == null) {
			throw new NullPointerException("imeiBytes are NULL!");
		}
		if (imeiBytes.length != 6) {
			throw new IllegalArgumentException("imeiBytes length invalid: " + imeiBytes.length);
		}
		return (imeiBytes[0] & 0xFF) << 56 //
				| (imeiBytes[1] & 0xFF) << 48 //
				| (imeiBytes[2] & 0xFF) << 40 //
				| (imeiBytes[3] & 0xFF) << 32 //
				| (imeiBytes[4] & 0xFF) << 24 //
				| (imeiBytes[5] & 0xFF) << 16 //
		;
	}

	/** Should parse:
	 *
	 * 49-015420-323751-8
	 *
	 * 49015420323751
	 *
	 * 49-015420-323751
	 *
	 * @param string
	 * @param soft
	 * @return */
	@ReflectionExplicit
	static long parseSingleImeiNumber(final String string, final boolean soft) {

		if (string == null) {
			if (soft) {
				return -1;
			}
			throw new NullPointerException("IMEI Number String is NULL");
		}
		final String imeiString = string.trim();
		switch (imeiString.length()) {
			case 0 : {
				if (soft) {
					return -1;
				}
				throw new NullPointerException("IMEI Number String is empty");
			}
			case 14 : {
				// Short format with no colons
				break;
			}
			case 15 : {
				// Short format with checksum but no colons
				break;
			}
			case 16 : {
				// Long format with colons but no Lunn checksum
				break;
			}
			case 18 : {
				// Long format with colons
				break;
			}
			default : {
				if (soft) {
					return -1;
				}
				throw new IllegalArgumentException("IMEI String format is invalid: " + string);
			}
		}

		final String imeiAddressClean = imeiString.replace(ImeiNumber.FMT_COLON_STRING, "");
		switch (imeiAddressClean.length()) {
			case 15 :
				try {
					final long imeiNumber = Long.parseLong(imeiAddressClean, 10);
					if (imeiNumber >= 0 && imeiNumber <= 999999999999999L) {
						if (soft) {
							return imeiNumber / 10;
						}
						int lunnSumm = 0;
						for (int digit = 0; digit < 14; ++digit) {
							if ((digit & 0x01) == 0) {
								lunnSumm += imeiAddressClean.charAt(digit) - '0';
							} else {
								final int d = (imeiAddressClean.charAt(digit) - '0') * 2;
								lunnSumm += d % 10 + d / 10;
							}
						}
						lunnSumm += imeiAddressClean.charAt(14) - '0';
						if (lunnSumm % 10 == 0) {
							return imeiNumber / 10;
						}
						throw new IllegalArgumentException("IMEI Checksum is invalid: " + imeiNumber);
					}
					if (soft) {
						return -1;
					}
					throw new IllegalArgumentException("IMEI Number is invalid: " + imeiNumber);
				} catch (final NumberFormatException e) {
					if (soft) {
						return -1;
					}
					throw new IllegalArgumentException("IMEI Number is invalid!", e);
				}
			case 14 :
				try {
					final long imeiNumber = Long.parseLong(imeiAddressClean, 10);
					if (imeiNumber >= 0 && imeiNumber <= 99999999999999L) {
						return imeiNumber;
					}
					if (soft) {
						return -1;
					}
					throw new IllegalArgumentException("IMEI Number is invalid: " + imeiNumber);
				} catch (final NumberFormatException e) {
					if (soft) {
						return -1;
					}
					throw new IllegalArgumentException("IMEI Number is invalid!", e);
				}
			default :
				if (soft) {
					return -1;
				}
				throw new IllegalArgumentException("IMEI String format is invalid: " + string);
		}

	}

	/** @return */
	default Collection<ImeiNumber> getAddresses() {

		return Collections.singletonList(this);
	}

	/** @return */
	@ReflectionExplicit
	String getCompactString();

	/** @return */
	@ReflectionExplicit
	long getImeiCount();

	/** @return */
	default long getImeiLastNumber() {

		final long lastImei = this.getImeiNumber() + this.getImeiCount() - 1;
		return (0x7FFFFFFFFFFFFFFFL & lastImei) % 100000000000000L;
	}

	/** @return */
	long getImeiNumber();

	/** Returns an instance of single imei address if it fully represents the range of this
	 * ImeiNumberObject. NULL otherwise.
	 *
	 * @return */
	ImeiNumberSingle getImeiSingle();

	/** 49015420323751
	 *
	 * @return */
	@ReflectionExplicit
	default String getImeiString14() {
		
		final long imeiNumber = this.getImeiNumber();
		if (imeiNumber >= 0 && imeiNumber <= 99999999999999L) {
			return ImeiNumber.formatImeiNumberFromNumber(imeiNumber, false, false);
		}
		return null;
	}

	/** 490154203237518
	 *
	 * @return */
	@ReflectionExplicit
	default String getImeiString15() {
		
		final long imeiNumber = this.getImeiNumber();
		if (imeiNumber >= 0 && imeiNumber <= 99999999999999L) {
			return ImeiNumber.formatImeiNumberFromNumber(imeiNumber, false, true);
		}
		return null;
	}

	/** 49-015420-323751
	 *
	 * @return */
	@ReflectionExplicit
	default String getImeiString16() {
		
		final long imeiNumber = this.getImeiNumber();
		if (imeiNumber >= 0 && imeiNumber <= 99999999999999L) {
			return ImeiNumber.formatImeiNumberFromNumber(imeiNumber, true, false);
		}
		return null;
	}

	/** 49-015420-323751-8
	 *
	 * @return */
	@ReflectionExplicit
	default String getImeiString18() {
		
		final long imeiNumber = this.getImeiNumber();
		if (imeiNumber >= 0 && imeiNumber <= 99999999999999L) {
			return ImeiNumber.formatImeiNumberFromNumber(imeiNumber, true, true);
		}
		return null;
	}

	/** @return */
	@ReflectionExplicit
	String getLongString();

	/** @return */
	default Collection<ImeiNumber> getNonEmptyAddresses() {

		return this.getImeiCount() > 0
			? Collections.singletonList(this)
			: null;
	}

	/** @param index
	 * @return */
	@ReflectionExplicit
	ImeiNumber imeiAt(final long index);

	/** @param other
	 * @return */
	@ReflectionExplicit
	ImeiNumber intersect(final ImeiNumber other);

	/** @return */
	@ReflectionExplicit
	default boolean isEmpty() {

		return this.getImeiCount() <= 0;
	}

	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param imeiCount
	 * @return */
	ImeiNumber slice(final long shift, final long imeiCount);

	/** @param imeis
	 * @return */
	@ReflectionExplicit
	ImeiNumber substract(final ImeiNumber imeis);

	@Override
	@ReflectionExplicit
	String toString();

	/** @param other
	 * @return */
	@ReflectionExplicit
	ImeiNumber union(final ImeiNumber other);
}
