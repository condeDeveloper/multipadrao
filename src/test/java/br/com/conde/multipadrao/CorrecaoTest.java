package br.com.conde.multipadrao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A equivalencia com o juiz, e a prova de que o juiz tem dente.
 *
 * <p>Dizer "o automato concorda com a forca bruta em 55.987 textos" so vale alguma coisa se um
 * automato quebrado DISCORDAR. O ultimo teste quebra o automato de proposito, de tres jeitos
 * diferentes, e confere que a varredura pega os tres.
 */
class CorrecaoTest {

    private List<Automato.Ocorrencia> ordenado(List<Automato.Ocorrencia> achados) {
        List<Automato.Ocorrencia> copia = new ArrayList<>(achados);

        copia.sort(null);

        return copia;
    }

    @Test
    @DisplayName("zero divergencias em todos os 55.987 textos de ate seis letras")
    void zeroDivergencias() {
        Medidas.Correcao medida = Medidas.correcao();

        assertEquals(55_987, medida.textos());
        assertEquals(0, medida.divergencias());
        assertEquals(63_677, medida.ocorrencias());
    }

    @Test
    @DisplayName("e a equivalencia vale tambem num alfabeto binario, com outros padroes")
    void equivalenciaNoBinario() {
        List<String> padroes = List.of("aa", "aba", "b", "bab", "abab");

        Automato automato = new Automato(Alfabeto.BINARIO, padroes);
        ForcaBruta juiz = new ForcaBruta(padroes);

        int textos = 0;

        for (String texto : Alfabeto.BINARIO.todasAte(12)) {
            textos++;

            assertEquals(juiz.buscar(texto), ordenado(automato.buscar(texto)), texto);
        }

        assertEquals(8191, textos);
    }

    @Test
    @DisplayName("toda ocorrencia achada e mesmo o padrao, naquela posicao do texto")
    void ocorrenciaApontaParaOPadrao() {
        Automato automato = new Automato(Medidas.ALFABETO, Medidas.PADROES);

        for (String texto : Medidas.ALFABETO.todasAte(4))
            for (Automato.Ocorrencia achado : automato.buscar(texto)) {
                String padrao = Medidas.PADROES.get(achado.padrao());

                assertTrue(achado.onde() >= 0, texto);
                assertTrue(achado.onde() + padrao.length() <= texto.length(), texto);

                assertEquals(padrao,
                        texto.substring(achado.onde(), achado.onde() + padrao.length()), texto);
            }
    }

    @Test
    @DisplayName("O JUIZ PEGA: tres jeitos de quebrar o automato, e a varredura acha os tres")
    void juizPegaOAutomatoQuebrado() {
        /* 1. sem o elo de saida */
        assertTrue(Medidas.elos().textosErrados() > 2000);

        /* 2. elo de falha trocado pela raiz: quem falha volta ao comeco e perde o que ja leu */
        assertTrue(quantosErram(new Voltando(Medidas.ALFABETO, Medidas.PADROES)) > 0,
                "voltar a raiz tinha de perder ocorrencia");

        /* 3. busca que para no primeiro padrao achado em cada texto */
        assertTrue(quantosErram(new Preguicosa(Medidas.ALFABETO, Medidas.PADROES)) > 0,
                "parar no primeiro tinha de perder ocorrencia");
    }

    private int quantosErram(Busca quebrada) {
        ForcaBruta juiz = new ForcaBruta(Medidas.PADROES);

        int erram = 0;

        for (String texto : Medidas.ALFABETO.todasAte(5))
            if (!ordenado(quebrada.buscar(texto)).equals(juiz.buscar(texto))) erram++;

        return erram;
    }

    private interface Busca {
        List<Automato.Ocorrencia> buscar(String texto);
    }

    /** O erro de quem nao acredita no elo de falha: voltar ao comeco quando a letra nao bate. */
    private record Voltando(Alfabeto alfabeto, List<String> padroes) implements Busca {

        @Override
        public List<Automato.Ocorrencia> buscar(String texto) {
            List<Automato.Ocorrencia> achados = new ArrayList<>();

            int comeco = 0;

            while (comeco < texto.length()) {
                for (int indice = 0; indice < padroes.size(); indice++) {
                    String padrao = padroes.get(indice);

                    if (texto.startsWith(padrao, comeco))
                        achados.add(new Automato.Ocorrencia(indice, comeco));
                }

                /* Pula o padrao achado inteiro em vez de andar uma letra. */
                comeco += maiorAchado(texto, comeco);
            }

            return achados;
        }

        private int maiorAchado(String texto, int comeco) {
            int maior = 1;

            for (String padrao : padroes)
                if (texto.startsWith(padrao, comeco)) maior = Math.max(maior, padrao.length());

            return maior;
        }
    }

    /** O erro de quem acha que achar um padrao encerra o assunto. */
    private record Preguicosa(Alfabeto alfabeto, List<String> padroes) implements Busca {

        @Override
        public List<Automato.Ocorrencia> buscar(String texto) {
            List<Automato.Ocorrencia> tudo =
                    new ArrayList<>(new Automato(alfabeto, padroes).buscar(texto));

            tudo.sort(null);

            return tudo.isEmpty() ? tudo : tudo.subList(0, 1);
        }
    }
}
