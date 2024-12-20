package kz.nik.socksbackspark.decreasetest;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import kz.nik.socksbackspark.exceptions.InsufficientStockException;
import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import kz.nik.socksbackspark.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.slf4j.Logger;

import java.util.Collections;

public class ServiceTest {
    @Mock
    private SocksRepository socksRepository;

    @InjectMocks
    private SocksServiceImpl socksService;

    private Socks sock;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sock = new Socks(1L,"Red", 80, 10);
    }

    @Test
    void testDecreaseSocksQuantity_Success() {

        when(socksRepository.findByColorAndCottonPercentage("Red", 80)).
                thenReturn(Collections.singletonList(sock));

        socksService.decreaseSocksQuantity("Red", 80, 5);

        assertEquals(5, sock.getQuantity());
        verify(socksRepository, times(1)).save(sock);
    }

    @Test
    void testDecreaseSocksQuantity_InsufficientStock_NoSocksFound() {

        when(socksRepository.findByColorAndCottonPercentage("Red", 80))
                .thenReturn(Collections.emptyList());

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            socksService.decreaseSocksQuantity("Red", 80, 5);
        });
        assertEquals("No socks found with the given color and cotton percentage", exception.getMessage());
    }

    @Test
    void testDecreaseSocksQuantity_InsufficientStock_NotEnoughSocks() {

        when(socksRepository.findByColorAndCottonPercentage("Red", 80))
                .thenReturn(Collections.singletonList(sock));

        InsufficientStockException exception = assertThrows(InsufficientStockException.class, () -> {
            socksService.decreaseSocksQuantity("Red", 80, 15);
        });
        assertEquals("Not enough socks in stock", exception.getMessage());
    }

    @Test
    void testDecreaseSocksQuantity_Error() {

        when(socksRepository.findByColorAndCottonPercentage("Red", 80))
                .thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            socksService.decreaseSocksQuantity("Red", 80, 5);
        });
        assertEquals("Error decreasing sock quantity", exception.getMessage());
    }
}
