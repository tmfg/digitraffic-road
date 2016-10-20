package fi.livi.digitraffic.tie.metadata.service.location;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import fi.livi.digitraffic.tie.metadata.dao.location.LocationSubtypeRepository;
import fi.livi.digitraffic.tie.metadata.model.location.Location;
import fi.livi.digitraffic.tie.metadata.model.location.LocationSubtype;

@Component
public class LocationXSSFReader {
    private static final Logger log = LoggerFactory.getLogger(LocationXSSFReader.class);

    private final LocationSubtypeRepository locationSubtypeRepository;

    public LocationXSSFReader(final LocationSubtypeRepository locationSubtypeRepository) {
        this.locationSubtypeRepository = locationSubtypeRepository;
    }

    public List<Location> readLocations(final List<Location> oldLocations, final Path path) throws IOException, OpenXML4JException, SAXException {
        final Map<Integer, Location> locationMap = oldLocations.stream().collect(Collectors.toMap(Location::getLocationCode, Function.identity()));
        final Map<String, LocationSubtype> subtypeMap = locationSubtypeRepository.findAll().stream().collect(Collectors.toMap(LocationSubtype::getSubtypeCode, Function.identity()));

        try (
            final OPCPackage pkg = OPCPackage.open(path.toFile())
        ) {
            final XSSFReader reader = new XSSFReader(pkg);
            final XMLReader parser = getSheetParser(reader);
            final InputStream sis = reader.getSheetsData().next();

            parser.parse(new InputSource(sis));

            return parseLocations(((SheetHandler)parser.getContentHandler()).getLines(), locationMap, subtypeMap);
        }
    }

    private List<Location> parseLocations(final List<String> lines,
                                          final Map<Integer, Location> locationMap,
                                          final Map<String, LocationSubtype> subtypeMap) {
        final LocationReader reader = new LocationReader(locationMap, subtypeMap);

        return lines.stream().skip(1).map(reader::convert).collect(Collectors.toList());
    }

    private XMLReader getSheetParser(final XSSFReader reader) throws IOException, InvalidFormatException, SAXException {
        final SharedStringsTable sst = reader.getSharedStringsTable();
        final XMLReader parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");

        parser.setContentHandler(new SheetHandler(sst, 22));

        return parser;
    }

    private class SheetHandler extends DefaultHandler {
        private final SharedStringsTable sst;
        private final int minColumnCount;

        private StringBuffer lastContents;
        private boolean nextIsString;
        private List<String> currentRow = new ArrayList<>();
        private List<String> rows = new ArrayList<>();

        private int lastColumnNumber = -1;
        private int thisColumnNumber = -1;

        private SheetHandler(final SharedStringsTable sst, int minColumnCount) {
            this.sst = sst;
            this.minColumnCount = minColumnCount;
        }

        @Override
        public void startElement(final String uri, final String localName, final String name, final Attributes attributes) throws SAXException {
            // c => cell
            if(name.equals("c")) {
                thisColumnNumber = nameToColumn(attributes.getValue("r"));
                // Figure out if the value is an index in the SST
                final String cellType = attributes.getValue("t");
                if(cellType != null && cellType.equals("s")) {
                    nextIsString = true;
                } else {
                    nextIsString = false;
                }
            } else if(name.equals("row")) {
                currentRow = new ArrayList<>();
                thisColumnNumber = -1;
                lastColumnNumber = -1;
            }
            // Clear contents cache
            lastContents = new StringBuffer();
        }

        private int nameToColumn(final String name) {
            return name.charAt(0) - 'A';
        }

        @Override
        public void endElement(final String uri, final String localName, final String name)
                throws SAXException {
            // Process the last contents as required.
            // Do now, as characters() may be called more than once
            if(nextIsString) {
                int idx = Integer.parseInt(lastContents.toString());
                lastContents = new StringBuffer(new XSSFRichTextString(sst.getEntryAt(idx)).toString());
                nextIsString = false;
            }

            // v => contents of a cell
            // Output after we've seen the string contents
            if(name.equals("v")) {
                // if there are empty cells, add them
                IntStream.rangeClosed(2, thisColumnNumber - lastColumnNumber).forEach(x -> currentRow.add(StringUtils.EMPTY));

                currentRow.add(lastContents.toString());

                lastColumnNumber = thisColumnNumber;
            } else if (name.equals("row")) {
                // if there are empty cells, add them
                IntStream.rangeClosed(1, minColumnCount - thisColumnNumber).forEach(x -> currentRow.add(StringUtils.EMPTY));

                rows.add(currentRow.stream().collect(Collectors.joining(AbstractReader.DELIMETER)));
            }
        }

        @Override
        public void characters(final char[] ch, final int start, final int length)
                throws SAXException {
            lastContents.append(new String(ch, start, length));
        }

        public List<String> getLines() {
            return rows;
        }
    }
}
