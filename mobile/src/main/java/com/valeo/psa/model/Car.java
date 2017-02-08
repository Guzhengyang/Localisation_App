package com.valeo.psa.model;

/**
 * This class represents a car
 *
 * @author GMAMESSI
 */
public class Car {
    private final int mImgCarId;
    private final String mRegPlate;
    private final String mBrandCar;
    private final String mVin;
    private final String mCarConfigFileId;

    /**
     * Class constructor
     *
     * @param imgCarId the car image id
     * @param regPlate the registration plate
     * @param brandCar the car brand
     * @param vin the car vin
     */
    public Car(int imgCarId, String regPlate, String brandCar, String vin, String carConfigFileId) {
        mImgCarId = imgCarId;
        mRegPlate = regPlate;
        mBrandCar = brandCar;
        mVin = vin;
        mCarConfigFileId = carConfigFileId;
    }

    /**
     * Default class constructor
     */
    public Car() {
        mImgCarId = 0;
        mRegPlate = "";
        mBrandCar = "";
        mVin = "";
        mCarConfigFileId = "";
    }

    //GETTER AND SETTERS
    public int getImgCarId() {
        return mImgCarId;
    }

    public String getRegPlate() {
        return mRegPlate;
    }

    public String getBrandCar() {
        return mBrandCar;
    }

    public String getVin() {
        return mVin;
    }

    public String getCarConfigFileId() {
        return mCarConfigFileId;
    }

    @Override
    public String toString() {
        return "Car{" +
                "mImgCarId=" + mImgCarId +
                ", mRegPlate='" + mRegPlate + '\'' +
                ", mBrandCar='" + mBrandCar + '\'' +
                ", mVin='" + mVin + '\'' +
                ", mCarConfigFileId='" + mCarConfigFileId + '\'' +
                '}';
    }
}
