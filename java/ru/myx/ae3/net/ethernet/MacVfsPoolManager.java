package ru.myx.ae3.net.ethernet;

import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.vfs.EntryContainer;
import ru.myx.ae3.vfs.Storage;
import ru.myx.sapi.FormatSAPI;

/** !!!FIXME: unfinished
 *
 *
 *
 * @author myx */
public class MacVfsPoolManager {

	/** @param vfsFolder
	 * @return */
	public static MacVfsPoolManager create(final Entry vfsFolder) {
		
		if (vfsFolder.isExist() && !vfsFolder.isContainer()) {
			throw new IllegalStateException("vfs entry exists but not a folder!");
		}
		return new MacVfsPoolManager(vfsFolder.toContainer());
	}
	
	private final EntryContainer vfsFolder;
	/**
	 *
	 */
	public MacAddressSet poolInitial = null;

	/**
	 *
	 */
	public MacAddressSet poolRemaining = null;

	private MacVfsPoolManager(final EntryContainer vfsFolder) {
		
		this.vfsFolder = vfsFolder;
	}
	
	/** @param create
	 */
	public void load(final boolean create) {
		
		if (!create && !this.vfsFolder.isContainer()) {
			throw new IllegalStateException("vfs entry is not an existing mac pool folder!");
		}
		
		if (this.vfsFolder.isExist() && !this.vfsFolder.isContainer()) {
			throw new IllegalStateException("vfs entry exists but not a folder!");
		}
	}
	
	@Override
	public String toString() {

		return "[object VfsPoolManager(" + FormatSAPI.jsString(Storage.getAbsolutePath(this.vfsFolder)) + ")]";
	}
}
