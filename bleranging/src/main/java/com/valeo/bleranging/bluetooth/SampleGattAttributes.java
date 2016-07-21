/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.valeo.bleranging.bluetooth;

import java.util.HashMap;

/**
 * Lists all the services and characteristics used during the application lifecycle 
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();
    //RKE
    public static String VALEO_GENERIC_SERVICE = "f000ff10-0451-4000-b000-000000000000";
    public static String VALEO_IN_CHARACTERISTIC = "f000ff11-0451-4000-b000-000000000000";
    public static String VALEO_OUT_CHARACTERISTIC = "f000ff12-0451-4000-b000-000000000000";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        // Services.
        attributes.put(VALEO_GENERIC_SERVICE, "Valeo Generic Service");
        //Characteristics
        attributes.put(VALEO_IN_CHARACTERISTIC, "Valeo IN Characteristic");
        attributes.put(VALEO_OUT_CHARACTERISTIC, "Valeo OUT Characteristic");
    }
}