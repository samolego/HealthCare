package org.samo_lego.healthcare.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.samo_lego.healthcare.HealthCare.MODID;

public class HealthConfig {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public final String _comment_blacklistedEntities = "// Which entities shouldn't have a healthbar above their name.";
    @SerializedName("blacklisted_entities")
    public ArrayList<String> blacklistedEntities = new ArrayList<>(Arrays.asList(
            "taterzens:npc",
            "specialmobs:mob_with_hidden_health"
    ));
    public String _comment_activationRange = "// When to activate the healthbar.";
    public float activationRange = 8.0F;

    public final Permissions perms = new Permissions();

    public final String _comment_maxHealthbarLength = "// Max length of healthbar a player can use.";
    public int maxHealthbarLength = 20;

    public static final class Permissions {
        public final String _comment = "// Enabled only if LuckPerms is loaded.";
        public final String healthcare_reloadConfig = "healthcare.reloadConfig";

        public final String _comment_playerPermissions = "// Player permissions";
        public final String healthbar_toggle = "healthcare.healthbar.toggle";
        public final String healthbar_edit_style = "healthcare.healthbar.edit.style";
        public final String healthbar_edit_visibility = "healthcare.healthbar.edit.visibility";
        public final String healthbar_edit_custom_length = "healthcare.healthbar.edit.custom.length";
        public final String healthbar_edit_custom_symbols_full = "healthcare.healthbar.edit.symbol.full";
        public final String healthbar_edit_custom_symbols_empty = "healthcare.healthbar.edit.symbol.empty";
    }

    public Language lang = new Language();
    public static class Language {

        public String noPermission = "You don't have permission to run this command.";
        public String configReloaded = "Config was reloaded successfully.";
        public String customLengthSet = "Length of healthbar was set to %s.";
        public String customSymbolSet = "%s healthbar symbol was set to %s.";
        public String visibilitySet = "Always-visible property of healthbar was set to: %s";
        public String styleSet = "Style of your healthbar has been set to %s.";
        public String useCustomStyle = "Make sure to use style CUSTOM to have your settings applied.";
        public String healthbarEnabled = "Healthbars are now enabled.";
        public String healthbarDisabled = "Healthbars are now disabled.";
        public String reloadRequired = "Changes will be visible after relogging or entity update.";
    }

    /**
     * Loads language file.
     *
     * @param file file to load the language file from.
     * @return HealthConfig object
     */
    public static HealthConfig loadConfigFile(File file) {
        HealthConfig config;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = gson.fromJson(fileReader, HealthConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(MODID + " Problem occurred when trying to load config: ", e);
            }
        }
        else {
            config = new HealthConfig();
        }
        config.saveConfigFile(file);

        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MODID).error("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
