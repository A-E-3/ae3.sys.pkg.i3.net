package ru.myx.ae3.net.ethernet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

/** @author myx */
public class MacAddressSet implements MacAddress /* , Collection<MacAddress> */ {

	/** @return */
	public static MacAddressSet create() {

		return new MacAddressSet();
	}

	/** @param string
	 * @param soft
	 * @return */
	public static MacAddressSet parse(final String string, final boolean soft) {

		final MacAddressSet result = new MacAddressSet();
		if (string == null || string.isBlank()) {
			return result;
		}

		for (final StringTokenizer st = new StringTokenizer(string, Character.toString(MacAddress.FMT_UNION)); st.hasMoreTokens();) {
			result.addAddress(MacAddressRange.parse(st.nextToken(), soft));
		}

		// ? result.normalizeRanges();

		return result;
	}

	/** @param string
	 * @return */
	public static MacAddressSet parseOrDie(final String string) {

		return MacAddressSet.parse(string, false);
	}

	/** @param string
	 * @return */
	public static MacAddressSet parseOrNull(final String string) {

		return MacAddressSet.parse(string, true);
	}

	/** no auto-sorting, order of appearance is important */
	private List<MacAddress> set = null;

	/** @param address
	 * @return */
	public MacAddressSet addAddress(final MacAddress address) {

		if (address == null || address.isEmpty()) {
			return this;
		}
		if (this.set == null) {
			this.set = new ArrayList<>();
		}
		if (address instanceof MacAddressSet) {
			final Collection<MacAddress> nonEmptyAddresses = ((MacAddressSet) address).getNonEmptyAddresses();
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
	public MacAddressSet addAddressSet(final MacAddressSet addressSet) {

		if (addressSet == null || addressSet.isEmpty()) {
			return this;
		}
		if (this.set == null) {
			this.set = new ArrayList<>();
		}
		final Collection<MacAddress> nonEmptyAddresses = addressSet.getNonEmptyAddresses();
		if (nonEmptyAddresses != null && !nonEmptyAddresses.isEmpty()) {
			this.set.addAll(nonEmptyAddresses);
		}
		return this;
	}

	/** @return */
	@Override
	public Collection<MacAddress> getAddresses() {

		return this.set;
	}

	/** @return */
	public Collection<MacAddressRange> getAllRanges() {

		final List<MacAddressRange> result = new ArrayList<>(this.set.size());
		for (final MacAddress address : this.set) {
			if (address instanceof MacAddressRange) {
				result.add((MacAddressRange) address);
				continue;
			}
			if (address instanceof MacAddressSet) {
				result.addAll(((MacAddressSet) address).getAllRanges());
				continue;
			}
			if (address instanceof MacAddressSingle) {
				result.add(new MacAddressRange(((MacAddressSingle) address).getMacNumber(), 1));
				continue;
			}
			throw new UnsupportedOperationException("Unknown MacAddress class: " + address.getClass().getSimpleName());
		}
		return result;
	}

	/** @return */
	@Override
	public String getCompactString() {

		final List<MacAddress> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		for (final MacAddress address : set) {
			if (builder.length() > 0) {
				builder.append(MacAddress.FMT_UNION);
			}
			builder.append(address.getCompactString());
		}

		return builder.toString();
	}

	/** @return */
	@Override
	public String getLongString() {

		final List<MacAddress> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		for (final MacAddress address : set) {
			if (builder.length() > 0) {
				builder.append(' ').append(MacAddress.FMT_UNION).append(' ');
			}
			builder.append(address.getLongString());
		}

		return builder.toString();
	}

	/** @return */
	@Override
	public long getMacCount() {

		final List<MacAddress> set = this.set;
		if (set == null || set.isEmpty()) {
			return 0;
		}

		long macCount = 0;
		for (final MacAddress address : set) {
			macCount += address.getMacCount();
		}

		return macCount;
	}

	/** returns -1 */
	@Override
	public long getMacNumber() {

		return -1;
	}

	@Override
	public MacAddressSingle getMacSingle() {

		/** MacAddress pool is never MacAddressSingle, even if there is exactly one address in the
		 * pool. */
		return null;
	}

	/** @return */
	@Override
	public Collection<MacAddress> getNonEmptyAddresses() {

		final List<MacAddress> result = new ArrayList<>(this.set.size());
		for (final MacAddress address : this.set) {
			if (address.getMacCount() > 0) {
				result.add(address);
			}
		}
		return result.isEmpty()
			? null
			: result;
	}

	@Override
	public MacAddress intersect(final MacAddress other) {

		if (other == null || other.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}

		if (this.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}

		MacAddress intersected = MacAddress.NULL_ADDRESS;
		for (final MacAddress current : this.set) {
			intersected = intersected.union(other.intersect(current));
		}
		return intersected;
	}

	/** @return */
	@Override
	public boolean isEmpty() {

		return this.set == null || this.set.isEmpty();
	}

	/** @param index
	 * @return */
	@Override
	public MacAddress macAt(final long index) {

		if (index < 0) {
			return null;
		}
		final List<MacAddress> set = this.set;
		if (set == null || set.isEmpty()) {
			return null;
		}

		long left = index;
		for (final MacAddress address : set) {
			final long macCount = address.getMacCount();
			if (macCount < left + 1) {
				left -= macCount;
				continue;
			}
			return address.macAt(left);
		}

		return null;
	}

	/** returns SAME this
	 *
	 * @return same `this` */
	public MacAddressSet normalizeRanges() {

		final List<MacAddress> set = this.set;

		Collections.sort(set, new Comparator<MacAddress>() {

			@Override
			public int compare(final MacAddress a, final MacAddress b) {

				return a.getMacNumber() < b.getMacNumber()
					? -1
					: a.getMacNumber() > b.getMacNumber()
						? 1
						: 0;
			}
		});

		focus : for (int f = 0;; ++f) {
			final int count = set.size();
			if (f >= count - 1) {
				return this;
			}

			final MacAddress focus = set.get(f);
			final long tmn = focus.getMacNumber();
			final long tml = focus.getMacLastNumber();

			other : for (int o = f + 1; o < count; ++o) {
				final MacAddress other = set.get(o);
				final long omn = other.getMacNumber();
				final long oml = other.getMacLastNumber();
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
	public MacAddress slice(final long shift, final long macCount) {
		
		if (macCount <= 0 || this.isEmpty()) {
			return null;
		}
		
		long shiftLeft = shift, countLeft = macCount;
		MacAddressSet collector = null;
		
		for (final MacAddress address : this.set) {
			final long itemCount = address.getMacCount();
			final long itemLeft = itemCount - shiftLeft;
			if (itemLeft < 0) {
				shiftLeft -= itemCount;
				continue;
			}
			if (collector == null) {
				if (itemLeft >= countLeft) {
					return address.slice(shiftLeft, countLeft);
				}
				collector = new MacAddressSet();
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
	public MacAddress substract(final MacAddress other) {

		if (other == null || other.isEmpty()) {
			return this;
		}

		if (this.isEmpty()) {
			return MacAddress.NULL_ADDRESS;
		}

		MacAddress substracted = MacAddress.NULL_ADDRESS;
		for (final MacAddress current : this.set) {
			substracted = substracted.union(current.substract(other));
		}
		return substracted;
	}

	@Override
	public String toString() {

		final List<MacAddress> set = this.set;
		if (set == null || set.isEmpty()) {
			return "";
		}

		final StringBuilder builder = new StringBuilder();
		for (final MacAddress address : set) {
			if (builder.length() > 0) {
				builder.append(MacAddress.FMT_UNION);
			}
			builder.append(address.toString());
		}

		return builder.toString();
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
			return new MacAddressSet().addAddressSet(this).addAddressSet((MacAddressSet) other).normalizeRanges();
		}

		return new MacAddressSet().addAddressSet(this).addAddress(other).normalizeRanges();
	}
}
