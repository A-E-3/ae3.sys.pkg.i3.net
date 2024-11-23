package ru.myx.ae3.net.mobile;

import ru.myx.ae3.help.Convert;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx */
public class ImeiNumberEmpty implements ImeiNumber {
	
	/** @param string
	 * @return */
	public static ImeiNumberEmpty parse(final String string) {
		
		return ImeiNumberEmpty.parse(string, false);
	}
	
	/** @param string
	 * @param soft
	 * @return */
	@ReflectionHidden
	public static ImeiNumberEmpty parse(final String string, final boolean soft) {
		
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
			
			if (imeiCount != 0) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is not zero: " + imeiCount);
			}
			
			return new ImeiNumberEmpty(start);
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
			
			if (imeiCount != 0) {
				if (soft) {
					return null;
				}
				throw new IllegalArgumentException("IMEI Count is not zero: " + imeiCount);
			}
			
			return new ImeiNumberEmpty(start);
		}
		
		{
			final long imeiNumber = ImeiNumber.parseSingleImeiNumber(string, soft);
			if (imeiNumber == -1) {
				if (soft) {
					return null;
				}
				throw new NullPointerException("IMEI String is invalid: " + string);
			}
			return new ImeiNumberEmpty(imeiNumber);
		}
	}
	
	/** @param string
	 * @return */
	public static ImeiNumberEmpty parseOrNull(final String string) {
		
		return ImeiNumberEmpty.parse(string, true);
	}
	
	private final long imeiNumber;
	
	/** @param imeiBytes
	 *            in network order */
	public ImeiNumberEmpty(final byte[] imeiBytes) {
		
		this.imeiNumber = ImeiNumber.imeiBytesToImeiNumber(imeiBytes);
	}
	
	/** @param imeiNumber
	 */
	public ImeiNumberEmpty(final long imeiNumber) {
		
		this.imeiNumber = (imeiNumber & 0x7FFFFFFFFFFFFFFFL) % 100000000000000L;
	}
	
	/** @return */
	@Override
	public String getCompactString() {
		
		if (this.imeiNumber == 0) {
			return "";
		}

		final String imeiString = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, false, false);
		return imeiString + ImeiNumber.FMT_COUNT + "0";
	}
	
	/** @return */
	@Override
	public long getImeiCount() {
		
		return 0;
	}
	
	/** @return */
	@Override
	public long getImeiNumber() {
		
		return this.imeiNumber;
	}
	
	@Override
	public ImeiNumberSingle getImeiSingle() {
		
		return null;
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
		return imeiString + ImeiNumber.FMT_COUNT + "0";
	}
	
	/** @param index
	 * @return */
	@Override
	public ImeiNumber imeiAt(final long index) {
		
		return null;
	}
	
	/** @param other
	 * @return */
	@Override
	public ImeiNumber intersect(final ImeiNumber other) {
		
		return ImeiNumber.NULL_IMEI;
	}
	
	/** Null or new object
	 *
	 *
	 * @param shift
	 * @param imeiCount
	 * @return */
	@Override
	public ImeiNumberEmpty slice(final long shift, final long imeiCount) {
		
		return null;
	}
	
	@Override
	public ImeiNumber substract(final ImeiNumber imeis) {
		
		return ImeiNumber.NULL_IMEI;
	}
	
	@Override
	public String toString() {
		
		if (this.imeiNumber == 0) {
			return "";
		}

		final String imeiString = ImeiNumber.formatImeiNumberFromNumber(this.imeiNumber, true, true);
		return imeiString + ImeiNumber.FMT_COUNT + "0";
	}
	
	/** @param other
	 * @return */
	@Override
	public ImeiNumber union(final ImeiNumber other) {
		
		return other;
	}
}
