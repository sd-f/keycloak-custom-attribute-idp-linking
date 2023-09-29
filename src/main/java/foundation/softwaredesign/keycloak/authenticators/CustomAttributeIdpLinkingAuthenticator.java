package foundation.softwaredesign.keycloak.authenticators;

import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.broker.IdpAutoLinkAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.events.Errors;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomAttributeIdpLinkingAuthenticator extends IdpAutoLinkAuthenticator {

  private static final Logger log = Logger.getLogger(CustomAttributeIdpLinkingAuthenticator.class);

  @Override
  protected void authenticateImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
      BrokeredIdentityContext brokerContext) {

    AuthenticatorConfigModel config = validateConfig(context);
    String failOnNoMatch = config.getConfig()
        .get(CustomAttributeIdpLinkingAuthenticatorFactory.CONFIG_FAIL_ON_NO_MATCH_ATTRIBUTE);
    UserModel existingUser = findMatchingUser(context, brokerContext, config);

    if (existingUser != null) {
      log.debugf(
          "User '%s' is set to authentication context when link with identity provider '%s' . Identity provider username is '%s' ",
          existingUser.getUsername(),
          brokerContext.getIdpConfig().getAlias(), brokerContext.getUsername());

      context.setUser(existingUser);
      context.success();
    } else {
      if (failOnNoMatch.equals("true")) {
        sendFailureChallenge(context, Response.Status.BAD_REQUEST, Errors.USER_NOT_FOUND,
            Messages.IDENTITY_PROVIDER_UNEXPECTED_ERROR, AuthenticationFlowError.ACCESS_DENIED);
      }
      context.attempted();
    }

  }

  private AuthenticatorConfigModel validateConfig(AuthenticationFlowContext context) {
    if (context.getAuthenticatorConfig() == null) {
      log.warn("Config must not be empty.");
      return null;
    }
    if (context.getAuthenticatorConfig().getConfig() == null) {
      log.warn("Config must not be empty.");
      return null;
    }
    return context.getAuthenticatorConfig();
  }

  protected UserModel findMatchingUser(AuthenticationFlowContext context, BrokeredIdentityContext brokerContext,
      AuthenticatorConfigModel config) {

    String idpAttribute = config.getConfig()
        .get(CustomAttributeIdpLinkingAuthenticatorFactory.CONFIG_IDP_ATTRIBUTE);

    if (idpAttribute == null) {
      log.warn("Identiy provider attribute must not be null.");
      return null;
    }

    if (idpAttribute.isEmpty()) {
      log.warn("Identiy provider attribute must not be empty.");
      return null;
    }

    String attribute = config.getConfig()
        .get(CustomAttributeIdpLinkingAuthenticatorFactory.CONFIG_LOOKUP_ATTRIBUTE);

    if (attribute == null) {
      log.warn("Lookup Attribute must not be null.");
      return null;
    }

    if (attribute.isEmpty()) {
      log.warn("Lookup Attribute must not be empty.");
      return null;
    }

    Object o = brokerContext.getContextData().get(idpAttribute);
    String value = null;
    if (o != null) {
      if (o instanceof ArrayList) {
        List list = (ArrayList) o;
        if (!list.isEmpty()) {
          if (list.get(0) instanceof String) {
            value = (String) list.get(0);
          }
        }
      } else if (o instanceof String) {
        value = (String) o;
      } else {
        log.warn("Unknown type of user attribute value.");
      }
    }
    log.debug("Identity provider attribute  \"" + idpAttribute + "\": " + value);
    if (value != null) {
      Optional<UserModel> user = context.getSession().users()
          .searchForUserByUserAttributeStream(context.getRealm(), attribute, value)
          .findFirst();

      if (user.isPresent()) {
        return user.get();
      } else {
        log.debug("No user found with \"" + attribute + "\" == " + value);
      }
    }
    return null;
  }

  @Override
  protected void actionImpl(AuthenticationFlowContext context, SerializedBrokeredIdentityContext serializedCtx,
      BrokeredIdentityContext brokerContext) {
  }

}
