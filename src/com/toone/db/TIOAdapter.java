package com.toone.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.db4o.Db4oIOException;
import com.db4o.internal.Platform4;
import com.db4o.io.IoAdapter;

/**
 * 加密输出流
 * 
 * @author lixuez
 * @createOn 2007-9-25
 * @modify 2008-5-16 lixuez<br>
 *         DB4O7.3时改变此接口
 *         <p/>
 */
public class TIOAdapter extends IoAdapter {
	private String _path;

	private RandomAccessFile _delegate;

	public final static String dbDesp = "Toone EasyCost2.0 db.";

	public final static String db4oDesp = "db4o";

	public final static byte[] dbbs = dbDesp.getBytes();

	public TIOAdapter() {}

	public TIOAdapter(String path, boolean lockFile, long initialLength) throws Db4oIOException {
		this(path, lockFile, initialLength, false);
	}

	public TIOAdapter(String path, boolean lockFile, long initialLength, boolean readOnly) throws Db4oIOException {
		try {
			_path = (new File(path)).getCanonicalPath();
			File f = new File(path);
			String flag = dbDesp;
			if (f.exists()) {
				FileInputStream fis = new FileInputStream(f);
				byte[] bs = new byte[dbbs.length + db4oDesp.getBytes().length + 10];
				fis.read(bs);
				fis.close();
				flag = new String(bs, "GBK");
			}
			if (flag.equals(dbDesp) || !flag.startsWith(db4oDesp)) {
				int tmp = flag.indexOf(db4oDesp);
				if (tmp == -1) tmp = dbbs.length;
				final int offset = tmp;
				_delegate = new RandomAccessFile(_path, readOnly ? "r" : "rw") {
					@Override
					public void seek(long pos) throws IOException {
						super.seek(pos + offset);
					}

					@Override
					public void close() throws IOException {
						super.seek(0);
						super.close();
					}
				};
			} else {
				_delegate = new RandomAccessFile(_path, readOnly ? "r" : "rw");
			}
			if (initialLength > 0) {
				_delegate.seek(initialLength - 1);
				_delegate.write(new byte[] { 0 });
			}
			if (lockFile) {
				Platform4.lockFile(path, _delegate);
			}
		} catch (IOException e) {
//			throw new LogicException(e.getMessage() + path, e);
		}
	}

	public IoAdapter open(String path, boolean lockFile, long initialLength) throws Db4oIOException {
		return new TIOAdapter(path, lockFile, initialLength);
	}

	public IoAdapter open(String path, boolean lockFile, long initialLength, boolean readOnly) throws Db4oIOException {
		return new TIOAdapter(path, lockFile, initialLength, readOnly);
	}

	public void close() throws Db4oIOException {
		try {
			Platform4.unlockFile(_path, _delegate);
		} catch (Exception exception) {}
		try {
			_delegate.close();
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}

	public void delete(String path) {
		(new File(path)).delete();
	}

	public boolean exists(String path) {
		File file = new File(path);
		return file.exists() && file.length() > 0L;
	}

	public long getLength() throws Db4oIOException {
		try {
			return _delegate.length();
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}

	public void seek(long pos) throws Db4oIOException {
		try {
			_delegate.seek(pos);
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}

	public void sync() throws Db4oIOException {
		try {
			_delegate.getFD().sync();
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}

	public int read(byte[] bytes, int length) throws Db4oIOException {
		try {
			return _delegate.read(bytes, 0, length);
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}

	public void write(byte[] buffer, int length) throws Db4oIOException {
		try {
			_delegate.write(buffer, 0, length);
		} catch (IOException e) {
			throw new Db4oIOException(e);
		}
	}
}
