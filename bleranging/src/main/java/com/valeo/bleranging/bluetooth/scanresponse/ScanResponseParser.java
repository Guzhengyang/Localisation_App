package com.valeo.bleranging.bluetooth.scanresponse;

/**
 * Created by l-avaratha on 01/12/2016
 */

public class ScanResponseParser {

    /**
     * Get a central scanResponse by parsing the advertised data
     *
     * @param advertisedData the advertised data
     * @return a centralScanResponse
     */
    public static CentralScanResponse parseCentralScanResponse(final byte[] advertisedData) {
        byte[] random = null;
        byte[] mac = null;
        byte vehicleState = (byte) 0xFF;
        byte protocolVersion = (byte) 0;
        byte antennaId = (byte) 0xFF;
        byte mode = (byte) 0xFF; //mode: RKE, PE, PS, etc
        int reSynchro = 0; //Timestamp InSync updated

        int offset = 0;
        while (offset < (advertisedData.length - 2)) {
            int len = advertisedData[offset++];
            if (len <= 0)
                break;
            int type = advertisedData[offset++];
            switch (type) {
                case (byte) 0x02: // Partial list of 16-bit UUIDs
                case (byte) 0x03: // Complete list of 16-bit UUIDs
                    while (len > 1) {
                        len -= 2;
                    }
                    break;
                case (byte) 0x06:// Partial list of 128-bit UUIDs
                case (byte) 0x07:// Complete list of 128-bit UUIDs
                    // Loop through the advertised 128-bit UUID's.
                    while (len >= 16) {
                        // Move the offset to read the next uuid.
                        offset += 15;
                        len -= 16;
                    }
                    break;
                case (byte) 0xFF:// GAP_ADTYPE_MANUFACTURER_SPECIFIC
                    // Type of the response : Random identifier data
                    if (advertisedData[offset] == (byte) 0xEE && advertisedData[offset + 1] == (byte) 0x01) {
                        //Length 8 is the content of the advertised data
                        if (len == 8) {
                            protocolVersion = advertisedData[offset + 2];
                            antennaId = (byte) ((advertisedData[offset + 3] >> 4) & 0x0F);
                            mode = (byte) (advertisedData[offset + 3] & 0x0F);
                            reSynchro = ((((int) advertisedData[offset + 4]) << 8) | advertisedData[offset + 5]);
                            vehicleState = advertisedData[offset + 6];
                        }
                        //Length 19 is the content of the Scan Response
                        else if (len == 19) {
                            random = new byte[9];
                            random[0] = advertisedData[offset + 2];
                            random[1] = advertisedData[offset + 3];
                            random[2] = advertisedData[offset + 4];
                            random[3] = advertisedData[offset + 5];
                            random[4] = advertisedData[offset + 6];
                            random[5] = advertisedData[offset + 7];
                            random[6] = advertisedData[offset + 8];
                            random[7] = advertisedData[offset + 9];
                            random[8] = advertisedData[offset + 10];
                            mac = new byte[7];
                            mac[0] = advertisedData[offset + 11];
                            mac[1] = advertisedData[offset + 12];
                            mac[2] = advertisedData[offset + 13];
                            mac[3] = advertisedData[offset + 14];
                            mac[4] = advertisedData[offset + 15];
                            mac[5] = advertisedData[offset + 16];
                            mac[6] = advertisedData[offset + 17];
                            //ACH DEBUG. Force uuids of extra services
                        }
                    }
                    offset += (len - 1);
                    break;
                default:
                    offset += (len - 1);
                    break;
            }
        }
        return new CentralScanResponse(random, mac, protocolVersion,
                antennaId, mode, reSynchro, vehicleState);
    }

    /**
     * Get a beacon scanResponse by parsing the advertised data
     *
     * @param advertisedData the advertised data
     * @return a beaconScanResponse
     */
    public static BeaconScanResponse parseBeaconScanResponse(final byte[] advertisedData) {
        byte pcbaPnIndicator;
        byte[] softwareVersion = new byte[2];
        byte advertisingChannel;
        int dataType;
        byte[] data = new byte[4];
        byte batteryMeasurement;
        byte[] rollingCounter = new byte[3];
        byte beaconPosition;
        int offset = 13;
        pcbaPnIndicator = advertisedData[offset + 1];
        System.arraycopy(advertisedData, offset + 2, softwareVersion, 0, 2);
        advertisingChannel = (byte) ((advertisedData[offset + 4] & 0xC0) >> 6);
        dataType = ((advertisedData[offset + 4] & 0x38) >> 3);
        System.arraycopy(advertisedData, offset + 5, data, 0, 4);
        batteryMeasurement = advertisedData[offset + 9];
        System.arraycopy(advertisedData, offset + 10, rollingCounter, 0, 3);
        beaconPosition = advertisedData[offset + 13];
        return new BeaconScanResponse(pcbaPnIndicator, softwareVersion, advertisingChannel,
                dataType, data, batteryMeasurement, rollingCounter, beaconPosition);
    }
}
