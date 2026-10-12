
package id.bbs.browser

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtensionController
import java.io.File
import java.io.FileOutputStream

class MainActivity : Activity() {
    private val green = Color.rgb(25, 145, 100)
    private val blue = Color.rgb(29, 104, 190)
    private val aqua = Color.rgb(31, 190, 190)
    private val ink = Color.rgb(26, 42, 51)

    private lateinit var runtime: GeckoRuntime
    private lateinit var session: GeckoSession
    private lateinit var address: EditText
    private lateinit var browserView: GeckoView
    private lateinit var status: TextView

    private var canGoBack = false
    private var canGoForward = false

    private val extensionPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) installExtensionFromDevice(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.open(runtime)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(8))
            setBackgroundColor(Color.rgb(242, 250, 248))
        }

        address = EditText(this).apply {
            isSingleLine = true
            hint = "Cari atau masukkan alamat situs"
            setTextColor(ink)
            setHintTextColor(Color.GRAY)
            textSize = 14f
            setPadding(dp(12), 0, dp(12), 0)
            background = roundedBackground(Color.WHITE, aqua)
            imeOptions = EditorInfo.IME_ACTION_GO

            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO ||
                    actionId == EditorInfo.IME_ACTION_DONE
                ) {
                    navigate(address.text.toString())
                    true
                } else {
                    false
                }
            }
        }

        top.addView(address, LinearLayout.LayoutParams(0, dp(46), 1f))

        val go = button("Buka", green)
        go.setOnClickListener { navigate(address.text.toString()) }
        top.addView(go, LinearLayout.LayoutParams(dp(64), dp(46)).apply {
            leftMargin = dp(7)
        })
        root.addView(top)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setBackgroundColor(Color.WHITE)
        }

        val back = button("‹", blue, 22f)
        back.contentDescription = "Kembali"
        back.setOnClickListener {
            if (canGoBack) session.goBack()
        }
        controls.addView(back, LinearLayout.LayoutParams(0, dp(42), 1f))

        val forward = button("›", blue, 22f)
        forward.contentDescription = "Maju"
        forward.setOnClickListener {
            if (canGoForward) session.goForward()
        }
        controls.addView(
            forward,
            LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                leftMargin = dp(5)
            }
        )

        val reload = button("↻", aqua, 20f)
        reload.contentDescription = "Muat ulang"
        reload.setOnClickListener { session.reload() }
        controls.addView(
            reload,
            LinearLayout.LayoutParams(0, dp(42), 1f).apply {
                leftMargin = dp(5)
            }
        )

        val addOn = button("Ekstensi", green, 12f)
        addOn.setOnClickListener {
            extensionPicker.launch(arrayOf("*/*"))
        }
        controls.addView(
            addOn,
            LinearLayout.LayoutParams(0, dp(42), 1.5f).apply {
                leftMargin = dp(5)
            }
        )
        root.addView(controls)

        browserView = GeckoView(this)
        browserView.setSession(session)
        root.addView(
            browserView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        status = TextView(this).apply {
            text = "BBS Browser • Siap menjelajah"
            textSize = 11f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(7), dp(12), dp(7))
            setBackgroundColor(green)
        }
        root.addView(status)
        setContentView(root)

        session.navigationDelegate = object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: MutableList<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean
            ) {
                if (!url.isNullOrBlank()) {
                    runOnUiThread {
                        address.setText(url)
                        status.text =
                            "BBS Browser • " + (Uri.parse(url).host ?: url)
                    }
                }
            }

            override fun onCanGoBack(
                session: GeckoSession,
                value: Boolean
            ) {
                canGoBack = value
            }

            override fun onCanGoForward(
                session: GeckoSession,
                value: Boolean
            ) {
                canGoForward = value
            }
        }

        navigate("https://www.google.com")
    }

    private fun navigate(input: String) {
        var value = input.trim()
        if (value.isEmpty()) return

        if (!value.contains("://")) {
            value = if (value.contains(".") && !value.contains(" ")) {
                "https://$value"
            } else {
                "https://www.google.com/search?q=" + Uri.encode(value)
            }
        }

        session.loadUri(value)
    }

    private fun installExtensionFromDevice(source: Uri) {
        try {
            val extensionFile = File(
                cacheDir,
                "selected-addon-${System.currentTimeMillis()}.xpi"
            )

            contentResolver.openInputStream(source).use { input ->
                requireNotNull(input) { "File tidak dapat dibaca" }.use { stream ->
                    FileOutputStream(extensionFile).use { output ->
                        stream.copyTo(output)
                    }
                }
            }

            status.text = "BBS Browser • Memeriksa ekstensi…"

            runtime.webExtensionController.install(
                Uri.fromFile(extensionFile).toString(),
                WebExtensionController.INSTALLATION_METHOD_FROM_FILE
            ).accept(
                { extension ->
                    runOnUiThread {
                        val name = extension?.metaData?.name ?: "Ekstensi"
                        status.text = "Ekstensi terpasang: $name"
                        Toast.makeText(
                            this,
                            "Ekstensi berhasil dipasang",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                { _ ->
                    runOnUiThread {
                        status.text = "Ekstensi gagal dipasang"
                        Toast.makeText(
                            this,
                            "Gunakan file .xpi yang ditandatangani Mozilla dan kompatibel.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        } catch (e: Exception) {
            status.text = "Tidak bisa membaca file ekstensi"
            Toast.makeText(
                this,
                "Pilih file ekstensi .xpi yang valid",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun button(
        label: String,
        color: Int,
        size: Float = 13f
    ) = Button(this).apply {
        text = label
        textSize = size
        isAllCaps = false
        setTextColor(Color.WHITE)
        background = roundedBackground(color, color)
        minHeight = 0
        minimumHeight = 0
        minWidth = 0
        minimumWidth = 0
        setPadding(dp(4), 0, dp(4), 0)
    }

    private fun roundedBackground(
        fill: Int,
        stroke: Int
    ): android.graphics.drawable.GradientDrawable {
        return android.graphics.drawable.GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(13).toFloat()
            setStroke(dp(1), stroke)
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        if (::session.isInitialized) {
            session.close()
        }
        super.onDestroy()
    }
}
