// src/main/java/com/pokerapp/api/dto/request/TableSettingsDto.java
package com.pokerapp.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableSettingsDto {
    @NotBlank
    @Size(min = 3, max = 50)
    private String name;

    @Size(max = 255)
    private String description;

    @NotNull
    @Positive
    private Integer maxPlayers;

    @NotNull
    @Positive
    private Double minBuyIn;

    @NotNull
    @Positive
    private Double maxBuyIn;

    private Boolean isPrivate = false;

    public @NotBlank @Size(min = 3, max = 50) String getName() {
        return name;
    }

    public void setName(@NotBlank @Size(min = 3, max = 50) String name) {
        this.name = name;
    }

    public @Size(max = 255) String getDescription() {
        return description;
    }

    public void setDescription(@Size(max = 255) String description) {
        this.description = description;
    }

    public @NotNull @Positive Integer getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(@NotNull @Positive Integer maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public @NotNull @Positive Double getMinBuyIn() {
        return minBuyIn;
    }

    public void setMinBuyIn(@NotNull @Positive Double minBuyIn) {
        this.minBuyIn = minBuyIn;
    }

    public @NotNull @Positive Double getMaxBuyIn() {
        return maxBuyIn;
    }

    public void setMaxBuyIn(@NotNull @Positive Double maxBuyIn) {
        this.maxBuyIn = maxBuyIn;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }
}
