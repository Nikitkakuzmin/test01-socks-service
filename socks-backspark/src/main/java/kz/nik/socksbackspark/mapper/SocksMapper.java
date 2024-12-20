package kz.nik.socksbackspark.mapper;

import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.model.Socks;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SocksMapper {
    SocksDto toDto(Socks socks);
    Socks toEntity(SocksDto socksDto);
    List<SocksDto> toDtoList(List<Socks> list);
}
