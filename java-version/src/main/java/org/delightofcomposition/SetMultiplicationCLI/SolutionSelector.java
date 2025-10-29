package org.delightofcomposition.SetMultiplicationCLI;

import org.delightofcomposition.Solution;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import war.CombinationsCalculator;
import war.PrimeFormCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SolutionSelector {

    private static final int KEY_UP = 65;
    private static final int KEY_DOWN = 66;
    private static final int KEY_ESCAPE = 27;
    private static final int KEY_BRACKET = 91;
    private static final int KEY_ENTER = 13;
    private static final int KEY_NEWLINE = 10;
    private static final int KEY_BACKSPACE = 127;
    private static final int KEY_DELETE = 8;

    public static String[] notesInSharps = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
    public static String[] notesInFlats = { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };

    PrimeFormCalculator pfc = new PrimeFormCalculator();

    public ArrayList<Solution> userSelectSolutions(ArrayList<Solution> solutions) {
        if (solutions == null || solutions.isEmpty()) {
            System.out.println("No solutions to display.");
            return new ArrayList<>();
        }

        Terminal terminal = null;
        try {
            terminal = TerminalBuilder.builder()
                    .system(true)
                    .build();

            terminal.enterRawMode();
            NonBlockingReader reader = terminal.reader();

            int currentIndex = 0;
            int displayCount = 1;
            StringBuilder inputBuffer = new StringBuilder();
            String statusMessage = "";

            while (true) {

                clearScreen(terminal);
                terminal.writer().print("\033[1;1H"); // Ensure cursor is at top
                terminal.writer().println("=== Solution Viewer ===");
                String inputString = null;
                Boolean filtedSolutions = false;
                boolean filteredByChord = false;
                ArrayList<Integer> pf = null;
                if (inputBuffer.length() > 0) {
                    inputString = inputBuffer.toString();

                    if (inputString.matches("[0-9,\\s]+")) {
                        Set<Integer> selections = getValidIndexes(inputString, solutions.size());

                        filtedSolutions = selections.size() > 0;

                        if (filtedSolutions) {
                            terminal.writer().println("Showing selected solutions");
                            for (Integer idx : selections) {
                                terminal.writer().println("Solution #" + idx + ":");
                                terminal.writer().println(solutions.get(idx));
                            }
                        }
                    } else {
                        pf = getPrimeForm(inputString);

                        ArrayList<Integer> selections = searchByString(pf, solutions);
                        filtedSolutions = true;// selections.size() > 0;
                        filteredByChord = true;
                        if (filtedSolutions) {
                            terminal.writer().println("Showing selected solutions");
                            for (Integer idx : selections) {
                                terminal.writer().println("Solution #" + idx + ":");
                                terminal.writer().println(solutions.get(idx));
                            }
                        }
                    }
                }

                if (filtedSolutions) {
                    displayControls(terminal);
                    if (filteredByChord) {
                        terminal.writer().println("\nPrime Form: ["
                                + pf.stream()
                                        .map(n -> (n + ""))
                                        .collect(Collectors.joining(","))
                                + "]");
                        terminal.writer().println("Showing Solutions with Chord: " + inputString);
                    } else
                        terminal.writer().println("\nSelected Solutions: " + inputString);
                } else {
                    displaySolutions(terminal, solutions, currentIndex, displayCount);
                    displayControls(terminal);
                }

                if (!statusMessage.isEmpty()) {
                    terminal.writer().println("Status: " + statusMessage);
                    statusMessage = "";
                }

                terminal.writer().flush();

                int key = reader.read();

                if (key == KEY_ESCAPE) {
                    // Check for arrow keys (ESC + [ + direction)
                    int next = reader.read();
                    if (next == KEY_BRACKET) {
                        int direction = reader.read();
                        if (direction == KEY_UP) {
                            currentIndex += displayCount;
                            currentIndex = Math.max(0, currentIndex);
                            currentIndex = Math.min(solutions.size() - displayCount, currentIndex);
                            inputBuffer.setLength(0);
                        } else if (direction == KEY_DOWN) {
                            currentIndex -= displayCount;
                            currentIndex = Math.max(0, currentIndex);
                            currentIndex = Math.min(solutions.size() - displayCount, currentIndex);
                            inputBuffer.setLength(0);
                        }
                    }
                } else if (key == '+') {
                    displayCount = Math.min(solutions.size(), displayCount + 1);
                    currentIndex = Math.max(0, currentIndex);
                    currentIndex = Math.min(solutions.size() - displayCount, currentIndex);
                    inputBuffer.setLength(0);
                } else if (key == '-' || key == '_') {
                    displayCount = Math.max(1, displayCount - 1);
                    currentIndex = Math.max(0, currentIndex);
                    currentIndex = Math.min(solutions.size() - displayCount, currentIndex);
                    inputBuffer.setLength(0);
                } else if (key == KEY_ENTER || key == KEY_NEWLINE) {
                    if (inputBuffer.length() > 0) {
                        String input = inputBuffer.toString().trim();
                        ArrayList<Solution> selected = processInput(input, solutions);
                        terminal.close();
                        return selected;
                    }
                } else if (key == KEY_BACKSPACE || key == KEY_DELETE) {
                    if (inputBuffer.length() > 0) {
                        inputBuffer.setLength(inputBuffer.length() - 1);
                    }
                } else if (key >= 32 && key <= 126) {
                    // Printable characters
                    inputBuffer.append((char) key);
                } else if (key == 'q' || key == 'Q') {
                    // Quit and return empty list
                    terminal.close();
                    return new ArrayList<>();
                }
            }

        } catch (IOException e) {
            System.err.println("Error initializing terminal: " + e.getMessage());
            return solutions;
        } finally {
            if (terminal != null) {
                try {
                    terminal.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    private void clearScreen(Terminal terminal) {
        terminal.writer().print("\033[2J"); // Clear entire screen
        terminal.writer().print("\033[H"); // Move cursor to top-left corner
        terminal.writer().print("\033[3J"); // Clear scrollback buffer
        terminal.writer().flush();
    }

    private void displaySolutions(Terminal terminal, ArrayList<Solution> solutions,
            int startIndex, int count) {
        terminal.writer().println("Showing solutions " + startIndex + " to " +
                Math.min(startIndex + count - 1, solutions.size() - 1) +
                " of " + (solutions.size() - 1) + "\n");

        int endIndex = Math.min(startIndex + count, solutions.size());
        for (int i = startIndex; i < endIndex; i++) {
            terminal.writer().println("--- Solution #" + i + " ---");
            terminal.writer().println(solutions.get(i).toString());
            terminal.writer().println();
        }
    }

    private void displayControls(Terminal terminal) {
        terminal.writer().println("\n--- Controls ---");
        terminal.writer().println("↑/↓: Navigate  |  +/-: Adjust display count");
        terminal.writer().println("Enter: Compose  |  q: Quit ");
        terminal.writer().println("Type numbers (e.g., '1,3,5') or note names (A,B,C#,Db)");
    }

    private Set<Integer> getValidIndexes(String input, int bound) {
        String[] indices = input.split(",");
        Set<Integer> validIndices = new HashSet<>();

        for (String indexStr : indices) {
            try {
                int index = Integer.parseInt(indexStr.trim());
                if (index >= 0 && index < bound) {
                    validIndices.add(index);
                }
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        return validIndices;
    }

    private ArrayList<Solution> processInput(String input, ArrayList<Solution> solutions) {
        ArrayList<Solution> result = new ArrayList<>();

        // Check if input contains only digits, commas, and whitespace (index selection)
        if (input.matches("[0-9,\\s]+")) {
            // Process as index selection
            Set<Integer> validIndices = getValidIndexes(input, solutions.size());

            for (int index : validIndices) {
                result.add(solutions.get(index));
            }

            return result.isEmpty() ? solutions : result;
        } else {
            // Process as search string
            ArrayList<Integer> indexes = searchByString(input, solutions);
            ArrayList<Solution> filtered = new ArrayList<Solution>();
            for (int idx : indexes)
                filtered.add(solutions.get(idx));
            return filtered;
        }
    }

    public ArrayList<Integer> getPrimeForm(String commaSeparatedList) {
        String[] noteNames = commaSeparatedList.split(",");
        Set<Integer> notes = new HashSet<>();

        for (String noteName : noteNames) {
            try {
                int note = Arrays.asList(notesInFlats).indexOf(noteName);
                if (note == -1) {
                    note = Arrays.asList(notesInSharps).indexOf(noteName);
                }
                notes.add(note);
            } catch (NumberFormatException e) {
                // Skip invalid numbers
            }
        }
        return pfc.getPrimeForm(new ArrayList<Integer>(notes));
    }

    public ArrayList<Integer> searchByString(String commaSeparatedList, ArrayList<Solution> solutions) {
        return searchByString(getPrimeForm(commaSeparatedList), solutions);
    }

    public ArrayList<Integer> searchByString(ArrayList<Integer> pf, ArrayList<Solution> solutions) {
        ArrayList<Integer> filtered = new ArrayList<Integer>();
        for (int i = 0; i < solutions.size(); i++) {
            Solution s = solutions.get(i);
            if (Arrays.stream(s.sm.multipliers)
                    .anyMatch(chord -> {
                        ArrayList<Integer> pf1 = pfc.getPrimeForm(Arrays.stream(chord)
                                .boxed() // Boxes each int to an Integer
                                .collect(Collectors.toCollection(ArrayList::new)));
                        return pf1.equals(pf);
                    }))
                filtered.add(i);
        }

        return filtered;
    }

}