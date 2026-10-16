package br.com.conde.multipadrao;

import java.util.ArrayList;
import java.util.List;

/**
 * O ALFABETO: as letras que podem aparecer, e quantas sao.
 *
 * <p>Fixar o alfabeto e o que torna este repositorio verificavel. Com duas letras, todos os
 * textos de ate 14 posicoes sao 32.767 palavras, e dai a comparacao com a forca bruta deixa de
 * ser amostra e vira varredura: nao ha texto que nao tenha sido testado.
 *
 * <p>Tambem e o alfabeto que decide o tamanho da tabela de transicoes, que e
 * {@code estados x letras}. Essa conta aparece na quinta medida, e e o preco escondido da
 * versao rapida do automato.
 */
public record Alfabeto(String letras) {

    public Alfabeto {
        if (letras.isEmpty()) throw new IllegalArgumentException("alfabeto vazio");

        if (letras.chars().distinct().count() != letras.length())
            throw new IllegalArgumentException("letra repetida no alfabeto: " + letras);
    }

    public static final Alfabeto BINARIO = new Alfabeto("ab");

    public int tamanho() {
        return letras.length();
    }

    public int indice(char letra) {
        int onde = letras.indexOf(letra);

        if (onde < 0) throw new IllegalArgumentException("letra fora do alfabeto: " + letra);

        return onde;
    }

    public char letra(int indice) {
        return letras.charAt(indice);
    }

    /** A palavra de numero dado entre as de certo comprimento, na ordem do alfabeto. */
    public String palavra(int comprimento, long numero) {
        StringBuilder saida = new StringBuilder();

        long resto = numero;

        for (int posicao = 0; posicao < comprimento; posicao++) {
            saida.append(letra((int) (resto % tamanho())));

            resto /= tamanho();
        }

        return saida.reverse().toString();
    }

    public long quantasPalavras(int comprimento) {
        long quantas = 1;

        for (int passo = 0; passo < comprimento; passo++) quantas *= tamanho();

        return quantas;
    }

    /** Todas as palavras de comprimento zero ate o teto, sem excecao e sem repeticao. */
    public List<String> todasAte(int teto) {
        List<String> palavras = new ArrayList<>();

        for (int comprimento = 0; comprimento <= teto; comprimento++)
            for (long numero = 0; numero < quantasPalavras(comprimento); numero++)
                palavras.add(palavra(comprimento, numero));

        return palavras;
    }
}
