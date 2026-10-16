package br.com.conde.multipadrao;

import java.util.ArrayList;
import java.util.List;

/**
 * O JUIZ: a busca burra, padrao por padrao, posicao por posicao.
 *
 * <p>Esta classe e devagar de proposito e nao tem um truque dentro. E exatamente por isso que
 * ela serve de juiz: nao ha elo de falha, nao ha tabela, nao ha nada que possa estar sutilmente
 * errado do mesmo jeito que o automato esta. Quando as duas concordam em todos os textos de um
 * alfabeto, a concordancia quer dizer alguma coisa.
 *
 * <p>O contador de comparacoes tambem e do juiz: e com ele que a quarta medida compara trabalho
 * de verdade em vez de repetir uma notacao.
 */
public final class ForcaBruta {

    private final List<String> padroes;

    private long comparacoes;

    public ForcaBruta(List<String> padroes) {
        this.padroes = List.copyOf(padroes);
    }

    public long comparacoes() {
        return comparacoes;
    }

    public void zerarContadores() {
        comparacoes = 0;
    }

    /** Todas as ocorrencias, na mesma ordem em que o automato as encontra. */
    public List<Automato.Ocorrencia> buscar(String texto) {
        List<Automato.Ocorrencia> achados = new ArrayList<>();

        for (int onde = 0; onde < texto.length(); onde++)
            for (int indice = 0; indice < padroes.size(); indice++) {
                String padrao = padroes.get(indice);

                if (onde + padrao.length() > texto.length()) continue;

                boolean bateu = true;

                for (int passo = 0; passo < padrao.length(); passo++) {
                    comparacoes++;

                    if (texto.charAt(onde + passo) != padrao.charAt(passo)) {
                        bateu = false;
                        break;
                    }
                }

                if (bateu) achados.add(new Automato.Ocorrencia(indice, onde));
            }

        /* O automato acha por posicao de FIM, e esta busca por posicao de COMECO. Como a
         * comparacao e entre conjuntos, as duas listas sao ordenadas do mesmo jeito antes de
         * se encontrarem: ordem diferente nao e resposta diferente. */
        achados.sort(null);

        return achados;
    }
}
