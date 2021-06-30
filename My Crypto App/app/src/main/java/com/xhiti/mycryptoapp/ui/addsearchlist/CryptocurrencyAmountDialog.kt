package com.baruckis.kriptofolio.ui.addsearchlist

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.baruckis.kriptofolio.R
import com.baruckis.kriptofolio.utilities.nonEmpty
import com.baruckis.kriptofolio.utilities.validate
import kotlinx.android.synthetic.main.dialog_add_crypto_amount.view.*

class CryptocurrencyAmountDialog : DialogFragment() {

    companion object {

        const val DIALOG_CRYPTOCURRENCY_AMOUNT_TAG = "cryptocurrency_amount_dialog"

        private const val EXTRA_TITLE = "title"
        private const val EXTRA_HINT = "hint"
        private const val EXTRA_CONFIRM_BUTTON = "confirm_button"
        private const val EXTRA_CANCEL_BUTTON = "cancel_button"
        private const val EXTRA_ERROR = "error"

        fun newInstance(title: String, hint: String, confirmButton: String, cancelButton: String, error: String): CryptocurrencyAmountDialog {
            val dialog = CryptocurrencyAmountDialog()
            val args = Bundle().apply {
                putString(EXTRA_TITLE, title)
                putString(EXTRA_HINT, hint)
                putString(EXTRA_CONFIRM_BUTTON, confirmButton)
                putString(EXTRA_CANCEL_BUTTON, cancelButton)
                putString(EXTRA_ERROR, error)
            }
            dialog.arguments = args
            return dialog
        }

    }

    internal lateinit var mListener: CryptocurrencyAmountDialogListener

    interface CryptocurrencyAmountDialogListener {
        fun onCryptocurrencyAmountDialogConfirmButtonClick(cryptocurrencyAmountDialog: CryptocurrencyAmountDialog)
        fun onCryptocurrencyAmountDialogCancel()
    }

    private lateinit var editTextAmount: EditText
    private var valueAmount: Double = 0.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mListener = context as CryptocurrencyAmountDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException((context.toString() +
                    " must implement CryptocurrencyAmountDialogListener."))
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val title = arguments?.getString(EXTRA_TITLE)
        val hint = arguments?.getString(EXTRA_HINT)
        val confirmButton = arguments?.getString(EXTRA_CONFIRM_BUTTON)
        val cancelButton = arguments?.getString(EXTRA_CANCEL_BUTTON)
        val error = arguments?.getString(EXTRA_ERROR) ?: ""

        val dialog = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
            val dialogView = it.layoutInflater.inflate(R.layout.dialog_add_crypto_amount, null)
            dialogView.edit_text_amount.hint = hint
            builder.setView(dialogView)
            builder.setCancelable(true)
            builder.setPositiveButton(confirmButton) { _, _ -> }
            builder.setNeutralButton(cancelButton) { _, _ ->
                mListener.onCryptocurrencyAmountDialogCancel()
            }

            editTextAmount = dialogView.edit_text_amount
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null!")

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.setOnShowListener {
            val buttonPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            editTextAmount.nonEmpty(
                    { buttonPositive.isEnabled = false },
                    { buttonPositive.isEnabled = true })

            buttonPositive.setOnClickListener {
                if (onValidateAndConfirm(error)) {
                    mListener.onCryptocurrencyAmountDialogConfirmButtonClick(this)
                }
            }
        }

        return dialog
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        mListener.onCryptocurrencyAmountDialogCancel()
    }

    private fun onValidateAndConfirm(errorMsg: String): Boolean {
        return editTextAmount.validate({ text ->
            try {
                valueAmount = text.toDouble()
                true
            } catch (e: Throwable) {
                false
            }
        }, errorMsg)
    }

    fun getAmount(): Double {
        return valueAmount
    }

}