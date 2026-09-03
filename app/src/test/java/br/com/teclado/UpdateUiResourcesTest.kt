package br.com.teclado

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUiResourcesTest {
    private val layout = File("src/main/res/layout/activity_main.xml").readText()
    private val strings = File("src/main/res/values/strings.xml").readText()

    @Test fun updateControlsArePresent() {
        assertTrue(layout.contains("@+id/check_update"))
        assertTrue(layout.contains("@+id/update_status"))
        assertTrue(strings.contains("name=\"check_update\""))
    }

    @Test fun privacyCopyExplainsManualGitHubNetworkUse() {
        assertTrue(strings.contains("GitHub"))
        assertTrue(strings.contains("Verificar atualização"))
        assertTrue(strings.contains("Digitação local"))
    }
}
