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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import java.security.Principal


public class MainActivity : Activity() {

    private lateinit var cService: CanisterService
    private var LOG: Logger = LoggerFactory.getLogger(MainActivity::class.java)

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cService = CanisterService(applicationContext.filesDir.path)

        setContentView(R.layout.activity_main)

        val challengeKeyEditText = findViewById<EditText>(R.id.challengeKey)
        val captchaImageView = findViewById<ImageView>(R.id.captchaImage)
        val captchaAnswerText = findViewById<EditText>(R.id.captchaAnswer)
        val deviceAliasEditText = findViewById<EditText>(R.id.deviceAlias)
        val userIdEditText = findViewById<EditText>(R.id.userId)
        val getChallenge = findViewById<Button>(R.id.getChallengeButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val addDeviceButton = findViewById<Button>(R.id.addDeviceButton)
        val removeDeviceButton = findViewById<Button>(R.id.removeDeviceButton)
        val lookupButton = findViewById<Button>(R.id.lookupButton)
        val testButton = findViewById<Button>(R.id.testButton)

        getChallenge.setOnClickListener {
                val challenge = cService.createChallenge()
                val captchaPath = Paths.get(applicationContext.filesDir.path,
                    "challenge.png").toString()

                // Display key for registration
                challengeKeyEditText.setText(challenge.challengeKey)

                // Convert byte[] to a Bitmap
                val captchaImageBytes = Base64.decode(challenge.pngBase64, Base64.DEFAULT)
                val captchaImage = BitmapFactory.decodeByteArray(
                    captchaImageBytes, 0, captchaImageBytes.size)

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

            // Register device using the captcha
            val userId = cService.register(challengeKey, captchaAnswer)

            // Display user id and device alias
            userIdEditText.setText(userId.toString())
            deviceAliasEditText.setText(cService.getDeviceAlias())
        }

        addDeviceButton.setOnClickListener {
            val deviceAlias = deviceAliasEditText.text.toString()
            val userId = userIdEditText.text.toString().toLong()
            // ...
        }

        removeDeviceButton.setOnClickListener {
            val userId = userIdEditText.text.toString().toLong()
            // ...
        }

        lookupButton.setOnClickListener {
            if (userIdEditText.text.toString().isNotEmpty()) {
                val userId = userIdEditText.text.toString().toLong()


                GlobalScope.launch(Dispatchers.Main) {
                    cService.lookup(userId)
                }
            }
        }

        testButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val pString: String = cService.createSkateProxy()
            }

            //val proxyResponse: CompletableFuture<Principal>? = skateProxy.idQuick()
            //val canisterId = proxyResponse?.get()
            //LOG.info("who am i: " + canisterId.toString())
        }

    }
}
