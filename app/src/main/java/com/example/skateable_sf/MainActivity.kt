package com.example.skateable_sf

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths


class MainActivity : Activity() {
    private lateinit var iis: InternetIdentityService

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iis = InternetIdentityService()
        val extPath: String = applicationContext.filesDir.path
        val pemFileEditText = findViewById<EditText>(R.id.pemFile)
        val challengeKeyEditText = findViewById<EditText>(R.id.challengeKey)
        val captchaImageView = findViewById<ImageView>(R.id.captchaImage)
        val captchaAnswerText = findViewById<EditText>(R.id.captchaAnswer)
        val deviceAliasEditText = findViewById<EditText>(R.id.deviceAlias)
        val userIdEditText = findViewById<EditText>(R.id.userId)
        val makePEMButton = findViewById<Button>(R.id.makePEM)
        val login = findViewById<Button>(R.id.getChallengeButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val addDeviceButton = findViewById<Button>(R.id.addDeviceButton)
        val removeDeviceButton = findViewById<Button>(R.id.removeDeviceButton)
        val lookupButton = findViewById<Button>(R.id.lookupButton)

        makePEMButton.setOnClickListener {
            pemFileEditText.setText(iis.createIdentity(extPath))
        }

        login.setOnClickListener {
                val challenge = iis.createChallenge(extPath)
                val captchaPath = Paths.get(extPath, "challenge.png").toString()

                // Display key for registration
                challengeKeyEditText.setText(challenge.challengeKey)

                // Convert byte[] to a Bitmap
                val captchaImageBytes = Base64.decode(challenge.pngBase64, Base64.DEFAULT)
                val captchaImage = BitmapFactory.decodeByteArray(captchaImageBytes, 0, captchaImageBytes.size)

                // Save the captcha image to a file
                val outputFile = File(captchaPath)
                val outputStream = FileOutputStream(outputFile)
                captchaImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Display captcha challenge
                captchaImageView.setImageBitmap(captchaImage)
        }

        registerButton.setOnClickListener {
            val challengeKey = challengeKeyEditText.text.toString()
            val captchaAnswer = captchaAnswerText.text.toString()

            GlobalScope.launch(Dispatchers.Main) {
                iis.register(challengeKey, "a")
            }
        }

        addDeviceButton.setOnClickListener {
            val pemFile = pemFileEditText.text.toString()
            val deviceAlias = deviceAliasEditText.text.toString()
            val userId = userIdEditText.text.toString().toLong()
            // ...
        }

        removeDeviceButton.setOnClickListener {
            val pemFile = pemFileEditText.text.toString()
            val userId = userIdEditText.text.toString().toLong()
            // ...
        }

        lookupButton.setOnClickListener {
            val pemFile = pemFileEditText.text.toString()
            val userId = userIdEditText.text.toString().toLong()
            // ...
        }


    }



}
