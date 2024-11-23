package ru.myx.ae3.net.mobile;

import ru.myx.ae3.net.DeviceIdentifier;

/** https://en.wikipedia.org/wiki/International_Mobile_Equipment_Identity
 *
 * @author myx */
public class ImeiNumberImpl extends Number implements DeviceIdentifier {

	private static final long serialVersionUID = -2473389813076213525L;

	private long imeiNumber;

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

		return this.imeiNumber;
	}
}
