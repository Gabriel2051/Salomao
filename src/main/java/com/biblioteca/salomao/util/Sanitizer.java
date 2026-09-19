package com.biblioteca.salomao.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Sanitizacao centralizada de HTML rico (editor) e de texto simples.
 * Todo conteudo vindo do usuario passa por aqui antes de persistir (anti-XSS).
 */
public final class Sanitizer {

    private static final Safelist RICH = Safelist.relaxed()
            .addTags("h1", "h2", "h3", "hr", "pre", "code", "span", "mark")
            .addAttributes("a", "href", "title", "target", "rel")
            .addAttributes("span", "class", "data-mention-id", "data-mention-type")
            .addProtocols("a", "href", "http", "https", "mailto")
            .removeTags("script", "style", "iframe", "object", "embed", "form", "input", "button");

    private Sanitizer() {}

    /** Sanitiza HTML do editor rico, preservando formatacao segura. */
    public static String richHtml(String input) {
        if (input == null) return "";
        String clean = Jsoup.clean(input, RICH);
        // forca links a abrirem em nova aba com rel seguro
        return clean.replace("<a ", "<a target=\"_blank\" rel=\"noopener noreferrer\" ");
    }

    /** Texto simples: remove todo HTML. */
    public static String text(String input) {
        if (input == null) return null;
        String s = Jsoup.clean(input, Safelist.none()).trim();
        return s.isEmpty() ? null : s;
    }

    /** Texto simples nunca-nulo (para campos opcionais). */
    public static String textOrEmpty(String input) {
        if (input == null) return "";
        return Jsoup.clean(input, Safelist.none()).trim();
    }

    /** Normaliza CSV de tags: minusculas, sem duplicadas, max 20. */
    public static String tags(String csv) {
        if (csv == null || csv.isBlank()) return "";
        String[] parts = csv.split(",");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        for (String p : parts) {
            String t = Jsoup.clean(p, Safelist.none()).trim().toLowerCase()
                    .replaceAll("[^\\p{L}\\p{N} _-]", "");
            if (!t.isBlank() && t.length() <= 40) set.add(t);
            if (set.size() >= 20) break;
        }
        return String.join(",", set);
    }
}
