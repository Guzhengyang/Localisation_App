package com.valeo.bleranging.persistence;

import android.Manifest;

/**
 * Created by l-avaratha on 06/07/2017
 */

public class Constants {
    public final static String DEFAULT_JSON_FILE_NAME = "connected_car.json";
    public final static String MAC_ADDRESSES_JSON_FILE_NAME = "mac_addresses.json";
    public final static String THATCHAM_ORIENTED = "thatcham_oriented";
    public final static String PASSIVE_ENTRY_ORIENTED = "passive_entry_oriented";
    public final static int N_VOTE_LONG = 5;
    public final static int N_VOTE_SHORT = 3;
    public final static double THRESHOLD_PROB_UNLOCK2LOCK = 0.5;
    public final static double THRESHOLD_PROB_LOCK2UNLOCK = 0.9;
    public final static String BASE_1 = "Base_1";
    public final static String BASE_2 = "Base_2";
    public final static String BASE_3 = "Base_3";
    public final static String BASE_4 = "Base_4";
    public final static String TYPE_2_A = "M_T";
    public final static String TYPE_2_B = "L_R";
    public final static String TYPE_3_A = "L_M_R";
    public final static String TYPE_4_A = "L_M_R_B";
    public final static String TYPE_4_B = "L_M_R_T";
    public final static String TYPE_5_A = "L_M_R_T_B";
    public final static String TYPE_6_A = "Fl_Fr_M_T_Rl_Rr";
    public final static String TYPE_7_A = "Fl_Fr_L_M_R_Rl_Rr";
    public final static String TYPE_8_A = "Fl_Fr_L_M_R_T_Rl_Rr";
    public final static String DEFAULT_ADDRESS_MAC = "FF:FF:FF:FF:FF:FF";
    public final static int NUMBER_TRX_FRONT_LEFT = 1;
    public final static int NUMBER_TRX_FRONT_RIGHT = 2;
    public final static int NUMBER_TRX_LEFT = 3;
    public final static int NUMBER_TRX_MIDDLE = 4;
    public final static int NUMBER_TRX_RIGHT = 5;
    public final static int NUMBER_TRX_TRUNK = 6;
    public final static int NUMBER_TRX_REAR_LEFT = 7;
    public final static int NUMBER_TRX_BACK = 8;
    public final static int NUMBER_TRX_REAR_RIGHT = 9;
    public static final int NUMBER_MAX_TRX = 16;
    public static final String PREDICTION_INTERNAL = "internal";
    public static final String PREDICTION_ACCESS = "access";
    public static final String PREDICTION_EXTERNAL = "external";
    public static final String PREDICTION_START_FL = "frontleft";
    public static final String PREDICTION_START_FR = "frontright";
    public static final String PREDICTION_START_RL = "backleft";
    public static final String PREDICTION_START_RR = "backright";
    public static final String PREDICTION_START = "start";
    public static final String PREDICTION_LOCK = "lock";
    public static final String PREDICTION_TRUNK = "trunk";
    public static final String PREDICTION_LEFT = "left";
    public static final String PREDICTION_RIGHT = "right";
    public static final String PREDICTION_BACK = "back";
    public static final String PREDICTION_ROOF = "roof";
    public static final String PREDICTION_FRONT = "front";
    public static final String PREDICTION_WELCOME = "welcome";
    public static final String PREDICTION_THATCHAM = "thatcham";
    public static final String PREDICTION_NEAR = "near";
    public static final String PREDICTION_FAR = "far";
    public static final String PREDICTION_INSIDE = "inside";
    public static final String PREDICTION_OUTSIDE = "outside";
    public static final String PREDICTION_UNKNOWN = "unknown";
    public final static String PREDICTION_STD = "standard_prediction";
    public final static String PREDICTION_COORD = "coord_prediction";
    public final static String PREDICTION_IN = "inside_prediction";
    public final static String PREDICTION_TEST = "test_prediction";
    public final static String PREDICTION_RP = "rp_prediction";
    public final static String PREDICTION_EAR = "ear_prediction";
    public final static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.CAMERA
    };
    public final static int RKE_USE_TIMEOUT = 5000;
    public final static int REQUEST_PERMISSION_ALL = 25110;
    public final static String RSSI_DIR = "/InBlueRssi/";
    public final static String CONFIG_DIR = "/InBlueConfig/";
    private static final String PREDICTION_INDOOR = "indoor";
    private static final String PREDICTION_OUTDOOR = "outdoor";
    public final static String[] PREDICTIONS = {
            PREDICTION_INTERNAL,
            PREDICTION_ACCESS,
            PREDICTION_EXTERNAL,
            PREDICTION_START,
            PREDICTION_START_FL,
            PREDICTION_START_FR,
            PREDICTION_START_RL,
            PREDICTION_START_RR,
            PREDICTION_LOCK,
            PREDICTION_ROOF,
            PREDICTION_TRUNK,
            PREDICTION_LEFT,
            PREDICTION_RIGHT,
            PREDICTION_BACK,
            PREDICTION_FRONT,
            PREDICTION_INSIDE,
            PREDICTION_OUTSIDE,
            PREDICTION_INDOOR,
            PREDICTION_OUTDOOR,
            PREDICTION_WELCOME,
            PREDICTION_THATCHAM,
            PREDICTION_NEAR,
            PREDICTION_FAR,
            PREDICTION_UNKNOWN
    };
}
