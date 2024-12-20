package kz.nik.socksbackspark.repository;

import kz.nik.socksbackspark.model.Socks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SocksRepository extends JpaRepository<Socks, Long> {
    List<Socks> findByColor(String color);
    List<Socks> findByCottonPercentageGreaterThan(Integer cottonPercentage);
    List<Socks> findByCottonPercentageLessThan(Integer cottonPercentage);
    List<Socks> findByCottonPercentage(Integer cottonPercentage);
    List<Socks> findByColorAndCottonPercentageGreaterThan(String color, int cottonPercentage);
    List<Socks> findByColorAndCottonPercentageLessThan(String color, int cottonPercentage);
    List<Socks> findByColorAndCottonPercentage(String color, int cottonPercentage);
    List<Socks> findByCottonPercentageBetween(int cottonPercentageFrom, int cottonPercentageTo);

}
