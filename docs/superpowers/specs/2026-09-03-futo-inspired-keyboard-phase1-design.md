# FUTO-inspired Keyboard Phase 1 Design

## Goal

Evolve the existing private Android keyboard toward a modern FUTO-inspired interaction model without copying FUTO source code and without adding heavy AI, swipe-decoding, or offline voice subsystems in this phase.

## Scope

Phase 1 includes:

1. Standard QWERTY letters layout without a dedicated `ç` key. `ç` remains available from long-press on `c`.
2. Backspace behavior that deletes once on press, repeats while held, and stops reliably on release or cancellation.
3. Horizontal spacebar drag to move the text cursor while preserving normal tap-to-insert-space behavior.
4. Long-press alternate-character popup that supports sliding over alternatives and committing the highlighted character on release.
5. Action-oriented top bar with emoji, clipboard, undo, redo, text-edit tools, and settings.
6. More defensive touch handling for cancellation, movement, and rapid touch sequences.
7. Unit tests for pure gesture/state logic and regression tests for layout and toolbar specifications.

Out of scope for this phase: ML/transformer suggestions, swipe typing word decoding, offline voice recognition, clipboard history, language switching, and word-at-a-time backspace.

## Architecture

`KeyboardService` remains the Android IME coordinator. `KeyboardViewRenderer` owns visual key rendering and translates touch streams into high-level callbacks. Pure gesture state is extracted into small classes that can be unit tested without Android UI dependencies.

The existing `KeyTouchSession` remains responsible for tap/hold/repeat lifecycle. Spacebar cursor movement uses a dedicated pure tracker that converts horizontal distance into discrete cursor steps. Alternate-character selection uses a pure index tracker while a small popup controller handles Android views.

Toolbar behavior remains declarative in `KeyboardToolbarSpec`. `KeyboardToolbarRenderer` renders the toolbar and panels, while `KeyboardService` performs editor operations through `InputConnection`.

## Detailed behavior

### Letters layout

The primary letter rows are `qwertyuiop`, `asdfghjkl`, and `zxcvbnm`. No dedicated `ç` key is shown. Portuguese alternatives continue to expose `ç` from `c` long-press.

### Backspace

A press deletes one character immediately. Holding continues using the existing acceleration policy. Backspace remains active through ordinary finger drift and stops only on `ACTION_UP` or `ACTION_CANCEL`, avoiding accidental cancellation during a hold.

### Spacebar cursor movement

A tap inserts one space. Horizontal movement beyond a threshold switches the interaction into cursor mode and suppresses the space insertion. Each threshold-sized horizontal increment emits a signed cursor delta. The service moves the selection using the active `InputConnection` and clamps selection to valid text bounds when extracted text is available.

### Alternate characters

Long-press opens alternatives above the key. While the finger remains down, horizontal movement updates the highlighted option. Releasing commits the highlighted option; cancellation dismisses the popup without committing. A short tap continues to commit the base character.

### Top bar

The toolbar contains six enabled local actions: emoji, clipboard, undo, redo, text edit, and settings. Emoji and clipboard open existing panels. Text edit opens a panel with left/right cursor movement and select-all/cut/copy/paste controls. Undo and redo call editor context menu actions when supported. Settings opens the existing app settings activity.

No network-backed action is enabled in this phase.

## Error handling

All editor actions are null-safe when `currentInputConnection` is unavailable. Cursor movement falls back to DPAD left/right key events if selection information cannot be obtained. Popup state is always dismissed on cancellation and when the keyboard rerenders. Unsupported undo/redo/context-menu actions fail silently without crashing the IME.

## Testing

Add or update unit tests for:

- QWERTY row without `ç`.
- `c` still exposing `ç` through alternatives.
- Backspace repeat lifecycle and cancellation.
- Spacebar tracker thresholding, direction, accumulation, and tap suppression state.
- Alternate selection index clamping and movement.
- Toolbar action order/destinations and text-edit panel presence.

The Android project build and unit test task must pass before the phase is considered complete.

## Licensing constraint

FUTO Keyboard is used only as behavioral and architectural inspiration. No FUTO Source First licensed implementation code is copied into this repository.