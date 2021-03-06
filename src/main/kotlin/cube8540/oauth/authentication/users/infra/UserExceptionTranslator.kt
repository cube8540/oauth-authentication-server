package cube8540.oauth.authentication.users.infra

import cube8540.oauth.authentication.error.ExceptionTranslator
import cube8540.oauth.authentication.error.message.ErrorMessage
import cube8540.oauth.authentication.error.message.ErrorMessage.Companion.instance
import cube8540.oauth.authentication.users.domain.UserAuthorizationException
import cube8540.oauth.authentication.users.domain.UserInvalidException
import cube8540.oauth.authentication.users.domain.UserNotFoundException
import cube8540.oauth.authentication.users.domain.UserRegisterException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class UserExceptionTranslator: ExceptionTranslator<ErrorMessage<Any>> {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun translate(exception: Exception): ResponseEntity<ErrorMessage<Any>> = when (exception) {
        is UserNotFoundException -> {
            response(HttpStatus.NOT_FOUND, instance(exception.code, exception.message))
        }
        is UserRegisterException -> {
            response(HttpStatus.BAD_REQUEST, instance(exception.code, exception.message))
        }
        is UserInvalidException -> {
            response(HttpStatus.BAD_REQUEST, instance(exception.code, exception.errors.toTypedArray()))
        }
        is UserAuthorizationException -> {
            response(HttpStatus.FORBIDDEN, instance(exception.code, exception.message))
        }
        else -> {
            logger.error("Handle exception {} {}", exception.javaClass, exception.message)
            response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessage.UNKNOWN_SERVER_ERROR)
        }
    }
}