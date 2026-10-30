package com.retailmanager.rmpayCalendar.admin.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reglas puras (sin estado ni dependencias) para comparar productos de Shopify y de RM.
 *  - Códigos: se quita el apóstrofo de Excel, los espacios y los ceros a la izquierda.
 *  - Nombres: Jaccard de palabras significativas, penalizando género y tamaño distintos.
 *    RM usa "MARCA - NOMBRE 3.4 OZ WOMAN" y Shopify "NOMBRE BY MARCA FOR HER".
 */
public final class MatchEngine {

    private static final Set<String> STOP = Set.of(
            "BY", "FOR", "THE", "DE", "LA", "LE", "AND", "OZ", "ML", "EDP", "EDT", "EAU", "PARFUM", "TOILETTE",
            "SPRAY", "HIM", "HER", "MAN", "WOMAN", "MEN", "WOMEN", "UNISEX", "HOMME", "FEMME", "POUR");
    private static final Pattern SIZE = Pattern.compile("(\\d+(?:[.,]\\d+)?)\\s*OZ");

    private MatchEngine() {
    }

    public static String normalizeCode(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        while (v.startsWith("'")) {
            v = v.substring(1).trim();
        }
        v = v.replace(" ", "");
        if (!v.isEmpty() && v.chars().allMatch(Character::isDigit)) {
            v = v.replaceFirst("^0+(?=.)", "");
        }
        return v.toUpperCase(Locale.ROOT);
    }

    public static Set<String> tokens(String text) {
        Set<String> result = new HashSet<>();
        if (text == null) {
            return result;
        }
        for (String t : text.toUpperCase(Locale.ROOT).split("[^A-Z0-9]+")) {
            if (t.length() < 2 || STOP.contains(t) || t.chars().allMatch(Character::isDigit)) {
                continue;
            }
            result.add(t);
        }
        return result;
    }

    /** M, F, U o null si no se puede determinar. */
    public static String gender(String text) {
        if (text == null) {
            return null;
        }
        Set<String> words = new HashSet<>(Arrays.asList(text.toUpperCase(Locale.ROOT).split("[^A-Z]+")));
        if (words.contains("UNISEX")) {
            return "U";
        }
        if (words.contains("HER") || words.contains("WOMAN") || words.contains("WOMEN") || words.contains("FEMME")) {
            return "F";
        }
        if (words.contains("HIM") || words.contains("MAN") || words.contains("MEN") || words.contains("HOMME")) {
            return "M";
        }
        return null;
    }

    /** Tamaño en onzas, o null. */
    public static Double size(String text) {
        if (text == null) {
            return null;
        }
        Matcher m = SIZE.matcher(text.toUpperCase(Locale.ROOT));
        if (m.find()) {
            try {
                return Double.parseDouble(m.group(1).replace(',', '.'));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Similitud entre 0 y 1.
     * @param shopifyText título del producto + título de la variante de Shopify
     * @param rmText descripción del producto en RM
     */
    public static double nameScore(String shopifyText, String rmText) {
        Set<String> a = tokens(shopifyText);
        Set<String> b = tokens(rmText);
        if (a.isEmpty() || b.isEmpty()) {
            return 0;
        }
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        double score = (double) inter.size() / union.size();

        String ga = gender(shopifyText);
        String gb = gender(rmText);
        if (ga != null && gb != null && !ga.equals(gb) && !"U".equals(ga) && !"U".equals(gb)) {
            score *= 0.5;
        }
        Double sa = size(shopifyText);
        Double sb = size(rmText);
        if (sa != null && sb != null) {
            score = Math.abs(sa - sb) < 0.05 ? Math.min(1, score + 0.1) : score * 0.6;
        }
        return Math.round(score * 100) / 100.0;
    }
}
