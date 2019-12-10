package com.itachi1706.cheesecakeutilities.util;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.MalformedJsonException;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static android.util.JsonToken.END_DOCUMENT;

/**
 * Created by Kenneth on 15/10/2017.
 * for com.itachi1706.cheesecakeutilities.Util in CheesecakeUtilities
 */

public class JSONHelper {
    public static boolean isJsonValid(final String json) {
        try {
            return isJsonValid(new StringReader(json));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isJsonValid(final Reader reader)
            throws IOException {
        return isJsonValid(new JsonReader(reader));
    }

    private static boolean isJsonValid(final JsonReader jsonReader)
            throws IOException {
        try {
            JsonToken token;
            loop:
            while ( (token = jsonReader.peek()) != END_DOCUMENT && token != null ) {
                switch ( token ) {
                    case BEGIN_ARRAY:
                        jsonReader.beginArray();
                        break;
                    case END_ARRAY:
                        jsonReader.endArray();
                        break;
                    case BEGIN_OBJECT:
                        jsonReader.beginObject();
                        break;
                    case END_OBJECT:
                        jsonReader.endObject();
                        break;
                    case NAME:
                        jsonReader.nextName();
                        break;
                    case STRING:
                    case NUMBER:
                    case BOOLEAN:
                    case NULL:
                        jsonReader.skipValue();
                        break;
                    case END_DOCUMENT:
                        break loop;
                    default:
                        throw new AssertionError(token);
                }
            }
            return true;
        } catch ( final MalformedJsonException ignored ) {
            return false;
        }
    }
}
