/*
 * ISO7816Card.java
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

import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.IsoDep;
import androidx.annotation.Nullable;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.Card;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.CardType;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.TagReaderFeedbackInterface;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.cepas.CEPASApplication;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitIdentity;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.ListItem;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.ListItemRecursive;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.itachi1706.cheesecakeutilities.R;

/**
 * Generic card implementation for ISO7816. This doesn't have many smarts, but dispatches to other
 * readers.
 */
@Root(name = "card")
public class ISO7816Card extends Card {
    private static final String TAG = ISO7816Card.class.getSimpleName();

    @ElementList(name = "applications", entry = "application")
    private List<ISO7816Application> mApplications;
    
    protected ISO7816Card() { /* For XML Serializer */ }

    public ISO7816Card(List<ISO7816Application> apps, byte[] tagId, Calendar scannedAt, boolean partialRead) {
        super(CardType.ISO7816, tagId, scannedAt, null, partialRead);
        mApplications = apps;
    }

    /**
     * Dumps a ISO7816 tag in the field.
     *
     * @param tag Tag to dump.
     * @return ISO7816Card of the card contents. Returns null if an unsupported card is in the
     * field.
     * @throws Exception On communication errors.
     */
    @Nullable
    public static ISO7816Card dumpTag(Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        IsoDep tech = IsoDep.get(tag);
        tech.connect();
        boolean partialRead = false;
        byte []tagId = tag.getId();
        ArrayList<ISO7816Application> apps = new ArrayList<>();

        try {
            ISO7816Protocol iso7816Tag = new ISO7816Protocol(tech);

            feedbackInterface.updateStatusText(Utils.localizeString(R.string.iso7816_probing));
            feedbackInterface.updateProgressBar(0, 1);

            byte []appData;

            /*
             * It's tempting to try to iterate over the apps on the card.
             * Unfortunately many cards don't reply to iterating requests
             *
             */

            // FIXME: At some point we want to make this an iteration over supported apps
            // rather than copy-paste.

            // CEPAS specification makes selection by AID optional. I couldn't find an AID that
            // works on my cards. But CEPAS needs to have CEPAS app implicitly selected,
            // so try selecting its main file
            // So this needs to be before selecting any real application as selecting APP by AID
            // may deselect default app
            ISO7816Application cepas = CEPASApplication.dumpTag(iso7816Tag, new ISO7816Application.ISO7816Info(null, null,
                                tag.getId(), CEPASApplication.TYPE),
                        feedbackInterface);
            if (cepas != null)
                apps.add(cepas);
        } catch (TagLostException ex) {
            Log.w(TAG, "tag lost", ex);
            partialRead = true;
        } finally {
            if (tech.isConnected())
                tech.close();
        }

        return new ISO7816Card(apps, tagId, GregorianCalendar.getInstance(), partialRead);
    }

    @Override
    public TransitIdentity parseTransitIdentity() {
        // FIXME: At some point we want to support multi-app cards
        // but currently we haven't come across one.
        for (ISO7816Application app : mApplications) {
            TransitIdentity id = app.parseTransitIdentity();
            if (id != null)
                return id;
        }
        return null;
    }

    @Override
    public TransitData parseTransitData() {
        for (ISO7816Application app : mApplications) {
            TransitData d = app.parseTransitData();
            if (d != null)
                return d;
        }
        return null;
    }

    @Override
    public List<ListItem> getManufacturingInfo() {
        List<ListItem> manufacturingInfo = new ArrayList<>();
        for (ISO7816Application app : mApplications) {
            List<ListItem> appManufacturingInfo = app.getManufacturingInfo();
            if (appManufacturingInfo != null) {
                manufacturingInfo.addAll(appManufacturingInfo);
            }
        }
        if (manufacturingInfo.isEmpty())
            return null;
        return manufacturingInfo;
    }

    @Override
    public List<ListItem> getRawData() {
        List<ListItem> rawData = new ArrayList<>();
        for (ISO7816Application app : mApplications) {
            String appTitle;
            byte[] appName = app.getAppName();
            if (appName == null)
                appTitle = app.getClass().getSimpleName();
            else if (Utils.isASCII(appName))
                appTitle = new String(appName);
            else
                appTitle = Utils.getHexString(appName);
            List<ListItem> rawAppData = new ArrayList<>();
            byte[] appData = app.getAppData();
            if (appData != null)
                rawAppData.add(ListItemRecursive.collapsedValue(
                        R.string.app_fci, Utils.getHexDump(appData)));
            List<ISO7816File> files = app.getFiles();
            for (ISO7816File file : files) {
                List<ListItem> recList = new ArrayList<>();
                byte[] binaryData = file.getBinaryData();
                byte[] fciData = file.getFci();
                if (binaryData != null)
                    recList.add(ListItemRecursive.collapsedValue(Utils.localizeString(R.string.binary_title_format),
                            Utils.getHexDump(binaryData)));
                if (fciData != null)
                    recList.add(ListItemRecursive.collapsedValue(Utils.localizeString(R.string.file_fci),
                            Utils.getHexDump(fciData)));
                List<ISO7816Record> records = file.getRecords();
                for (ISO7816Record record : records)
                    recList.add(ListItemRecursive.collapsedValue(Utils.localizeString(R.string.record_title_format, record.getIndex()),
                            Utils.getHexDump(record.getData())));
                ISO7816Selector selector = file.getSelector();
                String selectorStr = selector.formatString();
                String fileDesc = app.nameFile(selector);
                if (fileDesc != null)
                    selectorStr = String.format(Locale.ENGLISH, "%s (%s)", selectorStr, fileDesc);
                rawAppData.add(new ListItemRecursive(Utils.localizeString(R.string.file_title_format, selectorStr),
                        Utils.localizePlural(R.plurals.record_count, records.size(), records.size()),
                        recList));
            }
            List<ListItem> extra = app.getRawData();
            if (extra != null)
                rawAppData.addAll(extra);
            rawData.add(new ListItemRecursive(Utils.localizeString(R.string.application_title_format,
                    appTitle), null, rawAppData));
        }
        return rawData;
    }
}
