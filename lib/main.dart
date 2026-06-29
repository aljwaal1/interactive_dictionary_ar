import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';

void main() => runApp(const App());

class App extends StatelessWidget {
  const App({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      locale: const Locale('ar'),
      supportedLocales: const [Locale('ar')],
      localizationsDelegates: GlobalMaterialLocalizations.delegates,
      theme: ThemeData(useMaterial3: true, colorSchemeSeed: const Color(0xFF16A34A)),
      home: const Directionality(textDirection: TextDirection.rtl, child: Home()),
    );
  }
}

class Home extends StatefulWidget { const Home({super.key}); @override State<Home> createState() => _HomeState(); }
class _HomeState extends State<Home> {
  int tab = 0;
  final words = ['apple = تفاحة', 'book = كتاب', 'school = مدرسة', 'home = بيت'];
  @override
  Widget build(BuildContext context) {
    final pages = [home(), list(), about()];
    return Scaffold(
      backgroundColor: const Color(0xFFF0FDF4),
      body: SafeArea(child: pages[tab]),
      bottomNavigationBar: NavigationBar(
        selectedIndex: tab,
        onDestinationSelected: (v) => setState(() => tab = v),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.home_rounded), label: 'الرئيسية'),
          NavigationDestination(icon: Icon(Icons.menu_book_rounded), label: 'الكلمات'),
          NavigationDestination(icon: Icon(Icons.info_rounded), label: 'عن'),
        ],
      ),
    );
  }
  Widget home() => ListView(padding: const EdgeInsets.all(16), children: [hero(), const SizedBox(height: 12), card(Text('عدد الكلمات: ${words.length}', style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w900))), const SizedBox(height: 12), ...words.take(3).map(tile)]);
  Widget list() => ListView(padding: const EdgeInsets.all(16), children: [header('الكلمات'), ...words.map(tile)]);
  Widget about() => ListView(padding: const EdgeInsets.all(16), children: [header('عن التطبيق'), card(const Text('القاموس التفاعلي V2'))]);
  Widget hero() => Container(padding: const EdgeInsets.all(20), decoration: BoxDecoration(gradient: const LinearGradient(colors: [Color(0xFF16A34A), Color(0xFF86EFAC)]), borderRadius: BorderRadius.circular(28)), child: const Text('القاموس التفاعلي V2', style: TextStyle(color: Colors.white, fontSize: 26, fontWeight: FontWeight.w900)));
  Widget tile(String t) => Padding(padding: const EdgeInsets.only(bottom: 10), child: card(ListTile(leading: const Icon(Icons.translate_rounded), title: Text(t))));
  Widget header(String t) => Padding(padding: const EdgeInsets.only(bottom: 12), child: Text(t, style: const TextStyle(fontSize: 26, fontWeight: FontWeight.w900)));
  Widget card(Widget child) => Container(width: double.infinity, padding: const EdgeInsets.all(16), decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(22)), child: child);
}
