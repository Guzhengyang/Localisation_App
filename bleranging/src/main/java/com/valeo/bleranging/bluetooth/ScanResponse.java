package com.valeo.bleranging.bluetooth;

import java.util.Arrays;

/**
 * Created by nhaan on 24/08/2015.
 */
public class ScanResponse {
    public byte[] random;
    public byte[] cryptedCidpu;
    public byte protocolVersion;
    public byte antennaId;
    public byte mode;
    public int reSynchro;
    public byte vehicleState;

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
