package com.valeo.bleranging.utils;

import com.valeo.bleranging.machinelearningalgo.prediction.Coord;

import org.ejml.simple.SimpleMatrix;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INTERNAL;

/**
 * Created by l-avaratha on 30/06/2017
 */

public class CalculusUtils {
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private static final int MAX_X = 11;
    private static final int MAX_Y = 10;
    private static final double X1 = 3, X2 = 7, Y1 = 4, Y2 = 6;
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P0 = -22;
    private static SimpleMatrix X, P, F, G, H, Q, R;

    public static void initMatrix() {
        X = new SimpleMatrix(new double[][]{{0}, {0}, {0}, {0}});
        P = new SimpleMatrix(SimpleMatrix.identity(X.numRows()));
        double dt = 0.1;
        F = new SimpleMatrix(new double[][]{{1, dt, 0, 0}, {0, 1, 0, 1}, {0, 0, 1, dt}, {0, 0, 0, 1}});
        G = new SimpleMatrix(new double[][]{{dt * dt / 2, 0}, {dt, 0}, {0, dt * dt / 2}, {0, dt}});
        H = new SimpleMatrix(new double[][]{{1, 0, 0, 0}, {0, 0, 1, 0}});
        Q = new SimpleMatrix(SimpleMatrix.identity(2));
        R = new SimpleMatrix(SimpleMatrix.identity(H.numRows()));
    }

    public static void correctBoundary(final Coord coord) {
        if (coord.getCoord_x() > MAX_X) {
            coord.setCoord_x(MAX_X);
        } else if (coord.getCoord_x() < 0) {
            coord.setCoord_x(0);
        }
        if (coord.getCoord_y() > MAX_Y) {
            coord.setCoord_y(MAX_Y);
        } else if (coord.getCoord_y() < 0) {
            coord.setCoord_y(0);
        }
    }

    public static void correctCoordKalman(final Coord coord, final Coord coord_new) {
        SimpleMatrix z = new SimpleMatrix(new double[][]{{coord_new.getCoord_x()}, {coord_new.getCoord_y()}});
        X = F.mult(X);
        P = F.mult(P).mult(F.transpose()).plus(G.mult(Q).mult(G.transpose()));
        SimpleMatrix k = P.mult(H.transpose()).mult((H.mult(P).mult(H.transpose()).plus(R)).invert());
        X = X.plus(k.mult(z.minus(H.mult(X))));
        P = P.minus(k.mult(H.mult(P)));
        coord.setCoord_x(X.get(0));
        coord.setCoord_y(X.get(2));
    }

    public static double calculateDist2Car(double coord_x, double coord_y) {
        double dist2car;
        if ((coord_x < X1) & (coord_y < Y1)) {
            dist2car = Math.sqrt((coord_x - X1) * (coord_x - X1) + (coord_y - Y1) * (coord_y - Y1));
        } else if ((coord_x < X1) & (coord_y > Y2)) {
            dist2car = Math.sqrt((coord_x - X1) * (coord_x - X1) + (coord_y - Y2) * (coord_y - Y2));
        } else if ((coord_x > X2) & (coord_y < Y1)) {
            dist2car = Math.sqrt((coord_x - X2) * (coord_x - X2) + (coord_y - Y1) * (coord_y - Y1));
        } else if ((coord_x > X2) & (coord_y > Y2)) {
            dist2car = Math.sqrt((coord_x - X2) * (coord_x - X2) + (coord_y - Y2) * (coord_y - Y2));
        } else if ((coord_x < X1) & (coord_y > Y1) & (coord_y < Y2)) {
            dist2car = X1 - coord_x;
        } else if ((coord_x > X2) & (coord_y > Y1) & (coord_y < Y2)) {
            dist2car = coord_x - X2;
        } else if ((coord_y < Y1) & (coord_x > X1) & (coord_x < X2)) {
            dist2car = Y1 - coord_y;
        } else if ((coord_y > Y2) & (coord_x > X1) & (coord_x < X2)) {
            dist2car = coord_y - Y2;
        } else {
            dist2car = -1;
        }
        return dist2car;
    }

    public static String coord2zone(double coord_x, double coord_y, double threshold_unlock_lock) {
        double dist2car = calculateDist2Car(coord_x, coord_y);
        if (dist2car == -1) {
            return PREDICTION_INTERNAL;
        } else if (dist2car < threshold_unlock_lock) {
            return PREDICTION_ACCESS;
        } else {
            return PREDICTION_EXTERNAL;
        }
    }


    public static void correctCoordThreshold(final Coord coord, final Coord coordNew, final double threshold_dist) {
        double deltaX = coordNew.getCoord_x() - coord.getCoord_x();
        double deltaY = coordNew.getCoord_y() - coord.getCoord_y();
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (dist > threshold_dist) {
            double ratio = threshold_dist / dist;
            coord.setCoord_x(coord.getCoord_x() + deltaX * ratio);
            coord.setCoord_y(coord.getCoord_y() + deltaY * ratio);
        } else {
            coord.setCoord_x(coordNew.getCoord_x());
            coord.setCoord_y(coordNew.getCoord_y());
        }
    }

    public static double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P0) / 20);
    }

    public static double correctRssiUnilateral(double rssi_old, double rssi_new) {
        double rssi_corrected;
        if (rssi_new > rssi_old) {
            rssi_corrected = rssi_new;
        } else {
            rssi_corrected = rssi_old - Math.min(rssi_old - rssi_new, THRESHOLD_RSSI_AWAY);
        }
        return rssi_corrected;
    }

    public static double getAverage(final List<Double> doubleList) {
        if (doubleList == null || doubleList.size() == 0) {
            return 0;
        }
        double somme = 0;
        for (Double elem : doubleList) {
            somme += elem;
        }
        return somme / doubleList.size();
    }

    /**
     * Calculate the quadratic sum
     *
     * @param x the first axe value
     * @param y the second axe value
     * @param z the third axe value
     * @return the quadratic sum of the three axes
     */
    public static double getQuadratiqueSum(float x, float y, float z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    public static double[] point2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / (2 * MAX_X + 1));
        coord[0] = index - coord[1] * (2 * MAX_X + 1);
        return coord;
    }

    public static double[] square2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / MAX_X);
        coord[0] = index - coord[1] * MAX_X;
        coord[0] = coord[0] + 0.5;
        coord[1] = coord[1] + 0.5;
        return coord;
    }

    public static double max(double[] rssi) {
        if (rssi != null) {
            double result = rssi[0];
            for (int i = 1; i < rssi.length; i++) {
                if (rssi[i] > result) {
                    result = rssi[i];
                }
            }
            return result;
        }
        return -1;
    }

    public static synchronized Integer most(final List<Integer> list) {
        if (list.size() == 0) {
            return -1;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Map.Entry<Integer, Integer> max = null;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() >= max.getValue()) {
                max = e;
            }
        }
        return max == null ? -1 : max.getKey();
    }
}
