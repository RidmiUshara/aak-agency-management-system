package lk.aak.agency.service;

import lk.aak.agency.model.Route;
import lk.aak.agency.repository.RouteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll(Sort.by(Sort.Direction.ASC, "routeName"));
    }

    public List<Route> getActiveRoutes() {
        return getAllRoutes().stream()
                .filter(route -> "ACTIVE".equals(route.getStatus()))
                .toList();
    }

    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    public Route saveRoute(Route route) {
        String routeName = route.getRouteName().trim();

        boolean nameTaken = routeRepository.existsByRouteNameIgnoreCase(routeName)
                && getAllRoutes().stream()
                        .filter(existing -> existing.getRouteName().equalsIgnoreCase(routeName))
                        .anyMatch(existing -> !existing.getId().equals(route.getId()));

        if (nameTaken) {
            throw new IllegalArgumentException(
                    "A route named \"" + routeName + "\" already exists."
            );
        }

        route.setRouteName(routeName);
        return routeRepository.save(route);
    }
}
