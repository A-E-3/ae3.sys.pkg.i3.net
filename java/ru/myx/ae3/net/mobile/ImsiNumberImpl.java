package ru.myx.ae3.net.mobile;

import ru.myx.ae3.net.AccountIdentifier;

/** https://en.wikipedia.org/wiki/International_mobile_subscriber_identity
 *
 * @author myx */
public class ImsiNumberImpl extends Number implements AccountIdentifier {

	private static final long serialVersionUID = 1072748416023159588L;
	private long imsiNumber;

	@Override
	public double doubleValue() {

		return this.longValue();
	}

	@Override
	public float floatValue() {

		return this.longValue();
	}

	@Override
	public int intValue() {

		return (int) this.longValue();
	}

	@Override
	public long longValue() {

		return this.imsiNumber;
	}
}
