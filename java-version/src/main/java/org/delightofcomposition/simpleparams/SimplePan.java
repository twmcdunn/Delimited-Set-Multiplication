package org.delightofcomposition.simpleparams;

import org.delightofcomposition.envelopes.LoadEnvs;

public class SimplePan {
    public static double getPan(double time) {
        // assumes pan is defined in envelop 4 (index 3)
        // feel free to define a different function of time
        // to control pan
        return LoadEnvs.envs.get(3).getValue(time);
    }
}
