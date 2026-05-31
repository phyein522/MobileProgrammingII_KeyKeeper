package com.cookandroid.keykeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationBarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    LinearLayout layoutMain;    //카드 목록이 추가될 레이아웃
    LayoutInflater inflater;
    NavigationBarView bottomNavigation; //하단 네비게이션
    DBHelper dbHelper;
    List<AccountDto> accountList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("KeyKeeper");

        dbHelper = new DBHelper(this);
        layoutMain = (LinearLayout)findViewById(R.id.layoutMain);
        setBottomNavigation();  //하단 네비게이션 설정
        setCard();  //카드 목록 설정
    }

    private void setBottomNavigation() {    //하단 네비게이션 설정
        bottomNavigation = (NavigationBarView)findViewById(R.id.bottomNavigationMain);
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { //하단 네비게이션 아이템 설택 시 이벤트
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.page_activity_main) {   //하단 네비게이션 - 메인 (현재 위치)
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_add_pw) { //하단 네비게이션 - pw 추가
                    Intent intent = new Intent(getApplicationContext(), AddPwActivity.class);
                    intent.putExtra("isUpdate", false); //추가 작업 (수정/삭제 작업 아님)
                    intent.putExtra("id", -1);  //수정/삭제 위한 id 없음
//                    startActivity(intent);
                    startActivityForResult(intent, 0);
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_account_list) {   //하단 네비게이션 - 연결된 계정 목록
                    Intent intent = new Intent(getApplicationContext(), AccountListActivity.class);
                    startActivityForResult(intent, 0);
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_pw_list) {    //하단 네비게이션 - 계정-pw 목록
                    Intent intent = new Intent(getApplicationContext(), PwListActivity.class);
                    startActivityForResult(intent, 0);
                    return true;
                }
                bottomNavigation.setSelectedItemId(R.id.page_activity_main);
                return false;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.page_activity_main);    //선택된 네비게이션 아이템을 메인 액티비티(현재 위치)로 변경
    }

    private void setCard() {    //카드 목록 설정
        layoutMain.removeAllViews();    //기존 목록 레이아웃 초기화
        setAccountList();   //계정 목록 가져옴
        inflater = LayoutInflater.from(this);
        accountList.forEach(accountDto -> {
            View itemCard = inflater.inflate(R.layout.item_card, layoutMain, false);    //카드 틀 item_card.xml를 카드 목록 레이아웃  layoutMain에 담음

            TextView cardAccountName = (TextView)itemCard.findViewById(R.id.cardAccountName);
            LinearLayout cardListLinkedAccount = (LinearLayout)itemCard.findViewById(R.id.cardListLinkedAccount);
            TextView cardUrl = (TextView)itemCard.findViewById(R.id.cardUrl);
            Button cardPw = (Button)itemCard.findViewById(R.id.cardPw);
            TextView cardLastChangedDate = (TextView)itemCard.findViewById(R.id.cardLastChangedDate);
            TextView cardChangeCycle = (TextView)itemCard.findViewById(R.id.cardChangeCycle);
            TextView cardMemo = (TextView)itemCard.findViewById(R.id.cardMemo);
            LinearLayout cardListWarning = (LinearLayout)itemCard.findViewById(R.id.cardListWarning);

            cardAccountName.setText(accountDto.getName());

            List<String> names = getLinkedAccountNames(accountDto.getId().toString());  //해당 계정과 연결된 계정들의 이름을 가져옴
            names.forEach(name -> {
                TextView tv = new TextView(this);
                tv.setText(name);
                cardListLinkedAccount.addView(tv);  //이름을 해당 카드의 연결된 계정 목록에 텍스트뷰로 추가
            });

            cardUrl.setText(accountDto.getUrl());

            cardPw.setOnClickListener(new View.OnClickListener() {  //pw 버튼 클릭 시
                private boolean isShow = false; //pw 보임 여부
                @Override
                public void onClick(View view) {
                    if(isShow) {
                        cardPw.setText("비밀번호 보기");
                        isShow = false;
                    } else {
                        String pw;
                        try {
                            pw = CryptoManager.decryptPw(accountDto.getPw(), accountDto.getIv());   //pw 복호화
                        } catch(Exception e) {
                            pw = "";
                        }
                        cardPw.setText(pw);
//                        cardPw.setText(accountDto.getPw());
                        isShow = true;
                    }
                }
            });

            cardLastChangedDate.setText(accountDto.getLastChangedDate());

            cardChangeCycle.setText(accountDto.getChangeCycle());

            cardMemo.setText(accountDto.getMemo());

            setWarning(accountDto, cardListWarning);    //경고 설정

            itemCard.setOnClickListener(new View.OnClickListener() {    //카드 클릭 시, 수정/삭제 작업
                @Override
                public void onClick(View view) {
                    Long id = accountDto.getId();
                    Intent intent = new Intent(getApplicationContext(), AddPwActivity.class);
                    intent.putExtra("isUpdate", true);  //수정/삭제 작업임
                    intent.putExtra("id", id);  //수정/삭제할 계정 id
                    startActivityForResult(intent, 1);
                }
            });

            layoutMain.addView(itemCard);
        });
    }

    private void setAccountList() { //계정 목록 가져옴
        accountList = new ArrayList<>();    //db의 account 테이블에서 가져온 계정 목록
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase(); //db 읽기 모드

        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_ACCOUNT;   //account 테이블의 모든 데이터
        Cursor cursor = readableDB.rawQuery(selectQuery, null); //조건 없음

        if(cursor != null) {
            while(cursor.moveToNext()) {
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_URL));
                String pw = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_PW));
                String lastChangedDate = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_LAST_CHANGED_DATE));
                String changeCycle = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_CHANGE_CYCLE));
                String memo = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_MEMO));
                String iv = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_IV));
                accountList.add(new AccountDto(id, name, url, pw, lastChangedDate, changeCycle, memo, iv));   //가져온 account 테이블의 모든 행 담음
            }
            cursor.close();
        }
        readableDB.close();
    }

    public List<String> getLinkedAccountNames(String accountId) {   //해당 계정과 연결된 계정들의 이름을 가져옴
        List<String> names = new ArrayList<>(); //계정 이름을 담을 리스트
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase();

        String query = "SELECT " + DBHelper.TABLE_ACCOUNT + "." + DBHelper.COL_NAME
                + " FROM " + DBHelper.TABLE_LINKED_ACCOUNT
                + " JOIN " + DBHelper.TABLE_ACCOUNT
                + " ON " + DBHelper.TABLE_LINKED_ACCOUNT + "." + DBHelper.COL_LINKED_ACCOUNT_ID
                + " = " + DBHelper.TABLE_ACCOUNT + "." + DBHelper.COL_ID
                + " WHERE " + DBHelper.TABLE_LINKED_ACCOUNT + "." + DBHelper.COL_ACCOUNT_ID
                + " = ?";
        // SELECT account.name FROM linked_account JOIN account ON linked_account.linked_account_id = account.id WHERE linked_account.account_id = ?

        Cursor cursor = readableDB.rawQuery(query, new String[]{accountId});

        if (cursor != null) {
            while(cursor.moveToNext()) {
                String name = cursor.getString(0);
                names.add(name);
            }
            cursor.close();
        }
        readableDB.close();

        return names;
    }

    private WarningDto getWarning(String accountId) {   //warning 테이블의 경고 가져옴
        WarningDto warningDto = null;
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase();
        String query = "SELECT * FROM " + DBHelper.TABLE_WARNING + " WHERE " + DBHelper.COL_ACCOUNT_ID + " = ?";
        Cursor cursor = readableDB.rawQuery(query, new String[] {accountId});
        if(cursor != null) {
            while(cursor.moveToNext()) {
                Integer urlWarning = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_URL_WARNING));
                Integer pwWarning = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PW_WARNING));
                warningDto = new WarningDto(Long.parseLong(accountId), urlWarning, pwWarning);
            }
            cursor.close();
        }
        readableDB.close();
        return warningDto;
    }

    private void setWarning(AccountDto accountDto, LinearLayout cardListWarning) {  //경고 설정
        WarningDto warningDto = getWarning(accountDto.getId().toString());
        if(warningDto != null) {
            if(warningDto.getUrlWarning() != null && !CheckUrl.checkUrl(warningDto.getUrlWarning()).equals("")) {   //url에 대한 경고 있음
                TextView tv = new TextView(this);
                tv.setText(CheckUrl.checkUrl(warningDto.getUrlWarning()));
                cardListWarning.addView(tv);
            }
            TextView tv = new TextView(this);
            tv.setText(CheckPw.checkPw(warningDto.getPwWarning()));
            cardListWarning.addView(tv);    //pw에 대한 경고 추가
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA); //날짜 형식
            Date lastChangedDate = dateFormat.parse(accountDto.getLastChangedDate());   //마지막 pw 변경일 날짜 문자열을 yyyy-MM-dd 날짜 형식대로 날짜로 바꿈
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastChangedDate);  //마지막 pw 변경일 날짜로 설정
            calendar.add(Calendar.MONDAY, Integer.parseInt(accountDto.getChangeCycle()));   //마지막 pw 변경일 + 변경 주기(개월)
            Date nextChangeDate = calendar.getTime();   //다음 pw 변경일
            Date today = new Date();    //현재 날짜
            if(today.equals(nextChangeDate) || today.after(nextChangeDate)) {   //pw 변경 주기일 시, pw 변경 주기 알림 추가
                TextView tv = new TextView(this);
                tv.setText("비밀번호 변경 주기");
                cardListWarning.addView(tv);
            }
        } catch(Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {   //pw 추가/수정/삭제 성공 시
            setCard();  //카드 목록 레이아웃 초기화
        } else if(resultCode == RESULT_CANCELED) {
        } else {
            Intent intent;
            switch(resultCode) {
                case 1: //page_activity_add_pw
                    intent = new Intent(getApplicationContext(), AddPwActivity.class);
                    intent.putExtra("isUpdate", false);
                    intent.putExtra("id", -1);
                    startActivityForResult(intent, 0);
                    break;
                case 2: //page_activity_account_list
                    intent = new Intent(getApplicationContext(), AccountListActivity.class);
                    startActivityForResult(intent, 0);
                    break;
                case 3: //page_activity_pw_list
                    intent = new Intent(getApplicationContext(), PwListActivity.class);
                    startActivityForResult(intent, 0);
                    break;
            }
        }
        bottomNavigation.setSelectedItemId(R.id.page_activity_main);
    }
}