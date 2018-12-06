/*
 * ISO7816File.java
 *
 * Copyright 2018 Michael Farrell <micolous+git@gmail.com>
 * Copyright 2018 Google
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.iso7816;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml.Base64String;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

/**
 * Represents a file on a Calypso card.
 */
@Root(name = "file")
public class ISO7816File {
    @SuppressWarnings("FieldCanBeLocal")
    @Attribute(name = "name", required = false)
    private String mReadableName;

    @Element(name = "selector", required = false)
    private ISO7816Selector mSelector;
    @Element(name = "data", required = false)
    private Base64String mBinaryData;
    @ElementList(name = "records", required = false, empty = false)
    private List<ISO7816Record> mRecords;
    @Element(name = "fci", required = false)
    private Base64String mFci;

    ISO7816File() { /* For XML Serializer */ }

    ISO7816File(ISO7816Selector selector,
                List<ISO7816Record> records,
                byte[] binaryData, byte[] fci) {
        mSelector = selector;
        mRecords = records;
        if (binaryData != null)
            mBinaryData = new Base64String(binaryData);
        mReadableName = mSelector.formatString();
        if (fci != null)
            mFci = new Base64String(fci);
    }

    public List<ISO7816Record> getRecords() {
        Collections.sort(mRecords, (a, b) -> a.getIndex() - b.getIndex());
        return mRecords;
    }

    @Nullable
    public byte[] getBinaryData() {
        if (mBinaryData != null) {
            return mBinaryData.getData();
        } else {
            return null;
        }
    }

    @Nullable
    public byte[] getFci() {
        if (mFci != null) {
            return mFci.getData();
        } else {
            return null;
        }
    }

    /**
     * Gets a record for a given index.
     * @param index Record index to retrieve.
     * @return ISO7816Record with that index, or null if not present.
     */
    public ISO7816Record getRecord(int index) {
        for (ISO7816Record record : mRecords) {
            if (record.getIndex() == index) {
                return record;
            }
        }

        return null;
    }

    public ISO7816Selector getSelector() {
        return mSelector;
    }
}
