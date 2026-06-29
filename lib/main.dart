import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

void main() => runApp(const App());
const appTitle = 'القاموس التفاعلي';
const appVersion = 'V4';
const seed = Color(0xFF15803D);

class WordItem {
  final String en;
  final String ar;
  final String level;
  const WordItem(this.en, this.ar, this.level);
}

const words = [
  WordItem('apple', 'تفاحة', 'KG'), WordItem('book', 'كتاب', 'KG'), WordItem('school', 'مدرسة', 'KG'), WordItem('home', 'بيت', 'KG'), WordItem('cat', 'قطة', 'KG'), WordItem('dog', 'كلب', 'KG'),
  WordItem('water', 'ماء', '1'), WordItem('sun', 'شمس', '1'), WordItem('moon', 'قمر', '1'), WordItem('chair', 'كرسي', '1'), WordItem('table', 'طاولة', '1'), WordItem('pen', 'قلم', '1'),
  WordItem('family', 'عائلة', '2'), WordItem('garden', 'حديقة', '2'), WordItem('market', 'سوق', '2'), WordItem('friend', 'صديق', '2'), WordItem('teacher', 'معلم', '2'), WordItem('window', 'نافذة', '2'),
  WordItem('weather', 'الطقس', '3'), WordItem('library', 'مكتبة', '3'), WordItem('computer', 'حاسوب', '3'), WordItem('language', 'لغة', '3'), WordItem('question', 'سؤال', '3'), WordItem('answer', 'إجابة', '3'),
  WordItem('science', 'علوم', '4'), WordItem('history', 'تاريخ', '4'), WordItem('country', 'دولة', '4'), WordItem('travel', 'سفر', '4'), WordItem('healthy', 'صحي', '4'), WordItem('useful', 'مفيد', '4'),
];

class App extends StatelessWidget {
  const App({super.key});
  @override
  Widget build(BuildContext context) => MaterialApp(
    debugShowCheckedModeBanner: false,
    title: appTitle,
    locale: const Locale('ar'),
    supportedLocales: const [Locale('ar')],
    localizationsDelegates: GlobalMaterialLocalizations.delegates,
    theme: ThemeData(useMaterial3: true, colorScheme: ColorScheme.fromSeed(seedColor: seed), scaffoldBackgroundColor: const Color(0xFFF0FDF4), fontFamily: 'Arial'),
    home: const Directionality(textDirection: TextDirection.rtl, child: Home()),
  );
}

class Home extends StatefulWidget { const Home({super.key}); @override State<Home> createState() => _HomeState(); }

class _HomeState extends State<Home> {
  int tab = 0;
  String level = 'الكل';
  bool reveal = false;
  int quizIndex = 0;
  int correct = 0;
  int wrong = 0;
  final search = TextEditingController();
  final rnd = Random();

  List<WordItem> get filtered {
    final q = search.text.trim().toLowerCase();
    return words.where((w) {
      final l = level == 'الكل' || w.level == level;
      final s = q.isEmpty || w.en.toLowerCase().contains(q) || w.ar.contains(q) || w.level.contains(q);
      return l && s;
    }).toList();
  }

  WordItem get quizWord => words[quizIndex % words.length];
  List<String> get options { final set = <String>{quizWord.ar}; while (set.length < 4) { set.add(words[rnd.nextInt(words.length)].ar); } final list = set.toList(); list.shuffle(rnd); return list; }
  void answer(String choice) { setState(() { if (choice == quizWord.ar) { correct++; } else { wrong++; } quizIndex = (quizIndex + 1) % words.length; }); SystemSound.play(SystemSoundType.click); }

  @override Widget build(BuildContext context) { final pages = [home(), list(), quiz(), about()]; return Scaffold(body: SafeArea(child: AnimatedSwitcher(duration: const Duration(milliseconds: 220), child: pages[tab])), bottomNavigationBar: NavigationBar(selectedIndex: tab, onDestinationSelected: (v) => setState(() => tab = v), destinations: const [NavigationDestination(icon: Icon(Icons.home_rounded), label: 'الرئيسية'), NavigationDestination(icon: Icon(Icons.menu_book_rounded), label: 'الكلمات'), NavigationDestination(icon: Icon(Icons.quiz_rounded), label: 'اختبار'), NavigationDestination(icon: Icon(Icons.info_rounded), label: 'عن')]),); }

  Widget home() => ListView(padding: const EdgeInsets.all(16), children: [hero(), const SizedBox(height: 12), Row(children: [Expanded(child: stat('الكلمات', '${words.length}', Icons.translate_rounded)), const SizedBox(width: 10), Expanded(child: stat('المستويات', 'KG-4', Icons.school_rounded))]), const SizedBox(height: 12), header('بطاقة سريعة'), flashCard(words[quizIndex % words.length]), const SizedBox(height: 12), header('أمثلة'), ...words.take(4).map(tile)]);
  Widget list() => ListView(padding: const EdgeInsets.all(16), children: [header('الكلمات'), TextField(controller: search, onChanged: (_) => setState(() {}), decoration: input('بحث إنجليزي أو عربي', Icons.search_rounded)), const SizedBox(height: 10), SingleChildScrollView(scrollDirection: Axis.horizontal, child: Row(children: ['الكل', 'KG', '1', '2', '3', '4'].map((e) => Padding(padding: const EdgeInsetsDirectional.only(end: 8), child: ChoiceChip(label: Text(e), selected: level == e, onSelected: (_) => setState(() => level = e)))).toList())), const SizedBox(height: 12), if (filtered.isEmpty) empty(), ...filtered.map(tile)]);
  Widget quiz() { final opts = options; return ListView(padding: const EdgeInsets.all(16), children: [header('اختبار سريع'), card(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Text('ما معنى:', style: TextStyle(color: Colors.grey.shade700)), const SizedBox(height: 8), Text(quizWord.en, style: const TextStyle(fontSize: 34, fontWeight: FontWeight.w900)), const SizedBox(height: 12), ...opts.map((o) => Padding(padding: const EdgeInsets.only(bottom: 8), child: SizedBox(width: double.infinity, child: OutlinedButton(onPressed: () => answer(o), child: Text(o)))))])), const SizedBox(height: 12), Row(children: [Expanded(child: stat('صحيح', '$correct', Icons.check_circle_rounded)), const SizedBox(width: 10), Expanded(child: stat('خطأ', '$wrong', Icons.cancel_rounded))])]); }
  Widget about() => ListView(padding: const EdgeInsets.all(16), children: [header('عن التطبيق'), card(const Text('$appTitle V4\nتحسين بصري مريح، بحث، مستويات، بطاقة تفاعلية، واختبار سريع. يمكن لاحقًا توسيعه إلى 100 كلمة لكل صف وربطه بملف JSON.'))]);

  Widget hero() => Container(padding: const EdgeInsets.all(22), decoration: BoxDecoration(gradient: const LinearGradient(colors: [Color(0xFF15803D), Color(0xFF4ADE80)]), borderRadius: BorderRadius.circular(32), boxShadow: [BoxShadow(color: seed.withValues(alpha: .20), blurRadius: 28, offset: const Offset(0, 14))]), child: const Text('$appTitle V4', style: TextStyle(color: Colors.white, fontSize: 27, fontWeight: FontWeight.w900)));
  Widget flashCard(WordItem w) => card(Column(children: [Text(w.en, style: const TextStyle(fontSize: 34, fontWeight: FontWeight.w900)), const SizedBox(height: 10), if (reveal) Text(w.ar, style: const TextStyle(fontSize: 26, fontWeight: FontWeight.w800)), const SizedBox(height: 10), FilledButton.icon(onPressed: () => setState(() => reveal = !reveal), icon: Icon(reveal ? Icons.visibility_off_rounded : Icons.visibility_rounded), label: Text(reveal ? 'إخفاء المعنى' : 'إظهار المعنى'))]));
  Widget tile(WordItem w) => Padding(padding: const EdgeInsets.only(bottom: 10), child: card(ListTile(leading: CircleAvatar(backgroundColor: seed.withValues(alpha: .12), child: const Icon(Icons.translate_rounded, color: seed)), title: Text(w.en, style: const TextStyle(fontWeight: FontWeight.w900)), subtitle: Text(w.ar), trailing: Chip(label: Text(w.level)))));
  Widget stat(String t, String v, IconData icon) => card(Column(crossAxisAlignment: CrossAxisAlignment.start, children: [Icon(icon, color: seed), const SizedBox(height: 8), Text(v, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.w900)), Text(t)]));
  Widget header(String t) => Padding(padding: const EdgeInsets.only(bottom: 12), child: Text(t, style: const TextStyle(fontSize: 26, fontWeight: FontWeight.w900)));
  Widget empty() => card(Center(child: Text('لا توجد نتائج', style: TextStyle(color: Colors.grey.shade700))));
  InputDecoration input(String label, IconData icon) => InputDecoration(labelText: label, prefixIcon: Icon(icon), filled: true, fillColor: Colors.white, border: OutlineInputBorder(borderRadius: BorderRadius.circular(20), borderSide: BorderSide.none));
  Widget card(Widget child) => Container(width: double.infinity, padding: const EdgeInsets.all(18), decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(24), boxShadow: [BoxShadow(color: Colors.black.withValues(alpha: .045), blurRadius: 22, offset: const Offset(0, 10))]), child: child);
}
