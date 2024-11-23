package ru.myx.ae3.net.ethernet;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class MacAddressRange implements MacAddress {

	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static MacAddressRange parse(final String string, final boolean soft) {

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

			return new MacAddressRange(start, macCount);
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

			return new MacAddressRange(start, macCount);
		}

		{
			final String trimmed = string.trim();
			if (trimmed.length() == 0) {
				return new MacAddressRange(0, 0);
			}

			final long macNumber = MacAddress.parseSingleMacNumber(trimmed, soft);
			if (macNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("MAC String is invalid: " + string);
			}
			return new MacAddressRange(macNumber, 1);
		}
	}

	/** @param string
	 * @return */
	public static MacAddressRange parseOrDie(final String string) {

		return MacAddressRange.parse(string, false);
	}

	/** @param string
	 * @return */
	public static MacAddressRange parseOrNull(final String string) {

		return MacAddressRange.parse(string, true);
	}

	private final long macCount;

	private final long macNumber;

	/** @param macBytes
	 *            in network order
	 * @param macCount
	 */
	public MacAddressRange(final byte[] macBytes, final long macCount) {
		
		this.macNumber = MacAddress.macBytesToMacNumber(macBytes);
		this.macCount = macCount;
	}

	/** @param macNumber
	 * @param macCount
	 */
	public MacAddressRange(final long macNumber, final long macCount) {
		
		this.macNumber = macNumber & 0x0000FFFFFFFFFFFFL;
		this.macCount = macCount;
	}

	/** @return */
	@Override
	public String getCompactString() {

		final long macCount = this.macCount;
		if (macCount == 0 && this.macNumber == 0) {
			return "";
		}
		
		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, false);
		return macCount == 1
			? macString
			: macString + MacAddress.FMT_COUNT + macCount;
	}

	/** @return */
	@Override
	public String getLongString() {

		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, true);
		final long macCount = this.macCount;
		if (macCount == 1) {
			return macString;
		}
		if (macCount == 0) {
			return macString + MacAddress.FMT_COUNT + "0";
		}
		final String macLimit = MacAddress.formatMacAddressFromNumber(this.macNumber + macCount - 1, true);
		return macString + MacAddress.FMT_RANGE + macLimit;
	}

	/** @return */
	@Override
	public long getMacCount() {

		return this.macCount;
	}

	/** @return */
	@Override
	public long getMacNumber() {

		return this.macNumber;
	}

	@Override
	public MacAddressSingle getMacSingle() {

		return this.macCount == 1
			? new MacAddressSingle(this.macNumber)
			: null;
	}

	/** @return */
	@Override
	public String getMacString17() {

		return MacAddress.formatMacAddressFromNumber(this.macNumber, true);
	}

	@Override
	public MacAddress intersect(final MacAddress other) {
		
		if (other == null || other.isEmpty() || this.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}
		
		if (other instanceof MacAddressSet) {
			return other.intersect(this);
		}
		
		final long omn = other.getMacNumber();
		final long oml = other.getMacLastNumber();
		final long tmn = this.macNumber;
		final long tml = this.getMacLastNumber();
		
		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			return MacAddress.NULL_ADDRESS;
		}
		
		/** included in other */
		if (omn <= tmn && oml >= tml) {
			return this;
		}
		
		/** included in this */
		if (tmn <= omn && tml >= oml) {
			return other;
		}
		
		/** overlaps left */
		if (omn <= tmn) {
			return MacAddress.create(tmn, oml - tmn + 1);
		}
		
		/** overlaps right */
		if (oml >= tml) {
			return MacAddress.create(omn, tml - omn + 1);
		}

		return MacAddress.NULL_ADDRESS;
	}

	/** @param index
	 * @return */
	@Override
	public MacAddress macAt(final long index) {

		if (index < 0 || index >= this.macCount) {
			return null;
		}
		return new MacAddressSingle(this.macNumber + index);
	}

	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param macCount
	 * @return */
	@Override
	public MacAddressRange slice(final long shift, final long macCount) {

		if (shift < 0 || shift >= this.macCount) {
			return null;
		}
		if (macCount + shift > this.macCount) {
			return null;
		}
		return new MacAddressRange(this.macNumber + shift, macCount);
	}

	@Override
	public MacAddress substract(final MacAddress other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (this.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}

		if (other instanceof MacAddressSet) {
			return this.substract(other.intersect(this));
		}

		final long omn = other.getMacNumber();
		final long oml = other.getMacLastNumber();
		final long tmn = this.macNumber;
		final long tml = this.getMacLastNumber();

		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			return this;
		}

		/** completely included #2, in other */
		if (tmn >= omn && tml <= oml) {
			return MacAddress.NULL_ADDRESS;
		}

		/** overlaps left */
		if (omn <= tmn && oml >= tmn) {
			return MacAddress.create(oml + 1, tml - oml);
		}

		/** overlaps right */
		if (omn <= tml && oml >= tml) {
			return MacAddress.create(tmn, omn - tmn);
		}

		/** split in the middle */
		return MacAddressSet.create()//
				.addAddress(MacAddress.create(tmn, omn - tmn))//
				.union(MacAddress.create(oml + 1, tml - oml)//
				);
	}

	@Override
	public String toString() {

		final long macCount = this.macCount;
		final String macString = MacAddress.formatMacAddressFromNumber(this.macNumber, true);
		return macCount == 1
			? macString
			: macString + MacAddress.FMT_COUNT + macCount;
	}

	@Override
	public MacAddress union(final MacAddress other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (this.isEmpty()) {
			return other;
		}

		if (other instanceof MacAddressSet) {
			return other.union(this);
		}

		final long omn = other.getMacNumber();
		final long oml = other.getMacLastNumber();
		final long tmn = this.macNumber;
		final long tml = this.getMacLastNumber();

		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			/** connected left */
			if (tmn - 1 == oml) {
				return MacAddress.create(omn, tml - omn + 1);
			}
			/** connected right */
			if (omn - 1 == tml) {
				return MacAddress.create(tmn, oml - tmn + 1);
			}
			return MacAddressSet.create().addAddress(this).union(other);
		}

		/** completely included #1, in this */
		if (omn >= tmn && oml <= tml) {
			return this;
		}

		/** completely included #2, in other */
		if (tmn >= omn && tml <= oml) {
			return other;
		}

		/** overlaps/connects left */
		if (tmn <= oml) {
			return MacAddress.create(tmn, oml - tmn + 1);
		}

		/** overlaps/connects right */
		if (tml >= omn) {
			return MacAddress.create(omn, omn - tml + 1);
		}

		throw new IllegalStateException("invalid: tmn: " + tmn + ", omn: " + omn + ", tml: " + tml + ", oml: " + oml);
	}
}
