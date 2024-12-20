package kz.nik.socksbackspark.updatetests;

import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.mapper.SocksMapper;
import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import kz.nik.socksbackspark.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Optional;

import static org.mockito.Mockito.when;

public class ServiceTest {
    @Mock
    private SocksRepository socksRepository;

    @Mock
    private SocksMapper socksMapper;

    @InjectMocks
    private SocksServiceImpl socksService;

    private Socks existingSock;
    private SocksDto socksDto;
    private SocksDto updatedSocksDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        existingSock = new Socks(1L, "red", 50, 100);

        socksDto = new SocksDto(1L, "blue", 60, 150);

        updatedSocksDto = new SocksDto(1L, "blue", 60, 150);
    }

    @Test
    void testUpdateSockSuccessfully() {

        when(socksRepository.findById(1L)).thenReturn(Optional.of(existingSock));

        when(socksRepository.save(any(Socks.class))).thenReturn(existingSock);

        when(socksMapper.toDto(any(Socks.class))).thenReturn(updatedSocksDto);

        SocksDto result = socksService.updateSock(1L, socksDto);

        assertNotNull(result);
        assertEquals("blue", result.getColor());
        assertEquals(60, result.getCottonPercentage());
        assertEquals(150, result.getQuantity());

        verify(socksRepository, times(1)).findById(1L);
        verify(socksRepository, times(1)).save(any(Socks.class));
    }

    @Test
    void testUpdateSockNotFound() {

        when(socksRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(InsufficientStockException.class, () -> {
            socksService.updateSock(1L, socksDto);
        });

        verify(socksRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateSockWithException() {

        when(socksRepository.findById(1L)).thenReturn(Optional.of(existingSock));

        when(socksRepository.save(any(Socks.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            socksService.updateSock(1L, socksDto);
        });

        verify(socksRepository, times(1)).findById(1L);
        verify(socksRepository, times(1)).save(any(Socks.class));
    }
}


