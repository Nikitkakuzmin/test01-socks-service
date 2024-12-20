package kz.nik.socksbackspark.updatetests;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.nik.socksbackspark.api.SocksController;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.service.SocksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SocksController.class)
@RunWith(SpringRunner.class)
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
    void testUpdateSocks_Success() throws Exception {

        Long sockId = 1L;
        SocksDto socksDto = new SocksDto(1L, "Red", 80, 50);
        SocksDto updatedSocksDto = new SocksDto(1L, "Red", 80, 50);

        when(socksService.updateSock(eq(sockId), any(SocksDto.class))).thenReturn(updatedSocksDto);

        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.color").value("Red"))
                .andExpect(jsonPath("$.cottonPercentage").value(80))
                .andExpect(jsonPath("$.quantity").value(50));

        verify(socksService, times(1)).updateSock(eq(sockId), any(SocksDto.class));
    }

    @Test
    void testUpdateSocks_NotFound() throws Exception {

        Long sockId = 999L;
        SocksDto socksDto = new SocksDto(999L, "Blue", 60, 100);

        when(socksService.updateSock(eq(sockId), any(SocksDto.class)))
                .thenThrow(new InsufficientStockException("Sock not found with id: " + sockId));

        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Sock not found with id. Не найден. " + sockId));

        verify(socksService, times(1)).updateSock(eq(sockId), any(SocksDto.class));
    }

    @Test
    void testUpdateSocks_InternalServerError() throws Exception {

        Long sockId = 1L;
        SocksDto socksDto = new SocksDto(1L, "Green", 75, 30);

        when(socksService.updateSock(eq(sockId), any(SocksDto.class)))
                .thenThrow(new RuntimeException("Error updating sock"));

        mockMvc.perform(put("/api/socks/{id}", sockId)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(socksDto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Error updating sock. Ошибка обновления"));

        verify(socksService, times(1)).updateSock(eq(sockId), any(SocksDto.class));
    }
}

