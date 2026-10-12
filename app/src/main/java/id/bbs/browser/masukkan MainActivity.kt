
package id.bbs.browser

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView
import org.mozilla.geckoview.WebExtensionController

class MainActivity : Activity() {
    private val green = Color.rgb(25, 145, 100)
    private val blue = Color.rgb(29, 104, 190)
    private val aqua = Color.rgb(31, 190, 190)

    private lateinit var runtime: GeckoRuntime
    private lateinit var session: GeckoSession
    private lateinit var address: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runtime = GeckoRuntime.create(this)
        session = GeckoSession()
        session.open(runtime)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(10, 10, 10, 10)
            setBackgroundColor(Color.rgb(242, 250, 248))
        }

        address = EditText(this).apply {
            hint = "Cari atau masukkan alamat situs"
            singleLine = true
            textSize = 14f
            setPadding(12, 0, 12, 0)
            background = rounded(Color.WHITE, aqua)
            imeOptions = EditorInfo.IME_ACTION_GO
            setOnEditorActionListener { _, action, _ ->
                if (action == EditorInfo.IME_ACTION_GO ||
                    action == EditorInfo.IME_ACTION_DONE) {
                    navigate(text.toString())
                    true
                } else false
            }
        }

        bar.addView(address, LinearLayout.LayoutParams(0, 48, 1f))
        val go = makeButton("Buka", green)
        go.setOnClickListener { navigate(address.text.toString()) }
        bar.addView(go, LinearLayout.LayoutParams(70, 48))

        root.addView(bar)

        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(6, 4, 6, 4)
        }

        val back = makeButton("‹", blue)
        back.setOnClickListener {
            if (session.canGoBack) session.goBack()
        }
        controls.addView(back, LinearLayout.LayoutParams(0, 44, 1f))

        val forward = makeButton("›", blue)
        forward.setOnClickListener {
            if (session.canGoForward) session.goForward()
        }
        controls.addView(forward, LinearLayout.LayoutParams(0, 44, 1f))

        val reload = makeButton("↻", aqua)
        reload.setOnClickListener { session.reload() }
        controls.addView(reload, LinearLayout.LayoutParams(0, 44, 1f))

        val extension = makeButton("Ekstensi", green)
        extension.setOnClickListener {
            Toast.makeText(
                this,
                "Dukungan ekstensi akan ditambahkan setelah fitur instalasi diuji.",
                Toast.LENGTH_LONG
            ).show()
        }
        controls.addView(extension, LinearLayout.LayoutParams(0, 44, 2f))

        root.addView(controls)

        val web = GeckoView(this)
        web.setSession(session)
        root.addView(
            web,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        )

        setContentView(root)
        navigate("https://www.google.com")
    }

    private fun navigate(input: String) {
        val value = input.trim()
        if (value.isEmpty()) return

        val url = when {
            value.startsWith("https://") ||
                value.startsWith("http://") -> value
            value.contains(".") && !value.contains(" ") ->
                "https://$value"
            else -> "https://www.google.com/search?q=" + Uri.encode(value)
        }

        session.loadUri(url)
    }

    private fun makeButton(label: String, color: Int): Button {
        return Button(this).apply {
            text = label
            textSize = 13f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(color, color)
            minHeight = 0
            minimumHeight = 0
            minWidth = 0
            minimumWidth = 0
            setPadding(4, 0, 4, 0)
        }
    }

    private fun rounded(
        fill: Int,
        stroke: Int
    ): GradientDrawable {
        return GradientDrawable().apply {
            setColor(fill)
            cornerRadius = 13f
            setStroke(1, stroke)
        }
    }

    override fun onDestroy() {
        if (::session.isInitialized) session.close()
        super.onDestroy()
    }
}
