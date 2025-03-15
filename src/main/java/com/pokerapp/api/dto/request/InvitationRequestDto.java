// src/main/java/com/pokerapp/api/dto/request/InvitationRequestDto.java
package com.pokerapp.api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class InvitationRequestDto {
    @NotNull
    private Long recipientId;

    @NotNull
    private Long tableId;

    @Size(max = 500)
    private String message;

    public @NotNull Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(@NotNull Long recipientId) {
        this.recipientId = recipientId;
    }

    public @NotNull Long getTableId() {
        return tableId;
    }

    public void setTableId(@NotNull Long tableId) {
        this.tableId = tableId;
    }

    public @Size(max = 500) String getMessage() {
        return message;
    }

    public void setMessage(@Size(max = 500) String message) {
        this.message = message;
    }
}
