package kz.nik.socksbackspark.service;

import com.opencsv.CSVReader;
import kz.nik.socksbackspark.dto.SocksDto;
import kz.nik.socksbackspark.model.Socks;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.List;

public interface SocksService {

    List<SocksDto> getFilteredSocks(
            String color,
            String operation,
            Integer cottonPercentage,
            Integer cottonPercentageFrom,
            Integer cottonPercentageTo,
            String sortBy,
            String sortDirection);
    List<SocksDto>getAllSocks();

    SocksDto updateSock(Long id, SocksDto socksDto);

    void addSocks(SocksDto socksDto);

    void decreaseSocksQuantity(String color, int cottonPercentage, int quantity);

     void processCsvFile(MultipartFile file) throws Exception;
     void processExcelFile(MultipartFile file) throws Exception;


}
