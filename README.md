# Teclado joaoldsxyzbr

Teclado Android PT-BR em Kotlin com foco em privacidade. A digitação permanece local no aparelho; não há analytics, anúncios, login, telemetria ou histórico de digitação.

## Recursos

- QWERTY PT-BR com `ç`
- Shift, apagar, Enter e espaço
- acentos básicos
- números e símbolos
- tema claro/escuro
- `InputMethodService` nativo
- atualização manual de um toque pelas Releases oficiais do GitHub

## Abrir e instalar

1. Baixe o APK da Release mais recente deste repositório.
2. Instale o APK no Android.
3. Abra o app **Teclado joaoldsxyzbr**.
4. Toque em **Ativar teclado** e habilite **Teclado joaoldsxyzbr**.
5. Volte ao app, toque em **Selecionar teclado** e escolha o teclado.

A versão `1.1.0` é a primeira com atualizador integrado e precisa ser instalada manualmente uma vez. Depois disso, versões futuras podem ser obtidas pelo botão **Verificar atualização** dentro do app.

## Atualizações

Quando o usuário toca em **Verificar atualização**, o app consulta a Release mais recente de `joaoldsxyzbr/Teclado-android` no GitHub. Se existir uma versão nova, o APK oficial é baixado para o cache privado do app, o SHA-256 é validado e o instalador padrão do Android é aberto.

A instalação nunca é silenciosa: o Android exige confirmação e pode pedir autorização para **Instalar apps desconhecidos** para este aplicativo.

## Privacidade

O `KeyboardService` não possui acesso de rede e não envia texto digitado, estado do teclado ou conteúdo de `InputConnection`. A permissão de internet existe exclusivamente para o módulo `br.com.teclado.update`, acionado manualmente pelo botão de atualização e restrito à infraestrutura de Releases do GitHub.

O app não inclui Firebase, analytics, anúncios, telemetria, contas ou armazenamento de histórico de digitação.
