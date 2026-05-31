package com.cookandroid.keykeeper;

import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;

public class CheckPw {
    //비밀번호 강도를 점수로 반환(0: 매우 약함 ~ 4: 매우 강함)
    public static int checkPwScore(String pw) {
        Zxcvbn zxcvbn = new Zxcvbn();
        Strength strength = zxcvbn.measure(pw);
        return strength.getScore();
    }

    //점수 별 출력 문자열
    public static String checkPw(String pw) {
        return checkPw(checkPwScore(pw));
    }
    public static String checkPw(int score) {
        switch(score) {
            case 0:
                return "비밀번호 강도: 매우 약함";
            case 1:
                return "비밀번호 강도: 약함";
            case 2:
                return "비밀번호 강도: 보통";
            case 3:
                return "비밀번호 강도: 강함";
            case 4:
                return "비밀번호 강도: 매우 강함";
            default:
                return "비밀번호 강도: 알 수 없음";
        }
    }
}
