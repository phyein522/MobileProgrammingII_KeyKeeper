package com.cookandroid.keykeeper;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AccountListActivity extends AppCompatActivity {
    LinearLayout layoutAccountList; //계정 목록이 추가될 레이아웃
    DBHelper dbHelper;
    List<AccountDto> accountList;   //계정 목록
    LayoutInflater inflater;
    NavigationBarView bottomNavigation; //하단 네비게이션

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_list);
        setTitle("KeyKeeper");

        Intent intent = getIntent();    //MainActivity에서 전달받은 인텐트
        setBottomNavigation();  //하단 네비게이션 설정
        dbHelper = new DBHelper(this);
        layoutAccountList = (LinearLayout)findViewById(R.id.layoutAccountList); //계정-pw 리스트를 추가할 레이아웃
        setLinkedAccountList(); //연결된 계정 목록 설정
    }

    private void setBottomNavigation() {    //하단 네비게이션 설정
        bottomNavigation = (NavigationBarView)findViewById(R.id.bottomNavigationAccountList); //하단 네비게이션 id 찾음
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { //하단 네비게이션 아이템 설택 시 이벤트
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.page_activity_main) {   //하단 네비게이션 - 메인
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_add_pw) { //하단 네비게이션 - pw 추가
                    setResult(1);
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_account_list) {   //하단 네비게이션 - 연결된 계정 목록 (현재 위치)
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_pw_list) {    //하단 네비게이션 - 계정-pw 목록
                    setResult(3);
                    finish();
                    return true;
                }
                bottomNavigation.setSelectedItemId(R.id.page_activity_account_list);
                return false;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.page_activity_account_list);  //선택된 네비게이션 아이템을 연결된 계정 목록 액티비티로 변경
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

    private void setLinkedAccountList() {   //연결된 계정 목록 설정
        setAccountList();   //계정 목록 설정
        inflater = LayoutInflater.from(this);
        accountList.forEach(accountDto -> {
            View itemLinkedAccount = inflater.inflate(R.layout.item_linked_account, layoutAccountList, false);  //계정-연결된 계정을 표시할 틀

            TextView linkedAccountName = (TextView)itemLinkedAccount.findViewById(R.id.linkedAccountName);
            linkedAccountName.setText(accountDto.getName());

            LinearLayout linkedAccountList = (LinearLayout)itemLinkedAccount.findViewById(R.id.linkedAccountList);  //연결된 계정 목록을 추가할 레이아웃

            List<String> names = getLinkedAccountNames(accountDto.getId().toString());  //해당 계정과 연결된 계정 목록 가져옴
            names.forEach(name -> {
                TextView tv = new TextView(this);
                tv.setText(name);
                linkedAccountList.addView(tv);  //연결된 계정 목록에 이름 추가
            });

            layoutAccountList.addView(itemLinkedAccount);   //리스트 추가
        });
    }

    public List<String> getLinkedAccountNames(String accountId) {   //계정과 연결된 계정 이름 가져옴
        List<String> names = new ArrayList<>();
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
}
