package org.delightofcomposition;

import java.util.ArrayList;
import java.util.Random;

import org.delightofcomposition.envelopes.LoadEnvs;
import org.delightofcomposition.simpleparams.OnsetGenerator;
import org.delightofcomposition.simpleparams.SimpleAmp;
import org.delightofcomposition.simpleparams.SimpleDensity;
import org.delightofcomposition.simpleparams.SimplePan;
import org.delightofcomposition.sound.WaveWriter;
import org.delightofcomposition.synth.SimpleSynth;
import org.delightofcomposition.synth.Synth;
import org.delightofcomposition.util.ProgressBar;

public class Main {
    public static void main(String[] args) {
        int[][] harmonicCells = new int[][] { { 0, 4, 7, 11 }, { 0, 6, 7, 11 } };
        Multiplication m = new SetMultiplication(harmonicCells[0], harmonicCells[1]);

        // use the octatatonic scale, with 4-note chords, depth 3
        // the length of the progression is unpredictable due to uneven nesting (see
        // note in Multiplication.compose)
        ArrayList<int[]> chords = m.compose(new int[] { 0, 1, 3, 4, 6, 7, 9, 10 }, 4, 3);

        // for targeted voice-leading (depends on an envelope) follow this pattern
        generateComposition(chords, 0.05, new SimpleSynth(), new VoiceLeading()::uncommonDirectedVoiceLeading);

        // for non-targeted voice-leading (doesn't depend on an envelope) follow this
        // pattern
        // VoiceLeading vl = new VoiceLeading();
        // generateComposition(chords, 0.05, new SimpleSynth(), (fc, sc, t) ->
        // vl.stepwiseVoiceLeading(fc, sc));

    }

    public static void generateComposition(ArrayList<int[]> chords, double pulseDur, Synth synth,
            VoiceLeadingAlgorithm VLA) {
        Random rand = new Random(123);
        LoadEnvs.loadEnvs();
        WaveWriter ww = new WaveWriter("composition");
        int numOfPulses = 10;
        ArrayList<Integer> onsets = OnsetGenerator.generateOnsets(numOfPulses);
        double timelineStart = 0;

        // used for voiceleading logic
        double chordTime = 0;
        int[] lastChord = null;

        // used for progressbar
        int chordIndex = 0;
        int chordTotal = chords.size();

        adjustInitialOctave(chords, 4);

        while (chords.size() > 0) {
            ProgressBar.printProgressBar(chordIndex, chordTotal, "Generating Sounds");

            // used for progressbar
            chordIndex++;
            int[] chord = chords.remove(0);
            if (lastChord != null)
                chord = VLA.VL(lastChord, chord, (int) (48 + 12 * 4 * (LoadEnvs.envs.get(0).getValue(chordTime))));
            for (int n : chord) {
                double t = timelineStart + onsets.remove(0) * pulseDur;
                chordTime = t;
                if (onsets.size() == 0) {
                    timelineStart += numOfPulses * pulseDur;
                    onsets = OnsetGenerator.generateOnsets(numOfPulses);
                }
                double density = SimpleDensity.getDensity(t);

                if (rand.nextDouble() > density)
                    continue;

                double[] note = synth.note(440 * Math.pow(2, (n - 60) / 12.0), SimpleAmp.getAmp(t));
                for (int i = 0; i < note.length; i++) {
                    double pan = SimplePan.getPan(t + i / (double) WaveWriter.SAMPLE_RATE);
                    ww.df[0][(int) (t * WaveWriter.SAMPLE_RATE) + i] += pan * note[i];
                    ww.df[1][(int) (t * WaveWriter.SAMPLE_RATE) + i] += (1 - pan) * note[i];
                }
            }
            lastChord = chord;
        }
        ww.render();
    }

    public static void adjustInitialOctave(ArrayList<int[]> chords, int startingOct) {
        int[] firstChord = chords.get(0);
        for (int i = 0; i < firstChord.length; i++) {
            firstChord[i] += 12 * (startingOct + 1);
        }
    }
}
