package kz.nik.socksbackspark.batchtest;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import kz.nik.socksbackspark.api.SocksController;
import kz.nik.socksbackspark.exceptions.FileProcessingException;
import kz.nik.socksbackspark.service.SocksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(SocksController.class)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocksService socksService;

    private MockMultipartFile csvFile;
    private MockMultipartFile excelFile;
    private MockMultipartFile invalidFile;

    @BeforeEach
    void setUp() {
        csvFile = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                "color,cottonPercentage,quantity\nRed,80,10".getBytes()
        );

        excelFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[]{1, 2, 3}
        );

        invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Invalid content".getBytes()
        );
    }

    @Test
    void testAddBatch_SuccessCsvFile() throws Exception {

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(csvFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch processed successfully. Успешная загрузка."));

        verify(socksService, times(1)).processCsvFile(any());
        verify(socksService, never()).processExcelFile(any());
    }

    @Test
    void testAddBatch_SuccessExcelFile() throws Exception {

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(excelFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Batch processed successfully. Успешная загрузка."));

        verify(socksService, never()).processCsvFile(any());
        verify(socksService, times(1)).processExcelFile(any());
    }

    @Test
    void testAddBatch_InvalidFileType() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Invalid content".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/socks/batch")
                        .file(invalidFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid file type. Only CSV and Excel are supported." +
                        "Неверный файл. Только csv и excel."));
    }


    @Test
    void testAddBatch_CsvProcessingError() throws Exception {

        doThrow(new FileProcessingException("Error processing CSV file")).when(socksService).processCsvFile(any());

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(csvFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error processing file: Error processing CSV file"));

        verify(socksService, times(1)).processCsvFile(any());
        verify(socksService, never()).processExcelFile(any());
    }

    @Test
    void testAddBatch_InternalServerError() throws Exception {

        doThrow(new RuntimeException("Unexpected error")).when(socksService).processExcelFile(any());

        mockMvc.perform(multipart("/api/socks/batch")
                        .file(excelFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing batch file. Ошибка загрузки."));

        verify(socksService, never()).processCsvFile(any());
        verify(socksService, times(1)).processExcelFile(any());
    }
}
