package cube8540.oauth.authentication.credentials.oauth.security.endpoint

import cube8540.oauth.authentication.credentials.oauth.error.InvalidRequestException
import cube8540.oauth.authentication.credentials.oauth.error.RedirectMismatchException
import cube8540.oauth.authentication.credentials.oauth.error.UserDeniedAuthorizationException
import cube8540.oauth.authentication.credentials.oauth.security.AuthorizationRequest
import cube8540.oauth.authentication.credentials.oauth.security.OAuth2ClientDetails
import java.net.URI
import java.util.*
import kotlin.collections.HashSet

interface RedirectResolver {

    fun resolveRedirectURI(redirectURI: String?, clientDetails: OAuth2ClientDetails): URI
}

interface ScopeApprovalResolver {

    fun resolveApprovalScopes(originalRequest: AuthorizationRequest, approvalParameters: Map<String, String?>): Set<String>
}

class DefaultRedirectResolver: RedirectResolver {

    override fun resolveRedirectURI(redirectURI: String?, clientDetails: OAuth2ClientDetails): URI {
        if (redirectURI == null && clientDetails.registeredRedirectUris?.size?:0 == 1) {
            return clientDetails.registeredRedirectUris!!.iterator().next()
        }
        if (redirectURI == null) {
            throw InvalidRequestException.invalidRequest("redirect uri is required")
        }
        val requestingURI = URI.create(redirectURI)
        if (clientDetails.registeredRedirectUris!!.contains(requestingURI)) {
            return requestingURI
        } else {
            throw RedirectMismatchException("$redirectURI is not registered")
        }
    }
}

class DefaultScopeApprovalResolver: ScopeApprovalResolver {

    override fun resolveApprovalScopes(originalRequest: AuthorizationRequest, approvalParameters: Map<String, String?>): Set<String> {
        val approvalScopes: MutableSet<String> = HashSet()
        originalRequest.requestScopes?.forEach { scope ->
            val approvalScope = approvalParameters[scope]
            if ("true" == approvalScope?.toLowerCase()) {
                approvalScopes.add(scope)
            }
        }
        if (approvalScopes.isEmpty()) {
            throw UserDeniedAuthorizationException("User denied access")
        }
        return Collections.unmodifiableSet(approvalScopes)
    }
}