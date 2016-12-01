package com.valeo.bleranging.bluetooth.scanresponse;

import java.util.Arrays;

/**
 * Created by nhaan on 24/08/2015
 */
public class CentralScanResponse {
    public final byte antennaId;
    public final byte vehicleState;
    private final byte[] random;
    private final byte[] cryptedCidpu;
    private final byte protocolVersion;
    private final byte mode;
    private final int reSynchro;

    public CentralScanResponse(byte[] random, byte[] cryptedID, byte protocolVersion, byte antennaId, byte mode, int reSynchro, byte vehicleState) {
        this.random = random;
        this.cryptedCidpu = cryptedID;
        this.protocolVersion = protocolVersion;
        this.antennaId = antennaId;
        this.mode = mode;
        this.reSynchro = reSynchro;
        this.vehicleState = vehicleState;
    }

    @Override
    public String toString() {
        return "CentralScanResponse{" +
                "random=" + Arrays.toString(random) +
                ", cryptedCidpu=" + Arrays.toString(cryptedCidpu) +
                ", protocolVersion=" + protocolVersion +
                ", antennaId=" + antennaId +
                ", mode=" + mode +
                ", reSynchro=" + reSynchro +
                ", vehicleState=" + vehicleState +
                '}';
    }
}
