# Teclado Android V1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Entregar um teclado Android PT-BR funcional, privado e instalável, com visual moderno inspirado no Gboard e sem coleta de dados.

**Architecture:** Um único app Android nativo em Kotlin usando `InputMethodService`, Views/XML para a interface do teclado e classes puras para estado, layout e composição de acentos. A Activity principal apenas orienta o usuário a ativar e selecionar o IME; toda digitação permanece local e segue diretamente para `InputConnection`.

**Tech Stack:** Kotlin 2.3.21, Android Gradle Plugin 9.3.0, Gradle 9.5.0, JDK 17, compileSdk 36, targetSdk 36, minSdk 26, Android Views/XML, JUnit 4, AndroidX Test, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-03-teclado-v1-design.md`

## Global Constraints

- Android nativo em Kotlin.
- Implementação como `InputMethodService`.
- Layout QWERTY PT-BR.
- Letras, espaço, apagar, Enter e Shift.
- Acentos básicos do português.
- Tela de números e símbolos.
- Tema claro/escuro acompanhando o sistema.
- Teclas arredondadas com identidade própria, sem copiar assets do Gboard.
- Nenhuma permissão de internet.
- Nenhum SDK de analytics, anúncios ou telemetria.
- Nenhum login, conta ou persistência de histórico de digitação.
- Sem autocorreção, sugestões, swipe typing, voz, emojis avançados, clipboard manager, sincronização ou IA local no V1.

---

## File Structure

- `settings.gradle.kts` — configura o projeto e repositórios.
- `build.gradle.kts` — fixa plugins do Android/Kotlin.
- `gradle.properties` — opções do Gradle/AndroidX.
- `gradle/wrapper/gradle-wrapper.properties` — fixa Gradle 9.5.0.
- `app/build.gradle.kts` — configura SDKs, testes e dependências.
- `app/src/main/AndroidManifest.xml` — registra Activity e serviço IME, sem `INTERNET`.
- `app/src/main/res/xml/method.xml` — metadados do método de entrada PT-BR.
- `app/src/main/java/br/com/teclado/MainActivity.kt` — tela de ativação/seleção do teclado.
- `app/src/main/java/br/com/teclado/ime/KeyboardService.kt` — integração com `InputMethodService` e `InputConnection`.
- `app/src/main/java/br/com/teclado/ime/KeyboardController.kt` — estado transitório de Shift/layout/acento.
- `app/src/main/java/br/com/teclado/ime/KeyboardLayout.kt` — modelo declarativo das teclas.
- `app/src/main/java/br/com/teclado/ime/PortugueseAccentComposer.kt` — composição de acentos PT-BR.
- `app/src/main/res/layout/activity_main.xml` — instruções de ativação.
- `app/src/main/res/layout/keyboard_view.xml` — raiz visual do teclado.
- `app/src/main/res/values/colors.xml` e `values-night/colors.xml` — paleta claro/escuro.
- `app/src/main/res/values/dimens.xml` — altura, espaçamento e raio das teclas.
- `app/src/main/res/drawable/key_background.xml` — fundo arredondado das teclas.
- `app/src/test/...` — testes unitários de estado, layout e acentos.
- `app/src/androidTest/.../ImeRegistrationTest.kt` — valida registro do IME no pacote.
- `.github/workflows/android.yml` — build e testes em push/PR.

---

### Task 1: Bootstrap do projeto Android e contrato de privacidade

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Test: `app/src/test/java/br/com/teclado/ManifestPrivacyTest.kt`

**Interfaces:**
- Consumes: nenhum código anterior.
- Produces: módulo `:app` compilável, namespace `br.com.teclado`, `minSdk=26`, `targetSdk=36`, sem permissão `android.permission.INTERNET`.

- [ ] **Step 1: Criar teste que falha se o manifesto declarar internet**

```kotlin
package br.com.teclado

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Test

class ManifestPrivacyTest {
    @Test
    fun manifestDoesNotRequestInternetPermission() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.INTERNET"))
    }
}
```

- [ ] **Step 2: Criar configuração Gradle mínima**

Use AGP `9.3.0`, Kotlin `2.3.21`, Gradle `9.5.0`, JDK `17`, `compileSdk=36`, `targetSdk=36`, `minSdk=26`, `testInstrumentationRunner="androidx.test.runner.AndroidJUnitRunner"`.

- [ ] **Step 3: Criar manifesto sem permissões de rede**

O manifesto deve conter apenas `<application>`, a `MainActivity` e posteriormente o serviço IME. Não adicionar `<uses-permission android:name="android.permission.INTERNET"/>`.

- [ ] **Step 4: Rodar testes e build**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: PASS e geração de `app/build/outputs/apk/debug/app-debug.apk`.

- [ ] **Step 5: Commit**

```bash
git add .
git commit -m "build: bootstrap private Android keyboard app"
```

---

### Task 2: Estado, layouts QWERTY/símbolos e composição de acentos

**Files:**
- Create: `app/src/main/java/br/com/teclado/ime/KeyboardLayout.kt`
- Create: `app/src/main/java/br/com/teclado/ime/KeyboardController.kt`
- Create: `app/src/main/java/br/com/teclado/ime/PortugueseAccentComposer.kt`
- Test: `app/src/test/java/br/com/teclado/ime/KeyboardControllerTest.kt`
- Test: `app/src/test/java/br/com/teclado/ime/PortugueseAccentComposerTest.kt`

**Interfaces:**
- Consumes: nenhum componente Android de UI.
- Produces: `KeyboardMode`, `KeyboardAction`, `KeyboardKey`, `KeyboardLayout`, `KeyboardController.onAction(action)` e `PortugueseAccentComposer.compose(accent, char)`.

- [ ] **Step 1: Escrever testes do estado do teclado**

Cobrir: Shift alterna on/off, `123` muda para símbolos, `ABC` volta para letras e o estado nunca fica sem layout válido.

- [ ] **Step 2: Definir modelos declarativos**

```kotlin
enum class KeyboardMode { LETTERS, SYMBOLS }

sealed interface KeyboardAction {
    data class Character(val value: Char) : KeyboardAction
    data object Shift : KeyboardAction
    data object Backspace : KeyboardAction
    data object Enter : KeyboardAction
    data object Space : KeyboardAction
    data object Symbols : KeyboardAction
    data object Letters : KeyboardAction
    data class Accent(val value: Char) : KeyboardAction
}

data class KeyboardKey(val label: String, val action: KeyboardAction, val weight: Float = 1f)
data class KeyboardLayout(val rows: List<List<KeyboardKey>>)
```

- [ ] **Step 3: Implementar `KeyboardController`**

Estado público somente leitura: `mode`, `shiftEnabled`, `pendingAccent`. `onAction` deve atualizar apenas estado transitório e retornar o estado resultante.

- [ ] **Step 4: Testar composição de acentos PT-BR**

Casos mínimos: `´ + a = á`, `´ + e = é`, `^ + o = ô`, `~ + a = ã`, `` ` + a = à``, `¨ + u = ü`; combinação inválida retorna acento + caractere sem travar.

- [ ] **Step 5: Implementar `PortugueseAccentComposer`**

```kotlin
fun compose(accent: Char, char: Char): String
```

Usar tabela explícita, determinística e local; sem biblioteca externa.

- [ ] **Step 6: Rodar testes unitários**

Run: `./gradlew testDebugUnitTest`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/br/com/teclado/ime app/src/test
git commit -m "feat: add PT-BR keyboard state and accent composition"
```

---

### Task 3: Serviço IME e envio de teclas ao campo ativo

**Files:**
- Create: `app/src/main/java/br/com/teclado/ime/KeyboardService.kt`
- Create: `app/src/main/res/xml/method.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Test: `app/src/androidTest/java/br/com/teclado/ImeRegistrationTest.kt`

**Interfaces:**
- Consumes: `KeyboardController`, `KeyboardAction`, `PortugueseAccentComposer`.
- Produces: serviço `br.com.teclado.ime.KeyboardService` registrado com action `android.view.InputMethod`.

- [ ] **Step 1: Escrever teste instrumentado de registro do IME**

O teste deve consultar `PackageManager` para `android.view.InputMethod` e verificar que `KeyboardService` aparece na lista de serviços do próprio pacote.

- [ ] **Step 2: Registrar o serviço no manifesto**

Adicionar `<service>` com `android:permission="android.permission.BIND_INPUT_METHOD"`, `android:exported="true"`, intent-filter `android.view.InputMethod` e metadata `android.view.im` apontando para `@xml/method`.

- [ ] **Step 3: Criar metadata PT-BR**

`method.xml` deve declarar um subtipo com locale `pt_BR` e mode `keyboard`.

- [ ] **Step 4: Implementar despacho para `InputConnection`**

`KeyboardService` deve mapear ações para:
- caractere/espaço/acento composto: `commitText(...)`;
- apagar: `deleteSurroundingText(1, 0)`;
- Enter: `sendKeyEvent(KEYCODE_ENTER)` ou ação de editor quando aplicável;
- ausência de `currentInputConnection`: ignorar com segurança.

- [ ] **Step 5: Rodar build e testes unitários**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/androidTest
git commit -m "feat: register and implement Android IME service"
```

---

### Task 4: Interface visual inspirada em teclado moderno

**Files:**
- Create: `app/src/main/res/layout/keyboard_view.xml`
- Create: `app/src/main/res/drawable/key_background.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values-night/colors.xml`
- Create: `app/src/main/res/values/dimens.xml`
- Create: `app/src/main/java/br/com/teclado/ime/KeyboardViewRenderer.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardService.kt`
- Test: `app/src/test/java/br/com/teclado/ime/KeyboardLayoutTest.kt`

**Interfaces:**
- Consumes: `KeyboardLayout` e callback `(KeyboardAction) -> Unit`.
- Produces: `KeyboardViewRenderer.render(container, layout, shiftEnabled, onAction)`.

- [ ] **Step 1: Testar conteúdo dos layouts**

Verificar que o layout de letras contém QWERTY, `ç`, Shift, Backspace, `123`, espaço e Enter; o layout de símbolos contém dígitos e botão `ABC`.

- [ ] **Step 2: Criar renderer baseado em Views**

Construir linhas com `LinearLayout` e teclas com `TextView`/`MaterialButton` equivalente sem dependência de analytics. Cada tecla recebe `weight`, margem e callback da sua `KeyboardAction`.

- [ ] **Step 3: Criar estilo visual**

Teclas arredondadas, contraste adequado, espaçamento regular e tema automático claro/escuro. Não usar logotipo, ícones ou assets copiados do Gboard.

- [ ] **Step 4: Integrar renderer ao `onCreateInputView()`**

O serviço deve inflar `keyboard_view.xml`, renderizar o layout atual e redesenhar após Shift ou troca `123/ABC`.

- [ ] **Step 5: Rodar testes e build**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/test
git commit -m "feat: add modern PT-BR keyboard interface"
```

---

### Task 5: Activity de ativação e seleção do teclado

**Files:**
- Create: `app/src/main/java/br/com/teclado/MainActivity.kt`
- Create: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Consumes: intents do Android para configurações de entrada.
- Produces: botões `Ativar teclado` e `Selecionar teclado`.

- [ ] **Step 1: Criar Activity simples**

A tela explica que o teclado não usa internet nem envia o que é digitado.

- [ ] **Step 2: Implementar botão de ativação**

Abrir `Settings.ACTION_INPUT_METHOD_SETTINGS`.

- [ ] **Step 3: Implementar botão de seleção**

Usar `InputMethodManager.showInputMethodPicker()`.

- [ ] **Step 4: Validar build**

Run: `./gradlew assembleDebug`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main
git commit -m "feat: add keyboard setup activity"
```

---

### Task 6: CI no GitHub Actions e verificação final

**Files:**
- Create: `.github/workflows/android.yml`
- Create: `README.md`

**Interfaces:**
- Consumes: projeto Gradle completo.
- Produces: CI executando testes e build em `push` e `pull_request`.

- [ ] **Step 1: Criar workflow Android**

Usar `ubuntu-latest`, checkout, JDK 17, setup Gradle e executar:

```bash
./gradlew testDebugUnitTest assembleDebug
```

- [ ] **Step 2: Documentar instalação local**

README deve explicar: clonar, abrir no Android Studio, gerar APK, instalar, ativar o IME e selecionar o teclado.

- [ ] **Step 3: Fazer auditoria de privacidade**

Run:

```bash
grep -R "android.permission.INTERNET\|Firebase\|Analytics\|http://\|https://" app/src app/build.gradle.kts
```

Expected: nenhuma permissão/SDK/endereço de rede no código do app.

- [ ] **Step 4: Executar validação final**

Run:

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

Expected: BUILD SUCCESSFUL e APK debug produzido.

- [ ] **Step 5: Commit**

```bash
git add .github README.md
git commit -m "ci: validate private keyboard build and tests"
```

---

## Self-Review

- Cobertura da spec: Activity, IME, QWERTY PT-BR, Shift, acentos, símbolos, temas, ausência de internet e CI estão cobertos.
- Fora do escopo: não há tarefas para autocorreção, sugestões, swipe, voz, clipboard, sincronização ou IA.
- Privacidade: manifesto e dependências são auditados; não há persistência de texto digitado.
- Tipos/interfaces: `KeyboardAction`, `KeyboardLayout`, `KeyboardController` e `PortugueseAccentComposer` são definidos antes do serviço e renderer que os consomem.
