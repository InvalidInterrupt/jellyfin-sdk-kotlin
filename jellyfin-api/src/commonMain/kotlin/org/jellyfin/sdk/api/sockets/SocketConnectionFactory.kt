package org.jellyfin.sdk.api.sockets

import kotlinx.coroutines.CoroutineScope
import org.jellyfin.sdk.api.client.HttpClientOptions
import java.security.cert.Certificate

/**
 * The factory to create a [SocketConnection].
 */
public fun interface SocketConnectionFactory {
	public fun create(
		clientOptions: HttpClientOptions,
		scope: CoroutineScope,
		caCertificate: Certificate?,
		allowSystemCas: Boolean,
	): SocketConnection

	public fun create(
		clientOptions: HttpClientOptions, scope: CoroutineScope
	): SocketConnection = create(clientOptions, scope, caCertificate = null, allowSystemCas = true)
}
