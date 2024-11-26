package ru.myx.ae3.internal.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.security.DigestException;
import java.security.MessageDigest;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseArray;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.base.BasePrimitiveNumber;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.base.BaseProperty;
import ru.myx.ae3.base.ToPrimitiveHint;
import ru.myx.ae3.binary.Transfer;
import ru.myx.ae3.binary.TransferCopier;
import ru.myx.ae3.concurrent.CoarseDelayCache;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.exec.ResultHandler;
import ru.myx.ae3.help.Format;
import ru.myx.ae3.reflect.ReflectionContextArgument;
import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionManual;
import ru.myx.ae3.reflect.ReflectionThisArgument;
import ru.myx.ae3.report.Report;
import ru.myx.sapi.FormatSAPI;

/** @author myx */
@ReflectionManual
public final class UdpServiceHelper {
	
	private static final ExecProcess CTX = Exec.createProcess(Exec.getRootProcess(), "UdpServiceHelper Context");
	
	private final static BasePrimitiveNumber INT_32 = Base.forInteger(32);
	
	private final static BasePrimitiveString STR_address = Base.forString("address");
	private final static BasePrimitiveString STR_argument = Base.forString("argument");
	private final static BasePrimitiveString STR_build = Base.forString("build");
	private final static BasePrimitiveString STR_code = Base.forString("code");
	private final static BasePrimitiveString STR_component = Base.forString("component");
	private final static BasePrimitiveString STR_dst = Base.forString("dst");
	private final static BasePrimitiveString STR_encrypt = Base.forString("encrypt");
	private final static BasePrimitiveString STR_handlers = Base.forString("handlers");
	private final static BasePrimitiveString STR_hostAddress = Base.forString("hostAddress");
	private final static BasePrimitiveString STR_key = Base.forString("key");
	private final static BasePrimitiveString STR_log = Base.forString("log");
	private final static BasePrimitiveString STR_mode = Base.forString("mode");
	private final static BasePrimitiveString STR_onDestroy = Base.forString("onDestroy");
	private final static BasePrimitiveString STR_port = Base.forString("port");
	private final static BasePrimitiveString STR_secret = Base.forString("secret");
	private final static BasePrimitiveString STR_sendUdp = Base.forString("sendUdp");
	
	private final static BasePrimitiveString STR_serial = Base.forString("serial");
	private final static BasePrimitiveString STR_receiveQuerySerialsCache = Base.forString("receiveQuerySerialsCache");
	private final static BasePrimitiveString STR_receiveReplySerialsCache = Base.forString("receiveReplySerialsCache");
	private final static BasePrimitiveString STR_waitingTaskSerialsCache = Base.forString("waitingTaskSerialsCache");
	
	private final static BasePrimitiveString STR_src = Base.forString("src");
	private final static BasePrimitiveString STR_sRx = Base.forString("sRx");
	private final static BasePrimitiveString STR_sTx = Base.forString("sTx");

	/** @param instance
	 * @param bufferObject
	 * @param offsetObject
	 * @return
	 * @throws IOException
	 *
	 *             <code>
		function(b, o){
			var l = 0;
			// component X bytes
			var component = Transfer.createCopierUtf8(this.component);
			l += component.copy(0, b, o, 128);
			// ZERO byte
			b[o + l] = 0;
			++ l;
			// argument X bytes
			if(this.argument){
				l += this.argument.copy(0, b, o + l, 1024);
			}
			return l;
		}
	 </code> */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int buildMsgCall(//
			final BaseObject instance,
			final BaseObject bufferObject,
			final BaseObject offsetObject//
	) throws IOException {
		
		final byte[] buffer = (byte[]) bufferObject.baseValue();
		final int offset = offsetObject.baseToJavaInteger();
		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		if (buffer.length - offset < 1400) {
			throw new IllegalArgumentException("byte[] buffer is too small: length: " + buffer.length + ", offset: " + offset);
		}
		
		final String component = instance.baseGet(UdpServiceHelper.STR_component, BaseObject.UNDEFINED).baseToJavaString();
		final TransferCopier argument = Transfer.createCopierFromBinary(instance.baseGet(UdpServiceHelper.STR_argument, TransferCopier.NUL_COPIER));
		
		int length = 0;
		
		// component X bytes
		length += Transfer.createCopierUtf8(component).copy(0, buffer, offset, 128);
		
		// ZERO byte
		buffer[offset + length] = 0;
		length += 1;
		
		// argument X bytes
		if (argument != null) {
			length += argument.copy(0, buffer, offset + length, 1024);
		}
		
		return length;
	}
	
	/** @param instance
	 * @param bufferObject
	 * @param offsetObject
	 * @return
	 * @throws IllegalArgumentException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int buildMsgHelo(//
			final BaseObject instance,
			final BaseObject bufferObject,
			final BaseObject offsetObject) throws IllegalArgumentException {
		
		final byte[] buffer = (byte[]) bufferObject.baseValue();
		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		
		int offset = offsetObject.baseToJavaInteger();
		if (offset + 6 >= buffer.length) {
			throw new IllegalArgumentException("invalid offset: buffer length: " + buffer.length + ", offset: " + offset);
		}
		
		final BaseObject srcObject = instance//
				.baseGet(UdpServiceHelper.STR_src, BaseObject.UNDEFINED)//
		;
		if (srcObject == BaseObject.UNDEFINED) {
			throw new IllegalArgumentException("src object is expected, instance class: " + instance.getClass().getName());
		}
		
		final BaseObject addressObject = srcObject//
				.baseGet(UdpServiceHelper.STR_address, BaseObject.UNDEFINED)//
				.baseGet(UdpServiceHelper.STR_address, BaseObject.UNDEFINED)//
		;
		final byte[] address = (byte[]) addressObject.baseValue();
		if (address == null) {
			throw new IllegalArgumentException("byte[] address is expected, addressObject class: " + addressObject.getClass().getName());
		}
		
		final int port = srcObject//
				.baseGet(UdpServiceHelper.STR_port, BaseObject.UNDEFINED)//
				.baseToJavaInteger()//
		;
		
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("port number is invalid, portNnumber: " + port + ", srcObject class: " + srcObject.getClass().getName());
		}
		
		buffer[offset++] = address[0];
		buffer[offset++] = address[1];
		buffer[offset++] = address[2];
		buffer[offset++] = address[3];
		
		buffer[offset++] = (byte) ((port & 0xFF00) >> 8);
		buffer[offset++] = (byte) (port & 0xFF);
		
		// return BaseNumber.SIX;
		return 6;
	}
	
	/** @param instance
	 * @param bufferObject
	 * @param offsetObject
	 * @return
	 * @throws IllegalArgumentException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int buildMsgPoke(//
			final BaseObject instance,
			final BaseObject bufferObject,
			final BaseObject offsetObject) throws IllegalArgumentException {
		
		/** exactly the same */
		return UdpServiceHelper.buildMsgHelo(instance, bufferObject, offsetObject);
	}
	
	/** @param instance
	 * @param bufferObject
	 * @param offsetObject
	 * @return
	 * @throws IllegalArgumentException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int buildMsgSeen(//
			final BaseObject instance,
			final BaseObject bufferObject,
			final BaseObject offsetObject) throws IllegalArgumentException {
		
		final byte[] buffer = (byte[]) bufferObject.baseValue();
		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		
		int offset = offsetObject.baseToJavaInteger();
		if (offset + 3 >= buffer.length) {
			throw new IllegalArgumentException("invalid offset: buffer length: " + buffer.length + ", offset: " + offset);
		}
		
		final int port = instance//
				.baseGet(UdpServiceHelper.STR_port, BaseObject.UNDEFINED)//
				.baseToJavaInteger()//
		;
		
		if (port <= 0 || port > 65535) {
			throw new IllegalArgumentException("port number is invalid, portNnumber: " + port + ", instanceObject class: " + instance.getClass().getName());
		}
		
		final int mode = instance//
				.baseGet(UdpServiceHelper.STR_mode, BaseObject.UNDEFINED)//
				.baseToJavaInteger()//
		;
		
		if (mode < 0 || mode > 255) {
			throw new IllegalArgumentException("mode number is invalid, modeNnumber: " + mode + ", instanceObject class: " + instance.getClass().getName());
		}
		
		buffer[offset++] = (byte) ((port & 0xFF00) >> 8);
		buffer[offset++] = (byte) (port & 0xFF);
		
		buffer[offset++] = (byte) (mode & 0xFF);
		
		// return BaseNumber.THREE;
		return 3;
	}
	
	/** finished task expired
	 *
	 * @param instance
	 * @param serial
	 *
	 *
	 *            <code>
		function(serial, task){
			if(this.sRx < serial && (serial > 16000000) === (this.sRx > 16000000)){
				this.sRx = serial;
			}
		}
	 </code> */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static void expireReceiveCachedSerial(final BaseObject instance, final BaseObject serial) {
		
		final int thisRx = instance.baseGet(UdpServiceHelper.STR_sRx, BasePrimitiveNumber.ZERO).baseToJavaInteger();
		final int thisTx = instance.baseGet(UdpServiceHelper.STR_sTx, BasePrimitiveNumber.ZERO).baseToJavaInteger();
		final int taskAx = serial.baseToJavaInteger();
		if (thisRx < taskAx && thisTx > taskAx && taskAx > 16000000 == thisRx > 16000000) {
			instance.baseDefine(UdpServiceHelper.STR_sRx, serial, BaseProperty.ATTRS_MASK_WED);
		}
	}
	
	/** pending task timeout expired...
	 *
	 * @param ctx
	 * @param instance
	 * @param serial
	 * @param task
	 *
	 *
	 *            <code>
	function(serial, task){
		this.receiveReplySerialsCache.put(serial, task);
		task.onDestroy();
	}
	 </code> */
	@ReflectionExplicit
	@ReflectionContextArgument
	@ReflectionThisArgument
	public static void expireWaitingTaskSerial(final ExecProcess ctx, final BaseObject instance, final BaseObject serial, final BaseObject task) {
		
		final BaseObject receiveReplySerialsCache = instance.baseGet(UdpServiceHelper.STR_receiveReplySerialsCache, BaseObject.UNDEFINED);
		if (receiveReplySerialsCache instanceof final CoarseDelayCache coarseDelayCache) {
			coarseDelayCache.put(serial.baseToInt32(), task);
		} else {
			receiveReplySerialsCache.baseDefine(serial.baseToInt32(), task);
		}
		final BaseFunction onDestroy = task.baseGet(UdpServiceHelper.STR_onDestroy, BaseObject.UNDEFINED).baseCall();
		if (onDestroy == null) {
			return;
		}
		onDestroy.callVE0(ctx, task);
	}
	
	/** @param instance
	 *            - Principal
	 * @param pktObject
	 * @param bufferObject
	 * @param offsetObject
	 * @param payloadLengthObject
	 * @param digestObject
	 * @return
	 * @throws IOException
	 *
	 *             <code>
		payloadDecrypt : {
			value : UdpServiceHelper.payloadDecrypt || (function(pkt, b, offset, payloadLength, digest){
				digest = digest.clone();
				Transfer.updateMessageDigest(digest, pkt, 16, 16);
				this.secret.updateMessageDigest(digest);
				Transfer.xorBytes(pkt, 32, digest.result, b, offset, payloadLength);
			})
		},
	 </code>
	 * @throws CloneNotSupportedException
	 * @throws DigestException
	 * @throws IllegalArgumentException
	 * @throws ConcurrentModificationException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int payloadDecrypt(//
			final BaseObject instance,
			final BaseObject pktObject,
			final BaseObject bufferObject,
			final BaseObject offsetObject,
			final BaseObject payloadLengthObject,
			final BaseObject digestObject) throws IOException, CloneNotSupportedException, ConcurrentModificationException, IllegalArgumentException, DigestException {
		
		final TransferCopier pkt = Transfer.createCopierFromBinary(pktObject);
		if (pkt == null) {
			throw new IllegalArgumentException("'pkt' binary argument is expected, pkt: " + pkt);
		}
		
		final int offset = offsetObject.baseToJavaInteger();
		final int payloadLength = payloadLengthObject.baseToJavaInteger();
		
		final byte[] buffer = (byte[]) bufferObject.baseValue();
		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		
		final TransferCopier secret = Transfer.createCopierFromBinary(instance.baseGet(UdpServiceHelper.STR_secret, TransferCopier.NUL_COPIER));
		if (secret == null || secret.length() == 0) {
			throw new IllegalArgumentException("non empty 'secret' binary property is expected, secret: " + secret);
		}
		
		final MessageDigest digest = (MessageDigest) ((MessageDigest) digestObject.baseValue()).clone();
		Transfer.updateMessageDigest(digest, pkt, 16, 16);
		secret.updateMessageDigest(digest);
		Transfer.xorBytes(pkt, 32, digest.digest(), buffer, offset, payloadLength);
		return payloadLength;
	}
	/** @param instance
	 *            - Principal
	 * @param bufferObject
	 * @param payloadLengthObject
	 * @param digestObject
	 * @return
	 * @throws IOException
	 *
	 *             <code>
		payloadEncrypt : {
			value : function(b, payloadLength, digest){
				digest = digest.clone();
				Transfer.updateMessageDigest(digest, b, 16, 16);
				this.secret.updateMessageDigest(digest);
				Transfer.xorBytes(b, 32, digest.result, payloadLength);
			}
		},
	 </code>
	 * @throws CloneNotSupportedException
	 * @throws DigestException
	 * @throws IllegalArgumentException
	 * @throws ConcurrentModificationException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static int payloadEncrypt(final BaseObject instance, final BaseObject bufferObject, final BaseObject payloadLengthObject, final BaseObject digestObject)
			throws IOException, CloneNotSupportedException, ConcurrentModificationException, IllegalArgumentException, DigestException {
		
		final byte[] buffer = (byte[]) bufferObject.baseValue();
		if (buffer == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		final int payloadLength = payloadLengthObject.baseToJavaInteger();
		final TransferCopier secret = Transfer.createCopierFromBinary(instance.baseGet(UdpServiceHelper.STR_secret, TransferCopier.NUL_COPIER));
		if (secret == null || secret.length() == 0) {
			throw new IllegalArgumentException("non empty 'secret' binary property is expected, secret: " + secret);
		}
		
		final MessageDigest digest = (MessageDigest) ((MessageDigest) digestObject.baseValue()).clone();
		Transfer.updateMessageDigest(digest, buffer, 16, 16);
		secret.updateMessageDigest(digest);
		Transfer.xorBytes(buffer, 32, digest.digest(), payloadLength);
		return payloadLength;
	}
	
	/** @param ctx
	 * @param instance
	 * @param messageObject
	 * @param addressObject
	 * @param serialObject
	 *
	 *            <code>
		onReceive : {
			value : function(message, address, serial, c, h){
				if(this.sTx < serial && (serial > 16000000) === (this.sTx > 16000000)){
					this.sTx = serial;
				}
	
				c = message.code;
				if( ((c^0) !== c) ){
					return;
				}
				h = this.handlers[c];
				if(h){
					for(c of h){
						setTimeout(c.bind(this, message, address, serial), 0);
						// c(message, address, serial);
					}
					return;
				}
				return;
			}
		},
	 </code> */
	@ReflectionExplicit
	@ReflectionContextArgument
	@ReflectionThisArgument
	public static void principalOnReceive(//
			final ExecProcess ctx,
			final BaseObject instance,
			final BaseObject messageObject,
			final BaseObject addressObject,
			final BaseObject serialObject//
	) {
		
		/** <code>
			if(this.sTx < serial && (serial > 16000000) === (this.sTx > 16000000)){
				this.sTx = serial;
			}
		</code> */
		{
			final int thisTx = instance.baseGet(UdpServiceHelper.STR_sTx, BasePrimitiveNumber.ZERO).baseToJavaInteger();
			final int serial = serialObject.baseToJavaInteger();
			if (thisTx < serial && serial > 16000000 == thisTx > 16000000) {
				instance.baseDefine(UdpServiceHelper.STR_sTx, serialObject, BaseProperty.ATTRS_MASK_WED);
			}
		}
		
		/** <code>
			c = message.code;
			if( ((c^0) !== c) ){
				return;
			}
		</code> */
		final int code = messageObject.baseGet(UdpServiceHelper.STR_code, BasePrimitiveNumber.MONE).baseToJavaInteger();
		if (code < 0 || code > 255) {
			if (Report.MODE_DEBUG) {
				ctx.getConsole().log(//
						">>> >>> %s: onReceive, invalid code: %s, address: %s, serial: %s",
						instance.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
						messageObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
						addressObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
						serialObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue()//
				);
			}
			return;
		}
		
		/** <code>
			h = this.handlers[c];
			if(h){
				...
			}
			return;
		</code> */
		final BaseArray registry = instance.baseGet(UdpServiceHelper.STR_handlers, BaseObject.UNDEFINED).baseArray();
		if (registry == null) {
			throw new IllegalArgumentException("'handlers' array property expected on instance object");
		}
		final BaseArray handlers = registry.baseGet(code, BaseObject.UNDEFINED).baseArray();
		if (handlers == null) {
			if (Report.MODE_DEBUG) {
				ctx.getConsole().log("UDP::Principal:onReceive:Java: %s: no handler: %s, address: %s, serial: %s", instance, messageObject, addressObject, serialObject);
			}
			return;
		}
		
		/** <code>
			for(c of h){
				setTimeout(c.bind(this, message, address, serial), 0);
				// c(message, address, serial);
			}
			return;
		</code> */
		if (Report.MODE_DEBUG) {
			ctx.getConsole().log(
					"UDP::Principal:onReceive:Java: %s: type: %s, address: %s, serial: %s, type: %s",
					instance.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
					messageObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
					addressObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
					serialObject.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
					Format.Describe.toDescription(handlers, ""));
		}
		for (final Iterator<? extends BaseObject> i = handlers.baseIterator(); i.hasNext();) {
			final BaseFunction handler = i.next().baseCall();
			if (handler != null) {
				Exec.callAsyncUnrelated(
						Exec.createProcess(UdpServiceHelper.CTX, ctx, "UdpPrincipal::onReceive, handler"),
						handler,
						instance,
						ResultHandler.FU_BNN_NXT,
						messageObject,
						addressObject,
						serialObject//
				);
				
				// handler.callVEA(ctx, BaseObject.NULL, messageObject, addressObject,
				// serialObject);
			}
		}
	}
	
	/** @param ctx
	 * @param instance
	 * @param bufferObject
	 *            bufferObject
	 * @param d
	 *            digestObject
	 * @param m
	 *            messageObject
	 * @param addressObject
	 *            addressObject
	 *
	 *            <code>
			value : UdpServiceHelper.principalSendImpl || (function(b, d, m, a, key, s, len, pkt, k, v){
			})
	 * </code>
	 *
	 * @return
	 * @throws IOException
	 * @throws CloneNotSupportedException
	 * @throws DigestException */
	@ReflectionExplicit
	@ReflectionContextArgument
	@ReflectionThisArgument
	public static int principalSendImpl(//
			final ExecProcess ctx,
			final BaseObject instance,
			final BaseObject bufferObject,
			final BaseObject d,
			final BaseObject m,
			final BaseObject addressObject//
	) throws IOException, CloneNotSupportedException, DigestException {
		
		/** <code>
				if( ! (a ||= this.dst) ){
					if(false !== m.log){
						console.log(
							"UDP::Principal:sendImpl: %s: udp-send-skip, no address, message: %s",
							this,
							m
						);
					}
					return 0;
				}
		</code> */
		/** a - addressObject **/
		final BaseObject a = addressObject.baseToJavaBoolean()
			? addressObject
			: instance.baseGet(UdpServiceHelper.STR_dst, BaseObject.UNDEFINED);
		if (!a.baseToJavaBoolean()) {
			if (m.baseGet(UdpServiceHelper.STR_log, BaseObject.UNDEFINED) != BaseObject.FALSE) {
				ctx.getConsole().log(//
						"UDP::Principal:sendImpl:Java: %s: udp-send-skip, no address, message: %s",
						instance.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
						m.baseToPrimitive(ToPrimitiveHint.STRING).baseValue()//
				);
			}
			return 0;
		}
		
		/** <code>
				if( ! (key = (this.key ?? m.key)) ){
					console.log(
						"UDP::Principal:sendImpl: %s: udp-send-skip, no dst alias, message: %s",
						this,
						m
					);
					return 0;
				}
		</code> */
		final BaseObject keyObject;
		{
			final BaseObject keyInstanceObject = instance.baseGet(UdpServiceHelper.STR_key, BaseObject.UNDEFINED);
			keyObject = keyInstanceObject != BaseObject.UNDEFINED
				? keyInstanceObject
				: m.baseGet(UdpServiceHelper.STR_key, BaseObject.UNDEFINED);
			if (!keyObject.baseToJavaBoolean()) {
				ctx.getConsole().log(//
						"UDP::Principal:sendImpl:Java: %s: udp-send-skip, no dst alias, message: %s",
						instance.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
						m.baseToPrimitive(ToPrimitiveHint.STRING).baseValue()//
				);
				return 0;
			}
		}
		
		/** <code>
				s = m.serial ||= (this.sTx = 1 + Math.max(this.sTx, this.sRx));
				s = m.serial || (m.serial = (this.sTx = 1 + Math.max(this.sTx, this.sRx)));
		</code> */
		
		/** s - serial **/
		final int serial;
		{
			final BaseObject messageSerialObject = m.baseGet(UdpServiceHelper.STR_serial, BaseObject.UNDEFINED);
			if (messageSerialObject != BaseObject.UNDEFINED) {
				serial = messageSerialObject.baseToJavaInteger();
			} else {
				final int thisTx = instance.baseGet(UdpServiceHelper.STR_sTx, BasePrimitiveNumber.ZERO).baseToJavaInteger();
				final int thisRx = instance.baseGet(UdpServiceHelper.STR_sRx, BasePrimitiveNumber.ZERO).baseToJavaInteger();
				serial = 1 + Math.max(thisTx, thisRx);
				final BasePrimitiveNumber serialObject = Base.forInteger(serial);
				m.baseDefine(UdpServiceHelper.STR_serial, serialObject, BaseProperty.ATTRS_MASK_WED);
				instance.baseDefine(UdpServiceHelper.STR_sTx, serialObject, BaseProperty.ATTRS_MASK_WED);
			}
		}
		
		/** <code>
			b[16 + 12 + 1] = (s >> 16) & 0xFF;
			b[16 + 12 + 2] = (s >> 8) & 0xFF;
			b[16 + 12 + 3] = (s) & 0xFF;
		</code> */
		/** b - bufferObject **/
		final byte[] b = (byte[]) bufferObject.baseValue();
		if (b == null) {
			throw new IllegalArgumentException("byte[] buffer is expected, bufferObject class: " + bufferObject.getClass().getName());
		}
		b[16 + 12 + 1] = (byte) (serial >> 16 & 0xFF);
		b[16 + 12 + 2] = (byte) (serial >> 8 & 0xFF);
		b[16 + 12 + 3] = (byte) (serial >> 0 & 0xFF);
		
		/** <code>
			len = m.build(b, 32) + 32;
		</code> */
		final int len;
		{
			final BaseFunction messageBuildFn = m.baseGet(UdpServiceHelper.STR_build, BaseObject.UNDEFINED).baseCall();
			if (messageBuildFn == null) {
				throw new IllegalArgumentException("Message type build function is expected, messageObject: " + m);
			}
			len = messageBuildFn.callIE2(ctx, m, bufferObject, UdpServiceHelper.INT_32) + 32;
		}
		
		/** <code>
			key.copy(0, b, 16, 12);
		</code> */
		final TransferCopier keyBinary = Transfer.createCopierFromBinary(keyObject);
		if (keyBinary == null || keyBinary.length() == 0) {
			throw new IllegalArgumentException("non empty 'key' binary property is expected, key object: " + keyObject);
		}
		keyBinary.copy(0, b, 16, 12);
		
		/** <code>
			b[16 + 12] = m.code;
		</code> */
		b[16 + 12] = (byte) (m.baseGet(UdpServiceHelper.STR_code, BaseObject.UNDEFINED).baseToJavaInteger() & 0xFF);
		
		/** <code>
			pkt = Transfer.wrapCopier(b, 0, len);
		</code> */
		final TransferCopier packetBinary = Transfer.wrapCopier(b, 0, len);
		
		{
			final TransferCopier secret = Transfer.createCopierFromBinary(instance.baseGet(UdpServiceHelper.STR_secret, TransferCopier.NUL_COPIER));
			if (secret == null || secret.length() == 0) {
				throw new IllegalArgumentException("non empty 'secret' binary property is expected, secret: " + secret);
			}
			
			/** <code>
			m.encrypt && this.payloadEncrypt(b, len, d);
			</code> */
			if (m.baseGet(UdpServiceHelper.STR_encrypt, BaseObject.UNDEFINED).baseToJavaBoolean()) {
				final MessageDigest digest1 = (MessageDigest) ((MessageDigest) d.baseValue()).clone();
				Transfer.updateMessageDigest(digest1, b, 16, 16);
				secret.updateMessageDigest(digest1);
				Transfer.xorBytes(b, 32, digest1.digest(), len);
			}
			
			/** <code>
			d = d.clone();
			pkt.slice(16, len - 16).updateMessageDigest(d);
			this.secret.updateMessageDigest(d);
			</code> */
			final MessageDigest digest = (MessageDigest) ((MessageDigest) d.baseValue()).clone();
			packetBinary.slice(16, len - 16).updateMessageDigest(digest);
			secret.updateMessageDigest(digest);
			
			/** <code>
			copyBytes(d.result, 0, b, 0, 16);
			</code> */
			Transfer.copyBytes(digest.digest(), 0, b, 0, 16);
		}
		
		/** <code>
				if(true === m.log){
					Object.keys( (m = Object.create(m)) ).forEach(function(k, v){
						v = m[k];
						isSocketAddress(v) && (m[k] = v.address + ':' + v.port);
					});
					console.log("UDP::Principal:sendImpl: %s: udp-send: -> %s @ %s:%s, ser: %s, len: %s, %s",
						this,
						Format.binaryAsHex(key.slice(0, 12)),
						a.address.hostAddress,
						a.port,
						s,
						pkt.length(),
						m
					);
				}
		</code> */
		if (Report.MODE_DEVEL || m.baseGet(UdpServiceHelper.STR_log, BaseObject.UNDEFINED) == BaseObject.TRUE) {
			final BaseObject messageDerivedObject = BaseObject.createObject(m);
			for (final Iterator<? extends BasePrimitive<?>> keys = Base.keysPrimitive(m); keys.hasNext();) {
				final BasePrimitive<?> key = keys.next();
				final BaseObject valueObject = m.baseGet(key, BaseObject.UNDEFINED);
				if (valueObject.baseValue() instanceof SocketAddress) {
					messageDerivedObject.baseDefine(//
							key,
							Base.forString(//
									valueObject.baseGet(UdpServiceHelper.STR_address, BaseObject.UNDEFINED) + ":"
											+ valueObject.baseGet(UdpServiceHelper.STR_port, BaseObject.UNDEFINED)//
							)//
					);
				}
				
			}
			ctx.getConsole().log(//
					"UDP::Principal:sendImpl:Java: %s: udp-send: -> %s @ %s:%s, ser: %s, len: %s, %s'",
					instance.baseToPrimitive(ToPrimitiveHint.STRING).baseValue(),
					FormatSAPI.jsObject(keyBinary.slice(0, 12)),
					addressObject.baseGet(UdpServiceHelper.STR_address, BaseObject.UNDEFINED).baseGet(UdpServiceHelper.STR_hostAddress, BaseObject.UNDEFINED),
					addressObject.baseGet(UdpServiceHelper.STR_port, BaseObject.UNDEFINED),
					Base.forInteger(serial),
					Base.forLong(packetBinary.length()),
					m.baseToPrimitive(ToPrimitiveHint.STRING).baseValue()//
			);
		}
		
		/** <code>
			return this.sendUdp(pkt, a);
		</code> */
		{
			final BaseFunction sendUdpFn = instance.baseGet(UdpServiceHelper.STR_sendUdp, BaseObject.UNDEFINED).baseCall();
			if (sendUdpFn == null) {
				throw new IllegalArgumentException("sendUdp function is expected, instance: " + instance);
			}
			return sendUdpFn.callIE2(ctx, instance, packetBinary, a);
		}
	}
	
	/** @param instance
	 * @return <code>
	 *      function(){
				if(!this.argument){
					return "[CALL "+ Format.jsString(this.component) + ", sTx:"+(this.serial||0)+"]";
				}
				return "[CALL "+ Format.jsString(this.component) + ", " + Format.bytesRound(this.argument.length()) + "B, sTx:"+(this.serial||0)+"]";
			}
	 </code>
	 * @throws IOException */
	@ReflectionExplicit
	@ReflectionThisArgument
	public static String toStringMsgCall(final BaseObject instance) throws IOException {
		
		final String component = instance.baseGet(UdpServiceHelper.STR_component, BaseObject.UNDEFINED).baseToJavaString();
		final long argumentLength = Transfer.createCopierFromBinary(instance.baseGet(UdpServiceHelper.STR_argument, TransferCopier.NUL_COPIER)).length();
		final int serial = instance.baseGet(UdpServiceHelper.STR_serial, BaseObject.UNDEFINED).baseToJavaInteger();
		
		return argumentLength == 0
			? "[CALL " + FormatSAPI.jsString(component) + ", sTx:" + serial + "]"
			: "[CALL " + FormatSAPI.jsString(component) + ", " + FormatSAPI.bytesRound(argumentLength) + "B, sTx:" + serial + "]";
	}
}
