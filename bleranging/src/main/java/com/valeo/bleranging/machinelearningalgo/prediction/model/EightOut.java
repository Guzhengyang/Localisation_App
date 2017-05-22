/*
  Licensed under the Apache License, Version 2.0
    http://www.apache.org/licenses/LICENSE-2.0.html

  AUTOGENERATED BY H2O at 2017-05-19T16:01:11.403+02:00
  3.10.4.2
  
  Standalone prediction code with sample test data for DeepLearningModel named EightOut

  How to download, compile and execute:
      mkdir tmpdir
      cd tmpdir
      curl http://127.0.0.1:54321/3/h2o-genmodel.jar > h2o-genmodel.jar
      curl http://127.0.0.1:54321/3/Models.java/EightOut > EightOut.java
      javac -cp h2o-genmodel.jar -J-Xmx2g -J-XX:MaxPermSize=128m EightOut.java

     (Note:  Try java argument -XX:+PrintCompilation to show runtime JIT compiler behavior.)
*/

import hex.genmodel.GenModel;
import hex.genmodel.annotations.ModelPojo;

@ModelPojo(name = "EightOut", algorithm = "deeplearning")
public class EightOut extends GenModel {
    // Workspace for categorical offsets.
    public static final int[] CATOFFSETS = {0};
    // Number of neurons for each layer.
    public static final int[] NEURONS = {8, 16, 16, 7};
    // Neuron bias values.
    public static final double[][] BIAS = new double[][]{
      /* Input */ EightOut_Bias_0.VALUES,
      /* Rectifier */ EightOut_Bias_1.VALUES,
      /* Rectifier */ EightOut_Bias_2.VALUES,
      /* Softmax */ EightOut_Bias_3.VALUES
    };
    // Connecting weights between neurons.
    public static final float[][] WEIGHT = new float[][]{
      /* Input */ EightOut_Weight_0.VALUES,
      /* Rectifier */ EightOut_Weight_1.VALUES,
      /* Rectifier */ EightOut_Weight_2.VALUES,
      /* Softmax */ EightOut_Weight_3.VALUES
    };
    // Names of columns used by model.
    public static final String[] NAMES = NamesHolder_EightOut.VALUES;
    // Number of output classes included in training data response column.
    public static final int NCLASSES = 7;
    // Column domains. The last array contains domain of response column.
    public static final String[][] DOMAINS = new String[][]{
    /* RSSI LEFT_ORIGIN */ null,
    /* RSSI MIDDLE_ORIGIN */ null,
    /* RSSI RIGHT_ORIGIN */ null,
    /* RSSI TRUNK_ORIGIN */ null,
    /* RSSI FRONTLEFT_ORIGIN */ null,
    /* RSSI FRONTRIGHT_ORIGIN */ null,
    /* RSSI REARLEFT_ORIGIN */ null,
    /* RSSI REARRIGHT_ORIGIN */ null,
    /* class */ EightOut_ColInfo_8.VALUES
    };
    // Prior class distribution
    public static final double[] PRIOR_CLASS_DISTRIB = {0.13043478260869565, 0.13043478260869565, 0.13043478260869565, 0.13043478260869565, 0.13043478260869565, 0.21739130434782608, 0.13043478260869565};
    // Class distribution used for model building
    public static final double[] MODEL_CLASS_DISTRIB = null;
    // Thread-local storage for input neuron activation values.
    final double[] NUMS = new double[8];
    // Thread-local storage for neuron activation values.
    final double[][] ACTIVATION = new double[][]{
      /* Input */ EightOut_Activation_0.VALUES,
      /* Rectifier */ EightOut_Activation_1.VALUES,
      /* Rectifier */ EightOut_Activation_2.VALUES,
      /* Softmax */ EightOut_Activation_3.VALUES
    };

    public EightOut() {
        super(NAMES, DOMAINS);
    }

    public hex.ModelCategory getModelCategory() {
        return hex.ModelCategory.Multinomial;
    }

    public boolean isSupervised() {
        return true;
    }

    public int nfeatures() {
        return 8;
    }

    public int nclasses() {
        return 7;
    }

    public String getUUID() {
        return Long.toString(-1957713571973209456L);
    }

    // Pass in data in a double[], pre-aligned to the Model's requirements.
    // Jam predictions into the preds[] array; preds[0] is reserved for the
    // main prediction (class for classifiers or value for regression),
    // and remaining columns hold a probability distribution for classifiers.
    public final double[] score0(double[] data, double[] preds) {
        java.util.Arrays.fill(preds, 0);
        java.util.Arrays.fill(NUMS, 0);
        int i = 0, ncats = 0;
        final int n = data.length;
        for (; i < n; ++i) {
            NUMS[i] = Double.isNaN(data[i]) ? 0 : (data[i] - NORMSUB.VALUES[i]) * NORMMUL.VALUES[i];
        }
        java.util.Arrays.fill(ACTIVATION[0], 0);
        for (i = 0; i < NUMS.length; ++i) {
            ACTIVATION[0][CATOFFSETS[CATOFFSETS.length - 1] + i] = Double.isNaN(NUMS[i]) ? 0 : NUMS[i];
        }
        for (i = 1; i < ACTIVATION.length; ++i) {
            java.util.Arrays.fill(ACTIVATION[i], 0);
            int cols = ACTIVATION[i - 1].length;
            int rows = ACTIVATION[i].length;
            int extra = cols - cols % 8;
            int multiple = (cols / 8) * 8 - 1;
            int idx = 0;
            float[] a = WEIGHT[i];
            double[] x = ACTIVATION[i - 1];
            double[] y = BIAS[i];
            double[] res = ACTIVATION[i];
            for (int row = 0; row < rows; ++row) {
                double psum0 = 0, psum1 = 0, psum2 = 0, psum3 = 0, psum4 = 0, psum5 = 0, psum6 = 0, psum7 = 0;
                for (int col = 0; col < multiple; col += 8) {
                    int off = idx + col;
                    psum0 += a[off] * x[col];
                    psum1 += a[off + 1] * x[col + 1];
                    psum2 += a[off + 2] * x[col + 2];
                    psum3 += a[off + 3] * x[col + 3];
                    psum4 += a[off + 4] * x[col + 4];
                    psum5 += a[off + 5] * x[col + 5];
                    psum6 += a[off + 6] * x[col + 6];
                    psum7 += a[off + 7] * x[col + 7];
                }
                res[row] += psum0 + psum1 + psum2 + psum3;
                res[row] += psum4 + psum5 + psum6 + psum7;
                for (int col = extra; col < cols; col++)
                    res[row] += a[idx + col] * x[col];
                res[row] += y[row];
                idx += cols;
            }
            if (i < ACTIVATION.length - 1) {
                for (int r = 0; r < ACTIVATION[i].length; ++r) {
                    ACTIVATION[i][r] = Math.max(0, ACTIVATION[i][r]);
                }
            }
            if (i == ACTIVATION.length - 1) {
                double max = ACTIVATION[i][0];
                for (int r = 1; r < ACTIVATION[i].length; r++) {
                    if (ACTIVATION[i][r] > max) max = ACTIVATION[i][r];
                }
                double scale = 0;
                for (int r = 0; r < ACTIVATION[i].length; r++) {
                    ACTIVATION[i][r] = Math.exp(ACTIVATION[i][r] - max);
                    scale += ACTIVATION[i][r];
                }
                for (int r = 0; r < ACTIVATION[i].length; r++) {
                    if (Double.isNaN(ACTIVATION[i][r]))
                        throw new RuntimeException("Numerical instability, predicted NaN.");
                    ACTIVATION[i][r] /= scale;
                    preds[r + 1] = ACTIVATION[i][r];
                }
            }
        }
        preds[0] = hex.genmodel.GenModel.getPrediction(preds, PRIOR_CLASS_DISTRIB, data, 0.5);
        return preds;
    }

    static class NORMMUL implements java.io.Serializable {
        public static final double[] VALUES = new double[8];

        static {
            NORMMUL_0.fill(VALUES);
        }

        static final class NORMMUL_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.11828300635573843;
                sa[1] = 0.09320306522677553;
                sa[2] = 0.11465425200238977;
                sa[3] = 0.08098846943450522;
                sa[4] = 0.14094406131640466;
                sa[5] = 0.11124914489693975;
                sa[6] = 0.11212711810043471;
                sa[7] = 0.11263967509026614;
            }
        }
    }

    static class NORMSUB implements java.io.Serializable {
        public static final double[] VALUES = new double[8];

        static {
            NORMSUB_0.fill(VALUES);
        }

        static final class NORMSUB_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = -74.74703804347826;
                sa[1] = -73.85815217391306;
                sa[2] = -77.36679347826087;
                sa[3] = -70.15714673913043;
                sa[4] = -81.25690217391305;
                sa[5] = -84.4172010869565;
                sa[6] = -78.76160326086956;
                sa[7] = -79.53660326086957;
            }
        }
    }

    // Neuron activation values for Input layer
    static class EightOut_Activation_0 implements java.io.Serializable {
        public static final double[] VALUES = new double[8];

        static {
            EightOut_Activation_0_0.fill(VALUES);
        }

        static final class EightOut_Activation_0_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
                sa[2] = 0.0;
                sa[3] = 0.0;
                sa[4] = 0.0;
                sa[5] = 0.0;
                sa[6] = 0.0;
                sa[7] = 0.0;
            }
        }
    }

    // Neuron activation values for Rectifier layer
    static class EightOut_Activation_1 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            EightOut_Activation_1_0.fill(VALUES);
        }

        static final class EightOut_Activation_1_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
                sa[2] = 0.0;
                sa[3] = 0.0;
                sa[4] = 0.0;
                sa[5] = 0.0;
                sa[6] = 0.0;
                sa[7] = 0.0;
                sa[8] = 0.0;
                sa[9] = 0.0;
                sa[10] = 0.0;
                sa[11] = 0.0;
                sa[12] = 0.0;
                sa[13] = 0.0;
                sa[14] = 0.0;
                sa[15] = 0.0;
            }
        }
    }

    // Neuron activation values for Rectifier layer
    static class EightOut_Activation_2 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            EightOut_Activation_2_0.fill(VALUES);
        }

        static final class EightOut_Activation_2_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
                sa[2] = 0.0;
                sa[3] = 0.0;
                sa[4] = 0.0;
                sa[5] = 0.0;
                sa[6] = 0.0;
                sa[7] = 0.0;
                sa[8] = 0.0;
                sa[9] = 0.0;
                sa[10] = 0.0;
                sa[11] = 0.0;
                sa[12] = 0.0;
                sa[13] = 0.0;
                sa[14] = 0.0;
                sa[15] = 0.0;
            }
        }
    }

    // Neuron activation values for Softmax layer
    static class EightOut_Activation_3 implements java.io.Serializable {
        public static final double[] VALUES = new double[7];

        static {
            EightOut_Activation_3_0.fill(VALUES);
        }

        static final class EightOut_Activation_3_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
                sa[2] = 0.0;
                sa[3] = 0.0;
                sa[4] = 0.0;
                sa[5] = 0.0;
                sa[6] = 0.0;
            }
        }
    }

    // Neuron bias values for Input layer
    static class EightOut_Bias_0 implements java.io.Serializable {
        public static final double[] VALUES = null;
    }

    // Neuron bias values for Rectifier layer
    static class EightOut_Bias_1 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            EightOut_Bias_1_0.fill(VALUES);
        }

        static final class EightOut_Bias_1_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.318421636324525;
                sa[1] = 0.5056611059072111;
                sa[2] = 0.7323979324848857;
                sa[3] = 0.49049928352248867;
                sa[4] = 0.9519068570792774;
                sa[5] = 0.17794183099903113;
                sa[6] = 1.0329715991987773;
                sa[7] = 0.19231704745064382;
                sa[8] = 0.7263799737028659;
                sa[9] = 0.6685610144049237;
                sa[10] = 1.3642194402942263;
                sa[11] = 0.1783069185557576;
                sa[12] = 1.3600368681468502;
                sa[13] = 0.2995796194211711;
                sa[14] = 0.4822312085275441;
                sa[15] = 0.18909336666245824;
            }
        }
    }

    // Neuron bias values for Rectifier layer
    static class EightOut_Bias_2 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            EightOut_Bias_2_0.fill(VALUES);
        }

        static final class EightOut_Bias_2_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.8465299880798324;
                sa[1] = 1.8961923905073361;
                sa[2] = 1.2447230394243085;
                sa[3] = 0.9934671313561977;
                sa[4] = 1.1212294539385421;
                sa[5] = 0.6191081897996604;
                sa[6] = 1.1928779644619696;
                sa[7] = 1.4722039650517997;
                sa[8] = 1.9789739699156474;
                sa[9] = 0.9875176860978746;
                sa[10] = 0.9803862249714872;
                sa[11] = 0.8896962088060794;
                sa[12] = 0.505999137835365;
                sa[13] = 0.7818614524947853;
                sa[14] = 0.8725502225133278;
                sa[15] = 0.915486645787827;
            }
        }
    }

    // Neuron bias values for Softmax layer
    static class EightOut_Bias_3 implements java.io.Serializable {
        public static final double[] VALUES = new double[7];

        static {
            EightOut_Bias_3_0.fill(VALUES);
        }

        static final class EightOut_Bias_3_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.3200004141700644;
                sa[1] = 0.16496623330286897;
                sa[2] = 0.010426941760952692;
                sa[3] = 0.141682459575176;
                sa[4] = -0.16099614832536924;
                sa[5] = 0.2573445419768766;
                sa[6] = 0.04227842771459307;
            }
        }
    }

    static class EightOut_Weight_0 implements java.io.Serializable {
        public static final float[] VALUES = null;
    }

    // Neuron weights connecting Input and Rectifier layer
    static class EightOut_Weight_1 implements java.io.Serializable {
        public static final float[] VALUES = new float[128];

        static {
            EightOut_Weight_1_0.fill(VALUES);
        }

        static final class EightOut_Weight_1_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -0.09730357f;
                sa[1] = -0.01864925f;
                sa[2] = -0.3741248f;
                sa[3] = 1.0286732f;
                sa[4] = -0.17956275f;
                sa[5] = 0.098006755f;
                sa[6] = -0.08822017f;
                sa[7] = 0.20563006f;
                sa[8] = 0.45736805f;
                sa[9] = -0.07070861f;
                sa[10] = 0.4589557f;
                sa[11] = -0.41560307f;
                sa[12] = -0.58538496f;
                sa[13] = 0.34094065f;
                sa[14] = 0.04788742f;
                sa[15] = -0.6160425f;
                sa[16] = 0.15953127f;
                sa[17] = -0.11415155f;
                sa[18] = -0.35116294f;
                sa[19] = -0.08294909f;
                sa[20] = -0.15475222f;
                sa[21] = 0.8509197f;
                sa[22] = 0.5431624f;
                sa[23] = 0.40081623f;
                sa[24] = 0.13427451f;
                sa[25] = 0.714369f;
                sa[26] = -0.2632818f;
                sa[27] = 0.28963804f;
                sa[28] = -0.104095496f;
                sa[29] = 0.12919345f;
                sa[30] = -0.37401316f;
                sa[31] = -0.32997003f;
                sa[32] = -0.27420205f;
                sa[33] = -0.4779479f;
                sa[34] = -0.24265106f;
                sa[35] = 0.5267465f;
                sa[36] = 0.24864076f;
                sa[37] = -0.33669695f;
                sa[38] = 0.09539717f;
                sa[39] = -0.1004915f;
                sa[40] = -0.04783411f;
                sa[41] = -0.57149786f;
                sa[42] = 0.16639483f;
                sa[43] = -0.8947877f;
                sa[44] = -0.6419048f;
                sa[45] = -0.5769947f;
                sa[46] = -0.6289348f;
                sa[47] = 0.17127354f;
                sa[48] = 0.38843867f;
                sa[49] = -0.08439226f;
                sa[50] = -1.0280768f;
                sa[51] = 0.18752016f;
                sa[52] = 0.12798063f;
                sa[53] = -0.8701772f;
                sa[54] = 0.8278778f;
                sa[55] = 0.9030868f;
                sa[56] = -0.43402544f;
                sa[57] = 0.04222313f;
                sa[58] = 1.0944165f;
                sa[59] = -0.065263115f;
                sa[60] = -0.27432f;
                sa[61] = 0.1640658f;
                sa[62] = -0.13779388f;
                sa[63] = -0.28704318f;
                sa[64] = 0.19643965f;
                sa[65] = -0.2799629f;
                sa[66] = 0.580347f;
                sa[67] = -0.30474147f;
                sa[68] = -0.096099265f;
                sa[69] = -0.7649777f;
                sa[70] = 0.08175898f;
                sa[71] = -0.11075613f;
                sa[72] = 0.6719023f;
                sa[73] = 0.21206032f;
                sa[74] = 0.24924828f;
                sa[75] = -0.06887483f;
                sa[76] = 0.8367867f;
                sa[77] = -0.062225834f;
                sa[78] = 0.22627549f;
                sa[79] = 0.19910681f;
                sa[80] = 0.2094059f;
                sa[81] = 0.0013204749f;
                sa[82] = -0.18361683f;
                sa[83] = 0.09228685f;
                sa[84] = -0.15897976f;
                sa[85] = -0.9567753f;
                sa[86] = -0.84179616f;
                sa[87] = -0.5092641f;
                sa[88] = -0.42734885f;
                sa[89] = 0.6327522f;
                sa[90] = 0.2965445f;
                sa[91] = -0.11886708f;
                sa[92] = 0.08028061f;
                sa[93] = 0.46541277f;
                sa[94] = 1.3519536f;
                sa[95] = 0.0075436924f;
                sa[96] = -0.5890211f;
                sa[97] = 0.37473986f;
                sa[98] = 0.14839028f;
                sa[99] = 0.08301961f;
                sa[100] = 0.054324396f;
                sa[101] = -0.07429645f;
                sa[102] = -0.24909075f;
                sa[103] = 0.6777372f;
                sa[104] = 0.2695382f;
                sa[105] = -0.39048836f;
                sa[106] = 0.0850448f;
                sa[107] = 0.42142132f;
                sa[108] = 0.14124073f;
                sa[109] = -0.20234866f;
                sa[110] = -0.3662007f;
                sa[111] = 0.7153693f;
                sa[112] = -0.14127691f;
                sa[113] = 0.47831494f;
                sa[114] = 0.32678714f;
                sa[115] = -0.106851965f;
                sa[116] = -0.058069054f;
                sa[117] = 0.63480866f;
                sa[118] = -0.35818905f;
                sa[119] = -0.5119072f;
                sa[120] = -0.9544354f;
                sa[121] = -0.21868591f;
                sa[122] = -0.078260645f;
                sa[123] = -0.17347185f;
                sa[124] = -0.30803007f;
                sa[125] = 0.35255805f;
                sa[126] = -0.14983936f;
                sa[127] = 0.32134318f;
            }
        }
    }

    // Neuron weights connecting Rectifier and Rectifier layer
    static class EightOut_Weight_2 implements java.io.Serializable {
        public static final float[] VALUES = new float[256];

        static {
            EightOut_Weight_2_0.fill(VALUES);
        }

        static final class EightOut_Weight_2_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -0.6397813f;
                sa[1] = 0.079917006f;
                sa[2] = 0.292223f;
                sa[3] = 0.58923256f;
                sa[4] = 0.8598942f;
                sa[5] = 0.5902035f;
                sa[6] = 0.12174282f;
                sa[7] = 0.8319628f;
                sa[8] = -0.64155626f;
                sa[9] = 0.39859512f;
                sa[10] = -0.50322133f;
                sa[11] = -0.3922164f;
                sa[12] = 0.3604646f;
                sa[13] = 0.107336506f;
                sa[14] = 0.19053045f;
                sa[15] = -0.5393792f;
                sa[16] = -0.91119444f;
                sa[17] = -0.7071464f;
                sa[18] = 0.31350976f;
                sa[19] = -0.34562463f;
                sa[20] = -0.11729788f;
                sa[21] = -0.472147f;
                sa[22] = 0.37813604f;
                sa[23] = 0.5166021f;
                sa[24] = 0.2547412f;
                sa[25] = -0.7180827f;
                sa[26] = 1.325111f;
                sa[27] = -0.19473518f;
                sa[28] = 0.6922284f;
                sa[29] = -0.011500322f;
                sa[30] = -0.29651055f;
                sa[31] = -0.4882697f;
                sa[32] = 0.19111852f;
                sa[33] = -0.63658565f;
                sa[34] = 0.46821055f;
                sa[35] = -0.28068444f;
                sa[36] = -0.37156737f;
                sa[37] = 0.23798919f;
                sa[38] = 0.009037837f;
                sa[39] = -0.4889037f;
                sa[40] = -0.21684423f;
                sa[41] = -1.1276537f;
                sa[42] = 0.05170673f;
                sa[43] = 0.36864412f;
                sa[44] = 0.31194928f;
                sa[45] = 0.23374443f;
                sa[46] = -0.67901117f;
                sa[47] = -0.030172233f;
                sa[48] = 0.7782626f;
                sa[49] = 0.6766064f;
                sa[50] = 0.33714715f;
                sa[51] = 1.8626935f;
                sa[52] = -0.31332517f;
                sa[53] = -0.30040792f;
                sa[54] = 0.051504694f;
                sa[55] = 0.8341136f;
                sa[56] = 1.4143806f;
                sa[57] = 0.85067236f;
                sa[58] = 0.77276164f;
                sa[59] = -0.2533654f;
                sa[60] = 0.7901989f;
                sa[61] = 1.2005241f;
                sa[62] = 0.75601673f;
                sa[63] = -0.3172861f;
                sa[64] = -0.063946284f;
                sa[65] = 0.80849624f;
                sa[66] = -0.3876299f;
                sa[67] = -0.06427924f;
                sa[68] = -0.09153101f;
                sa[69] = 0.3145474f;
                sa[70] = -0.030881371f;
                sa[71] = -0.23425822f;
                sa[72] = -0.21677352f;
                sa[73] = 0.6504692f;
                sa[74] = 0.33854985f;
                sa[75] = -0.31968284f;
                sa[76] = 0.41399774f;
                sa[77] = -0.49611658f;
                sa[78] = -0.11700996f;
                sa[79] = -0.16332167f;
                sa[80] = -0.7184572f;
                sa[81] = 0.04365517f;
                sa[82] = 0.79682434f;
                sa[83] = -0.31722304f;
                sa[84] = -0.35065073f;
                sa[85] = -0.33946025f;
                sa[86] = -0.88601565f;
                sa[87] = -0.10742811f;
                sa[88] = -0.012507124f;
                sa[89] = -0.09428132f;
                sa[90] = 0.22072677f;
                sa[91] = -0.44818822f;
                sa[92] = 0.5606788f;
                sa[93] = 0.45130765f;
                sa[94] = 0.67150444f;
                sa[95] = 0.3419758f;
                sa[96] = 0.21998927f;
                sa[97] = -0.27739924f;
                sa[98] = 0.34051773f;
                sa[99] = 0.6605815f;
                sa[100] = -0.45485583f;
                sa[101] = 0.17517594f;
                sa[102] = -0.86189646f;
                sa[103] = 0.22129828f;
                sa[104] = -0.5972468f;
                sa[105] = 0.11587149f;
                sa[106] = 0.3583948f;
                sa[107] = 0.41156375f;
                sa[108] = 0.2412875f;
                sa[109] = 0.41561309f;
                sa[110] = -0.56785214f;
                sa[111] = -0.18049209f;
                sa[112] = 0.669963f;
                sa[113] = -0.18691538f;
                sa[114] = -0.107166916f;
                sa[115] = 0.33381185f;
                sa[116] = 0.17103401f;
                sa[117] = -0.08187051f;
                sa[118] = 0.195757f;
                sa[119] = -0.2127836f;
                sa[120] = -0.30098447f;
                sa[121] = -0.60562694f;
                sa[122] = -0.002086736f;
                sa[123] = 1.1381333f;
                sa[124] = -0.16878276f;
                sa[125] = -0.16698867f;
                sa[126] = 0.18269248f;
                sa[127] = -0.27589944f;
                sa[128] = -0.28636056f;
                sa[129] = 0.37791485f;
                sa[130] = 0.44598052f;
                sa[131] = -0.39215034f;
                sa[132] = 0.96177673f;
                sa[133] = 0.49038804f;
                sa[134] = 0.41642508f;
                sa[135] = -0.3329473f;
                sa[136] = -0.4597373f;
                sa[137] = 0.21244277f;
                sa[138] = -0.021174412f;
                sa[139] = -0.3312231f;
                sa[140] = 0.32707545f;
                sa[141] = -0.29862836f;
                sa[142] = 0.16333216f;
                sa[143] = 0.2850509f;
                sa[144] = 0.21550484f;
                sa[145] = 0.09028263f;
                sa[146] = 0.0018873062f;
                sa[147] = -1.273454f;
                sa[148] = -0.0062064985f;
                sa[149] = 0.02081763f;
                sa[150] = 0.08653826f;
                sa[151] = 0.61266303f;
                sa[152] = 0.10754754f;
                sa[153] = -0.011798672f;
                sa[154] = -0.21520536f;
                sa[155] = 0.40270022f;
                sa[156] = 0.055609025f;
                sa[157] = 0.4921919f;
                sa[158] = 0.30045602f;
                sa[159] = 0.17611378f;
                sa[160] = -0.7053144f;
                sa[161] = -0.09833509f;
                sa[162] = 0.48602903f;
                sa[163] = 0.76711637f;
                sa[164] = 0.4071804f;
                sa[165] = -0.013358148f;
                sa[166] = -0.1791927f;
                sa[167] = 0.15991542f;
                sa[168] = -0.23723623f;
                sa[169] = 0.4266747f;
                sa[170] = 0.25052235f;
                sa[171] = 0.58352244f;
                sa[172] = 0.0426065f;
                sa[173] = -0.31817713f;
                sa[174] = -0.5928399f;
                sa[175] = -0.4933729f;
                sa[176] = 1.1002278f;
                sa[177] = 0.42368418f;
                sa[178] = -0.9527058f;
                sa[179] = 0.6735039f;
                sa[180] = -0.6811578f;
                sa[181] = 0.1559416f;
                sa[182] = 0.70491886f;
                sa[183] = 0.28158683f;
                sa[184] = 0.7610333f;
                sa[185] = 0.3249633f;
                sa[186] = -0.04672799f;
                sa[187] = 0.23787874f;
                sa[188] = -0.22426121f;
                sa[189] = 0.21645448f;
                sa[190] = -1.0238959f;
                sa[191] = -0.030566925f;
                sa[192] = -1.1046344f;
                sa[193] = 0.08025417f;
                sa[194] = 0.03999105f;
                sa[195] = -0.07232065f;
                sa[196] = 0.32451072f;
                sa[197] = 0.3774471f;
                sa[198] = 0.01913257f;
                sa[199] = -0.1202914f;
                sa[200] = 0.081508294f;
                sa[201] = 0.04269282f;
                sa[202] = -0.024552828f;
                sa[203] = -0.3576968f;
                sa[204] = -0.32833105f;
                sa[205] = -0.40592456f;
                sa[206] = 0.91943836f;
                sa[207] = 0.9429066f;
                sa[208] = -0.38734305f;
                sa[209] = 0.03553307f;
                sa[210] = -0.75658107f;
                sa[211] = -1.0499525f;
                sa[212] = -0.123617396f;
                sa[213] = 0.1649294f;
                sa[214] = -0.2101467f;
                sa[215] = -0.39408377f;
                sa[216] = -0.13896196f;
                sa[217] = 0.47903368f;
                sa[218] = 0.36562452f;
                sa[219] = -0.3055791f;
                sa[220] = -1.8323964f;
                sa[221] = -1.5415313f;
                sa[222] = -0.7833739f;
                sa[223] = 0.8415319f;
                sa[224] = -1.159115f;
                sa[225] = 0.37087816f;
                sa[226] = -0.26111266f;
                sa[227] = -0.59697264f;
                sa[228] = 0.31028885f;
                sa[229] = 0.34856904f;
                sa[230] = -0.057367176f;
                sa[231] = 0.20324859f;
                sa[232] = -0.6610623f;
                sa[233] = 0.7831022f;
                sa[234] = 0.19583462f;
                sa[235] = -0.95179045f;
                sa[236] = 0.16648374f;
                sa[237] = -0.2936784f;
                sa[238] = 0.555908f;
                sa[239] = -0.1406799f;
                sa[240] = 1.102431f;
                sa[241] = 0.7702934f;
                sa[242] = 0.03054596f;
                sa[243] = 0.9829194f;
                sa[244] = -0.542763f;
                sa[245] = 0.46774316f;
                sa[246] = 0.42905617f;
                sa[247] = 0.8266513f;
                sa[248] = -0.228967f;
                sa[249] = -0.07470266f;
                sa[250] = 0.2839442f;
                sa[251] = 0.42559004f;
                sa[252] = 0.17765999f;
                sa[253] = 0.5809779f;
                sa[254] = -0.34821275f;
                sa[255] = -0.22121815f;
            }
        }
    }

    // Neuron weights connecting Rectifier and Softmax layer
    static class EightOut_Weight_3 implements java.io.Serializable {
        public static final float[] VALUES = new float[112];

        static {
            EightOut_Weight_3_0.fill(VALUES);
        }

        static final class EightOut_Weight_3_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -1.6355622f;
                sa[1] = 2.581744f;
                sa[2] = 2.3885071f;
                sa[3] = 1.1956325f;
                sa[4] = -1.1180191f;
                sa[5] = -1.8541952f;
                sa[6] = -1.2513319f;
                sa[7] = 2.4210513f;
                sa[8] = 0.008584702f;
                sa[9] = -0.56702507f;
                sa[10] = -0.17675996f;
                sa[11] = -1.5005704f;
                sa[12] = -1.3305916f;
                sa[13] = -1.2129453f;
                sa[14] = -5.8186526f;
                sa[15] = -1.2141435f;
                sa[16] = 0.37328836f;
                sa[17] = -1.14522f;
                sa[18] = -2.0502272f;
                sa[19] = 0.54469734f;
                sa[20] = -0.39947858f;
                sa[21] = 0.5145217f;
                sa[22] = -0.94165707f;
                sa[23] = -0.4612756f;
                sa[24] = 1.3239934f;
                sa[25] = -1.2082846f;
                sa[26] = 2.2749963f;
                sa[27] = -4.26141f;
                sa[28] = 1.7194463f;
                sa[29] = -0.8823842f;
                sa[30] = 2.198381f;
                sa[31] = -2.8212562f;
                sa[32] = -2.577771f;
                sa[33] = -0.46339497f;
                sa[34] = -7.284787f;
                sa[35] = 0.56723446f;
                sa[36] = 0.6105781f;
                sa[37] = -4.102188f;
                sa[38] = -3.3311536f;
                sa[39] = -1.071547f;
                sa[40] = 1.8970063f;
                sa[41] = -2.0233686f;
                sa[42] = 1.4235013f;
                sa[43] = 0.67980886f;
                sa[44] = 1.7541515f;
                sa[45] = -2.159888f;
                sa[46] = -0.4012392f;
                sa[47] = 0.21029723f;
                sa[48] = 0.27372825f;
                sa[49] = 0.9130002f;
                sa[50] = -2.3535764f;
                sa[51] = -1.6654434f;
                sa[52] = 1.0655811f;
                sa[53] = 0.69475895f;
                sa[54] = 0.038978636f;
                sa[55] = 0.30899918f;
                sa[56] = 2.042636f;
                sa[57] = 1.5750389f;
                sa[58] = -1.1480938f;
                sa[59] = -1.043554f;
                sa[60] = 0.51315814f;
                sa[61] = 3.4537098f;
                sa[62] = 1.3500121f;
                sa[63] = 0.14841011f;
                sa[64] = -1.0680237f;
                sa[65] = -0.15992817f;
                sa[66] = 1.2025733f;
                sa[67] = 1.8283755f;
                sa[68] = -0.09031117f;
                sa[69] = 1.7541219f;
                sa[70] = 0.28538206f;
                sa[71] = -2.414643f;
                sa[72] = -2.2509108f;
                sa[73] = 2.1244876f;
                sa[74] = -3.8796437f;
                sa[75] = -4.917043f;
                sa[76] = 1.253518f;
                sa[77] = 1.6357516f;
                sa[78] = 0.30868143f;
                sa[79] = 0.61847144f;
                sa[80] = 3.2355824f;
                sa[81] = -0.65859056f;
                sa[82] = -2.939598f;
                sa[83] = 0.1536855f;
                sa[84] = 1.110369f;
                sa[85] = -1.7579979f;
                sa[86] = 2.0484478f;
                sa[87] = -0.23888682f;
                sa[88] = -1.8778274f;
                sa[89] = -1.5322201f;
                sa[90] = 2.5519555f;
                sa[91] = -0.09429241f;
                sa[92] = -1.5570751f;
                sa[93] = -2.706348f;
                sa[94] = -1.4097514f;
                sa[95] = 1.7084486f;
                sa[96] = -2.2254226f;
                sa[97] = -1.9901267f;
                sa[98] = -0.77001053f;
                sa[99] = 0.21566382f;
                sa[100] = 0.13801496f;
                sa[101] = -1.6869571f;
                sa[102] = 0.2486186f;
                sa[103] = 2.7012105f;
                sa[104] = -2.557333f;
                sa[105] = 2.6340659f;
                sa[106] = -1.9799879f;
                sa[107] = 2.0691016f;
                sa[108] = -3.810001f;
                sa[109] = 1.4748061f;
                sa[110] = -1.8481412f;
                sa[111] = 1.9053981f;
            }
        }
    }

    // The class representing training column names
    static class NamesHolder_EightOut implements java.io.Serializable {
        public static final String[] VALUES = new String[8];

        static {
            NamesHolder_EightOut_0.fill(VALUES);
        }

        static final class NamesHolder_EightOut_0 implements java.io.Serializable {
            static final void fill(String[] sa) {
                sa[0] = "RSSI LEFT_ORIGIN";
                sa[1] = "RSSI MIDDLE_ORIGIN";
                sa[2] = "RSSI RIGHT_ORIGIN";
                sa[3] = "RSSI TRUNK_ORIGIN";
                sa[4] = "RSSI FRONTLEFT_ORIGIN";
                sa[5] = "RSSI FRONTRIGHT_ORIGIN";
                sa[6] = "RSSI REARLEFT_ORIGIN";
                sa[7] = "RSSI REARRIGHT_ORIGIN";
            }
        }
    }

    // The class representing column class
    static class EightOut_ColInfo_8 implements java.io.Serializable {
        public static final String[] VALUES = new String[7];

        static {
            EightOut_ColInfo_8_0.fill(VALUES);
        }

        static final class EightOut_ColInfo_8_0 implements java.io.Serializable {
            static final void fill(String[] sa) {
                sa[0] = "back";
                sa[1] = "front";
                sa[2] = "left";
                sa[3] = "lock";
                sa[4] = "right";
                sa[5] = "start";
                sa[6] = "trunk";
            }
        }
    }
}

