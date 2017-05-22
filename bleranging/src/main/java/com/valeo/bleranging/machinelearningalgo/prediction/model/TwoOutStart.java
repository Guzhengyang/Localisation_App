/*
  Licensed under the Apache License, Version 2.0
    http://www.apache.org/licenses/LICENSE-2.0.html

  AUTOGENERATED BY H2O at 2017-05-19T16:04:39.477+02:00
  3.10.4.2
  
  Standalone prediction code with sample test data for DeepLearningModel named TwoOutStart

  How to download, compile and execute:
      mkdir tmpdir
      cd tmpdir
      curl http://127.0.0.1:54321/3/h2o-genmodel.jar > h2o-genmodel.jar
      curl http://127.0.0.1:54321/3/Models.java/TwoOutStart > TwoOutStart.java
      javac -cp h2o-genmodel.jar -J-Xmx2g -J-XX:MaxPermSize=128m TwoOutStart.java

     (Note:  Try java argument -XX:+PrintCompilation to show runtime JIT compiler behavior.)
*/

import hex.genmodel.GenModel;
import hex.genmodel.annotations.ModelPojo;

@ModelPojo(name = "TwoOutStart", algorithm = "deeplearning")
public class TwoOutStart extends GenModel {
    // Workspace for categorical offsets.
    public static final int[] CATOFFSETS = {0};
    // Number of neurons for each layer.
    public static final int[] NEURONS = {2, 16, 16, 2};
    // Neuron bias values.
    public static final double[][] BIAS = new double[][]{
      /* Input */ TwoOutStart_Bias_0.VALUES,
      /* Rectifier */ TwoOutStart_Bias_1.VALUES,
      /* Rectifier */ TwoOutStart_Bias_2.VALUES,
      /* Softmax */ TwoOutStart_Bias_3.VALUES
    };
    // Connecting weights between neurons.
    public static final float[][] WEIGHT = new float[][]{
      /* Input */ TwoOutStart_Weight_0.VALUES,
      /* Rectifier */ TwoOutStart_Weight_1.VALUES,
      /* Rectifier */ TwoOutStart_Weight_2.VALUES,
      /* Softmax */ TwoOutStart_Weight_3.VALUES
    };
    // Names of columns used by model.
    public static final String[] NAMES = NamesHolder_TwoOutStart.VALUES;
    // Number of output classes included in training data response column.
    public static final int NCLASSES = 2;
    // Column domains. The last array contains domain of response column.
    public static final String[][] DOMAINS = new String[][]{
    /* RSSI MIDDLE_ORIGIN */ null,
    /* RSSI TRUNK_ORIGIN */ null,
    /* class */ TwoOutStart_ColInfo_2.VALUES
    };
    // Prior class distribution
    public static final double[] PRIOR_CLASS_DISTRIB = {0.42857142857142855, 0.5714285714285714};
    // Class distribution used for model building
    public static final double[] MODEL_CLASS_DISTRIB = null;
    // Thread-local storage for input neuron activation values.
    final double[] NUMS = new double[2];
    // Thread-local storage for neuron activation values.
    final double[][] ACTIVATION = new double[][]{
      /* Input */ TwoOutStart_Activation_0.VALUES,
      /* Rectifier */ TwoOutStart_Activation_1.VALUES,
      /* Rectifier */ TwoOutStart_Activation_2.VALUES,
      /* Softmax */ TwoOutStart_Activation_3.VALUES
    };

    public TwoOutStart() {
        super(NAMES, DOMAINS);
    }

    public hex.ModelCategory getModelCategory() {
        return hex.ModelCategory.Binomial;
    }

    public boolean isSupervised() {
        return true;
    }

    public int nfeatures() {
        return 2;
    }

    public int nclasses() {
        return 2;
    }

    public String getUUID() {
        return Long.toString(-4571302123387763920L);
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
        preds[0] = hex.genmodel.GenModel.getPrediction(preds, PRIOR_CLASS_DISTRIB, data, 0.4969116603429489);
        return preds;
    }

    static class NORMMUL implements java.io.Serializable {
        public static final double[] VALUES = new double[2];

        static {
            NORMMUL_0.fill(VALUES);
        }

        static final class NORMMUL_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.09904260977364068;
                sa[1] = 0.07497896500830031;
            }
        }
    }

    static class NORMSUB implements java.io.Serializable {
        public static final double[] VALUES = new double[2];

        static {
            NORMSUB_0.fill(VALUES);
        }

        static final class NORMSUB_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = -71.80825892857143;
                sa[1] = -67.10129464285714;
            }
        }
    }

    // Neuron activation values for Input layer
    static class TwoOutStart_Activation_0 implements java.io.Serializable {
        public static final double[] VALUES = new double[2];

        static {
            TwoOutStart_Activation_0_0.fill(VALUES);
        }

        static final class TwoOutStart_Activation_0_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
            }
        }
    }

    // Neuron activation values for Rectifier layer
    static class TwoOutStart_Activation_1 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            TwoOutStart_Activation_1_0.fill(VALUES);
        }

        static final class TwoOutStart_Activation_1_0 implements java.io.Serializable {
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
    static class TwoOutStart_Activation_2 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            TwoOutStart_Activation_2_0.fill(VALUES);
        }

        static final class TwoOutStart_Activation_2_0 implements java.io.Serializable {
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
    static class TwoOutStart_Activation_3 implements java.io.Serializable {
        public static final double[] VALUES = new double[2];

        static {
            TwoOutStart_Activation_3_0.fill(VALUES);
        }

        static final class TwoOutStart_Activation_3_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.0;
                sa[1] = 0.0;
            }
        }
    }

    // Neuron bias values for Input layer
    static class TwoOutStart_Bias_0 implements java.io.Serializable {
        public static final double[] VALUES = null;
    }

    // Neuron bias values for Rectifier layer
    static class TwoOutStart_Bias_1 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            TwoOutStart_Bias_1_0.fill(VALUES);
        }

        static final class TwoOutStart_Bias_1_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 0.436030683218977;
                sa[1] = 0.4919805202069337;
                sa[2] = 0.48698384547490353;
                sa[3] = 0.46006850334969096;
                sa[4] = 0.4452187063104936;
                sa[5] = 0.5937782615452561;
                sa[6] = 0.5597439764078681;
                sa[7] = 0.5289587338544509;
                sa[8] = 0.4898424105869332;
                sa[9] = 0.32791617181985966;
                sa[10] = 0.5387161641153244;
                sa[11] = 0.6273145900558481;
                sa[12] = 0.4876303139600162;
                sa[13] = 0.6715753948524776;
                sa[14] = 0.5491615672916683;
                sa[15] = 0.5581378821673102;
            }
        }
    }

    // Neuron bias values for Rectifier layer
    static class TwoOutStart_Bias_2 implements java.io.Serializable {
        public static final double[] VALUES = new double[16];

        static {
            TwoOutStart_Bias_2_0.fill(VALUES);
        }

        static final class TwoOutStart_Bias_2_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = 1.0791818368917965;
                sa[1] = 0.8912856535910968;
                sa[2] = 0.9111296735231832;
                sa[3] = 1.1188506957534825;
                sa[4] = 0.8810867723646979;
                sa[5] = 0.8430437677927197;
                sa[6] = 1.1043958005873742;
                sa[7] = 0.9314236781427367;
                sa[8] = 0.9234055970205446;
                sa[9] = 0.9014974088397021;
                sa[10] = 1.0977925039976448;
                sa[11] = 0.9157027227771654;
                sa[12] = 1.09292767373642;
                sa[13] = 0.8642131131999705;
                sa[14] = 1.0752628023780737;
                sa[15] = 1.041580423289574;
            }
        }
    }

    // Neuron bias values for Softmax layer
    static class TwoOutStart_Bias_3 implements java.io.Serializable {
        public static final double[] VALUES = new double[2];

        static {
            TwoOutStart_Bias_3_0.fill(VALUES);
        }

        static final class TwoOutStart_Bias_3_0 implements java.io.Serializable {
            static final void fill(double[] sa) {
                sa[0] = -0.05897663901836263;
                sa[1] = 0.06232863730956692;
            }
        }
    }

    static class TwoOutStart_Weight_0 implements java.io.Serializable {
        public static final float[] VALUES = null;
    }

    // Neuron weights connecting Input and Rectifier layer
    static class TwoOutStart_Weight_1 implements java.io.Serializable {
        public static final float[] VALUES = new float[32];

        static {
            TwoOutStart_Weight_1_0.fill(VALUES);
        }

        static final class TwoOutStart_Weight_1_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -0.2737392f;
                sa[1] = -0.009584657f;
                sa[2] = -0.30308273f;
                sa[3] = 0.07944059f;
                sa[4] = 0.14948905f;
                sa[5] = -0.30810523f;
                sa[6] = -0.5032481f;
                sa[7] = 0.09284413f;
                sa[8] = 0.69308466f;
                sa[9] = -0.57694256f;
                sa[10] = -0.41877785f;
                sa[11] = -0.36199355f;
                sa[12] = 0.6988564f;
                sa[13] = 0.068473265f;
                sa[14] = -0.04519224f;
                sa[15] = 0.78516436f;
                sa[16] = 0.15280423f;
                sa[17] = -0.3784073f;
                sa[18] = -0.6656429f;
                sa[19] = 0.28584713f;
                sa[20] = 0.4741103f;
                sa[21] = -0.21792117f;
                sa[22] = 0.60875237f;
                sa[23] = 0.40890914f;
                sa[24] = 0.075528316f;
                sa[25] = -0.7560559f;
                sa[26] = -0.024243081f;
                sa[27] = -0.4616048f;
                sa[28] = 0.046631265f;
                sa[29] = -0.4258165f;
                sa[30] = -0.31235075f;
                sa[31] = -0.3712856f;
            }
        }
    }

    // Neuron weights connecting Rectifier and Rectifier layer
    static class TwoOutStart_Weight_2 implements java.io.Serializable {
        public static final float[] VALUES = new float[256];

        static {
            TwoOutStart_Weight_2_0.fill(VALUES);
        }

        static final class TwoOutStart_Weight_2_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -0.09899126f;
                sa[1] = 0.43291613f;
                sa[2] = 0.43393904f;
                sa[3] = 0.40169752f;
                sa[4] = 0.27931795f;
                sa[5] = 0.4035309f;
                sa[6] = -0.13629723f;
                sa[7] = 0.01743929f;
                sa[8] = -0.055404678f;
                sa[9] = -0.21956275f;
                sa[10] = -0.24119134f;
                sa[11] = -0.47058165f;
                sa[12] = 0.076106764f;
                sa[13] = -0.08788374f;
                sa[14] = 0.4404463f;
                sa[15] = 0.048648257f;
                sa[16] = 0.095880695f;
                sa[17] = 0.007886587f;
                sa[18] = -0.4142914f;
                sa[19] = -0.4258201f;
                sa[20] = -0.43817854f;
                sa[21] = -0.49190953f;
                sa[22] = -0.11551162f;
                sa[23] = 0.19130892f;
                sa[24] = 0.1309081f;
                sa[25] = -0.017449137f;
                sa[26] = -0.34426668f;
                sa[27] = -0.28877062f;
                sa[28] = 0.011997693f;
                sa[29] = -0.46746436f;
                sa[30] = 0.08117765f;
                sa[31] = 0.03201149f;
                sa[32] = -0.12010714f;
                sa[33] = -0.21608563f;
                sa[34] = -0.26553705f;
                sa[35] = -0.03150712f;
                sa[36] = 0.41934872f;
                sa[37] = 0.25716323f;
                sa[38] = 0.32935274f;
                sa[39] = 0.27846196f;
                sa[40] = -0.04157926f;
                sa[41] = -0.39733678f;
                sa[42] = 0.2555052f;
                sa[43] = -0.012346397f;
                sa[44] = -0.036853414f;
                sa[45] = -0.5314284f;
                sa[46] = 0.0889104f;
                sa[47] = 0.17021981f;
                sa[48] = -0.36905915f;
                sa[49] = 0.21532987f;
                sa[50] = 0.25204682f;
                sa[51] = -0.18206474f;
                sa[52] = 0.13359737f;
                sa[53] = 0.004551772f;
                sa[54] = -0.3574806f;
                sa[55] = -0.3698526f;
                sa[56] = 0.23561513f;
                sa[57] = 0.11750419f;
                sa[58] = -0.24063762f;
                sa[59] = -0.020156965f;
                sa[60] = -0.07660337f;
                sa[61] = 0.45333585f;
                sa[62] = 0.034676373f;
                sa[63] = 0.5558586f;
                sa[64] = -0.064188816f;
                sa[65] = -0.4685859f;
                sa[66] = -0.23079292f;
                sa[67] = -0.28395066f;
                sa[68] = -0.31976974f;
                sa[69] = 0.13379315f;
                sa[70] = -0.30426127f;
                sa[71] = -0.25364977f;
                sa[72] = 0.030390045f;
                sa[73] = 0.079091f;
                sa[74] = -0.19037247f;
                sa[75] = -0.40987992f;
                sa[76] = -0.23552953f;
                sa[77] = 0.20100394f;
                sa[78] = -0.419176f;
                sa[79] = -0.017281828f;
                sa[80] = -0.035911445f;
                sa[81] = 0.04360353f;
                sa[82] = -0.040348705f;
                sa[83] = 0.052806653f;
                sa[84] = -0.27460498f;
                sa[85] = -0.31261763f;
                sa[86] = -0.18146956f;
                sa[87] = 0.027786044f;
                sa[88] = -0.42697573f;
                sa[89] = -0.11934885f;
                sa[90] = -0.41947576f;
                sa[91] = -0.038770072f;
                sa[92] = 0.11487149f;
                sa[93] = -0.5601446f;
                sa[94] = 0.11230796f;
                sa[95] = -0.345052f;
                sa[96] = 0.32365087f;
                sa[97] = 0.24738787f;
                sa[98] = -0.25393903f;
                sa[99] = 0.31532365f;
                sa[100] = -0.0022400932f;
                sa[101] = -0.10564995f;
                sa[102] = -0.31070575f;
                sa[103] = -0.30950415f;
                sa[104] = -0.32113665f;
                sa[105] = 0.17293885f;
                sa[106] = 0.13453315f;
                sa[107] = -0.23941417f;
                sa[108] = 0.3934318f;
                sa[109] = -0.24943176f;
                sa[110] = 0.27130702f;
                sa[111] = 0.12802625f;
                sa[112] = 0.41775665f;
                sa[113] = -0.018278822f;
                sa[114] = 0.19471143f;
                sa[115] = -0.30444196f;
                sa[116] = 0.27860594f;
                sa[117] = -0.2729778f;
                sa[118] = -0.3995129f;
                sa[119] = 0.18799597f;
                sa[120] = -0.20921808f;
                sa[121] = 0.16671737f;
                sa[122] = -0.14839062f;
                sa[123] = 0.25845125f;
                sa[124] = -0.051528983f;
                sa[125] = 0.058711413f;
                sa[126] = -0.09245494f;
                sa[127] = 0.09252384f;
                sa[128] = -0.27497223f;
                sa[129] = 0.13164824f;
                sa[130] = 0.04675959f;
                sa[131] = 0.20819922f;
                sa[132] = -0.078807816f;
                sa[133] = 0.034110885f;
                sa[134] = 0.27244458f;
                sa[135] = -0.03241046f;
                sa[136] = 0.035839602f;
                sa[137] = 0.22362952f;
                sa[138] = 0.38377532f;
                sa[139] = 0.14160842f;
                sa[140] = 0.1339214f;
                sa[141] = 0.20458792f;
                sa[142] = -0.0142444f;
                sa[143] = -0.51252764f;
                sa[144] = -0.3606508f;
                sa[145] = -0.33517942f;
                sa[146] = -0.13428828f;
                sa[147] = 0.38125187f;
                sa[148] = 0.23509747f;
                sa[149] = -0.4060918f;
                sa[150] = -0.2634155f;
                sa[151] = 0.066497535f;
                sa[152] = 0.10520716f;
                sa[153] = -0.41082793f;
                sa[154] = 0.2752088f;
                sa[155] = 0.12950906f;
                sa[156] = -0.11442821f;
                sa[157] = -0.16734256f;
                sa[158] = -0.2798427f;
                sa[159] = -0.18812737f;
                sa[160] = 0.3335639f;
                sa[161] = 0.2638649f;
                sa[162] = 0.031738084f;
                sa[163] = 0.17069979f;
                sa[164] = 0.12245918f;
                sa[165] = 0.25640205f;
                sa[166] = 0.21324739f;
                sa[167] = -0.40791714f;
                sa[168] = 0.276274f;
                sa[169] = 0.41890633f;
                sa[170] = 0.21149427f;
                sa[171] = 0.1571925f;
                sa[172] = 0.15802182f;
                sa[173] = -0.14353235f;
                sa[174] = 0.25836086f;
                sa[175] = -0.11538774f;
                sa[176] = 0.17507789f;
                sa[177] = 0.30315325f;
                sa[178] = -0.15775824f;
                sa[179] = -0.041780014f;
                sa[180] = 0.07298007f;
                sa[181] = -0.12649935f;
                sa[182] = -0.13985851f;
                sa[183] = 0.08101301f;
                sa[184] = -0.21251608f;
                sa[185] = 0.02841157f;
                sa[186] = 0.24698837f;
                sa[187] = 0.08486095f;
                sa[188] = -0.3574084f;
                sa[189] = -0.34157193f;
                sa[190] = -0.31249216f;
                sa[191] = -0.025118057f;
                sa[192] = 0.31637388f;
                sa[193] = 0.46959075f;
                sa[194] = 0.362587f;
                sa[195] = 0.15427703f;
                sa[196] = -0.16788484f;
                sa[197] = 0.3160905f;
                sa[198] = 0.30036822f;
                sa[199] = -0.1673262f;
                sa[200] = 0.06434048f;
                sa[201] = -0.17946549f;
                sa[202] = -0.30172783f;
                sa[203] = 0.0058342866f;
                sa[204] = -0.36121073f;
                sa[205] = -0.013613948f;
                sa[206] = -0.07326042f;
                sa[207] = 0.48260793f;
                sa[208] = 0.32157826f;
                sa[209] = -0.2983844f;
                sa[210] = -0.42744187f;
                sa[211] = -0.43736643f;
                sa[212] = 0.2691224f;
                sa[213] = 0.094028555f;
                sa[214] = 0.0047728564f;
                sa[215] = -0.21008427f;
                sa[216] = -0.36970696f;
                sa[217] = -0.21265374f;
                sa[218] = 0.25279433f;
                sa[219] = -0.26182255f;
                sa[220] = -0.31958216f;
                sa[221] = 0.14516981f;
                sa[222] = -0.35450906f;
                sa[223] = 0.17612232f;
                sa[224] = -0.12524116f;
                sa[225] = -0.26340926f;
                sa[226] = 0.101216674f;
                sa[227] = -0.17533682f;
                sa[228] = -0.4211705f;
                sa[229] = 0.44591242f;
                sa[230] = -0.0060980017f;
                sa[231] = 0.0626769f;
                sa[232] = 0.4519788f;
                sa[233] = 0.26767823f;
                sa[234] = -0.020881003f;
                sa[235] = 0.24933377f;
                sa[236] = -0.21082139f;
                sa[237] = 0.2161451f;
                sa[238] = -0.22675966f;
                sa[239] = 0.00864444f;
                sa[240] = -0.3786651f;
                sa[241] = 0.17270678f;
                sa[242] = -0.013426562f;
                sa[243] = 0.3322193f;
                sa[244] = -0.4488941f;
                sa[245] = -0.036031216f;
                sa[246] = -0.2817789f;
                sa[247] = -0.12302359f;
                sa[248] = -0.08249254f;
                sa[249] = -0.23379174f;
                sa[250] = -0.13239972f;
                sa[251] = -0.3169415f;
                sa[252] = -0.44605416f;
                sa[253] = 0.17295715f;
                sa[254] = 0.006492542f;
                sa[255] = 0.14546421f;
            }
        }
    }

    // Neuron weights connecting Rectifier and Softmax layer
    static class TwoOutStart_Weight_3 implements java.io.Serializable {
        public static final float[] VALUES = new float[32];

        static {
            TwoOutStart_Weight_3_0.fill(VALUES);
        }

        static final class TwoOutStart_Weight_3_0 implements java.io.Serializable {
            static final void fill(float[] sa) {
                sa[0] = -0.51248205f;
                sa[1] = 1.5901641f;
                sa[2] = 0.72768766f;
                sa[3] = -2.0391543f;
                sa[4] = 0.6566883f;
                sa[5] = -0.63409525f;
                sa[6] = 1.6596706f;
                sa[7] = 2.2548969f;
                sa[8] = 0.6358926f;
                sa[9] = 1.717727f;
                sa[10] = -2.2357929f;
                sa[11] = 0.015926322f;
                sa[12] = 1.7333534f;
                sa[13] = -1.1971216f;
                sa[14] = -1.4990138f;
                sa[15] = -1.6926152f;
                sa[16] = 0.3962589f;
                sa[17] = -0.3753426f;
                sa[18] = -1.9084059f;
                sa[19] = -0.2445689f;
                sa[20] = -2.0173826f;
                sa[21] = -2.3526049f;
                sa[22] = 2.4153376f;
                sa[23] = 1.3479662f;
                sa[24] = -1.3939612f;
                sa[25] = -0.09394375f;
                sa[26] = -0.7546077f;
                sa[27] = -0.77318656f;
                sa[28] = 2.0425313f;
                sa[29] = -1.725354f;
                sa[30] = -0.95106083f;
                sa[31] = 1.5806248f;
            }
        }
    }

    // The class representing training column names
    static class NamesHolder_TwoOutStart implements java.io.Serializable {
        public static final String[] VALUES = new String[2];

        static {
            NamesHolder_TwoOutStart_0.fill(VALUES);
        }

        static final class NamesHolder_TwoOutStart_0 implements java.io.Serializable {
            static final void fill(String[] sa) {
                sa[0] = "RSSI MIDDLE_ORIGIN";
                sa[1] = "RSSI TRUNK_ORIGIN";
            }
        }
    }

    // The class representing column class
    static class TwoOutStart_ColInfo_2 implements java.io.Serializable {
        public static final String[] VALUES = new String[2];

        static {
            TwoOutStart_ColInfo_2_0.fill(VALUES);
        }

        static final class TwoOutStart_ColInfo_2_0 implements java.io.Serializable {
            static final void fill(String[] sa) {
                sa[0] = "inside";
                sa[1] = "outside";
            }
        }
    }
}
