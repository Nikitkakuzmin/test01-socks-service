package kz.nik.socksbackspark.batchtest;

import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import kz.nik.socksbackspark.service.impl.SocksServiceImpl;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
public class ServiceTest {
    @InjectMocks
    private SocksServiceImpl socksService;

    @Mock
    private SocksRepository socksRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessCsvFile() throws Exception {

        String csvContent = "color,cottonPercentage,quantity\nRed,50,100\nBlue,80,200";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        MultipartFile file = new MockMultipartFile("file", "test.csv",
                "text/csv", inputStream);

        when(socksRepository.saveAll(anyList())).thenReturn(List.of(new Socks(), new Socks()));

        socksService.processCsvFile(file);

        verify(socksRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testProcessCsvFile_withError() throws Exception {

        String csvContent = "color,cottonPercentage,quantity\nRed,50,100\nBlue,80,200";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv",
                inputStream);

        doThrow(new RuntimeException("Error")).when(socksRepository).saveAll(anyList());

        assertThrows(RuntimeException.class, () -> socksService.processCsvFile(file));
    }

    @Test
    public void testProcessExcelFile() {
        String filePath = "test.xlsx";

        try (FileInputStream fis = new FileInputStream(filePath)) {

            Workbook workbook = new XSSFWorkbook(fis);

            assertNotNull(workbook, "Excel файл должен быть открыт.");

            int numberOfSheets = workbook.getNumberOfSheets();
            assertTrue(numberOfSheets > 0, "Excel файл должен содержать хотя бы один лист.");

        } catch (IOException e) {
            fail("Произошла ошибка при чтении файла: " + e.getMessage());
        }
    }
    @Test
    void testProcessExcelFile_withError() throws Exception {

        String excelContent = "color,cottonPercentage,quantity\nRed,50,100\nBlue,80,200";
        InputStream inputStream = new ByteArrayInputStream(excelContent.getBytes());
        MultipartFile file = new MockMultipartFile("file", "test.xlsx", "application/vnd" +
                ".openxmlformats-officedocument.spreadsheetml.sheet", inputStream);

        doThrow(new RuntimeException("Error processing Excel")).when(socksRepository).save(any(Socks.class));

        assertThrows(RuntimeException.class, () -> socksService.processExcelFile(file));
    }
}


