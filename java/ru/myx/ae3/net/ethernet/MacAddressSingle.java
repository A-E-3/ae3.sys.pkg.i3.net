package ru.myx.ae3.net.ethernet;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.net.DeviceIdentifier;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class MacAddressSingle implements MacAddress, DeviceIdentifier {
	
	static final char[] BASE16 = new char[]{
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static MacAddressSingle parse(final String string, final boolean soft) {
		
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
			
			if (macCount != 1) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is not one: " + macCount);
			}
			
			return new MacAddressSingle(start);
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
			
			if (macCount != 1) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("MAC macCount is not one: " + macCount);
			}
			
			return new MacAddressSingle(start);
		}
		
		{
			final long macNumber = MacAddress.parseSingleMacNumber(string, soft);
			if (macNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String is invalid: " + string);
			}
			return new MacAddressSingle(macNumber);
		}
	}
	
	/** @param string
	 * @return */
	public static MacAddressSingle parseOrDie(final String string) {
		
		return MacAddressSingle.parse(string, false);
	}
	
	/** @param string
	 * @return */
	public static MacAddressSingle parseOrNull(final String string) {
		
		return MacAddressSingle.parse(string, true);
	}
	
	private final long macNumber;
	
	/** @param macBytes
	 *            in network order */
	public MacAddressSingle(final byte[] macBytes) {
		
		this.macNumber = MacAddress.macBytesToMacNumber(macBytes);
	}
	
	/** @param macNumber
	 */
	public MacAddressSingle(final long macNumber) {
		
		this.macNumber = macNumber & 0x0000FFFFFFFFFFFFL;
	}
	
	/** @return */
	@Override
	public String getCompactString() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, false);
	}
	
	/** @return */
	@Override
	public String getLongString() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, true);
	}
	
	/** @return */
	@Override
	public long getMacCount() {
		
		return 1;
	}
	
	/** @return */
	@Override
	public long getMacNumber() {
		
		return this.macNumber;
	}
	
	@Override
	public MacAddressSingle getMacSingle() {
		
		return this;
	}
	
	/** @return */
	@Override
	public String getMacString12() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, false);
	}
	
	/** @return */
	@Override
	public String getMacString17() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, true);
	}
	
	@Override
	public MacAddress intersect(final MacAddress other) {
		
		if (other == null || other.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}
		
		if (other instanceof MacAddressSet) {
			return other.intersect(this);
		}
		
		/** non-intersecting */
		if (other.getMacNumber() > this.macNumber || other.getMacLastNumber() < this.macNumber) {
			return MacAddress.NULL_ADDRESS;
		}
		return this;
	}
	
	/** @param index
	 * @return */
	@Override
	public MacAddressSingle macAt(final long index) {
		
		if (index == 0) {
			return this;
		}
		return null;
	}
	
	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param macCount
	 * @return */
	@Override
	public MacAddressSingle slice(final long shift, final long macCount) {
		
		if (shift != 0) {
			return null;
		}
		if (macCount != 1) {
			return null;
		}
		return this;
	}
	
	@Override
	public MacAddress substract(final MacAddress other) {
		
		if (other == null || other.isEmpty()) {
			return this;
		}
		
		if (other instanceof MacAddressSet) {
			return this.substract(other.intersect(this));
		}
		
		/** non-intersecting */
		if (other.getMacNumber() > this.macNumber || other.getMacLastNumber() < this.macNumber) {
			return this;
		}
		
		return MacAddress.NULL_ADDRESS;
	}
	
	@Override
	public String toString() {
		
		return MacAddress.formatMacAddressFromNumber(this.macNumber, true);
	}
	
	@Override
	public MacAddress union(final MacAddress other) {
		
		if (other == null || other.isEmpty()) {
			return this;
		}
		
		if (other instanceof MacAddressSet) {
			return other.union(this);
		}
		
		/** non-intersecting */
		final long omn = other.getMacNumber();
		final long tmn = this.macNumber;
		final long oml = other.getMacLastNumber();
		if (omn > tmn || oml < tmn) {
			
			/** connects left */
			if (tmn + 1 == omn) {
				return MacAddress.create(tmn, 1 + other.getMacCount());
			}
			
			/** connects right */
			if (tmn - 1 == oml) {
				return MacAddress.create(omn, 1 + other.getMacCount());
			}
			
			return MacAddressSet.create().addAddress(this).union(other);
		}
		
		/** overlaps with other (included) */
		if (omn <= tmn && oml >= tmn) {
			return other;
		}
		
		/** completely intersecting */
		return this;
	}
}
