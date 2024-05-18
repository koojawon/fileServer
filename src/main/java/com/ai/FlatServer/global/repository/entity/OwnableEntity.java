package com.ai.FlatServer.global.repository.entity;

import com.ai.FlatServer.domain.user.repository.entity.User;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
@Data
public class OwnableEntity extends BaseEntity {

    private Long ownerId;

    public boolean checkAuthority(User user) {

        return ownerId.equals(user.getId());
    }
}
