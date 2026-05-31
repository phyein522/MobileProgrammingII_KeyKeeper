package com.cookandroid.keykeeper;

public class EncryptResultDto {
    public String pw;
    public String iv;

    public EncryptResultDto() {}
    public EncryptResultDto(String pw, String iv) {
        this.pw = pw;
        this.iv = iv;
    }

    public String getPw() {
        return pw;
    }
    public void setPw(String pw) {
        this.pw = pw;
    }

    public String getIv() {
        return iv;
    }
    public void setIv(String iv) {
        this.iv = iv;
    }
}
