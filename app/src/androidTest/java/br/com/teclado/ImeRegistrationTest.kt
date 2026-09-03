package br.com.teclado

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.com.teclado.ime.KeyboardService
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ImeRegistrationTest {
    @Test fun keyboardServiceIsRegistered() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val services = context.packageManager.queryIntentServices(Intent("android.view.InputMethod"), 0)
        assertTrue(services.any { it.serviceInfo.packageName == context.packageName && it.serviceInfo.name == KeyboardService::class.java.name })
    }
}
