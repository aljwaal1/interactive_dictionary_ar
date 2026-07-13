package com.explapp.interactivedictionarylegacy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
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
    private static final int GREEN=Color.rgb(21,128,61), MINT=Color.rgb(220,252,231), INK=Color.rgb(30,41,59), BG=Color.rgb(244,248,246);
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
    private TextView flashEnglish, flashArabic, quizWord, score, flashProgress;
    private Button masteredButton;
    private int flashIndex=0, quizIndex=0, correct=0, wrong=0;
    private boolean revealed=false;
    private final Random random = new Random();
    private SharedPreferences prefs;
    private TextToSpeech tts;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setTitle("القاموس التفاعلي");
        prefs=getSharedPreferences("dictionary_score",MODE_PRIVATE); correct=prefs.getInt("correct",0); wrong=prefs.getInt("wrong",0); flashIndex=prefs.getInt("flash_index",0)%words.length; quizIndex=prefs.getInt("quiz_index",0)%words.length;
        tts=new TextToSpeech(this,new TextToSpeech.OnInitListener(){public void onInit(int status){if(status==TextToSpeech.SUCCESS)tts.setLanguage(Locale.US);}});
        showHome();
    }

    private TextView text(String value, int size, boolean bold) {
        TextView v=new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(INK);
        v.setGravity(Gravity.RIGHT); v.setPadding(dp(10),dp(8),dp(10),dp(8));
        if (bold) v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); return v;
    }

    private Button button(String title) {
        Button b=new Button(this); b.setText(title); b.setTextSize(16); b.setTextColor(Color.WHITE); b.setTypeface(Typeface.DEFAULT,Typeface.BOLD); b.setAllCaps(false); b.setMinHeight(dp(52)); b.setBackground(round(GREEN,16)); return b;
    }

    private LinearLayout root() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(14),dp(14),dp(14),dp(14)); root.setBackgroundColor(BG); return root;
    }

    private void nav(LinearLayout root) {
        LinearLayout bar=new LinearLayout(this); bar.setOrientation(LinearLayout.HORIZONTAL); bar.setGravity(Gravity.CENTER);
        String[] names={"الرئيسية","الكلمات","اختبار","عن"};
        for (int i=0;i<names.length;i++) { final int n=i; Button b=new Button(this); b.setText(names[i]); b.setTextSize(13); b.setTextColor(GREEN); b.setAllCaps(false); b.setBackgroundColor(Color.TRANSPARENT); b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ if(n==0)showHome(); else if(n==1)showList(); else if(n==2)showQuiz(); else showAbout(); }}); bar.addView(b,new LinearLayout.LayoutParams(0,dp(48),1)); }
        bar.setBackground(round(Color.WHITE,18));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.topMargin=dp(14);
        root.addView(bar,p);
    }

    private void showHome() {
        ScrollView s=new ScrollView(this); LinearLayout r=root(); s.addView(r);
        LinearLayout hero=new LinearLayout(this); hero.setOrientation(LinearLayout.VERTICAL); hero.setPadding(dp(18),dp(18),dp(18),dp(18)); hero.setBackground(round(GREEN,24));
        TextView title=text("القاموس التفاعلي",27,true); title.setTextColor(Color.WHITE); hero.addView(title);
        TextView intro=text("30 كلمة • KG إلى 4 • يعمل دون إنترنت",15,false); intro.setTextColor(Color.rgb(220,252,231)); hero.addView(intro); r.addView(hero);
        TextView mastered=text("أتقنت " + masteredCount() + " من " + words.length + " كلمة",16,true); mastered.setTextColor(GREEN); mastered.setGravity(Gravity.CENTER); r.addView(mastered,margin(-1,-2,12));
        r.addView(text("بطاقة اليوم",23,true));
        LinearLayout flashCard=new LinearLayout(this); flashCard.setOrientation(LinearLayout.VERTICAL); flashCard.setPadding(dp(18),dp(24),dp(18),dp(18)); flashCard.setBackground(round(Color.WHITE,22));
        flashEnglish=text("",34,true); flashEnglish.setGravity(Gravity.CENTER);
        flashArabic=text("",28,true); flashArabic.setGravity(Gravity.CENTER);
        flashProgress=text("",13,false); flashProgress.setGravity(Gravity.CENTER); flashCard.addView(flashProgress);
        flashCard.addView(flashEnglish); flashCard.addView(flashArabic);
        Button reveal=button("إظهار / إخفاء المعنى"); reveal.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ revealed=!revealed; updateFlash(); }});
        flashCard.addView(reveal,margin(-1,dp(52),14));
        LinearLayout actions=new LinearLayout(this); actions.setOrientation(LinearLayout.HORIZONTAL);
        Button voice=button("استمع"); voice.setOnClickListener(new View.OnClickListener(){public void onClick(View v){speak(words[flashIndex].en);}}); actions.addView(voice,new LinearLayout.LayoutParams(0,dp(52),1));
        masteredButton=button("أتقنتها"); masteredButton.setOnClickListener(new View.OnClickListener(){public void onClick(View v){toggleMastered();}}); LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(0,dp(52),1); mp.leftMargin=dp(8); actions.addView(masteredButton,mp); flashCard.addView(actions);
        Button next=button("الكلمة التالية"); next.setOnClickListener(new View.OnClickListener(){public void onClick(View v){ flashIndex=(flashIndex+1)%words.length; prefs.edit().putInt("flash_index",flashIndex).apply(); revealed=false; updateFlash(); }}); flashCard.addView(next,margin(-1,dp(52),8));
        r.addView(flashCard,margin(-1,-2,8));
        updateFlash(); nav(r); setContentView(s);
    }

    private void updateFlash() { WordItem w=words[flashIndex]; flashEnglish.setText(w.en); flashArabic.setText(revealed?w.ar:"اضغط لإظهار المعنى"); if(flashProgress!=null)flashProgress.setText("الكلمة "+(flashIndex+1)+" من "+words.length+" • المستوى "+w.level); if(masteredButton!=null)masteredButton.setText(isMastered(w)?"تم إتقانها ✓":"أتقنتها"); }
    private boolean isMastered(WordItem w){return prefs.getBoolean("mastered_"+w.en,false);}
    private int masteredCount(){int n=0;for(WordItem w:words)if(isMastered(w))n++;return n;}
    private void toggleMastered(){WordItem w=words[flashIndex];prefs.edit().putBoolean("mastered_"+w.en,!isMastered(w)).apply();showHome();}

    private void showList() {
        LinearLayout r=root();
        r.addView(text("الكلمات",27,true));
        search=new EditText(this); search.setHint("بحث إنجليزي أو عربي"); search.setSingleLine(true); search.setPadding(dp(14),dp(8),dp(14),dp(8)); search.setBackground(round(Color.WHITE,16)); r.addView(search,margin(-1,dp(54),8));
        levelSpinner=new Spinner(this); String[] levels={"الكل","KG","1","2","3","4"}; levelSpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,levels)); r.addView(levelSpinner);
        ScrollView s=new ScrollView(this); listBox=new LinearLayout(this); listBox.setOrientation(LinearLayout.VERTICAL); s.addView(listBox); r.addView(s,new LinearLayout.LayoutParams(-1,0,1));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence x,int a,int b,int c){} public void onTextChanged(CharSequence x,int a,int b,int c){refreshList();} public void afterTextChanged(Editable e){}});
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){refreshList();} public void onNothingSelected(AdapterView<?> p){}});
        nav(r); setContentView(r); refreshList();
    }

    private void refreshList() {
        if(listBox==null)return; listBox.removeAllViews(); String q=search.getText().toString().trim().toLowerCase(); String level=String.valueOf(levelSpinner.getSelectedItem()); int count=0;
        for(WordItem w:words){ if(!"الكل".equals(level)&&!level.equals(w.level))continue; if(q.length()>0&&!w.en.toLowerCase().contains(q)&&!w.ar.contains(q)&&!w.level.contains(q))continue; TextView row=text(w.en+"  —  "+w.ar+"   ["+w.level+"]"+(isMastered(w)?"  ✓":""),19,true); row.setBackground(round(Color.WHITE,16)); listBox.addView(row,margin(-1,-2,6)); count++; }
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
        for(final String o:opts){Button b=button(o); b.setOnClickListener(new View.OnClickListener(){public void onClick(View v){WordItem current=words[quizIndex%words.length]; if(o.equals(current.ar)){correct++;Toast.makeText(MainActivity.this,"إجابة صحيحة",Toast.LENGTH_SHORT).show();}else{wrong++;Toast.makeText(MainActivity.this,"الإجابة: "+current.ar,Toast.LENGTH_SHORT).show();} quizIndex=(quizIndex+1)%words.length; prefs.edit().putInt("correct",correct).putInt("wrong",wrong).putInt("quiz_index",quizIndex).apply(); buildQuiz();}}); quizOptions.addView(b,margin(-1,dp(54),7));}
        score.setText("صحيح: "+correct+"   •   خطأ: "+wrong+"   •   النقاط: "+String.format(Locale.US,"%.1f",correct*.5f));
    }

    private void showAbout() {
        ScrollView s=new ScrollView(this); LinearLayout r=root(); s.addView(r); r.addView(text("عن التطبيق",27,true)); TextView about=text("نسخة Java Native خفيفة للأجهزة القديمة. تحتوي على بحث، تصفية حسب الصف، بطاقات تعليمية، تتبع للإتقان واختبار سريع، وتعمل دون اتصال بالإنترنت.",19,false); about.setBackground(round(Color.WHITE,20)); r.addView(about,margin(-1,-2,10)); nav(r); setContentView(s);
    }

    private void speak(String word){if(tts!=null)tts.speak(word,TextToSpeech.QUEUE_FLUSH,null);}
    @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}

    private int dp(int value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable round(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
    private LinearLayout.LayoutParams margin(int w,int h,int top){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);p.topMargin=dp(top);return p;}
}
