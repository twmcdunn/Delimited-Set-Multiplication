package org.delightofcomposition.simpleparams;

import org.delightofcomposition.envelopes.LoadEnvs;

public class SimpleAmp {
    public static double getAmp(double time){
        //assumes amp is defined in envelop 2 (index 1)
        //feel free to define a different function of time
        //to control amp
        return LoadEnvs.envs.get(1).getValue(time);
    }
}
