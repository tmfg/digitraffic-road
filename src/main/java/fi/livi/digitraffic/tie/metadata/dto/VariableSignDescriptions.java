package fi.livi.digitraffic.tie.metadata.dto;

import java.util.List;

public class VariableSignDescriptions {
    public final List<CodeDescription> signTypes;

    public VariableSignDescriptions(final List<CodeDescription> signTypes) {
        this.signTypes = signTypes;
    }
}
