package com.cookandroid.keykeeper;

public class WarningDto {
    private Long accountId;
    private Integer urlWarning;
    private Integer pwWarning;

    public WarningDto() {}
    public WarningDto(Long accountId, Integer urlWarning, Integer pwWarning) {
        this.accountId = accountId;
        this.urlWarning = urlWarning;
        this.pwWarning = pwWarning;
    }

    public Long getAccountId() {
        return accountId;
    }
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getUrlWarning() {
        return urlWarning;
    }
    public void setUrlWarning(Integer urlWarning) {
        this.urlWarning = urlWarning;
    }

    public Integer getPwWarning() {
        return pwWarning;
    }
    public void setPwWarning(Integer pwWarning) {
        this.pwWarning = pwWarning;
    }
}
