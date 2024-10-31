package com.example.skateable_sf

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import org.ic4j.agent.Agent
import org.ic4j.agent.AgentBuilder
import org.ic4j.agent.http.ReplicaApacheHttpTransport
import org.ic4j.agent.identity.BasicIdentity
import java.util.Properties

class MainActivity : Activity() {
    private lateinit var internetIdentityService: InternetIdentityService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the InternetIdentityService instance here
        //val agent = createAgent()
        //val env = createEnv()
        //internetIdentityService = InternetIdentityService(agent, env)

        val createChallengeButton = findViewById<Button>(R.id.createChallengeButton)
        createChallengeButton.setOnClickListener {
            val challenge = internetIdentityService.createChallenge("captcha.png")
            // Update the UI with the challenge information
        }

        // Other button click listeners and UI updates here
    }

    private fun createAgent(): Agent {
        // Implement the logic to create the Agent instance here
        // For example:
        val identity = BasicIdentity.fromPEMFile(java.io.File("identity.pem").toPath())
        val transport = ReplicaApacheHttpTransport.create("http://localhost:8000")
        return AgentBuilder().transport(transport).identity(identity).build()
    }

    private fun createEnv(): Properties {
        // Implement the logic to create the Properties instance here
        // For example:
        val env = Properties()
        env.setProperty("ii.location", "http://localhost:8000")
        return env
    }
}
