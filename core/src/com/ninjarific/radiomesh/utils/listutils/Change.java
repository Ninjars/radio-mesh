package com.ninjarific.radiomesh.utils.listutils;

public class Change<T> {
    public enum Type {
        ADD,
        REMOVE,
        UPDATE,
    }

    private final Type type;
    private final T value;

    public Change(Type type, T value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public T getValue() {
        return value;
    }
}
