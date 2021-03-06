package jenkins.plugins.rocketchatnotifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;

import hudson.security.ACL;
import jenkins.model.Jenkins;
import jenkins.plugins.rocketchatnotifier.rocket.RocketChatClient;
import jenkins.plugins.rocketchatnotifier.rocket.RocketChatClientImpl;
import sun.security.validator.ValidatorException;

public class RocketClientWebhookImpl implements RocketClient {

  private static final Logger LOGGER = Logger.getLogger(RocketClientImpl.class.getName());

  private RocketChatClient client;

  public RocketClientWebhookImpl(String serverUrl, boolean trustSSL, String token, String tokenCredentialId) {
    client = new RocketChatClientImpl(serverUrl, trustSSL, getTokenToUse(tokenCredentialId, token));
  }

  public boolean publish(String message) {
    try {
      LOGGER.fine("Starting sending message to webhook");
      this.client.send("", message);
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "I/O error error during publishing message", e);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error error during publishing message", e);
      return false;
    }
  }

  @Override
  public boolean publish(final String message, final String emoji, final String avatar) {
    try {
      LOGGER.fine("Starting sending message to webhook");
      this.client.send("", message, emoji, avatar);
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "I/O error error during publishing message", e);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error error during publishing message", e);
      return false;
    }
  }

  @Override
  public boolean publish(final String message, final String emoji, final String avatar,
      final List<Map<String, Object>> attachments) {
    try {
      LOGGER.fine("Starting sending message to webhook");
      this.client.send("", message, emoji, avatar, attachments);
      return true;
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "I/O error error during publishing message", e);
      return false;
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error error during publishing message", e);
      return false;
    }
  }

  @Override
  public void validate() throws ValidatorException, IOException {
    this.client.send("", "Test message from Jenkins via Webhook");
  }

  private String getTokenToUse(String tokenCredentialId, String tokenString) {
    if (!StringUtils.isEmpty(tokenCredentialId)) {
      StringCredentials credentials = lookupCredentials(tokenCredentialId);
      if (credentials != null) {
        LOGGER.fine("Using Integration Token Credential ID.");
        return credentials.getSecret().getPlainText();
      }
    }

    LOGGER.fine("Using Integration Token.");

    return tokenString;
  }

  private StringCredentials lookupCredentials(String credentialId) {
    List<StringCredentials> credentials = com.cloudbees.plugins.credentials.CredentialsProvider.lookupCredentials(
        StringCredentials.class, Jenkins.getInstance(), ACL.SYSTEM, Collections.<DomainRequirement>emptyList());
    CredentialsMatcher matcher = CredentialsMatchers.withId(credentialId);
    return CredentialsMatchers.firstOrNull(credentials, matcher);
  }
}
