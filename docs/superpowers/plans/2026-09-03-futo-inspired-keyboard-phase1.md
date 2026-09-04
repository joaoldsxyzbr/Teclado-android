# FUTO-inspired Keyboard Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a modern FUTO-inspired first interaction phase for the existing Android IME while keeping the implementation local, lightweight, testable, and independently licensed.

**Architecture:** Keep `KeyboardService` as IME coordinator, `KeyboardViewRenderer` as touch/UI adapter, and move gesture state into pure Kotlin trackers. Keep toolbar actions declarative and let the service translate them into `InputConnection` editor operations.

**Tech Stack:** Kotlin, Android InputMethodService/InputConnection, classic Android Views, JUnit 4, Gradle.

**Spec:** `docs/superpowers/specs/2026-09-03-futo-inspired-keyboard-phase1-design.md`

## Global Constraints

- Modify `main` directly.
- Do not copy FUTO Source First licensed implementation code.
- No ML suggestions, swipe word decoding, offline voice, clipboard history, language switching, or word-at-a-time delete in this phase.
- Preserve Android `minSdk = 26` and `targetSdk = 36`.
- All new gesture state must be unit-testable without Android UI dependencies where practical.

---

### Task 1: Standard QWERTY without dedicated cedilla

**Files:**
- Modify: `app/src/test/java/br/com/teclado/ime/GboardStyleLayoutTest.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardLayout.kt`

**Interfaces:**
- Consumes: `KeyboardLayout.letters()`
- Produces: letters row 2 exactly `asdfghjkl`; `PortugueseAlternates.forCharacter('c')` remains responsible for `ç`.

- [ ] **Step 1: Update the layout regression test**

Assert that the second alphabetic row labels join to `asdfghjkl` and that no primary key label equals `ç`.

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.GboardStyleLayoutTest`
Expected: FAIL while the current row still contains `ç`.

- [ ] **Step 3: Make the minimal layout change**

Change the row source from `"asdfghjklç"` to `"asdfghjkl"`.

- [ ] **Step 4: Run the focused test and verify it passes**

Run the same Gradle command. Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: use standard qwerty letter layout`

---

### Task 2: Pure spacebar cursor tracker

**Files:**
- Create: `app/src/main/java/br/com/teclado/ime/SpaceCursorTracker.kt`
- Create: `app/src/test/java/br/com/teclado/ime/SpaceCursorTrackerTest.kt`

**Interfaces:**
- Produces: `class SpaceCursorTracker(private val stepPx: Float)` with `down(x: Float)`, `move(x: Float): Int`, `up(): Boolean`, and `cancel()`.
- `move` returns signed cursor steps since the previous emitted threshold; `up` returns whether cursor movement occurred and therefore whether a normal space tap must be suppressed.

- [ ] **Step 1: Write failing tests**

Cover: sub-threshold movement returns 0; right movement returns positive steps; left movement returns negative steps; accumulated movement emits multiple steps; `up()` reports moved state; `cancel()` resets state.

- [ ] **Step 2: Run the focused test and verify failure**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.SpaceCursorTrackerTest`
Expected: FAIL because the class does not exist.

- [ ] **Step 3: Implement the tracker**

Track start/last emitted X, active state, and whether any step was emitted. Convert `(x - lastEmittedX) / stepPx` to a truncated signed integer; after emitting, advance `lastEmittedX` by `steps * stepPx`.

- [ ] **Step 4: Run the focused test**

Expected: PASS.

- [ ] **Step 5: Commit**

Commit message: `feat: add spacebar cursor gesture tracker`

---

### Task 3: Integrate spacebar cursor movement

**Files:**
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardViewRenderer.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardService.kt`

**Interfaces:**
- `KeyboardViewRenderer.render(..., onAction, onCursorMove: (Int) -> Unit)`
- `KeyboardService.moveCursor(delta: Int)` moves the current editor selection.

- [ ] **Step 1: Add renderer integration around `KeyboardAction.Space`**

On down: haptic + tracker down. On move: emit non-zero `tracker.move(event.x)`. On up: call `view.performClick()` only if tracker reports no cursor movement. On cancel: reset tracker.

- [ ] **Step 2: Implement service cursor movement**

Use `ExtractedTextRequest` to read current selection and call `setSelection(newPosition, newPosition)` clamped to `[0, text.length]`. If extracted text is unavailable, send `KEYCODE_DPAD_LEFT` or `KEYCODE_DPAD_RIGHT` events once per requested step.

- [ ] **Step 3: Run unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 4: Commit**

Commit message: `feat: move cursor by dragging spacebar`

---

### Task 4: Sliding alternate-character selection

**Files:**
- Create: `app/src/main/java/br/com/teclado/ime/AlternateSelectionTracker.kt`
- Create: `app/src/test/java/br/com/teclado/ime/AlternateSelectionTrackerTest.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyTouchSession.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardViewRenderer.kt`

**Interfaces:**
- `AlternateSelectionTracker(optionCount: Int, optionWidthPx: Float, popupLeftPx: Float)` exposes `indexFor(rawX: Float): Int` clamped to available options.
- `KeyTouchSession.isLongPressTriggered: Boolean` exposes read-only long-press state.

- [ ] **Step 1: Write failing tracker tests**

Cover first/middle/last option mapping and clamping beyond both popup edges.

- [ ] **Step 2: Run focused tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.AlternateSelectionTrackerTest`
Expected: FAIL because tracker does not exist.

- [ ] **Step 3: Implement the tracker and expose long-press state**

Keep selection math pure. Rename the internal long-press boolean if needed and expose it through a getter without allowing external mutation.

- [ ] **Step 4: Replace click-only alternate popup behavior**

Create popup option views, track popup screen-left and option width, update the highlighted option from `MotionEvent.rawX` during move, and commit the highlighted alternative on `ACTION_UP`. Dismiss without commit on `ACTION_CANCEL`. Short taps still call `performClick()`.

- [ ] **Step 5: Run touch/alternate tests**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.KeyTouchSessionTest --tests br.com.teclado.ime.PortugueseAlternatesTest --tests br.com.teclado.ime.AlternateSelectionTrackerTest`
Expected: PASS.

- [ ] **Step 6: Commit**

Commit message: `feat: support sliding long press alternatives`

---

### Task 5: Harden backspace hold behavior

**Files:**
- Modify: `app/src/test/java/br/com/teclado/ime/KeyTouchSessionTest.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardViewRenderer.kt`

**Interfaces:**
- Existing `BackspaceRepeatPolicy` remains unchanged.

- [ ] **Step 1: Add regression expectations**

Keep tests proving immediate delete, repeat acceleration, stop on release, and stop on cancellation.

- [ ] **Step 2: Change renderer move handling for backspace**

Do not cancel a backspace repeat merely because the pointer drifts outside the key. Only `ACTION_UP` and `ACTION_CANCEL` terminate the repeat lifecycle.

- [ ] **Step 3: Run touch tests**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.KeyTouchSessionTest`
Expected: PASS.

- [ ] **Step 4: Commit**

Commit message: `fix: keep backspace repeating during hold`

---

### Task 6: FUTO-inspired local action bar

**Files:**
- Modify: `app/src/test/java/br/com/teclado/ime/KeyboardToolbarSpecTest.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardToolbarSpec.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardToolbarRenderer.kt`
- Modify: `app/src/main/java/br/com/teclado/ime/KeyboardService.kt`
- Modify: `app/src/main/res/values/strings.xml`

**Interfaces:**
- Toolbar actions: `EMOJI`, `CLIPBOARD`, `UNDO`, `REDO`, `TEXT_EDIT`, `SETTINGS`.
- Destinations: `EMOJI_PANEL`, `CLIPBOARD_PANEL`, `UNDO`, `REDO`, `TEXT_EDIT_PANEL`, `OPEN_SETTINGS`.
- New panel: `TEXT_EDIT`.
- Renderer panel callback: `onEditorAction: (KeyboardEditorAction) -> Unit`.
- Editor actions: left, right, select all, cut, copy, paste.

- [ ] **Step 1: Update toolbar spec tests first**

Assert all six toolbar items are enabled/local-only and map to the expected destination; assert text-edit panel/action enum exists.

- [ ] **Step 2: Run focused toolbar tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests br.com.teclado.ime.KeyboardToolbarSpecTest`
Expected: FAIL against the old tools/translate/voice spec.

- [ ] **Step 3: Update declarative toolbar spec**

Replace old items/destinations with the six approved local actions.

- [ ] **Step 4: Render text-edit panel**

Add chips for `←`, `→`, `Selecionar tudo`, `Recortar`, `Copiar`, `Colar`, routed through `KeyboardEditorAction`.

- [ ] **Step 5: Implement service editor operations**

Undo/redo: `performContextMenuAction(android.R.id.undo/redo)`. Editing actions: DPAD left/right key events and `android.R.id.selectAll/cut/copy/paste` context-menu actions.

- [ ] **Step 6: Run toolbar and full unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: PASS.

- [ ] **Step 7: Commit**

Commit message: `feat: add local keyboard action bar`

---

### Task 7: Version, verification, and regression check

**Files:**
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Increase `versionCode` by 1 and set the next patch/minor version consistently with repository history.

- [ ] **Step 1: Bump app version**

Move from version code 8 / version name 1.3.0 to version code 9 / version name 1.4.0.

- [ ] **Step 2: Run all unit tests**

Run: `./gradlew testDebugUnitTest`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Build debug APK**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Inspect final diff**

Confirm no network permission, no FUTO source copied, no unrelated update/signing behavior changed.

- [ ] **Step 5: Commit**

Commit message: `release: prepare keyboard interaction phase 1`
