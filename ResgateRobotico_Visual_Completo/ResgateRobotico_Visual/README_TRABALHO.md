# Sistema de Coordenação de Robôs para Missões de Resgate

Trabalho final de Programação Orientada a Objetos, 2026/02. Integrante identificado: André Matheus.

Aplicação de console em Java, com código no nível das aulas até streams e lambdas. O ZIP inclui código-fonte, programa compilado, dados fictícios iniciais, relatório final, UML, guia de estudo, testes e capturas reais da execução.

## Começar no Windows

1. Extraia o ZIP inteiro. Preserve as pastas `src`, `bin`, `dados`, `docs`, `evidencias` e `relatorios`.
2. Com Java 8 ou superior instalado, abra `executar.bat` ou execute `java -jar ResgateRobotico.jar` dentro da pasta extraída.
3. O arquivo `dados/central.dat` já contém quatro robôs, três áreas e quatro missões pendentes.
4. Use a opção 0 para salvar e sair. A opção 20 salva sem encerrar.

Para modificar e compilar o código, é necessário o **JDK**, que inclui `javac` e `jar`. Execute `compilar.bat`. No Linux ou macOS, use `sh compilar.sh` e `sh executar.sh`.

## Comandos manuais

```text
javac -encoding UTF-8 -d bin src/*.java
java -cp bin Aplicacao
java -cp bin TestesSistema
java -cp bin Demonstracao
java -cp bin TesteRetomada gravar
java -cp bin TesteRetomada retomar
```

O caractere separador na linha do `javac` pode ser escrito como `src\*.java` no Prompt de Comando do Windows. Os scripts já fazem isso.

Para começar em outro arquivo, sem alterar os exemplos entregues:

```text
java -jar ResgateRobotico.jar dados/minha_central.dat
```

Se esse arquivo ainda não existir, a aplicação cria uma central vazia. A opção 21 preenche exemplos somente quando a central está vazia. `DadosExemplo` também aceita um caminho de saída; ele grava o arquivo indicado, portanto use um nome novo ao gerar outra cópia.

## Demonstração pelo menu

| Etapa | Digitar | Resultado esperado nos dados iniciais |
|---|---|---|
| Consultar | 2 | Quatro robôs, todos disponíveis |
| Inovação | 14, M3 | T1 consome 20; A1 consome 25; sugestão T1 |
| Atribuir | 8, D1, M1 | D1 reservado para M1 |
| Iniciar | 9, M1 | M1 em andamento; D1 em missão |
| Executar | 10, M1 | M1 concluída; D1 com 75% e uma missão realizada |
| Erro de compatibilidade | 8, T1, M4 | Atribuição rejeitada; M4 permanece pendente |
| Erro de estado | 9, M1 | Reinício da missão concluída rejeitado |
| Bônus | 8, A1, M4; depois 10, M4 | Entrega no alagado; A1 com 60% |
| Arquivo de relatório | 19 | Resumo e arquivo `relatorios/relatorio_geral.txt` |
| Persistir | 0; abrir novamente | Cadastros, estados e resultados recuperados |

`Demonstracao` usa outra central em memória e não altera `dados/central.dat`. Ela gera `relatorios/relatorio_demonstracao.txt`.

## Arquivos principais

| Caminho | Conteúdo |
|---|---|
| `src` | Código Java comentado, incluindo testes e demonstração |
| `ResgateRobotico.jar` | Aplicação executável compilada para Java 8 |
| `dados/central.dat` | Estado inicial serializado para a apresentação |
| `docs/Relatorio_Final_ResgateRobotico.pdf` | Relatório acadêmico com todos os itens pedidos |
| `docs/Guia_Explicado_ResgateRobotico.pdf` | Explicação didática e preparação individual |
| `docs/*.md` | Textos editáveis da documentação |
| `docs/diagrama_classes.puml` | UML completo editável em PlantUML |
| `docs/uml_*.png` | UML em imagens para consulta |
| `evidencias` | Saídas dos testes, do menu, da retomada e capturas |
| `relatorios` | Relatórios em texto produzidos pelo próprio sistema |

## Regras adotadas

- IDs distinguem maiúsculas de minúsculas e perdem espaços externos no cadastro. A unicidade é por categoria.
- A fila usa a ordem de registro. A prioridade fica registrada para consulta e decisão manual; não reordena automaticamente a fila.
- Uma missão atribuída ainda é PENDENTE. Seu robô fica RESERVADO. Ao iniciar, ela sai da fila e o robô muda para EM_MISSAO.
- Se a primeira missão sem atribuição não tiver um robô adequado, ela fica na mesma posição. A atribuição manual pode atender outra missão.
- A bateria é revalidada no início. O consumo fica fixado nesse momento e é descontado na conclusão.
- Cancelar antes do início não consome bateria. Cancelar depois do início aplica toda a previsão e não aumenta o contador de concluídas.
- O anfíbio é um terceiro tipo de robô, como bônus. Ele transporta no urbano e no alagado.
- A simulação produz resultados fictícios e determinísticos, para que possam ser repetidos durante a demonstração.
- As operações da aplicação passam por `CentralResgate`, que mantém as coleções e a fila em conjunto com os objetos.

## Verificações realizadas nesta versão

Compilação com Eclipse ECJ 3.40.0, sintaxe e bytecode Java 8, e execução em OpenJDK 21: 50 testes de regras aprovados, seis testes do menu aprovados e retomada validada em dois processos Java separados. O JAR também foi aberto no terminal gráfico e as capturas constam em `evidencias/capturas`.

Os comandos de compilação dos scripts foram preparados para o JDK. Os testes desta entrega foram executados no Linux; os arquivos `.bat` devem ser abertos no Windows com o Java configurado no PATH.

## Antes da entrega à disciplina

O enunciado exige dupla ou trio e não informa a data exata de entrega. O material identifica André Matheus; acrescente o nome dos demais integrantes e o identificador do grupo quando estiverem definidos. Todos precisam estudar o código, reproduzir os testes e conseguir explicar a inovação. A apresentação e a avaliação individual são realizadas pelos integrantes.

O uso de ChatGPT/Codex está declarado no relatório, com os pedidos reais, a revisão de uma sugestão e os pontos que exigem estudo. Não se atribuem à equipe testes presenciais nem revisões que ainda não tenham sido realizados por ela.
