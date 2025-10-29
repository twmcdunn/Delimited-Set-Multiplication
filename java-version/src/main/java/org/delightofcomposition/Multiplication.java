package org.delightofcomposition;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.delightofcomposition.SetMultiplicationCLI.SolutionSelector;
import org.delightofcomposition.util.FindResourceFile;
import org.delightofcomposition.util.Types;

import war.CombinationsCalculator;

public abstract class Multiplication {
    public Random rand;
    public static String[] notesInSharps = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static String[] notesInFlats = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

    /*
     * Precondition: the multipliers must each contain the same
     * number of notes (for use with voiceleading algorithms)
     */
    public int[][] multipliers;

    /*
     * Move the existing logic of this method to the method below.
     * Refactor this as an overload for backward compatibility. It now
     * simiply generates an array of random ints in the range of
     * multiplies.length and passes it, together with mulplicand to
     * the method below
     */
    public abstract ArrayList<int[]> multiply(int[] multiplicand);

    /*
     * Allows input to specify multipliers. Move the bulk of your
     * multiply logic to this method, and turn the existing method into an
     * overload for backward compatibility.
     */
    public abstract ArrayList<int[]> multiply(int[] multiplicand, int[] multiplierIndexes);

    /*
     * Using the helper method above, multiply all chord
     * in the input chord progression by randomly selected multipliers
     */
    public abstract ArrayList<int[]> multiply(ArrayList<int[]> multiplicands);

    /*
     * generate an intial progression,
     * multiply(multipier[rand.nextInt(multipier.length)])
     * and pass it through
     * the multiply methon above as many times as specified by
     * depth
     */
    public abstract ArrayList<int[]> generateProgression(int depth);

    /*
     * Returns effecy possible multiplierIndexes array.
     * These are a lot like permutations, except they contain
     * repeated elements.
     * 
     * You may use a recursive, depth-first backtracking algorithm,
     * similar to your solution for permutations in stepwise
     * voiceleading (but with repeated notes), or you may use
     * a different approach. If you choice the former approach
     * you will need to make a helper method that includes a
     * startsWith parameter. The helper method with be the recursive
     * piecce, while this method is just an endpoint. You may
     * also need to convert between arraylists and arrays. A helper
     * for that is provided below, if needed.
     * 
     * This algorithm is equivalent to listing all integers in base
     * n (multipliers.length) within range 0 - n ^ length
     */
    public abstract ArrayList<int[]> getAllMultipierOptions(int length);

    /*
     * Returns all delimited multiplier options for which the superset
     * of the product belongs to the target set class. This lets us use
     * set multiplication while controling the large pitch collection
     * it forms (through exhaustive search).
     * 
     * Depends on war.PrimeFormCalculator, getAllMuliplierOptions, and
     * Types.arrToArrList.
     * 
     * First get all multiplier options for multiplicand.length. Instantiate
     * a prime form calculator. get the primeform of the targetSet. Iterate
     * through all multiplier options. For each, mutliply using the multiplicand
     * and the option. Convert the result to a stream, flatmap the stream to int,
     * distinct the stream (so there are no duplicates), box the elements, and
     * collect
     * them in an arraylist. This creates the superset of the product for the given
     * option. Get the prime form of this. If it is equal to the primeform of the
     * target, add it to the array of options you will return.
     */
    public abstract ArrayList<int[]> getDelimitedMultiplierOptions(int[] multiplicand, int[] targetSet);

    public abstract ArrayList<Solution> findSolutions(int[] targetSet, int chordCardinality);

    public ArrayList<int[]> compose(int[] targetSet, int chordCardinality, int depth) {
        CombinationsCalculator cc = new CombinationsCalculator();

        String cachePath = FindResourceFile.findResourceDirectory("resources/solution-cache/").toString()
                + cc.getForteNumber(Types.arrToArrList(targetSet)) + "_" + chordCardinality + ".txt";

        ArrayList<Solution> solutions = null;
        try {
            File solutionFile = FindResourceFile.findResourceFile(cachePath);
            solutions = Solution.readFromFile(solutionFile.getAbsolutePath());
        } catch (RuntimeException ex) {
            System.out.println("No solutions cached. Calculating solutions...");
            solutions = findSolutions(targetSet, chordCardinality);
            if (solutions.size() == 0) {
                System.out.println("No chords in the universe satisfy these contraints... quiting");
                return null;
            }
            System.out.println(
                    "Found " + solutions.size() + " solutions. Writing them to a cache file to save time later...");
            Solution.writeToFile(solutions, cachePath);
        }

        solutions = new SolutionSelector().userSelectSolutions(solutions);

        System.out.println("\n\nYou selected " + solutions.size() + " solution(s):");
        for (int i = 0; i < solutions.size(); i++) {
            System.out.println("Selected #" + i + ":");
            System.out.println(solutions.get(i));
            System.out.println();
        }

        ArrayList<ArrayList<int[]>> localProgressionOptions = solutions
                .stream()
                .map(s -> {
                    ArrayList<ArrayList<int[]>> lpo = new ArrayList<ArrayList<int[]>>();
                    for (int[] option : s.options)
                        lpo.add(s.sm.multiply(s.sm.multipliers[s.multiplicandIndex], option));
                    return lpo;
                })
                .flatMap(List::stream)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<int[]> result = null;
        for (int i = 0; i < depth; i++) {
            int[][] m = solutions.get(rand.nextInt(solutions.size())).sm.multipliers;
            result = iterateBottomUp(m[rand.nextInt(m.length)], localProgressionOptions);

            // result is now a valid local progression (made up of multiple sections,
            // each of which fits the scale). we can add it to localProgressionOptions,
            // but because it is longer than the previous local progression, this will
            // cause uneven nesting, which makes the form interesting. The listener
            // won't be able to tell this is happening, since the progressions don't
            // correspond
            // to a particular rhythmic or registeral profile, but that could be added
            // later, if desired, or we can just enjoy the uniform texture with the
            // characteristic
            // color that comes from the choice of scale and multipliers, shaping it by
            // hand with envelopes, instead of automating more parameters.
            localProgressionOptions.add(result);
        }
        return result;
    }

    /*
     * helper method. need a to take a bottom up approach
     * here, since the local chord progressions are constrained.
     * This method selects a random local progression that satisfies
     * the constraints and transposes it at the pitch levels of each
     * of the members of the muliplicand chord.
     */
    public ArrayList<int[]> iterateBottomUp(int[] multiplicand, ArrayList<ArrayList<int[]>> localProgressionOptions) {
        ArrayList<int[]> iteration = new ArrayList<int[]>();
        for (int r : multiplicand) {
            ArrayList<int[]> localProgression = localProgressionOptions
                    .get(rand.nextInt(localProgressionOptions.size()));
            for (int[] chord : localProgression) {
                int[] transposedChord = new int[chord.length];
                for (int i = 0; i < transposedChord.length; i++) {
                    transposedChord[i] = (chord[i] + r) % 12;
                }
                iteration.add(transposedChord);
            }
        }
        return iteration;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < 2; i++) {
            str += "\n      Chord " + (i + 1) + ":" +
                    Arrays.stream(multipliers[i])
                            .mapToObj(n -> notesInSharps[n])
                            .collect(Collectors.joining(","));
        }
        return str;
    }
}
