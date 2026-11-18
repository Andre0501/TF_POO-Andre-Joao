# Guia explicado do projeto de resgate robótico

André Matheus
Guia de estudo e preparação individual

Este guia explica o raciocínio e os trechos principais do projeto completo. A referência de estilo são os materiais da disciplina: classes comuns, atributos privados, construtores, herança, interfaces, coleções, arquivos, exceções, streams e lambdas.

## Como o problema foi dividido

O enunciado descreve coisas que precisam existir: robô, área e missão. Cada uma virou uma classe. A central coordena essas coisas. A aplicação conversa com quem usa o programa. O relatório apresenta os resultados e a persistência guarda o estado em arquivo.

| Pergunta | Classe que responde |
| --- | --- |
| Qual é a bateria e o estado deste robô? | Robo. |
| Esse robô pode fazer esta missão? | Drone, RoboTerrestre ou RoboAnfibio. |
| Em que fase está a atividade? | Missao. |
| Quem está cadastrado e o que está pendente? | CentralResgate. |
| O que a pessoa digitou no menu? | Aplicacao. |
| Como guardar e recuperar tudo? | Persistencia. |

## Ordem sugerida de leitura do código

Comece pelas enumerações e AreaOperacao. Depois leia Robo e as três subclasses. Em seguida, acompanhe Missao e CentralResgate juntas. Por último, leia Relatorio, Persistencia e Aplicacao. Use Demonstracao para ligar os métodos a uma execução curta.

Os arquivos TestesSistema e TesteRetomada são apoio à verificação. Eles não são necessários para executar o menu, mas mostram casos que o sistema deve aceitar ou recusar.

# Objetos atributos e encapsulamento

Uma classe define quais informações e comportamentos os objetos terão. Cada novo Drone ou Missao é um objeto com seus próprios valores. O atributo id identifica aquele objeto no cadastro; a variável que guarda sua referência permite acessá-lo.

~~~java
private int nivelBateria;
private StatusRobo status;

public int consultarBateria() {
    return nivelBateria;
}
~~~

private impede que outra classe faça uma atribuição direta ao atributo. O método público oferece a consulta. Não há um setter livre para o status ou para a bateria: eles mudam por operações que verificam regras, como recarregar, reservar e concluir.

~~~java
if (nivelBateria < 0 || nivelBateria > 100) {
    throw new IllegalArgumentException(
            "A bateria deve estar entre 0 e 100.");
}
~~~

O construtor verifica antes de guardar o valor. A expressão || significa “ou”: um valor menor que zero ou maior que cem é inválido. throw interrompe a operação com uma exceção. O menu captura a exceção e mostra uma mensagem.

## Por que não validar apenas no menu

O teste e a demonstração também criam objetos sem passar pelo teclado. Se a regra estivesse somente no menu, esses caminhos poderiam produzir robôs inválidos. Validar no objeto protege todos os caminhos. O menu acrescenta uma ajuda: repete a pergunta quando o número está fora da faixa.

## Identificadores e referências

O mesmo texto AR1 não torna dois objetos diferentes a mesma área. Por isso registrarMissao confere se a referência de sua área é exatamente a cadastrada. A missão deve usar central.buscarArea("AR1"), preservando o vínculo com o objeto correto.

Os IDs não têm setters. Assim, a chave do mapa não fica diferente do id guardado dentro do objeto depois do cadastro.

# Herança classe abstrata e sobrescrita

Todos os robôs têm id, nome, bateria, status e contador de missões. Repetir esses campos em cada tipo dificultaria a manutenção. Robo guarda essa parte comum, e as subclasses a recebem por herança.

~~~java
public class Drone extends Robo {
    private int altitudeMaxima;

    public Drone(String id, String nome, int bateria,
            int altitudeMaxima) {
        super(id, nome, bateria);
        // Depois valida e guarda a altitude.
    }
}
~~~

extends diz que Drone é um tipo de Robo. super chama o construtor da superclasse para inicializar a parte comum. A altitude é a característica própria do drone. Nos outros tipos, a característica própria é a capacidade de carga.

~~~java
public abstract class Robo implements Comunicavel,
        Serializable {
    public abstract boolean podeExecutar(Missao missao);
    public abstract int calcularConsumo(Missao missao);
    public abstract void executarMissao(Missao missao);
}
~~~

Uma classe abstrata pode ter atributos, construtor e métodos implementados, mas não pode ser instanciada diretamente. O método abstrato termina com ponto e vírgula e não tem corpo. Cada subclasse concreta precisa fornecer sua implementação.

Recarregar é comum, portanto está implementado em Robo. Executar uma missão depende das capacidades, portanto é abstrato e é sobrescrito nas subclasses. @Override indica que o método está redefinindo um método herdado.

RoboAnfibio usa exatamente o mesmo mecanismo. Ele foi incluído como bônus para atender entregas em terreno alagado. Acrescentar uma subclasse não exige introduzir um novo conceito de linguagem.

# Polimorfismo e interface

~~~java
Robo robo = missao.getRoboResponsavel();
robo.executarMissao(missao);
~~~

A referência é do tipo Robo. O objeto real pode ser Drone, RoboTerrestre ou RoboAnfibio. Na chamada, Java executa o método sobrescrito da classe real. Isso é polimorfismo: a mesma chamada assume comportamentos específicos.

| Objeto real | Comportamento de executarMissao |
| --- | --- |
| Drone em reconhecimento | Monta um resultado de observação aérea. |
| Drone em localização | Monta um resultado de busca aérea simulada. |
| RoboTerrestre | Monta um resultado de entrega pelo solo. |
| RoboAnfibio | Monta um resultado de entrega anfíbia. |

A central não precisa verificar “se é drone” para decidir como executar. A subclasse responde pela execução. As verificações de compatibilidade e de consumo também são chamadas pelo tipo comum Robo.

~~~java
public interface Comunicavel {
    void enviarMensagem(String mensagem);
    void receberMensagem(String mensagem);
}
~~~

Uma interface declara operações que um objeto deve oferecer. Aqui, ela define a comunicação com a central. Robo implementa os dois métodos e as subclasses herdam essa implementação.

enviarMensagem significa robô para central; receberMensagem significa central para robô. Ambos registram data, sentido e texto e imprimem no console. A implementação não usa rede: é a comunicação simulada pedida no enunciado.

Comunicavel tem dois métodos abstratos e não é uma interface funcional. A interface AcaoTeste dos testes tem apenas um método abstrato e pode receber uma lambda, como VerificaPessoa no material da aula.

# Coleções e sequência da fila

~~~java
private Map<String, Robo> robos;
private Queue<Missao> missoesPendentes;
private List<String> historicoOperacoes;

robos = new HashMap<>();
missoesPendentes = new LinkedList<>();
historicoOperacoes = new ArrayList<>();
~~~

Map associa uma chave a um valor. Em robos, a chave é String e o valor é Robo. containsKey verifica duplicidade, put cadastra e get consulta. List guarda uma sequência de registros. Queue representa a fila de espera. LinkedList é a implementação usada para essa fila.

| Momento | Fila | D1 |
| --- | --- | --- |
| Registrar M1 e M2 | M1, M2 | Disponível. |
| Atribuir D1 a M1 | M1, M2 | Reservado; M1 ainda pendente. |
| Iniciar M1 | M2 | Em missão. |
| Concluir M1 | M2 | Disponível com bateria atualizada. |
| Cancelar M2 | Vazia | Sem alteração. |

offer insere no fim da fila. A remoção acontece depois de um início válido, ou no cancelamento. Retirar no momento de atribuir faria a fila deixar de representar todas as missões que ainda não começaram.

atenderProximaPendente percorre a fila em sua ordem e pega a primeira sem responsável. Se não existir candidato, deixa a missão e a ordem como estavam. Por isso não usa poll para retirar uma pendência antes de saber se poderá atendê-la.

A prioridade é registrada, mas não muda o FIFO automaticamente. O operador pode usar a prioridade para escolher uma missão manualmente. Essa escolha de comportamento está documentada e não deve ser confundida com a implementação de uma fila de prioridades.

A lista devolvida por uma consulta é nova. Limpar essa lista não limpa o mapa ou a fila da central. As referências aos objetos continuam compartilhadas para manter a operação consistente.

# Streams e lambdas no padrão da aula

~~~java
return listarRobos().stream()
        .filter(r -> r.verificarDisponibilidade())
        .collect(Collectors.toList());
~~~

listarRobos devolve uma lista. stream fornece uma sequência de elementos para processamento. A lambda r -> r.verificarDisponibilidade() recebe um robô e produz true ou false. filter conserva os que produziram true. collect monta a lista final.

Esse código corresponde a percorrer a lista com for, fazer um if de disponibilidade e adicionar os aprovados em outra lista. A stream descreve essa seleção de forma encadeada, como nos exemplos de pessoas e produtos.

~~~java
long concluidas = missoes.stream()
        .filter(m -> m.getStatus() == StatusMissao.CONCLUIDA)
        .count();

int total = robos.stream()
        .mapToInt(r -> r.getMissoesRealizadas())
        .sum();
~~~

No primeiro trecho, o filtro seleciona concluídas e count conta os elementos. O retorno é long. No segundo, mapToInt transforma cada robô em sua quantidade inteira de missões e sum soma esses valores.

~~~java
if (!robos.isEmpty()) {
    mediaBateria = robos.stream()
            .mapToInt(r -> r.consultarBateria())
            .average()
            .getAsDouble();
}
~~~

average calcula a média. getAsDouble obtém o valor quando a média existe. Uma lista vazia não tem média para obter; por isso o teste isEmpty vem antes. A central vazia recebe média zero por decisão documentada.

List.sort também recebe uma lambda. Ela devolve um número negativo, zero ou positivo para ordenar dois candidatos. A regra compara consumo; se empatar, bateria; se ainda empatar, id. Os exemplos da aula já utilizam sort com lambda e Integer.compare.

# Bateria compatibilidade e inovação

A compatibilidade responde se o tipo de robô consegue fazer a atividade, considerando tipo de missão, terreno e carga. A disponibilidade responde se ele está livre. Bateria suficiente é uma terceira verificação. Um robô pode estar livre e ainda não ter carga para uma missão específica.

~~~java
if (!podeExecutar(missao)) {
    throw new IllegalArgumentException("Robo incompativel.");
}
if (nivelBateria < calcularConsumo(missao)) {
    throw new IllegalStateException("Bateria insuficiente.");
}
~~~

O trecho ilustra as duas verificações usadas na reserva; as mensagens completas estão em Robo.java. Apenas depois de passar por todas as verificações a reserva é registrada. Assim, uma tentativa recusada não deixa o robô parcialmente ocupado.

| Exemplo | Cálculo | Bateria final |
| --- | --- | --- |
| Drone reconhece risco alto | 15 + 10 = 25 | 100 - 25 = 75%. |
| Drone busca em risco baixo | 25 + 0 = 25 | 100 - 25 = 75%. |
| Terrestre em irregular de risco alto | 20 + 10 + 10 = 40 | 100 - 40 = 60%. |
| Anfíbio em alagado de risco médio | 25 + 10 + 5 = 40 | 100 - 40 = 60%. |

A recomendação filtra candidatos disponíveis, compatíveis e com bateria suficiente. Depois compara consumos. Para M3, T1 gastará 20 e A1 gastará 25; T1 é sugerido. A tabela também mostra a bateria restante prevista.

A simulação não desconta nada e não reserva. Esses efeitos acontecem somente ao atribuir e finalizar uma operação. Separar consulta de alteração permite comparar várias opções sem modificar o estado.

A regra favorece menor gasto nesta missão. Ela não resolve um problema global de otimização de várias missões simultâneas. O critério simples é suficiente para a funcionalidade acadêmica e pode ser explicado e reproduzido.

# Persistência e referências entre objetos

~~~java
try (ObjectOutputStream saida = new ObjectOutputStream(
        new FileOutputStream(arquivo))) {
    saida.writeObject(central);
}
~~~

Serializar significa transformar o estado dos objetos em dados que podem ser gravados e depois reconstruídos. Serializable marca as classes que aceitam esse processo. ObjectOutputStream escreve os objetos e ObjectInputStream faz a leitura.

Não é necessário salvar separadamente cada mapa, robô e missão. Eles são alcançáveis a partir da central. A serialização percorre essas referências e conserva o compartilhamento: a área da missão recuperada é a mesma área que está no cadastro recuperado.

~~~java
Object objeto = entrada.readObject();
if (!(objeto instanceof CentralResgate)) {
    throw new IOException(
            "O arquivo nao contem uma CentralResgate.");
}
return (CentralResgate) objeto;
~~~

readObject devolve Object. instanceof verifica se o conteúdo é uma central. O cast, escrito como (CentralResgate), permite tratar esse objeto com o tipo correto depois da verificação. Um objeto de outra classe é recusado.

O bloco try com recurso fecha o arquivo automaticamente ao terminar, inclusive quando há erro. Esse formato também aparece nos exemplos de leitura e escrita de arquivos consultados.

Salvar uma missão em andamento mantém seu responsável, o consumo previsto, o estado do robô, a fila e as mensagens. Ao abrir novamente, executar conclui essa missão sem começar outra e sem descontar a bateria duas vezes. Isso foi verificado em processos separados.

O relatório em texto tem outra finalidade: ele é legível e resume a operação. Ele não é usado para reconstruir os objetos. O arquivo .dat é o estado serializado da aplicação.

A gravação direta é adequada à solução didática, mas não oferece proteção completa contra desligamento durante a escrita. Não altere os arquivos de dados manualmente. Se mudar a estrutura de serialização, use dados de demonstração compatíveis com essa versão.

# Exceções leitura do teclado e estados

Uma exceção indica que uma operação não pôde continuar normalmente. O projeto distingue argumentos inválidos, estado incompatível com a ação e problemas de arquivo.

| Exceção | Exemplo do projeto |
| --- | --- |
| IllegalArgumentException | Bateria fora da faixa, id duplicado, robô incompatível. |
| IllegalStateException | Iniciar sem responsável, recarregar robô ocupado, reexecutar concluída. |
| NumberFormatException | Texto como abc quando o menu pede um inteiro. |
| IOException | Arquivo ausente, inválido ou sem possibilidade de gravação. |
| ClassNotFoundException | Classe necessária para recuperar o arquivo não está disponível. |

~~~java
try {
    return Integer.parseInt(lerTexto(mensagem));
} catch (NumberFormatException e) {
    System.out.println("Digite um numero inteiro valido.");
}
~~~

lerTexto usa nextLine e remove os espaços externos. parseInt converte o texto em número. Se falhar, o método avisa e pergunta novamente. Ler sempre a linha inteira evita o problema de uma quebra de linha que sobra ao misturar nextInt e nextLine.

As mudanças de estado acontecem por métodos. atribuirRobo reserva; iniciar valida e muda para EM_ANDAMENTO; concluir desconta, conta e libera. O status não tem setter público porque alterar só o texto do estado deixaria bateria e vínculos incoerentes.

Se o risco aumenta entre reserva e início, a bateria é verificada de novo. Se faltar bateria, a operação é recusada e a missão permanece pendente com o robô reservado. O usuário pode retirar a atribuição, recarregar e atribuir novamente.

Depois que a missão começou, o consumo previsto fica fixado. Cancelar nesse momento desconta esse valor, mas não aumenta missoesRealizadas. Essa regra adicional evita tratar uma operação já iniciada como consumo zero.

# Percurso completo de uma missão

| Passo | Chamada e efeito |
| --- | --- |
| 1 Cadastro | DadosExemplo ou o menu cria robôs e áreas. A central armazena por id. |
| 2 Registro | registrarMissao valida a referência da área, põe no mapa e oferece na fila. |
| 3 Atribuição | Central chama Missao.atribuirRobo, que pede a reserva em Robo. |
| 4 Validações | Robo exige disponibilidade, compatibilidade e bateria suficiente. |
| 5 Início | Missao calcula a previsão e pede iniciarOperacao; a central remove da fila. |
| 6 Execução | Central obtém a referência Robo e chama executarMissao polimorficamente. |
| 7 Conclusão | A subclasse monta o resultado e chama Missao.concluir. |
| 8 Atualizações | O robô desconta bateria, aumenta o contador, limpa missão atual e muda de estado. |
| 9 Histórico | A missão guarda o resultado e o responsável; central e robô registram mensagens. |
| 10 Persistência | Salvar escreve toda a central para recuperar o estado em outra execução. |

## Como acompanhar no código

Abra CentralResgate.executarMissao. Ela inicia a missão se ainda estiver pendente e atribuída, e depois chama o robô. Abra Drone.executarMissao para ver o resultado e siga a chamada missao.concluir. Em seguida, abra Robo.finalizarOperacao para ver o desconto e a liberação.

## Valores que devem ser observados

Na M1 inicial: antes de atribuir, D1 está disponível com 100%. Depois de atribuir, está reservado. Depois de iniciar, está em missão com 100% e previsão de 25 pontos. Depois de concluir, está disponível com 75% e contador 1. M1 terminou e não pode ser reiniciada.

Nos testes 21, 22 e 26 esses pontos foram conferidos. O teste 40 verifica a mesma continuidade quando a gravação acontece entre o início e a conclusão.

# Preparação para a avaliação individual

| Pergunta provável | Ponto que deve aparecer na resposta |
| --- | --- |
| Por que Robo é abstrata? | Existe a parte comum, mas a execução depende de um tipo concreto. |
| Onde aparece polimorfismo? | Robo robo = ... e robo.executarMissao(missao) na central. |
| Qual a diferença entre interface e herança aqui? | Robo compartilha estado e código; Comunicavel define o contrato das mensagens. |
| Por que Map para o cadastro? | Consulta por id e verificação de chave duplicada. |
| Por que Queue para pendências? | Preservar a ordem de registro e manter espera sem perder missões. |
| Como impedir duas missões com o mesmo robô? | Reserva e missaoAtual; verificarDisponibilidade fica false. |
| Por que validar bateria outra vez no início? | O risco pode ter mudado depois da reserva. |
| Qual é a inovação? | Comparar consumo e bateria restante das alternativas e recomendar com desempates. |
| O que fica salvo? | A central e todo o conjunto alcançável: objetos, vínculos, fila, estados e históricos. |
| Para que servem os testes inválidos? | Comprovar que a regra bloqueia a operação e preserva o estado anterior. |

## Pequenas alterações para praticar

Mude o consumo de reconhecimento de 15 para 18 em Drone.calcularConsumo. Na missão M1 de risco alto, o custo passa a 28 e a bateria final passa a 72. Depois ajuste os testes que conferem 25 e 75 nesse cenário.

Outra prática: crie uma nova missão de transporte urbano com 60 kg e confirme que T1 e A1 são rejeitados pelas capacidades dos dados iniciais. Explique a diferença entre essa incompatibilidade e bateria insuficiente.

Uma terceira prática: coloque os drones em manutenção e tente atender a fila. Explique por que M1 permanece na frente e por que a central não perde a missão.

# Arquivos entregues e revisão para apresentar

O código-fonte é a implementação principal. O JAR facilita executar sem recompilar. O relatório final é o documento acadêmico, e este guia dá suporte ao estudo. As imagens UML e o arquivo PlantUML permitem revisar a modelagem.

## Como rodar e estudar

~~~java
java -jar ResgateRobotico.jar
java -cp bin Demonstracao
java -cp bin TestesSistema
java -cp bin TesteRetomada gravar
java -cp bin TesteRetomada retomar
~~~

Execute os dois últimos comandos separadamente: a segunda execução prova que os dados vieram do arquivo. Para compilar no seu computador, use o JDK e os scripts compilar.bat ou compilar.sh. O README contém o roteiro do menu.

## O que foi simplificado na modelagem

Os dados são fictícios; não há mapas ou sensores reais. O consumo usa fórmulas inteiras. Os resultados são determinísticos para facilitar a verificação. A fila é FIFO. A altitude do drone é informativa. A operação passa pela central para coordenar os objetos com as coleções. São decisões explícitas dentro da liberdade oferecida pelo enunciado.

## O que precisa ser compreendido pelos integrantes

Cada integrante deve conseguir explicar o vínculo entre Missao e Robo, acompanhar as mudanças de estado e modificar uma regra simples. Os métodos não foram repartidos em partes independentes para dispensar essa compreensão; a avaliação individual exige conhecer o conjunto.

Os nomes reais dos demais integrantes e do grupo devem ser acrescentados antes da submissão. O enunciado exige dupla ou trio e define a data pelo cronograma. A apresentação não é substituída pelos arquivos.

O uso de IA está declarado com os pedidos e a revisão realizada. Reproduza os testes no seu ambiente e confirme as explicações com o código, usando este guia como apoio.
