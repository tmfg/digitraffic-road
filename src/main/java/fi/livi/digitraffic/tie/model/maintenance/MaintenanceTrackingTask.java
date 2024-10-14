package fi.livi.digitraffic.tie.model.maintenance;

import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.ASFALTOINTI;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.AURAUSVIITOITUS_JA_KINOSTIMET;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.AURAUS_JA_SOHJONPOISTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.HARJAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.JYRAYS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.KELINTARKASTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.KONEELLINEN_NIITTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.KONEELLINEN_VESAKONRAIVAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.KUUMENNUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.LIIKENNEMERKKIEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.LIIK_OPAST_JA_OHJAUSL_HOITO_SEKA_REUNAPAALUJEN_KUN_PITO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.LINJAHIEKOITUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.LUMENSIIRTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.LUMIVALLIEN_MADALTAMINEN;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.L_JA_P_ALUEIDEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.MUU;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.OJITUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PAALLYSTEIDEN_JUOTOSTYOT;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PAALLYSTEIDEN_PAIKKAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PAALLYSTETYN_TIEN_SORAPIENTAREEN_TAYTTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PAANNEJAAN_POISTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PALTEEN_POISTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PINNAN_TASAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.PISTEHIEKOITUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SEKOITUS_TAI_STABILOINTI;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SILTOJEN_PUHDISTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SORAPIENTAREEN_TAYTTO;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SORASTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SORATEIDEN_MUOKKAUSHOYLAYS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SORATEIDEN_POLYNSIDONTA;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SORATEIDEN_TASAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SULAMISVEDEN_HAITTOJEN_TORJUNTA;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.SUOLAUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.TIEMERKINTA;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.TIESTOTARKASTUS;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.TILAAJAN_LAADUNVALVONTA;
import static fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema.TURVALAITE;

import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;

import fi.livi.digitraffic.common.dto.data.v1.DataUpdatedSupportV1;
import fi.livi.digitraffic.common.util.StringUtil;
import fi.livi.digitraffic.tie.external.harja.entities.SuoritettavatTehtavatSchema;
import io.swagger.v3.oas.annotations.media.Schema;

public enum MaintenanceTrackingTask implements DataUpdatedSupportV1 {

    BRUSHING(HARJAUS, "Harjaus", "Borstning", "Brushing", Constants.MODIFIED_2020_03_30),
    BRUSH_CLEARING(KONEELLINEN_VESAKONRAIVAUS, "Koneellinen vesakonraivaus", "Slyklippning", "Brush clearing", Constants.MODIFIED_2020_04_09),
    CLEANSING_OF_BRIDGES(SILTOJEN_PUHDISTUS, "Siltojen puhdistus", "Rengöring av broar", "Cleansing of bridges", Constants.MODIFIED_2020_04_23),
    CLEANSING_OF_REST_AREAS(L_JA_P_ALUEIDEN_PUHDISTUS, "L- ja p-alueiden puhdistus", "Rast- och parkeringsplatsernas rengöring", "Cleansing of rest areas", Constants.MODIFIED_2020_03_30),
    CLEANSING_OF_TRAFFIC_SIGNS(LIIKENNEMERKKIEN_PUHDISTUS, "Liikennemerkkien puhdistus", "Rengöring av trafikmärken", "Cleansing of traffic signs", Constants.MODIFIED_2020_03_30),
    CLIENTS_QUALITY_CONTROL(TILAAJAN_LAADUNVALVONTA, "Tilaajan laadunvalvonta", "Beställarens kvalitetsövervakning", "Clients quality control", Constants.MODIFIED_2020_04_23),
    COMPACTION_BY_ROLLING(JYRAYS, "Jyräys", "Vibrering", "Compaction by rolling", Constants.MODIFIED_2020_04_23),
    CRACK_FILLING(PAALLYSTEIDEN_JUOTOSTYOT, "Päällysteiden juotostyöt", "Fyllning av sprickor i beläggningen", "Crack filling", Constants.MODIFIED_2020_04_23),
    DITCHING(OJITUS, "Ojitus", "Dikning", "Ditching", Constants.MODIFIED_2020_04_23),
    DUST_BINDING_OF_GRAVEL_ROAD_SURFACE(SORATEIDEN_POLYNSIDONTA, "Sorateiden pölynsidonta", "Dammbindning av grusväg", "Dust binding of gravel road surface", Constants.MODIFIED_2020_04_23),
    FILLING_OF_GRAVEL_ROAD_SHOULDERS(SORAPIENTAREEN_TAYTTO, "Sorapientareen täyttö", "Fyllning av grusväg gruskanter", "Filling of gravel road shoulders", Constants.MODIFIED_2020_04_23),
    FILLING_OF_ROAD_SHOULDERS(PAALLYSTETYN_TIEN_SORAPIENTAREEN_TAYTTO, "Päällystetyn tien sorapientareen täyttö", "Belagda vägars kantfyllning", "Filling of paved road shoulders", Constants.MODIFIED_2020_04_23),
    HEATING(KUUMENNUS, "Kuumennus", "Upphettning", "Heating", Constants.MODIFIED_2020_04_23),
    LEVELLING_GRAVEL_ROAD_SURFACE(SORATEIDEN_TASAUS, "Sorateiden tasaus", "Utjämning av grusväg", "Levelling gravel road surface", Constants.MODIFIED_2020_04_23),
    LEVELLING_OF_ROAD_SHOULDERS(PALTEEN_POISTO, "Palteen poisto", "Kantskärning", "Levelling of road shoulders", Constants.MODIFIED_2020_04_23),
    LEVELLING_OF_ROAD_SURFACE(PINNAN_TASAUS, "Pinnan tasaus", "Utjämning av vägytan", "Levelling of road surface", Constants.MODIFIED_2020_04_23),
    LINE_SANDING(LINJAHIEKOITUS, "Linjahiekoitus", "Linjesandning", "Line sanding", Constants.MODIFIED_2020_03_30),
    LOWERING_OF_SNOWBANKS(LUMIVALLIEN_MADALTAMINEN, "Lumivallien madaltaminen", "Nedsänkning av plogvall", "Lowering of snowbanks", Constants.MODIFIED_2020_04_23),
    MAINTENANCE_OF_GUIDE_SIGNS_AND_REFLECTOR_POSTS(LIIK_OPAST_JA_OHJAUSL_HOITO_SEKA_REUNAPAALUJEN_KUN_PITO, "Liikenteen opasteiden ja ohjauslaitteiden hoito sekä reunapaalujen kunnossapito", "Trafikanordningars och kantpålars underhåll", "Maintenance of guide signs and reflector posts", Constants.MODIFIED_2020_04_23),
    MECHANICAL_CUT(KONEELLINEN_NIITTO, "Koneellinen niitto", "Mekanisk klippning", "Mechanical cut", Constants.MODIFIED_2020_04_23),
    MIXING_OR_STABILIZATION(SEKOITUS_TAI_STABILOINTI, "Sekoitus tai stabilointi", "Blandning och stabilisering", "Mixing or stabilization", Constants.MODIFIED_2020_04_23),
    OTHER(MUU, "Muu", "Annat dylikt", "Other", Constants.MODIFIED_2020_03_30),
    PATCHING(PAALLYSTEIDEN_PAIKKAUS, "Päällysteiden paikkaus", "Lappning av beläggning", "Patching", Constants.MODIFIED_2020_04_02),
    PAVING(ASFALTOINTI, "Asfaltointi", "Asfaltering", "Paving", Constants.MODIFIED_2020_04_23),
    PLOUGHING_AND_SLUSH_REMOVAL(AURAUS_JA_SOHJONPOISTO, "Auraus ja sohjonpoisto", "Plog- och sörjröjning", "Ploughing and slush removal", Constants.MODIFIED_2020_04_23),
    PREVENTING_MELTING_WATER_PROBLEMS(SULAMISVEDEN_HAITTOJEN_TORJUNTA, "Sulamisveden haittojen torjunta", "Smältvattensbekämpning", "Preventing melting water problems", Constants.MODIFIED_2020_04_23),
    REMOVAL_OF_BULGE_ICE(PAANNEJAAN_POISTO, "Paannejään poisto", "Borttagning av svall-is", "Removal of bulge ice", Constants.MODIFIED_2020_04_02),
    RESHAPING_GRAVEL_ROAD_SURFACE(SORATEIDEN_MUOKKAUSHOYLAYS, "Sorateiden muokkaushöyläys", "Grusvägsytans omblandning ", "Reshaping gravel road surface", Constants.MODIFIED_2020_04_23),
    ROAD_INSPECTIONS(TIESTOTARKASTUS, "Tiestötarkastus", "Väggranskning", "Road inspections ", Constants.MODIFIED_2020_04_23),
    ROAD_MARKINGS(TIEMERKINTA, "Tiemerkintä", "Vägmarkering", "Road markings", Constants.MODIFIED_2020_04_23),
    ROAD_STATE_CHECKING(KELINTARKASTUS, "Kelintarkastus", "Väglagsgranskning", "Road state checking", Constants.MODIFIED_2020_04_23),
    SAFETY_EQUIPMENT(TURVALAITE, "Turvalaite", "Säkerhetsanordning", "Safety equipment", Constants.MODIFIED_2020_03_30),
    SALTING(SUOLAUS, "Suolaus", "Saltning", "Salting", Constants.MODIFIED_2020_04_23),
    SNOW_PLOUGHING_STICKS_AND_SNOW_FENCES(AURAUSVIITOITUS_JA_KINOSTIMET, "Aurausviitoitus ja kinostimet", "Plogkäppsmarkering och snödrivor", "Snow-ploughing sticks and snow fences", Constants.MODIFIED_2020_04_23),
    SPOT_SANDING(PISTEHIEKOITUS, "Pistehiekoitus", "Punktsandning", "Spot sanding", Constants.MODIFIED_2020_04_23),
    SPREADING_OF_CRUSH(SORASTUS, "Sorastus", "Grusning", "Spreading of crush", Constants.MODIFIED_2020_04_23),
    TRANSFER_OF_SNOW(LUMENSIIRTO, "Lumensiirto", "Bortforsling av snömassor", "Transfer of snow", Constants.MODIFIED_2020_04_23),
    SERVICE_ROUND(SuoritettavatTehtavatSchema.HUOLTOKIERROS, "Huoltokierros", "Servicerunda", "Service round", Constants.MODIFIED_2024_10_21),
    ENSURING_TRAFFIC_IN_RASPUTITSA(SuoritettavatTehtavatSchema.LIIKENTEEN_VARMISTAMINEN_KELIRIKKOKOHTEESSA, "Liikenteen varmistaminen kelirikkokohteessa", "Säkra trafik i menföre", "Ensuring traffic in frost heave damage location", Constants.MODIFIED_2024_10_21),
    OTHER_OPERATIONS_OF_LIGHTING_CONTRACTS(SuoritettavatTehtavatSchema.MUUT_VALAISTUSURAKOIDEN_TOIMENPITEET, "Muut valaistusurakoiden toimenpiteet", "Andra mått på belysningskontrakt", "Other operations of lighting contracts", Constants.MODIFIED_2024_10_21),
    LEVELLING_OF_ROAD_SHOULDERS_UNDER_RAILING(SuoritettavatTehtavatSchema.PALTEEN_POISTO_KAITEEN_ALTA, "Palteen poisto kaiteen alta", "Kantskärning från under räcket", "Levelling of road shoulders under the railing", Constants.MODIFIED_2024_10_21),
    DUST_BINDING_OF_PAVED_ROAD_SURFACE(SuoritettavatTehtavatSchema.PAALLYSTETYN_TIEN_POLYNSIDONTA, "Paallystetyn tien polynsidonta", "Dammbindning av belagda väg", "Dust binding of paved road surface", Constants.MODIFIED_2024_10_21),
    RENEWAL_OF_EDGE_COLUMNS(SuoritettavatTehtavatSchema.REUNAPAALUJEN_UUSIMINEN, "Reunapaalujen uusiminen", "Förnyelse av reflektorstolpe", "Renewal of reflector posts", Constants.MODIFIED_2024_10_21),
    GARBAGE_OLLECTION(SuoritettavatTehtavatSchema.ROSKIEN_KERUU, "Roskien keruu", "Sophämtning", "Garbage collection", Constants.MODIFIED_2024_10_21),
    GROUP_REPLACEMENT_OF_LAMPS(SuoritettavatTehtavatSchema.RYHMAVAIHTO, "Valaisimien ryhmavaihto", "Gruppbyte av lampor", "Group replacement of lamps", Constants.MODIFIED_2024_10_21),
    PLOUGHING_OF_SLUSH_DITCH(SuoritettavatTehtavatSchema.SOHJO_OJIEN_TEKO, "Sohjo-ojien teko", "Plöjning av slask dike", "Ploughing of slush ditch", Constants.MODIFIED_2024_10_21),
    UNKNOWN(null, "Tuntematon", "Obekant", "Unknown", Constants.MODIFIED_2020_03_30),
    ;

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTrackingTask.class);

    private final String harjaEnumName;
    private final String nameFi;
    private final String nameSv;
    private final String nameEn;
    private final Instant dataUpdated;

    MaintenanceTrackingTask(final SuoritettavatTehtavatSchema harjaEnum, final String nameFi, final String nameSv, final String nameEn, final String dataUpdated) {
        this.harjaEnumName = harjaEnum != null ? harjaEnum.name() : null;
        this.nameFi = nameFi;
        this.nameSv = nameSv;
        this.nameEn = nameEn;
        this.dataUpdated = Instant.parse(dataUpdated);
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

    @Override
    public Instant getDataUpdatedTime() {
        return dataUpdated;
    }

    public String getHarjaEnumName() {
        return harjaEnumName;
    }

    public static MaintenanceTrackingTask getByharjaEnumName(final String harjaEnumName) {
        return Arrays.stream(values())
            .filter(task -> harjaEnumName.equals(task.getHarjaEnumName()))
            .findFirst().orElse(UNKNOWN);
    }

    private static class Constants {
        public static final String MODIFIED_2024_10_21 = "2024-10-21T00:00:00Z";
        public static final String MODIFIED_2020_03_30 = "2020-03-30T00:00:00Z";
        public static final String MODIFIED_2020_04_02 = "2020-04-02T00:00:00Z";
        public static final String MODIFIED_2020_04_09 = "2020-04-09T00:00:00Z";
        public static final String MODIFIED_2020_04_23 = "2020-04-23T00:00:00Z";
    }

    /**
     * Generates SQL for db update ie. <code>/dbroad/sql/update/V3/V3.17.0__DPO-2617_maintenance_tracking_tasks_values.sql</code>
     */
    public static void main(final String[] args) {
        final StringBuilder sb = new StringBuilder();
        sb.append("""
                UPDATE SQL:
                INSERT INTO maintenance_tracking_task_value (name, name_harja, name_fi, name_sv, name_en)
                VALUES
                """);
        final String values = Arrays.stream(MaintenanceTrackingTask.values())
                .map(t ->
                        StringUtil.format("  ('{}', '{}', '{}', '{}', '{}')",
                                t.name(), MoreObjects.firstNonNull(t.getHarjaEnumName(), "TUNTEMATON"), t.getNameFi(), t.getNameSv(), t.getNameEn()))
                .collect(Collectors.joining(",\n"));
        sb.append(values);
        sb.append("\n");
        sb.append("""
                on conflict (name) do update\s
                  SET name_harja = excluded.name_harja
                    , name_fi = excluded.name_fi
                    , name_sv = excluded.name_sv
                    , name_en = excluded.name_en;""");
        log.info(sb.toString());
    }
}
