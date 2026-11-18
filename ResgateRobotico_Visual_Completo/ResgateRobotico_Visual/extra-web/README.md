# Camada visual extra

Extensão por hobby para André Matheus. As **21 classes originais em src/** foram preservadas. O servidor chama CentralResgate, Missao, Robo e suas subclasses para realizar as operações.

## Organização

| Caminho | Finalidade |
|---|---|
| src/ServidorWeb.java | Adaptador HTTP e representação JSON dos dados. |
| web/index.html | Estrutura da interface. |
| web/app.css | Identidade visual, temas e adaptação a telas menores. |
| web/app.js | Navegação, telas, formulários e chamadas à API. |
| web/scenario.js | Reprodução local dos cenários de treinamento. |
| web/*.webp | Imagens ilustrativas de terreno e protótipos da frota. |
| dados/central-web.dat | Estado da interface, criado na primeira execução. |
| tests/ | Testes e evidências da camada extra. |
| ../ResgateRoboticoVisual.jar | Interface e servidor compilados para Java 21. |
| ../iniciar-web.sh e ../iniciar-web.bat | Compilação e abertura do servidor. |
| ../.devcontainer/devcontainer.json | Ambiente Java 21 e encaminhamento da porta 8080. |

O arquivo dados/central.dat na raiz do projeto e o JAR ResgateRobotico.jar continuam pertencendo à aplicação de console.

## Telas e operações

| Tela | Operações |
|---|---|
| Visão geral | Mapa de treinamento, indicadores, fila, disponibilidade, áreas e atividades. |
| Missões | Cadastro, busca, filtros, candidatos, reserva, retirada, início, execução e cancelamento. |
| Frota | Cadastro, bateria, capacidade, recarga, manutenção e mensagens simuladas. |
| Áreas | Cadastro, terreno, risco, localização e missões vinculadas. |
| Relatórios | Distribuição dos estados, energia da frota, relatório integral e download TXT. |
| Atividade | Histórico da central com busca e categorias. |

Também há busca global, tema claro/escuro, navegação responsiva, mensagens de erro e salvamento automático.

## Cenários de treinamento

Em **Visão geral**, selecione Enchente, Deslizamento ou Tempestade. Use **Reproduzir**, pause ou vá a uma fase pela barra de evolução. **Impacto** alterna a camada de efeitos no mapa. As imagens aéreas e as posições são ilustrativas. Os marcadores abrem os cadastros da central e mostram o risco registrado pelo operador.

A reprodução ilustra uma sequência qualitativa em tempo acelerado. Ela não usa geolocalização, meteorologia em tempo real ou modelos de previsão. Escolher um cenário não envia comandos e não altera o risco cadastrado, a fila, o consumo ou o histórico.

Referências para a sequência visual: [inundações, NWS](https://www.weather.gov/safety/flood-hazards), [deslizamentos por chuva, USGS](https://www.usgs.gov/programs/landslide-hazards/science/overview-rainfall-induced-landslides) e [tempestades, NWS](https://www.weather.gov/safety/thunderstorm).

## Como a integração funciona

1. O navegador consulta GET /api/estado.
2. Um formulário ou botão envia POST /api/acao com os parâmetros.
3. ServidorWeb chama os métodos da CentralResgate original.
4. As classes Java validam a operação e alteram o estado.
5. O adaptador salva pela Persistencia original em arquivo temporário e substitui o arquivo final.
6. O navegador recebe o novo estado confirmado e atualiza as telas.

Os comandos são sincronizados no servidor. Uma falha de validação ou gravação restaura o estado em memória anterior ao comando. Se a conexão cair depois da confirmação, uma nova consulta recupera o estado já salvo.

O frontend não replica as fórmulas de consumo nem decide elegibilidade por conta própria. O endpoint de candidatos usa recomendarRobos, podeExecutar e calcularConsumo do Java.

## API

| Rota | Finalidade |
|---|---|
| GET /api/estado | Cadastros, fila, métricas, históricos e token da sessão. |
| GET /api/candidatos?id=M1 | Recomendação para missão pendente sem atribuição. |
| GET /api/relatorio | Texto produzido pela classe Relatorio. |
| GET /api/relatorio.txt | Download do relatório em UTF-8. |
| POST /api/acao | Operações via formulário URL-encoded e cabeçalho X-Resgate-Token. |

As respostas são JSON; as mensagens de validação vêm das regras do projeto. Os recursos estáticos servidos são limitados aos arquivos da interface.

## Compilar

Na raiz do projeto:

    bash compilar-web.sh

O script compila src/ e extra-web/src/ em extra-web/build, inclui os arquivos web no JAR e produz ResgateRoboticoVisual.jar.

O frontend usa HTML, CSS e JavaScript sem dependências externas. package.json existe para ferramentas de prévia: npm run dev chama o mesmo servidor Java.

## Verificação

Na entrega da primeira versão web, foram registrados os seguintes resultados:

- 50 testes originais de regras aprovados.
- 38 verificações de integração HTTP aprovadas.
- 38 verificações de navegação, formulários e estados da interface em DOM, sem erros de JavaScript.
- Retomada validada em outro processo Java, comparando cadastros, estados, vínculos, fila e históricos.
- Fluxos da interface conferidos no navegador.

As evidências estão em tests/. O teste abaixo **reinicia os dados da interface visual** e só deve ser usado em uma demonstração descartável:

    python3 extra-web/tests/verificar_api.py http://localhost:8080 --demo

O teste encerra restaurando os exemplos. --manter mantém o cenário final para conferir a retomada. Python é usado apenas por esse teste; não é necessário para executar o sistema.

Validação executada em Linux com Java 21. Os scripts de Windows foram preparados para o JDK 21 e não foram executados em Windows neste ambiente.

## Recursos preservados do trabalho

- Fila por ordem de registro; prioridade informativa.
- Reserva enquanto a missão ainda está pendente.
- Revalidação no início e custo fixado nesse momento.
- Consumo na conclusão ou cancelamento de missão já iniciada.
- Compatibilidade por tipo, terreno e capacidade.
- Polimorfismo na execução.
- Persistência dos objetos e suas referências.
