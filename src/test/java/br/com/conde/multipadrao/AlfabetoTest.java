package br.com.conde.multipadrao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O alfabeto e o que torna a varredura possivel, entao ele precisa enumerar certo: sem pular
 * palavra e sem repetir nenhuma. Se {@code todasAte} mentisse, toda medida deste repositorio
 * estaria medindo um pedaco do assunto e dizendo que mediu o assunto inteiro.
 */
class AlfabetoTest {

    @Test
    @DisplayName("alfabeto vazio ou com letra repetida nao existe")
    void alfabetoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Alfabeto(""));
        assertThrows(IllegalArgumentException.class, () -> new Alfabeto("aba"));
    }

    @Test
    @DisplayName("letra fora do alfabeto e erro, e nao uma busca que devolve nada")
    void letraDeFora() {
        assertThrows(IllegalArgumentException.class, () -> Alfabeto.BINARIO.indice('z'));
    }

    @Test
    @DisplayName("as palavras saem na ordem do alfabeto")
    void ordemDasPalavras() {
        assertEquals("", Alfabeto.BINARIO.palavra(0, 0));
        assertEquals("a", Alfabeto.BINARIO.palavra(1, 0));
        assertEquals("b", Alfabeto.BINARIO.palavra(1, 1));
        assertEquals("aa", Alfabeto.BINARIO.palavra(2, 0));
        assertEquals("ab", Alfabeto.BINARIO.palavra(2, 1));
        assertEquals("ba", Alfabeto.BINARIO.palavra(2, 2));
        assertEquals("bb", Alfabeto.BINARIO.palavra(2, 3));
    }

    @Test
    @DisplayName("a varredura nao pula nem repete palavra")
    void varreduraCompleta() {
        for (int teto = 0; teto <= 10; teto++) {
            List<String> palavras = Alfabeto.BINARIO.todasAte(teto);

            long esperado = (1L << (teto + 1)) - 1;

            assertEquals(esperado, palavras.size(), "teto " + teto);
            assertEquals(esperado, new HashSet<>(palavras).size(), "teto " + teto);

            for (String palavra : palavras) assertTrue(palavra.length() <= teto);
        }
    }

    @Test
    @DisplayName("o corpus medido tem 55.987 textos, que e a soma das potencias de seis")
    void corpusMedido() {
        long esperado = 0;

        for (int comprimento = 0; comprimento <= Medidas.TETO; comprimento++)
            esperado += Math.round(Math.pow(6, comprimento));

        assertEquals(55_987, esperado);
        assertEquals(55_987, Medidas.ALFABETO.todasAte(Medidas.TETO).size());
    }
}
