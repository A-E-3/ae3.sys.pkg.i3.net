package ru.myx.ae3.net.mobile;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.net.DeviceIdentifier;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class ImeiNumberSingle implements ImeiNumber, DeviceIdentifier {

	static final char[] BASE10 = new char[]{
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
	};

	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static ImeiNumberSingle parse(final String string, final boolean soft) {

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
			if (imeiCount < 0 || imeiCount >= 100000000000000L) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is invalid: " + imeiCount);
			}

			if (imeiCount != 1) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is not one: " + imeiCount);
			}

			return new ImeiNumberSingle(start);
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

			if (imeiCount < 0 || imeiCount > 0x0000FFFFFFFFFFFFL) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is invalid: " + imeiCount);
			}

			if (imeiCount != 1) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is not one: " + imeiCount);
			}

			return new ImeiNumberSingle(start);
		}

		{
			final long imeiNumber = ImeiNumber.parseSingleImeiNumber(string, soft);
			if (imeiNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String is invalid: " + string);
			}
			return new ImeiNumberSingle(imeiNumber);
		}
	}

	/** @param string
	 * @return */
	public static ImeiNumberSingle parseOrDie(final String string) {

		return ImeiNumberSingle.parse(string, false);
	}

	/** @param string
	 * @return */
	public static ImeiNumberSingle parseOrNull(final String string) {

		return ImeiNumberSingle.parse(string, true);
	}

	private final long imeiNumber;

	/** @param imeiBytes
	 *            in network order */
	public ImeiNumberSingle(final byte[] imeiBytes) {

		this.imeiNumber = ImeiNumber.imeiBytesToImeiNumber(imeiBytes);
	}

	/** @param imeiNumber
	 */
	public ImeiNumberSingle(final long imeiNumber) {
		
		this.imeiNumber = (0x7FFFFFFFFFFFFFFFL & imeiNumber) % 100000000000000L;
	}

	/** @return */
	@Override
	public String getCompactString() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, false, false);
	}

	/** @return */
	@Override
	public long getImeiCount() {

		return 1;
	}

	/** @return */
	@Override
	public long getImeiNumber() {

		return this.imeiNumber;
	}

	@Override
	public ImeiNumberSingle getImeiSingle() {

		return this;
	}

	/** @return */
	@Override
	public String getImeiString15() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, false, true);
	}

	/** @return */
	@Override
	public String getImeiString18() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
	}

	/** @return */
	@Override
	public String getLongString() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
	}

	/** @param index
	 * @return */
	@Override
	public ImeiNumberSingle imeiAt(final long index) {

		if (index == 0) {
			return this;
		}
		return null;
	}

	@Override
	public ImeiNumber intersect(final ImeiNumber other) {

		if (other == null || other.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}

		if (other instanceof ImeiNumberSet) {
			return other.intersect(this);
		}

		/** non-intersecting */
		if (other.getImeiNumber() > this.imeiNumber || other.getImeiLastNumber() < this.imeiNumber) {
			return ImeiNumber.NULL_IMEI;
		}
		return this;
	}

	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param imeiCount
	 * @return */
	@Override
	public ImeiNumberSingle slice(final long shift, final long imeiCount) {

		if (shift != 0) {
			return null;
		}
		if (imeiCount != 1) {
			return null;
		}
		return this;
	}

	@Override
	public ImeiNumber substract(final ImeiNumber other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (other instanceof ImeiNumberSet) {
			return this.substract(other.intersect(this));
		}

		/** non-intersecting */
		if (other.getImeiNumber() > this.imeiNumber || other.getImeiLastNumber() < this.imeiNumber) {
			return this;
		}

		return ImeiNumber.NULL_IMEI;
	}

	@Override
	public String toString() {

		return ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
	}

	@Override
	public ImeiNumber union(final ImeiNumber other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (other instanceof ImeiNumberSet) {
			return other.union(this);
		}

		/** non-intersecting */
		final long omn = other.getImeiNumber();
		final long tmn = this.imeiNumber;
		final long oml = other.getImeiLastNumber();
		if (omn > tmn || oml < tmn) {

			/** connects left */
			if (tmn + 1 == omn) {
				return ImeiNumber.create(tmn, 1 + other.getImeiCount());
			}

			/** connects right */
			if (tmn - 1 == oml) {
				return ImeiNumber.create(omn, 1 + other.getImeiCount());
			}

			return ImeiNumberSet.create().addAddress(this).union(other);
		}

		/** overlaps with other (included) */
		if (omn <= tmn && oml >= tmn) {
			return other;
		}

		/** completely intersecting */
		return this;
	}
}
