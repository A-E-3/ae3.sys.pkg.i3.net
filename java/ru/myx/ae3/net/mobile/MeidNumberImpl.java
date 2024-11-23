package ru.myx.ae3.net.mobile;

import ru.myx.ae3.net.DeviceIdentifier;

/** https://en.wikipedia.org/wiki/Mobile_equipment_identifier
 *
 * @author myx */
public class MeidNumberImpl extends Number implements DeviceIdentifier {

	private static final long serialVersionUID = -8238628352067379302L;

	private long meidNumber;

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

		return this.meidNumber;
	}
}
