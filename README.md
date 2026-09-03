# Teclado Privado Android

Teclado Android PT-BR em Kotlin, sem permissão de internet, analytics, anúncios, login ou histórico de digitação.

## V1

- QWERTY PT-BR com `ç`
- Shift, apagar, Enter e espaço
- acentos básicos
- números e símbolos
- tema claro/escuro
- `InputMethodService` nativo

## Abrir e instalar

1. Clone o repositório e abra no Android Studio.
2. Use JDK 17 e sincronize o Gradle.
3. Gere o APK debug com `gradle assembleDebug` (ou execute pelo Android Studio).
4. Instale `app/build/outputs/apk/debug/app-debug.apk`.
5. Abra o app **Teclado Privado**.
6. Toque em **Ativar teclado** e habilite **Teclado Privado PT-BR**.
7. Volte ao app, toque em **Selecionar teclado** e escolha o teclado.

## Privacidade

O manifesto não solicita `android.permission.INTERNET`. O app não inclui Firebase, analytics, anúncios, telemetria ou endpoints de rede. A digitação é enviada apenas ao `InputConnection` do campo ativo no Android.
