/*
 * ISO7816SelectorElement.java
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

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml.SkippableRegistryStrategy;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

import java.io.IOException;

abstract public class ISO7816SelectorElement {
    @Attribute(name = "kind")
    String mKind;

    abstract byte[] select(ISO7816Protocol tag) throws IOException;

    public abstract String formatString();

    public static class XMLConverter implements Converter<ISO7816SelectorElement> {
        private final Serializer mSerializer;

        public XMLConverter(Serializer serializer) {
            mSerializer = serializer;
        }

        @Override
        public ISO7816SelectorElement read(InputNode node) throws Exception {
            String kind = node.getAttribute("kind").getValue();
            if (ISO7816SelectorByName.KIND.equals(kind)) {
                return mSerializer.read(ISO7816SelectorByName.class, node);
            }
            if (ISO7816SelectorById.KIND.equals(kind)) {
                return mSerializer.read(ISO7816SelectorById.class, node);
            }
            throw new UnsupportedOperationException("Unsupported selector " + kind);
        }

        @Override
        public void write(OutputNode node, ISO7816SelectorElement value) throws Exception {
            throw new SkippableRegistryStrategy.SkipException();
        }
    }
}
