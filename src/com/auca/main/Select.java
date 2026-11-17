package com.auca.main;

import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

import java.util.List;
import java.util.function.Supplier;


final class Select<X extends Identifiable & Registrable> {
    private final String name;
    private final Supplier<List<X>> options;
    private final Supplier<Form<X>> register; // null = no nested registration offered
    private final boolean required;

    Select(String name, Supplier<List<X>> options, Supplier<Form<X>> register, boolean required) {
        this.name = name;
        this.options = options;
        this.register = register;
        this.required = required;
    }

    String hint() {
        return name + " | select" + (required ? "" : " | optional");
    }

    
    void ensureAvailable(String formName) throws PrerequisiteMissingException {
        if (!required || !options.get().isEmpty()) {
            return;
        }
        System.out.println("\n! No " + name + " exists yet. " + formName + " registration needs one first.");
        if (register == null || Console.choose("[1] Register " + name + " now  [0] Cancel " + formName + " : ", 0, 1) == 0) {
            throw new PrerequisiteMissingException(formName + " cancelled: " + name + " is required first.");
        }
        if (register.get().create() == null) {
            throw new PrerequisiteMissingException(formName + " cancelled: the " + name + " was not saved.");
        }
        System.out.println("+ " + name + " saved. Returning to " + formName + "...");
    }

    /** Returns the chosen record, null for "none", Form.BACK or Form.CANCEL. */
    Object read(String prompt, Object current) {
        while (true) {
            List<X> list = options.get();
            System.out.println(prompt);
            Console.title("CHOOSE " + name.toUpperCase());
            if (list.isEmpty()) {
                System.out.println("  (no " + name + " available yet)");
            }
            for (int i = 0; i < list.size(); i++) {
                System.out.printf("  %d %s%n", i + 1, list.get(i).getRegistrationSummary());
            }
            int next = list.size() + 1;
            int registerOption = register == null ? -1 : next++;
            int noneOption = required ? -1 : next;
            if (registerOption > 0) {
                System.out.printf("  %d [+] Register a new %s%n", registerOption, name);
            }
            if (noneOption > 0) {
                System.out.printf("  %d None for now%n", noneOption);
            }
            System.out.println("  0 Cancel  |  BACK previous step" + (current == null ? "" : "  |  Enter keeps current"));

            String raw = Console.read("Select option: ");
            if (raw.isEmpty() && current != null) {
                return current;
            }
            if (raw.equalsIgnoreCase("BACK")) {
                return Form.BACK;
            }
            if (raw.equals("0") || raw.equalsIgnoreCase("CANCEL")) {
                return Form.CANCEL;
            }
            try {
                int choice = Integer.parseInt(raw);
                if (choice >= 1 && choice <= list.size()) {
                    return list.get(choice - 1);
                }
                if (choice == noneOption) {
                    return null;
                }
                if (choice == registerOption) {
                    X created = register.get().create();
                    if (created != null) {
                        return created;
                    }
                } else {
                    System.out.println("! Choose a number from the list.");
                }
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a number.");
            } catch (PrerequisiteMissingException e) {
                System.out.println("! " + e.getMessage());
            }
        }
    }
}
