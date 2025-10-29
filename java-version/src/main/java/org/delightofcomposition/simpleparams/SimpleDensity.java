package org.delightofcomposition.simpleparams;

import org.delightofcomposition.envelopes.LoadEnvs;

public class SimpleDensity {
    public static double getDensity(double time) {
        // assumes density is defined in envelop 3 (index 2)
        // feel free to define a different function of time
        // to control density
        return LoadEnvs.envs.get(2).getValue(time);
    }
}
