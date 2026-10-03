package voice.core.sync

/**
 * Supplies short-lived OAuth access tokens to the Drive transport.
 *
 * Android authorization is deliberately kept outside the HTTP client so
 * consent can be requested when the user explicitly chooses cloud sync.
 */
public fun interface DriveAccessTokenProvider {
  public suspend fun accessToken(): String
}
