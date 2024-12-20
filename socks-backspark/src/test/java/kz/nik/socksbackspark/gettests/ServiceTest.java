package kz.nik.socksbackspark.gettests;

import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.InvalidDataFormatException;
import kz.nik.socksbackspark.mapper.SocksMapper;
import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import kz.nik.socksbackspark.service.SocksService;
import kz.nik.socksbackspark.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

public class ServiceTest {
    @Mock
    private SocksRepository socksRepository;

    @Mock
    private SocksMapper socksMapper;

    private SocksService socksService;

    private List<Socks> socksList;
    private List<SocksDto> socksDtoList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        socksService = new SocksServiceImpl(socksRepository, socksMapper);

        socksList = new ArrayList<>();
        socksDtoList = new ArrayList<>();

        // Пример носок (Socks)
        Socks sock1 = new Socks(1L, "red", 50, 100);
        Socks sock2 = new Socks(2L, "blue", 80, 150);
        socksList.add(sock1);
        socksList.add(sock2);

        // Пример DTO (SocksDto)
        SocksDto sockDto1 = new SocksDto(1L, "red", 50, 100);
        SocksDto sockDto2 = new SocksDto(2L, "blue", 80, 150);
        socksDtoList.add(sockDto1);
        socksDtoList.add(sockDto2);
    }

    @Test
    void testGetFilteredSocksWithColor() {

        when(socksRepository.findByColor("red")).thenReturn(Collections.singletonList(socksList.get(0)));
        when(socksMapper.toDtoList(anyList())).thenReturn(Collections.singletonList(socksDtoList.get(0)));

        List<SocksDto> result = socksService.getFilteredSocks("red", null, null,
                null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("red", result.get(0).getColor());
        assertEquals(50, result.get(0).getCottonPercentage());
        assertEquals(100, result.get(0).getQuantity());
    }

    @Test
    void testGetFilteredSocksWithCottonPercentageGreaterThan() {

        when(socksRepository.findByCottonPercentageGreaterThan(60)).thenReturn(Collections.singletonList(
                socksList.get(1)));
        when(socksMapper.toDtoList(anyList())).thenReturn(Collections.singletonList(socksDtoList.get(1)));

        List<SocksDto> result = socksService.getFilteredSocks(null, "greaterThan", 60,
                null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("blue", result.get(0).getColor());
        assertEquals(80, result.get(0).getCottonPercentage());
        assertEquals(150, result.get(0).getQuantity());
    }

    @Test
    void testGetFilteredSocksWithCottonPercentageRange() {

        when(socksRepository.findByCottonPercentageBetween(50, 80))
                .thenReturn(socksList);
        when(socksMapper.toDtoList(anyList())).thenReturn(socksDtoList);

        List<SocksDto> result = socksService.getFilteredSocks(null, null, null,
                50, 80, null, null);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetFilteredSocksWithSortBy() {

        when(socksRepository.findByColor("red")).thenReturn(Collections.singletonList(socksList.get(0)));
        when(socksMapper.toDtoList(anyList())).thenReturn(Collections.singletonList(socksDtoList.get(0)));

        List<SocksDto> result = socksService.getFilteredSocks("red", null, null,
                null, null, "color", "asc");

        assertNotNull(result);
        assertEquals("red", result.get(0).getColor());
    }

    @Test
    void testGetAllSocks() {

        when(socksRepository.findAll()).thenReturn(socksList);
        when(socksMapper.toDtoList(anyList())).thenReturn(socksDtoList);

        List<SocksDto> result = socksService.getAllSocks();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllSocksWithException() {

        when(socksRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> socksService.getAllSocks());
    }

    @Test
    void testGetFilteredSocksWithInvalidData() {

        assertThrows(InvalidDataFormatException.class, () -> {
            socksService.getFilteredSocks(null, "invalidOperation", null,
                    null, null, null, null);
        });
    }
}

