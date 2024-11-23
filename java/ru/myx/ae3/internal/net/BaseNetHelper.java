package ru.myx.ae3.internal.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveBoolean;
import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionManual;
import ru.myx.sapi.FormatSAPI;

/** @author myx */
@ReflectionManual
public final class BaseNetHelper {

	/** @param addressObject
	 * @return
	 * @throws IOException
	 *
	 *             <code>
	 * 		value : function(addr){
			if (!addr) {
				return InetAddress.loopbackAddress;
			}
			if ('string' === typeof addr) {
				return Net.dns.resolveOne(addr);
			}
			{
				return addr;
			}
		}
	 * </code> */
	@ReflectionExplicit
	public static BaseObject inetAddress(//
			final BaseObject addressObject//
	) throws IOException {

		final Object baseValue = addressObject.baseValue();
		if (baseValue == null) {
			return Base.forUnknown(InetAddress.getLoopbackAddress());
		}
		if (baseValue instanceof InetAddress) {
			return addressObject;
		}
		if (addressObject instanceof CharSequence) {
			return Base.forUnknown(InetAddress.getByName(addressObject.baseToJavaString()));
		}
		return addressObject;
	}

	/** @param bufferObject
	 * @param offsetObject
	 * @param lengthObject
	 *            only 4 or 16 values are supported
	 * @return
	 * @throws IOException
	 *			
	 *             <code>
		value : function(buffer, offset, length){
			switch(length){
			case 4:
			case 16:
				return Net.inetAddress( Format.binaryAsInetAddress(buffer, offset, length) );
			default:
				throw new Error("inetAddressFromBuffer: incorrect address length: " + length);
			}
		}
	 * </code> */
	@ReflectionExplicit
	public static BaseObject inetAddressFromBuffer(//
			final BaseObject bufferObject,
			final BaseObject offsetObject,
			final BaseObject lengthObject //
	) throws IOException {

		final byte[] buffer = (byte[]) bufferObject.baseValue();
		final int offset = offsetObject.baseToJavaInteger();
		final int length = lengthObject.baseToJavaInteger();

		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}

		switch (length) {
			case 4 :
			case 16 :

				if (buffer.length - offset < length) {
					throw new IllegalArgumentException("byte[] buffer is too small: length: " + buffer.length + ", offset: " + offset);
				}
				return Base.forUnknown(InetAddress.getByName(FormatSAPI.binaryAsInetAddress(buffer, offset, length).toString()));

			default :

				throw new IllegalArgumentException("inetAddressFromBuffer: invalid address length: " + length);
		}

	}

	/** Checks that random string from an unknown origin is an ip4 address
	 *
	 * @param a
	 * @return */
	@ReflectionExplicit
	public static BasePrimitiveBoolean isValidIPv4(final CharSequence a) {

		/** is there an address at all? */
		if (a == null) {
			return BaseObject.FALSE;
		}

		final int length = a.length();
		/** toot short or too long 8) */
		if (length < 7 || length > 15) {
			return BaseObject.FALSE;
		}

		int lastPeriod = length;
		characterLoop : for (int i = length - 1; i >= 0; --i) {
			final char c = a.charAt(i);
			if (c == '.') {
				switch (lastPeriod - i) {
					case 1 :
						return BaseObject.FALSE;
					case 2 :
					case 3 :
					case 4 :
						lastPeriod = i;
						continue characterLoop;
					default :
						return BaseObject.FALSE;
				}
			}
			if (c >= '0' && c <= '9') {
				switch (lastPeriod - i) {
					case 3 :
						if (c > '2') {
							return BaseObject.FALSE;
						}
						//$FALL-THROUGH$
					case 1 :
					case 2 :
						continue characterLoop;
					default :
						return BaseObject.FALSE;
				}
			}
			return BaseObject.FALSE;
		}
		return BaseObject.TRUE;
	}

	/** @param bufferObject
	 * @param offsetObject
	 * @param lengthObject
	 *            only 6 (4+2) or 18 (16+2) values are supported
	 * @return
	 * @throws IOException
	 *             <code>
		function(buffer, offset, length){
			switch(length){
			case 6:
				return new InetSocketAddress(Net.inetAddress( Format.binaryAsInetAddress4(buffer, offset), ((buffer[offset+4] & 0xFF) << 8) | (buffer[offset+5] & 0xFF)) );
			case 18:
				return new InetSocketAddress(Net.inetAddress( Format.binaryAsInetAddress6(buffer, offset), ((buffer[offset+16] & 0xFF) << 8) | (buffer[offset+17] & 0xFF)) );
			default:
				throw new Error("socketAddressFromBuffer: incorrect address length: " + length);
			}
		}
	 * </code> */
	@ReflectionExplicit
	public static BaseObject socketAddressFromBuffer(//
			final BaseObject bufferObject,
			final BaseObject offsetObject,
			final BaseObject lengthObject //
	) throws IOException {

		final byte[] buffer = (byte[]) bufferObject.baseValue();
		final int offset = offsetObject.baseToJavaInteger();
		final int length = lengthObject.baseToJavaInteger();

		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}

		switch (length) {
			case 6 :

				if (buffer.length - offset < length) {
					throw new IllegalArgumentException("byte[] buffer is too small: length: " + buffer.length + ", offset: " + offset);
				}
				return Base.forUnknown(//
						new InetSocketAddress(//
								InetAddress.getByName(FormatSAPI.binaryAsInetAddress4(buffer, offset).toString()),
								(buffer[offset + 4] & 0xFF) << 8 | buffer[offset + 5] & 0xFF //
						)//
				);

			case 18 :

				if (buffer.length - offset < length) {
					throw new IllegalArgumentException("byte[] buffer is too small: length: " + buffer.length + ", offset: " + offset);
				}
				return Base.forUnknown(//
						new InetSocketAddress(//
								InetAddress.getByName(FormatSAPI.binaryAsInetAddress6(buffer, offset).toString()),
								(buffer[offset + 16] & 0xFF) << 8 | buffer[offset + 17] & 0xFF //
						)//
				);

			default :

				throw new IllegalArgumentException("socketAddressFromBuffer: invalid address length: " + length);
		}

	}
}
