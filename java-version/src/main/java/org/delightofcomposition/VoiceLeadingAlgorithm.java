package org.delightofcomposition;

@FunctionalInterface
public interface VoiceLeadingAlgorithm {
    int[] VL(int[] firstChord, int[] secondChord, int target);
}
