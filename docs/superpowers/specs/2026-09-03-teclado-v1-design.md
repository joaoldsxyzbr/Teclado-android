# Teclado Android V1 — Design

## Objetivo
Criar a primeira versão de um teclado Android privado, em português do Brasil, com experiência visual inspirada no Gboard, mas com identidade própria e sem coleta de dados.

## Escopo do V1
- Android nativo em Kotlin.
- Implementação como `InputMethodService`.
- Layout QWERTY PT-BR.
- Letras, espaço, apagar, Enter e Shift.
- Acentos básicos do português.
- Tela de números e símbolos.
- Tema claro/escuro acompanhando o sistema.
- Teclas arredondadas e espaçamento visual semelhante ao padrão moderno de teclados Android.
- Sem autocorreção, sugestões ou swipe typing nesta versão.

## Privacidade
- Nenhuma permissão de internet.
- Nenhum SDK de analytics, anúncios ou telemetria.
- Nenhum login ou conta.
- Nenhum envio de texto digitado para servidores.
- O V1 não precisa persistir histórico de digitação.

## Arquitetura
### App shell
Uma Activity simples para explicar como ativar e selecionar o teclado nas configurações do Android.

### Serviço IME
`InputMethodService` será responsável por:
- criar a interface do teclado;
- receber eventos das teclas;
- enviar caracteres e comandos ao campo de texto ativo;
- alternar entre letras, símbolos e Shift.

### Camada de layout
Os layouts de teclas serão definidos de forma declarativa e separados da lógica do serviço. Isso facilitará futuras versões com sugestões, idiomas adicionais e temas.

### Estado do teclado
Um controlador pequeno manterá apenas estado transitório:
- Shift ligado/desligado;
- layout alfabético ou símbolos;
- estado necessário para composição de acentos.

## Fluxo de dados
1. O Android abre o IME para um campo de texto.
2. O serviço renderiza o layout apropriado.
3. O usuário toca uma tecla.
4. A ação é interpretada localmente.
5. O caractere ou comando é enviado diretamente ao `InputConnection` do Android.
6. Nenhum conteúdo digitado sai do dispositivo.

## Tratamento de erros
- Se não houver `InputConnection`, a ação da tecla é ignorada com segurança.
- Alternâncias de layout devem sempre retornar a um estado conhecido.
- Acentos incompletos não devem travar nem bloquear a digitação.

## Testes
- Testes unitários para mapeamento de teclas, Shift, símbolos e composição de acentos.
- Testes instrumentados básicos para inicialização do IME.
- Build debug via Gradle.
- GitHub Actions para validar build e testes a cada push/PR.

## Fora do escopo do V1
- autocorreção;
- sugestões preditivas;
- swipe typing;
- voz;
- emojis avançados;
- clipboard manager;
- sincronização;
- IA local.

## Critérios de sucesso
- O teclado pode ser ativado nas configurações do Android.
- Pode ser selecionado como método de entrada.
- Permite escrever texto normal em PT-BR com Shift, acentos, números e símbolos.
- Funciona em tema claro e escuro.
- O manifesto não solicita permissão de internet.
- O projeto compila e passa nos testes pelo GitHub Actions.
