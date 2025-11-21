package com.userservise.app.utils;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class CardNumberGenerator {
    private static final Random random = new Random();

    public static String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10)); // добавляем случайную цифру 0-9
        }
        return sb.toString();
    }
}
