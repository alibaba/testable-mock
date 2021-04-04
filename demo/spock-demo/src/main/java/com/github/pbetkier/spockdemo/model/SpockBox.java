package com.github.pbetkier.spockdemo.model;

import java.util.ArrayList;
import java.util.List;

public class SpockBox {

    private List<String> contents = new ArrayList<>();

    public int size() {
        return contents.size();
    }

    public void put(String data) {
        contents.add(data);
    }

    public String pop() {
        if (contents.isEmpty()) {
            return null;
        }
        String data = contents.get(size() - 1);
        contents.remove(size() - 1);
        return data;
    }

}
