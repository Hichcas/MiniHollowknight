package Model;

import Model.Enums.CharmState;

import java.util.function.Consumer;

public class CharmDefinition {
    public final CharmState state;
    public final String name;
    public final String description;
    public final int notches;
    public final Consumer<CharmStats> modifier;

    public CharmDefinition(CharmState state, String name, String description, int notches, Consumer<CharmStats> modifier) {
        this.state = state;
        this.name = name;
        this.description = description;
        this.notches = notches;
        this.modifier = modifier;
    }
}
