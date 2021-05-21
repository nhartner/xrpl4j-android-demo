package com.ripple.xrpl4j.demo

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.common.io.BaseEncoding
import org.xrpl.xrpl4j.keypairs.Ed25519KeyPairService

class MainActivity : AppCompatActivity() {

    val keypairService: Ed25519KeyPairService = Ed25519KeyPairService.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    /** Called when the user taps the Generate Seed button  */
    fun handleGenerateSeed(view: View?) {
        val seed = generateSeed()
        val seedInput = getSeedEditText()
        seedInput.setText(seed)
    }

    /** Called when the user taps the Sign button  */
    fun handleSign(view: View?) {
        safely {
            hideKeyboard()
            val encodedMessage = getHexEncodedMessage()
            val seed = valueOf(getSeedEditText())
            val signatureInput = getSignatureEditText()

            val result = sign(seed, encodedMessage)

            signatureInput.setText(result)
            getVerifyResultTextView().setText("")
        }
    }

    fun handleVerify(view: View?) {
        safely {
            hideKeyboard()
            val encodedMessage = getHexEncodedMessage()
            val seed = valueOf(getSeedEditText())
            val signature = valueOf(getSignatureEditText())

            val verified = verify(seed, encodedMessage, signature)
            val verifyResult = getVerifyResultTextView()
            verifyResult.setText(if (verified) "Verified" else "Failed")
            verifyResult.setTextColor(if (verified) ColorStateList.valueOf(Color.GREEN) else ColorStateList.valueOf(Color.RED))
        }
    }

    private fun sign(seed: String, encodedMessage: String?): String? {
        val key = keypairService.deriveKeyPair(seed)
        val result = keypairService.sign(encodedMessage, key.privateKey())
        return result
    }

    private fun getHexEncodedMessage(): String? {
        val message = getMessageEditText()
        val encodedMessage = BaseEncoding.base16().encode(message.text.toString()?.toByteArray())
        return encodedMessage
    }

    private fun generateSeed() = Ed25519KeyPairService.getInstance().generateSeed()

    private fun verify(seed: String, encodedMessage: String?, signature: String): Boolean {
        val key = keypairService.deriveKeyPair(seed)
        val verified = keypairService.verify(encodedMessage, signature, key.publicKey())
        return verified
    }

    private fun safely(runnable: () -> Unit) {
        try {
            runnable.invoke()
        }
        catch (e: Exception) {
            e.printStackTrace()
            showError("Oops, something went wrong")
        }
    }

    private fun hideKeyboard() {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(
            getSignButton().windowToken,
            InputMethodManager.RESULT_UNCHANGED_SHOWN
        )
    }

    private fun getSignButton() = findViewById<View>(R.id.button) as Button

    private fun getSignatureEditText() = findViewById<View>(R.id.signature) as EditText

    private fun getSeedEditText() = findViewById<View>(R.id.seed) as EditText

    private fun getMessageEditText() = findViewById<View>(R.id.message) as EditText

    private fun getVerifyResultTextView() = findViewById<View>(R.id.verifyResult) as TextView

    private fun valueOf(input: EditText) = input.text.toString().trim()

    private fun showError(error: String) {
        val alertDialog: AlertDialog = AlertDialog.Builder(this@MainActivity).create()
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(error)
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ -> dialog.dismiss() }
        alertDialog.show()
    }
}
