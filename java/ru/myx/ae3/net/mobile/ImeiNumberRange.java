package ru.myx.ae3.net.mobile;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class ImeiNumberRange implements ImeiNumber {

	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static ImeiNumberRange parse(final String string, final boolean soft) {

		if (string == null) {
			if (soft) {
				return null;
			}
			throw new NullPointerException("IMEI String is NULL");
		}

		final int imeiCountIndex = string.indexOf(ImeiNumber.FMT_COUNT);
		final int imeiLimitIndex = string.indexOf(ImeiNumber.FMT_RANGE);

		if (imeiCountIndex != -1) {
			/** Not both! */
			if (imeiLimitIndex != -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String is invalid: " + string);
			}

			final String startString = string.substring(0, imeiCountIndex);
			final long start = ImeiNumber.parseSingleImeiNumber(startString, soft);
			if (start == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String start is invalid: " + startString);
			}

			final long imeiCount = imeiCountIndex == -1
				? 1
				: Convert.Any.toLong(string.substring(imeiCountIndex + 1).trim(), -1);

			if (imeiCount < 0 || imeiCount > 100000000000000L) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is invalid: " + imeiCount);
			}

			return new ImeiNumberRange(start, imeiCount);
		}

		if (imeiLimitIndex != -1) {
			/** Not both! */
			if (imeiCountIndex != -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String is invalid: " + string);
			}

			final String startString = string.substring(0, imeiLimitIndex);
			final long start = ImeiNumber.parseSingleImeiNumber(startString, soft);
			if (start == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String start is invalid: " + startString);
			}
			final String limitString = string.substring(imeiLimitIndex + 1);
			final long limit = ImeiNumber.parseSingleImeiNumber(limitString, soft);
			if (limit == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String limit is invalid: " + limitString);
			}
			if (limit < start) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String start is more than limit: start=" + start + ", limit=" + limit);
			}

			final long imeiCount = limit - start + 1;

			if (imeiCount < 0 || imeiCount > 100000000000000L) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is invalid: " + imeiCount);
			}

			return new ImeiNumberRange(start, imeiCount);
		}

		{
			final String trimmed = string.trim();
			if (trimmed.length() == 0) {
				return new ImeiNumberRange(0, 0);
			}

			final long imeiNumber = ImeiNumber.parseSingleImeiNumber(trimmed, soft);
			if (imeiNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String is invalid: " + string);
			}
			return new ImeiNumberRange(imeiNumber, 1);
		}
	}

	/** @param string
	 * @return */
	public static ImeiNumberRange parseOrDie(final String string) {

		return ImeiNumberRange.parse(string, false);
	}

	/** @param string
	 * @return */
	public static ImeiNumberRange parseOrNull(final String string) {

		return ImeiNumberRange.parse(string, true);
	}

	private final long imeiCount;

	private final long imeiNumber;

	/** @param imeiBytes
	 *            in network order
	 * @param imeiCount */
	public ImeiNumberRange(final byte[] imeiBytes, final long imeiCount) {
		
		this.imeiNumber = ImeiNumber.imeiBytesToImeiNumber(imeiBytes);
		this.imeiCount = imeiCount;
	}

	/** @param imeiNumber
	 * @param imeiCount */
	public ImeiNumberRange(final long imeiNumber, final long imeiCount) {
		
		this.imeiNumber = (imeiNumber & 0x7FFFFFFFFFFFFFFFL) % 100000000000000L;
		this.imeiCount = imeiCount;
	}

	/** @return */
	@Override
	public String getCompactString() {

		final long imeiCount = this.imeiCount;
		if (imeiCount == 0 && this.imeiNumber == 0) {
			return "";
		}
		
		final String imeiString = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, false, false);
		return imeiCount == 1
			? imeiString
			: imeiString + ImeiNumber.FMT_COUNT + imeiCount;
	}

	/** @return */
	@Override
	public long getImeiCount() {

		return this.imeiCount;
	}

	/** @return */
	@Override
	public long getImeiNumber() {

		return this.imeiNumber;
	}

	@Override
	public ImeiNumberSingle getImeiSingle() {

		return this.imeiCount == 1
			? new ImeiNumberSingle(this.imeiNumber)
			: null;
	}

	/** @return */
	@Override
	public String getImeiString18() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
	}

	/** @return */
	@Override
	public String getLongString() {

		final String imeiString = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
		final long imeiCount = this.imeiCount;
		if (imeiCount == 1) {
			return imeiString;
		}
		if (imeiCount == 0) {
			return imeiString + ImeiNumber.FMT_COUNT + "0";
		}
		final String imeiLimit = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber + imeiCount - 1, true, true);
		return imeiString + ImeiNumber.FMT_RANGE + imeiLimit;
	}

	/** @param index
	 * @return */
	@Override
	public ImeiNumber imeiAt(final long index) {

		if (index < 0 || index >= this.imeiCount) {
			return null;
		}
		return new ImeiNumberSingle(this.imeiNumber + index);
	}

	@Override
	public ImeiNumber intersect(final ImeiNumber other) {
		
		if (other == null || other.isEmpty() || this.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}
		
		if (other instanceof ImeiNumberSet) {
			return other.intersect(this);
		}
		
		final long omn = other.getImeiNumber();
		final long oml = other.getImeiLastNumber();
		final long tmn = this.imeiNumber;
		final long tml = this.getImeiLastNumber();
		
		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			return ImeiNumber.NULL_IMEI;
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
			return ImeiNumber.create(tmn, oml - tmn + 1);
		}
		
		/** overlaps right */
		if (oml >= tml) {
			return ImeiNumber.create(omn, tml - omn + 1);
		}

		return ImeiNumber.NULL_IMEI;
	}

	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param imeiCount
	 * @return */
	@Override
	public ImeiNumberRange slice(final long shift, final long imeiCount) {

		if (shift < 0 || shift >= this.imeiCount) {
			return null;
		}
		if (imeiCount + shift > this.imeiCount) {
			return null;
		}
		return new ImeiNumberRange(this.imeiNumber + shift, imeiCount);
	}

	@Override
	public ImeiNumber substract(final ImeiNumber other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (this.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}

		if (other instanceof ImeiNumberSet) {
			final ImeiNumber intersected = other.intersect(this);
			if (intersected instanceof ImeiNumberSet) {
				ImeiNumber substracted = this;
				for (final ImeiNumber current : intersected.getAddresses()) {
					substracted = substracted.substract(current);
				}
				return substracted;
			}
			return this.substract(intersected);
		}

		final long omn = other.getImeiNumber();
		final long oml = other.getImeiLastNumber();
		final long tmn = this.imeiNumber;
		final long tml = this.getImeiLastNumber();

		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			return this;
		}

		/** completely included #2, in other */
		if (tmn >= omn && tml <= oml) {
			return ImeiNumber.NULL_IMEI;
		}

		/** overlaps left */
		if (omn <= tmn && oml >= tmn) {
			return ImeiNumber.create(oml + 1, tml - oml);
		}

		/** overlaps right */
		if (omn <= tml && oml >= tml) {
			return ImeiNumber.create(tmn, omn - tmn);
		}

		/** split in the middle */
		return ImeiNumberSet.create()//
				.addAddress(ImeiNumber.create(tmn, omn - tmn))//
				.union(ImeiNumber.create(oml + 1, tml - oml)//
				);
	}

	@Override
	public String toString() {

		final long imeiCount = this.imeiCount;
		final String imeiString = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
		return imeiCount == 1
			? imeiString
			: imeiString + ImeiNumber.FMT_COUNT + imeiCount;
	}

	@Override
	public ImeiNumber union(final ImeiNumber other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (this.isEmpty()) {
			return other;
		}

		if (other instanceof ImeiNumberSet) {
			return other.union(this);
		}

		final long omn = other.getImeiNumber();
		final long oml = other.getImeiLastNumber();
		final long tmn = this.imeiNumber;
		final long tml = this.getImeiLastNumber();

		/** non-intersecting */
		if (omn > tml || oml < tmn) {
			/** connected left */
			if (tmn - 1 == oml) {
				return ImeiNumber.create(omn, tml - omn + 1);
			}
			/** connected right */
			if (omn - 1 == tml) {
				return ImeiNumber.create(tmn, oml - tmn + 1);
			}
			return ImeiNumberSet.create().addAddress(this).union(other);
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
			return ImeiNumber.create(tmn, oml - tmn + 1);
		}

		/** overlaps/connects right */
		if (tml >= omn) {
			return ImeiNumber.create(omn, omn - tml + 1);
		}

		throw new IllegalStateException("invalid: tmn: " + tmn + ", omn: " + omn + ", tml: " + tml + ", oml: " + oml);
	}
}
