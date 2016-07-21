package com.valeo.psa.model;

/**
 * This class represents a car
 *
 * @author GMAMESSI
 */
public class Car {
    private int mImgCarId;
    private String mRegPlate;
    private String mBrandCar;
    private String mVin;

    /**
     * Class constructor
     *
     * @param imgCarId
     * @param regPlate
     * @param brandCar
     * @param vin
     */
    public Car(int imgCarId, String regPlate, String brandCar, String vin) {
        mImgCarId = imgCarId;
        mRegPlate = regPlate;
        mBrandCar = brandCar;
        mVin = vin;
    }

    /**
     * Default class constructor
     */
    public Car() {
        mImgCarId = 0;
        mRegPlate = "";
        mBrandCar = "";
        mVin = "";
    }

    //GETTER AND SETTERS
    public int getImgCarId() {
        return mImgCarId;
    }

    public void setImgCarId(int imgCarId) {
        mImgCarId = imgCarId;
    }

    public String getRegPlate() {
        return mRegPlate;
    }

    public void setRegPlate(String regPlate) {
        mRegPlate = regPlate;
    }

    public String getBrandCar() {
        return mBrandCar;
    }

    public void setBrandCar(String brandCar) {
        mBrandCar = brandCar;
    }

    public String getVin() {
        return mVin;
    }

    public void setVin(String vin) {
        mVin = vin;
    }

    @Override
    public String toString() {
        return "Car{" +
                "mImgCarId=" + mImgCarId +
                ", mRegPlate='" + mRegPlate + '\'' +
                ", mBrandCar='" + mBrandCar + '\'' +
                ", mVin='" + mVin + '\'' +
                '}';
    }
}
