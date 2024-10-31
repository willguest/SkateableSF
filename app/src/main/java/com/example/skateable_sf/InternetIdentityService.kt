package com.example.skateable_sf


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import org.ic4j.agent.Agent
import org.ic4j.internetidentity.Challenge
import org.ic4j.internetidentity.ChallengeResult
import org.ic4j.internetidentity.DeviceData
import org.ic4j.internetidentity.InternetIdentityService
import org.ic4j.internetidentity.RegisterResponse
import java.io.File
import java.io.FileOutputStream
import java.util.Properties


class InternetIdentityService(private val agent: Agent, private val env: Properties) {
    private val internetIdentityService = InternetIdentityService.create(agent, env)

    fun createChallenge(captchaFile: String): Challenge {
        val challengeResponse = internetIdentityService.createChallenge()
        val challenge = challengeResponse.get()

        // Convert byte[] to a Bitmap
        val captchaImageBytes = Base64.decode(challenge.pngBase64, Base64.DEFAULT)
        val captchaImage = BitmapFactory.decodeByteArray(captchaImageBytes, 0, captchaImageBytes.size)

        // Save the captcha image to a file
        val outputFile = File(captchaFile)
        val outputStream = FileOutputStream(outputFile)
        captchaImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return challenge
    }

    fun register(device: DeviceData, challengeResult: ChallengeResult): RegisterResponse {
        val registerResponse = internetIdentityService.register(device, challengeResult)
        return registerResponse.get()
    }

    fun addDevice(userId: Long, device: DeviceData) {
        internetIdentityService.add(userId, device)
    }

    fun removeDevice(userId: Long, devicePublicKey: ByteArray) {
        internetIdentityService.remove(userId, devicePublicKey)
    }

    fun lookup(userId: Long): Array<DeviceData> {
        return internetIdentityService.lookup(userId)
    }

    // Other methods as needed
}
