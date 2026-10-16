package br.com.conde.multipadrao;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: tudo o que o repositorio afirma sai daqui, e nada sai de citacao.
 *
 * <p>O alfabeto e finito e os textos sao varridos todos ate um comprimento. Nao e amostra: se
 * existe um texto onde o automato erra, ele esta na varredura.
 */
public final class Medidas {

    private Medidas() {}

    /** O conjunto classico, o mesmo do artigo de 1975: um padrao dentro do outro. */
    public static final Alfabeto ALFABETO = new Alfabeto("hersiu");
    public static final List<String> PADROES = List.of("he", "she", "his", "hers", "s");

    public static final int TETO = 6;

    /* ===================== 1. correcao ===================== */

    public record Correcao(int textos, long ocorrencias, int divergencias) {}

    private static Correcao correcao;

    /**
     * O AUTOMATO CONTRA A FORCA BRUTA, EM TODOS OS TEXTOS DO ALFABETO.
     *
     * <p>O juiz nao tem truque dentro: ele compara padrao por padrao, posicao por posicao. Por
     * nao ter truque, ele nao pode estar errado do mesmo jeito que o automato.
     */
    public static Correcao correcao() {
        if (correcao != null) return correcao;

        Automato automato = new Automato(ALFABETO, PADROES);
        ForcaBruta juiz = new ForcaBruta(PADROES);

        int textos = 0;
        int divergencias = 0;

        long ocorrencias = 0;

        for (String texto : ALFABETO.todasAte(TETO)) {
            textos++;

            List<Automato.Ocorrencia> achou = new ArrayList<>(automato.buscar(texto));

            achou.sort(null);

            List<Automato.Ocorrencia> devia = juiz.buscar(texto);

            ocorrencias += devia.size();

            if (!achou.equals(devia)) divergencias++;
        }

        return correcao = new Correcao(textos, ocorrencias, divergencias);
    }

    /* ===================== 2. elos ===================== */

    public record Elos(int textos, int textosErrados, long ocorrenciasPerdidas,
            String piorTexto, int perdidasNoPior) {}

    private static Elos elos;

    /**
     * O QUE ACONTECE SEM O ELO DE SAIDA.
     *
     * <p>Chegar num estado que termina um padrao nao quer dizer que so aquele padrao terminou
     * ali. Dentro de "hers" termina "hers" e termina "s". Quem para no primeiro responde menos
     * do que existe, e responde sem reclamar: a busca nao quebra, so mente.
     */
    public static Elos elos() {
        if (elos != null) return elos;

        Automato manco = new Automato(ALFABETO, PADROES, false);
        ForcaBruta juiz = new ForcaBruta(PADROES);

        int textos = 0;
        int errados = 0;

        long perdidas = 0;

        String pior = "";
        int noPior = 0;

        for (String texto : ALFABETO.todasAte(TETO)) {
            textos++;

            List<Automato.Ocorrencia> achou = new ArrayList<>(manco.buscar(texto));

            achou.sort(null);

            List<Automato.Ocorrencia> devia = juiz.buscar(texto);

            if (achou.equals(devia)) continue;

            errados++;

            int faltou = devia.size() - achou.size();

            perdidas += faltou;

            if (faltou > noPior) {
                noPior = faltou;
                pior = texto;
            }
        }

        return elos = new Elos(textos, errados, perdidas, pior, noPior);
    }

    /* ===================== 3. custo ===================== */

    public record Custo(int padroes, long letrasLidas, long saltosDeFalha,
            long comparacoesDaForcaBruta) {}

    private static List<Custo> custo;

    /**
     * QUANTO TRABALHO CADA UM FAZ, CONTADO EM VEZ DE ESTIMADO.
     *
     * <p>A promessa do automato e que o texto e lido UMA vez, nao importa quantos padroes
     * existam. A forca bruta recomeca a cada padrao. A tabela mostra os dois numeros crescendo
     * lado a lado enquanto o conjunto de padroes cresce.
     */
    public static List<Custo> custo() {
        if (custo != null) return custo;

        List<Custo> saida = new ArrayList<>();

        List<String> acumulado = new ArrayList<>();

        for (String padrao : PADROES) {
            acumulado.add(padrao);

            Automato automato = new Automato(ALFABETO, acumulado);
            ForcaBruta juiz = new ForcaBruta(acumulado);

            for (String texto : ALFABETO.todasAte(TETO)) {
                automato.buscar(texto);
                juiz.buscar(texto);
            }

            saida.add(new Custo(acumulado.size(), automato.letrasLidas(),
                    automato.saltosDeFalha(), juiz.comparacoes()));
        }

        return custo = List.copyOf(saida);
    }

    /** Quantas letras os textos varridos tem ao todo. */
    public static long letrasDoCorpus() {
        long letras = 0;

        for (String texto : ALFABETO.todasAte(TETO)) letras += texto.length();

        return letras;
    }

    /* ===================== 4. ocorrencias ===================== */

    public record Ocorrencias(int texto, int padroes, long achadas, long letrasLidas) {}

    private static List<Ocorrencias> ocorrencias;

    /**
     * O TERMO QUE SOME DA FRASE "O AHO-CORASICK E LINEAR".
     *
     * <p>O custo e {@code O(n + m + z)}, onde z e o numero de ocorrencias. Com os padroes
     * {@code a, aa, aaa, ...} e o texto {@code aaaa...}, cada posicao termina um padrao de cada
     * tamanho, e z cresce com o quadrado de n. O automato continua lendo o texto uma vez: o que
     * explode e a RESPOSTA, nao a busca.
     */
    public static List<Ocorrencias> ocorrencias() {
        if (ocorrencias != null) return ocorrencias;

        Alfabeto umaLetra = new Alfabeto("a");

        List<Ocorrencias> saida = new ArrayList<>();

        for (int tamanho = 4; tamanho <= 64; tamanho *= 2) {
            List<String> padroes = new ArrayList<>();

            for (int passo = 1; passo <= tamanho; passo++) padroes.add("a".repeat(passo));

            Automato automato = new Automato(umaLetra, padroes);

            String texto = "a".repeat(tamanho);

            long achadas = automato.buscar(texto).size();

            saida.add(new Ocorrencias(tamanho, padroes.size(), achadas,
                    automato.letrasLidas()));
        }

        return ocorrencias = List.copyOf(saida);
    }

    /* ===================== 5. alfabeto ===================== */

    public record Tabela(int letras, int estados, long casasDaTabela, long casasUsadas) {}

    private static List<Tabela> alfabeto;

    /**
     * O PRECO ESCONDIDO DA VERSAO RAPIDA.
     *
     * <p>Guardar a transicao de cada estado para cada letra transforma a busca em um acesso de
     * tabela por letra lida, sem laco nenhum de falha. A conta e {@code estados x letras}, e ela
     * cresce com o alfabeto mesmo quando os padroes nao mudam de tamanho.
     */
    public static List<Tabela> alfabeto() {
        if (alfabeto != null) return alfabeto;

        String letras = "abcdefghijklmnopqrstuvwxyz";

        List<Tabela> saida = new ArrayList<>();

        for (int quantas = 2; quantas <= 26; quantas += 6) {
            Alfabeto alfa = new Alfabeto(letras.substring(0, quantas));

            /* Os mesmos padroes em todos: as duas primeiras letras, duas a duas. */
            List<String> padroes = new ArrayList<>();

            for (int primeira = 0; primeira < quantas; primeira++)
                padroes.add("" + alfa.letra(primeira) + alfa.letra((primeira + 1) % quantas));

            Automato automato = new Automato(alfa, padroes);

            long casas = (long) automato.estados() * quantas;

            saida.add(new Tabela(quantas, automato.estados(), casas,
                    padroes.stream().mapToLong(String::length).sum()));
        }

        return alfabeto = List.copyOf(saida);
    }
}
