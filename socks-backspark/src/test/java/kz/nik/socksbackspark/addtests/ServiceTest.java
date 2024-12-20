package kz.nik.socksbackspark.addtests;

import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.mapper.SocksMapper;
import kz.nik.socksbackspark.model.Socks;
import kz.nik.socksbackspark.repository.SocksRepository;
import kz.nik.socksbackspark.service.impl.SocksServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
public class ServiceTest {
    @Mock
    private SocksRepository socksRepository;

    @Mock
    private SocksMapper socksMapper;

    @InjectMocks
    private SocksServiceImpl socksService;

    private SocksDto socksDto;
    private Socks existingSock;
    private Socks newSock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        socksDto = new SocksDto(1L, "blue", 60, 10);

        existingSock = new Socks(1L, "blue", 60, 20);

        newSock = new Socks(2L, "green", 80, 15);
    }

    @Test
    void testAddExistingSocks() {

        when(socksRepository.findByColorAndCottonPercentage("blue", 60))
                .thenReturn(List.of(existingSock));

        when(socksRepository.save(any(Socks.class))).thenReturn(existingSock);

        socksService.addSocks(socksDto);

        assertEquals(30, existingSock.getQuantity());


        verify(socksRepository, times(1)).findByColorAndCottonPercentage(
                "blue", 60);
        verify(socksRepository, times(1)).save(existingSock);
    }

    @Test
    void testAddNewSocks() {

        SocksDto socksDto = new SocksDto(0L, "green", 80, 10);

        when(socksRepository.findByColorAndCottonPercentage("green", 80)).thenReturn(List.of());

        Socks newSock = new Socks(0L, "green", 80, 10);
        when(socksMapper.toEntity(socksDto)).thenReturn(newSock);

        when(socksRepository.save(any(Socks.class))).thenReturn(newSock);

        socksService.addSocks(socksDto);

        verify(socksRepository, times(1)).save(newSock);

        verify(socksRepository, times(1)).findByColorAndCottonPercentage("green", 80);
    }



    @Test
    void testAddSocksError() {

        when(socksRepository.findByColorAndCottonPercentage("blue", 60)).thenReturn(
                List.of(existingSock));
        when(socksRepository.save(any(Socks.class))).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> {
            socksService.addSocks(socksDto);
        });

        verify(socksRepository, times(1)).findByColorAndCottonPercentage(
                "blue", 60);
        verify(socksRepository, times(1)).save(existingSock);
    }
}

