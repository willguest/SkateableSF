package com.example.skateable_sf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.ic4j.agent.Agent
import org.ic4j.agent.AgentBuilder
import org.ic4j.agent.ReplicaTransport
import org.ic4j.agent.http.ReplicaApacheHttpTransport
import org.ic4j.agent.identity.BasicIdentity
import org.ic4j.agent.identity.Identity
import org.ic4j.internetidentity.Challenge
import org.ic4j.internetidentity.ChallengeResult
import org.ic4j.internetidentity.DeviceData
import org.ic4j.internetidentity.DeviceProtection
import org.ic4j.internetidentity.InternetIdentityService
import org.ic4j.internetidentity.KeyType
import org.ic4j.internetidentity.Purpose
import org.ic4j.internetidentity.RegisterResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.URISyntaxException
import java.nio.file.Paths
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.Security
import java.util.Optional
import java.util.Properties

class InternetIdentityService() {

    var PROPERTIES_FILE_NAME = "application.properties"
    var LOG: Logger = LoggerFactory.getLogger(InternetIdentityService::class.java)

    private var pemFile = "identity.pem"
    private var devicePemFile: String? = null
    private var fileStoragePath: String? = null
    private val deviceAlias = "Device1"
    private var envFile: String? = null
    private val userId: Long? = null

    lateinit var sessionKey: ByteArray

    fun createIdentity(keyPath: String): String {
        setupBouncyCastle()
        val keyPair: KeyPair
        try {
            keyPair = KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
                .generateKeyPair()
            val identity: Identity = BasicIdentity.fromKeyPair(keyPair)

            this.fileStoragePath = keyPath
            this.devicePemFile = Paths.get(fileStoragePath, pemFile).toString()

            val outputFile = File(keyPath + '/' + pemFile)
            val outputStream = FileOutputStream(outputFile)
            val pemWriter = JcaPEMWriter(outputStream.writer())
            pemWriter.writeObject(keyPair.private)
            pemWriter.close()

            this.envFile = PROPERTIES_FILE_NAME

            LOG.info("Created identity PEM file " + pemFile)
            return pemFile
        } catch (e: NoSuchAlgorithmException) {
            LOG.error(e.localizedMessage, e)
            return e.toString()
        } catch (e: IOException) {
            LOG.error(e.localizedMessage, e)
            return e.toString()
        } catch (e: NoSuchProviderException) {
            LOG.error(e.localizedMessage, e)
            return e.toString()
        }
    }

    fun createChallenge(captchaPath: String): Challenge {
        val internetIdentityService = createInternetIdentityService()
        val challengeResponse = internetIdentityService.createChallenge()
        return challengeResponse.get()
    }

    suspend fun register(challengeKey: String, captchaAnswer: String) = withContext(Dispatchers.IO) {
        try {
            val internetIdentityService = createInternetIdentityService()

            val challengeResult = ChallengeResult()
            challengeResult.challengeKey = challengeKey
            challengeResult.chars = captchaAnswer

            val device = DeviceData()
            device.alias = deviceAlias
            device.pubkey = this@InternetIdentityService.sessionKey
            val purpose = Purpose.authentication
            device.purpose = purpose
            val keyType = KeyType.platform
            device.keyType = keyType
            device.protection = DeviceProtection.isunprotected
            device.credentialId = Optional.empty()

            LOG.info("inputs created: $challengeResult, $device")

            val registerResponse = internetIdentityService.register(device, challengeResult)

            LOG.info("registerResponse created")

            val result = registerResponse.await()

            LOG.info("Registration status:" + result.name)

            if (result == RegisterResponse.registered) {
                LOG.info("User Id:" + result.registeredValue.userNumber.toString())
            }
        } catch (e: java.lang.Exception) {
            LOG.error(e.localizedMessage, e)
        }
    }

    fun addDevice() {
        try {
            val internetIdentityService = createInternetIdentityService()
            val keyPair =
                KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
                    .generateKeyPair()
            if (devicePemFile == null) devicePemFile = "$deviceAlias.pem"
            InternetIdentityService.savePrivateKey(
                keyPair.private,
                devicePemFile
            )
            LOG.info("Created device " + deviceAlias + " identity PEM file " + devicePemFile)
            val device = DeviceData()
            device.alias = deviceAlias
            device.pubkey = keyPair.public.encoded
            device.protection = DeviceProtection.isunprotected
            device.credentialId = Optional.empty()
            val purpose = Purpose.authentication
            device.purpose = purpose
            val keyType = KeyType.platform
            device.keyType = keyType
            internetIdentityService.add(userId, device)
        } catch (e: java.lang.Exception) {
            LOG.error(e.localizedMessage, e)
        }
    }

    fun removeDevice() {
        try {
            val internetIdentityService: InternetIdentityService =
                this.createInternetIdentityService()
            val identity: BasicIdentity = BasicIdentity.fromPEMFile(Paths.get(this.devicePemFile))
            internetIdentityService.remove(this.userId, identity.derEncodedPublickey)
        } catch (e: Exception) {
            LOG.error(e.localizedMessage, e)
        }
    }

    private fun fetchRootKey(transport: ReplicaTransport, identity: BasicIdentity): Agent {
        val agent = AgentBuilder().transport(transport).identity(identity).build()
        agent.fetchRootKey()
        agent.setVerify(false)
        return agent
    }

    @Throws(IOException::class, URISyntaxException::class, NoSuchAlgorithmException::class)
    private fun createInternetIdentityService(): InternetIdentityService {
        setupBouncyCastle()
        val identity: BasicIdentity = BasicIdentity.fromPEMFile(Paths.get(this.devicePemFile))
        this.sessionKey = identity.derEncodedPublickey
        val env: Properties

        if (this.envFile != null) {
            //LOG.info("looking for " + this@InternetIdentityService.envFile.toString())
            val propInputStream: InputStream = javaClass.classLoader.getResourceAsStream(this.envFile)
            env = Properties()
            env.load(propInputStream)
            propInputStream.close()
        } else
            env = InternetIdentityService.getIIProperties()

        val iiLocation = env.getProperty("ii.location")
        val transport = ReplicaApacheHttpTransport.create(iiLocation)
        val agent = fetchRootKey(transport, identity)
        return InternetIdentityService.create(agent, env)
    }

    fun lookup() {
        try {
            val internetIdentityService = createInternetIdentityService()
            val identityAnchorInfoResponse =
                internetIdentityService.getAnchorInfo(this.userId).get()
            val deviceDataWithUsage = identityAnchorInfoResponse.devices
            LOG.info("User Id:" + this.userId)
            for (i in deviceDataWithUsage.indices) LOG.info(
                deviceDataWithUsage[i].alias
            )
        } catch (e: java.lang.Exception) {
            LOG.error(e.localizedMessage, e)
        }
    }

    private fun setupBouncyCastle() {
        val provider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME)
            ?: // Web3j will set up the provider lazily when it's first used.
            return
        if (provider == BouncyCastleProvider::class.java) {
            // BC with same package name, shouldn't happen in real life.
            return
        }
        // Android registers its own BC provider. As it might be outdated and might not include
        // all needed ciphers, we substitute it with a known BC bundled in the app.
        // Android's BC has its package rewritten to "com.android.org.bouncycastle" and because
        // of that it's possible to have another BC implementation loaded in VM.
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME)
        Security.insertProviderAt(BouncyCastleProvider(), 1)
    }
}
