package com.github.basdgrt.config

import org.yaml.snakeyaml.Yaml
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Loads secrets from the secrets.yaml file.
 */
class SecretsLoader {
    companion object {
        private const val SECRETS_FILE_PATH = "src/main/resources/secrets.yaml"
        private const val CLASSPATH_SECRETS_FILE = "/resources/secrets.yaml"

        /**
         * Loads the secrets from the secrets.yaml file.
         * 
         * @return A map containing the secrets.
         * @throws FileNotFoundException if the secrets.yaml file is not found.
         */
        @Suppress("UNCHECKED_CAST")
        fun loadSecrets(): Map<String, String> {
            val inputStream = getSecretsInputStream()
            return Yaml().load(inputStream) as Map<String, String>
        }

        /**
         * Gets the input stream for the secrets file, trying first from the classpath
         * and falling back to the file system.
         */
        private fun getSecretsInputStream(): InputStream {
            // First try to load from classpath (for JAR deployment)
            val classpathStream = SecretsLoader::class.java.getResourceAsStream(CLASSPATH_SECRETS_FILE)
            if (classpathStream != null) {
                return classpathStream
            }

            // Fall back to file system (for local development)
            return FileInputStream(SECRETS_FILE_PATH)
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
