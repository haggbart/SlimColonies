package no.monopixel.slimcolonies.api.research;

import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interface defining how a research globally is defined.
 */
public interface IGlobalResearch
{
    /**
     * Check if this research can be executed at this moment.
     *
     * @param uni_level the level of the university.
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    boolean canResearch(int uni_level, @NotNull final ILocalResearchTree localTree);

    /**
     * Check if this research can be displayed in the GUI.
     *
     * @param uni_level the level of the university.
     * @return true if so.
     */
    boolean canDisplay(int uni_level);

    // hasEnoughResources method removed - research no longer has item costs

    // getCostList method removed - research no longer has item costs

    /**
     * Start the research.
     * @param localResearchTree the local research tree to store in the colony.
     */
    void startResearch(@NotNull final ILocalResearchTree localResearchTree);

    /**
     * Human-readable description of research, in human-readable text or as a translation key.
     * @return the description.
     */
    TranslatableContents getName();

    /**
     * Subtitle description of research, in human-readable text or as a translation key.
     * @return the optional subtitle name.
     */
    TranslatableContents getSubtitle();

    /**
     * Getter of the id of the research.
     * @return the research id, as a ResourceLocation
     */
    ResourceLocation getId();

    /**
     * Get the id of the parent IResearch.
     *
     * @return the parent id, as a ResourceLocation
     */
    @Nullable
    ResourceLocation getParent();

    /**
     * Get the id of the branch.
     *
     * @return the branch id, as a ResourceLocation
     */
    ResourceLocation getBranch();

    /**
     * Get the depth in the research tree.
     *
     * @return the depth.
     */
    int getDepth();

    /**
     * Get the sort order for relative display position.
     *
     * @return the depth.
     */
    int getSortOrder();

    /**
     * Check if this research is an instant research.  If so, it will attempt to start when its requirements are complete, and prompt the player.
     *
     * @return true if so.
     */
    boolean isInstant();

    /**
     * Check if this research should automatically start when requirements are complete.
     * This can temporarily exceed normal limits of the max number of concurrent researches.
     * @return true if so.
     */
    boolean isAutostart();

    /**
     * Check if this research is a hidden research.  If so, it (and its children) should only be visible if all requirements are met.
     *
     * @return true if so.
     */
    boolean isHidden();

    /**
     * Check if this research is an immutable research.  If so, it (and ancestor research unlocking it) can not be reset once completed.
     *
     * @return true if so.
     */
    boolean isImmutable();

    /**
     * Check if this research is an only child research. This means, after researching one child no other children can be researched.
     *
     * @return true if so.
     */
    boolean hasOnlyChild();

    /**
     * Check if this research has other children and if one of these children has been research already.
     *
     * @param localTree the local tree of the colony.
     * @return true if so.
     */
    boolean hasResearchedChild(@NotNull final ILocalResearchTree localTree);

    /**
     * Add a child to a research.
     *
     * @param child the child to add.
     */
    void addChild(IGlobalResearch child);

    /**
     * Add a child to a research, without setting parentage.
     * @param child the child to add
     */
    void addChild(final ResourceLocation child);

    // addCost method removed - research no longer has item costs

    /**
     * Add an individual effect.
     * @param effect the individual effect to add to the research, as a IResearchEffect.
     */
    void addEffect(final IResearchEffect effect);

    /**
     * Add an individual requirement
     * @param requirement the individual requirement to add to the research, as an IResearchRequirement.
     */
    void addRequirement(final IResearchRequirement requirement);

    /**
     * Get the list of children of the research.
     *
     * @return a copy of the list of child identifiers.
     */
    List<ResourceLocation> getChildren();

    /**
     * Getter for the research requirement.
     *
     * @return the requirement.
     */
    List<IResearchRequirement> getResearchRequirements();

    /**
     * Get the effect of the research.
     *
     * @return the effect.
     */
    List<IResearchEffect> getEffects();
}
