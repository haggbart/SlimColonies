package no.monopixel.slimcolonies.core.generation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import no.monopixel.slimcolonies.api.util.constant.Constants;

public class DataGeneratorConstants
{

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static final String DATAPACK_DIR = "data/" + Constants.MOD_ID + "/";

    // DataPack Directories \\

    public static final String LOOT_TABLES_DIR = DATAPACK_DIR + "loot_tables/blocks";

    public static final String ASSETS_DIR = "assets/" + Constants.MOD_ID + "/";

    public static final String COLONY_STORIES_DIR           = "colony/stories";
    public static final String COLONY_QUESTS_DIR            = "colony/quests";
    public static final String COLONY_RECRUITMENT_ITEMS_DIR = "colony/recruitment_items";
}
