/*
 * CardInfo.java
 *
 * Copyright 2011 Eric Butler
 * Copyright 2015-2018 Michael Farrell
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
package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.card.CardType;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.ezlink.EZLinkTransitData;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;

import java.util.Locale;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.content.res.AppCompatResources;

/**
 * List of all the cards we know about.
 */

@SuppressWarnings("WeakerAccess")
public class CardInfo {
    /**
     * A list of all cards in alphabetical order of their name.
     */
    public static final CardInfo[] ALL_CARDS_ALPHABETICAL = {
            EZLinkTransitData.EZ_LINK_CARD_INFO,
            EZLinkTransitData.NETS_FLASHPAY_CARD_INFO,
            EZLinkTransitData.EZ_LINK_CONCESSION_CARD_INFO
    };

    @DrawableRes
    private final int mImageId;
    @DrawableRes
    private final int mImageAlphaId;
    private final String mName;
    @StringRes
    private final int mLocationId;
    private final CardType mCardType;
    private final boolean mKeysRequired;
    private final boolean mPreview;
    @StringRes
    private final int mResourceExtraNote;

    private CardInfo(@DrawableRes int imageId, String name, @StringRes int locationId, CardType cardType, boolean keysRequired, boolean preview, @StringRes int resourceExtraNote,  @DrawableRes int imageAlphaId) {
        mImageId = imageId;
        mImageAlphaId = imageAlphaId;
        mName = name;
        mLocationId = locationId;
        mCardType = cardType;
        mKeysRequired = keysRequired;
        mPreview = preview;
        mResourceExtraNote = resourceExtraNote;
    }

    public boolean hasBitmap() {
        return mImageAlphaId != 0 || mImageId != 0;
    }

    public Drawable getDrawable(Context ctxt) {
        if (mImageAlphaId != 0) {
            Log.d("CardInfo", String.format(Locale.ENGLISH, "masked bitmap %x / %x", mImageId, mImageAlphaId));
            Resources res = ctxt.getResources();
            return new BitmapDrawable(res, Utils.getMaskedBitmap(res, mImageId, mImageAlphaId));
        } else {
            return AppCompatResources.getDrawable(ctxt, mImageId);
        }
    }

    @NonNull
    public String getName() {
        return mName;
    }

    @StringRes
    public int getLocationId() {
        return mLocationId;
    }

    public CardType getCardType() {
        return mCardType;
    }

    public boolean getKeysRequired() {
        return mKeysRequired;
    }

    /**
     * Indicates if the card is a "preview" / beta decoder, with possibly
     * incomplete / incorrect data.
     *
     * @return true if this is a beta version of the card decoder.
     */
    public boolean getPreview() {
        return mPreview;
    }

    @StringRes
    public int getResourceExtraNote() {
        return mResourceExtraNote;
    }

    public static class Builder {
        @DrawableRes
        private int mImageId;
        @DrawableRes
        private int mImageAlphaId;
        private String mName;
        @StringRes
        private int mLocationId;
        private CardType mCardType;
        private boolean mKeysRequired;
        private boolean mPreview;
        @StringRes
        private int mResourceExtraNote;

        public Builder() {
        }

        public CardInfo build() {
            return new CardInfo(mImageId, mName, mLocationId, mCardType, mKeysRequired, mPreview, mResourceExtraNote, mImageAlphaId);
        }

        public Builder setImageId(@DrawableRes int id) {
            return setImageId(id, 0);
        }

        public Builder setImageId(@DrawableRes int id, @DrawableRes int alpha) {
            mImageId = id;
            mImageAlphaId = alpha;
            return this;
        }

        public Builder setName(String name) {
            mName = name;
            return this;
        }

        public Builder setLocation(@StringRes int id) {
            mLocationId = id;
            return this;
        }

        public Builder setCardType(CardType type) {
            mCardType = type;
            return this;
        }

        public Builder setKeysRequired() {
            mKeysRequired = true;
            return this;
        }

        public Builder setPreview() {
            mPreview = true;
            return this;
        }

        public Builder setExtraNote(@StringRes int id) {
            mResourceExtraNote = id;
            return this;
        }

    }
}
