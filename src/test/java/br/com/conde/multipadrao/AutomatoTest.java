package br.com.conde.multipadrao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * O exemplo do artigo de 1975, que e exemplo justamente porque tem padrao dentro de padrao:
 * em "ushers" terminam "she", "he", "hers" e "s", e dois deles terminam na mesma posicao.
 */
class AutomatoTest {

    private static final Alfabeto ALFA = new Alfabeto("hersiu");
    private static final List<String> PADROES = List.of("he", "she", "his", "hers", "s");

    private List<Automato.Ocorrencia> ordenado(List<Automato.Ocorrencia> achados) {
        List<Automato.Ocorrencia> copia = new ArrayList<>(achados);

        copia.sort(null);

        return copia;
    }

    @Test
    @DisplayName("acha os cinco padroes em ushers, na posicao certa")
    void exemploDoArtigo() {
        Automato automato = new Automato(ALFA, PADROES);

        assertEquals(List.of(new Automato.Ocorrencia(1, 1), new Automato.Ocorrencia(4, 1),
                        new Automato.Ocorrencia(0, 2), new Automato.Ocorrencia(3, 2),
                        new Automato.Ocorrencia(4, 5)),
                ordenado(automato.buscar("ushers")));
    }

    @Test
    @DisplayName("a arvore tem um estado por letra distinta de prefixo, mais a raiz")
    void quantosEstados() {
        /* he, she, his, hers, s: os prefixos distintos sao h, he, her, hers, s, sh, she,
         * hi, his, mais a raiz. */
        assertEquals(10, new Automato(ALFA, PADROES).estados());

        assertEquals(1, new Automato(ALFA, List.of()).estados());
        assertEquals(3, new Automato(ALFA, List.of("he")).estados());
    }

    @Test
    @DisplayName("SEM o elo de saida a busca perde padrao, e perde calada")
    void semEloDeSaidaPerde() {
        Automato manco = new Automato(ALFA, PADROES, false);

        List<Automato.Ocorrencia> achou = ordenado(manco.buscar("ushers"));
        List<Automato.Ocorrencia> devia = new ForcaBruta(PADROES).buscar("ushers");

        assertNotEquals(devia, achou);

        assertTrue(achou.size() < devia.size());

        /* Perde o "he" dentro do "she" e o "s" final: nada reclama. */
        assertFalse(achou.contains(new Automato.Ocorrencia(0, 2)));
        assertFalse(achou.contains(new Automato.Ocorrencia(4, 5)));
    }

    @Test
    @DisplayName("o texto e lido uma vez: uma letra lida por letra do texto")
    void leOTextoUmaVez() {
        Automato automato = new Automato(ALFA, PADROES);

        automato.zerarContadores();

        long letras = 0;

        for (String texto : ALFA.todasAte(4)) {
            automato.buscar(texto);

            letras += texto.length();
        }

        assertEquals(letras, automato.letrasLidas());
    }

    @Test
    @DisplayName("padrao vazio e lista vazia nao quebram a busca")
    void casosDegenerados() {
        assertEquals(List.of(), new Automato(ALFA, List.of()).buscar("hers"));
        assertEquals(List.of(), new Automato(ALFA, PADROES).buscar(""));
    }

    @Test
    @DisplayName("padrao repetido na lista nao inventa nem esconde ocorrencia")
    void padraoRepetido() {
        List<String> comRepetido = List.of("he", "he");

        Automato automato = new Automato(ALFA, comRepetido);

        assertEquals(1, automato.buscar("he").size());
    }

    @Test
    @DisplayName("o salto de falha so conta quando nao havia aresta propria")
    void contagemDeSaltos() {
        Automato automato = new Automato(ALFA, List.of("he"));

        automato.zerarContadores();

        /* "he" anda por aresta propria nas duas letras: nenhum salto. */
        automato.buscar("he");

        assertEquals(0, automato.saltosDeFalha());

        automato.zerarContadores();

        /* "uu" nao tem aresta nenhuma a partir da raiz: dois saltos. */
        automato.buscar("uu");

        assertEquals(2, automato.saltosDeFalha());
    }
}
