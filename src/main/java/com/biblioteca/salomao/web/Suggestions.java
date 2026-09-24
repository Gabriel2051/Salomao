package com.biblioteca.salomao.web;

import java.util.List;

/**
 * Sugestões de preenchimento rápido para os formulários (<input list> + <datalist>).
 * São apenas atalhos: o usuário pode escolher uma opção OU digitar um valor próprio —
 * nada aqui é um enum fechado, então nenhuma criatividade fica limitada.
 */
public final class Suggestions {

    private Suggestions() {}

    public static final List<String> GENEROS = List.of(
            "Feminino", "Masculino", "Não-binário", "Agênero", "Gênero fluido", "Desconhecido");

    public static final List<String> ESPECIES = List.of(
            "Humano", "Elfo", "Meio-elfo", "Anão", "Orc", "Gnomo", "Halfling",
            "Vampiro", "Lobisomem", "Demônio", "Anjo", "Fada", "Draconiano",
            "Híbrido", "Espírito", "Morto-vivo", "Celestial", "Divindade", "Androide", "Constructo");

    public static final List<String> STATUS_PERSONAGEM = List.of(
            "Vivo", "Morto", "Desaparecido", "Desconhecido", "Lendário", "Aposentado");

    public static final List<String> OLHOS = List.of(
            "Castanhos", "Azuis", "Verdes", "Âmbar", "Cinzentos", "Pretos",
            "Vermelhos", "Dourados", "Violeta", "Brancos", "Heterocromia");

    public static final List<String> CABELOS = List.of(
            "Pretos", "Castanhos", "Loiros", "Ruivos", "Brancos", "Grisalhos", "Coloridos", "Careca");

    public static final List<String> CATEGORIAS_PODER = List.of(
            "Elemental", "Mágico", "Psíquico", "Físico", "Divino", "Sombrio", "Luminoso",
            "Tecnológico", "Maldição", "Bênção", "Transformação", "Invocação", "Ilusão", "Suporte");

    public static final List<String> NIVEIS_PODER = List.of(
            "Iniciante", "Aprendiz", "Intermediário", "Avançado", "Mestre",
            "Grão-mestre", "Lendário", "Supremo", "Ômega");

    public static final List<String> TIPOS_ITEM = List.of(
            "Arma", "Arma de fogo", "Armadura", "Traje", "Acessório", "Escudo",
            "Feitiço", "Artefato", "Relíquia", "Material", "Metal", "Gema", "Joia",
            "Consumível", "Poção", "Veículo", "Livro/Tomo", "Ferramenta", "Instrumento", "Outro");

    public static final List<String> RARIDADES = List.of(
            "Comum", "Incomum", "Raro", "Épico", "Lendário", "Mítico", "Único");
}
