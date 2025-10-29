package org.delightofcomposition.simpleparams;

import java.util.ArrayList;

public class OnsetGenerator {

    /*
     * Dummy method right now. Simply generates even onsets
     * Can be adapted later to generate more interesting rhythms
     * and integrate directly with the rest of the project.
     */
    public static ArrayList<Integer> generateOnsets(int numOfPulses){
        ArrayList<Integer> onsets = new ArrayList<Integer>();
        for(int i = 0; i < numOfPulses; i++){
            onsets.add(i);
        }
        return onsets;
    }
}
