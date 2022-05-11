package fi.livi.digitraffic.tie.model.v2.maintenance;

import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.AURAUSVIITOITUS_JA_KINOSTIMET;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.AURAUS_JA_SOHJONPOISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.HARJAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.JYRAYS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.KELINTARKASTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.KONEELLINEN_NIITTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.KONEELLINEN_VESAKONRAIVAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.KUUMENNUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.LIIKENNEMERKKIEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.LIIK_OPAST_JA_OHJAUSL_HOITO_SEKA_REUNAPAALUJEN_KUN_PITO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.LINJAHIEKOITUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.LUMENSIIRTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.LUMIVALLIEN_MADALTAMINEN;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.L_JA_P_ALUEIDEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.MUU;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.OJITUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_JUOTOSTYOT;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAALLYSTETYN_TIEN_SORAPIENTAREEN_TAYTTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PAANNEJAAN_POISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PALTEEN_POISTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PINNAN_TASAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.PISTEHIEKOITUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SEKOITUS_TAI_STABILOINTI;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SILTOJEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SORAPIENTAREEN_TAYTTO;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SORASTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SORATEIDEN_MUOKKAUSHOYLAYS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SORATEIDEN_POLYNSIDONTA;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SORATEIDEN_TASAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SULAMISVEDEN_HAITTOJEN_TORJUNTA;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.SUOLAUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.TIEMERKINTA;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.TIESTOTARKASTUS;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.TILAAJAN_LAADUNVALVONTA;
import static fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat.TURVALAITE;

import java.util.Arrays;

import fi.livi.digitraffic.tie.external.harja.SuoritettavatTehtavat;
import io.swagger.v3.oas.annotations.media.Schema;

public enum MaintenanceTrackingTask {

    BRUSHING(HARJAUS, "Harjaus", "Borstning", "Brushing"),
    BRUSH_CLEARING(KONEELLINEN_VESAKONRAIVAUS, "Koneellinen vesakonraivaus", "Slyklippning", "Brush clearing"),
    CLEANSING_OF_BRIDGES(SILTOJEN_PUHDISTUS, "Siltojen puhdistus", "Rengöring av broar", "Cleansing of bridges"),
    CLEANSING_OF_REST_AREAS(L_JA_P_ALUEIDEN_PUHDISTUS, "L- ja p-alueiden puhdistus", "Rast- och parkeringsplatsernas rengöring", "Cleansing of rest areas"),
    CLEANSING_OF_TRAFFIC_SIGNS(LIIKENNEMERKKIEN_PUHDISTUS, "Liikennemerkkien puhdistus", "Rengöring av trafikmärken", "Cleansing of traffic signs"),
    CLIENTS_QUALITY_CONTROL(TILAAJAN_LAADUNVALVONTA, "Tilaajan laadunvalvonta", "Beställarens kvalitetsövervakning", "Clients quality control"),
    COMPACTION_BY_ROLLING(JYRAYS, "Jyräys", "Vibrering", "Compaction by rolling"),
    CRACK_FILLING(PAALLYSTEIDEN_JUOTOSTYOT, "Päällysteiden juotostyöt", "Fyllning av sprickor i beläggningen", "Crack filling"),
    DITCHING(OJITUS, "Ojitus", "Dikning", "Ditching"),
    DUST_BINDING_OF_GRAVEL_ROAD_SURFACE(SORATEIDEN_POLYNSIDONTA, "Sorateiden pölynsidonta", "Dammbindning av grusväg", "Dust binding of gravel road surface"),
    FILLING_OF_GRAVEL_ROAD_SHOULDERS(SORAPIENTAREEN_TAYTTO, "Sorapientareen täyttö", "Fyllning av gruskanter", "Filling of gravel road shoulders"),
    FILLING_OF_ROAD_SHOULDERS(PAALLYSTETYN_TIEN_SORAPIENTAREEN_TAYTTO, "Päällystetyn tien sorapientareen täyttö", "Belagda vägars kantfyllning", "Filling of road shoulders"),
    HEATING(KUUMENNUS, "Kuumennus", "Upphettning", "Heating"),
    LEVELLING_GRAVEL_ROAD_SURFACE(SORATEIDEN_TASAUS, "Sorateiden tasaus", "Utjämning av grusväg", "Levelling gravel road surface"),
    LEVELLING_OF_ROAD_SHOULDERS(PALTEEN_POISTO, "Palteen poisto", "Kantskärning", "Levelling of road shoulders"),
    LEVELLING_OF_ROAD_SURFACE(PINNAN_TASAUS, "Pinnan tasaus", "Utjämning av vägytan", "Levelling of road surface"),
    LINE_SANDING(LINJAHIEKOITUS, "Linjahiekoitus", "Linjesandning", "Line sanding"),
    LOWERING_OF_SNOWBANKS(LUMIVALLIEN_MADALTAMINEN, "Lumivallien madaltaminen", "Nedsänkning av plogvall", "Lowering of snowbanks"),
    MAINTENANCE_OF_GUIDE_SIGNS_AND_REFLECTOR_POSTS(LIIK_OPAST_JA_OHJAUSL_HOITO_SEKA_REUNAPAALUJEN_KUN_PITO, "Liikenteen opasteiden ja ohjauslaitteiden hoito sekä reunapaalujen kunnossapito", "Trafikanordningars och kantpålars underhåll", "Maintenance of guide signs and reflector posts"),
    MECHANICAL_CUT(KONEELLINEN_NIITTO, "Koneellinen niitto", "Mekanisk klippning", "Mechanical cut"),
    MIXING_OR_STABILIZATION(SEKOITUS_TAI_STABILOINTI, "Sekoitus tai stabilointi", "Blandning och stabilisering", "Mixing or stabilization"),
    OTHER(MUU, "Muu", "Annat dylikt", "Other"),
    PATCHING(PAALLYSTEIDEN_PAIKKAUS, "Päällysteiden paikkaus", "Lappning av beläggning", "Patching"),
    PAVING(ASFALTOINTI, "Asfaltointi", "Asfaltering", "Paving"),
    PLOUGHING_AND_SLUSH_REMOVAL(AURAUS_JA_SOHJONPOISTO, "Auraus ja sohjonpoisto", "Plog- och sörjröjning", "Ploughing and slush removal"),
    PREVENTING_MELTING_WATER_PROBLEMS(SULAMISVEDEN_HAITTOJEN_TORJUNTA, "Sulamisveden haittojen torjunta", "Smältvattensbekämpning", "Preventing melting water problems"),
    REMOVAL_OF_BULGE_ICE(PAANNEJAAN_POISTO, "Paannejään poisto", "Borttagning av svall-is", "Removal of bulge ice"),
    RESHAPING_GRAVEL_ROAD_SURFACE(SORATEIDEN_MUOKKAUSHOYLAYS, "Sorateiden muokkaushöyläys", "Grusvägsytans omblandning ", "Reshaping gravel road surface"),
    ROAD_INSPECTIONS(TIESTOTARKASTUS, "Tiestötarkastus", "Väggranskning", "Road inspections "),
    ROAD_MARKINGS(TIEMERKINTA, "Tiemerkintä", "Vägmarkering", "Road markings"),
    ROAD_STATE_CHECKING(KELINTARKASTUS, "Kelintarkastus", "Väglagsgranskning", "Road state checking"),
    SAFETY_EQUIPMENT(TURVALAITE, "Turvalaite", "Säkerhetsanordning", "Safety equipment"),
    SALTING(SUOLAUS, "Suolaus", "Saltning", "Salting"),
    SNOW_PLOUGHING_STICKS_AND_SNOW_FENCES(AURAUSVIITOITUS_JA_KINOSTIMET, "Aurausviitoitus ja kinostimet", "Plogkäppsmarkering och snödrivor", "Snow-ploughing sticks and snow fences"),
    SPOT_SANDING(PISTEHIEKOITUS, "Pistehiekoitus", "Punktsandning", "Spot sanding"),
    SPREADING_OF_CRUSH(SORASTUS, "Sorastus", "Grusning", "Spreading of crush"),
    TRANSFER_OF_SNOW(LUMENSIIRTO, "Lumensiirto", "Bortforsling av snömassor", "Transfer of snow"),
    UNKNOWN(null, "Tuntematon", "Obekant", "Unknown");

    private final String harjaEnumName;
    private final String nameFi;
    private final String nameSv;
    private final String nameEn;

    MaintenanceTrackingTask(final SuoritettavatTehtavat harjaEnum, final String nameFi, final String nameSv, final String nameEn) {
        this.harjaEnumName = harjaEnum != null ? harjaEnum.name() : null;
        this.nameFi = nameFi;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
    }

    public String getNameFi() {
        return nameFi;
    }

    public String getNameSv() {
        return nameSv;
    }

    public String getNameEn() {
        return nameEn;
    }

    @Schema(description = "Enum id")
    public String getId() {
        return this.name();
    }

    public String getHarjaEnumName() {
        return harjaEnumName;
    }

    public static MaintenanceTrackingTask getByharjaEnumName(final String harjaEnumName) {
        return Arrays.stream(values())
            .filter(task -> harjaEnumName.equals(task.getHarjaEnumName()))
            .findFirst().orElse(UNKNOWN);
    }
}
