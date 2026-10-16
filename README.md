# Busca de vários padrões

Aho-Corasick do zero em Java 21: achar todos os padrões de uma vez, numa passada só no texto. E a busca burra do lado, como juiz, rodando em **todos** os textos de um alfabeto para que "funciona" deixe de ser opinião.

A frase que se repete é que o Aho-Corasick é **linear**. Com um texto de 64 letras e os padrões `a, aa, aaa, ...`, ele lê 64 letras e devolve **2.080 ocorrências**. A busca é linear; a resposta não é, e é a resposta que alguém vai ter de processar.

```
mvn -q exec:java -Dexec.args=ocorrencias
```

## O que é

O algoritmo tem três camadas, e cada uma existe por um motivo diferente.

**A árvore.** Os padrões viram caminhos de letras a partir de uma raiz. Quem lê o texto anda nessa árvore enquanto as letras baterem.

**O elo de falha.** Quando a letra não bate, não dá para voltar ao começo do texto: parte do que já foi lido ainda serve. O elo de falha de um estado aponta para o estado do maior sufixo próprio que também é prefixo de algum padrão. Seguir o elo não devolve nenhuma letra do texto.

**O elo de saída.** Esse é o que se esquece. Chegar num estado que termina um padrão não quer dizer que **só** um padrão terminou ali: dentro de `hers` termina `hers` e termina `s`; dentro de `she` termina `he`. O elo de saída é a corrente dos padrões que terminam na mesma posição.

O conjunto de padrões em todas as medidas é o do artigo de 1975, escolhido justamente porque tem padrão dentro de padrão: `he`, `she`, `his`, `hers`, `s`.

## As medidas

### 1. O autômato contra a força bruta

```
alfabeto            hersiu (6 letras)
textos varridos     55.987 (todos os de 0 a 6 letras)
ocorrências achadas 63.677
divergências        0
```

Não é amostra. Se existe um texto de até seis letras nesse alfabeto onde o autômato erra, ele está na varredura.

O juiz é a busca burra: padrão por padrão, posição por posição, sem truque dentro. É por não ter truque que ele serve de juiz, porque não pode estar errado do mesmo jeito que o autômato.

E o juiz tem dente. Três sabotagens diferentes, e a varredura pega as três: tirar o elo de saída, trocar o elo de falha por uma volta à raiz, e parar no primeiro padrão encontrado.

### 2. O que acontece sem o elo de saída

```
textos varridos        55.987
textos com erro        2.060  (3,7%)
ocorrências perdidas   2.091
pior texto             hishe, perde 2
```

Em `hishe` terminam quatro padrões: `his` em 0, `s` em 2, `she` em 2 e `he` em 3. Dois deles terminam na mesma posição, e é exatamente aí que o autômato sem elo de saída responde dois em vez de quatro.

A parte ruim não é o número, é o jeito: **a busca não quebra e não avisa**. Ela devolve uma lista plausível, mais curta do que deveria. O elo de falha sabe onde continuar; ele não sabe o que terminou.

### 3. Quanto trabalho cada um faz

Sobre os mesmos 55.987 textos, 324.726 letras ao todo:

| padrões | letras lidas | saltos de falha | comparações da força bruta |
|---:|---:|---:|---:|
| 1 | 324.726 | 271.590 | 313.530 |
| 2 | 324.726 | 233.384 | 567.660 |
| 3 | 324.726 | 228.010 | 821.790 |
| 4 | 324.726 | 227.280 | 1.009.824 |
| 5 | 324.726 | 227.280 | 1.334.550 |

A coluna das letras lidas não se mexe: o texto é lido uma vez, tenha o autômato um padrão ou cinco. A da força bruta multiplica.

Mas olhe a primeira linha. **Com um padrão só, o autômato trabalha mais**: 324.726 letras lidas contra 313.530 comparações. A vantagem começa no segundo padrão, não no primeiro, e isso está travado em teste.

### 4. O termo que some da frase "é linear"

| texto | padrões | ocorrências | letras lidas |
|---:|---:|---:|---:|
| 4 | 4 | 10 | 4 |
| 8 | 8 | 36 | 8 |
| 16 | 16 | 136 | 16 |
| 32 | 32 | 528 | 32 |
| 64 | 64 | 2.080 | 64 |

O custo é O(n + m + z), onde z é o número de ocorrências. Com os padrões `a, aa, aaa, ...` e o texto `aaaa...`, cada posição termina um padrão de cada tamanho, e z é exatamente n(n+1)/2. O teste confere a fórmula em todas as linhas.

O autômato continua lendo o texto uma vez. Quem explode é a resposta, não a busca, e é a resposta que vai ocupar memória e tempo de quem chamou.

### 5. O preço escondido da tabela de transições

| letras | estados | casas da tabela | letras de padrão |
|---:|---:|---:|---:|
| 2 | 5 | 10 | 4 |
| 8 | 17 | 136 | 16 |
| 14 | 29 | 406 | 28 |
| 20 | 41 | 820 | 40 |
| 26 | 53 | 1.378 | 52 |

Guardar a transição de cada estado para cada letra transforma a busca num acesso de tabela por letra lida, sem laço nenhum de falha em tempo de execução. É o que esta implementação faz. A conta é estados × letras, e no alfabeto de 26 ela gasta **26,5 casas para cada letra de padrão** que existe de verdade.

## O que as medidas me corrigiram

**"É linear" perdeu o z no caminho.** O algoritmo lê o texto uma vez, e isso é verdade. A saída dele, no caso ruim, é quadrática: 64 letras entram, 2.080 ocorrências saem. Quem chamou vai pagar por essas 2.080.

**O elo de falha não basta, e falhar sem ele é silencioso.** Eu esperava que faltar o elo de saída fosse um detalhe de completude. São 3,7% dos textos respondidos errado, sem exceção, sem aviso e com uma lista que parece certa.

**Com um padrão só, o autômato perde.** A primeira linha da terceira tabela contraria a propaganda do algoritmo: ele faz mais trabalho que a busca burra quando há um padrão apenas. O ganho vem de amortizar a leitura entre vários padrões, e com um só não há o que amortizar.

**Um número que eu chutei e o teste derrubou.** Escrevi que `hishe` tinha três ocorrências. São quatro: `his`, `s`, `she` e `he`. O teste caiu antes de o número chegar aqui, que é para isso que ele existe.

## Rodar

```bash
mvn -q verify                              # 25 testes
mvn -q exec:java                           # todas as medidas
mvn -q exec:java -Dexec.args=correcao      # 1
mvn -q exec:java -Dexec.args=elos          # 2
mvn -q exec:java -Dexec.args=custo         # 3
mvn -q exec:java -Dexec.args=ocorrencias   # 4
mvn -q exec:java -Dexec.args=alfabeto      # 5
```

Java 21, sem dependência fora do JUnit. Nenhum número deste README foi digitado à mão: todos saem de `Medidas`, e os testes conferem os que aparecem escritos aqui.
