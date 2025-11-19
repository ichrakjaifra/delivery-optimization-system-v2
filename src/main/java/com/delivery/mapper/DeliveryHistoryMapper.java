package com.delivery.mapper;

import com.delivery.dto.DeliveryHistoryDTO;
import com.delivery.entity.DeliveryHistory;
import org.springframework.stereotype.Component;

@Component
public class DeliveryHistoryMapper {

    public DeliveryHistoryDTO toDTO(DeliveryHistory history) {
        if (history == null) {
            return null;
        }

        DeliveryHistoryDTO dto = new DeliveryHistoryDTO();
        dto.setId(history.getId());


        if (history.getCustomer() != null) {
            dto.setCustomerId(history.getCustomer().getId());
            dto.setCustomerName(history.getCustomer().getName());
        }
        if (history.getDelivery() != null) {
            dto.setDeliveryId(history.getDelivery().getId());
            dto.setDeliveryAddress(history.getDelivery().getAddress());
        }
        if (history.getTour() != null) {
            dto.setTourId(history.getTour().getId());
        }


        dto.setDeliveryDate(history.getDeliveryDate());
        dto.setPlannedTime(history.getPlannedTime());
        dto.setActualTime(history.getActualTime());
        dto.setDelayMinutes(history.getDelayMinutes());
        dto.setDayOfWeek(history.getDayOfWeek());
        dto.setNotes(history.getNotes());

        return dto;
    }
}