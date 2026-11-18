# Resgate — Central visual

Interface extra criada por hobby para André Matheus. O navegador opera as classes Java do projeto original.

## Rodar pelo GitHub Codespaces

1. Extraia o ZIP.
2. Coloque o **conteúdo** da pasta ResgateRobotico_Visual na raiz do repositório: as pastas src, extra-web e .devcontainer devem ficar junto dos arquivos iniciar-web.sh e README.md.
3. No GitHub, abra **Code → Codespaces → Create codespace on main**.
4. A configuração incluída prepara o Java 21. Se o Codespace já existia, use **Ctrl+Shift+P → Codespaces: Rebuild Container** para aplicar a configuração.
5. No terminal, dentro da pasta do projeto, execute:

    bash iniciar-web.sh

6. Abra **PORTS / PORTAS**, encontre **8080** e clique em **Open in Browser / Abrir no navegador**. Se a porta não aparecer, escolha **Add Port / Adicionar porta**, digite 8080 e abra pelo endereço encaminhado.
7. Deixe o terminal e o Codespace abertos enquanto estiver usando o sistema. Ctrl+C encerra o servidor; os comandos já concluídos foram salvos.

No Codespaces, use o endereço da aba PORTS. Digitar localhost:8080 no navegador do seu computador acessa o próprio computador, não o Codespace.

## Rodar no computador

Use **JDK 21 ou superior**.

- Windows: abra iniciar-web.bat e depois http://localhost:8080.
- Linux/macOS: execute bash iniciar-web.sh e abra http://localhost:8080.
- JAR pronto, com Java 21 ou superior:

    java -Duser.timezone=America/Sao_Paulo --add-modules jdk.httpserver -jar ResgateRoboticoVisual.jar

Não é necessário instalar Node, npm, React, banco de dados ou pacotes externos.

Outra porta, se 8080 já estiver em uso:

    bash iniciar-web.sh 8081

Outro arquivo de dados, independente:

    bash iniciar-web.sh 8080 extra-web/dados/minha-central.dat

Um arquivo novo começa com os dados de exemplo. Nas próximas aberturas, a central recupera o mesmo arquivo.

## Primeira missão pela interface

1. Em **Missões**, abra **M1 — Reconhecer a encosta**.
2. Compare os robôs e clique em **Reservar** na unidade **D1**.
3. Clique em **Iniciar missão**: M1 sai da fila, D1 entra em missão e a previsão fica fixada em 25 pontos.
4. Clique em **Executar e concluir**: M1 fica concluída e D1 termina com 75% de bateria.
5. Em **Frota de robôs**, abra D1 para consultar a unidade, recarregar, fazer manutenção ou trocar mensagens simuladas.
6. Em **Relatórios**, consulte os indicadores ou baixe o relatório em TXT.

O botão **Atender próxima** reserva o melhor robô para a primeira missão sem atribuição da fila. Depois abra a missão para iniciar e concluir.

## O que está disponível

- Painel com indicadores e atividades reais.
- Cadastro de robôs, áreas e missões.
- Busca global por Ctrl+K e filtros por página.
- Comparação de candidatos por consumo, bateria e compatibilidade.
- Reserva, retirada de reserva, início, execução e cancelamento.
- Recarga e início/finalização de manutenção.
- Atualização do risco das áreas.
- Instruções e respostas simuladas dos robôs.
- Relatórios com gráficos e download em TXT.
- Tema claro/escuro, navegação para telas menores e salvamento automático.

As missões são simulações do trabalho. O programa não se conecta a robôs físicos.

## Onde ficam os dados

- Versão visual: extra-web/dados/central-web.dat.
- Console original: dados/central.dat.

Os arquivos são independentes. A versão visual é salva depois de cada comando aceito. O botão **Salvar agora** também está disponível.

Em **Como operar a central**, há uma opção para reiniciar a demonstração. Ela exige digitar REINICIAR e substitui apenas os dados usados pela interface visual.

## Se algo não abrir

- “UnsupportedClassVersionError”: o Java usado é anterior ao 21. Confira com java -version e utilize o JDK 21.
- “Address already in use”: escolha outra porta e encaminhe essa mesma porta no Codespaces.
- A página perdeu conexão: confirme que o servidor continua aberto e clique em **Tentar novamente**.
- Arquivo de dados inválido: o servidor informa a falha e interrompe a abertura. Preserve o arquivo e experimente um novo caminho de dados, em vez de sobrescrevê-lo.

Referências oficiais: [Java HTTP Server](https://docs.oracle.com/en/java/javase/21/docs/api/jdk.httpserver/module-summary.html), [imagem Java para Dev Containers](https://github.com/devcontainers/images/blob/main/src/java/README.md) e [portas no Codespaces](https://docs.github.com/en/codespaces/developing-in-a-codespace/forwarding-ports-in-your-codespace).

## Código e documentação

As 21 classes originais em src/ foram preservadas. Consulte [extra-web/README.md](extra-web/README.md) para a arquitetura da interface e os testes, e [README_TRABALHO.md](README_TRABALHO.md) para o console original.
