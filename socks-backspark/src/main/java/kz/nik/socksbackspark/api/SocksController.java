package kz.nik.socksbackspark.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.FileProcessingException;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.exceptions.InvalidDataFormatException;
import kz.nik.socksbackspark.handler.ErrorDetails;
import kz.nik.socksbackspark.service.SocksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/socks")
public class SocksController {

    private final SocksService socksService;

    @Operation(summary = "Retrieve socks with filters.Извлечение с фильтром",
            description = "Fetch socks based on optional filters such as color, cotton percentage range, and sorting " +
                    "options. Выбор по дополнительным фильтрам, таким как цвет, процентное содержание хлопка и " +
                    "параметры сортировки.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved socks. Успех.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SocksDto.class),
                            examples = @ExampleObject(value = "[{\"color\": \"red\", \"cottonPercentage\": 50, " +
                                    "\"quantity\": 100}]"))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid filter parameters. Неверные параметры.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Invalid data format\"}"))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error. Ошибка сервера.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unexpected server error\"}")))
    })
    @GetMapping
    public ResponseEntity<?> getSocks(
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Integer cottonPercentage,
            @RequestParam(required = false) Integer cottonPercentageFrom,
            @RequestParam(required = false) Integer cottonPercentageTo,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection
    ) {
        log.info("Request to get socks with filters: color={}, operation={}, cottonPercentage={}, " +
                        "cottonPercentageFrom={}, cottonPercentageTo={}, sortBy={}, sortDirection={}",
                color, operation, cottonPercentage, cottonPercentageFrom, cottonPercentageTo, sortBy, sortDirection);

        try {
            if (color == null && operation == null && cottonPercentage == null &&
                    cottonPercentageFrom == null && cottonPercentageTo == null) {
                List<SocksDto> allSocks = socksService.getAllSocks();
                return ResponseEntity.ok(allSocks);
            }
            List<SocksDto> socks = socksService.getFilteredSocks(color, operation, cottonPercentage,
                    cottonPercentageFrom, cottonPercentageTo, sortBy, sortDirection);
            return ResponseEntity.ok(socks);
        } catch (InvalidDataFormatException e) {
            log.error("Invalid data format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ErrorDetails(HttpStatus.BAD_REQUEST,
                    "Invalid data format. Неверный формат.", e.getMessage()));
        } catch (Exception e) {
            log.error("Error retrieving socks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new
                    ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving socks. Ошибка при " +
                    "получении носков", e.getMessage()));
        }
    }


    @Operation(summary = "Update socks details. Обновление.",
            description = "Update the details of a specific sock item by its ID. Обновление по идентификатору.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Sock updated successfully. Успех.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SocksDto.class),
                            examples = @ExampleObject(value = "{\"color\": \"blue\", \"cottonPercentage\": 60," +
                                    " \"quantity\": 50}"))),
            @ApiResponse(responseCode = "404",
                    description = "Sock not found or insufficient stock. Не найден или недостаток.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Sock not found\"}"))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error. Ошибка сервера.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Unexpected server error\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSocks(@PathVariable Long id, @RequestBody SocksDto socksDto) {
        log.info("Request to update sock with id={} to {}", id, socksDto);

        try {
            SocksDto updatedSock = socksService.updateSock(id, socksDto);
            return ResponseEntity.ok(updatedSock);
        } catch (InsufficientStockException e) {
            log.error("Sock not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sock not found with id. Не найден. " + id);
        } catch (Exception e) {
            log.error("Error updating sock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating sock. " +
                    "Ошибка обновления");
        }
    }


    @Operation(summary = "Add new socks. Добавление.",
            description = "Add new socks to the inventory. Добавление на склад.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Income processed successfully. Успешное добавление"),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error. Ошибка серввера.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Error processing batch\"}")))
    })
    @PostMapping("/income")
    public ResponseEntity<String> addSocks(@RequestBody SocksDto socksDto) {
        log.info("Received request to add batch of socks: {}", socksDto);

        try {
            socksService.addSocks(socksDto);
            return ResponseEntity.ok("Income processed successfully. Успешное Добавление.");
        } catch (Exception e) {
            log.error("Error adding socks: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing income. " +
                    "Ошибка при добавлении");
        }
    }


    @Operation(summary = "Outcome socks quantity. Уменьшение количества. ",
            description = "Decrease the quantity of specific socks in the inventory. Уменьшение количества.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Socks quantity decreased successfully. Успешно уменьшено."),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input or insufficient stock. Неверный ввод или недостаточный запас.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Insufficient stock\"}"))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error. Ошибка сервера.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Error decreasing socks quantity\"}")))
    })
    @PostMapping("/outcome")
    public ResponseEntity<String> decreaseSocksQuantity(@RequestBody SocksDto socksDto) {
        log.info("Request to decrease socks quantity: {}", socksDto);

        try {
            socksService.decreaseSocksQuantity(socksDto.getColor(), socksDto.getCottonPercentage(),
                    socksDto.getQuantity());
            return ResponseEntity.ok("Socks quantity decreased successfully. Успешно уменьшено.");
        } catch (InsufficientStockException e) {
            log.error("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error decreasing socks quantity: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error decreasing socks quantity. " +
                    "Ошибка уменьшения.");
        }
    }


    @Operation(summary = "Process batch file. Добавление из файла.",
            description = "Process a batch file to add or update socks inventory. Supports CSV and Excel formats. " +
                    "Добавление из файлов.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Batch processed successfully. Успех."),
            @ApiResponse(responseCode = "400",
                    description = "Invalid file type or processing error. Неверный формат файла",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Invalid file type\"}"))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error. Ошибка сервера.",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Error processing batch file\"}")))
    })
    @PostMapping("/batch")
    public ResponseEntity<String> addBatch(@RequestParam("file") MultipartFile file) {
        log.info("Received request to process batch file: {}", file.getOriginalFilename());

        try {
            String fileName = file.getOriginalFilename();
            if (fileName.endsWith(".csv")) {
                socksService.processCsvFile(file);
            } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
                socksService.processExcelFile(file);
            } else {
                return ResponseEntity.badRequest().body("Invalid file type. Only CSV and Excel are supported." +
                        "Неверный файл. Только csv и excel.");
            }
            return ResponseEntity.ok("Batch processed successfully. Успешная загрузка.");
        } catch (FileProcessingException e) {
            log.error("Error processing file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing file: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing batch file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing batch file. " +
                    "Ошибка загрузки.");
        }
    }
}
