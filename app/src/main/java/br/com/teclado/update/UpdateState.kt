package br.com.teclado.update

sealed interface UpdateState {
    data object Checking : UpdateState
    data object UpToDate : UpdateState
    data class Available(val tag: String) : UpdateState
    data object Downloading : UpdateState
    data object Verifying : UpdateState
    data object PermissionRequired : UpdateState
    data object Installing : UpdateState
    data class Failed(val message: String) : UpdateState
}
