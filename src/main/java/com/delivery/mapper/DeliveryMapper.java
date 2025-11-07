package com.delivery.mapper;

import com.delivery.dto.DeliveryDTO;
import com.delivery.entity.Delivery;
import org.springframework.stereotype.Component;

@Component
public class DeliveryMapper {

    public DeliveryDTO toDTO(Delivery delivery) {
        if (delivery == null) {
            return null;
        }

        DeliveryDTO dto = new DeliveryDTO();
        dto.setId(delivery.getId());
        dto.setAddress(delivery.getAddress());
        dto.setLatitude(delivery.getLatitude());
        dto.setLongitude(delivery.getLongitude());
        dto.setWeight(delivery.getWeight());
        dto.setVolume(delivery.getVolume());
        dto.setPreferredTimeSlot(delivery.getPreferredTimeSlot());
        dto.setStatus(delivery.getStatus());
        dto.setTourId(delivery.getTour() != null ? delivery.getTour().getId() : null);
        dto.setOrder(delivery.getOrder());

        return dto;
    }

    public Delivery toEntity(DeliveryDTO dto) {
        if (dto == null) {
            return null;
        }

        Delivery delivery = new Delivery();
        delivery.setId(dto.getId());
        delivery.setAddress(dto.getAddress());
        delivery.setLatitude(dto.getLatitude());
        delivery.setLongitude(dto.getLongitude());
        delivery.setWeight(dto.getWeight());
        delivery.setVolume(dto.getVolume());
        delivery.setPreferredTimeSlot(dto.getPreferredTimeSlot());
        delivery.setStatus(dto.getStatus());
        delivery.setOrder(dto.getOrder());

        return delivery;
    }
}