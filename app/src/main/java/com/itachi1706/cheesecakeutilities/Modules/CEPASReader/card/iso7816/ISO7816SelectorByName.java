/*
 * ISO7816SelectorByName.java
 *
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

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml.Base64String;

import org.simpleframework.xml.Element;

import java.io.IOException;

class ISO7816SelectorByName extends ISO7816SelectorElement {
    @Element(name="name")
    private Base64String mName;

    public static final String KIND = "name";

    ISO7816SelectorByName() { /* for XML serializer. */ }

    @Override
    byte[] select(ISO7816Protocol tag) throws IOException {
        return tag.selectByName(mName.getData(), false);
    }

    @Override
    public String formatString() {
        return "#" + Utils.getHexString(mName.getData());
    }

    ISO7816SelectorByName(byte[] name) {
        mKind = KIND;
        mName = new Base64String(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ISO7816SelectorByName))
            return false;
        return ((ISO7816SelectorByName) obj).mName.toBase64().equals(mName.toBase64());
    }
}
