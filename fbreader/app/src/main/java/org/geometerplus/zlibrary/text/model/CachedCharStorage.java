/*
 * Copyright (C) 2007-2017 FBReader.ORG Limited <contact@fbreader.org>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.text.model;

import java.lang.ref.WeakReference;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

public final class CachedCharStorage {
	protected final ArrayList<WeakReference<char[]>> myArray =
		new ArrayList<WeakReference<char[]>>();

	private final String myDirectoryName;
	private final String myFileExtension;

	public CachedCharStorage(String directoryName, String fileExtension, int blocksNumber) {
		myDirectoryName = directoryName + '/';
		myFileExtension = '.' + fileExtension;
		myArray.addAll(Collections.nCopies(blocksNumber, new WeakReference<char[]>(null)));
	}

	private String fileName(int index) {
		return myDirectoryName + index + myFileExtension;
	}

	public int size() {
		return myArray.size();
	}

	public char[] block(int index) {
		if (index < 0 || index >= myArray.size()) {
			return null;
		}
		char[] block = myArray.get(index).get();
		if (block == null) {
			try {
				File file = new File(fileName(index));
				int size = (int)file.length();
				if (size < 0) {
					throw new CachedCharStorageException("Error during reading " + fileName(index) + "; size = " + size);
				}
				block = new char[size / 2];
				InputStreamReader reader =
					new InputStreamReader(
						new FileInputStream(file),
						"UTF-16LE"
					);
				final int rd = reader.read(block);
				if (rd != block.length) {
					throw new CachedCharStorageException("Error during reading " + fileName(index) + "; " + rd + " != " + block.length);
				}
				reader.close();
			} catch (IOException e) {
				throw new CachedCharStorageException("Error during reading " + fileName(index), e);
			}
			myArray.set(index, new WeakReference<char[]>(block));
		}
		return block;
	}
}
