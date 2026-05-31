package com.cookandroid.keykeeper;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class AddPwActivity extends AppCompatActivity {
    NavigationBarView bottomNavigation; //하단 네비게이션
    Button btnAddPw, btnCheckUrl, btnCheckPw, btnUpdatePw, btnDeletePw;    //패스워드 추가하기, url 체크, pw 체크, 패스워드 수정하기, 패스워드 삭제하기
    DBHelper dbHelper;  //sqlite db helper
    EditText accountName, url, pw, changeCycle, memo;    //계정 이름, url, pw, 변경 주기, 메모
    TextView resultCheckUrl, resultCheckPw; //url 검사 결과, pw 검사 결과
    DatePicker lastChangedDate; //마지막 pw 변경일
    LinearLayout listLinkedAccount;   //연결된 계정 목록
    AccountDto account; //현재 입력한 계정 정보
    List<AccountDto> accountList; //계정 목록 정보
    boolean isUpdate;   //수정/삭제를 위함인지
    long cardId;    //수정/삭제를 위해 전달받은 id
    GridLayout layoutBtnUpdateDeletePw; //수정/삭제 버튼이 있는 그리드 레이아웃
    CheckUrl checkUrl;  //url 검사 클래스

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pw);

        Intent intent = getIntent();    //MainActivity에서 전달받은 인텐트
        isUpdate = intent.getBooleanExtra("isUpdate", false);   //수정/삭제할 것인지에 대한 여부
        cardId = intent.getLongExtra("id", -1); //수정/삭제할 id 전달받음

        setBottomNavigation();  //하단 네비게이션 설정
        setInput(); //변수 대입
    }

    private void setBottomNavigation() {    //하단 네비게이션 설정
        bottomNavigation = (NavigationBarView)findViewById(R.id.bottomNavigationAddPw); //하단 네비게이션 id 찾음
        bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { //하단 네비게이션 아이템 설택 시 이벤트
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.page_activity_main) {   //하단 네비게이션 - 메인
                    setResult(RESULT_CANCELED); //추가/수정/삭제 작업을 중단하고 되돌아감
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_add_pw) { //하단 네비게이션 - pw 추가 (현재 위치)
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_account_list) {   //하단 네비게이션 - 연결된 계정 목록
                    setResult(2);
                    finish();
                    return true;
                }
                if(item.getItemId() == R.id.page_activity_pw_list) {    //하단 네비게이션 - 계정-pw 목록
                    setResult(3);
                    finish();
                    return true;
                }
                bottomNavigation.setSelectedItemId(R.id.page_activity_add_pw);
                return false;
            }
        });
        bottomNavigation.setSelectedItemId(R.id.page_activity_add_pw);  //선택된 네비게이션 아이템을 비밀번호 추가 액티비티로 변경
    }

    private void setInput() {
        dbHelper = new DBHelper(this);  //dbHelper

        accountName = (EditText)findViewById(R.id.accountName); //입력한 이름

        listLinkedAccount = (LinearLayout)findViewById(R.id.listLinkedAccount); //연동할 계정 목록이 표시될 리니어 레이아웃

        url = (EditText)findViewById(R.id.url); //입력할 url
        btnCheckUrl = (Button)findViewById(R.id.btnCheckUrl);   //url 검사 버튼
        resultCheckUrl = (TextView)findViewById(R.id.resultCheckUrl);   //url 검사 결과 표시 텍스트뷰
        resultCheckUrl.setVisibility(View.GONE);    //url 검사 결과 표시 텍스트뷰 표시 안함
        btnCheckUrl.setOnClickListener(new View.OnClickListener() { //url 검사 버튼 클릭 시
            @Override
            public void onClick(View view) {
                if(url.getText().toString().isEmpty()) {    //url 입력 창에 미입력 시
                    showToast("URL을 입력하세요");
                    return;
                }
                resultCheckUrl.setText(CheckUrl.checkUrl(checkUrl.checkUrlScore(url.getText().toString())));
                if(!resultCheckUrl.getText().toString().isEmpty()) {    //url 검사 결과가 빈 문자열이 아닐 시
                    resultCheckUrl.setVisibility(View.VISIBLE); //url 검사 결과 표시
                }
            }
        });

        pw = (EditText)findViewById(R.id.pw);   //입력할 pw
        btnCheckPw = (Button)findViewById(R.id.btnCheckPw); //pw 검사 버튼
        resultCheckPw = (TextView)findViewById(R.id.resultCheckPw); //pw 검사 결과 표시 텍스트뷰
        resultCheckPw.setVisibility(View.GONE); //pw 검사 결과 표시 텍스트뷰 표시 안함
        btnCheckPw.setOnClickListener(new View.OnClickListener() {  //pw 검사 버튼 클릭 시
            @Override
            public void onClick(View view) {
                if(pw.getText().toString().isEmpty()) { //pw 입력 창에 미입력 시
                    showToast("비밀번호를 입력하세요");
                    return;
                }
                resultCheckPw.setText(CheckPw.checkPw(pw.getText().toString()));    //pw 검사 결과 문자열 설정
                resultCheckPw.setVisibility(View.VISIBLE);  //pw 검사 결과 표시
            }
        });

        lastChangedDate = (DatePicker)findViewById(R.id.lastChangedDate);   //마지막 pw 변경일 (기본: pw 추가한 날짜)
        changeCycle = (EditText)findViewById(R.id.changeCycle); //pw 변경 주기 (기본: 3)

        memo = (EditText)findViewById(R.id.memo);   //입력할 메모

        btnAddPw = (Button)findViewById(R.id.btnAddPw); //pw 추가 버튼
        layoutBtnUpdateDeletePw = (GridLayout)findViewById(R.id.layoutBtnUpdateDeletePw);   //pw 수정/삭제 버튼이 있는 그리드 레이아웃
        btnUpdatePw = (Button)findViewById(R.id.btnUpdatePw);   //pw 수정 버튼
        btnDeletePw = (Button)findViewById(R.id.btnDeletePw);   //pw 삭제 버튼

        if(isUpdate) {  //pw 수정/삭제를 위함
            btnAddPw.setVisibility(View.GONE);  //pw 추가 버튼 안보이게
            layoutBtnUpdateDeletePw.setVisibility(View.VISIBLE);    //pw 수정/삭제 버튼이 있는 그리드 레이아웃 보이게
            setAccount();   //update or delete
            setWarning();   //저장된 경고 가져옴
        } else {    //pw 추가를 위함
            btnAddPw.setVisibility(View.VISIBLE);   //pw 추가 버튼 보이게
            layoutBtnUpdateDeletePw.setVisibility(View.GONE);   //pw 수정/삭제 버튼이 있는 그리드 레이아웃 안보이게
        }

        setAccountList();   //db의 account 테이블에서 연동 가능한 계정 목록 가져옴

        btnAddPw.setOnClickListener(new View.OnClickListener() {    //pw 추가 버튼 클릭 시
            @Override
            public void onClick(View view) {
                boolean isInsertAccount = insertAccount();    //account 테이블에 계정 추가 + linked_account 테이블에 연동된 계정 목록 추가 + warning 테이블에 경고 추가
                if(isInsertAccount) {   //모든 insert 성공 시
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        btnUpdatePw.setOnClickListener(new View.OnClickListener() { //pw 수정 버튼 클릭 시
            @Override
            public void onClick(View view) {
                boolean isUpdateAccount = updateAccount();    //account 테이블의 계정 정보 수정 + linked_account 테이블에 연동된 계정 목록 추가/삭제
                if(isUpdateAccount) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });

        btnDeletePw.setOnClickListener(new View.OnClickListener() { //pw 삭제 버튼 클릭 시
            @Override
            public void onClick(View view) {
                deleteAccount();    //account 테이블의 계정 삭제 -> linked_account 테이블의 동일 id 값 가진 행 삭제
                setResult(RESULT_OK);
                finish();
            }
        });

        checkUrl = new CheckUrl(this);  //url 체크 클래스
        checkUrl.insertCsvOnlyFirstTime();  //피싱 url 목록을 db에 저장 (최초 한 번만)
    }

    private String getLastChangedDate() {   //마지막 pw 변경일 -> 문자열로 반환
        Integer year = lastChangedDate.getYear();   //년
        Integer month = lastChangedDate.getMonth() + 1; //월
        Integer dayOfMonth = lastChangedDate.getDayOfMonth();   //일

        String lastChangedDate = new StringBuilder()
                .append(year).append("-").append(month).append("-").append(dayOfMonth)
                .toString();    //년-월-일 형태

        return lastChangedDate;
    }

    private boolean insertAccount() {   //계정 insert
        if(accountName.getText().toString().isEmpty()) {    //계정 이름 미입력 시
            showToast("계정 이름을 입력하세요");
            return false;
        }
        if(pw.getText().toString().isEmpty()) { //pw 미입력 시
            showToast("비밀번호를 입력하세요");
            return false;
        }

        account = new AccountDto(); //입력한 계정 정보 담을 AccountDto
        account.setName(accountName.getText().toString());  //입력한 이름
        account.setUrl(url.getText().toString());   //입력한 url
        //account.setPw(pw.getText().toString()); //입력한 pw
        account.setLastChangedDate(getLastChangedDate());   //입력한 마지막 pw 변경일
//        account.setChangeCycle(changeCycle.getText().toString().isEmpty() ? "3" : changeCycle.getText().toString());  //입력한 pw 변경 주기
        if(changeCycle.getText().toString().trim().isEmpty()) { //pw 변경 주기 미입력 시
            account.setChangeCycle("3");    //기본값 3
        } else {    //pw 변경 주기 입력 시
            try {
                Integer.parseInt(changeCycle.getText().toString());
            } catch (NumberFormatException e) { //입력한 pw 변경 주기가 숫자만 입력되지 않음 (or Integer 범위 외)
                showToast("변경 주기는 숫자만 입력해주세요");
                return false;
            }
            account.setChangeCycle(changeCycle.getText().toString().trim());    //입력한 pw 변경 주기
        }
        account.setMemo(memo.getText().toString()); //입력한 memo
        EncryptResultDto encryptResultDto;  //암호화 결과 pw, iv 담는 객체
        try {
            encryptResultDto = CryptoManager.encryptPw(pw.getText().toString());    //암호화된 pw, iv 담음
        } catch(Exception e) {
            System.out.println(e.getMessage());
            showToast("pw 암호화 오류");
            return false;
        }
        account.setPw(encryptResultDto.getPw());   //암호화된 pw
        account.setIv(encryptResultDto.getIv());    //iv

        SQLiteDatabase writableDB = dbHelper.getWritableDatabase(); //쓰기 모드
        ContentValues values = new ContentValues(); //삽입 값 목록 설정
        values.put(DBHelper.COL_NAME, account.getName());  //입력한 이름
        values.put(DBHelper.COL_URL, account.getUrl());    //입력한 url
        values.put(DBHelper.COL_PW, account.getPw());  //암호화된 pw
        values.put(DBHelper.COL_LAST_CHANGED_DATE, account.getLastChangedDate());  //입력한 마지막 pw 변경일
        values.put(DBHelper.COL_CHANGE_CYCLE, account.getChangeCycle());   //입력한 pw 변경 주기
        values.put(DBHelper.COL_MEMO, account.getMemo());  //입력한 memo
        values.put(DBHelper.COL_IV, account.getIv());   //iv

        Long newId = writableDB.insert(DBHelper.TABLE_ACCOUNT, null, values);    //새 pw 정보 추가

        writableDB.close();

        if (newId != -1) {  //새 pw 정보 추가 성공
            account.setId(newId);   //추가한 새 pw 정보의 id 저장
        } else {
            showToast("비밀번호 추가 실패");
            return false;
        }

        boolean isInsertLinkedAccount = insertLinkedAccount();  //연결된 계정 목록을 account_list 테이블에 추가
        boolean isInsertWarning = insertWarning();    //경고를 warning 테이블에 저장

        if(isInsertLinkedAccount && isInsertWarning) {  //연결된 계정 목록 추가 성공, 경고 추가 설공
            return true;
        } else {    //연결된 계정 목록 추가 실패 혹은 경고 추가 실패
            deleteAccount();    //추가했던 계정 삭제
            account = null;
        }
        return false;
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private boolean isLinkedAccount(String accountId, String linkedAccountId) { //이미 연결된 계정인지에 대한 여부
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase(); //읽기 모드

        Cursor cursor = readableDB.query(DBHelper.TABLE_LINKED_ACCOUNT   //linked_account 테이블
                , new String[] {"count(*)"} //이미 연결된 계정인지 셈
                , DBHelper.COL_ACCOUNT_ID + " = ? AND " + DBHelper.COL_LINKED_ACCOUNT_ID + " = ?"    //조건: 자신의 id, 이미 연결된 계정인지 검사할 계정의 id
                , new String[] {accountId, linkedAccountId}
                , null, null, null
        );
        int count = 0;
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                count = cursor.getInt(0);   //이미 연결된 계정인지 셈
            }
            cursor.close();
        }
        readableDB.close();
        if(count > 0) { //1개 이상 -> 이미 연결된 계정임
            return true;
        }

        return false;   //연결된 계정이 아님
    }

    private void setAccountList() { //연결 가능 or 연결된 계정 목록 설정
        accountList = new ArrayList<>();    //db의 account 테이블에서 가져온 연결 가능한 계정 목록
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase(); //읽기 모드

        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_ACCOUNT;   //account 테이블의 모든 데이터
        Cursor cursor = readableDB.rawQuery(selectQuery, null);

        if(cursor != null) {
            while(cursor.moveToNext()) {
                AccountDto accountDto = new AccountDto();
                Long id = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ID));   //id
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DBHelper.COL_NAME));   //이름
                accountDto.setId(id);
                accountDto.setName(name);
                if(isUpdate) {  //수정/삭제를 위한 작업일 시
                    if(accountDto.getId() == account.getId()) { //자기 자신일 시 담지 않음
                        continue;
                    }
                }
                accountList.add(accountDto);   //가져온 account 테이블의 id와 name 담음
            }
            cursor.close();
        }
        readableDB.close();

        accountList.forEach(accountDto -> { //가져온 account의 id와 name으로 체크박스 생성 (자기 자신 제외)
            CheckBox cb = new CheckBox(this);
            cb.setText(accountDto.getName());   //account 테이블의 name을 체크박스의 텍스트로 지정
            if(isUpdate) {  //수정을 위함이라면, 이미 연결된 계정인지 확인 -> 이미 연결된 계정이라면 체크
                cb.setChecked(isLinkedAccount(account.getId().toString(), accountDto.getId().toString()));
            }
            listLinkedAccount.addView(cb);  //연결된 계정 목록에 체크박스 추가
        });
    }

    private boolean insertLinkedAccount() {    //연결된 계정 목록을 linked_account 테이블에 추가
        SQLiteDatabase writableDB = dbHelper.getWritableDatabase(); //쓰기 모드

        for(int i = 0; i < listLinkedAccount.getChildCount(); i++) {    //연결 가능한 계정 목록의 계정 개수만큼 반복
            View child = listLinkedAccount.getChildAt(i);

            if(child instanceof  CheckBox) {
                CheckBox cb = (CheckBox)child;

                if(isUpdate) {  //수정/삭제를 위함이라면
                    //linked_account 테이블에 있는가
                    if(isLinkedAccount(account.getId().toString(), accountList.get(i).getId().toString())) {
                        //언체크인가? -> delete
                        if(!cb.isChecked()) {
                            int isDeleted = writableDB.delete(
                                    DBHelper.TABLE_LINKED_ACCOUNT
                                    , DBHelper.COL_ACCOUNT_ID + " = ? AND " + DBHelper.COL_LINKED_ACCOUNT_ID + " = ?"
                                    , new String[] {account.getId().toString()
                                            , accountList.get(i).getId().toString()}
                            );
                            if(isDeleted < 1) {
                                showToast("연결된 계정의 아무 행도 삭제되지 않음");
                                writableDB.close();
                                return false;
                            }
                        }
                        continue;
                    }
                }
                //isUdate 아님
                //linked_account 테이블에 없음
                //체크인가? -> insert
                if(cb.isChecked()) {
                    //insert
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.COL_ACCOUNT_ID, account.getId());
                    values.put(DBHelper.COL_LINKED_ACCOUNT_ID, accountList.get(i).getId());

                    Long newId = writableDB.insert(DBHelper.TABLE_LINKED_ACCOUNT, null, values);

                    if (newId == -1) {
                        showToast("연결된 계정 추가 실패");
                        writableDB.close();
                        return false;
                    }
                }
            }
        }

        writableDB.close();

        return true;
    }

    private void setAccount() { //pw 수정/삭제를 위함일 시, 전달받은 id를 이용해서 해당 pw 정보 가져옴
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase(); //읽기 모드

        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_ACCOUNT + " WHERE " + DBHelper.COL_ID + " = ?";
        Cursor cursor = readableDB.rawQuery(selectQuery, new String[] {Long.toString(cardId)});

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
                account = new AccountDto(id, name, url, pw, lastChangedDate, changeCycle, memo, iv);   //수정할 account 정보 가져옴
            }
            cursor.close();
        }
        readableDB.close();

        accountName.setText(account.getName());
        url.setText(account.getUrl());
//        pw.setText(account.getPw());
        try {
            pw.setText(CryptoManager.decryptPw(account.getPw(), account.getIv()));
        } catch(Exception e) {
            showToast("pw 복호화 오류");
            pw.setText("");
        }
        String[] strLastChangedDate = account.getLastChangedDate().split("-");
        lastChangedDate.init(Integer.parseInt(strLastChangedDate[0]), Integer.parseInt(strLastChangedDate[1]) - 1, Integer.parseInt(strLastChangedDate[2]), null);
        changeCycle.setText(account.getChangeCycle());
        memo.setText(account.getMemo());
    }

    private boolean updateAccount() {  //pw 수정
        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_NAME, accountName.getText().toString());
        values.put(DBHelper.COL_URL, url.getText().toString());
//        values.put(DBHelper.COL_PW, pw.getText().toString());
        values.put(DBHelper.COL_LAST_CHANGED_DATE, getLastChangedDate());
        values.put(DBHelper.COL_CHANGE_CYCLE, changeCycle.getText().toString());
        values.put(DBHelper.COL_MEMO, memo.getText().toString());
        EncryptResultDto encryptResultDto;
        try {
            encryptResultDto = CryptoManager.encryptPw(pw.getText().toString());
        } catch(Exception e) {
            showToast("pw 암호화 오류");
            return false;
        }
        values.put(DBHelper.COL_PW, encryptResultDto.getPw());
        values.put(DBHelper.COL_IV, encryptResultDto.getIv());

        SQLiteDatabase writableDB = dbHelper.getWritableDatabase(); //쓰기 모드
        int isUpdated = writableDB.update(
                DBHelper.TABLE_ACCOUNT   // 테이블명
                , values    // 변경할 값
                , DBHelper.COL_ID + " = ?"  // 조건
                , new String[] {Long.toString(account.getId())} // 조건 값
        );
        writableDB.close();

        if (isUpdated < 1) {
            showToast("update 실패");
            return false;
        }

        boolean isInsertLinkedAccount = insertLinkedAccount();
        boolean isUpdateWarning = updateWarning();

        if(isInsertLinkedAccount && isUpdateWarning) {
            return true;
        }
        return true;
    }

    private void deleteAccount() {  //pw 삭제
        SQLiteDatabase writableDB = dbHelper.getWritableDatabase(); //쓰기 모드
        int isDeleted = writableDB.delete(
                DBHelper.TABLE_ACCOUNT
                , DBHelper.COL_ID + " = ?"
                , new String[] {account.getId().toString()} //acount 변수에 저장된 id 가져옴
        );
        if(isDeleted < 1) {
            showToast("계정이 삭제되지 않음");
        } else {
            showToast("계정이 삭제됨");
        }
    }

    private boolean insertWarning() {   //경고 추가
        Integer urlWarning = null;  //url 미입력 시 null
        String inputUrl = url.getText().toString(); //입력된 url 가져옴
        if(!inputUrl.isEmpty()) {   //url 입력 시
            urlWarning = checkUrl.checkUrlScore(inputUrl);  //url 검사 결과 점수
        }
        Integer pwWarning = CheckPw.checkPwScore(pw.getText().toString());  //pw 검사 결과 점수
        Long accountId = account.getId();
        WarningDto warningDto = new WarningDto(accountId, urlWarning, pwWarning);

        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_ACCOUNT_ID, warningDto.getAccountId());
        values.put(DBHelper.COL_URL_WARNING, warningDto.getUrlWarning());
        values.put(DBHelper.COL_PW_WARNING, warningDto.getPwWarning());
        SQLiteDatabase writableDB = dbHelper.getWritableDatabase();
        Long insert = writableDB.insert(DBHelper.TABLE_WARNING, null, values);  //경고 추가
        writableDB.close();
        
        if(insert == -1) {
            showToast("경고 추가 실패");
            return false;
        }
        return true;
    }

    private void setWarning() { //경고 설정
        SQLiteDatabase readableDB = dbHelper.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + DBHelper.TABLE_WARNING + " WHERE " + DBHelper.COL_ACCOUNT_ID + " = ?";
        Cursor cursor = readableDB.rawQuery(selectQuery, new String[] {Long.toString(cardId)});

        WarningDto warningDto = new WarningDto();

        if(cursor != null) {
            while(cursor.moveToNext()) {
                Long accountId = cursor.getLong(cursor.getColumnIndexOrThrow(DBHelper.COL_ACCOUNT_ID));
                Integer urlWarning = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_URL_WARNING));
                Integer pwWarning = cursor.getInt(cursor.getColumnIndexOrThrow(DBHelper.COL_PW_WARNING));
                warningDto = new WarningDto(accountId, urlWarning, pwWarning);
            }
            cursor.close();
        }
        readableDB.close();

        if(warningDto.getUrlWarning() != null && !CheckUrl.checkUrl(warningDto.getUrlWarning()).isEmpty()) {    //url 경고가 null이 아니고, ""가 아님
            resultCheckUrl.setText(CheckUrl.checkUrl(warningDto.getUrlWarning()));  //url 경고 문자열 설정
            resultCheckUrl.setVisibility(View.VISIBLE); //url 경고 보임
        } else {
            resultCheckUrl.setVisibility(View.GONE);
        }
        resultCheckPw.setText(CheckPw.checkPw(warningDto.getPwWarning()));  //pw 경고 문자열 설정
        resultCheckPw.setVisibility(View.VISIBLE);  //pw 경고 보임
    }

    private boolean updateWarning() {   //경고 수정
        Integer urlWarning = null;
        String inputUrl = url.getText().toString();
        if(!inputUrl.isEmpty()) {
            urlWarning = checkUrl.checkUrlScore(inputUrl);
        }
        Integer pwWarning = CheckPw.checkPwScore(pw.getText().toString());
        Long accountId = account.getId();
        WarningDto warningDto = new WarningDto(accountId, urlWarning, pwWarning);

        ContentValues values = new ContentValues();
        values.put(DBHelper.COL_URL_WARNING, warningDto.getUrlWarning());
        values.put(DBHelper.COL_PW_WARNING, warningDto.getPwWarning());

        SQLiteDatabase writableDB = dbHelper.getWritableDatabase();
        int isUpdated = writableDB.update(
                DBHelper.TABLE_WARNING  //테이블명
                , values    //변경할 값
                , DBHelper.COL_ACCOUNT_ID + " = ?"  //조건
                , new String[] {Long.toString(warningDto.getAccountId())}   //조건 값
        );
        writableDB.close();
        
        if(isUpdated < 1) {
            showToast("warning update 실패");
            return false;
        }

        return true;
    }
}
