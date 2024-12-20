package kz.nik.socksbackspark.gettests;


import kz.nik.socksbackspark.api.SocksController;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.service.SocksService;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.Mockito.when;


@RunWith(SpringRunner.class)
@WebMvcTest(SocksController.class)
public class ControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SocksService socksService;

    @Test
    public void testGetAllSocks() throws Exception {
        List<SocksDto> socksList = Arrays.asList(
                new SocksDto(1L, "Red", 80, 10),
                new SocksDto(2L, "Blue", 75, 20)
        );

        when(socksService.getAllSocks()).thenReturn(socksList);

        mockMvc.perform(get("/api/socks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].color", is("Red")))
                .andExpect(jsonPath("$[1].color", is("Blue")));
    }

    @Test
    public void testGetSocksWithFilters() throws Exception {
        List<SocksDto> filteredSocks = Arrays.asList(
                new SocksDto(1L, "Red", 80, 10)
        );

        when(socksService.getFilteredSocks("Red", "greaterThan", 70,
                null, null, null, null))
                .thenReturn(filteredSocks);

        mockMvc.perform(get("/api/socks")
                        .param("color", "Red")
                        .param("operation", "greaterThan")
                        .param("cottonPercentage", "70"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].color", is("Red")));
    }


    @Test
    public void testGetSocksInternalServerError() throws Exception {
        when(socksService.getFilteredSocks("Red", "greaterThan", 70,
                null, null, null, null))
                .thenThrow(new RuntimeException("Internal server error"));

        mockMvc.perform(get("/api/socks")
                        .param("color", "Red")
                        .param("operation", "greaterThan")
                        .param("cottonPercentage", "70"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is("Error retrieving socks." +
                        " Ошибка при получении носков")));
    }
}