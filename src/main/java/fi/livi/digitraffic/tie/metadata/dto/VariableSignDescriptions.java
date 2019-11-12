package fi.livi.digitraffic.tie.metadata.dto;

import java.util.List;

public class VariableSignDescriptions {
    public final List<CodeDescriptionJson> signTypes;

    public VariableSignDescriptions(final List<CodeDescriptionJson> signTypes) {
        this.signTypes = signTypes;
    }
}
