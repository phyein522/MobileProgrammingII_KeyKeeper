package com.cookandroid.keykeeper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class PwListActivity extends AppCompatActivity {
    LinearLayout layoutPwList;  //pw 목록이 추가될 레이아웃
    DBHelper dbHelper;
    List<AccountDto> accountList;   //계정 목록
    LayoutInflater inflater;
    NavigationBarView bottomNavigation; //하단 네비게이션

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pw_list);
        setTitle("KeyKeeper");

        Intent intent = getIntent();    //MainActivity에서 전달받은 인텐트
        dbHelper = new DBHelper(this);
        layoutPwList = (LinearLayout)findViewById(R.id.layoutPwList);
        setBottomNavigation();  //하단 네비게이션 설정
        setPwList();    //pw 목록 설정
    }

    private void setBottomNavigation() {    //하단 네비게이션 설정
        bottomNavigation = (NavigationBarView)findViewById(R.id.bottomNavigationPwList); //하단 네비게이션 id 찾음
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { //하단 네비게이션 아이템 설택 시 이벤트
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.page_activity_main) {   //하단 네비게이션 - 메인
                    setResult(RESULT_CANCELED); //추가/수정/삭제 작업을 중단하고 되돌아감
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_add_pw) { //하단 네비게이션 - pw 추가
                    setResult(1);
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_account_list) {   //하단 네비게이션 - 연결된 계정 목록
                    setResult(2);
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_pw_list) {    //하단 네비게이션 - 계정-pw 목록 (현재 위치)
                    return true;
                }
                bottomNavigation.setSelectedItemId(R.id.page_activity_pw_list);
                return false;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.page_activity_pw_list);  //선택된 네비게이션 아이템을 계정-pw 목록 액티비티로 변경
    }

    private void setAccountList() { //계정 목록 설정
        accountList = new ArrayList<>();    //db의 account 테이블에서 가져온 계정 목록
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase(); //db 읽기 모드

        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_ACCOUNT;   //account 테이블의 모든 데이터
        Cursor cursor = readableDB.rawQuery(selectQuery, null);

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

    private void setPwList() {  //pw 목록 설정
        setAccountList();
        inflater = LayoutInflater.from(this);
        accountList.forEach(accountDto -> {
            View itemList = inflater.inflate(R.layout.item_list, layoutPwList, false);  //계정-pw를 표시할 틀

            TextView listAccountName = (TextView)itemList.findViewById(R.id.listAccountName);
            listAccountName.setText(accountDto.getName());

            TextView listPw = (TextView)itemList.findViewById(R.id.listPw);
            listPw.setVisibility(View.GONE);    //기본적으로 pw 안보이게 함
            com.google.android.material.card.MaterialCardView listCard = (com.google.android.material.card.MaterialCardView)itemList.findViewById(R.id.listCard);
            listCard.setOnCheckedChangeListener(new MaterialCardView.OnCheckedChangeListener() {    //리스트의 체크 여부 변화 시
                @Override
                public void onCheckedChanged(MaterialCardView card, boolean isChecked) {
                    if(isChecked) { //리스트 체크 시, 비밀번호 표시
                        String pw;
                        try {
                            pw = CryptoManager.decryptPw(accountDto.getPw(), accountDto.getIv());
                        } catch(Exception e) {
                            pw = "";
                        }
                        listPw.setText(pw);
                        listPw.setVisibility(View.VISIBLE);
                    } else {    //리스트 언체크 시, 비밀번호 숨김
                        listPw.setText("");
                        listPw.setVisibility(View.GONE);
                    }
                }
            });
            itemList.setOnClickListener(new View.OnClickListener() {    //리스트 클릭 시, 체크 여부 반전
                @Override
                public void onClick(View view) {
                    listCard.setChecked(!listCard.isChecked());
                }
            });

            layoutPwList.addView(itemList);   //리스트 추가
        });
    }
}
