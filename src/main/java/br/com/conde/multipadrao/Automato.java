package br.com.conde.multipadrao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * O AUTOMATO DE AHO-CORASICK: acha todos os padroes de uma vez, numa passada so no texto.
 *
 * <p>A ideia tem tres camadas, e cada uma existe por um motivo diferente:
 *
 * <ol>
 *   <li>A ARVORE. Os padroes viram um caminho de letras a partir da raiz. Quem le o texto anda
 *       nessa arvore enquanto as letras baterem.
 *   <li>O ELO DE FALHA. Quando a letra nao bate, nao da para voltar ao comeco do texto: parte
 *       do que foi lido ainda serve. O elo de falha de um estado aponta para o estado do MAIOR
 *       sufixo proprio que tambem e prefixo de algum padrao. Seguir o elo custa nada e nunca
 *       devolve uma letra do texto.
 *   <li>O ELO DE SAIDA. Esse e o que se esquece. Chegar num estado que termina um padrao nao
 *       quer dizer que so um padrao terminou ali: dentro de "hers" termina "hers" e termina
 *       "s", e dentro de "she" termina "he". O elo de saida e a corrente de padroes que
 *       terminam na mesma posicao, e sem ela o automato responde menos do que existe.
 * </ol>
 *
 * <p>Os contadores de {@link #letrasLidas()} e {@link #saltosDeFalha()} nao servem ao
 * algoritmo: existem so para as medidas poderem contar trabalho de verdade em vez de citar
 * uma notacao assintotica.
 */
public final class Automato {

    private final Alfabeto alfabeto;
    private final List<String> padroes;

    /** transicao[estado][letra], ja com os elos de falha embutidos. */
    private int[][] transicao;

    /** A arvore crua, antes de os elos de falha preencherem os buracos. Serve so para as
     * medidas saberem quando a busca andou por aresta propria e quando caiu por falha. */
    private int[][] arvore;

    private int[] falha;
    private int[] saida;

    /** Qual padrao termina exatamente neste estado, ou -1. */
    private int[] padraoQueTermina;

    private int estados;

    private long letrasLidas;
    private long saltosDeFalha;

    /** Se falso, o automato ignora o elo de saida: e o erro classico, de proposito. */
    private final boolean comElosDeSaida;

    public Automato(Alfabeto alfabeto, List<String> padroes) {
        this(alfabeto, padroes, true);
    }

    public Automato(Alfabeto alfabeto, List<String> padroes, boolean comElosDeSaida) {
        this.alfabeto = alfabeto;
        this.padroes = List.copyOf(padroes);
        this.comElosDeSaida = comElosDeSaida;

        montar();
    }

    public Alfabeto alfabeto() {
        return alfabeto;
    }

    public List<String> padroes() {
        return padroes;
    }

    public int estados() {
        return estados;
    }

    public long letrasLidas() {
        return letrasLidas;
    }

    public long saltosDeFalha() {
        return saltosDeFalha;
    }

    /* ===================== a montagem ===================== */

    private void montar() {
        int teto = 1;

        for (String padrao : padroes) teto += padrao.length();

        transicao = new int[teto][alfabeto.tamanho()];
        falha = new int[teto];
        saida = new int[teto];
        padraoQueTermina = new int[teto];

        for (int[] linha : transicao) java.util.Arrays.fill(linha, -1);

        java.util.Arrays.fill(padraoQueTermina, -1);
        java.util.Arrays.fill(saida, -1);

        estados = 1;

        /* 1. a arvore dos padroes */
        for (int indice = 0; indice < padroes.size(); indice++) {
            int atual = 0;

            for (char letra : padroes.get(indice).toCharArray()) {
                int coluna = alfabeto.indice(letra);

                if (transicao[atual][coluna] < 0) transicao[atual][coluna] = estados++;

                atual = transicao[atual][coluna];
            }

            /* Dois padroes iguais na lista: o primeiro fica. A medida conta padroes
             * distintos, entao isso nunca esconde ocorrencia. */
            if (padraoQueTermina[atual] < 0) padraoQueTermina[atual] = indice;
        }

        arvore = new int[estados][alfabeto.tamanho()];

        for (int estado = 0; estado < estados; estado++)
            arvore[estado] = transicao[estado].clone();

        /* 2. os elos de falha, em largura: o elo de um estado so pode ser calculado depois
         * do elo do pai, porque ele e uma caminhada a partir dali. */
        Deque<Integer> fila = new ArrayDeque<>();

        for (int letra = 0; letra < alfabeto.tamanho(); letra++) {
            int filho = transicao[0][letra];

            if (filho < 0) {
                transicao[0][letra] = 0;
                continue;
            }


            falha[filho] = 0;
            fila.add(filho);
        }

        while (!fila.isEmpty()) {
            int atual = fila.poll();

            /* 3. o elo de saida: a corrente dos padroes que terminam aqui dentro. Se o
             * estado do elo de falha termina um padrao, ele e o proximo da corrente; se nao,
             * a corrente dele ja e a minha. */
            int destino = falha[atual];

            saida[atual] = padraoQueTermina[destino] >= 0 ? destino : saida[destino];

            for (int letra = 0; letra < alfabeto.tamanho(); letra++) {
                int filho = transicao[atual][letra];

                if (filho < 0) {
                    /* Sem aresta: a transicao vira a do elo de falha, ja calculada. Isso e o
                     * que troca o laco de falhas em tempo de busca por uma tabela. */
                    transicao[atual][letra] = transicao[falha[atual]][letra];
                    continue;
                }

                falha[filho] = transicao[falha[atual]][letra];

                fila.add(filho);
            }
        }
    }

    /* ===================== a busca ===================== */

    /** Um padrao achado, e onde ele comeca no texto. */
    public record Ocorrencia(int padrao, int onde) implements Comparable<Ocorrencia> {

        @Override
        public int compareTo(Ocorrencia outra) {
            return onde != outra.onde ? Integer.compare(onde, outra.onde)
                    : Integer.compare(padrao, outra.padrao);
        }
    }

    /** Uma passada so no texto, achando todos os padroes. */
    public List<Ocorrencia> buscar(String texto) {
        List<Ocorrencia> achados = new ArrayList<>();

        int atual = 0;

        for (int posicao = 0; posicao < texto.length(); posicao++) {
            letrasLidas++;

            int letra = alfabeto.indice(texto.charAt(posicao));

            /* Nao havia aresta propria: a transicao veio de um elo de falha. */
            if (arvore[atual][letra] < 0) saltosDeFalha++;

            atual = transicao[atual][letra];

            if (padraoQueTermina[atual] >= 0) anotar(achados, atual, posicao);

            if (!comElosDeSaida) continue;

            for (int elo = saida[atual]; elo >= 0; elo = saida[elo])
                anotar(achados, elo, posicao);
        }

        return achados;
    }

    private void anotar(List<Ocorrencia> achados, int estado, int posicao) {
        int indice = padraoQueTermina[estado];

        achados.add(new Ocorrencia(indice, posicao - padroes.get(indice).length() + 1));
    }

    public void zerarContadores() {
        letrasLidas = 0;
        saltosDeFalha = 0;
    }
}
