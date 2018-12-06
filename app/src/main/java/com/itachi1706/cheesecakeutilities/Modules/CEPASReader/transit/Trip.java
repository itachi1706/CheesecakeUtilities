/*
 * Trip.java
 *
 * Copyright 2011 Eric Butler <eric@codebutler.com>
 * Copyright 2016-2018 Michael Farrell <micolous+git@gmail.com>
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

import android.os.Build;
import android.os.Parcelable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TtsSpan;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.ui.HiddenSpan;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util.Utils;
import com.itachi1706.cheesecakeutilities.R;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

public abstract class Trip implements Parcelable {
    private static final String TAG = Trip.class.getName();

    /**
     * Formats a trip description into a localised label, with appropriate language annotations.
     *
     * @param trip The trip to describe.
     * @return null if both the start and end stations are unknown.
     */
    public static Spannable formatStationNames(Trip trip) {
        String startStationName = null, endStationName = null;
        String startLanguage = null;

        if (trip.getStartStation() != null) {
            startStationName = trip.getStartStation().getShortStationName();
            startLanguage = trip.getStartStation().getLanguage();
        }

        if (trip.getEndStation() != null &&
                (trip.getStartStation() == null ||
                        !trip.getEndStation().getStationName().equals(trip.getStartStation().getStationName()))) {
            endStationName = trip.getEndStation().getShortStationName();
        }

        // No information is available.
        if (startStationName == null && endStationName == null) {
            return null;
        }

        // If only the start station is available, just return that.
        if (startStationName != null && endStationName == null) {
            return new SpannableStringBuilder(startStationName);
        }

        // Both the start and end station are known.
        String startPlaceholder = "%1$s";
        String endPlaceholder = "%2$s";
        String s = Utils.localizeString(R.string.trip_description, startPlaceholder, endPlaceholder);

        if (startStationName == null) {
            s = Utils.localizeString(R.string.trip_description_unknown_start, endPlaceholder);
        }

        // Build the spans
        SpannableStringBuilder b = new SpannableStringBuilder(s);

        // Find the TTS-exclusive bits
        // They are wrapped in parentheses: ( )
        int x = 0;
        while (x < b.toString().length()) {
            int start = b.toString().indexOf("(", x);
            if (start == -1) break;
            int end = b.toString().indexOf(")", start);
            if (end == -1) break;

            // Delete those characters
            b.delete(end, end+1);
            b.delete(start, start+1);

            // We have a range, create a span for it
            b.setSpan(new HiddenSpan(), start, --end, 0);

            x = end;
        }

        // Find the display-exclusive bits.
        // They are wrapped in square brackets: [ ]
        x = 0;
        while (x < b.toString().length()) {
            int start = b.toString().indexOf("[", x);
            if (start == -1) break;
            int end = b.toString().indexOf("]", start);
            if (end == -1) break;

            // Delete those characters
            b.delete(end, end+1);
            b.delete(start, start+1);
            end--;

            // We have a range, create a span for it
            // This only works properly on Lollipop. It's a pretty reasonable target for
            // compatibility, and most TTS software will not speak out Unicode arrows anyway.
            //
            // This works fine with Talkback, but *doesn't* work with Select to Speak.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                b.setSpan(new TtsSpan.TextBuilder().setText(" ").build(), start, end, 0);
            }

            x = end;
        }

        if (startStationName != null) {
            // Finally, insert the actual station names back in the data.
            x = b.toString().indexOf(startPlaceholder);
            if (x == -1) {
                Log.w(TAG, "couldn't find start station placeholder to put back");
                return null;
            }
            b.replace(x, x + startPlaceholder.length(), startStationName);

        } else {
            x = 0;
        }

        int y = b.toString().indexOf(endPlaceholder);
        if (y == -1) {
            Log.w(TAG, "couldn't find end station placeholder to put back");
            return null;
        }
        b.replace(y, y + endPlaceholder.length(), endStationName);

        return b;
    }

    /**
     * Starting time of the trip.
     */
    public abstract Calendar getStartTimestamp();

    /**
     * Ending time of the trip. If this is not known, return null.
     *
     * This returns null if not overridden in a subclass.
     */
    public Calendar getEndTimestamp() {
        return null;
    }

    /**
     * Route name for the trip. This could be a bus line, a tram line, a rail line, etc.
     * If this is not known, then return null.
     */
    @Nullable
    public String getRouteName() {
        return null;
    }

    /**
     * Language that the route name is written in. This is used to aid text to speech software
     * with pronunciation. If null, then uses the system language instead.
     */
    public String getRouteLanguage() {
        return null;
    }

    /**
     * Vehicle number where the event was recorded.
     *
     * This is generally <em>not</em> the Station ID, which is declared in
     * {@link #getStartStation()}.
     *
     * This is <em>not</em> the Farebox or Machine number.
     */
    @Nullable
    public String getVehicleID() {
        return null;
    }

    /**
     * Machine ID that recorded the transaction. A machine in this context is a farebox, ticket
     * machine, or ticket validator.
     *
     * This is generally <em>not</em> the Station ID, which is declared in
     * {@link #getStartStation()}.
     *
     * This is <em>not</em> the Vehicle number.
     */
    @Nullable
    public String getMachineID() {
        return null;
    }

    /**
     * Number of passengers.
     *
     * -1 is unknown or irrelevant (eg: ticket machine purchases).
     */
    public int getPassengerCount() {
        return -1;
    }

    /**
     * Full name of the agency for the trip. This is used on the map of the trip, where there is
     * space for longer agency names.
     *
     * If this is not known (or there is only one agency for the card), then return null.
     *
     * By default, this returns null.
     *
     * When isShort is true it means to return short name for trip list where space is limited.
     */
    public String getAgencyName(boolean isShort) {
        return null;
    }

    /**
     * Starting station info for the trip, or null if there is no station information available.
     *
     * If supplied, this will be used to render a map of the trip.
     *
     * If there is station information available on the card, but the station is unknown (maybe it
     * doesn't appear in a MdST file, or there is no MdST data available yet), use
     * {@link Station#unknown(String)} or {@link Station#unknown(Integer)} to create an unknown
     * {@link Station}.
     */
    @Nullable
    public Station getStartStation() {
        return null;
    }

    /**
     * Ending station info for the trip, or null if there is no station information available.
     *
     * If supplied, this will be used to render a map of the trip.
     *
     * If there is station information available on the card, but the station is unknown (maybe it
     * doesn't appear in a MdST file, or there is no MdST data available yet), use
     * {@link Station#unknown(String)} or {@link Station#unknown(Integer)} to create an unknown
     * {@link Station}.
     */
    @Nullable
    public Station getEndStation() {
        return null;
    }

    /**
     * Formats the cost of the trip in the appropriate local currency.  Be aware that your
     * implementation should use language-specific formatting and not rely on the system language
     * for that information.
     * <p>
     * For example, if a phone is set to English and travels to Japan, it does not make sense to
     * format their travel costs in dollars.  Instead, it should be shown in Yen, which the Japanese
     * currency formatter does.
     *
     * @return The cost of the fare formatted in the local currency of the card.
     */
    @Nullable
    public abstract TransitCurrency getFare();

    public abstract Mode getMode();

    /**
     * Some cards don't store the exact time of day for each transaction, and only store the date.
     * <p>
     * If true, then a time should be shown next to the transaction in the history view. If false,
     * then the time of day will be hidden.
     * <p>
     * Trips are always sorted by the startTimestamp (including time of day), regardless of the
     * value given here.
     *
     * @return true if a time of day should be displayed.
     */
    public boolean hasTime() {
	return true;
    }

    /**
     * Is there geographic data associated with this trip?
     */
    public boolean hasLocation() {
        final Station startStation = getStartStation();
        final Station endStation = getEndStation();
        return (startStation != null && startStation.hasLocation()) ||
                (endStation != null && endStation.hasLocation());
    }

    public enum Mode {
        BUS(0, R.string.mode_bus),
        /** Used for non-metro (rapid transit) trains */
        TRAIN(1, R.string.mode_train),
        /** Used for trams and light rail */
        TRAM(2, R.string.mode_tram),
        /** Used for electric metro and subway systems */
        METRO(3, R.string.mode_metro),
        FERRY(4, R.string.mode_ferry),
        TICKET_MACHINE(5, R.string.mode_ticket_machine),
        VENDING_MACHINE(6, R.string.mode_vending_machine),
        /** Used for transactions at a store, buying something other than travel. */
        POS(7, R.string.mode_pos),
        OTHER(8, R.string.mode_unknown),
        BANNED(9, R.string.mode_banned),
        TROLLEYBUS(10, R.string.mode_trolleybus);

        final int mImageResourceIdx;
        @StringRes
        final int mDescription;

        Mode(int imageResourceIdx, @StringRes int description) {
            mImageResourceIdx = imageResourceIdx;
            mDescription = description;
        }

        public int getImageResourceIdx() {
            return mImageResourceIdx;
        }

        @StringRes
        public int getDescription() {
            return mDescription;
        }
    }

    public static class Comparator implements java.util.Comparator<Trip> {
        @Override
        public int compare(Trip trip1, Trip trip2) {
            Calendar t1 = trip1.getStartTimestamp() != null ? trip1.getStartTimestamp() : trip1.getEndTimestamp();
            Calendar t2 = trip2.getStartTimestamp() != null ? trip2.getStartTimestamp() : trip2.getEndTimestamp();
            if (t2 != null && t1 != null) {
                return t2.compareTo(t1);
            } else if (t2 != null) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}
