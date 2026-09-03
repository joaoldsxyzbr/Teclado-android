# Teclado joaoldsxyzbr — Atualizador pelo GitHub

Data: 2026-09-03
Status: aprovado para planejamento

## Objetivo

Adicionar à tela principal do app um botão de atualização que consulte a Release mais recente do repositório `joaoldsxyzbr/Teclado-android`, compare a versão publicada com a versão instalada, baixe o APK oficial quando houver uma versão nova, valide a integridade do arquivo e abra o instalador padrão do Android.

A experiência será de um toque: `Verificar atualização` executa verificação, download e validação automaticamente quando houver uma versão nova. A única etapa manual obrigatória continuará sendo a confirmação do Package Installer do Android e, quando necessário, a autorização “Instalar apps desconhecidos” para este app.

## Escopo

O recurso será acionado manualmente pelo botão `Verificar atualização`. Não haverá serviço em segundo plano, polling, notificações automáticas nem telemetria.

A fonte de atualização será exclusivamente a GitHub Release mais recente do próprio repositório. O APK esperado seguirá o padrão já usado pelo CI:

`teclado-joaoldsxyzbr-v<versão>.apk`

## Arquitetura

O atualizador ficará isolado do `KeyboardService`. Nenhum texto digitado, estado do teclado ou conteúdo de `InputConnection` será acessível ao código de atualização.

Componentes planejados:

- `UpdateChecker`: consulta `https://api.github.com/repos/joaoldsxyzbr/Teclado-android/releases/latest`, extrai `tag_name`, asset APK, tamanho e digest quando disponível.
- `VersionComparator`: compara semanticamente a versão instalada com a tag da Release, removendo o prefixo `v`.
- `ApkDownloader`: baixa o APK para armazenamento privado temporário do app.
- `ChecksumVerifier`: valida SHA-256 usando o digest publicado pela API do GitHub. Se a Release não fornecer digest, a atualização será recusada em vez de instalar um arquivo não verificado.
- `UpdateInstaller`: expõe o APK por `FileProvider` e abre o Package Installer do Android.
- `MainActivity`: apresenta o botão e os estados da operação; não conterá lógica de rede ou instalação diretamente.

## Fluxo de dados

1. Usuário toca em `Verificar atualização`.
2. O app consulta a API pública de Releases do GitHub.
3. O app compara `versionName` instalado com `tag_name` da Release.
4. Se não houver versão nova, mostra `Você já está na versão mais recente`.
5. Se houver atualização, mostra brevemente a versão disponível e continua automaticamente para o download, sem exigir um segundo toque.
6. O APK é salvo somente no cache privado do app.
7. O SHA-256 do arquivo baixado é calculado e comparado ao digest da Release.
8. Se a validação passar, o app abre o instalador do Android.
9. O Android solicita confirmação da instalação e, se necessário, autorização para instalar apps desconhecidos.
10. Arquivos temporários antigos serão substituídos/removidos pelo fluxo de atualização.

## Rede e privacidade

O projeto deixará de ter a regra absoluta “sem permissão de internet”. Serão adicionadas `android.permission.INTERNET` e `android.permission.REQUEST_INSTALL_PACKAGES` apenas para o atualizador.

A permissão `INTERNET` do Android não pode ser limitada por domínio no Manifest. Portanto, a restrição será arquitetural e testada: somente o módulo de atualização poderá abrir conexões, e ele aceitará URLs HTTPS da infraestrutura GitHub necessária (`api.github.com`, `github.com` e hosts de download `githubusercontent.com`). URLs externas ou HTTP serão rejeitadas.

O `KeyboardService` continuará sem qualquer chamada de rede. O app continuará sem conta, analytics, anúncios, Firebase, telemetria, histórico de digitação ou envio de texto digitado.

A auditoria de privacidade do CI será atualizada: em vez de proibir toda permissão de internet e qualquer URL, ela verificará que não existem SDKs de telemetria, que os endpoints de rede pertencem ao atualizador e que os hosts aceitos estão limitados ao GitHub.

## Segurança

- Aceitar somente Release do repositório fixo `joaoldsxyzbr/Teclado-android`.
- Aceitar somente asset com MIME/APK e nome esperado para a tag publicada.
- Exigir HTTPS.
- Validar hosts antes da requisição e após redirects.
- Exigir SHA-256 da Release e comparar antes de abrir o instalador.
- Não executar APK diretamente; sempre delegar ao Package Installer do Android.
- Usar `FileProvider` com caminho restrito ao cache de atualização, sem `file://` público.
- Não armazenar token GitHub, credenciais ou segredo no APK; a API utilizada é pública.

## Interface

A tela principal terá o botão `Verificar atualização` abaixo das ações de ativar/selecionar teclado.

Estados mínimos:

- `Verificar atualização`
- `Verificando…`
- `Você já está na versão mais recente`
- `Nova versão vX.Y.Z disponível`
- `Baixando atualização…`
- `Validando atualização…`
- `Abrindo instalador…`
- erro de rede
- erro de integridade
- permissão para instalar apps desconhecidos necessária

Durante verificação/download, o botão ficará desabilitado para impedir operações concorrentes.

## Erros e recuperação

Falha de rede não altera o teclado nem bloqueia o app; o usuário poderá tentar novamente.

Release sem APK compatível, digest ausente, checksum inválido, URL fora da whitelist ou resposta inválida serão tratados como falha segura: nenhum instalador será aberto.

Se a permissão de instalação de fontes desconhecidas ainda não estiver concedida, o app abrirá a tela específica do Android para o usuário autorizar este aplicativo. Depois, o usuário poderá tocar novamente em `Verificar atualização`; se o APK já validado ainda estiver no cache, o fluxo poderá reutilizá-lo em vez de baixar novamente.

## Testes

Testes unitários deverão cobrir:

- comparação de versões (`1.0.2` vs `1.0.3`, versões iguais e prefixo `v`);
- parsing de Release válida e inválida;
- seleção do asset correto;
- whitelist de URLs e redirects;
- comparação SHA-256;
- estados do controlador de atualização.

O CI continuará executando `testDebugUnitTest` e `assembleDebug`, além da auditoria de privacidade revisada.

## Versão, Release e adoção inicial

A implementação incrementará `versionCode` e `versionName`. Ao chegar à `main`, o workflow existente continuará criando automaticamente a nova GitHub Release e anexando o APK, que passa a ser a fonte consumida pelas futuras atualizações.

A versão atualmente instalada (`v1.0.2`) não contém o atualizador e não consegue adicionar esse recurso a si mesma. Portanto, a primeira versão que incluir o botão deverá ser instalada manualmente a partir do GitHub uma única vez. A partir dela, as versões futuras poderão ser obtidas pelo próprio botão do app.

## Critérios de sucesso

- Um único toque detecta corretamente quando existe uma Release mais nova e, havendo atualização, segue automaticamente para download e validação.
- O APK oficial é baixado e validado por SHA-256.
- O Package Installer do Android é aberto com o APK validado.
- Nenhuma instalação silenciosa é tentada.
- O teclado continua funcional offline.
- Nenhum dado digitado é enviado à rede.
- O CI confirma build, testes e regras de privacidade/host.
