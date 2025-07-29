package com.github.basdgrt.config

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileNotFoundException

/**
 * Loads secrets from the secrets.yaml file.
 */
class SecretsLoader {
    companion object {
        private const val SECRETS_FILE_PATH = "src/main/resources/secrets.yaml"
        
        /**
         * Loads the secrets from the secrets.yaml file.
         * 
         * @return A map containing the secrets.
         * @throws FileNotFoundException if the secrets.yaml file is not found.
         */
        @Suppress("UNCHECKED_CAST")
        fun loadSecrets(): Map<String, String> {
            val inputStream = FileInputStream(SECRETS_FILE_PATH)
            return Yaml().load(inputStream) as Map<String, String>
        }
        
        /**
         * Gets a specific secret value by key.
         * 
         * @param key The key of the secret to get.
         * @return The secret value, or null if the key is not found.
         * @throws FileNotFoundException if the secrets.yaml file is not found.
         */
        fun getSecret(key: String): String? {
            return loadSecrets()[key]
        }
    }
}