package com.delivery.service;

import com.delivery.entity.*;
import com.delivery.repository.DeliveryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryHistoryServiceTest {

    @Mock
    private DeliveryHistoryRepository deliveryHistoryRepository;

    @InjectMocks
    private DeliveryHistoryService deliveryHistoryService;

    private Tour tour;
    private Customer customer;
    private Delivery delivery;
    private DeliveryHistory deliveryHistory;

    @BeforeEach
    void setUp() {
        // Setup Customer
        customer = new Customer();
        customer.setId(1L);
        customer.setName("Client History");
        customer.setAddress("123 Rue History, Casablanca");

        // Setup Delivery
        delivery = new Delivery();
        delivery.setId(1L);
        delivery.setWeight(5.0);
        delivery.setVolume(0.5);
        delivery.setCustomer(customer);
        delivery.setOrder(1);

        // Setup Tour
        tour = new Tour();
        tour.setId(1L);
        tour.setDate(LocalDate.now());
        tour.setDeliveries(Arrays.asList(delivery));

        // Setup Delivery History
        deliveryHistory = new DeliveryHistory();
        deliveryHistory.setId(1L);
        deliveryHistory.setCustomer(customer);
        deliveryHistory.setDelivery(delivery);
        deliveryHistory.setTour(tour);
        deliveryHistory.setDeliveryDate(LocalDate.now());
        deliveryHistory.setPlannedTime(LocalDateTime.now().withHour(10).withMinute(0));
        deliveryHistory.setActualTime(LocalDateTime.now().withHour(10).withMinute(10));
        deliveryHistory.calculateDelay();
        deliveryHistory.setDayOfWeekFromDate();
    }

    @Test
    void createDeliveryHistoryFromCompletedTour_ShouldCreateHistoryForEachDelivery() {
        // Arrange
        when(deliveryHistoryRepository.save(any(DeliveryHistory.class))).thenReturn(deliveryHistory);

        // Act
        deliveryHistoryService.createDeliveryHistoryFromCompletedTour(tour);

        // Assert
        verify(deliveryHistoryRepository, times(1)).save(any(DeliveryHistory.class));
        // Vérifier que l'historique est créé pour chaque livraison
    }

    @Test
    void getCustomerDeliveryHistory_ShouldReturnCustomerHistory() {
        // Arrange
        List<DeliveryHistory> expectedHistory = Arrays.asList(deliveryHistory);
        when(deliveryHistoryRepository.findByCustomerId(1L)).thenReturn(expectedHistory);

        // Act
        List<DeliveryHistory> result = deliveryHistoryService.getCustomerDeliveryHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.get(0).getCustomer().getId());
        verify(deliveryHistoryRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void getTourDeliveryHistory_ShouldReturnTourHistory() {
        // Arrange
        List<DeliveryHistory> expectedHistory = Arrays.asList(deliveryHistory);
        when(deliveryHistoryRepository.findByTourId(1L)).thenReturn(expectedHistory);

        // Act
        List<DeliveryHistory> result = deliveryHistoryService.getTourDeliveryHistory(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(tour.getId(), result.get(0).getTour().getId());
        verify(deliveryHistoryRepository, times(1)).findByTourId(1L);
    }

    @Test
    void getDelayedDeliveries_ShouldReturnDelayedHistory() {
        // Arrange
        List<DeliveryHistory> delayedHistory = Arrays.asList(deliveryHistory);
        when(deliveryHistoryRepository.findDelaysGreaterThan(10)).thenReturn(delayedHistory);

        // Act
        List<DeliveryHistory> result = deliveryHistoryService.getDelayedDeliveries(10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(deliveryHistoryRepository, times(1)).findDelaysGreaterThan(10);
    }

    @Test
    void existsHistoryForTour_WithHistory_ShouldReturnTrue() {
        // Arrange
        when(deliveryHistoryRepository.findByTourId(1L)).thenReturn(Arrays.asList(deliveryHistory));

        // Act
        boolean exists = deliveryHistoryService.existsHistoryForTour(1L);

        // Assert
        assertTrue(exists);
        verify(deliveryHistoryRepository, times(1)).findByTourId(1L);
    }

    @Test
    void getDeliveryHistoryPaged_ShouldReturnPagedResults() {
        // Arrange
        Page<DeliveryHistory> expectedPage = new PageImpl<>(Arrays.asList(deliveryHistory));
        when(deliveryHistoryRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // Act
        Page<DeliveryHistory> result = deliveryHistoryService.getDeliveryHistoryPaged(mock(Pageable.class));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(deliveryHistoryRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void deliveryHistoryDelayCalculation_ShouldComputeCorrectly() {
        // Arrange
        deliveryHistory.setPlannedTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        deliveryHistory.setActualTime(LocalDateTime.of(2024, 1, 1, 10, 15));

        // Act
        deliveryHistory.calculateDelay();

        // Assert
        assertEquals(15, deliveryHistory.getDelayMinutes());
    }

    @Test
    void deliveryHistoryDayOfWeek_ShouldSetFromDate() {
        // Arrange
        deliveryHistory.setDeliveryDate(LocalDate.of(2024, 1, 1)); // Lundi

        // Act
        deliveryHistory.setDayOfWeekFromDate();

        // Assert
        assertEquals(java.time.DayOfWeek.MONDAY, deliveryHistory.getDayOfWeek());
    }
}