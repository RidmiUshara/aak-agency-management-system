package lk.aak.agency.repository;

import lk.aak.agency.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RouteRepository extends JpaRepository<Route, Long> {

    boolean existsByRouteNameIgnoreCase(String routeName);
}
