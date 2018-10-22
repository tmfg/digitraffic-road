
package fi.livi.digitraffic.tie.data.model.maintenance;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum PerformedTask {

    // TODO to English
    ASPHALTING("asfaltointi"),
    SNOW_PLOWING_AND_SLUSH_REMOVAL("auraus ja sohjonpoisto"),
    SNOW_PLOW_STICKS_JA_FENCE("aurausviitoitus ja kinostimet"),
    SWEEPING("harjaus"),
    ROLLING("jyrays"),
    DRIVING_CONDITIONS_INSPECTION("kelintarkastus"),
    MACHINERY_MOWING("koneellinen niitto"),
    MACHINERY_VESAKONRAIVAUS("koneellinen vesakonraivaus"),
    KUUMENNUS("kuumennus"),
    L_JA_P_ALUEIDEN_PUHDISTUS("l- ja p-alueiden puhdistus"),
    LIIKENNEMERKKIEN_PUHDISTUS("liikennemerkkien puhdistus"),
    LIIK_OPAST_JA_OHJAUSL_HOITO_SEKA_REUNAPAALUJEN_KUN_PITO("liik. opast. ja ohjausl. hoito seka reunapaalujen kun.pito"),
    LINJAHIEKOITUS("linjahiekoitus"),
    LUMENSIIRTO("lumensiirto"),
    LUMIVALLIEN_MADALTAMINEN("lumivallien madaltaminen"),
    MUU("muu"),
    OJITUS("ojitus"),
    PAALLYSTEIDEN_JUOTOSTYOT("paallysteiden juotostyot"),
    PAALLYSTEIDEN_PAIKKAUS("paallysteiden paikkaus"),
    PAANNEJAAN_POISTO("paannejaan poisto"),
    PALTEEN_POISTO("palteen poisto"),
    PINNAN_TASAUS("pinnan tasaus"),
    PISTEHIEKOITUS("pistehiekoitus"),
    PAALLYSTETYN_TIEN_SORAPIENTAREEN_TAYTTO("paallystetyn tien sorapientareen taytto"),
    SEKOITUS_TAI_STABILOINTI("sekoitus tai stabilointi"),
    SILTOJEN_PUHDISTUS("siltojen puhdistus"),
    SORASTUS("sorastus"),
    SORAPIENTAREEN_TAYTTO("sorapientareen taytto"),
    SORATEIDEN_MUOKKAUSHOYLAYS("sorateiden muokkaushoylays"),
    SORATEIDEN_POLYNSIDONTA("sorateiden polynsidonta"),
    SORATEIDEN_TASAUS("sorateiden tasaus"),
    SULAMISVEDEN_HAITTOJEN_TORJUNTA("sulamisveden haittojen torjunta"),
    SUOLAUS("suolaus"),
    TIEMERKINTA("tiemerkinta"),
    TIESTOTARKASTUS("tiestotarkastus"),
    TILAAJAN_LAADUNVALVONTA("tilaajan laadunvalvonta"),
    TURVALAITE("turvalaite");

    private final String value;
    private final static Map<String, PerformedTask> CONSTANTS = new HashMap<String, PerformedTask>();

    static {
        for (PerformedTask c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private PerformedTask(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static PerformedTask fromValue(String value) {
        PerformedTask constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
