package foundation.softwaredesign.keycloak.authenticators;

import org.jboss.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.UserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomAttributeIdpLinkingAuthenticator extends IdpCreateUserIfUniqueAuthenticator {

  private static final Logger log = Logger.getLogger(CustomAttributeIdpLinkingAuthenticator.class);


  @Override protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String username,
      SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
    ExistingUserInfo existingUserInfo = super.checkExistingUser(context, username, serializedCtx, brokerContext);

    if (existingUserInfo != null) {
      return existingUserInfo;
    }

    if (context.getAuthenticatorConfig() == null) {
      log.warn("Config must not be empty.");
      return null;
    }
    if (context.getAuthenticatorConfig().getConfig() == null) {
      log.warn("Config must not be empty.");
      return null;
    }

    String idpAttribute = context.getAuthenticatorConfig().getConfig()
        .get(CustomAttributeIdpLinkingAuthenticatorFactory.CONFIG_IDP_ATTRIBUTE);

    if (idpAttribute == null) {
      log.warn("Identiy provider attribute must not be null.");
      return null;
    }

    if (idpAttribute.isEmpty()) {
      log.warn("Identiy provider attribute must not be empty.");
      return null;
    }

    String attribute = context.getAuthenticatorConfig().getConfig()
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
        return new ExistingUserInfo(user.get().getId(), UserModel.USERNAME, user.get().getUsername());
      } else {
        log.debug("No user found with \"" + attribute + "\" == " + value);
      }
    }
    return null;
  }

}
