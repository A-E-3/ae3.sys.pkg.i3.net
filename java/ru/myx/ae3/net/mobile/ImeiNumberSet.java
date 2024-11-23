package ru.myx.ae3.net.mobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

/** @author myx */
public class ImeiNumberSet implements ImeiNumber /* , Collection<ImeiNumber> */ {
	
	/** @return */
	public static ImeiNumberSet create() {
		
		return new ImeiNumberSet();
	}
	
	/** @param string
	 * @param soft
	 * @return */
	public static ImeiNumberSet parse(final String string, final boolean soft) {
		
		final ImeiNumberSet result = new ImeiNumberSet();
		if (string == null || string.isBlank()) {
			return result;
		}
		
		for (final StringTokenizer st = new StringTokenizer(string, Character.toString(ImeiNumber.FMT_UNION)); st.hasMoreTokens();) {
			result.addAddress(ImeiNumberRange.parse(st.nextToken(), soft));
		}
		
		// ? result.normalizeRanges();
		
		return result;
	}
	
	/** @param string
	 * @return */
	public static ImeiNumberSet parseOrDie(final String string) {
		
		return ImeiNumberSet.parse(string, false);
	}
	
	/** @param string
	 * @return */
	public static ImeiNumberSet parseOrNull(final String string) {
		
		return ImeiNumberSet.parse(string, true);
	}
	
	/** no auto-sorting, order of appearance is important */
	private List<ImeiNumber> set = null;
	
	/** @param address
	 * @return */
	public ImeiNumberSet addAddress(final ImeiNumber address) {
		
		if (address == null || address.isEmpty()) {
			return this;
		}
		if (this.set == null) {
			this.set = new ArrayList<>();
		}
		if (address instanceof ImeiNumberSet) {
			final Collection<ImeiNumber> nonEmptyAddresses = ((ImeiNumberSet) address).getNonEmptyAddresses();
			if (nonEmptyAddresses != null && !nonEmptyAddresses.isEmpty()) {
				this.set.addAll(nonEmptyAddresses);
			}
			return this;
		}
		this.set.add(address);
		return this;
	}
	
	/** @param addressSet
	 * @return */
	public ImeiNumberSet addAddressSet(final ImeiNumberSet addressSet) {
		
		if (addressSet == null || addressSet.isEmpty()) {
			return this;
		}
		if (this.set == null) {
			this.set = new ArrayList<>();
		}
		final Collection<ImeiNumber> nonEmptyAddresses = addressSet.getNonEmptyAddresses();
		if (nonEmptyAddresses != null && !nonEmptyAddresses.isEmpty()) {
			this.set.addAll(nonEmptyAddresses);
		}
		return this;
	}
	
	/** @return */
	@Override
	public Collection<ImeiNumber> getAddresses() {
		
		return this.set;
	}
	
	/** @return */
	public Collection<ImeiNumberRange> getAllRanges() {
		
		final List<ImeiNumberRange> result = new ArrayList<>(this.set.size());
		for (final ImeiNumber address : this.set) {
			if (address instanceof ImeiNumberRange) {
				result.add((ImeiNumberRange) address);
				continue;
			}
			if (address instanceof ImeiNumberSet) {
				result.addAll(((ImeiNumberSet) address).getAllRanges());
				continue;
			}
			if (address instanceof ImeiNumberSingle) {
				result.add(new ImeiNumberRange(((ImeiNumberSingle) address).getImeiNumber(), 1));
				continue;
			}
			throw new UnsupportedOperationException("Unknown ImeiNumber class: " + address.getClass().getSimpleName());
		}
		return result;
	}
	
	/** @return */
	@Override
	public String getCompactString() {
		
		final List<ImeiNumber> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}
		
		final StringBuilder builder = new StringBuilder();
		for (final ImeiNumber address : set) {
			if (builder.length() > 0) {
				builder.append(ImeiNumber.FMT_UNION);
			}
			builder.append(address.getCompactString());
		}
		
		return builder.toString();
	}
	
	/** @return */
	@Override
	public long getImeiCount() {
		
		final List<ImeiNumber> set = this.set;
		if (set == null || set.isEmpty()) {
			return 0;
		}
		
		long imeiCount = 0;
		for (final ImeiNumber address : set) {
			imeiCount += address.getImeiCount();
		}
		
		return imeiCount;
	}
	
	@Override
	public long getImeiNumber() {
		
		return -1;
	}
	
	@Override
	public ImeiNumberSingle getImeiSingle() {
		
		/** ImeiNumber pool is never ImeiNumberSingle, even if there is exactly one address in the
		 * pool. */
		return null;
	}
	
	/** @return */
	@Override
	public String getLongString() {
		
		final List<ImeiNumber> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}
		
		final StringBuilder builder = new StringBuilder();
		for (final ImeiNumber address : set) {
			if (builder.length() > 0) {
				builder.append(' ').append(ImeiNumber.FMT_UNION).append(' ');
			}
			builder.append(address.getLongString());
		}
		
		return builder.toString();
	}
	
	/** @return */
	@Override
	public Collection<ImeiNumber> getNonEmptyAddresses() {
		
		final List<ImeiNumber> result = new ArrayList<>(this.set.size());
		for (final ImeiNumber address : this.set) {
			if (address.getImeiCount() > 0) {
				result.add(address);
			}
		}
		return result.isEmpty()
			? null
			: result;
	}
	
	/** @param index
	 * @return */
	@Override
	public ImeiNumber imeiAt(final long index) {
		
		if (index < 0) {
			return null;
		}
		final List<ImeiNumber> set = this.set;
		if (set == null || set.isEmpty()) {
			return null;
		}
		
		long left = index;
		for (final ImeiNumber address : set) {
			final long imeiCount = address.getImeiCount();
			if (imeiCount < left + 1) {
				left -= imeiCount;
				continue;
			}
			return address.imeiAt(left);
		}
		
		return null;
	}
	
	@Override
	public ImeiNumber intersect(final ImeiNumber other) {
		
		if (other == null || other.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}
		
		if (this.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}
		
		ImeiNumber intersected = ImeiNumber.NULL_IMEI;
		for (final ImeiNumber current : this.set) {
			intersected = intersected.union(other.intersect(current));
		}
		return intersected;
	}
	
	/** @return */
	@Override
	public boolean isEmpty() {
		
		return this.set == null || this.set.isEmpty();
	}
	
	/** returns SAME this
	 *
	 * @return same `this` */
	public ImeiNumberSet normalizeRanges() {
		
		final List<ImeiNumber> set = this.set;
		
		Collections.sort(set, new Comparator<ImeiNumber>() {
			
			@Override
			public int compare(final ImeiNumber a, final ImeiNumber b) {
				
				return a.getImeiNumber() < b.getImeiNumber()
					? -1
					: a.getImeiNumber() > b.getImeiNumber()
						? 1
						: 0;
			}
		});
		
		focus : for (int f = 0;; ++f) {
			final int count = set.size();
			if (f >= count - 1) {
				return this;
			}
			
			final ImeiNumber focus = set.get(f);
			final long tmn = focus.getImeiNumber();
			final long tml = focus.getImeiLastNumber();
			
			other : for (int o = f + 1; o < count; ++o) {
				final ImeiNumber other = set.get(o);
				final long omn = other.getImeiNumber();
				final long oml = other.getImeiLastNumber();
				/** non-intersecting, non-connected */
				if (omn > tml + 1 || oml + 1 < tmn) {
					continue other;
				}
				
				set.set(f, focus.union(other));
				set.remove(o);
				--f;
				continue focus;
			}
		}
	}
	
	@Override
	public ImeiNumber slice(final long shift, final long imeiCount) {
		
		if (imeiCount <= 0 || this.isEmpty()) {
			return null;
		}
		
		long shiftLeft = shift, countLeft = imeiCount;
		ImeiNumberSet collector = null;
		
		for (final ImeiNumber address : this.set) {
			final long itemCount = address.getImeiCount();
			final long itemLeft = itemCount - shiftLeft;
			if (itemLeft < 0) {
				shiftLeft -= itemCount;
				continue;
			}
			if (collector == null) {
				if (itemLeft >= countLeft) {
					return address.slice(shiftLeft, countLeft);
				}
				collector = new ImeiNumberSet();
				collector.addAddress(address.slice(shiftLeft, itemLeft));
				shiftLeft = 0;
				countLeft -= itemLeft;
				continue;
			}
			{
				if (itemLeft >= countLeft) {
					collector.addAddress(address.slice(shiftLeft, countLeft));
					return collector.normalizeRanges();
				}
				collector.addAddress(address.slice(shiftLeft, itemLeft));
				shiftLeft = 0;
				countLeft -= itemLeft;
				continue;
			}
		}
		
		return null;
	}
	
	@Override
	public ImeiNumber substract(final ImeiNumber other) {
		
		if (other == null || other.isEmpty()) {
			return this;
		}
		
		if (this.isEmpty()) {
			return ImeiNumber.NULL_IMEI;
		}
		
		ImeiNumber substracted = ImeiNumber.NULL_IMEI;
		for (final ImeiNumber current : this.set) {
			substracted = substracted.union(current.substract(other));
		}
		return substracted;
	}
	
	@Override
	public String toString() {
		
		final List<ImeiNumber> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}
		
		final StringBuilder builder = new StringBuilder();
		for (final ImeiNumber address : set) {
			if (builder.length() > 0) {
				builder.append(ImeiNumber.FMT_UNION);
			}
			builder.append(address.toString());
		}
		
		return builder.toString();
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
			return new ImeiNumberSet().addAddressSet(this).addAddressSet((ImeiNumberSet) other).normalizeRanges();
		}
		
		return new ImeiNumberSet().addAddressSet(this).addAddress(other).normalizeRanges();
	}
}
