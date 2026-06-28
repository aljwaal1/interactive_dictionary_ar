
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const SmartToolApp());
}

class SmartToolApp extends StatelessWidget {
  const SmartToolApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: "قاموس تفاعلي",
      theme: ThemeData(
        useMaterial3: true,
        fontFamily: 'Roboto',
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF16A34A)),
      ),
      home: const HomePage(),
    );
  }
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  final TextEditingController inputController = TextEditingController();
  final TextEditingController extraController = TextEditingController();
  String selectedTone = 'واضح ومباشر';
  String output = '';
  int tab = 0;

  final List<String> tones = const [
    'واضح ومباشر',
    'رسمي',
    'ودي',
    'مختصر',
    'تسويقي',
  ];

  @override
  void initState() {
    super.initState();
    output = welcomeText();
  }

  @override
  void dispose() {
    inputController.dispose();
    extraController.dispose();
    super.dispose();
  }

  String welcomeText() {
    switch ("dictionary") {
      case 'rewriter':
        return 'اكتب نصك ثم اختر الأسلوب. هذه نسخة أولى تعمل محليًا بدون إنترنت.';
      case 'email':
        return 'اكتب فكرة الرسالة أو الرد، وسأرتب لك قالبًا جاهزًا للنسخ.';
      case 'study':
        return 'اكتب فقرة من الدرس وسأخرج لك ملخصًا ونقاط مراجعة وأسئلة.';
      case 'dictionary':
        return 'ابحث عن كلمة من القائمة أو أضف كلمة في المربع لرؤية بطاقة تعليمية بسيطة.';
      default:
        return 'اكتب فكرة المنشور، ثم اختر النبرة لتحصل على صياغة جاهزة.';
    }
  }

  void softClick() => SystemSound.play(SystemSoundType.click);
  void doneSound() => SystemSound.play(SystemSoundType.alert);

  void generate() {
    softClick();
    final text = inputController.text.trim();
    if (text.isEmpty) {
      setState(() => output = 'اكتب نصًا أولًا حتى أساعدك.');
      return;
    }
    setState(() {
      output = buildResult(text, selectedTone, extraController.text.trim());
    });
    doneSound();
  }

  String buildResult(String text, String tone, String extra) {
    switch ("dictionary") {
      case 'rewriter':
        return 'النص بعد إعادة الصياغة ($tone):\n\n${cleanText(text)}\n\nاقتراح تحسين:\n- اجعل الجمل أقصر.\n- ابدأ بالفكرة الأهم.\n- احذف التكرار.';
      case 'email':
        return 'الموضوع: ${extra.isEmpty ? 'رسالة متابعة' : extra}\n\nالسلام عليكم،\n\n${cleanText(text)}\n\nأرجو إفادتي بما يلزم، وشكرًا لتعاونكم.\n\nمع التحية.';
      case 'study':
        return 'ملخص سريع:\n${summarize(text)}\n\nنقاط مهمة:\n1. الفكرة الرئيسية واضحة في النص.\n2. احفظ الكلمات المفتاحية.\n3. راجع المثال المرتبط بالدرس.\n\nأسئلة مراجعة:\n- ما الفكرة الأساسية؟\n- اذكر مثالًا عليها.\n- كيف تشرحها لصديقك؟';
      case 'dictionary':
        return dictionaryCard(text);
      default:
        return 'منشور مقترح ($tone):\n\n${cleanText(text)}\n\n✨ فكرة مختصرة وواضحة تستحق التجربة.\n\n#تطبيقات #أدوات_ذكية #إنتاجية';
    }
  }

  String cleanText(String text) {
    var t = text.replaceAll(RegExp(r'\s+'), ' ').trim();
    if (t.endsWith('.')) return t;
    return '$t.';
  }

  String summarize(String text) {
    final sentences = text.split(RegExp(r'[.!؟\n]')).map((e) => e.trim()).where((e) => e.isNotEmpty).toList();
    if (sentences.isEmpty) return cleanText(text);
    return sentences.take(3).map((e) => '• $e').join('\n');
  }

  String dictionaryCard(String word) {
    final lower = word.toLowerCase();
    final data = <String, List<String>>{
      'book': ['كتاب', 'I read a book every day.'],
      'school': ['مدرسة', 'My school is near my home.'],
      'water': ['ماء', 'Water is important.'],
      'family': ['عائلة', 'I love my family.'],
      'friend': ['صديق', 'My friend is kind.'],
      'قلم': ['pen', 'I write with a pen.'],
      'بيت': ['house', 'This is my house.'],
      'طعام': ['food', 'Food gives us energy.'],
    };
    final found = data[lower] ?? data[word];
    if (found == null) {
      return 'بطاقة كلمة:\n\nالكلمة: $word\nالمعنى: أضف معناها لاحقًا\nمثال: اكتب مثالًا بسيطًا عليها.\n\nهذه نسخة أولى، وبعد نجاح APK نضيف قاموسًا أكبر.';
    }
    return 'بطاقة كلمة:\n\nالكلمة: $word\nالمعنى: ${found[0]}\nمثال: ${found[1]}\n\nتدريب: حاول تكوين جملة جديدة بهذه الكلمة.';
  }

  Future<void> copyOutput() async {
    softClick();
    await Clipboard.setData(ClipboardData(text: output));
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('تم نسخ النتيجة')));
  }

  Future<void> copyDeveloperMessage() async {
    softClick();
    final message = 'السلام عليكم، لدي ملاحظة حول تطبيق "قاموس تفاعلي": ';
    await Clipboard.setData(const ClipboardData(text: 'السلام عليكم، لدي ملاحظة حول التطبيق. البريد: fastunlocked2017@gmail.com'));
    if (!mounted) return;
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('تم نسخ رسالة المطور')));
  }

  @override
  Widget build(BuildContext context) {
    final pages = [buildToolPage(), buildAboutPage(), buildDeveloperPage()];
    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        backgroundColor: const Color(0xFFF8FAFC),
        body: SafeArea(child: pages[tab]),
        bottomNavigationBar: NavigationBar(
          selectedIndex: tab,
          onDestinationSelected: (i) {
            softClick();
            setState(() => tab = i);
          },
          destinations: const [
            NavigationDestination(icon: Icon(Icons.auto_awesome), label: 'الأداة'),
            NavigationDestination(icon: Icon(Icons.info_outline), label: 'عن التطبيق'),
            NavigationDestination(icon: Icon(Icons.mail_outline), label: 'المطور'),
          ],
        ),
      ),
    );
  }

  Widget buildToolPage() {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        HeroHeader(),
        const SizedBox(height: 18),
        TextField(
          controller: inputController,
          minLines: 5,
          maxLines: 9,
          textDirection: TextDirection.rtl,
          decoration: InputDecoration(
            labelText: inputLabel(),
            hintText: inputHint(),
            filled: true,
            fillColor: Colors.white,
            border: OutlineInputBorder(borderRadius: BorderRadius.circular(22), borderSide: BorderSide.none),
          ),
        ),
        const SizedBox(height: 12),
        if ("dictionary" != 'dictionary') Row(
          children: [
            Expanded(
              child: DropdownButtonFormField<String>(
                initialValue: selectedTone,
                decoration: InputDecoration(
                  labelText: 'الأسلوب',
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(18), borderSide: BorderSide.none),
                ),
                items: tones.map((t) => DropdownMenuItem(value: t, child: Text(t))).toList(),
                onChanged: (v) => setState(() => selectedTone = v ?? selectedTone),
              ),
            ),
            if ("dictionary" == 'email') const SizedBox(width: 10),
            if ("dictionary" == 'email') Expanded(
              child: TextField(
                controller: extraController,
                decoration: InputDecoration(
                  labelText: 'موضوع الرسالة',
                  filled: true,
                  fillColor: Colors.white,
                  border: OutlineInputBorder(borderRadius: BorderRadius.circular(18), borderSide: BorderSide.none),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 14),
        FilledButton.icon(
          onPressed: generate,
          icon: const Icon(Icons.bolt),
          label: Text(actionText()),
          style: FilledButton.styleFrom(
            backgroundColor: const Color(0xFF16A34A),
            padding: const EdgeInsets.symmetric(vertical: 16),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          ),
        ),
        const SizedBox(height: 16),
        OutputCard(text: output, onCopy: copyOutput),
      ],
    );
  }

  String inputLabel() {
    switch ("dictionary") {
      case 'dictionary': return 'اكتب كلمة عربية أو إنجليزية';
      case 'email': return 'فكرة الرسالة أو الرد';
      case 'study': return 'نص الدرس';
      case 'posts': return 'فكرة المنشور';
      default: return 'النص الأصلي';
    }
  }

  String inputHint() {
    switch ("dictionary") {
      case 'dictionary': return 'مثال: book أو قلم';
      case 'study': return 'انسخ فقرة قصيرة من الدرس هنا';
      case 'email': return 'أريد الرد على العميل بخصوص...';
      case 'posts': return 'تطبيق جديد يساعدك على...';
      default: return 'اكتب النص الذي تريد تحسينه';
    }
  }

  String actionText() {
    switch ("dictionary") {
      case 'dictionary': return 'اعرض بطاقة الكلمة';
      case 'email': return 'اكتب الرسالة';
      case 'study': return 'لخص الدرس';
      case 'posts': return 'أنشئ المنشور';
      default: return 'أعد الصياغة';
    }
  }

  Widget buildAboutPage() {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: const [
        HeroHeader(),
        SizedBox(height: 18),
        InfoCard(title: 'فكرة التطبيق', text: "تعلم كلمات عربية وإنجليزية بطريقة بسيطة"),
        InfoCard(title: 'مهم', text: 'هذه نسخة أولى خفيفة تعمل محليًا. بعد نجاح APK يمكن ربطها بخدمة ذكاء اصطناعي حقيقية.'),
        InfoCard(title: 'الخصوصية', text: 'لا يتم إرسال النصوص لأي خادم في هذه النسخة.'),
      ],
    );
  }

  Widget buildDeveloperPage() {
    return ListView(
      padding: const EdgeInsets.all(18),
      children: [
        const HeroHeader(),
        const SizedBox(height: 18),
        const InfoCard(title: 'مراسلة المطور', text: 'البريد: fastunlocked2017@gmail.com'),
        FilledButton.icon(
          onPressed: copyDeveloperMessage,
          icon: const Icon(Icons.copy),
          label: const Text('نسخ رسالة للمطور'),
          style: FilledButton.styleFrom(
            backgroundColor: const Color(0xFF16A34A),
            padding: const EdgeInsets.symmetric(vertical: 16),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          ),
        ),
      ],
    );
  }
}

class HeroHeader extends StatelessWidget {
  const HeroHeader({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: LinearGradient(colors: [const Color(0xFF16A34A), const Color(0xFF16A34A).withValues(alpha: .78)]),
        borderRadius: BorderRadius.circular(30),
        boxShadow: [BoxShadow(color: const Color(0xFF16A34A).withValues(alpha: .25), blurRadius: 22, offset: const Offset(0, 10))],
      ),
      child: Row(
        children: [
          Container(
            width: 70,
            height: 70,
            alignment: Alignment.center,
            decoration: BoxDecoration(color: Colors.white.withValues(alpha: .18), borderRadius: BorderRadius.circular(24)),
            child: const Text("🌍", style: TextStyle(fontSize: 34)),
          ),
          const SizedBox(width: 16),
          const Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text("قاموس تفاعلي", style: TextStyle(fontSize: 23, fontWeight: FontWeight.w900, color: Colors.white)),
                SizedBox(height: 6),
                Text("تعلم كلمات عربية وإنجليزية بطريقة بسيطة", style: TextStyle(fontSize: 14, color: Colors.white, height: 1.5)),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class OutputCard extends StatelessWidget {
  final String text;
  final VoidCallback onCopy;
  const OutputCard({super.key, required this.text, required this.onCopy});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: const Color(0xFFDCFCE7)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(Icons.article_outlined),
              const SizedBox(width: 8),
              const Expanded(child: Text('النتيجة', style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold))),
              IconButton(onPressed: onCopy, icon: const Icon(Icons.copy)),
            ],
          ),
          const SizedBox(height: 8),
          SelectableText(text, style: const TextStyle(fontSize: 16, height: 1.7)),
        ],
      ),
    );
  }
}

class InfoCard extends StatelessWidget {
  final String title;
  final String text;
  const InfoCard({super.key, required this.title, required this.text});

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(color: Colors.white, borderRadius: BorderRadius.circular(22)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        const SizedBox(height: 8),
        Text(text, style: const TextStyle(fontSize: 15, height: 1.6)),
      ]),
    );
  }
}
