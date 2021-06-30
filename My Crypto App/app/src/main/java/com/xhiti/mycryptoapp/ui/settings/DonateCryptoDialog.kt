package com.baruckis.kriptofolio.ui.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.fragment.app.DialogFragment
import com.baruckis.kriptofolio.R
import kotlinx.android.synthetic.main.dialog_donate_crypto.view.*

class DonateCryptoDialog : DialogFragment() {

    companion object {

        const val DIALOG_DONATE_CRYPTO_TAG = "donate_crypto_dialog"
        private const val EXTRA_TITLE = "title"
        private const val EXTRA_POSITIVE_BUTTON = "positive_button"

        fun newInstance(title: String, positiveButton: String): DonateCryptoDialog {
            val dialog = DonateCryptoDialog()
            val args = Bundle().apply {
                putString(EXTRA_TITLE, title)
                putString(EXTRA_POSITIVE_BUTTON, positiveButton)
            }
            dialog.arguments = args
            return dialog
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments?.getString(EXTRA_TITLE)
        val positiveButton = arguments?.getString(EXTRA_POSITIVE_BUTTON)
        val dialog = activity?.let { activity ->
            val builder = AlertDialog.Builder(activity)

            builder.setTitle(title)
            val dialogView = activity.layoutInflater.inflate(R.layout.dialog_donate_crypto, null)

            dialogView.item_bitcoin_address.setOnClickListener {
                copyCryptoAddressToClipBoard(activity, getString(R.string.dialog_donate_crypto_bitcoin_address))
                Toast.makeText(context, getString(R.string.dialog_donate_crypto_bitcoin_address_copy_confirmation), Toast.LENGTH_SHORT).show()
            }

            dialogView.item_ethereum_address.setOnClickListener {
                copyCryptoAddressToClipBoard(activity, getString(R.string.dialog_donate_crypto_ethereum_address))
                Toast.makeText(context, getString(R.string.dialog_donate_crypto_ethereum_address_copy_confirmation), Toast.LENGTH_SHORT).show()
            }

            dialogView.scrollview.viewTreeObserver.addOnScrollChangedListener {
                controlScrollDividersVisibility(dialogView.scrollview, dialogView.divider_bottom, dialogView.divider_top)
            }

            dialogView.scrollview.doOnLayout {
                controlScrollDividersVisibility(dialogView.scrollview, dialogView.divider_bottom, dialogView.divider_top)
            }

            builder.setView(dialogView)
            builder.setCancelable(true)
            builder.setPositiveButton(positiveButton) { _, _ ->

            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null!")

        return dialog
    }

    private fun controlScrollDividersVisibility(scrollView: ScrollView,
                                                dividerBottom: View,
                                                dividerTop: View) {
        if (!scrollView.canScrollVertically(1)) {
            // Bottom of scroll view.
            dividerBottom.visibility = View.INVISIBLE
        } else dividerBottom.visibility = View.VISIBLE
        if (!scrollView.canScrollVertically(-1)) {
            // Top of scroll view.
            dividerTop.visibility = View.INVISIBLE
        } else dividerTop.visibility = View.VISIBLE
    }

    private fun copyCryptoAddressToClipBoard(context: Context, address: String) {
        val clipboard =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        val clip = ClipData.newPlainText(null, address)
        clipboard?.setPrimaryClip(clip)
    }

}