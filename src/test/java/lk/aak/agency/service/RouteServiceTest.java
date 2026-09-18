package lk.aak.agency.service;

import lk.aak.agency.model.Route;
import lk.aak.agency.repository.RouteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteService routeService;

    @Test
    void saveRoute_rejectsDuplicateNameFromAnotherRoute() {
        Route existing = new Route();
        existing.setId(1L);
        existing.setRouteName("Colombo North");

        Route newRoute = new Route();
        newRoute.setRouteName("Colombo North");

        when(routeRepository.existsByRouteNameIgnoreCase("Colombo North")).thenReturn(true);
        when(routeRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(existing));

        assertThatThrownBy(() -> routeService.saveRoute(newRoute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void saveRoute_allowsEditingSameRouteWithSameName() {
        Route existing = new Route();
        existing.setId(1L);
        existing.setRouteName("Colombo North");

        when(routeRepository.existsByRouteNameIgnoreCase("Colombo North")).thenReturn(true);
        when(routeRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(existing));
        when(routeRepository.save(any(Route.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Route saved = routeService.saveRoute(existing);

        assertThat(saved.getRouteName()).isEqualTo("Colombo North");
    }
}
