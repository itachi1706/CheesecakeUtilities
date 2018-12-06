/*
 * Card.java
 *
 * Copyright 2011-2014 Eric Butler <eric@codebutler.com>
 * Copyright 2016 Michael Farrell <micolous+git@gmail.com>
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

package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card;

import android.nfc.Tag;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.iso7816.ISO7816Card;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.TransitIdentity;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.ListItem;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.xml.HexString;
import com.itachi1706.cheesecakeutilities.R;

import org.apache.commons.lang3.ArrayUtils;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Serializer;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;

public abstract class Card {
    private static String TAG = Card.class.getName();
    // This must be protected, not private, as otherwise the XML deserialiser fails to read the
    // card.
    @SuppressWarnings("WeakerAccess")
    @Attribute(name = "label", required = false)
    protected String mLabel;
    @Attribute(name = "type")
    private CardType mType;
    @Attribute(name = "id")
    private HexString mTagId;
    @Attribute(name = "scanned_at")
    private Calendar mScannedAt;
    @Attribute(name = "partial_read", required = false)
    private boolean mPartialRead;

    protected Card() {
    }

    protected Card(CardType type, byte[] tagId, Calendar scannedAt) {
        this(type, tagId, scannedAt, null);
    }

    protected Card(CardType type, byte[] tagId, Calendar scannedAt, String label) {
        this(type, tagId, scannedAt, label, false);
    }

    protected Card(CardType type, byte[] tagId, Calendar scannedAt, String label, boolean partialRead) {
        mType = type;
        mTagId = new HexString(tagId);
        mScannedAt = scannedAt;
        mLabel = label;
        mPartialRead = partialRead;
    }

    public static Card dumpTag(byte[] tagId, Tag tag, TagReaderFeedbackInterface feedbackInterface) throws Exception {
        final String[] techs = tag.getTechList();
        Log.d(TAG, String.format(Locale.ENGLISH, "Reading tag %s. %d tech(s) supported:",
                Utils.getHexString(tagId), techs.length));
        for (String tech : techs) {
            Log.d(TAG, tech);
        }

        if (ArrayUtils.contains(techs, "android.nfc.tech.IsoDep")) {
            feedbackInterface.updateStatusText(Utils.localizeString(R.string.iso14a_detect));

            // ISO 14443-4 card types
            // This also encompasses NfcA (ISO 14443-3A) and NfcB (ISO 14443-3B)
            ISO7816Card isoCard = ISO7816Card.dumpTag(tag, feedbackInterface);
            if (isoCard != null) {
                return isoCard;
            }

            // Credit cards fall through here...
        }

        throw new UnsupportedTagException(techs, Utils.getHexString(tag.getId()));
    }

    public static Card fromXml(Serializer serializer, String xml) {
        try {
            return serializer.read(Card.class, xml);
        } catch (Exception ex) {
            Log.e("Card", "Failed to deserialize", ex);
            throw new RuntimeException(ex);
        }
    }

    public String toXml(Serializer serializer) {
        try {
            StringWriter writer = new StringWriter();
            serializer.write(this, writer);
            return writer.toString();
        } catch (Exception ex) {
            Log.e("Card", "Failed to serialize", ex);
            throw new RuntimeException(ex);
        }
    }

    public CardType getCardType() {
        return mType;
    }

    public byte[] getTagId() {
        return mTagId.getData();
    }

    public Calendar getScannedAt() {
        return mScannedAt;
    }

    public String getLabel() {
        return mLabel;
    }

    /**
     * Is this a partial or incomplete card read?
     * @return true if there is not complete data in this scan.
     */
    public boolean isPartialRead() {
        return mPartialRead;
    }

    /**
     * This is where the "transit identity" is parsed, that is, a combination of the card type,
     * and the card's serial number (according to the operator).
     * @return Transit Identity
     */
    public abstract TransitIdentity parseTransitIdentity();

    /**
     * This is where a card is actually parsed into TransitData compatible data.
     * @return Data of the current Transit
     */
    public abstract TransitData parseTransitData();

    @Nullable
    public List<ListItem> getManufacturingInfo() {
        return null;
    }

    public List<ListItem> getRawData() {
        return null;
    }
}
