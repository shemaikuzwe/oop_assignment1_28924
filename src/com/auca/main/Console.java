package com.auca.main;

import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/** The single shared Scanner on System.in plus small line-based input helpers (no nextInt()). */
final class Console {
    private static final Scanner IN = new Scanner(System.in);

    private Console() {
    }

    static String read(String prompt) {
        System.out.print(prompt);
        if (!IN.hasNextLine()) {
            System.out.println("\nInput closed. Goodbye.");
            System.exit(0);
        }
        return IN.nextLine().trim();
    }

    /** Reads a whole line and converts it to a number in [min, max], re-prompting on bad input. */
    static int choose(String prompt, int min, int max) {
        while (true) {
            String raw = read(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("! Choose a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a number. Choose " + min + "-" + max + ".");
            }
        }
    }

    static boolean confirm(String question) {
        return choose(question + " [1] Yes [0] No : ", 0, 1) == 1;
    }

    /** Numbered list of existing records; returns the chosen one or null when cancelled or empty. */
    static <X> X pick(String title, List<X> items, Function<X, String> label) {
        title(title);
        if (items.isEmpty()) {
            System.out.println("  (no records found)");
            return null;
        }
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("  %d %s%n", i + 1, label.apply(items.get(i)));
        }
        System.out.println("  0 Cancel");
        int choice = choose("Select option: ", 0, items.size());
        return choice == 0 ? null : items.get(choice - 1);
    }

    static void title(String text) {
        String bar = "-".repeat(Math.max(4, (56 - text.length()) / 2));
        System.out.println("\n" + bar + " " + text + " " + bar);
    }
}
