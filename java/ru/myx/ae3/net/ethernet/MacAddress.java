package ru.myx.ae3.net.ethernet;

import java.util.Collection;
import java.util.Collections;

import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionHidden;
import ru.myx.ae3.reflect.ReflectionManual;

/** @author myx */
@ReflectionManual
public interface MacAddress {
	
	/**
	 *
	 */
	@ReflectionExplicit
	MacAddress NULL_ADDRESS = new MacAddressEmpty(0);
	
	/** https://www.iana.org/assignments/ethernet-numbers/ethernet-numbers.xhtml */
	@ReflectionExplicit
	MacAddress EXAMPLE_RANGE = new MacAddressRange(0x005300000000L, 0x0000FFFFFFFFL);
	
	/** https://www.iana.org/assignments/ethernet-numbers/ethernet-numbers.xhtml */
	@ReflectionExplicit
	MacAddress EXAMPLE_RANGES = new MacAddressSet()//
			.union(new MacAddressRange(0x005300000000L, 0x0000FFFFFFFFL))//
			.union(new MacAddressRange(0x901000000000L, 0x0000FFFFFFFFL))//
	;
	
	/**
	 *
	 */
	static final char FMT_COUNT = '/';
	
	/**
	 *
	 */
	static final char FMT_COLON = ':';
	
	/**
	 *
	 */
	static final String FMT_COLON_STRING = ":";
	
	/**
	 *
	 */
	static final char FMT_RANGE = '-';
	
	/**
	 *
	 */
	static final char FMT_UNION = '+';
	
	/** @param macNumber
	 * @param macCount
	 * @return */
	@ReflectionHidden
	static MacAddress create(final long macNumber, final long macCount) {
		
		return macCount == 1
			? new MacAddressSingle(macNumber)
			: macCount == 0
				? new MacAddressEmpty(macNumber)
				: new MacAddressRange(macNumber, macCount);
	}
	
	/** @param macNumber
	 * @param colons
	 * @return */
	@ReflectionExplicit
	static String formatMacAddressFromNumber(final long macNumber, final boolean colons) {
		
		final StringBuilder builder = new StringBuilder(//
				12 + (colons
					? 5
					: 0)//
		);
		
		long x = macNumber;
		for (int left = 5; left >= 0; --left, x <<= 8) {
			final int b = 0xff & (int) (x >> 40);
			builder.append(MacAddressSingle.BASE16[b >> 4 & 0x0F]);
			builder.append(MacAddressSingle.BASE16[b >> 0 & 0x0F]);
			if (colons && left > 0) {
				builder.append(MacAddress.FMT_COLON);
			}
		}
		
		return builder.toString();
	}
	
	/** @return */
	@ReflectionExplicit
	static Class<MacAddressEmpty> getMacEmptyClass() {
		
		return MacAddressEmpty.class;
	}
	
	/** @return */
	@ReflectionExplicit
	static Class<MacAddressRange> getMacRangeClass() {
		
		return MacAddressRange.class;
	}
	
	/** @return */
	@ReflectionExplicit
	static Class<MacAddressSet> getMacSetClass() {
		
		return MacAddressSet.class;
	}
	
	/** @return */
	@ReflectionExplicit
	static Class<MacAddressSingle> getMacSingleClass() {
		
		return MacAddressSingle.class;
	}
	
	/** @param macBytes
	 *            in network order
	 * @return */
	@ReflectionExplicit
	static long macBytesToMacNumber(final byte[] macBytes) {
		
		if (macBytes == null) {
			throw new NullPointerException("macBytes are NULL!");
		}
		if (macBytes.length != 6) {
			throw new IllegalArgumentException("macBytes length invalid: " + macBytes.length);
		}
		return (macBytes[0] & 0xFF) << 56 //
				| (macBytes[1] & 0xFF) << 48 //
				| (macBytes[2] & 0xFF) << 40 //
				| (macBytes[3] & 0xFF) << 32 //
				| (macBytes[4] & 0xFF) << 24 //
				| (macBytes[5] & 0xFF) << 16 //
		;
	}
	
	/** Sould parse:
	 *
	 * AABBCCDDEEFF
	 *
	 * aabbccddeeff
	 *
	 * AA:BB:CC:DD:EE:FF
	 *
	 * aa:bb:cc:dd:ee:ff
	 *
	 * @param string
	 * @param soft
	 * @return */
	@ReflectionExplicit
	static long parseSingleMacNumber(final String string, final boolean soft) {
		
		if (string == null) {
			if (soft) {
				return -1;
			}
			throw new NullPointerException("MAC Address String is NULL");
		}
		final String macString = string.trim();
		switch (macString.length()) {
			case 0 : {
				if (soft) {
					return -1;
				}
				throw new NullPointerException("MAC Address String is empty");
			}
			case 12 : {
				// Short format with no colons
				break;
			}
			case 17 : {
				// Long format with colons
				break;
			}
			default : {
				if (soft) {
					return -1;
				}
				throw new IllegalArgumentException("MAC String format is invalid: " + string);
			}
		}
		
		final String macAddressClean = macString.replace(MacAddress.FMT_COLON_STRING, "");
		if (macAddressClean.length() != 12) {
			if (soft) {
				return -1;
			}
			throw new IllegalArgumentException("MAC String format is invalid: " + string);
		}
		
		try {
			final long macNumber = Long.parseLong(macAddressClean, 16);
			if (macNumber >= 0 && macNumber <= 0x0000FFFFFFFFFFFFL) {
				return macNumber;
			}
			if (soft) {
				return -1;
			}
			throw new IllegalArgumentException("MAC macNumber is invalid: " + macNumber);
		} catch (final NumberFormatException e) {
			if (soft) {
				return -1;
			}
			throw new IllegalArgumentException("MAC macNumber is invalid!", e);
		}
		
	}
	
	/** @return */
	default Collection<MacAddress> getAddresses() {
		
		return Collections.singletonList(this);
	}
	
	/** @return */
	@ReflectionExplicit
	String getCompactString();
	
	/** @return */
	@ReflectionExplicit
	String getLongString();
	
	/** @return */
	@ReflectionExplicit
	long getMacCount();
	
	/** @return */
	default long getMacLastNumber() {
		
		final long lastMac = this.getMacNumber() + this.getMacCount() - 1;
		return 0x0000FFFFFFFFFFFFL & lastMac;
	}
	
	/** @return */
	long getMacNumber();
	
	/** Returns an instance of single mac address if it fully represents the range of this
	 * MacAddressObject. NULL otherwise.
	 *
	 * @return */
	MacAddressSingle getMacSingle();
	
	/** @return */
	@ReflectionExplicit
	default String getMacString12() {
		
		final long macNumber = this.getMacNumber();
		if ((macNumber & 0x0000FFFFFFFFFFFFL) == macNumber) {
			return MacAddress.formatMacAddressFromNumber(macNumber, false);
			
		}
		return null;
	}
	
	/** @return */
	@ReflectionExplicit
	default String getMacString17() {
		
		final long macNumber = this.getMacNumber();
		if ((macNumber & 0x0000FFFFFFFFFFFFL) == macNumber) {
			return MacAddress.formatMacAddressFromNumber(macNumber, true);
			
		}
		return null;
	}
	
	/** @return */
	default Collection<MacAddress> getNonEmptyAddresses() {
		
		return this.getMacCount() > 0
			? Collections.singletonList(this)
			: null;
	}
	
	/** @param other
	 * @return */
	@ReflectionExplicit
	MacAddress intersect(final MacAddress other);
	
	/** @return */
	@ReflectionExplicit
	default boolean isEmpty() {
		
		return this.getMacCount() <= 0;
	}
	
	/** @param index
	 * @return */
	@ReflectionExplicit
	MacAddress macAt(final long index);
	
	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param macCount
	 * @return */
	MacAddress slice(final long shift, final long macCount);
	
	/** @param macs
	 * @return */
	@ReflectionExplicit
	MacAddress substract(final MacAddress macs);
	
	@Override
	@ReflectionExplicit
	String toString();
	
	/** @param other
	 * @return */
	@ReflectionExplicit
	MacAddress union(final MacAddress other);
}
