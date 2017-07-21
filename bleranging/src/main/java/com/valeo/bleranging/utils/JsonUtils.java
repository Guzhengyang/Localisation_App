package com.valeo.bleranging.utils;

import android.content.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.valeo.bleranging.model.Trx;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.valeo.bleranging.persistence.Constants.DEFAULT_JSON_FILE_NAME;

/**
 * Created by l-avaratha on 17/07/2017
 */

public class JsonUtils {
    /**
     * Contains the content of the JSON file.
     */
    private static JsonArray mJsonConnectedCarArray;
    private static JsonArray mJsonTrxArray;
    private static JsonArray mJsonPredictionZoneArray;
    private static JsonArray mJsonPredictionCoordArray;

    /**
     * Read the JSON file to get the stored data (mainly the default values).
     *
     * @param context Context
     */
    public static void getStoredData(Context context) {
        InputStream stream;
        JsonParser parser = new JsonParser();
        Reader reader;
        try {
            stream = context.getAssets().open(DEFAULT_JSON_FILE_NAME);
            if (stream != null) {
                reader = new InputStreamReader(stream, "UTF-8");
                // We parse the Reader in order to use it as a JsonObject and import it into the preferences part.
                JsonObject config = parser.parse(reader).getAsJsonObject();
                mJsonConnectedCarArray = config.getAsJsonArray("connected_car");
                mJsonTrxArray = config.getAsJsonArray("trx");
                mJsonPredictionZoneArray = config.getAsJsonArray("prediction_zone");
                mJsonPredictionCoordArray = config.getAsJsonArray("prediction_coord");
            }
        } catch (IOException e) {
            PSALogs.e("json", e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convert the Reader into a JsonObject and use it to access the value we want.
     *
     * @param searchedKey The key of the value we want
     * @return A string array containing the values (car_type_code, car_type_string value, total_beacon)
     */
    public static String[] getConnectedCarJsonContent(String searchedKey) {
        PSALogs.d("json", "getConnectedCarJsonContent searchedKey = " + searchedKey);
        String[] data = new String[3];
        // Then, we read through the array until we find our key
        for (int i = 0; i < mJsonConnectedCarArray.size(); ++i) {
            JsonObject connectedCar = mJsonConnectedCarArray.get(i).getAsJsonObject();
            String carTypeString = connectedCar.getAsJsonPrimitive("car_type_string").getAsString();
            PSALogs.d("json", "getConnectedCarJsonContent car_type_string = " + carTypeString);
            if (carTypeString.compareTo(searchedKey) == 0) {
                data[0] = carTypeString;
                data[1] = connectedCar.getAsJsonPrimitive("car_type_code").getAsString();
                PSALogs.d("json", "getConnectedCarJsonContent car_type_code = " + data[1]);
                data[2] = connectedCar.getAsJsonPrimitive("total_beacon").getAsString();
                PSALogs.d("json", "getConnectedCarJsonContent total_beacon = " + data[2]);
                break;
            }
        }
        return data;
    }

    private static String[] getTrxJsonContent(String searchedKey) {
        String[] data = new String[4];
        // Then, we read through the array until we find our key
        for (int i = 0; i < mJsonTrxArray.size(); ++i) {
            JsonObject trx = mJsonTrxArray.get(i).getAsJsonObject();
            String trx_code = trx.getAsJsonPrimitive("trx_code").getAsString();
            PSALogs.d("json", "getTrxJsonContent trx_code = " + trx_code);
            if (trx_code.compareTo(searchedKey) == 0) {
                data[0] = trx_code;
                data[1] = trx.getAsJsonPrimitive("trx_name").getAsString();
                PSALogs.d("json", "getTrxJsonContent trx_name = " + data[1]);
                data[2] = trx.getAsJsonPrimitive("trx_number").getAsString();
                PSALogs.d("json", "getTrxJsonContent trx_number = " + data[2]);
                data[3] = trx.getAsJsonPrimitive("row_data_key_set_title").getAsString();
                PSALogs.d("json", "getTrxJsonContent row_data_key_set_title = " + data[3]);
                break;
            }
        }
        return data;
    }

    public static LinkedHashMap<Integer, Trx> createTrxList(String carTypeString, int totalBeacon) {
        final LinkedHashMap<Integer, Trx> trxLinked = new LinkedHashMap<>();
        String[] splitCode = carTypeString.split("_");
        for (int i = 0; i < totalBeacon; i++) {
            String[] data = getTrxJsonContent(splitCode[i]);
            if (data != null && data.length >= 3) {
                trxLinked.put(Integer.valueOf(data[2]), new Trx(Integer.valueOf(data[2]), data[1]));
            }
        }
        return trxLinked;
    }

    public static List<String> createRowDataList(final String carTypeString) {
        List<String> rowDataArrayList = new ArrayList<>();
        final String[] splitCode = carTypeString.split("_");
        for (String aSplitCode : splitCode) {
            String[] data = getTrxJsonContent(aSplitCode);
            if (data != null && data.length >= 3) {
                rowDataArrayList.add(data[3]);
            }
        }
        return rowDataArrayList;
    }

    /**
     * Convert the Reader into a JsonObject and use it to access the value we want.
     *
     * @param searchedKey The key of the value we want
     * @return A string array containing the values (row_data_key_set_string, model_file_name)
     */
    public static String[] getPredictionZoneJsonContent(String searchedKey,
                                                        String expectedPredictionType,
                                                        boolean expectedMini,
                                                        String expectedThatchamOrientation) {
        PSALogs.d("json", "getPredictionZoneJsonContent searchedKey = " + searchedKey);
        PSALogs.d("json", "getPredictionZoneJsonContent expectedPredictionType = " + expectedPredictionType);
        PSALogs.d("json", "getPredictionZoneJsonContent expectedMini = " + expectedMini);
        PSALogs.d("json", "getPredictionZoneJsonContent expectedThatchamOrientation = " + expectedThatchamOrientation);
        String[] data = null;
        // Then, we read through the array until we find our key
        for (int i = 0; i < mJsonPredictionZoneArray.size(); ++i) {
            JsonObject predictionZone = mJsonPredictionZoneArray.get(i).getAsJsonObject();
            String carTypeString = predictionZone.getAsJsonPrimitive("car_type_string").getAsString();
            PSALogs.d("json", "getPredictionZoneJsonContent car_type_string = " + carTypeString);
            String predictionType = predictionZone.getAsJsonPrimitive("prediction_type").getAsString();
            PSALogs.d("json", "getPredictionZoneJsonContent prediction_type = " + predictionType);
            boolean isMini = predictionZone.getAsJsonPrimitive("is_mini").getAsBoolean();
            PSALogs.d("json", "getPredictionZoneJsonContent is_mini = " + isMini);
            String isThatchamOriented = predictionZone.getAsJsonPrimitive("is_thatcham_oriented").getAsString();
            PSALogs.d("json", "getPredictionZoneJsonContent is_thatcham_oriented = " + isThatchamOriented);
            if ((carTypeString.compareTo(searchedKey) == 0)
                    && (predictionType.compareTo(expectedPredictionType) == 0)
                    && (expectedMini == isMini)
                    && (expectedThatchamOrientation.compareTo(isThatchamOriented) == 0)) {
                data = new String[6];
                data[0] = carTypeString;
                data[1] = predictionType;
                data[2] = String.valueOf(isMini);
                data[3] = isThatchamOriented;
                data[4] = predictionZone.getAsJsonPrimitive("row_data_key_set_string").getAsString();
                PSALogs.d("json", "getPredictionZoneJsonContent row_data_key_set_string = " + data[4]);
                data[5] = predictionZone.getAsJsonPrimitive("model_file_name").getAsString();
                PSALogs.d("json", "getPredictionZoneJsonContent model_file_name = " + data[5]);
                break;
            }
        }
        return data;
    }

    /**
     * Convert the Reader into a JsonObject and use it to access the value we want.
     *
     * @param searchedKey The key of the value we want
     * @return A string array containing the values (row_data_key_set_string, model_file_name)
     */
    public static String[] getPredictionCoordJsonContent(String searchedKey) {
        PSALogs.d("json", "getPredictionZoneJsonContent searchedKey = " + searchedKey);
        String[] data = null;
        // Then, we read through the array until we find our key
        for (int i = 0; i < mJsonPredictionCoordArray.size(); ++i) {
            JsonObject predictionZone = mJsonPredictionCoordArray.get(i).getAsJsonObject();
            String carTypeString = predictionZone.getAsJsonPrimitive("car_type_string").getAsString();
            PSALogs.d("json", "getPredictionZoneJsonContent car_type_string = " + carTypeString);
            if (carTypeString.compareTo(searchedKey) == 0) {
                data = new String[4];
                data[0] = carTypeString;
                data[1] = predictionZone.getAsJsonPrimitive("row_data_key_set_string").getAsString();
                PSALogs.d("json", "getPredictionZoneJsonContent row_data_key_set_string = " + data[1]);
                data[2] = predictionZone.getAsJsonPrimitive("model_file_name_X").getAsString();
                PSALogs.d("json", "getPredictionZoneJsonContent model_file_name_X = " + data[2]);
                data[3] = predictionZone.getAsJsonPrimitive("model_file_name_Y").getAsString();
                PSALogs.d("json", "getPredictionZoneJsonContent model_file_name_Y = " + data[3]);
                break;
            }
        }
        return data;
    }
}
