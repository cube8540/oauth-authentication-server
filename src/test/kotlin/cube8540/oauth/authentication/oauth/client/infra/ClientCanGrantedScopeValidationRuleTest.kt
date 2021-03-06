package cube8540.oauth.authentication.oauth.client.infra

import cube8540.oauth.authentication.oauth.client.domain.OAuth2Client
import cube8540.oauth.authentication.oauth.scope.application.OAuth2ScopeDetails
import cube8540.oauth.authentication.oauth.scope.application.OAuth2ScopeDetailsService
import cube8540.oauth.authentication.security.AuthorityCode
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClientCanGrantedScopeValidationRuleTest {

    private val scopeDetailsService: OAuth2ScopeDetailsService = mockk()
    private val client: OAuth2Client = mockk()

    private val rule = ClientCanGrantedScopeValidationRule()

    init {
        rule.scopeDetailsService = scopeDetailsService
    }

    @Test
    fun `client scope is null`() {
        every { client.scopes } returns null

        val result = rule.isValid(client)
        assertThat(result).isFalse
    }

    @Test
    fun `client scope is empty`() {
        every { client.scopes } returns emptySet<AuthorityCode>().toMutableSet()

        val result = rule.isValid(client)
        assertThat(result).isFalse
    }

    @Test
    fun `client include scope not found`() {
        val requestScopes = setOf(AuthorityCode("code-1"), AuthorityCode("code-2"), AuthorityCode("code-3")).toMutableSet()
        val searchedScopes = setOf(makeAuthorityDetails("code-1"), makeAuthorityDetails("code-2")).toMutableSet()

        every { client.scopes } returns requestScopes
        every { scopeDetailsService.loadScopes() } returns searchedScopes

        val result = rule.isValid(client)
        assertThat(result).isFalse
    }

    @Test
    fun `client scopes all found`() {
        val requestScopes = setOf(AuthorityCode("code-1"), AuthorityCode("code-2"), AuthorityCode("code-3")).toMutableSet()
        val searchedScopes = setOf(makeAuthorityDetails("code-1"), makeAuthorityDetails("code-2"), makeAuthorityDetails("code-3")).toMutableSet()

        every { client.scopes } returns requestScopes
        every { scopeDetailsService.loadScopes() } returns searchedScopes

        val result = rule.isValid(client)
        assertThat(result).isTrue
    }

    private fun makeAuthorityDetails(returnsCode: String): OAuth2ScopeDetails = mockk {
        every { code } returns returnsCode
    }
}