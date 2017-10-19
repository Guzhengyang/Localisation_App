package com.valeo.bleranging.managers;

import java.util.HashMap;

import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_BACK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FRONT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LEFT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RIGHT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_FR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_START_RR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_TRUNK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;

/**
 * Truth table used by the CommandManager, when the user chooses to send the data from both algorithms.
 * Simply put, an algorithm will give us the zone where the user is supposed to be and sent it to the car so it can react accordingly.
 * When we send only one of them, there is no problem, but with two, we need to decide how to react when then do not give the same zone.
 *
 * We have two options available: Random Forest algorithm OR Neural Network algorithm, and RF AND NN.
 */
public class TruthTable {
    // Used to break ties when they give two different results
    // The one with the higher value is the "most reliable". If both have the same, then it'll be more generic
    private final int FOREST_VALUE = 2;
    private final int NEURAL_NETWORK_VALUE = 1;

    // As far as I know, the whole zone thing can give us two main zones, the first being a lot less important than the second: far or near.
    // If it's far, we don't really mind where it is. As such, the truth table is simple. AND -> far + near = far, while OR -> far + near = near, for example
    // It turns a lot more complex when both of them are near, because then, we'll display the location on the phone. And if we have two completely different zones...

    // It's also quite hard for the OR table, since it's then a question of what has priority. If one says far and the other near, do we return far for security, or near for convenience?
    // To deal with it, we're going to give values to the "main cases" (near/far/etc.).

    // The value of the "main cases" to decide witch one take priority
    // For example, if we have Forest that says "near" and NN "far", if "far" has a stronger value for security purpose, we prioritize "far"

    /**
     * The constructor, since we're not going to need that much functions here.
     * @param typeOfTruth 1 = AND, 2 = OR
     * @param forestPrediction The prediction of the Forest algorithm
     * @param neuralNetworkPrediction The prediction of the Neural Network algorithm
     */
    public String truthTable(int typeOfTruth, String forestPrediction, String neuralNetworkPrediction) {
        HashMap predictionValue = new HashMap();
        predictionValue.put(PREDICTION_INSIDE, 0);
        predictionValue.put(PREDICTION_OUTSIDE, 1);
        predictionValue.put(PREDICTION_EXTERNAL, 3);
        predictionValue.put(PREDICTION_INTERNAL, 2);
        predictionValue.put(PREDICTION_ACCESS, 2);
        predictionValue.put(PREDICTION_UNKNOWN, 0);

        // We have only two options: AND and OR
        // The AND is actually very simple. Both must say the same thing for it to count
        // One big question is actually "what do we return when they are different?"

        // The first step is to find the "category" of the prediction
        String categoryFirstPrediction = findCategoryOfPrediction(forestPrediction);
        String categorySecondPrediction = findCategoryOfPrediction(neuralNetworkPrediction);

        // AND case
        if(typeOfTruth == 1) {
            if(forestPrediction.compareTo(neuralNetworkPrediction) == 0)
                return forestPrediction;
            else {
                // if they are not the same, we'll at least see if they are in the same category
                if(categoryFirstPrediction.compareTo(categorySecondPrediction) == 0) {
                    // If that's the case, we return at least the category
                    return categoryFirstPrediction;
                } else {
                    // For now, we return "nothing", but...
                    return PREDICTION_UNKNOWN;
                }
            }
        } else if(typeOfTruth == 2) {
            // OR case
            // It's a lot harder for this one, since we need to know what we're going to answer to most of the case
            // For example, we have a near and a far. Which one counts?
            // That's the whole point of the values, as the will prioritize some of the results
            // But first, we're obviously going to check if they are the same
            if(forestPrediction.compareTo(neuralNetworkPrediction) == 0)
                return forestPrediction;
            else {
                // It's not the same, so we're going to see their categories
                if(categoryFirstPrediction.compareTo(categorySecondPrediction) == 0) {
                    // Same category, but not the same prediction
                    // As such, we can either give the prediction from the most reliable source
                    if(FOREST_VALUE > NEURAL_NETWORK_VALUE)
                        return forestPrediction;
                    else if(FOREST_VALUE < NEURAL_NETWORK_VALUE)
                        return neuralNetworkPrediction;
                    else // If they have the same value, we cannot chose, so we sent the category instead
                        return categoryFirstPrediction;
                } else {
                    // They are not from the same category
                    // We don't have much options there. We can give the prediction or the category back
                    // Since we are with two different categories, though, the accuracy might no be that great
                    // The category seems like a safe bet?
                    // As such, we are going to send back the category of the most reliable one
                    if(FOREST_VALUE > NEURAL_NETWORK_VALUE)
                        return categoryFirstPrediction;
                    else if(FOREST_VALUE < NEURAL_NETWORK_VALUE)
                        return categorySecondPrediction;
                    else // They both have the same value, so... we return nothing
                        return PREDICTION_UNKNOWN;
                }
            }
        }

        return PREDICTION_UNKNOWN;
    }

    /**
     * Take the prediction and find which category it's supposed to be part of.
     * @param prediction The prediction we're analysing.
     * @return The category
     */
    private String findCategoryOfPrediction(String prediction) {
        // Note that the prediction can actually be the category itself, which makes things easier
        // If not, each category seems to have a few predictions linked to them
        switch(prediction) {
            case PREDICTION_LOCK:
            case PREDICTION_EXTERNAL:
                return PREDICTION_EXTERNAL;
            case PREDICTION_INTERNAL:
            case PREDICTION_START:
            case PREDICTION_START_FL:
            case PREDICTION_START_FR:
            case PREDICTION_START_RL:
            case PREDICTION_START_RR:
            case PREDICTION_TRUNK:
                return PREDICTION_INTERNAL;
            case PREDICTION_ACCESS:
            case PREDICTION_BACK:
            case PREDICTION_RIGHT:
            case PREDICTION_LEFT:
            case PREDICTION_FRONT:
                return PREDICTION_ACCESS;
        }

        return PREDICTION_UNKNOWN;
    }
}
