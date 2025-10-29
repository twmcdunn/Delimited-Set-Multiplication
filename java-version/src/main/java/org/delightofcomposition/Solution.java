package org.delightofcomposition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.delightofcomposition.util.TextIO;

public class Solution {
    public SetMultiplication sm;
    public ArrayList<int[]> options;
    public int multiplicandIndex;

    public Solution(SetMultiplication sm, ArrayList<int[]> options, int multiplicandIndex) {
        this.sm = sm;
        this.options = options;
        this.multiplicandIndex = multiplicandIndex;
    }

    /**
     * Writes an ArrayList of Solution objects to a text file.
     * Format: For each solution:
     * Line 1: First multiplier chord (space-separated integers)
     * Line 2: Second multiplier chord (space-separated integers)
     * Line 3: Number of options
     * Line 4+: Each option chord (space-separated integers)
     * Line final: Multiplicand index
     * Line empty: Separator between solutions
     */
    public static void writeToFile(ArrayList<Solution> solutions, String filename) {
        TextIO.writeFile(filename);
        for (Solution solution : solutions) {
            // Write first multiplier chord
            for (int note : solution.sm.multipliers[0]) {
                TextIO.put(note + " ");
            }
            TextIO.putln();

            // Write second multiplier chord
            for (int note : solution.sm.multipliers[1]) {
                TextIO.put(note + " ");
            }
            TextIO.putln();

            // Write number of options
            TextIO.putln(solution.options.size());

            // Write each option
            for (int[] option : solution.options) {
                for (int note : option) {
                    TextIO.put(note + " ");
                }
                TextIO.putln();
            }

            // Write multiplicand index
            TextIO.putln(solution.multiplicandIndex);

            // Write separator
            TextIO.putln();
        }
        TextIO.writeStandardOutput();
    }

    /**
     * Reads a text file and returns an ArrayList of Solution objects.
     * Expects the format written by writeToFile.
     */
    public static ArrayList<Solution> readFromFile(String filename) {
        ArrayList<Solution> solutions = new ArrayList<>();
        TextIO.readFile(filename);

        while (!TextIO.eof()) {
            String line = TextIO.getln().trim();
            if (line.isEmpty())
                continue;

            // Read first multiplier chord
            int[] firstChord = Arrays.stream(line.split(" "))
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();

            // Read second multiplier chord
            line = TextIO.getln().trim();
            int[] secondChord = Arrays.stream(line.split(" "))
                    .filter(s -> !s.isEmpty())
                    .mapToInt(Integer::parseInt)
                    .toArray();

            // Create SetMultiplication object
            SetMultiplication sm = new SetMultiplication(firstChord, secondChord);

            // Read number of options
            int numOptions = Integer.parseInt(TextIO.getln().trim());

            // Read options
            ArrayList<int[]> options = new ArrayList<>();
            for (int i = 0; i < numOptions; i++) {
                line = TextIO.getln().trim();
                int[] option = Arrays.stream(line.split(" "))
                        .filter(s -> !s.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .toArray();
                options.add(option);
            }

            // Read multiplicand index
            int multiplicandIndex = Integer.parseInt(TextIO.getln().trim());

            // Create and add solution
            solutions.add(new Solution(sm, options, multiplicandIndex));
        }

        TextIO.readStandardInput();
        return solutions;
    }

    @Override
    public String toString() {
        String str = sm + "";
        str += "\n      Multiplicand: Chord " + (multiplicandIndex + 1);
        str += "\n      Progressions: ";
        for (int[] prog : options) {
            str += "\n            [" + Arrays.stream(prog)
                    .mapToObj(c -> "Chord " + (c + 1))
                    .collect(Collectors.joining(" -> ")) + "]";
        }
        return str;
    }
}
