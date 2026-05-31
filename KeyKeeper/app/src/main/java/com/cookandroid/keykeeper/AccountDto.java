package com.cookandroid.keykeeper;

public class AccountDto {
    private Long id;
    private String name;
    private String url;
    private String pw;
    private String lastChangedDate;
    private String changeCycle;
    private String memo;
    private String iv;

    public AccountDto() {}
    public AccountDto(Long id, String name, String url, String pw, String lastChangedDate, String changeCycle, String memo, String iv) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.pw = pw;
        this.lastChangedDate = lastChangedDate;
        this.changeCycle = changeCycle;
        this.memo = memo;
        this.iv = iv;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPw() {
        return pw;
    }
    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getLastChangedDate() {
        return lastChangedDate;
    }
    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public String getChangeCycle() {
        return changeCycle;
    }
    public void setChangeCycle(String changeCycle) {
        this.changeCycle = changeCycle;
    }

    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getIv() {
        return iv;
    }
    public void setIv(String iv) {
        this.iv = iv;
    }
}
