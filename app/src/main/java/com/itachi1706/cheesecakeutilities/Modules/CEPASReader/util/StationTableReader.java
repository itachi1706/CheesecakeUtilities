package com.itachi1706.cheesecakeutilities.Modules.CEPASReader.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.itachi1706.cheesecakeutilities.R;
import com.itachi1706.cheesecakeutilities.CheesecakeUtilitiesApplication;
import com.itachi1706.sgcardreader.proto.Stations;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.Station;
import com.itachi1706.cheesecakeutilities.Modules.CEPASReader.transit.Trip;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * SGCardReader Station Table (MdST) file reader.
 *
 * For more information about the file format, see extras/mdst/README.md in the SGCardReader source
 * repository.
 */
public class StationTableReader {
    private static final byte[] MAGIC = new byte[] { 0x4d, 0x64, 0x53, 0x54 };
    private static final int VERSION = 1;
    private static final String TAG = "StationTableReader";

    private Stations.StationDb mStationDb;
    private Stations.StationIndex mStationIndex;
    private DataInputStream mTable;

    private static final Map<String,StationTableReader> mSTRs = new HashMap<>();

    static private StationTableReader getSTR(@Nullable String name) {
        if (name == null)
            return null;
        synchronized (mSTRs) {
            if (mSTRs.containsKey(name))
                return mSTRs.get(name);
        }

        try {
            StationTableReader str = new StationTableReader(CheesecakeUtilitiesApplication.getInstance(),
                    name + ".mdst");
            synchronized (mSTRs) {
                mSTRs.put(name, str);
            }
            return str;
        } catch (Exception e) {
            Log.w(TAG, "Couldn't open DB " + name, e);
            return null;
        }
    }

    @Nullable
    public static Station getStationNoFallback(@Nullable String reader, int id,
                                               String humanReadableId) {
        StationTableReader str = StationTableReader.getSTR(reader);
        if (str == null)
            return null;
        try {
            return str.getStationById(id, humanReadableId);
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    public static Station getStationNoFallback(@Nullable String reader, int id) {
        return getStationNoFallback(reader, id, "0x" + Integer.toHexString(id));
    }

    @NonNull
    public static Station getStation(@Nullable String reader, int id, String humanReadableId) {
        Station s = getStationNoFallback(reader, id, humanReadableId);
        if (s == null)
            return Station.unknown(humanReadableId);
        return s;
    }

    @NonNull
    public static Station getStation(@Nullable String reader, int id) {
        return getStation(reader, id, "0x" + Integer.toHexString(id));
    }

    private static String fallbackName(int id) {
        return Utils.localizeString(R.string.unknown_format, "0x" + Integer.toHexString(id));
    }

    private static String fallbackName(String humanReadableId) {
        return Utils.localizeString(R.string.unknown_format, humanReadableId);
    }

    public static Trip.Mode getOperatorDefaultMode(@Nullable String reader, int id) {
        if (reader == null)
            return Trip.Mode.OTHER;
        StationTableReader str = StationTableReader.getSTR(reader);
        if (str == null)
            return Trip.Mode.OTHER;
        Trip.Mode m = str.getOperatorDefaultMode(id);
        if (m == null)
            return Trip.Mode.OTHER;
        return m;
    }

    public class InvalidHeaderException extends Exception {}

    /**
     * Initialises a "connection" to a SGCardReader Station Table kept in the `assets/` directory.
     * @param context Application context to use for fetching Assets.
     * @param dbName MdST filename
     * @throws IOException On read errors
     * @throws InvalidHeaderException If the file is not a MdST file.
     */
    private StationTableReader(Context context, String dbName) throws IOException, InvalidHeaderException {
        InputStream i = context.getAssets().open(dbName, AssetManager.ACCESS_RANDOM);
        mTable = new DataInputStream(i);

        // Read the Magic, and validate it.
        byte[] header = new byte[4];
        mTable.read(header);
        if (!Arrays.equals(header, MAGIC)) {
            throw new InvalidHeaderException();
        }

        // Check the version
        int version = mTable.readInt();
        if (version != VERSION) {
            throw new InvalidHeaderException();
        }

        int stationsLength = mTable.readInt();

        // Read out the header
        mStationDb = Stations.StationDb.parseDelimitedFrom(mTable);

        // Mark where the start of the station list is.
        // AssetInputStream allows unlimited seeking, no need to specify a readlimit.
        mTable.mark(0);

        // Skip over the station list
        mTable.skipBytes(stationsLength);

        // Read out the index
        mStationIndex = Stations.StationIndex.parseDelimitedFrom(mTable);

        // Reset back to the start of the station list.
        mTable.reset();
    }

    private boolean useEnglishName() {
        String locale = Locale.getDefault().getLanguage();
        return !mStationDb.getLocalLanguagesList().contains(locale);
    }

    public String selectBestName(Stations.Names name, boolean isShort) {
        String englishFull = name.getEnglish();
        String englishShort = name.getEnglishShort();
        String english;
        boolean hasEnglishFull = englishFull != null && englishFull.length() != 0;
        boolean hasEnglishShort = englishShort != null && englishShort.length() != 0;

        if (hasEnglishFull && !hasEnglishShort)
            english = englishFull;
        else if (!hasEnglishFull && hasEnglishShort)
            english = englishShort;
        else
            english = isShort ? englishShort : englishFull;

        String localFull = name.getLocal();
        String localShort = name.getLocalShort();
        String local;
        boolean hasLocalFull = localFull != null && localFull.length() != 0;
        boolean hasLocalShort = localShort != null && localShort.length() != 0;

        if (hasLocalFull && !hasLocalShort)
            local = localFull;
        else if (!hasLocalFull && hasLocalShort)
            local = localShort;
        else
            local = isShort ? localShort : localFull;

        if (useEnglishName() && english != null && !english.equals("")) {
            return english;
        }

        if (local != null && !local.equals("")) {
            // Local preferred, or English not available
            return local;
        } else {
            // Local unavailable, use English
            return english;
        }
    }

    /**
     * Gets a Station object, according to the MdST Protobuf definition.
     * @param id Stop ID
     * @return Station object, or null if it could not be found.
     * @throws IOException on read errors
     */
    private Stations.Station getProtoStationById(int id) throws IOException {
        mTable.reset();

        int offset;
        try {
            offset = mStationIndex.getStationMapOrThrow(id);
        } catch (IllegalArgumentException e) {
            Log.d(TAG, String.format(Locale.ENGLISH, "Unknown station %d", id), e);
            return null;
        }

        mTable.skipBytes(offset);
        return Stations.Station.parseDelimitedFrom(mTable);
    }

    public static String getLineName(@Nullable String reader, int id) {
        return getLineName(reader, id, "0x" + Integer.toHexString(id));
    }

    public static String getLineName(@Nullable String reader, int id, String humanReadableId) {
        if (reader == null)
            return fallbackName(humanReadableId);

        StationTableReader str = getSTR(reader);
        if (str == null)
            return fallbackName(humanReadableId);
        Stations.Line pl = str.mStationDb.getLinesOrDefault(id, null);
        if (pl == null)
            return fallbackName(humanReadableId);
        return str.selectBestName(pl.getName(), false);
    }

    public static Trip.Mode getLineMode(@Nullable String reader, int id) {
        StationTableReader str = getSTR(reader);
        if (str == null)
            return null;
        Stations.Line pl = str.mStationDb.getLinesOrDefault(id, null);
        if (pl == null)
            return null;
        if (pl.getTransport() == Stations.TransportType.UNKNOWN)
            return null;
        return Trip.Mode.valueOf(pl.getTransport().toString());
    }

    private Trip.Mode getOperatorDefaultMode(int oper) {
        Stations.Operator po = mStationDb.getOperatorsOrDefault(oper, null);
        if (po == null)
            return null;
        if (po.getDefaultTransport() == Stations.TransportType.UNKNOWN)
            return null;
        return Trip.Mode.valueOf(po.getDefaultTransport().toString());
    }

    public static String getOperatorName(@Nullable String reader, int id, boolean isShort) {
        StationTableReader str = StationTableReader.getSTR(reader);
        if (str == null)
            return fallbackName(id);
        Stations.Operator po = str.mStationDb.getOperatorsOrDefault(id, null);
        if (po == null)
            return fallbackName(id);
        return str.selectBestName(po.getName(), isShort);
    }

    /**
     * Gets a SGCardReader-native Station object for a given stop ID.
     * @param id Stop ID.
     * @return Station object, or null if it could not be found.
     * @throws IOException on read errors
     */
    private Station getStationById(int id, String humanReadableID) throws IOException {
        Stations.Station ps = getProtoStationById(id);
        if (ps == null) return null;

        return Station.fromProto(humanReadableID, ps,
                mStationDb.getOperatorsOrDefault(ps.getOperatorId(), null),
                mStationDb.getLinesOrDefault(ps.getLineId(), null),
                mStationDb.getTtsHintLanguage(), this);
    }

}
