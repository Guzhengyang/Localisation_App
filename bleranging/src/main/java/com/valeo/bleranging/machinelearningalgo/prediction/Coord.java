package com.valeo.bleranging.machinelearningalgo.prediction;

/**
 * Created by l-avaratha on 30/06/2017
 */

public class Coord {
    private double coord_x, coord_y;

    public Coord(double coord_x, double coord_y) {
        this.coord_x = coord_x;
        this.coord_y = coord_y;
    }

    public double getCoord_x() {
        return coord_x;
    }

    public void setCoord_x(double coord_x) {
        this.coord_x = coord_x;
    }

    public double getCoord_y() {
        return coord_y;
    }

    public void setCoord_y(double coord_y) {
        this.coord_y = coord_y;
    }
}
