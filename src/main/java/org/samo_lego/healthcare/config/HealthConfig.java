package org.samo_lego.healthcare.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import net.fabricmc.loader.api.FabricLoader;
import org.samo_lego.config2brigadier.common.IBrigadierConfigurator;
import org.samo_lego.config2brigadier.common.annotation.BrigadierDescription;
import org.samo_lego.healthcare.healthbar.HealthbarStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.samo_lego.healthcare.HealthCare.CONFIG_FILE;
import static org.samo_lego.healthcare.HealthCare.MODID;

public class HealthConfig implements IBrigadierConfigurator {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public final String _comment_blacklistedEntities = "// Which entities shouldn't have a healthbar above their name.";
    @SerializedName("blacklisted_entities")
    @BrigadierDescription(defaultOption = "[\"taterzens:npc\",\"specialmobs:mob_with_hidden_health\"]")
    public List<String> blacklistedEntities = Arrays.asList(
            "taterzens:npc",
            "specialmobs:mob_with_hidden_health"
    );
    @SerializedName("// When to activate the healthbar.")
    public String _comment_activationRange = "";
    @BrigadierDescription(defaultOption = "8.0")
    public float activationRange = 8.0F;

    @SerializedName("// Max length of healthbar a player can use.")
    public final String _comment_maxHealthbarLength = "";
    @BrigadierDescription(defaultOption = "20")
    public int maxHealthbarLength = 20;

    @SerializedName("// Whether to show entity type next to health.")
    public final String _comment_showType = "";
    @BrigadierDescription(defaultOption = "true")
    public boolean showType = true;


    @SerializedName("// Compatibility with Nameplate / other mob 'buffing' mods.")
    public String _comment_mobLevels = "";
    @SerializedName("mob_levels")
    public MobLevels mobLevels = new MobLevels();
    public static class MobLevels {
        @SerializedName("// Whether to show mob level")
        public final String _comment_showMobLevel = "";
        @BrigadierDescription(defaultOption = "true")
        public boolean showMobLevel = true;

        @SerializedName("// Mob level display text")
        public final String _comment_mobLevelText = "";
        @BrigadierDescription(defaultOption = "\"[Lv. %d]\"")
        public String mobLevelText = "[Lv. %d]";

        @SerializedName("// Mob level multiplier. Used in formula from Nameplate")
        public final String _comment_mobLevelMultiplier = "";
        @SerializedName("level_multiplier")
        public int mobLevelMultiplier = 10;
    }

    @SerializedName("// The default style of healthbar. The following are available")
    public final String _comment_defaultStyle1 = Arrays.toString(HealthbarStyle.values());
    @BrigadierDescription(defaultOption = "SKYBLOCK")
    public HealthbarStyle defaultStyle = HealthbarStyle.SKYBLOCK;

    @SerializedName("// Whether healthbar is enabled by default.")
    public final String _comment_enabled = "";
    @BrigadierDescription(defaultOption = "true")
    @SerializedName("enabled_by_default")
    public boolean enabledByDefault = true;

    @SerializedName("// Whether healthbar should always be visible (not just on entity hover) by default.")
    public final String _comment_alwaysVisibleDefault = "";
    @SerializedName("always_visible_by_default")
    @BrigadierDescription(defaultOption = "false")
    public boolean alwaysVisibleDefault = false;

    @Override
    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MODID).error("Problem occurred when saving config: {}", e.getMessage());
        }
    }

    /**
     * Loads language file.
     *
     * @param file file to load the language file from.
     * @return HealthConfig object
     */
    public static HealthConfig loadConfigFile(File file) {
        return IBrigadierConfigurator.loadConfigFile(file, HealthConfig.class, HealthConfig::new);
    }
}
