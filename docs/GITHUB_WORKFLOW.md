# GitHub workflow

O GitHub é a fonte de verdade do projeto Teclado Android.

## Desenvolvimento

- As alterações do projeto são feitas diretamente na `main`.
- Antes de alterar código, consultar o estado atual da `main`, commits recentes e a Issue correspondente quando existir.
- Manter mudanças pequenas e focadas.
- Usar commits descritivos, preferindo `test:`, `feat:`, `fix:`, `ci:`, `docs:` e `release:` conforme o tipo da mudança.
- Uma funcionalidade só é considerada concluída depois da validação automática correspondente.

## Android CI

O workflow `.github/workflows/android.yml` roda em pushes e pull requests da `main` e deve:

1. executar `gradle clean testDebugUnitTest assembleDebug`;
2. executar a auditoria de privacidade;
3. rejeitar telemetria e acesso de rede dentro do IME;
4. publicar o APK de debug como artefato temporário da execução.

O CI usa apenas permissão de leitura do conteúdo do repositório e não acessa as credenciais de assinatura de release.

## Release

O workflow `.github/workflows/release.yml` só roda para tags `v*`.

Para publicar uma versão:

1. atualizar `versionCode` e `versionName` em `app/build.gradle.kts`;
2. garantir que a `main` esteja validada pelo Android CI;
3. criar uma tag no formato `vX.Y.Z` apontando para o commit de release;
4. a tag deve corresponder exatamente ao `versionName` (`versionName = "1.5.0"` exige `v1.5.0`);
5. o workflow testa novamente, executa a auditoria de privacidade, assina o APK, verifica o certificado e publica o GitHub Release.

Pushes comuns na `main` nunca devem assinar nem publicar releases.

## Issues

- Uma Issue por funcionalidade, bug ou melhoria relevante.
- Toda Issue deve ter objetivo e critérios de conclusão verificáveis.
- Implementação e commits devem permanecer focados no escopo da Issue.
- Fechar a Issue somente depois da implementação e da validação do CI.

## Privacidade

O IME deve permanecer local. Acesso de rede permitido para atualização do aplicativo deve ficar isolado no pacote de updater e continuar protegido pela auditoria automatizada do workflow.
