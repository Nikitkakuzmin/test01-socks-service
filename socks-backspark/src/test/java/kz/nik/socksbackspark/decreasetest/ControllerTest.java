package kz.nik.socksbackspark.decreasetest;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.nik.socksbackspark.api.SocksController;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
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
    void testDecreaseSocksQuantity_Success() throws Exception {

        SocksDto socksDto = new SocksDto(null, "Red", 80, 10);

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Socks quantity decreased successfully. Успешно уменьшено."));

        verify(socksService, times(1)).decreaseSocksQuantity(eq("Red"),
                eq(80), eq(10));
    }

    @Test
    void testDecreaseSocksQuantity_InsufficientStock() throws Exception {

        SocksDto socksDto = new SocksDto(null, "Blue", 60, 50);

        doThrow(new InsufficientStockException("Not enough socks in stock")).when(socksService)
                .decreaseSocksQuantity(eq("Blue"), eq(60), eq(50));

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not enough socks in stock"));

        verify(socksService, times(1)).decreaseSocksQuantity(eq("Blue"),
                eq(60), eq(50));
    }

    @Test
    void testDecreaseSocksQuantity_InvalidArgument() throws Exception {

        SocksDto socksDto = new SocksDto(null, "", 60, 10);

        doThrow(new IllegalArgumentException("Invalid argument: color cannot be empty")).when(socksService)
                .decreaseSocksQuantity(eq(""), eq(60), eq(10));

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid argument: color cannot be empty"));

        verify(socksService, times(1)).decreaseSocksQuantity(eq(""), eq(60), eq(10));
    }

    @Test
    void testDecreaseSocksQuantity_InternalServerError() throws Exception {

        SocksDto socksDto = new SocksDto(null, "Green", 75, 5);

        doThrow(new RuntimeException("Unexpected error")).when(socksService)
                .decreaseSocksQuantity(eq("Green"), eq(75), eq(5));

        mockMvc.perform(post("/api/socks/outcome")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error decreasing socks quantity. Ошибка уменьшения."));

        verify(socksService, times(1)).decreaseSocksQuantity(eq("Green"), eq(75), eq(5));
    }
}
