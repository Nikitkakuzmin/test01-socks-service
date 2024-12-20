package kz.nik.socksbackspark.addtests;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.nik.socksbackspark.api.SocksController;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.service.SocksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SocksController.class)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocksService socksService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testAddSocks_Success() throws Exception {

        SocksDto socksDto = new SocksDto(null, "Red", 80, 50);

        mockMvc.perform(post("/api/socks/income")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Income processed successfully. Успешное Добавление."));

        verify(socksService, times(1)).addSocks(any(SocksDto.class));
    }

    @Test
    void testAddSocks_InternalServerError() throws Exception {

        SocksDto socksDto = new SocksDto(null, "Blue", 60, 30);

        doThrow(new RuntimeException("Error processing income")).when(socksService).addSocks(any(SocksDto.class));

        mockMvc.perform(post("/api/socks/income")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error processing income. Ошибка при добавлении"));

        verify(socksService, times(1)).addSocks(any(SocksDto.class));
    }
}

