package com.chemistry.android;

import java.util.Random;

public class UtilSolve {

    public static String shuffle(String name) {

        // Remove spaces and convert to lowercase
        name = name.replaceAll("\\s", "").toLowerCase();

        // Remove vowels
        name = name.replaceAll("[aeiou]", "");

        // Convert the remaining characters to a character array
        char[] chars = name.toCharArray();

        // Shuffle the character array using Fisher-Yates algorithm
        Random rand = new Random();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = rand.nextInt(i + 1);
            char temp = chars[j];
            chars[j] = chars[i];
            chars[i] = temp;
        }

        // Return the shuffled string
        return new String(chars);
    }
}
