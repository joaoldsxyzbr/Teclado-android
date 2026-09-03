package br.com.teclado

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import br.com.teclado.update.ApkDownloader
import br.com.teclado.update.SelfUpdateManager
import br.com.teclado.update.UpdateChecker
import br.com.teclado.update.UpdateInstaller
import br.com.teclado.update.UpdateState
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val updateExecutor = Executors.newSingleThreadExecutor()
    private lateinit var checkUpdateButton: Button
    private lateinit var updateStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Teclado)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.enable_keyboard).setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        findViewById<Button>(R.id.select_keyboard).setOnClickListener {
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }

        checkUpdateButton = findViewById(R.id.check_update)
        updateStatus = findViewById(R.id.update_status)

        val updater = SelfUpdateManager(
            releaseSource = UpdateChecker(),
            apkSource = ApkDownloader(applicationContext),
            installer = UpdateInstaller(applicationContext)
        )

        checkUpdateButton.setOnClickListener {
            checkUpdateButton.isEnabled = false
            updateExecutor.execute {
                updater.run(installedVersion()) { state ->
                    runOnUiThread { renderUpdateState(state) }
                }
                runOnUiThread { checkUpdateButton.isEnabled = true }
            }
        }
    }

    override fun onDestroy() {
        updateExecutor.shutdownNow()
        super.onDestroy()
    }

    private fun installedVersion(): String =
        packageManager.getPackageInfo(packageName, 0).versionName
            ?: error("Versão instalada indisponível")

    private fun renderUpdateState(state: UpdateState) {
        updateStatus.visibility = View.VISIBLE
        updateStatus.text = when (state) {
            UpdateState.Checking -> getString(R.string.update_checking)
            UpdateState.UpToDate -> getString(R.string.update_current)
            is UpdateState.Available -> getString(R.string.update_available, state.tag)
            UpdateState.Downloading -> getString(R.string.update_downloading)
            UpdateState.Verifying -> getString(R.string.update_verifying)
            UpdateState.PermissionRequired -> getString(R.string.update_permission)
            UpdateState.Installing -> getString(R.string.update_installing)
            is UpdateState.Failed -> getString(R.string.update_error, state.message)
        }
    }
}
