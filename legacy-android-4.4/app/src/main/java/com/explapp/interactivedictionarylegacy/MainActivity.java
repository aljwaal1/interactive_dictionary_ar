package com.explapp.interactivedictionarylegacy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int GREEN = Color.rgb(22, 128, 79);
    private static final int GREEN_DARK = Color.rgb(16, 91, 58);
    private static final int MINT = Color.rgb(228, 247, 238);
    private static final int CREAM = Color.rgb(255, 249, 235);
    private static final int YELLOW = Color.rgb(248, 186, 57);
    private static final int CORAL = Color.rgb(232, 104, 91);
    private static final int BLUE = Color.rgb(54, 123, 190);
    private static final int INK = Color.rgb(31, 45, 57);
    private static final int MUTED = Color.rgb(94, 111, 119);
    private static final int BG = Color.rgb(246, 250, 248);

    private static class WordItem {
        final String en;
        final String ar;
        final String level;
        final String group;

        WordItem(String en, String ar, String level, String group) {
            this.en = en;
            this.ar = ar;
            this.level = level;
            this.group = group;
        }
    }

    private final WordItem[] words = new WordItem[] {
            new WordItem("apple", "تفاحة", "KG", "food"), new WordItem("book", "كتاب", "KG", "school"),
            new WordItem("school", "مدرسة", "KG", "place"), new WordItem("home", "بيت", "KG", "place"),
            new WordItem("cat", "قطة", "KG", "animal"), new WordItem("dog", "كلب", "KG", "animal"),
            new WordItem("water", "ماء", "1", "nature"), new WordItem("sun", "شمس", "1", "nature"),
            new WordItem("moon", "قمر", "1", "nature"), new WordItem("chair", "كرسي", "1", "object"),
            new WordItem("table", "طاولة", "1", "object"), new WordItem("pen", "قلم", "1", "school"),
            new WordItem("family", "عائلة", "2", "people"), new WordItem("garden", "حديقة", "2", "nature"),
            new WordItem("market", "سوق", "2", "place"), new WordItem("friend", "صديق", "2", "people"),
            new WordItem("teacher", "معلم", "2", "people"), new WordItem("window", "نافذة", "2", "object"),
            new WordItem("weather", "الطقس", "3", "nature"), new WordItem("library", "مكتبة", "3", "place"),
            new WordItem("computer", "حاسوب", "3", "object"), new WordItem("language", "لغة", "3", "idea"),
            new WordItem("question", "سؤال", "3", "idea"), new WordItem("answer", "إجابة", "3", "idea"),
            new WordItem("science", "علوم", "4", "idea"), new WordItem("history", "تاريخ", "4", "idea"),
            new WordItem("country", "دولة", "4", "place"), new WordItem("travel", "سفر", "4", "idea"),
            new WordItem("healthy", "صحي", "4", "idea"), new WordItem("useful", "مفيد", "4", "idea")
    };

    private final String[] levels = {"الكل", "KG", "1", "2", "3", "4"};
    private final String[] levelLabels = {"الكل", "روضة", "الأول", "الثاني", "الثالث", "الرابع"};
    private final Random random = new Random();
    private SharedPreferences prefs;
    private TextToSpeech tts;
    private boolean ttsReady;
    private LinearLayout content;
    private ScrollView scroll;
    private int section;
    private int cardIndex;
    private boolean revealed;
    private String selectedLevel = "الكل";
    private String query = "";
    private ArrayList<WordItem> round = new ArrayList<WordItem>();
    private int roundIndex;
    private int roundCorrect;
    private int roundWrong;
    private boolean answerLocked;
    private boolean reviewRound;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("interactive_learning", MODE_PRIVATE);
        cardIndex = Math.max(0, prefs.getInt("card_index", 0)) % words.length;
        selectedLevel = prefs.getString("level", "الكل");
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int language = tts.setLanguage(Locale.US);
                    ttsReady = language != TextToSpeech.LANG_MISSING_DATA && language != TextToSpeech.LANG_NOT_SUPPORTED;
                }
            }
        });
        showLearn();
    }

    private void page(String title, String subtitle, int active) {
        section = active;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(BG);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(dp(17), dp(14), dp(17), dp(13));
        header.setBackground(round(GREEN_DARK, 0));
        TextView app = label("القاموس التفاعلي", 25, Color.WHITE, Typeface.BOLD);
        TextView context = label(title + "  •  " + subtitle, 14, Color.rgb(207, 237, 222), Typeface.NORMAL);
        header.addView(app);
        header.addView(context, params(-1, -2, 0, 0, 4, 0, 0));
        root.addView(header);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(14), dp(12), dp(14), dp(12));
        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(4), dp(4), dp(4), dp(4));
        nav.setBackgroundColor(Color.WHITE);
        nav.addView(navButton("تعلّم", android.R.drawable.ic_menu_view, 0), navParams());
        nav.addView(navButton("استكشف", android.R.drawable.ic_menu_search, 1), navParams());
        nav.addView(navButton("العب", android.R.drawable.ic_menu_edit, 2), navParams());
        nav.addView(navButton("إنجازي", android.R.drawable.star_big_on, 3), navParams());
        root.addView(nav);
        setContentView(root);

        content.setAlpha(0f);
        content.setTranslationY(dp(7));
        content.animate().alpha(1f).translationY(0).setDuration(180).start();
    }

    private void showLearn() {
        page("تعلّم بالصورة", "المس الصورة واسمع الكلمة", 0);
        TextView progress = label("أتقنت " + masteredCount() + " من " + words.length + " كلمة", 15, GREEN, Typeface.BOLD);
        progress.setGravity(Gravity.CENTER);
        progress.setBackground(round(MINT, 14));
        content.addView(progress, params(-1, dp(46), 0, 0, 0, 0, 10));

        final WordItem word = words[cardIndex];
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(15), dp(14), dp(15), dp(18));
        TextView counter = label("الكلمة " + (cardIndex + 1) + " من " + words.length + "  •  " + levelName(word.level), 13, MUTED, Typeface.BOLD);
        counter.setGravity(Gravity.CENTER);
        card.addView(counter, params(-1, -2, 0, 0, 0, 0, 6));
        WordIllustration picture = new WordIllustration(this, word);
        picture.setContentDescription(word.ar);
        picture.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { speak(word.en); animateTap(view); } });
        card.addView(picture, params(-1, compactHeight() ? dp(155) : dp(205), 0, 0, 0, 0, 5));
        TextView english = label(word.en, 32, INK, Typeface.BOLD);
        english.setGravity(Gravity.CENTER);
        english.setTextDirection(View.TEXT_DIRECTION_LTR);
        card.addView(english);
        final TextView arabic = label(revealed ? word.ar : "اضغط لإظهار المعنى", 21, revealed ? GREEN : MUTED, Typeface.BOLD);
        arabic.setGravity(Gravity.CENTER);
        card.addView(arabic, params(-1, -2, 0, 0, 8, 0, 12));
        Button reveal = primary(revealed ? "إخفاء المعنى" : "إظهار المعنى والنطق");
        reveal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) { revealed = !revealed; speak(word.en); showLearn(); }
        });
        card.addView(reveal, params(-1, dp(52), 0, 0, 0, 0, 8));
        LinearLayout actions = new LinearLayout(this);
        Button repeat = outline("استمع مرة أخرى");
        repeat.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { speak(word.en); } });
        Button mastered = colored(isMastered(word) ? "تم إتقانها ✓" : "أتقنتها", isMastered(word) ? GREEN_DARK : GREEN);
        mastered.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { setMastered(word, !isMastered(word)); showLearn(); } });
        actions.addView(repeat, weight(1, 0, 4));
        actions.addView(mastered, weight(1, 4, 0));
        card.addView(actions, params(-1, dp(50), 0, 0, 0, 0, 8));
        Button next = outline("الكلمة التالية");
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                cardIndex = (cardIndex + 1) % words.length;
                prefs.edit().putInt("card_index", cardIndex).apply();
                revealed = false;
                showLearn();
            }
        });
        card.addView(next, params(-1, dp(50)));
        content.addView(elevated(card));
        TextView tip = label("فكرة صغيرة: انظر إلى الرسم، حاول قول الكلمة، ثم اضغط على الصورة لسماعها.", 14, MUTED, Typeface.NORMAL);
        tip.setGravity(Gravity.CENTER);
        content.addView(tip, params(-1, -2, 0, 4, 11, 4, 0));
    }

    private void showExplore() {
        page("استكشف الكلمات", "ابحث واختر كلمة لتعلّمها", 1);
        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setHint("بحث إنجليزي أو عربي");
        search.setText(query);
        search.setTextSize(16);
        search.setTextColor(INK);
        search.setHintTextColor(MUTED);
        search.setCompoundDrawablesWithIntrinsicBounds(0, 0, android.R.drawable.ic_menu_search, 0);
        search.setCompoundDrawablePadding(dp(8));
        search.setPadding(dp(14), dp(8), dp(14), dp(8));
        search.setBackground(round(Color.WHITE, 14, Color.rgb(211, 224, 217), 1));
        search.setSelection(search.length());
        content.addView(search, params(-1, dp(54), 0, 0, 0, 0, 9));
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            public void onTextChanged(CharSequence value, int start, int before, int count) { query = value.toString(); renderExplore(); }
            public void afterTextChanged(Editable editable) { }
        });
        addLevelChips(new LevelListener() { public void selected(String level) { selectedLevel = level; prefs.edit().putString("level", level).apply(); showExplore(); } });
        renderExplore();
    }

    private void renderExplore() {
        while (content.getChildCount() > 2) content.removeViewAt(2);
        ArrayList<WordItem> filtered = filter(selectedLevel, query);
        if (filtered.isEmpty()) {
            content.addView(emptyState("لا توجد نتيجة", "جرّب جزءاً من الكلمة أو مستوى آخر"));
            return;
        }
        TextView count = label(filtered.size() + " كلمة", 13, MUTED, Typeface.BOLD);
        count.setGravity(Gravity.CENTER);
        content.addView(count, params(-1, -2, 0, 0, 1, 0, 7));
        for (final WordItem word : filtered) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(9), dp(10), dp(9));
            row.setBackground(round(Color.WHITE, 14, Color.rgb(225, 234, 229), 1));
            WordIllustration art = new WordIllustration(this, word);
            row.addView(art, new LinearLayout.LayoutParams(dp(74), dp(68)));
            LinearLayout copy = new LinearLayout(this);
            copy.setOrientation(LinearLayout.VERTICAL);
            TextView en = label(word.en, 20, INK, Typeface.BOLD);
            en.setTextDirection(View.TEXT_DIRECTION_LTR);
            TextView ar = label(word.ar + "  •  " + levelName(word.level), 15, GREEN, Typeface.BOLD);
            copy.addView(en);
            copy.addView(ar, params(-1, -2, 0, 0, 3, 0, 0));
            row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
            ImageView sound = new ImageView(this);
            sound.setImageResource(android.R.drawable.ic_btn_speak_now);
            sound.setContentDescription("استمع");
            sound.setPadding(dp(10), dp(10), dp(10), dp(10));
            row.addView(sound, new LinearLayout.LayoutParams(dp(48), dp(48)));
            View.OnClickListener open = new View.OnClickListener() {
                public void onClick(View view) { cardIndex = indexOf(word); revealed = true; speak(word.en); showLearn(); }
            };
            row.setOnClickListener(open);
            sound.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { speak(word.en); } });
            content.addView(elevated(row), params(-1, -2, 0, 0, 0, 0, 8));
        }
    }

    private void showGameSetup() {
        page("لعبة المعاني", "اختر المستوى وابدأ جولة حتى 8 كلمات", 2);
        TextView intro = label("أي مجموعة تريد أن تتدرّب عليها؟", 20, INK, Typeface.BOLD);
        content.addView(intro, params(-1, -2, 0, 0, 5, 0, 12));
        for (int i = 1; i < levels.length; i++) {
            final String level = levels[i];
            LinearLayout option = card();
            TextView title = label(levelName(level), 19, GREEN_DARK, Typeface.BOLD);
            TextView detail = label(levelWordCount(level) + " كلمات  •  أتقنت " + masteredInLevel(level), 14, MUTED, Typeface.NORMAL);
            option.addView(title);
            option.addView(detail, params(-1, -2, 0, 0, 3, 0, 8));
            Button play = primary("ابدأ اللعب");
            play.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { startRound(level); } });
            option.addView(play, params(-1, dp(50)));
            content.addView(elevated(option), params(-1, -2, 0, 0, 0, 0, 9));
        }
        Button mixed = outline("جولة متنوعة من كل المستويات");
        mixed.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { startRound("الكل"); } });
        content.addView(mixed, params(-1, dp(52), 0, 0, 5, 0, 0));
    }

    private void startRound(String level) {
        reviewRound = false;
        selectedLevel = level;
        round = filter(level, "");
        Collections.shuffle(round);
        if (round.size() > 8) round = new ArrayList<WordItem>(round.subList(0, 8));
        roundIndex = 0;
        roundCorrect = 0;
        roundWrong = 0;
        showQuestion();
    }

    private void showQuestion() {
        page("لعبة " + levelName(selectedLevel), "السؤال " + Math.min(roundIndex + 1, round.size()) + " من " + round.size(), 2);
        if (roundIndex >= round.size()) { showRoundResult(); return; }
        answerLocked = false;
        final WordItem target = round.get(roundIndex);
        LinearLayout top = new LinearLayout(this);
        TextView correct = stat("صحيح", String.valueOf(roundCorrect), GREEN);
        TextView review = stat("مراجعة", String.valueOf(roundWrong), CORAL);
        top.addView(correct, weight(1, 0, 4));
        top.addView(review, weight(1, 4, 0));
        content.addView(top, params(-1, dp(68), 0, 0, 0, 0, 10));

        LinearLayout question = card();
        question.setGravity(Gravity.CENTER);
        WordIllustration art = new WordIllustration(this, target);
        art.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { speak(target.en); } });
        question.addView(art, params(-1, compactHeight() ? dp(130) : dp(165), 0, 0, 0, 0, 2));
        TextView en = label(target.en, 29, INK, Typeface.BOLD);
        en.setGravity(Gravity.CENTER);
        en.setTextDirection(View.TEXT_DIRECTION_LTR);
        question.addView(en);
        Button sound = outline("استمع للكلمة");
        sound.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { speak(target.en); } });
        question.addView(sound, params(-1, dp(46), 0, 0, 8, 0, 0));
        content.addView(elevated(question), params(-1, -2, 0, 0, 0, 0, 10));

        for (final String choice : choices(target)) {
            final Button answer = outline(choice);
            answer.setTextSize(17);
            answer.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    if (answerLocked) return;
                    answerLocked = true;
                    boolean ok = choice.equals(target.ar);
                    if (ok) {
                        roundCorrect++;
                        setMastered(target, true);
                        setNeedsReview(target, false);
                        answer.setBackground(round(MINT, 14, GREEN, 2));
                        answer.setTextColor(GREEN_DARK);
                        speak(target.en);
                    } else {
                        roundWrong++;
                        setNeedsReview(target, true);
                        answer.setBackground(round(Color.rgb(253, 235, 232), 14, CORAL, 2));
                        answer.setTextColor(CORAL);
                    }
                    prefs.edit().putInt("correct", prefs.getInt("correct", 0) + (ok ? 1 : 0))
                            .putInt("wrong", prefs.getInt("wrong", 0) + (ok ? 0 : 1)).apply();
                    addFeedback(target, ok);
                }
            });
            content.addView(answer, params(-1, dp(53), 0, 0, 0, 0, 8));
        }
    }

    private void addFeedback(WordItem target, boolean ok) {
        TextView feedback = label(ok ? "أحسنت! إجابة صحيحة" : "المعنى الصحيح: " + target.ar, 16, ok ? GREEN_DARK : CORAL, Typeface.BOLD);
        feedback.setGravity(Gravity.CENTER);
        feedback.setBackground(round(ok ? MINT : Color.rgb(253, 235, 232), 13));
        content.addView(feedback, params(-1, dp(51), 0, 0, 2, 0, 8));
        Button next = primary(roundIndex + 1 == round.size() ? "عرض النتيجة" : "الكلمة التالية");
        next.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { roundIndex++; showQuestion(); } });
        content.addView(next, params(-1, dp(52), 0, 0, 0, 0, 5));
        scroll.post(new Runnable() { public void run() { scroll.smoothScrollTo(0, content.getHeight()); } });
    }

    private void showRoundResult() {
        int percent = round.isEmpty() ? 0 : Math.round(roundCorrect * 100f / round.size());
        LinearLayout result = card();
        result.setGravity(Gravity.CENTER);
        WordIllustration celebration = new WordIllustration(this, new WordItem("answer", "إجابة", "", "idea"));
        result.addView(celebration, params(-1, dp(150), 0, 0, 0, 0, 4));
        TextView title = label(percent >= 75 ? "بطل الكلمات!" : percent >= 50 ? "تقدّم جميل" : "المحاولة تصنع الفرق", 25, GREEN_DARK, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        result.addView(title);
        TextView score = label(percent + "%\n" + roundCorrect + " صحيحة  •  " + roundWrong + " للمراجعة", 17, MUTED, Typeface.BOLD);
        score.setGravity(Gravity.CENTER);
        result.addView(score, params(-1, -2, 0, 0, 8, 0, 15));
        Button replay = primary(reviewRound ? "راجع الكلمات مرة أخرى" : "العب جولة أخرى");
        replay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (reviewRound) startReviewRound(); else startRound(selectedLevel);
            }
        });
        result.addView(replay, params(-1, dp(52), 0, 0, 0, 0, 8));
        Button levels = outline("تغيير المستوى");
        levels.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { showGameSetup(); } });
        result.addView(levels, params(-1, dp(50)));
        content.addView(elevated(result), params(-1, -2, 0, 0, 10, 0, 0));
    }

    private void showProgress() {
        page("إنجازي", "كل كلمة متقنة خطوة جديدة", 3);
        int mastered = masteredCount();
        LinearLayout hero = card();
        hero.setGravity(Gravity.CENTER);
        ImageView star = new ImageView(this);
        star.setImageResource(android.R.drawable.star_big_on);
        star.setColorFilter(YELLOW);
        hero.addView(star, new LinearLayout.LayoutParams(dp(62), dp(62)));
        TextView number = label(mastered + " / " + words.length, 27, GREEN_DARK, Typeface.BOLD);
        number.setGravity(Gravity.CENTER);
        hero.addView(number);
        TextView message = label(masteryMessage(mastered), 15, MUTED, Typeface.NORMAL);
        message.setGravity(Gravity.CENTER);
        hero.addView(message, params(-1, -2, 0, 0, 6, 0, 5));
        hero.addView(progressBar(mastered / (float) words.length), params(-1, dp(10), 0, 8, 8, 8, 0));
        content.addView(elevated(hero), params(-1, -2, 0, 0, 0, 0, 11));

        LinearLayout stats = new LinearLayout(this);
        stats.addView(stat("إجابات صحيحة", String.valueOf(prefs.getInt("correct", 0)), GREEN), weight(1, 0, 4));
        stats.addView(stat("تحتاج مراجعة", String.valueOf(needsReviewCount()), CORAL), weight(1, 4, 0));
        content.addView(stats, params(-1, dp(76), 0, 0, 0, 0, 12));

        TextView title = label("المستويات", 19, INK, Typeface.BOLD);
        content.addView(title, params(-1, -2, 0, 0, 0, 0, 7));
        for (int i = 1; i < levels.length; i++) {
            String level = levels[i];
            int done = masteredInLevel(level);
            int total = levelWordCount(level);
            LinearLayout row = card();
            LinearLayout line = new LinearLayout(this);
            TextView name = label(levelName(level), 16, GREEN_DARK, Typeface.BOLD);
            TextView count = label(done + " / " + total, 14, MUTED, Typeface.BOLD);
            count.setGravity(Gravity.LEFT);
            line.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
            line.addView(count, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(line);
            row.addView(progressBar(total == 0 ? 0f : done / (float) total), params(-1, dp(8), 0, 0, 8, 0, 0));
            content.addView(elevated(row), params(-1, -2, 0, 0, 0, 0, 7));
        }
        if (needsReviewCount() > 0) {
            Button review = primary("راجع " + needsReviewCount() + " كلمات تحتاج تثبيتاً");
            review.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) { startReviewRound(); }
            });
            content.addView(review, params(-1, dp(52), 0, 0, 7, 0, 7));
        }
        Button reset = outline("إعادة ضبط الإنجاز");
        reset.setTextColor(CORAL);
        reset.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { confirmReset(); } });
        content.addView(reset, params(-1, dp(50), 0, 0, 7, 0, 0));
    }

    private void startReviewRound() {
        round = new ArrayList<WordItem>();
        for (WordItem word : words) if (needsReview(word)) round.add(word);
        if (round.isEmpty()) {
            Toast.makeText(this, "لا توجد كلمات تحتاج مراجعة الآن", Toast.LENGTH_SHORT).show();
            showProgress();
            return;
        }
        Collections.shuffle(round);
        if (round.size() > 8) round = new ArrayList<WordItem>(round.subList(0, 8));
        reviewRound = true;
        selectedLevel = "مراجعة";
        roundIndex = 0;
        roundCorrect = 0;
        roundWrong = 0;
        showQuestion();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this).setTitle("إعادة ضبط الإنجاز؟")
                .setMessage("ستبدأ الكلمات والنتائج من جديد.")
                .setNegativeButton("إلغاء", null)
                .setPositiveButton("إعادة الضبط", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { prefs.edit().clear().apply(); cardIndex = 0; selectedLevel = "الكل"; showProgress(); }
                }).show();
    }

    private ArrayList<String> choices(WordItem target) {
        Set<String> options = new HashSet<String>();
        options.add(target.ar);
        ArrayList<WordItem> pool = filter(target.level, "");
        while (options.size() < 4 && options.size() < pool.size()) options.add(pool.get(random.nextInt(pool.size())).ar);
        ArrayList<String> result = new ArrayList<String>(options);
        Collections.shuffle(result);
        return result;
    }

    private ArrayList<WordItem> filter(String level, String search) {
        String q = normalize(search);
        ArrayList<WordItem> result = new ArrayList<WordItem>();
        for (WordItem word : words) {
            if (!"الكل".equals(level) && !level.equals(word.level)) continue;
            if (q.length() > 0 && !normalize(word.en).contains(q) && !normalize(word.ar).contains(q)) continue;
            result.add(word);
        }
        return result;
    }

    private void addLevelChips(final LevelListener listener) {
        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        for (int i = 0; i < levels.length; i++) {
            final String level = levels[i];
            Button chip = chip(levelLabels[i], level.equals(selectedLevel));
            chip.setOnClickListener(new View.OnClickListener() { public void onClick(View view) { listener.selected(level); } });
            row.addView(chip, params(-2, dp(42), 0, 0, 0, 6, 0));
        }
        horizontal.addView(row);
        content.addView(horizontal, params(-1, -2, 0, 0, 0, 0, 7));
    }

    private interface LevelListener { void selected(String level); }

    private Button navButton(String text, int icon, final int target) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(12);
        button.setAllCaps(false);
        button.setTextColor(section == target ? GREEN : MUTED);
        button.setTypeface(Typeface.DEFAULT, section == target ? Typeface.BOLD : Typeface.NORMAL);
        NavGlyph glyph = new NavGlyph(target, section == target ? GREEN : MUTED);
        glyph.setBounds(0, 0, dp(23), dp(23));
        button.setCompoundDrawables(null, glyph, null, null);
        button.setCompoundDrawablePadding(dp(2));
        button.setGravity(Gravity.CENTER);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (target == 0) showLearn();
                else if (target == 1) showExplore();
                else if (target == 2) showGameSetup();
                else showProgress();
            }
        });
        return button;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(13), dp(14), dp(13));
        card.setBackground(round(Color.WHITE, 17, Color.rgb(225, 234, 229), 1));
        return card;
    }

    private TextView stat(String label, String value, int color) {
        TextView stat = label(value + "\n" + label, 15, color, Typeface.BOLD);
        stat.setGravity(Gravity.CENTER);
        stat.setBackground(round(Color.WHITE, 14, Color.rgb(225, 234, 229), 1));
        return stat;
    }

    private View progressBar(float progress) {
        FrameLayout track = new FrameLayout(this);
        track.setBackground(round(Color.rgb(225, 235, 229), 8));
        View fill = new View(this);
        fill.setBackground(round(GREEN, 8));
        int available = (int) (getResources().getDisplayMetrics().widthPixels * .79f);
        track.addView(fill, new FrameLayout.LayoutParams(Math.max(dp(3), (int) (available * Math.max(0, Math.min(1, progress)))), -1));
        return track;
    }

    private LinearLayout emptyState(String title, String body) {
        LinearLayout empty = new LinearLayout(this);
        empty.setOrientation(LinearLayout.VERTICAL);
        empty.setGravity(Gravity.CENTER);
        empty.setPadding(dp(20), dp(35), dp(20), dp(35));
        ImageView image = new ImageView(this);
        image.setImageResource(android.R.drawable.ic_menu_search);
        image.setColorFilter(GREEN);
        empty.addView(image, new LinearLayout.LayoutParams(dp(52), dp(52)));
        TextView heading = label(title, 20, INK, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        empty.addView(heading, params(-1, -2, 0, 0, 10, 0, 5));
        TextView copy = label(body, 15, MUTED, Typeface.NORMAL);
        copy.setGravity(Gravity.CENTER);
        empty.addView(copy);
        return empty;
    }

    private Button primary(String text) { return colored(text, GREEN); }
    private Button colored(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(9), 0, dp(9), 0);
        button.setBackground(round(color, 14));
        return button;
    }
    private Button outline(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(15);
        button.setTextColor(GREEN_DARK);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(8), 0, dp(8), 0);
        button.setBackground(round(Color.WHITE, 14, Color.rgb(171, 207, 188), 1));
        return button;
    }
    private Button chip(String text, boolean selected) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setMinWidth(dp(65));
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setTextColor(selected ? Color.WHITE : GREEN_DARK);
        button.setTypeface(Typeface.DEFAULT, selected ? Typeface.BOLD : Typeface.NORMAL);
        button.setBackground(round(selected ? GREEN : MINT, 15));
        return button;
    }
    private TextView label(String text, int size, int color, int style) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(size);
        label.setTextColor(color);
        label.setTypeface(Typeface.DEFAULT, style);
        label.setLineSpacing(0, 1.08f);
        return label;
    }

    private View elevated(View view) { if (Build.VERSION.SDK_INT >= 21) view.setElevation(dp(2)); return view; }
    private LinearLayout.LayoutParams navParams() { return new LinearLayout.LayoutParams(0, dp(58), 1); }
    private LinearLayout.LayoutParams weight(float value, int left, int right) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, value);
        params.setMargins(dp(left), 0, dp(right), 0);
        return params;
    }
    private LinearLayout.LayoutParams params(int width, int height) { return new LinearLayout.LayoutParams(width, height); }
    private LinearLayout.LayoutParams params(int width, int height, float weight, int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height, weight);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }
    private GradientDrawable round(int color, int radius) { return round(color, radius, Color.TRANSPARENT, 0); }
    private GradientDrawable round(int color, int radius, int stroke, int width) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        if (width > 0) drawable.setStroke(dp(width), stroke);
        return drawable;
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private static final class NavGlyph extends Drawable {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); private final RectF r = new RectF(); private final int kind;
        NavGlyph(int kind, int color) { this.kind = kind; p.setColor(color); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); }
        @Override public void draw(Canvas c) {
            float w=getBounds().width(), h=getBounds().height(); c.save(); c.translate(getBounds().left,getBounds().top); p.setStrokeWidth(Math.max(2f,w*.09f)); p.setStyle(Paint.Style.STROKE);
            if(kind==0){r.set(w*.14f,h*.19f,w*.86f,h*.81f);c.drawRoundRect(r,w*.1f,w*.1f,p);c.drawCircle(w*.5f,h*.5f,w*.16f,p);}
            else if(kind==1){c.drawCircle(w*.42f,h*.42f,w*.25f,p);c.drawLine(w*.61f,h*.61f,w*.84f,h*.84f,p);}
            else if(kind==2){r.set(w*.14f,h*.14f,w*.86f,h*.86f);c.drawRoundRect(r,w*.16f,w*.16f,p);c.drawLine(w*.30f,h*.52f,w*.44f,h*.66f,p);c.drawLine(w*.44f,h*.66f,w*.72f,h*.34f,p);}
            else{p.setStyle(Paint.Style.FILL);r.set(w*.15f,h*.58f,w*.31f,h*.86f);c.drawRoundRect(r,w*.04f,w*.04f,p);r.set(w*.42f,h*.38f,w*.58f,h*.86f);c.drawRoundRect(r,w*.04f,w*.04f,p);r.set(w*.69f,h*.16f,w*.85f,h*.86f);c.drawRoundRect(r,w*.04f,w*.04f,p);}c.restore();
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}@Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
    private boolean compactHeight() { return getResources().getDisplayMetrics().heightPixels / getResources().getDisplayMetrics().density < 560; }
    private int indexOf(WordItem item) { for (int i = 0; i < words.length; i++) if (words[i] == item) return i; return 0; }
    private String levelName(String level) {
        for (int i = 0; i < levels.length; i++) if (levels[i].equals(level)) return levelLabels[i];
        return level;
    }
    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value.trim().toLowerCase(Locale.US), Normalizer.Form.NFD);
        return normalized.replaceAll("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]", "").replace("ـ", "").replace("أ", "ا").replace("إ", "ا").replace("آ", "ا");
    }
    private boolean isMastered(WordItem word) { return prefs.getBoolean("mastered_" + word.en, false); }
    private void setMastered(WordItem word, boolean mastered) {
        prefs.edit().putBoolean("mastered_" + word.en, mastered).apply();
        if (mastered) setNeedsReview(word, false);
    }
    private boolean needsReview(WordItem word) { return prefs.getBoolean("review_" + word.en, false); }
    private void setNeedsReview(WordItem word, boolean review) { prefs.edit().putBoolean("review_" + word.en, review).apply(); }
    private int needsReviewCount() { int total = 0; for (WordItem word : words) if (needsReview(word)) total++; return total; }
    private int levelWordCount(String level) { int total = 0; for (WordItem word : words) if (level.equals(word.level)) total++; return total; }
    private int masteredCount() { int total = 0; for (WordItem word : words) if (isMastered(word)) total++; return total; }
    private int masteredInLevel(String level) { int total = 0; for (WordItem word : words) if (level.equals(word.level) && isMastered(word)) total++; return total; }
    private String masteryMessage(int mastered) {
        if (mastered == words.length) return "ممتاز! أتقنت جميع كلمات القاموس.";
        if (mastered >= 20) return "اقتربت كثيراً من إكمال القاموس.";
        if (mastered >= 10) return "تقدّم رائع، استمر بنفس الهدوء.";
        return "ابدأ بكلمة واحدة، وكل يوم ستعرف أكثر.";
    }
    private void speak(String word) {
        if (!ttsReady) { Toast.makeText(this, "النطق غير متاح على هذا الجهاز", Toast.LENGTH_SHORT).show(); return; }
        tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }
    private void animateTap(View view) {
        view.animate().scaleX(.96f).scaleY(.96f).setDuration(70).withEndAction(new Runnable() {
            public void run() { view.animate().scaleX(1f).scaleY(1f).setDuration(100).start(); }
        }).start();
    }

    @Override public void onBackPressed() { if (section != 0) showLearn(); else super.onBackPressed(); }
    @Override protected void onDestroy() { if (tts != null) { tts.stop(); tts.shutdown(); } super.onDestroy(); }

    private static class WordIllustration extends View {
        private final WordItem word;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        WordIllustration(Activity context, WordItem word) { super(context); this.word = word; }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float cx = w / 2f;
            float cy = h / 2f;
            float unit = Math.min(w, h) / 100f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(background(word.group));
            canvas.drawRoundRect(new RectF(unit * 4, unit * 4, w - unit * 4, h - unit * 4), unit * 16, unit * 16, paint);
            drawObject(canvas, cx, cy, unit, word.en);
        }

        private void drawObject(Canvas canvas, float x, float y, float u, String key) {
            if ("apple".equals(key)) drawApple(canvas, x, y, u);
            else if ("book".equals(key) || "library".equals(key) || "history".equals(key)) drawBook(canvas, x, y, u, key);
            else if ("school".equals(key) || "home".equals(key)) drawHouse(canvas, x, y, u, "school".equals(key));
            else if ("cat".equals(key) || "dog".equals(key)) drawAnimal(canvas, x, y, u, "cat".equals(key));
            else if ("water".equals(key)) drawDrop(canvas, x, y, u);
            else if ("sun".equals(key) || "moon".equals(key) || "weather".equals(key)) drawSky(canvas, x, y, u, key);
            else if ("chair".equals(key) || "table".equals(key) || "window".equals(key)) drawFurniture(canvas, x, y, u, key);
            else if ("pen".equals(key)) drawPen(canvas, x, y, u);
            else if ("family".equals(key) || "friend".equals(key) || "teacher".equals(key)) drawPeople(canvas, x, y, u, key);
            else if ("garden".equals(key)) drawGarden(canvas, x, y, u);
            else if ("market".equals(key)) drawMarket(canvas, x, y, u);
            else if ("computer".equals(key)) drawComputer(canvas, x, y, u);
            else if ("science".equals(key)) drawScience(canvas, x, y, u);
            else if ("country".equals(key) || "travel".equals(key)) drawTravel(canvas, x, y, u, key);
            else if ("healthy".equals(key)) drawHeart(canvas, x, y, u);
            else if ("useful".equals(key)) drawCheck(canvas, x, y, u);
            else drawSpeech(canvas, x, y, u, key);
        }

        private int background(String group) {
            if ("nature".equals(group)) return Color.rgb(225, 245, 230);
            if ("animal".equals(group)) return Color.rgb(255, 241, 218);
            if ("people".equals(group)) return Color.rgb(239, 232, 252);
            if ("place".equals(group)) return Color.rgb(226, 240, 252);
            if ("idea".equals(group)) return Color.rgb(255, 236, 232);
            return Color.rgb(250, 245, 223);
        }

        private void fill(int color) { paint.setStyle(Paint.Style.FILL); paint.setColor(color); }
        private void stroke(int color, float width) { paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(width); paint.setStrokeCap(Paint.Cap.ROUND); paint.setStrokeJoin(Paint.Join.ROUND); paint.setColor(color); }

        private void drawApple(Canvas c, float x, float y, float u) {
            fill(Color.rgb(226, 70, 64)); c.drawCircle(x - 12*u, y + 7*u, 22*u, paint); c.drawCircle(x + 12*u, y + 7*u, 22*u, paint);
            fill(Color.rgb(124, 67, 36)); c.drawRoundRect(new RectF(x - 3*u, y - 28*u, x + 3*u, y - 9*u), 2*u, 2*u, paint);
            fill(Color.rgb(46, 145, 74)); c.drawOval(new RectF(x + 2*u, y - 30*u, x + 25*u, y - 15*u), paint);
            fill(Color.WHITE); c.drawCircle(x - 18*u, y - 1*u, 5*u, paint);
        }
        private void drawBook(Canvas c, float x, float y, float u, String key) {
            fill("history".equals(key) ? Color.rgb(190, 136, 75) : Color.rgb(61, 126, 191));
            c.drawRoundRect(new RectF(x - 34*u, y - 27*u, x + 34*u, y + 29*u), 5*u, 5*u, paint);
            fill(Color.WHITE); c.drawRect(x - 28*u, y - 21*u, x - 3*u, y + 23*u, paint); c.drawRect(x + 3*u, y - 21*u, x + 28*u, y + 23*u, paint);
            stroke(Color.rgb(150, 164, 170), 2*u); c.drawLine(x, y - 23*u, x, y + 25*u, paint);
            c.drawLine(x - 22*u, y - 10*u, x - 8*u, y - 10*u, paint); c.drawLine(x + 8*u, y - 10*u, x + 22*u, y - 10*u, paint);
        }
        private void drawHouse(Canvas c, float x, float y, float u, boolean school) {
            fill(school ? Color.rgb(245, 193, 64) : Color.rgb(246, 151, 94)); c.drawRect(x - 34*u, y - 5*u, x + 34*u, y + 32*u, paint);
            path.reset(); path.moveTo(x - 43*u, y - 5*u); path.lineTo(x, y - 38*u); path.lineTo(x + 43*u, y - 5*u); path.close(); fill(Color.rgb(191, 74, 65)); c.drawPath(path, paint);
            fill(Color.rgb(78, 124, 154)); c.drawRect(x - 9*u, y + 7*u, x + 9*u, y + 32*u, paint);
            fill(Color.rgb(211, 238, 248)); c.drawRect(x - 28*u, y + 4*u, x - 15*u, y + 17*u, paint); c.drawRect(x + 15*u, y + 4*u, x + 28*u, y + 17*u, paint);
            if (school) { fill(Color.WHITE); c.drawCircle(x, y - 3*u, 8*u, paint); stroke(GREEN_DARK, 2*u); c.drawLine(x, y - 3*u, x, y - 8*u, paint); c.drawLine(x, y - 3*u, x + 5*u, y, paint); }
        }
        private void drawAnimal(Canvas c, float x, float y, float u, boolean cat) {
            int color = cat ? Color.rgb(239, 161, 72) : Color.rgb(178, 125, 75);
            fill(color); c.drawCircle(x, y + 3*u, 31*u, paint);
            path.reset(); path.moveTo(x - 28*u, y - 13*u); path.lineTo(x - 23*u, y - 40*u); path.lineTo(x - 8*u, y - 24*u); path.close(); c.drawPath(path, paint);
            path.reset(); path.moveTo(x + 28*u, y - 13*u); path.lineTo(x + 23*u, y - 40*u); path.lineTo(x + 8*u, y - 24*u); path.close(); c.drawPath(path, paint);
            fill(Color.WHITE); c.drawCircle(x - 12*u, y - 3*u, 7*u, paint); c.drawCircle(x + 12*u, y - 3*u, 7*u, paint);
            fill(INK); c.drawCircle(x - 12*u, y - 3*u, 3*u, paint); c.drawCircle(x + 12*u, y - 3*u, 3*u, paint); c.drawCircle(x, y + 10*u, 4*u, paint);
            stroke(INK, 2*u); c.drawArc(new RectF(x - 13*u, y + 8*u, x, y + 23*u), 5, 75, false, paint); c.drawArc(new RectF(x, y + 8*u, x + 13*u, y + 23*u), 100, 75, false, paint);
        }
        private void drawDrop(Canvas c, float x, float y, float u) {
            path.reset(); path.moveTo(x, y - 39*u); path.cubicTo(x - 8*u, y - 20*u, x - 29*u, y + 2*u, x - 29*u, y + 17*u); path.cubicTo(x - 29*u, y + 40*u, x + 29*u, y + 40*u, x + 29*u, y + 17*u); path.cubicTo(x + 29*u, y + 2*u, x + 8*u, y - 20*u, x, y - 39*u); path.close(); fill(Color.rgb(64, 155, 218)); c.drawPath(path, paint);
            fill(Color.argb(170, 255, 255, 255)); c.drawOval(new RectF(x - 16*u, y - 3*u, x - 7*u, y + 14*u), paint);
        }
        private void drawSky(Canvas c, float x, float y, float u, String key) {
            if ("sun".equals(key)) {
                stroke(Color.rgb(245, 176, 39), 5*u); for (int i=0;i<8;i++){double a=i*Math.PI/4;c.drawLine(x+(float)Math.cos(a)*30*u,y+(float)Math.sin(a)*30*u,x+(float)Math.cos(a)*42*u,y+(float)Math.sin(a)*42*u,paint);} fill(Color.rgb(255, 198, 54)); c.drawCircle(x,y,24*u,paint);
            } else if ("moon".equals(key)) {
                fill(Color.rgb(245, 205, 82)); c.drawCircle(x, y, 32*u, paint); fill(background(word.group)); c.drawCircle(x + 16*u, y - 10*u, 31*u, paint);
            } else {
                fill(Color.WHITE); c.drawCircle(x - 19*u,y,18*u,paint);c.drawCircle(x,y-12*u,24*u,paint);c.drawCircle(x+23*u,y,18*u,paint);c.drawRect(x-28*u,y,x+31*u,y+17*u,paint);
                stroke(Color.rgb(73, 150, 207), 4*u); c.drawLine(x-18*u,y+25*u,x-24*u,y+38*u,paint);c.drawLine(x,y+25*u,x-6*u,y+38*u,paint);c.drawLine(x+18*u,y+25*u,x+12*u,y+38*u,paint);
            }
        }
        private void drawFurniture(Canvas c, float x, float y, float u, String key) {
            stroke(Color.rgb(132, 83, 52), 7*u);
            if ("chair".equals(key)) { c.drawLine(x-23*u,y-28*u,x-23*u,y+34*u,paint);c.drawLine(x-23*u,y+5*u,x+23*u,y+5*u,paint);c.drawLine(x+23*u,y+5*u,x+23*u,y+34*u,paint);c.drawLine(x-23*u,y-14*u,x+15*u,y-14*u,paint); }
            else if ("table".equals(key)) { c.drawLine(x-35*u,y-5*u,x+35*u,y-5*u,paint);c.drawLine(x-25*u,y-5*u,x-28*u,y+34*u,paint);c.drawLine(x+25*u,y-5*u,x+28*u,y+34*u,paint); }
            else { fill(Color.rgb(106, 158, 191)); c.drawRect(x-32*u,y-32*u,x+32*u,y+32*u,paint); fill(Color.rgb(218, 241, 250)); c.drawRect(x-25*u,y-25*u,x+25*u,y+25*u,paint); stroke(Color.WHITE,3*u);c.drawLine(x,y-25*u,x,y+25*u,paint);c.drawLine(x-25*u,y,x+25*u,y,paint); }
        }
        private void drawPen(Canvas c,float x,float y,float u){c.save();c.rotate(-35,x,y);fill(Color.rgb(55,120,188));c.drawRoundRect(new RectF(x-8*u,y-37*u,x+8*u,y+28*u),5*u,5*u,paint);path.reset();path.moveTo(x-8*u,y+28*u);path.lineTo(x,y+43*u);path.lineTo(x+8*u,y+28*u);path.close();fill(Color.rgb(241,194,124));c.drawPath(path,paint);c.restore();}
        private void drawPeople(Canvas c,float x,float y,float u,String key){int count="family".equals(key)?3:2;for(int i=0;i<count;i++){float px=x+(i-(count-1)/2f)*27*u;fill(i%2==0?Color.rgb(72,132,190):Color.rgb(232,122,104));c.drawCircle(px,y-16*u,(i==1?13:11)*u,paint);c.drawRoundRect(new RectF(px-14*u,y,px+14*u,y+34*u),10*u,10*u,paint);}if("teacher".equals(key)){stroke(GREEN_DARK,3*u);c.drawLine(x+25*u,y-5*u,x+40*u,y-25*u,paint);}}
        private void drawGarden(Canvas c,float x,float y,float u){fill(Color.rgb(88,170,91));c.drawRect(x-40*u,y+20*u,x+40*u,y+35*u,paint);for(int i=-1;i<=1;i++){float px=x+i*25*u;stroke(Color.rgb(45,132,69),3*u);c.drawLine(px,y+22*u,px,y-7*u,paint);fill(i==0?CORAL:YELLOW);c.drawCircle(px,y-14*u,9*u,paint);fill(Color.WHITE);c.drawCircle(px,y-14*u,3*u,paint);}}
        private void drawMarket(Canvas c,float x,float y,float u){fill(Color.rgb(246,201,85));c.drawRect(x-34*u,y-4*u,x+34*u,y+32*u,paint);for(int i=0;i<4;i++){fill(i%2==0?CORAL:Color.WHITE);c.drawRect(x-40*u+i*20*u,y-27*u,x-20*u+i*20*u,y-4*u,paint);}stroke(Color.rgb(126,82,54),4*u);c.drawLine(x-31*u,y+32*u,x-31*u,y+40*u,paint);c.drawLine(x+31*u,y+32*u,x+31*u,y+40*u,paint);}
        private void drawComputer(Canvas c,float x,float y,float u){fill(Color.rgb(67,91,110));c.drawRoundRect(new RectF(x-39*u,y-29*u,x+39*u,y+21*u),6*u,6*u,paint);fill(Color.rgb(139,208,224));c.drawRect(x-32*u,y-22*u,x+32*u,y+14*u,paint);stroke(Color.rgb(67,91,110),6*u);c.drawLine(x,y+21*u,x,y+34*u,paint);c.drawLine(x-18*u,y+35*u,x+18*u,y+35*u,paint);}
        private void drawScience(Canvas c,float x,float y,float u){stroke(Color.rgb(76,98,112),5*u);path.reset();path.moveTo(x-10*u,y-38*u);path.lineTo(x-10*u,y-5*u);path.lineTo(x-31*u,y+31*u);path.lineTo(x+31*u,y+31*u);path.lineTo(x+10*u,y-5*u);path.lineTo(x+10*u,y-38*u);c.drawPath(path,paint);fill(Color.rgb(81,190,157));path.reset();path.moveTo(x-24*u,y+18*u);path.lineTo(x+24*u,y+18*u);path.lineTo(x+31*u,y+31*u);path.lineTo(x-31*u,y+31*u);path.close();c.drawPath(path,paint);fill(Color.WHITE);c.drawCircle(x-8*u,y+10*u,4*u,paint);}
        private void drawTravel(Canvas c,float x,float y,float u,String key){if("country".equals(key)){fill(Color.rgb(72,145,198));c.drawCircle(x,y,34*u,paint);stroke(Color.WHITE,3*u);c.drawOval(new RectF(x-18*u,y-34*u,x+18*u,y+34*u),paint);c.drawLine(x-32*u,y,x+32*u,y,paint);}else{fill(Color.rgb(233,132,82));c.drawRoundRect(new RectF(x-31*u,y-21*u,x+31*u,y+32*u),8*u,8*u,paint);stroke(Color.rgb(97,74,61),5*u);c.drawArc(new RectF(x-15*u,y-39*u,x+15*u,y-5*u),190,160,false,paint);fill(Color.rgb(250,208,70));c.drawRect(x-4*u,y-21*u,x+4*u,y+32*u,paint);}}
        private void drawHeart(Canvas c,float x,float y,float u){path.reset();path.moveTo(x,y+36*u);path.cubicTo(x-55*u,y+2*u,x-30*u,y-35*u,x,y-18*u);path.cubicTo(x+30*u,y-35*u,x+55*u,y+2*u,x,y+36*u);path.close();fill(CORAL);c.drawPath(path,paint);fill(Color.WHITE);c.drawRect(x-4*u,y-12*u,x+4*u,y+15*u,paint);c.drawRect(x-14*u,y-2*u,x+14*u,y+6*u,paint);}
        private void drawCheck(Canvas c,float x,float y,float u){fill(GREEN);c.drawCircle(x,y,35*u,paint);stroke(Color.WHITE,8*u);path.reset();path.moveTo(x-19*u,y);path.lineTo(x-5*u,y+15*u);path.lineTo(x+23*u,y-17*u);c.drawPath(path,paint);}
        private void drawSpeech(Canvas c,float x,float y,float u,String key){fill(Color.rgb(77,137,194));c.drawRoundRect(new RectF(x-38*u,y-27*u,x+38*u,y+22*u),15*u,15*u,paint);path.reset();path.moveTo(x-10*u,y+21*u);path.lineTo(x-22*u,y+37*u);path.lineTo(x+4*u,y+22*u);path.close();c.drawPath(path,paint);paint.setTypeface(Typeface.DEFAULT_BOLD);paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(27*u);fill(Color.WHITE);String mark="question".equals(key)?"?":"answer".equals(key)?"✓":"A";c.drawText(mark,x,y+8*u,paint);paint.setTextAlign(Paint.Align.LEFT);}
    }
}
