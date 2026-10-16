package br.com.conde.multipadrao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros que aparecem escritos no README, travados em teste: se alguem mexer na busca e a
 * tabela mudar, estes testes caem antes de o texto virar mentira.
 */
class MedidasTest {

    /* ===================== 2. elos ===================== */

    @Test
    @DisplayName("sem o elo de saida, 2.060 dos 55.987 textos dao resposta errada")
    void semEloDeSaida() {
        Medidas.Elos medida = Medidas.elos();

        assertEquals(55_987, medida.textos());
        assertEquals(2_060, medida.textosErrados());
        assertEquals(2_091, medida.ocorrenciasPerdidas());
        assertEquals(2, medida.perdidasNoPior());

        assertTrue(100.0 * medida.textosErrados() / medida.textos() < 4);
    }

    @Test
    @DisplayName("e o pior texto e um onde dois padroes terminam na mesma posicao")
    void oPiorTexto() {
        String pior = Medidas.elos().piorTexto();

        assertEquals("hishe", pior);

        /* his em 0, s em 2, she em 2 e he em 3: quatro, e dois terminam na mesma posicao.
         * O automato sem elo de saida acha dois deles e nao avisa que faltou. */
        assertEquals(4, new ForcaBruta(Medidas.PADROES).buscar(pior).size());
        assertEquals(2, new Automato(Medidas.ALFABETO, Medidas.PADROES, false)
                .buscar(pior).size());
    }

    /* ===================== 3. custo ===================== */

    @Test
    @DisplayName("as letras lidas nao mudam com o numero de padroes")
    void letrasLidasNaoMudam() {
        List<Medidas.Custo> tabela = Medidas.custo();

        assertEquals(5, tabela.size());

        long letras = Medidas.letrasDoCorpus();

        assertEquals(324_726, letras);

        for (Medidas.Custo linha : tabela)
            assertEquals(letras, linha.letrasLidas(), "com " + linha.padroes() + " padroes");
    }

    @Test
    @DisplayName("as comparacoes da forca bruta crescem com cada padrao novo")
    void forcaBrutaMultiplica() {
        List<Medidas.Custo> tabela = Medidas.custo();

        assertEquals(313_530, tabela.get(0).comparacoesDaForcaBruta());
        assertEquals(1_334_550, tabela.get(4).comparacoesDaForcaBruta());

        for (int linha = 1; linha < tabela.size(); linha++)
            assertTrue(tabela.get(linha).comparacoesDaForcaBruta()
                            > tabela.get(linha - 1).comparacoesDaForcaBruta(),
                    "linha " + linha);
    }

    @Test
    @DisplayName("COM UM PADRAO SO o automato trabalha MAIS que a forca bruta")
    void comUmPadraoSoOAutomatoPerde() {
        /* O numero que contraria a propaganda: 324.726 letras lidas contra 313.530
         * comparacoes. A vantagem comeca no segundo padrao, nao no primeiro. */
        Medidas.Custo umPadrao = Medidas.custo().get(0);

        assertTrue(umPadrao.letrasLidas() > umPadrao.comparacoesDaForcaBruta(),
                umPadrao.letrasLidas() + " contra " + umPadrao.comparacoesDaForcaBruta());

        Medidas.Custo doisPadroes = Medidas.custo().get(1);

        assertTrue(doisPadroes.letrasLidas() < doisPadroes.comparacoesDaForcaBruta());
    }

    /* ===================== 4. ocorrencias ===================== */

    @Test
    @DisplayName("a resposta cresce com o quadrado do texto, e e exatamente n(n+1)/2")
    void respostaQuadratica() {
        for (Medidas.Ocorrencias linha : Medidas.ocorrencias()) {
            long n = linha.texto();

            assertEquals(n * (n + 1) / 2, linha.achadas(), "texto de " + n);
            assertEquals(n, linha.letrasLidas(), "texto de " + n);
        }
    }

    @Test
    @DisplayName("num texto de 64 letras saem 2.080 ocorrencias de 64 letras lidas")
    void sessentaEQuatro() {
        Medidas.Ocorrencias ultima = Medidas.ocorrencias()
                .get(Medidas.ocorrencias().size() - 1);

        assertEquals(64, ultima.texto());
        assertEquals(2_080, ultima.achadas());
        assertEquals(64, ultima.letrasLidas());

        assertTrue(ultima.achadas() > 30 * ultima.letrasLidas());
    }

    /* ===================== 5. alfabeto ===================== */

    @Test
    @DisplayName("a tabela e estados vezes letras, e so uma fracao dela e usada")
    void tabelaDensa() {
        for (Medidas.Tabela linha : Medidas.alfabeto())
            assertEquals((long) linha.estados() * linha.letras(), linha.casasDaTabela(),
                    "alfabeto de " + linha.letras());

        Medidas.Tabela maior = Medidas.alfabeto().get(Medidas.alfabeto().size() - 1);

        assertEquals(26, maior.letras());
        assertEquals(53, maior.estados());
        assertEquals(1_378, maior.casasDaTabela());
        assertEquals(52, maior.casasUsadas());

        assertEquals(26.5, maior.casasDaTabela() / (double) maior.casasUsadas(), 1e-9);
    }

    /* ===================== 1. correcao ===================== */

    @Test
    @DisplayName("o corpus medido tem 63.677 ocorrencias ao todo")
    void ocorrenciasDoCorpus() {
        assertEquals(63_677, Medidas.correcao().ocorrencias());
        assertEquals(0, Medidas.correcao().divergencias());
    }
}
