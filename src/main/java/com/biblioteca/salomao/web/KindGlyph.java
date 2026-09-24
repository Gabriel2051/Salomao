package com.biblioteca.salomao.web;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Glifo tipográfico por tipo de item do catálogo (identidade visual dos cards).
 * O mapeamento é por prefixo normalizado (sem acentos, minúsculo); tipos
 * personalizados caem no glifo padrão.
 */
public final class KindGlyph {

    private KindGlyph() {}

    private static final String PADRAO = "❖";
    private static final Map<String, String> MAPA = new LinkedHashMap<>();

    static {
        MAPA.put("arma de fogo", "⌖");
        MAPA.put("arma", "⚔");
        MAPA.put("armadura", "▣");
        MAPA.put("escudo", "▣");
        MAPA.put("traje", "◈");
        MAPA.put("acess", "◈");      // acessório
        MAPA.put("feitico", "✦");    // feitiço
        MAPA.put("magia", "✦");
        MAPA.put("pocao", "⚗");      // poção
        MAPA.put("consum", "⚗");     // consumível
        MAPA.put("artefato", "⌬");
        MAPA.put("reliquia", "✧");   // relíquia
        MAPA.put("material", "◆");
        MAPA.put("metal", "◆");
        MAPA.put("gema", "◆");
        MAPA.put("joia", "◆");       // joia
        MAPA.put("veiculo", "►");    // veículo
        MAPA.put("livro", "❧");
        MAPA.put("tomo", "❧");
        MAPA.put("ferramenta", "⚒");
        MAPA.put("instrumento", "♪");
        MAPA.put("criatura", "♞");
    }

    /** Glifo do tipo informado; nunca nulo. */
    public static String of(String kind) {
        if (kind == null || kind.isBlank()) return PADRAO;
        String k = Normalizer.normalize(kind.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        for (var e : MAPA.entrySet()) {
            if (k.startsWith(e.getKey())) return e.getValue();
        }
        return PADRAO;
    }
}
