package com.auca.main;

import com.auca.exceptions.RecordNotFoundException;
import com.auca.interfaces.Identifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


final class Store {
    private final List<Identifiable> records = new ArrayList<>();

    void add(Identifiable record) {
        records.add(record);
    }

    <X> List<X> all(Class<X> type) {
        return records.stream().filter(type::isInstance).map(type::cast).toList();
    }

    <X> Optional<X> find(Class<X> type, Predicate<X> match) {
        return all(type).stream().filter(match).findFirst();
    }

    /** IDs are unique across all record types, so a single lookup is unambiguous. */
    Identifiable findById(String id) throws RecordNotFoundException {
        return find(Identifiable.class, r -> r.getId().equalsIgnoreCase(id))
                .orElseThrow(() -> new RecordNotFoundException("No record with ID '" + id + "' exists."));
    }
}
