package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.R;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

/**
 * Created by Zhengyang on 29/08/2016.
 */
public class Ranging {
    private Context mContext;
    private Instances testSet;
    private Instance instance;
    private RandomForest rf;
    private String[] classValues;

    public Ranging(Context context) {
        try {
            mContext = context;
            rf = (RandomForest) SerializationHelper.read(mContext.getResources().openRawResource(R.raw.rf));
            classValues = (String[]) SerializationHelper.read(mContext.getResources().openRawResource(R.raw.classvalues));
            testSet = ConverterUtils.DataSource.read(mContext.getResources().openRawResource(R.raw.test));
        } catch (Exception e) {
            e.printStackTrace();
        }
        testSet.setClassIndex(testSet.numAttributes() - 1);
        instance = testSet.instance(0);
    }

    public void setLeft(double left) {
        this.instance.setValue(0, left);
    }

    public void setMiddle(double middle) {
        this.instance.setValue(1, middle);
    }

    public void setRight(double right) {
        this.instance.setValue(2, right);
    }

    public void setBack(double back) {
        this.instance.setValue(3, back);
    }

    public void setPocket(double pocket) {
        this.instance.setValue(4, pocket);
    }

    public int predict2int() {
        try {
            return (int) rf.classifyInstance(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String predict2str() {
        try {
            return classValues[(int) rf.classifyInstance(instance)];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }
}

