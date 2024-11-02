package com.example.skateable_sf

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.jcajce.JcaPEMWriter
import org.ic4j.agent.Agent
import org.ic4j.agent.AgentBuilder
import org.ic4j.agent.ProxyBuilder
import org.ic4j.agent.ReplicaTransport
import org.ic4j.agent.http.ReplicaApacheHttpTransport
import org.ic4j.agent.identity.BasicIdentity
import org.ic4j.agent.identity.Identity
import org.ic4j.internetidentity.Challenge
import org.ic4j.internetidentity.ChallengeResult
import org.ic4j.internetidentity.DeviceData
import org.ic4j.internetidentity.DeviceProtection
import org.ic4j.internetidentity.IdentityAnchorInfo
import org.ic4j.internetidentity.InternetIdentityService
import org.ic4j.internetidentity.KeyType
import org.ic4j.internetidentity.Purpose
import org.ic4j.internetidentity.RegisterResponse
import org.ic4j.types.Principal
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
import java.util.Properties
import java.util.concurrent.CompletableFuture
import kotlin.io.path.exists


class CanisterService(fileStoragePath: String) {

    private var PROPERTIES_FILE_NAME = "application.properties"
    private var LOG: Logger = LoggerFactory.getLogger(InternetIdentityService::class.java)
    private var fileStoragePath: String? = fileStoragePath

    private var pemFile = "identity.pem"
    private var devicePemFile: String? = null
    private val deviceAlias = "Device1"
    private val userId: Long? = null

    private lateinit var sessionKey: ByteArray

    fun getDeviceAlias(): String {
        return this.deviceAlias
    }

    private fun createIdentity() : String {
        setupBouncyCastle()
        val keyPair: KeyPair
        try {
            keyPair = KeyPairGenerator.getInstance(
                "Ed25519", BouncyCastleProvider.PROVIDER_NAME)
                .generateKeyPair()
            val identity: Identity = BasicIdentity.fromKeyPair(keyPair)
            this.devicePemFile = Paths.get(fileStoragePath, pemFile).toString()

            val outputFile = File("$this.fileStoragePath/$pemFile")
            val outputStream = FileOutputStream(outputFile)
            val pemWriter = JcaPEMWriter(outputStream.writer())
            pemWriter.writeObject(keyPair.private)
            pemWriter.close()
            return pemFile
        } catch (e: NoSuchAlgorithmException) {
            return e.localizedMessage!!.toString()
        } catch (e: IOException) {
            return e.localizedMessage!!.toString()
        } catch (e: NoSuchProviderException) {
            return e.localizedMessage!!.toString()
        }
    }


    fun createChallenge(): Challenge {
        val internetIdentityService = this.createInternetIdentityService()
        val challengeResponse = internetIdentityService.createChallenge()
        return challengeResponse.get()
    }

    fun register(challengeKey: String, captchaAnswer: String) : Long {
        try {
            val internetIdentityService = this.createInternetIdentityService()

            val challengeResult = ChallengeResult()
            challengeResult.challengeKey = challengeKey
            challengeResult.chars = captchaAnswer

            val device = DeviceData()
            device.alias = this.deviceAlias
            device.pubkey = this.sessionKey
            val purpose : Purpose = Purpose.authentication
            device.purpose = purpose
            val keyType : KeyType = KeyType.platform
            device.keyType = keyType
            device.protection = DeviceProtection.isunprotected
            //device.credentialId  = Optional.empty()  // this causes a ClassCastException

            val registerResponse : CompletableFuture<RegisterResponse> =
                internetIdentityService.register(device, challengeResult)

            val result : RegisterResponse = registerResponse.get()

            return if (result == RegisterResponse.registered) {
                LOG.info("User registered:" + result.registeredValue.userNumber.toString())
                result.registeredValue.userNumber
            } else 0
        } catch (e: java.lang.Exception) {
            LOG.error(e.localizedMessage, e)
            return -1
        }
    }

    fun addDevice() {
        try {
            val internetIdentityService = this.createInternetIdentityService()
            val keyPair =
                KeyPairGenerator.getInstance("Ed25519", BouncyCastleProvider.PROVIDER_NAME)
                    .generateKeyPair()
            if (devicePemFile == null) devicePemFile = "$deviceAlias.pem"
            InternetIdentityService.savePrivateKey(
                keyPair.private,
                devicePemFile
            )
            LOG.info("Created device $deviceAlias identity PEM file $devicePemFile")
            val device = DeviceData()
            device.alias = deviceAlias
            device.pubkey = keyPair.public.encoded
            device.protection = DeviceProtection.isunprotected
            //device.credentialId = Optional.empty()
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

    private fun getAgent(transport: ReplicaTransport, identity: BasicIdentity): Agent {
        val agent = AgentBuilder().transport(transport).identity(identity).build()
        agent.fetchRootKey()
        agent.setVerify(false)
        return agent
    }

    private fun getProperties() : Properties{
        val propInputStream: InputStream? =
            javaClass.classLoader.getResourceAsStream(this.PROPERTIES_FILE_NAME)
        val p = Properties()
        p.load(propInputStream)
        propInputStream?.close()
        return p
    }

    @Throws(IOException::class, URISyntaxException::class, NoSuchAlgorithmException::class)
    private fun createInternetIdentityService(): InternetIdentityService {
        setupBouncyCastle()
        val pemPath = Paths.get(this.fileStoragePath, this.pemFile)

        if (!pemPath.exists() && !this.fileStoragePath.isNullOrEmpty()){
            LOG.info("no identity found, creating it...")
            createIdentity()
        }

        LOG.info("starting II service")

        this.devicePemFile = pemPath.toString()
        val identity: BasicIdentity = BasicIdentity.fromPEMFile(pemPath)
        this.sessionKey = identity.derEncodedPublickey
        val env: Properties = getProperties()
        val iiLocation = env.getProperty("ii.location")
        val transport = ReplicaApacheHttpTransport.create(iiLocation)
        val agent = getAgent(transport, identity)
        return InternetIdentityService.create(agent, env)
    }

    fun createSkateProxy(): String {
        setupBouncyCastle()
        val identity: BasicIdentity = BasicIdentity.fromPEMFile(
            Paths.get(this.fileStoragePath, this.pemFile))
        this.sessionKey = identity.derEncodedPublickey
        val env: Properties = getProperties()
        val icLocation = env.getProperty("sc.location")
        val icCanister = env.getProperty("sc.canister")
        val transport = ReplicaApacheHttpTransport.create(icLocation)
        val agent = getAgent(transport, identity)
        val skateProxy = ProxyBuilder.create(agent, Principal.fromString(icCanister))
            .getProxy(SkateProxy::class.java)

        val proxyResponse: CompletableFuture<Principal>? = skateProxy.idQuick()
        LOG.info("proxy response: " + proxyResponse.toString())

        val canisterId = proxyResponse?.get()
        LOG.info("who am i: " + canisterId.toString())

        return canisterId.toString()
    }

    suspend fun lookup(userId: Long) {
        try {
            val internetIdentityService = this.createInternetIdentityService()

            LOG.info("iis: " + internetIdentityService.toString())

            val identityAnchorInfoResponse : IdentityAnchorInfo? =
                withContext(Dispatchers.IO) {
                    internetIdentityService.getAnchorInfo(userId).get()
                }

            LOG.info("info response: " + identityAnchorInfoResponse.toString())

            val deviceDataWithUsage = identityAnchorInfoResponse?.devices

            for (i in deviceDataWithUsage!!.indices)
                LOG.info(deviceDataWithUsage[i].alias)

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
