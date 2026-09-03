package br.com.teclado.ime

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class KeyboardSizingTest {
    @Test
    fun keysUseTheApprovedLargerDimensions() {
        val dimens = File("src/main/res/values/dimens.xml").readText()

        assertTrue(dimens.contains("<dimen name=\"key_height\">58dp</dimen>"))
        assertTrue(dimens.contains("<dimen name=\"key_text_size\">19sp</dimen>"))
    }
}
