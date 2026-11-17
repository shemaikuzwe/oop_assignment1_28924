package com.auca.main;

import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;


final class Form<T extends Identifiable & Registrable> {
    static final Object BACK = new Object();
    static final Object CANCEL = new Object();
    static final List<String> YES_NO = List.of("YES", "NO");

    
    interface Parser {
        Object parse(String raw, String field) throws ValidationException;
    }


    interface Rule<T> {
        void check(T record) throws ValidationException, DuplicateRecordException, BookUnavailableException;
    }

   
    record Values(Object[] values) {
        @SuppressWarnings("unchecked")
        <X> X get(int index) {
            return (X) values[index];
        }
    }

    private static final class Field<T> {
        final String label;
        final String hint;
        final int step;
        final Parser parser; // text field
        final Select<?> select; // related-record field
        final Function<T, Object> get;
        final BiConsumer<T, Object> set;
        Function<Object, Identifiable> owner; 
        Field(String label, String hint, int step, Parser parser, Select<?> select, Function<T, Object> get,
                BiConsumer<T, Object> set) {
            this.label = label;
            this.hint = hint;
            this.step = step;
            this.parser = parser;
            this.select = select;
            this.get = get;
            this.set = set;
        }
    }

    final String name;
    private final Class<T> type;
    private final Store store;
    private final List<String> steps = new ArrayList<>();
    private final List<Field<T>> fields = new ArrayList<>();
    private Function<Values, T> creator;
    private Rule<T> check = record -> {
    };
    private Rule<T> onCreate = record -> {
    };

    Form(Class<T> type, String name, Store store) {
        this.type = type;
        this.name = name;
        this.store = store;
    }

   

    Form<T> step(String title) {
        steps.add(title);
        return this;
    }

    Form<T> field(String label, String hint, Parser parser, Function<T, Object> get, BiConsumer<T, Object> set) {
        fields.add(new Field<>(label, hint, steps.size() - 1, parser, null, get, set));
        return this;
    }

    Form<T> choice(String label, List<String> options, Function<T, Object> get, BiConsumer<T, Object> set) {
        return field(label, optionsHint(options), (raw, field) -> pickOption(raw, field, options), get, set);
    }

    Form<T> yesNo(String label, Function<T, Object> get, BiConsumer<T, Object> set) {
        return field(label, optionsHint(YES_NO), (raw, field) -> "YES".equals(pickOption(raw, field, YES_NO)), get, set);
    }

    Form<T> select(String label, Select<?> select, Function<T, Object> get, BiConsumer<T, Object> set) {
        fields.add(new Field<>(label, select.hint(), steps.size() - 1, null, select, get, set));
        return this;
    }

    /** A value the system fills in itself (never typed by the user). */
    Form<T> derived(String label, Function<T, Object> get) {
        fields.add(new Field<>(label, "derived", steps.size() - 1, null, null, get, null));
        return this;
    }

    /** Marks the last declared field as unique among records of the given type. */
    <X extends Identifiable> Form<T> unique(Class<X> owners, Function<X, String> key) {
        fields.get(fields.size() - 1).owner = value -> store
                .find(owners, x -> key.apply(x).equalsIgnoreCase(String.valueOf(value))).orElse(null);
        return this;
    }

    Form<T> creator(Function<Values, T> creator) {
        this.creator = creator;
        return this;
    }

    Form<T> check(Rule<T> check) {
        this.check = check;
        return this;
    }

    /** Side effect run once, right before a new record is stored (e.g. stock decrement). */
    Form<T> onCreate(Rule<T> onCreate) {
        this.onCreate = onCreate;
        return this;
    }

    // ---- workflows ---------------------------------------------------------

    /** Runs the full registration; returns the saved record or null when cancelled. */
    T create() throws PrerequisiteMissingException {
        for (Field<T> f : fields) {
            if (f.select != null) {
                f.select.ensureAvailable(name);
            }
        }
        Object[] values = new Object[fields.size()];
        int step = 0;
        while (step < steps.size()) {
            Object result = fillStep(step, values);
            if (result != CANCEL) {
                step = result == BACK ? Math.max(0, step - 1) : step + 1;
            } else if (Console.confirm("Discard the unsaved " + name + "?")) {
                System.out.println("x " + name + " registration cancelled. Nothing was saved.");
                return null;
            }
        }
        return review(values, null);
    }

    /** Edits an existing record in place; only fields with a setter can change. */
    void update(Identifiable record) {
        T target = type.cast(record);
        review(read(target), target);
    }

    boolean handles(Identifiable record) {
        return type.isInstance(record);
    }

    private Object fillStep(int step, Object[] values) {
        header(step);
        for (int i = 0; i < fields.size(); i++) {
            Field<T> f = fields.get(i);
            if (f.step != step || (f.parser == null && f.select == null)) {
                continue;
            }
            Object value = ask(i, values[i], null);
            if (value == BACK || value == CANCEL) {
                return value;
            }
            values[i] = value;
        }
        return null;
    }

    private Object ask(int i, Object current, T self) {
        Field<T> f = fields.get(i);
        String keep = current == null ? "" : " (Enter keeps " + show(current) + ")";
        String prompt = String.format("%02d %-20s [%s]%s : ", i + 1, f.label, f.hint, keep);
        while (true) {
            Object value = f.select != null ? f.select.read(prompt, current) : readText(f, prompt, current);
            if (value == BACK || value == CANCEL) {
                return value;
            }
            try {
                checkUnique(f, value, self);
                return value;
            } catch (DuplicateRecordException e) {
                System.out.println("! " + e.getMessage() + " Please enter a different value.");
            }
        }
    }

    private static Object readText(Field<?> f, String prompt, Object current) {
        while (true) {
            String raw = Console.read(prompt);
            if (raw.equalsIgnoreCase("BACK")) {
                return BACK;
            }
            if (raw.equalsIgnoreCase("CANCEL")) {
                return CANCEL;
            }
            if (raw.isEmpty() && current != null) {
                return current;
            }
            try {
                return f.parser.parse(raw, f.label);
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("! '" + raw + "' is not a valid number for " + f.label + ".");
            } catch (DateTimeParseException e) {
                System.out.println("! Invalid date: use YYYY-MM-DD (example: 2004-05-16).");
            }
        }
    }

    private T review(Object[] values, T self) {
        while (true) {
            printReview(values, self);
            int choice = Console.choose("[1] Save " + name + "  [2] Edit field  [0] Cancel : ", 0, 2);
            if (choice == 2) {
                edit(values, self);
            } else if (choice == 0) {
                if (Console.confirm("Discard " + (self == null ? "the unsaved " + name : "these changes") + "?")) {
                    System.out.println("x Cancelled. Nothing was saved.");
                    return null;
                }
            } else {
                T saved = self == null ? save(values) : apply(values, self);
                if (saved != null) {
                    return saved;
                }
            }
        }
    }

    private void edit(Object[] values, T self) {
        int n = Console.choose("Field number to edit (01-" + fields.size() + ", 0 = back) : ", 0, fields.size());
        if (n == 0) {
            return;
        }
        Field<T> f = fields.get(n - 1);
        if (!editable(f, self)) {
            System.out.println("! " + f.label + " cannot be edited.");
            return;
        }
        System.out.println("Old value: " + show(values[n - 1]));
        Object value = ask(n - 1, values[n - 1], self);
        if (value != BACK && value != CANCEL) {
            values[n - 1] = value;
        }
    }

    private T save(Object[] values) {
        try {
            T record = creator.apply(new Values(values));
            record.validate();
            checkAllUnique(values, null);
            check.check(record);
            onCreate.check(record);
            store.add(record);
            System.out.println("+ " + name + " " + record.getId() + " registered successfully.");
            return record;
        } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
            System.out.println("! " + e.getMessage() + " Use [2] Edit field to correct it.");
            return null;
        }
    }

    private T apply(Object[] values, T record) {
        if (!Console.confirm("Apply these changes to " + record.getId() + "?")) {
            return null;
        }
        Object[] old = read(record);
        try {
            checkAllUnique(values, record);
            write(record, values);
            record.validate();
            check.check(record);
            System.out.println("+ " + name + " " + record.getId() + " updated successfully.");
            return record;
        } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
            write(record, old);
            System.out.println("! " + e.getMessage() + " Changes were not applied.");
            return null;
        }
    }

    private void checkUnique(Field<T> f, Object value, T self) throws DuplicateRecordException {
        Identifiable owner = f.owner == null || value == null ? null : f.owner.apply(value);
        if (owner != null && owner != self) {
            throw new DuplicateRecordException(f.label + " " + value + " already exists (" + owner.getId() + ").");
        }
    }

    private void checkAllUnique(Object[] values, T self) throws DuplicateRecordException {
        for (int i = 0; i < fields.size(); i++) {
            checkUnique(fields.get(i), values[i], self);
        }
    }

    private Object[] read(T record) {
        return fields.stream().map(f -> f.get.apply(record)).toArray();
    }

    private void write(T record, Object[] values) {
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).set != null) {
                fields.get(i).set.accept(record, values[i]);
            }
        }
    }

    private static boolean editable(Field<?> f, Object self) {
        boolean typed = f.parser != null || f.select != null;
        return typed && (self == null || f.set != null);
    }

    // ---- rendering ---------------------------------------------------------

    private void header(int step) {
        String line = "+" + "-".repeat(62) + "+";
        System.out.println("\n" + line);
        System.out.printf("| %-60s |%n", "AUCA_ID / SCHOOL MANAGEMENT");
        System.out.printf("| %-44s Step %d of %-3d |%n", name.toUpperCase() + " REGISTRATION", step + 1, steps.size() + 1);
        System.out.println(line);
        List<String> all = new ArrayList<>(steps);
        all.add("Review & confirm");
        for (int i = 0; i < all.size(); i += 2) {
            String left = "[" + (i + 1) + "] " + all.get(i);
            String right = i + 1 < all.size() ? "[" + (i + 2) + "] " + all.get(i + 1) : "";
            System.out.printf("| %-30s%-30s |%n", left, right);
        }
        System.out.println(line);
        System.out.printf("| %-60s |%n", "Enter the requested value. Type BACK to return;");
        System.out.printf("| %-60s |%n", "CANCEL to discard the unsaved " + name + ".");
        System.out.println(line);
        System.out.printf("[STEP %d/%d] %s%n", step + 1, steps.size() + 1, steps.get(step).toUpperCase());
    }

    private void printReview(Object[] values, T self) {
        Console.title("REVIEW " + name.toUpperCase());
        for (int i = 0; i < fields.size(); i++) {
            Field<T> f = fields.get(i);
            if (i == 0 || fields.get(i - 1).step != f.step) {
                System.out.println("  " + steps.get(f.step).toUpperCase());
            }
            String lock = editable(f, self) ? "" : "  (locked)";
            System.out.printf("  %02d %-20s: %s%s%n", i + 1, f.label, show(values[i]), lock);
        }
    }

    static String show(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof Boolean b) {
            return b ? "YES" : "NO";
        }
        if (value instanceof Identifiable record) {
            return Identifiable.label(record);
        }
        if (value instanceof BigDecimal amount) {
            return amount.toPlainString();
        }
        return value.toString();
    }

    private static String optionsHint(List<String> options) {
        List<String> numbered = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            numbered.add((i + 1) + " " + options.get(i));
        }
        return String.join(" / ", numbered);
    }

    /** Accepts either the option number or its exact name. */
    private static String pickOption(String raw, String field, List<String> options) throws ValidationException {
        String upper = raw.trim().toUpperCase();
        if (options.contains(upper)) {
            return upper;
        }
        try {
            return options.get(Integer.parseInt(upper) - 1);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new ValidationException(field + " must be one of: " + optionsHint(options) + ".");
        }
    }

    /** Free-text field limited to min-max characters. */
    static Parser text(int min, int max) {
        return (raw, field) -> Registrable.text(raw, field, min, max);
    }
}
