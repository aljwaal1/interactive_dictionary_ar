package com.explapp.interactivedictionarylegacy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    private static class WordItem {
        final String en, ar, level;
        WordItem(String en, String ar, String level) { this.en=en; this.ar=ar; this.level=level; }
    }

    private final WordItem[] words = new WordItem[] {
        new WordItem("apple","تفاحة","KG"), new WordItem("book","كتاب","KG"), new WordItem("school","مدرسة","KG"),
        new WordItem("home","بيت","KG"), new WordItem("cat","قطة","KG"), new WordItem("dog","كلب","KG"),
        new WordItem("water","ماء","1"), new WordItem("sun","شمس","1"), new WordItem("moon","قمر","1"),
        new WordItem("chair","كرسي","1"), new WordItem("table","طاولة","1"), new WordItem("pen","قلم","1"),
        new WordItem("family","عائلة","2"), new WordItem("garden","حديقة","2"), new WordItem("market","سوق","2"),
        new WordItem("friend","صديق","2"), new WordItem("teacher","معلم","2"), new WordItem("window","نافذة","2"),
        new WordItem("weather","الطقس","3"), new WordItem("library","مكتبة","3"), new WordItem("computer","حاسوب","3"),
        new WordItem("language","لغة","3"), new WordItem("question","سؤال","3"), new WordItem("answer","إجابة","3"),
        new WordItem("science","علوم","4"), new WordItem("history","تاريخ","4"), new WordItem("country","دولة","4"),
        new WordItem("travel","سفر","4"), new WordItem("healthy","صحي","4"), new WordItem("useful","مفيد","4")
    };

    private LinearLayout listBox, quizOptions;
    private EditText search;
    private Spinner levelSpinner;
    private TextView flashEnglish, flashArabic, quizWord, score;
    private int flashIndex=0, quizIndex=0, correct=0, wrong=0;
    private boolean revealed=false;
    private final Random random = new Random();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("القاموس التفاعلي");
        showHome();
    }

    private TextView text(String value, int size, boolean bold) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(Color.rgb(30,41,59));
        v.setGravity(Gravity.RIGHT); v.setPadding(dp(10),dp(8),dp(10),dp(8));
        if (bold) v.setTypeface(null,1); return v;
    }

    private Button button(String title) {
        Button b=new Button(this); b.setText(title); b.setTextSize(17); b.setAllCaps(false); return b;
    }

    private LinearLayout root() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(14),dp(14),dp(14),dp(14)); root.setBackgroundColor(Color.rgb(240,253,244)); return root;
    }

    private void nav(LinearLayout root) {
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setGravity(Gravity.CENTER);
        String[] names={"الرئيسية","الكلمات","اختبار","عن"};
        for (int i=0;i<names.length;i++) { final int n=i; Button b=button(names[i]); b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ if(n==0)showHome(); else if(n==1)showList(); else if(n==2)showQuiz(); else showAbout(); }}); bar.addView(b,new LinearLayout.LayoutParams(0,-2,1)); }
        root.addView(bar);
    }

    private void showHome() {
        ScrollView s=new ScrollView(this); LinearLayout r=root(); s.addView(r);
        TextView title=text("القاموس التفاعلي — Android 4.4",27,true); title.setTextColor(Color.rgb(21,128,61)); r.addView(title);
        r.addView(text("30 كلمة • مستويات KG إلى 4 • يعمل دون إنترنت",17,false));
        r.addView(text("بطاقة سريعة",23,true));
        flashEnglish=text("",34,true); flashEnglish.setGravity(Gravity.CENTER); r.addView(flashEnglish);
        flashArabic=text("",28,true); flashArabic.setGravity(Gravity.CENTER); r.addView(flashArabic);
        Button reveal=button("إظهار / إخفاء المعنى"); reveal.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ revealed=!revealed; updateFlash(); }}); r.addView(reveal);
        Button next=button("الكلمة التالية"); next.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ flashIndex=(flashIndex+1)%words.length; revealed=false; updateFlash(); }}); r.addView(next);
        updateFlash(); nav(r); setContentView(s);
    }

    private void updateFlash() { WordItem w=words[flashIndex]; flashEnglish.setText(w.en); flashArabic.setText(revealed?w.ar:"اضغط لإظهار المعنى"); }

    private void showList() {
        LinearLayout r=root();
        r.addView(text("الكلمات",27,true));
        search=new EditText(this); search.setHint("بحث إنجليزي أو عربي"); search.setSingleLine(true); r.addView(search);
        levelSpinner=new Spinner(this); String[] levels={"الكل","KG","1","2","3","4"}; levelSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,levels)); r.addView(levelSpinner);
        ScrollView s=new ScrollView(this); listBox=new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL); s.addView(listBox); r.addView(s,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence x,int a,int b,int c){} public void onTextChanged(CharSequence x,int a,int b,int c){refreshList();} public void afterTextChanged(Editable e){}});
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){refreshList();} public void onNothingSelected(AdapterView<?> p){}});
        nav(r); setContentView(r); refreshList();
    }

    private void refreshList() {
        if(listBox==null)return; listBox.removeAllViews(); String q=search.getText().toString().trim().toLowerCase(); String level=String.valueOf(levelSpinner.getSelectedItem()); int count=0;
        for(WordItem w:words){ if(!"الكل".equals(level)&&!level.equals(w.level))continue; if(q.length()>0&&!w.en.toLowerCase().contains(q)&&!w.ar.contains(q)&&!w.level.contains(q))continue; TextView row=text(w.en+"  —  "+w.ar+"   ["+w.level+"]",20,true); row.setBackgroundColor(Color.WHITE); listBox.addView(row,new LinearLayout.LayoutParams(-1,-2)); count++; }
        if(count==0)listBox.addView(text("لا توجد نتائج",18,false));
    }

    private void showQuiz() {
        ScrollView s=new ScrollView(this); LinearLayout r=root(); s.addView(r); r.addView(text("اختبار سريع",27,true));
        quizWord=text("",36,true); quizWord.setGravity(Gravity.CENTER); r.addView(quizWord);
        quizOptions=new LinearLayout(this); quizOptions.setOrientation(LinearLayout.VERTICAL); r.addView(quizOptions);
        score=text("",20,true); score.setGravity(Gravity.CENTER); r.addView(score); buildQuiz(); nav(r); setContentView(s);
    }

    private void buildQuiz() {
        WordItem target=words[quizIndex%words.length]; quizWord.setText("ما معنى: "+target.en); quizOptions.removeAllViews();
        List<String> opts=new ArrayList<String>(); opts.add(target.ar); while(opts.size()<4){String a=words[random.nextInt(words.length)].ar;if(!opts.contains(a))opts.add(a);} Collections.shuffle(opts);
        for(final String o:opts){Button b=button(o); b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){WordItem current=words[quizIndex%words.length]; if(o.equals(current.ar)){correct++;Toast.makeText(MainActivity.this,"إجابة صحيحة",Toast.LENGTH_SHORT).show();}else{wrong++;Toast.makeText(MainActivity.this,"الإجابة: "+current.ar,Toast.LENGTH_SHORT).show();} quizIndex=(quizIndex+1)%words.length; buildQuiz();}}); quizOptions.addView(b);}
        score.setText("صحيح: "+correct+"   |   خطأ: "+wrong);
    }

    private void showAbout() {
        ScrollView s=new ScrollView(this); LinearLayout r=root(); s.addView(r); r.addView(text("عن التطبيق",27,true)); r.addView(text("نسخة مستقلة خفيفة للأجهزة القديمة. تحتوي على بحث، تصفية حسب الصف، بطاقات تعليمية واختبار سريع، وتعمل دون اتصال بالإنترنت.",19,false)); nav(r); setContentView(s);
    }

    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
}
