package com.github.pbetkier.spockdemo;

import com.github.pbetkier.spockdemo.model.SpockBox;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class DemoSpock {

    public SpockBox createBoxOfNum() {
        SpockBox box = new SpockBox();
        for (int i = 1; i <= 3; i++) {
            box.put(String.valueOf(i));
        }
        return box;
    }

}
