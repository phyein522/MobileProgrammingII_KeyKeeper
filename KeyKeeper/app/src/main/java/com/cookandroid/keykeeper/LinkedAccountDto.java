package com.cookandroid.keykeeper;

public class LinkedAccountDto {
    private Long accountId;
    private Long linkedAccountId;

    public LinkedAccountDto() {}
    public LinkedAccountDto(Long accountId, Long linkedAccountId) {
        this.accountId = accountId;
        this.linkedAccountId = linkedAccountId;
    }

    public Long getAccountId() {
        return accountId;
    }
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getLinkedAccountId() {
        return linkedAccountId;
    }
    public void setLinkedAccountId(Long linkedAccountId) {
        this.linkedAccountId = linkedAccountId;
    }
}
