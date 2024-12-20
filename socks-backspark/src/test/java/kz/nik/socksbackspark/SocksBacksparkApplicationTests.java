package kz.nik.socksbackspark;

import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.mapper.SocksMapper;
import kz.nik.socksbackspark.model.Socks;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SocksBacksparkApplicationTests {

    @Autowired
    private SocksMapper socksMapper;


    @Test
    public void testToDto() {

        Socks socks = new Socks();
        socks.setId(1L);
        socks.setColor("white");
        socks.setQuantity(500);
        socks.setCottonPercentage(25);

        SocksDto socksDto = socksMapper.toDto(socks);

        assertNotNull(socksDto);
        assertEquals(socks.getId(), socksDto.getId());
        assertEquals(socks.getQuantity(), socksDto.getQuantity());
        assertEquals(socks.getCottonPercentage(), socksDto.getCottonPercentage());
        assertEquals(socks.getColor(), socksDto.getColor());
    }

    @Test
    public void testToEntity() {

        SocksDto socksDto = new SocksDto();
        socksDto.setId(1L);
        socksDto.setColor("brown");
        socksDto.setQuantity(500);
        socksDto.setCottonPercentage(30);

        Socks socks = socksMapper.toEntity(socksDto);

        assertNotNull(socks);
        assertEquals(socksDto.getId(), socks.getId());
        assertEquals(socksDto.getColor(), socks.getColor());
        assertEquals(socksDto.getQuantity(), socks.getQuantity());
        assertEquals(socksDto.getCottonPercentage(), socks.getCottonPercentage());
    }

    @Test
    public void testToDtoList() {

        Socks socks1 = new Socks();
        socks1.setId(1L);
        socks1.setColor("red");
        socks1.setQuantity(850);
        socks1.setCottonPercentage(78);

        Socks socks2 = new Socks();
        socks2.setId(2L);
        socks2.setColor("gold");
        socks2.setQuantity(510);
        socks2.setCottonPercentage(96);

        List<SocksDto> socksDtoList = socksMapper.toDtoList(Arrays.asList(socks1, socks2));

        assertNotNull(socksDtoList);
        assertEquals(2, socksDtoList.size());
        assertEquals(socks1.getId(), socksDtoList.get(0).getId());
        assertEquals(socks2.getId(), socksDtoList.get(1).getId());
    }

}
