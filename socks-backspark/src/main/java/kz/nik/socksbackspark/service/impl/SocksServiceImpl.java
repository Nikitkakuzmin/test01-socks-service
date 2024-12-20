package kz.nik.socksbackspark.service.impl;

import com.opencsv.CSVReader;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.FileProcessingException;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.exceptions.InvalidDataFormatException;
import kz.nik.socksbackspark.mapper.SocksMapper;
import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import kz.nik.socksbackspark.service.SocksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocksServiceImpl implements SocksService {

    private final SocksRepository socksRepository;
    private final SocksMapper socksMapper;

    @Override
    public List<SocksDto> getFilteredSocks(
            String color,
            String operation,
            Integer cottonPercentage,
            Integer cottonPercentageFrom,
            Integer cottonPercentageTo,
            String sortBy,
            String sortDirection) {
        try {
            log.info("Request to filter socks with color={}, operation={}, cottonPercentage={}, " +
                            "cottonPercentageFrom={}, cottonPercentageTo={}, sortBy={}, sortDirection={}",
                    color, operation, cottonPercentage, cottonPercentageFrom, cottonPercentageTo,
                    sortBy, sortDirection);

            List<Socks> socksList;

            if (color != null && !color.isEmpty()) {
                if (operation == null || operation.isEmpty()) {
                    socksList = socksRepository.findByColor(color);
                } else {
                    socksList = filterByCottonPercentage(color, operation, cottonPercentage);
                }
            } else if (cottonPercentage != null) {
                socksList = filterByCottonPercentage(null, operation, cottonPercentage);
            } else if (cottonPercentageFrom != null && cottonPercentageTo != null) {
                socksList = socksRepository.findByCottonPercentageBetween(cottonPercentageFrom, cottonPercentageTo);
            } else {
                throw new InvalidDataFormatException("At least one parameter (color, cottonPercentage, " +
                        "cottonPercentageFrom or cottonPercentageTo) must be provided.");
            }

            if (sortBy != null) {
                Comparator<Socks> comparator = null;
                if ("color".equalsIgnoreCase(sortBy)) {
                    comparator = Comparator.comparing(Socks::getColor);
                } else if ("cottonPercentage".equalsIgnoreCase(sortBy)) {
                    comparator = Comparator.comparingInt(Socks::getCottonPercentage);
                }

                if (comparator != null) {
                    if ("desc".equalsIgnoreCase(sortDirection)) {
                        comparator = comparator.reversed();
                    }
                    socksList.sort(comparator);
                }
            }

            log.info("Returning {} socks after filtering and sorting", socksList.size());
            return socksMapper.toDtoList(socksList);

        } catch (InvalidDataFormatException e) {
            log.error("Invalid data format: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in getFilteredSocks method: {}", e.getMessage());
            throw new RuntimeException("Error retrieving filtered socks", e);
        }
    }


    public List<SocksDto> getAllSocks() {
        try {
            List<Socks> socksList = socksRepository.findAll();
            return socksMapper.toDtoList(socksList);
        } catch (Exception e) {
            log.error("Error retrieving all socks: {}", e.getMessage());
            throw new RuntimeException("Error retrieving all socks", e);
        }
    }


    private List<Socks> filterByCottonPercentage(String color, String operation, Integer cottonPercentage) {
        try {
            switch (operation) {
                case "greaterThan":
                    return color != null
                            ? socksRepository.findByColorAndCottonPercentageGreaterThan(color, cottonPercentage)
                            : socksRepository.findByCottonPercentageGreaterThan(cottonPercentage);
                case "lessThan":
                    return color != null
                            ? socksRepository.findByColorAndCottonPercentageLessThan(color, cottonPercentage)
                            : socksRepository.findByCottonPercentageLessThan(cottonPercentage);
                case "equalTo":
                    return color != null
                            ? socksRepository.findByColorAndCottonPercentage(color, cottonPercentage)
                            : socksRepository.findByCottonPercentage(cottonPercentage);
                default:
                    throw new InvalidDataFormatException("Invalid operation. Please use 'greaterThan'," +
                            " 'lessThan' or 'equalTo'.");
            }
        } catch (InvalidDataFormatException e) {
            log.error("Invalid data format: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in filterByCottonPercentage method: {}", e.getMessage());
            throw new RuntimeException("Error filtering socks by cotton percentage", e);
        }
    }

    @Override
    public SocksDto updateSock(Long id, SocksDto socksDto) {
        try {
            log.info("Request to update sock with id={} to {}", id, socksDto);

            Socks existingSock = socksRepository.findById(id)
                    .orElseThrow(() -> new InsufficientStockException("Sock not found with id: " + id));

            existingSock.setColor(socksDto.getColor());
            existingSock.setCottonPercentage(socksDto.getCottonPercentage());
            existingSock.setQuantity(socksDto.getQuantity());

            Socks updatedSock = socksRepository.save(existingSock);
            log.info("Updated sock: {}", updatedSock);
            return socksMapper.toDto(updatedSock);
        } catch (InsufficientStockException e) {
            log.error("Sock not found: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in updateSock method: {}", e.getMessage());
            throw new RuntimeException("Error updating sock", e);
        }
    }

    @Override
    public void addSocks(SocksDto socksDto) {
        try {
            log.info("Processing sock: {}", socksDto);

            List<Socks> existingSocksList = socksRepository.findByColorAndCottonPercentage(
                    socksDto.getColor(), socksDto.getCottonPercentage());

            if (!existingSocksList.isEmpty()) {
                Socks existingSock = existingSocksList.get(0);
                existingSock.setQuantity(existingSock.getQuantity() + socksDto.getQuantity());
                socksRepository.save(existingSock);
                log.info("Updated sock: {}", existingSock);
            } else {
                Socks newSock = socksMapper.toEntity(socksDto);
                socksRepository.save(newSock);
                log.info("Added new sock: {}", newSock);
            }
        } catch (Exception e) {
            log.error("Error in addSock method: {}", e.getMessage());
            throw new RuntimeException("Error adding sock", e);
        }
    }


    @Override
    public void decreaseSocksQuantity(String color, int cottonPercentage, int quantity) {
        try {
            log.info("Request to decrease quantity for color: {}, cottonPercentage: {}, quantity: {}", color,
                    cottonPercentage, quantity);

            List<Socks> existingSocksList = socksRepository.findByColorAndCottonPercentage(color, cottonPercentage);

            if (existingSocksList.isEmpty()) {
                log.error("No socks found with color: {} and cottonPercentage: {}", color, cottonPercentage);
                throw new InsufficientStockException("No socks found with the given color and cotton percentage");
            }

            Socks existingSock = existingSocksList.get(0);

            if (existingSock.getQuantity() < quantity) {
                log.error("Not enough socks in stock for color: {} and cottonPercentage: {}. Available: {}, " +
                                "Requested: {}",
                        color, cottonPercentage, existingSock.getQuantity(), quantity);
                throw new InsufficientStockException("Not enough socks in stock");
            }

            existingSock.setQuantity(existingSock.getQuantity() - quantity);
            socksRepository.save(existingSock);
            log.info("Decreased quantity of socks: {}", existingSock);
        } catch (InsufficientStockException e) {
            log.error("Insufficient stock: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error in decreaseSocksQuantity method: {}", e.getMessage());
            throw new RuntimeException("Error decreasing sock quantity", e);
        }
    }

    @Override
    public void processCsvFile(MultipartFile file) throws Exception {
        try {
            log.info("Processing CSV file: {}", file.getOriginalFilename());

            try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                List<String[]> rows = csvReader.readAll();
                List<Socks> socksList = new ArrayList<>();

                rows.remove(0);

                for (String[] row : rows) {
                    String color = row[0];
                    int cottonPercentage = Integer.parseInt(row[1]);
                    int quantity = Integer.parseInt(row[2]);

                    Socks sock = new Socks();
                    sock.setColor(color);
                    sock.setCottonPercentage(cottonPercentage);
                    sock.setQuantity(quantity);

                    socksList.add(sock);
                }

                socksRepository.saveAll(socksList);
                log.info("Processed and saved {} socks from CSV file", socksList.size());
            } catch (Exception e) {
                log.error("Error processing CSV file: {}", e.getMessage());
                throw new FileProcessingException("Error processing CSV file: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error in processCsvFile method: {}", e.getMessage());
            throw new RuntimeException("Error processing file", e);
        }
    }

    @Override
    public void processExcelFile(MultipartFile file) throws Exception {
        try {
            log.info("Processing Excel file: {}", file.getOriginalFilename());
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);
                Iterator<Row> rowIterator = sheet.iterator();

                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();

                    if (row.getRowNum() == 0) continue;

                    String color = row.getCell(0).getStringCellValue();
                    int cottonPercentage = (int) row.getCell(1).getNumericCellValue();
                    int quantity = (int) row.getCell(2).getNumericCellValue();

                    SocksDto socksDto = new SocksDto();
                    socksDto.setColor(color);
                    socksDto.setCottonPercentage(cottonPercentage);
                    socksDto.setQuantity(quantity);

                    addBatchToDatabase(socksDto);
                }
            }
        } catch (Exception e) {
            log.error("Error in processExcelFile method: {}", e.getMessage());
            throw new RuntimeException("Error processing Excel file", e);
        }
    }

    private void addBatchToDatabase(SocksDto socksDto) {
        try {
            List<Socks> existingSocksList = socksRepository.findByColorAndCottonPercentage(
                    socksDto.getColor(), socksDto.getCottonPercentage());

            if (!existingSocksList.isEmpty()) {
                Socks existingSock = existingSocksList.get(0);
                existingSock.setQuantity(existingSock.getQuantity() + socksDto.getQuantity());
                socksRepository.save(existingSock);
            } else {
                Socks newSock = socksMapper.toEntity(socksDto);
                socksRepository.save(newSock);
            }
        } catch (Exception e) {
            log.error("Error in addBatchToDatabase method: {}", e.getMessage());
            throw new RuntimeException("Error adding batch to database", e);
        }
    }
}





