package com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.Helpers;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects.Gender;
import com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.JsonObjects.Main;
import com.itachi1706.cheesecakeutilities.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Kenneth on 7/9/2016.
 * for com.itachi1706.cheesecakeutilities.Modules.IPPTCalculator.Helpers in CheesecakeUtilities
 */
public class JsonHelper {

    public static Main readFromJsonRaw(Context context) {
        String jsonString = readFromRaw(context);
        if (jsonString.isEmpty() || jsonString.equalsIgnoreCase("")) {
            return null;
        }

        Gson gson = new Gson();
        return gson.fromJson(jsonString, Main.class);
    }

    private static String readFromRaw(Context context) {
        //Get Data From Text Resource File Contains Json Data.
        InputStream inputStream = context.getResources().openRawResource(R.raw.ippt_minified);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        int ctr;
        try {
            ctr = inputStream.read();
            while (ctr != -1) {
                byteArrayOutputStream.write(ctr);
                ctr = inputStream.read();
            }
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return byteArrayOutputStream.toString();
    }

    public static final int MALE = 0, FEMALE = 1;

    public static int getGender(String gender) {
        switch (gender.toLowerCase()) {
            case "m":
            case "male": return MALE;
            case "f":
            case "female": return FEMALE;
        }
        return MALE;
    }

    public static List<String> getAgeRangeText(Context context) {
        return getAgeRangeText(readFromJsonRaw(context));
    }

    public static List<String> getAgeRangeText(Main object) {
        JsonObject jsonObject = object.getAgeRangeText();

        List<String> tmp = new ArrayList<>();
        for (Map.Entry<String,JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            tmp.add(key);
        }
        return tmp;
    }

    public static int getAgeGroup(String ageText, Context context) {
        return getAgeGroup(ageText, readFromJsonRaw(context));
    }

    public static int getAgeGroup(String ageText, Main object) {
        JsonObject jsonObject = object.getAgeRangeText();
        for (Map.Entry<String,JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            if (key.equals(ageText)) {
                JsonElement element = entry.getValue();
                return element.getAsInt();
            }
        }
        return 1;
    }

    public static int calculateScore(int pushup, int situp, int runMin, int runSec, int age, int gender, Context context) {
        return calculateScore(pushup, situp, runMin, runSec, age, gender, readFromJsonRaw(context));
    }

    public static int calculateScore(int pushup, int situp, int runMin, int runSec, int age, int gender, Main object) {
        Gender exercisesScore;
        if (gender == FEMALE) exercisesScore = object.getDataFemale();
        else exercisesScore = object.getDataMale();
        Log.i("IPPTCalc", "Age Group: " + age);

        int situpScore = getSitUpScore(situp, age, exercisesScore);
        int pushupScore = getPushUpScore(pushup, age, exercisesScore);
        int runScore = getRunScore(runMin, runSec, age, exercisesScore);
        Log.i("IPPTCalc", "Situp Score: " + situp + " (" + situpScore + ")");
        Log.i("IPPTCalc", "Pushup Score: " + pushup + " (" + pushupScore + ")");
        Log.i("IPPTCalc", "Run Score: " + runMin + ":" + runSec + " (" + runScore + ")");
        return situpScore+pushupScore+runScore;
    }

    public static String getScoreResults(int score) {
        if (score >= 90) return "Gold (Commando/Guards)";
        if (score >= 85) return "Gold";
        if (score >= 75) return "Silver";
        if (score >= 61) return "Pass (Active)/Pass with Incentive (NSMen)";
        if (score >= 51) return "Fail (Active)/Pass (NSMen)";
        return "Fail";
    }

    public static int getSitUpScore(int situp, int ageGroup, Gender object) {
        JsonObject obj = object.getSitups();
        JsonElement element = null;
        for (Map.Entry<String,JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if (key.equals(situp + "")) {
                element = entry.getValue();
                break;
            }
        }
        if (element == null) return 25; // Presume full marks
        JsonObject el = element.getAsJsonObject();
        return el.get(ageGroup + "").getAsInt();
    }

    public static int getPushUpScore(int pushup, int ageGroup, Gender object) {
        JsonObject obj = object.getPushups();
        JsonElement element = null;
        for (Map.Entry<String,JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            if (key.equals(pushup + "")) {
                element = entry.getValue();
                break;
            }
        }
        if (element == null) return 25; // Presume full marks
        JsonObject el = element.getAsJsonObject();
        return el.get(ageGroup + "").getAsInt();
    }

    public static int getRunScore(int runMin, int runSec, int ageGroup, Gender object) {
        int totalSecs = (runMin * 60) + runSec;
        JsonObject obj = object.getRun();
        JsonElement element = null;
        for (Map.Entry<String,JsonElement> entry : obj.entrySet()) {
            String key = entry.getKey();
            String[] values = key.split(":");
            int totalSec = (Integer.parseInt(values[0]) * 60) + Integer.parseInt(values[1]);
            if (totalSecs <= totalSec) {
                element = entry.getValue();
                break;
            }
        }
        if (element == null) return 0; // Presume run out of time
        JsonObject el = element.getAsJsonObject();
        return el.get(ageGroup + "").getAsInt();
    }
}
