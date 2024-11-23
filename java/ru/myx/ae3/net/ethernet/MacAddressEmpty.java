package ru.myx.ae3.net.ethernet;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class MacAddressEmpty implements MacAddress {
	
	/** @param string
	 * @return */
	public static MacAddressEmpty parse(final String string) {
		
		return MacAddressEmpty.parse(string, false);
	}
	
	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static MacAddressEmpty parse(final String string, final boolean soft) {
		
		if (string == null) {
			if (soft) {
				return null;
			}
			throw new NullPointerException("MAC String is NULL");
		}
		final int macCountIndex = string.indexOf(MacAddress.FMT_COUNT);
		final int macLimitIndex = string.indexOf(MacAddress.FMT_RANGE);
		
		if (macCountIndex != -1) {
			/** Not both! */
			if (macLimitIndex != -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String is invalid: " + string);
			}
			
			final String startString = string.substring(0, macCountIndex);
			final long start = MacAddress.parseSingleMacNumber(startString, soft);
			if (start == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String start is invalid: " + startString);
			}
			
			final long macCount = macCountIndex == -1
				? 1
				: Convert.Any.toLong(string.substring(macCountIndex + 1).trim(), -1);
			
			if (macCount < 0 || macCount > 0x0000FFFFFFFFFFFFL) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is invalid: " + macCount);
			}
			
			if (macCount != 0) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is not zero: " + macCount);
			}
			
			return new MacAddressEmpty(start);
		}
		
		if (macLimitIndex != -1) {
			/** Not both! */
			if (macCountIndex != -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String is invalid: " + string);
			}
			
			final String startString = string.substring(0, macLimitIndex);
			final long start = MacAddress.parseSingleMacNumber(startString, soft);
			if (start == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String start is invalid: " + startString);
			}
			final String limitString = string.substring(macLimitIndex + 1);
			final long limit = MacAddress.parseSingleMacNumber(limitString, soft);
			if (limit == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String limit is invalid: " + limitString);
			}
			if (limit < start) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String start is more than limit: start=" + start + ", limit=" + limit);
			}
			
			final long macCount = limit - start + 1;
			
			if (macCount < 0 || macCount > 0x0000FFFFFFFFFFFFL) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is invalid: " + macCount);
			}
			
			if (macCount != 0) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is not zero: " + macCount);
			}
			
			return new MacAddressEmpty(start);
		}
		
		{
			final long macNumber = MacAddress.parseSingleMacNumber(string, soft);
			if (macNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String is invalid: " + string);
			}
			return new MacAddressEmpty(macNumber);
		}
	}
	
	/** @param string
	 * @return */
	public static MacAddressEmpty parseOrNull(final String string) {
		
		return MacAddressEmpty.parse(string, true);
	}
	
	private final long macNumber;
	
	/** @param macBytes
	 *            in network order */
	public MacAddressEmpty(final byte[] macBytes) {
		
		this.macNumber = MacAddress.macBytesToMacNumber(macBytes);
	}
	
	/** @param macNumber
	 */
	public MacAddressEmpty(final long macNumber) {
		
		this.macNumber = macNumber & 0x0000FFFFFFFFFFFFL;
	}
	
	/** @return */
	@Override
	public String getCompactString() {
		
		if (this.macNumber == 0) {
			return "";
		}

		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, false);
		return macString + MacAddress.FMT_COUNT + "0";
	}
	
	/** @return */
	@Override
	public String getLongString() {
		
		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, true);
		return macString + MacAddress.FMT_COUNT + "0";
	}
	
	/** @return */
	@Override
	public long getMacCount() {
		
		return 0;
	}
	
	/** @return */
	@Override
	public long getMacNumber() {
		
		return this.macNumber;
	}
	
	@Override
	public MacAddressSingle getMacSingle() {
		
		return null;
	}
	
	/** @return */
	@Override
	public String getMacString17() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, true);
	}
	
	/** @param other
	 * @return */
	@Override
	public MacAddress intersect(final MacAddress other) {
		
		return MacAddress.NULL_ADDRESS;
	}
	
	/** @param index
	 * @return */
	@Override
	public MacAddress macAt(final long index) {
		
		return null;
	}
	
	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param macCount
	 * @return */
	@Override
	public MacAddressEmpty slice(final long shift, final long macCount) {
		
		return null;
	}
	
	@Override
	public MacAddress substract(final MacAddress macs) {
		
		return MacAddress.NULL_ADDRESS;
	}
	
	@Override
	public String toString() {
		
		if (this.macNumber == 0) {
			return "";
		}

		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, true);
		return macString + MacAddress.FMT_COUNT + "0";
	}
	
	/** @param other
	 * @return */
	@Override
	public MacAddress union(final MacAddress other) {
		
		return other;
	}
}
