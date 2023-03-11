package com.chemistry.android;

import java.util.ArrayList;
import java.util.List;

public class Chemistries {

    // Initialize chemistries list
    private static final List<Chemistry> chemistriesList = new ArrayList<>();

    // Add chemistries to list
    static {
        chemistriesList.add(new Chemistry("bff", "You're my best friend forever", "🥇", 1, 10000, true));
        chemistriesList.add(new Chemistry("clf", "We're close friends", "💛", 3, 1000, false));
        chemistriesList.add(new Chemistry("skp", "You're my no.1 secret keeper", "🤐", 1, 9000, true));
        chemistriesList.add(new Chemistry("inc", "You're my inner circle", "✊", 3, 500, false));
        chemistriesList.add(new Chemistry("nfx", "We share a Netflix password", "🔑", 1, 8000, true));
        chemistriesList.add(new Chemistry("kdr", "You're my kindred spirit", "👥", 3, 800, false));
        chemistriesList.add(new Chemistry("nsn", "I can never say no to you", "🐼", 1, 7000, true));
        chemistriesList.add(new Chemistry("mov", "We share a Netflix password", "🍿", 1, 6500, true));
        chemistriesList.add(new Chemistry("3am", "You're my 3am friend", "🚨", 1, 9500, true));
        chemistriesList.add(new Chemistry("drv", "I enjoy long drives with you", "🚘", 1, 8500, true));
        chemistriesList.add(new Chemistry("ilu", "I like you", "❤️‍🔥", 1, 6400, true));
        chemistriesList.add(new Chemistry("wrk", "You're my workout buddy", "💪", 5, 400, false));
        chemistriesList.add(new Chemistry("wng", "You're my greatest wingman", "🦋", 1, 450, false));
        chemistriesList.add(new Chemistry("sho", "We watch the same shows", "📺", 3, 425, false));
        chemistriesList.add(new Chemistry("bia", "We're brothers in arms'", "🙌", 5, 420, false));
        chemistriesList.add(new Chemistry("crk", "We play cricket together", "🏏", 5, 410, false));
        chemistriesList.add(new Chemistry("crt", "Your criticise to make be better", "🤨", 1, 300, false));
    }

    // Get chemistries list
    public static List<Chemistry> getChemistries() {
        return chemistriesList;
    }

    // Get name by key
    public static String getNameByKey(String key) {
        for (Chemistry chemistry : chemistriesList) {
            if (chemistry.getKey().equals(key)) {
                return chemistry.getName();
            }
        }
        return null;
    }

    // Get icon by key
    public static String getIconByKey(String key) {
        for (Chemistry chemistry : chemistriesList) {
            if (chemistry.getKey().equals(key)) {
                return chemistry.getIcon();
            }
        }
        return null;
    }

    // Get count by key
    public static int getCountByKey(String key) {
        for (Chemistry chemistry : chemistriesList) {
            if (chemistry.getKey().equals(key)) {
                return chemistry.getCount();
            }
        }
        return 0;
    }

    // Get weight by key
    public static int getWeightByKey(String key) {
        for (Chemistry chemistry : chemistriesList) {
            if (chemistry.getKey().equals(key)) {
                return chemistry.getWeight();
            }
        }
        return 0;
    }

    // Get isSpecial by key
    public static boolean getIsSpecialByKey(String key) {
        for (Chemistry chemistry : chemistriesList) {
            if (chemistry.getKey().equals(key)) {
                return chemistry.getIsSpecial();
            }
        }
        return false;
    }
}