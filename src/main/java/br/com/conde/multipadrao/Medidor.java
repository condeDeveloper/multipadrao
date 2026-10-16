package br.com.conde.multipadrao;

import java.util.Locale;

/**
 * O MEDIDOR: roda as varreduras e imprime as tabelas que estao no README.
 *
 * <p>Nenhum numero do README foi digitado a mao.
 */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "elos" -> elos();
            case "custo" -> custo();
            case "ocorrencias" -> ocorrencias();
            case "alfabeto" -> alfabeto();
            default -> {
                correcao();
                elos();
                custo();
                ocorrencias();
                alfabeto();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. o automato contra a forca bruta");

        Medidas.Correcao medida = Medidas.correcao();

        System.out.printf("alfabeto            %s (%d letras)%n", Medidas.ALFABETO.letras(),
                Medidas.ALFABETO.tamanho());
        System.out.printf("padroes             %s%n", Medidas.PADROES);
        System.out.printf("textos varridos     %d (todos os de 0 a %d letras)%n",
                medida.textos(), Medidas.TETO);
        System.out.printf("ocorrencias achadas %d%n", medida.ocorrencias());
        System.out.printf("divergencias        %d%n", medida.divergencias());

        System.out.println();
        System.out.println("nao e amostra: se existe um texto deste tamanho onde o automato");
        System.out.println("erra, ele esta na varredura.");
    }

    static void elos() {
        titulo("2. o que acontece sem o elo de saida");

        Medidas.Elos medida = Medidas.elos();

        System.out.printf("textos varridos        %d%n", medida.textos());
        System.out.printf("textos com erro        %d  (%.1f%%)%n", medida.textosErrados(),
                100.0 * medida.textosErrados() / medida.textos());
        System.out.printf("ocorrencias perdidas   %d%n", medida.ocorrenciasPerdidas());
        System.out.printf("pior texto             %s, perde %d%n", medida.piorTexto(),
                medida.perdidasNoPior());

        System.out.println();
        System.out.println("a busca nao quebra nem avisa: ela responde menos do que existe.");
        System.out.println("o elo de falha sozinho acha onde continuar, nao o que terminou.");
    }

    static void custo() {
        titulo("3. quanto trabalho cada um faz");

        System.out.printf("sobre os mesmos %d textos, %d letras ao todo%n%n",
                Medidas.correcao().textos(), Medidas.letrasDoCorpus());

        System.out.println("padroes  letras lidas  saltos de falha  comparacoes da forca bruta");

        for (Medidas.Custo linha : Medidas.custo())
            System.out.printf("%7d  %12d  %15d  %26d%n", linha.padroes(), linha.letrasLidas(),
                    linha.saltosDeFalha(), linha.comparacoesDaForcaBruta());

        System.out.println();
        System.out.println("a coluna das letras lidas nao se mexe: o texto e lido uma vez,");
        System.out.println("tenha o automato um padrao ou cinco. a da forca bruta multiplica.");
    }

    static void ocorrencias() {
        titulo("4. o termo que some da frase o aho-corasick e linear");

        System.out.println("texto  padroes  ocorrencias  letras lidas");

        for (Medidas.Ocorrencias linha : Medidas.ocorrencias())
            System.out.printf("%5d  %7d  %11d  %12d%n", linha.texto(), linha.padroes(),
                    linha.achadas(), linha.letrasLidas());

        System.out.println();
        System.out.println("o custo e n mais m mais z, e z e o numero de ocorrencias. aqui z");
        System.out.println("cresce com o quadrado: quem explode e a resposta, nao a busca.");
    }

    static void alfabeto() {
        titulo("5. o preco escondido da tabela de transicoes");

        System.out.println("letras  estados  casas da tabela  letras de padrao");

        for (Medidas.Tabela linha : Medidas.alfabeto())
            System.out.printf("%6d  %7d  %15d  %16d%n", linha.letras(), linha.estados(),
                    linha.casasDaTabela(), linha.casasUsadas());

        System.out.println();
        System.out.println("a tabela e estados vezes letras, e cresce com o alfabeto mesmo");
        System.out.println("quando os padroes nao mudam de tamanho.");
    }
}
