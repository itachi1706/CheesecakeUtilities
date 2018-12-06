/*
 * CardConverter.java
 *
 * Copyright (C) 2014 Eric Butler <eric@codebutler.com>
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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
package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.cepas.CEPASApplication;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.iso7816.ISO7816Application;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class ISO7816Converter implements Converter<ISO7816Application> {
    private final Serializer mSerializer;

    public ISO7816Converter(Serializer serializer) {
        mSerializer = serializer;
    }

    @Override
    public ISO7816Application read(InputNode node) throws Exception {
        String appType = node.getAttribute("type").getValue();
        if (CEPASApplication.TYPE.equals(appType)) {
            return mSerializer.read(CEPASApplication.class, node);
        }
        throw new SkippableRegistryStrategy.SkipException();
    }

    @Override
    public void write(OutputNode node, ISO7816Application value) throws Exception {
        throw new SkippableRegistryStrategy.SkipException();
    }
}
