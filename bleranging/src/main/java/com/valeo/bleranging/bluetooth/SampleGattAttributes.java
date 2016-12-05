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

/**
 * Lists all the services and characteristics used during the application lifecycle 
 */
public class SampleGattAttributes {
    //RKE
    public final static String VALEO_GENERIC_SERVICE = "f000ff10-0451-4000-b000-000000000000";
    public final static String VALEO_IN_CHARACTERISTIC = "f000ff11-0451-4000-b000-000000000000";
    public final static String VALEO_OUT_CHARACTERISTIC = "f000ff12-0451-4000-b000-000000000000";
    public final static String VALEO_REMOTE_CONTROL_GENERIC_SERVICE = "f000ff13-0451-4000-b000-000000000000";
    public final static String VALEO_REMOTE_CONTROL_IN_CHARACTERISTIC = "f000ff14-0451-4000-b000-000000000000";
    public final static String VALEO_REMOTE_CONTROL_OUT_CHARACTERISTIC = "f000ff15-0451-4000-b000-000000000000";
    public final static String VALEO_REMOTE_CONTROL_ADV_SERVICE_UUID = "0000ff15-0000-1000-8000-00805f9b34fb";
    public final static String VALEO_PC_GENERIC_SERVICE = "F0001130-0451-4000-B000-000000000000";
    public final static String VALEO_PC_IN_CHARACTERISTIC = "F0001131-0451-4000-B000-000000000000";
    public final static String VALEO_PC_OUT_CHARACTERISTIC = "F0001132-0451-4000-B000-000000000000";
    public final static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
}