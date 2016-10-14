package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Component
public class LocationReader {
    private static final Logger log = LoggerFactory.getLogger(LocationReader.class);

    private final LocationSubtypeRepository locationSubtypeRepository;

    public LocationReader(final LocationSubtypeRepository locationSubtypeRepository) {
        this.locationSubtypeRepository = locationSubtypeRepository;
    }

    public List<Location> readLocations(final List<Location> oldLocations, final Path path) {
        final Map<Integer, Location> locationMap = oldLocations.stream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
        final Map<String, LocationSubtype> subtypeMap = locationSubtypeRepository.findAll().stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        try (final FileInputStream fis = new FileInputStream(path.toFile());
             final XSSFWorkbook book = new XSSFWorkbook(fis)) {
            final XSSFSheet s = book.getSheetAt(0);

            return StreamSupport.stream(s.spliterator(), false).skip(1).map(r -> convert(r, locationMap, subtypeMap)).collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private Location convert(final Row row, final Map<Integer, Location> locationMap, final Map<String, LocationSubtype> subtypeMap) {
        final Location location = new Location();

        location.setLocationCode((int)row.getCell(2).getNumericCellValue());
        location.setRoadName(parseString(row.getCell(7)));
        location.setFirstName(parseString(row.getCell(8)));
        location.setSecondName(parseString(row.getCell(9)));
        location.setNegOffset(parseInteger(row.getCell(12)));
        location.setPosOffset(parseInteger(row.getCell(13)));
        location.setUrban(parseBoolean(row.getCell(14)));
        location.setWsg84Lat(parseDecimal(row.getCell(16)));
        location.setWsg84Long(parseDecimal(row.getCell(17)));

        location.setLinearRef(parseReference(row.getCell(10), locationMap));
        location.setAreaRef(parseReference(row.getCell(11), locationMap));
        location.setLocationSubtype(parseSubtype(row.getCell(3), row.getCell(4), row.getCell(5), subtypeMap));

        locationMap.put(location.getLocationCode(), location);

        return location;
    }

    private LocationSubtype parseSubtype(final Cell classCell, final Cell typeCell, final Cell subtypeCell, final Map<String, LocationSubtype> subtypeMap) {
        final String classValue = classCell.getStringCellValue();
        final int typeValue = parseInteger(typeCell);
        final int subtypeValue = parseInteger(subtypeCell);
        final String subtypeCode = String.format("%s%d.%d", classValue, typeValue, subtypeValue);

        final LocationSubtype subtype = subtypeMap.get(subtypeCode);

        if(subtype == null) {
            log.error("Could not find subtype " + subtypeCode);
        }

        return subtype;
    }

    private Boolean parseBoolean(final Cell cell) {
        final Integer i = parseInteger(cell);

        return i == null ? null : (i == 0 ? false : true);
    }

    private BigDecimal parseDecimal(final Cell cell) {
        return cell == null ? null : new BigDecimal(cell.getNumericCellValue());
    }

    private Location parseReference(final Cell cell, final Map<Integer, Location> locationMap) {
        final Integer refValue = parseInteger(cell);

        // for some reason, there is no 0 present
        if(refValue == null || refValue == 0) return null;

        final Location refLocation = locationMap.get(refValue);

        if(refLocation == null) {
            log.error("Could not find reference " + refValue);
        }

        return refLocation;
    }

    private String parseString(final Cell cell) {
        if(cell == null) {
            return null;
        }

        if(cell.getCellType() == CellType.NUMERIC.getCode()) {
            return Integer.toString((int)cell.getNumericCellValue());
        }

        return cell.getStringCellValue();
    }

    private Integer parseInteger(final Cell cell) {
        return cell == null ? null : (int)cell.getNumericCellValue();
    }
}
