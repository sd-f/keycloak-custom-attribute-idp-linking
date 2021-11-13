package foundation.softwaredesign.keycloak.authenticators;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.authentication.ConfigurableAuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

import java.util.List;

public class CustomAttributeIdpLinkingAuthenticatorFactory implements AuthenticatorFactory, ConfigurableAuthenticatorFactory {

  public static final String PROVIDER_ID = "custom-attribute-idp-linking";
  public static final String CONFIG_IDP_ATTRIBUTE = "caila-idp-attribute";
  public static final String CONFIG_LOOKUP_ATTRIBUTE = "caila-lookup-attribute";

  private static final Logger log = Logger.getLogger(CustomAttributeIdpLinkingAuthenticatorFactory.class);

  static CustomAttributeIdpLinkingAuthenticator SINGLETON = new CustomAttributeIdpLinkingAuthenticator();

  @Override
  public Authenticator create(KeycloakSession session) {
    log.debug("Authenticator: custom-attribute-idp-linking created.");
    return SINGLETON;
  }

  @Override
  public void init(Config.Scope config) {
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {
  }

  @Override
  public void close() {
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getReferenceCategory() {
    return "idp-auto-linking";
  }

  @Override
  public boolean isConfigurable() {
    return true;
  }

  @Override
  public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
    return REQUIREMENT_CHOICES;
  }

  @Override
  public String getDisplayType() {
    return "Custom Attribute IDP Linking";
  }

  @Override
  public String getHelpText() {
    return "Lookup exiting user by custom Attribute";
  }

  @Override
  public List<ProviderConfigProperty> getConfigProperties() {
    return ProviderConfigurationBuilder.create()
        .property().name(CONFIG_IDP_ATTRIBUTE)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("Identity provider user attribute")
        .helpText("Attribute from identity provider to match against existing users.")
        .defaultValue("user.attributes.eid")
        .add()
        .property().name(CONFIG_LOOKUP_ATTRIBUTE)
        .type(ProviderConfigProperty.STRING_TYPE)
        .label("Lookup Attribute")
        .helpText("User attribute used to compare to identity provider attribute.")
        .defaultValue("eid")
        .add()
        .build();
  }

  @Override
  public boolean isUserSetupAllowed() {
    return true;
  }

}
