package com.cookandroid.keykeeper;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CheckUrl {
    private Context context;
    private DBHelper dbHelper;

    public CheckUrl(Context context){
        this.context = context;
        this.dbHelper = new DBHelper(context);
    }

    public void insertCsvToDB() {
        SQLiteDatabase writableDB = dbHelper.getWritableDatabase();

        writableDB.beginTransaction();  //트랜잭션 사용

        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.phishing_url);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;
            String headerLine = reader.readLine();  //헤더 제거
            int urlColumIndex = 1;

            while((line = reader.readLine()) != null) { //읽은 행이 null이 아님
                if(line.trim().isEmpty()) { //빈 행이라면
                    continue;   //건너뜀
                }

                String[] columns = line.split(",");
                if(columns.length <= urlColumIndex) {   //열 개수가 부족함(url 행이 없음)
                    continue;   //건너뜀
                }

                String url = columns[urlColumIndex];    //url 행
                url = url.trim();   //앞뒤 공백 제거
                url = url.replace("\"", "");    // " 제거
                url = normalizeUrl(url);    //url 정규화

                if(url.isEmpty()) { //url이 비어있음
                    continue;   //건너뜀
                }

                ContentValues values = new ContentValues();
                values.put(DBHelper.COL_URL, url);

                writableDB.insertWithOnConflict(DBHelper.TABLE_PHISHING_URL, null, values, SQLiteDatabase.CONFLICT_IGNORE);  //이미 같은 url 있으면 무시
                reader.close();
                writableDB.setTransactionSuccessful();  //트랜잭션 성공
            }

        } catch(Exception e) {
        } finally {
            writableDB.endTransaction();    //트랜잭션 종료
        }
    }

    private String normalizeUrl(String url) {
        if(url == null) {
            return "";
        }

        url = url.trim();   //앞뒤 공백 제거
        url = url.replace("\"", "");    // " 제거
        url = url.toLowerCase();    //소문자 변환

        while(url.endsWith("/")) {  //마지막에 / 존재
            url = url.substring(0, url.length() - 1);   // / 제거
        }

        return url;
    }

    public boolean isPhishingUrl(String url) {
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase();

        String normalizedUrl = normalizeUrl(url);

        Cursor cursor = null;

        try {
            cursor = readableDB.rawQuery("SELECT COUNT(*) FROM " + DBHelper.TABLE_PHISHING_URL + " WHERE " + DBHelper.COL_URL + " = ?"
                    , new String[]{normalizedUrl});

            if(cursor != null) {
                if(cursor.moveToFirst()) {
                    int count = cursor.getInt(0);
                    return count > 0;
                }
            }
        } catch(Exception e) {
        } finally {
            if(cursor != null) {
                cursor.close();
            }
        }

        return false;
    }

    public void insertCsvOnlyFirstTime() {
        SharedPreferences pref = context.getSharedPreferences("app_pref", MODE_PRIVATE);
        boolean isCsvInserted = pref.getBoolean("is_csv_inserted", false);  //csv를 이미 저장했는지
        if(!isCsvInserted) {
            insertCsvToDB();   //csv를 db에 저장
            pref.edit().putBoolean("is_csv_inserted", true).apply();    //저좡 완료 여부 저장
        }
    }

    public Integer checkUrlScore(String url) {
        int score = 0;
        url = normalizeUrl(url);

        if(isPhishingUrl(url)) {    //db의 phishing_url 테이블과 일치
            score += 50;
        }
        if(url.length() >= 75) {    //길이가 긺
            score += 10;
        }
        if(containsIpAddress(url)) {    //ip 주소 사용
            score += 20;
        }
        if(url.contains("@")) { // @ 포함
            score += 20;
        }
        if(countChar(url, '-') >= 3) {  // - 개수가 많음
            score += 10;
        }
        if(countChar(url, '.') >= 4) {  // . 개수가 많음
            score += 10;
        }
        if(url.startsWith("http://")) { //https가 아닌 http 사용
            score += 10;
        }

        if(score >= 100) {
            score = 100;
        }

        return score;
    }

    private boolean containsIpAddress(String url) {
        String ipPattern = "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b";
        return url.matches(".*" + ipPattern + ".*");
    }

    private int countChar(String text, char target) {
        int count = 0;

        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) == target) {
                count++;
            }
        }

        return count;
    }

    public static String checkUrl(int score) {
        if(score >= 70) {
            return "URL 위험성: 높음";
        } else if(score >= 40) {
            return "URL 위험성: 주의 필요";
        } else if(score >= 20) {
            return "URL 위험성: 의심스러움";
        } else if(score >= 5) {
            return "URL 위험성: 큰 위험은 발견되지 않음";
        }
        return "";
    }
}
