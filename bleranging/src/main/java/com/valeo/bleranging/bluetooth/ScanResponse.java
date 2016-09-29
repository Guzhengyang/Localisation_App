package com.valeo.bleranging.bluetooth;

import java.util.Arrays;

/**
 * Created by nhaan on 24/08/2015.
 */
public class ScanResponse {
    public final byte antennaId;
    public final byte vehicleState;
    private final byte[] random;
    private final byte[] cryptedCidpu;
    private final byte protocolVersion;
    private final byte mode;
    private final int reSynchro;

    public ScanResponse(byte[] random, byte[] cryptedID, byte protocolVersion, byte antennaId, byte mode, int reSynchro, byte vehicleState) {
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
        return "ScanResponse{" +
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
