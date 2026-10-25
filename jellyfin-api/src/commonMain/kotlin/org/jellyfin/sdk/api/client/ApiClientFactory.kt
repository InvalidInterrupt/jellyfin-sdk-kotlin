package org.jellyfin.sdk.api.client

import org.jellyfin.sdk.api.sockets.SocketConnectionFactory
import org.jellyfin.sdk.model.ClientInfo
import org.jellyfin.sdk.model.DeviceInfo
import java.security.cert.Certificate

public fun interface ApiClientFactory {
	@Suppress("LongParameterList")
	public fun create(
		baseUrl: String?,
		accessToken: String?,
		clientInfo: ClientInfo,
		deviceInfo: DeviceInfo,
		httpClientOptions: HttpClientOptions,
		socketConnectionFactory: SocketConnectionFactory,
		caCertificate: Certificate?,
		allowSystemCas: Boolean,
	): ApiClient

	@Suppress("LongParameterList")
	public fun create(
		baseUrl: String?,
		accessToken: String?,
		clientInfo: ClientInfo,
		deviceInfo: DeviceInfo,
		httpClientOptions: HttpClientOptions,
		socketConnectionFactory: SocketConnectionFactory,
	): ApiClient = create(
		baseUrl,
		accessToken,
		clientInfo,
		deviceInfo,
		httpClientOptions,
		socketConnectionFactory,
		caCertificate = null,
		allowSystemCas = true,
	)
}
