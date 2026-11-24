package com.example.data.local

import android.content.Context
import com.example.data.file.QuizFileCodec
import com.example.model.DifficultyLevel
import com.example.model.QuizCategoryId

object BundledQuestionBank {

    const val ASSET_FILE_NAME = "soal_kuis.json"

    fun loadFromAssets(context: Context): List<QuestionEntity> {
        return runCatching {
            val jsonText = context.assets.open(ASSET_FILE_NAME).bufferedReader().use { it.readText() }
            val parsed = QuizFileCodec.parseQuestionsFromJson(jsonText)
            if (parsed.isNotEmpty()) parsed else allQuestions
        }.getOrElse {
            allQuestions
        }
    }

    private fun q(
        id: Int,
        category: QuizCategoryId,
        difficulty: DifficultyLevel,
        prompt: String,
        emojiCue: String,
        options: List<String>,
        correctIndex: Int,
        funFact: String
    ) = QuestionEntity(
        id = id,
        categoryName = category.name,
        difficultyName = difficulty.name,
        prompt = prompt,
        emojiCue = emojiCue,
        optionA = options[0],
        optionB = options[1],
        optionC = options[2],
        optionD = options[3],
        correctIndex = correctIndex,
        funFact = funFact
    )

    val allQuestions: List<QuestionEntity> = listOf(
        // ==================== DUNIA HEWAN (1-10) ====================
        q(
            1, QuizCategoryId.ANIMALS, DifficultyLevel.EASY,
            "Hewan mana yang memiliki belalai panjang untuk minum dan mengambil makanan?",
            "🐘",
            listOf("Gajah", "Jerapah", "Kuda Nil", "Singa"),
            0,
            "Belalai gajah memiliki lebih dari 40.000 otot sehingga bisa mengangkat barang berat sekaligus memetik daun kecil!"
        ),
        q(
            2, QuizCategoryId.ANIMALS, DifficultyLevel.EASY,
            "Suara hewan apakah yang berbunyi 'Meong' dan suka mengejar benang wol?",
            "🐱",
            listOf("Anjing", "Kucing", "Bebek", "Sapi"),
            1,
            "Kucing menggunakan kumisnya yang sensitif untuk mengukur lebar celah sebelum masuk ke dalamnya."
        ),
        q(
            3, QuizCategoryId.ANIMALS, DifficultyLevel.EASY,
            "Hewan laut manakah yang memiliki delapan lengan dan bisa menyemprotkan tinta?",
            "🐙",
            listOf("Lumba-lumba", "Gurita", "Penyu", "Kuda Laut"),
            1,
            "Gurita sangat pintar dan memiliki tiga jantung serta darah yang berwarna biru!"
        ),
        q(
            4, QuizCategoryId.ANIMALS, DifficultyLevel.EASY,
            "Burung manakah yang aktif terbang di malam hari dan matanya besar?",
            "🦉",
            listOf("Burung Hantu", "Ayam", "Merpati", "Flamingo"),
            0,
            "Burung hantu bisa memutar kepalanya hampir 270 derajat untuk melihat ke sekeliling!"
        ),
        q(
            5, QuizCategoryId.ANIMALS, DifficultyLevel.EASY,
            "Hewan apakah yang membawa cangkang keras di punggungnya sebagai rumah?",
            "🐢",
            listOf("Katak", "Kura-kura", "Kelinci", "Tupai"),
            1,
            "Tempurung kura-kura tersambung dengan tulang rusuknya sehingga tidak bisa dilepas."
        ),
        q(
            6, QuizCategoryId.ANIMALS, DifficultyLevel.CHALLENGING,
            "Hewan darat manakah yang bisa berlari paling cepat di dunia?",
            "🐆",
            listOf("Cheetah", "Kangguru", "Rusa", "Zebra"),
            0,
            "Cheetah dapat berlari hingga kecepatan 110 km/jam hanya dalam beberapa detik!"
        ),
        q(
            7, QuizCategoryId.ANIMALS, DifficultyLevel.CHALLENGING,
            "Mamalia laut besar apakah yang bernapas dengan paru-paru dan bernyanyi di dalam laut?",
            "🐋",
            listOf("Hiu Putih", "Paus Bungkuk", "Ikan Pari", "Ubur-ubur"),
            1,
            "Paus bukan ikan melainkan mamalia, sehingga ia harus muncul ke permukaan laut untuk menghirup udara."
        ),
        q(
            8, QuizCategoryId.ANIMALS, DifficultyLevel.CHALLENGING,
            "Serangga manakah yang mengalami metamorfosis sempurna dari ulat menjadi kepompong?",
            "🦋",
            listOf("Belalang", "Kupu-kupu", "Jangkrik", "Capung"),
            1,
            "Kupu-kupu mencicipi rasa nektar bunga menggunakan sensor di kakinya!"
        ),
        q(
            9, QuizCategoryId.ANIMALS, DifficultyLevel.CHALLENGING,
            "Kadal purba raksasa asli Indonesia yang hidup di Nusa Tenggara Timur adalah...",
            "🦎",
            listOf("Iguana", "Komodo", "Bunglon", "Tokek"),
            1,
            "Komodo adalah kadal terbesar di dunia yang hanya hidup alami di Pulau Komodo dan sekitarnya."
        ),
        q(
            10, QuizCategoryId.ANIMALS, DifficultyLevel.CHALLENGING,
            "Lebah mengumpulkan nektar bunga untuk membuat cairan manis yang disebut...",
            "🐝",
            listOf("Sirup", "Madu", "Susu", "Getah"),
            1,
            "Untuk membuat satu sendok makan madu, lebah harus mengunjungi ribuan bunga!"
        ),

        // ==================== HITUNG CERIA (11-20) ====================
        q(
            11, QuizCategoryId.MATH, DifficultyLevel.EASY,
            "Rara punya 3 apel merah dan mendapat 4 apel lagi. Berapa jumlah semua apel Rara?",
            "🍎",
            listOf("6 apel", "7 apel", "8 apel", "5 apel"),
            1,
            "3 ditambah 4 sama dengan 7. Menghitung dengan jari membantu kita belajar penjumlahan!"
        ),
        q(
            12, QuizCategoryId.MATH, DifficultyLevel.EASY,
            "Ada 9 balon warna-warni, lalu 2 balon terbang ke langit. Berapa balon yang tersisa?",
            "🎈",
            listOf("6 balon", "7 balon", "8 balon", "5 balon"),
            1,
            "9 dikurangi 2 sama dengan 7. Pengurangan berarti menghitung sisa setelah sebagian diambil."
        ),
        q(
            13, QuizCategoryId.MATH, DifficultyLevel.EASY,
            "Berapakah hasil dari 5 + 5?",
            "🖐️",
            listOf("8", "9", "10", "12"),
            2,
            "Dua tangan kita masing-masing punya 5 jari. Jika digabungkan semuanya ada 10 jari!"
        ),
        q(
            14, QuizCategoryId.MATH, DifficultyLevel.EASY,
            "Angka berapakah yang datang tepat setelah angka 14?",
            "🔢",
            listOf("13", "15", "16", "12"),
            1,
            "Urutan hitung maju setelah 14 adalah 15, lalu 16, 17, dan seterusnya!"
        ),
        q(
            15, QuizCategoryId.MATH, DifficultyLevel.EASY,
            "Jika satu mobil mainan punya 4 roda, berapa roda yang dimiliki 2 mobil mainan?",
            "🚗",
            listOf("6 roda", "8 roda", "10 roda", "4 roda"),
            1,
            "4 + 4 = 8 roda! Penjumlahan berulang adalah dasar dari perkalian."
        ),
        q(
            16, QuizCategoryId.MATH, DifficultyLevel.CHALLENGING,
            "Berapakah hasil dari 3 x 6?",
            "✖️",
            listOf("15", "18", "21", "12"),
            1,
            "3 x 6 artinya 6 + 6 + 6, yang hasilnya adalah 18!"
        ),
        q(
            17, QuizCategoryId.MATH, DifficultyLevel.CHALLENGING,
            "Bibo punya 24 permen yang ingin dibagikan rata kepada 4 temannya. Berapa permen per anak?",
            "🍬",
            listOf("5 permen", "6 permen", "7 permen", "8 permen"),
            1,
            "24 dibagi 4 sama dengan 6, karena 4 x 6 = 24!"
        ),
        q(
            18, QuizCategoryId.MATH, DifficultyLevel.CHALLENGING,
            "Berapakah hasil dari 45 - 17?",
            "➖",
            listOf("28", "32", "27", "38"),
            0,
            "45 - 10 = 35, lalu 35 - 7 = 28. Memecah angka puluhan dan satuan bikin berhitung lebih cepat!"
        ),
        q(
            19, QuizCategoryId.MATH, DifficultyLevel.CHALLENGING,
            "Dalam 1 jam ada berapa menit?",
            "⏰",
            listOf("30 menit", "60 menit", "90 menit", "100 menit"),
            1,
            "Jarum panjang jam berputar satu lingkaran penuh sebanyak 60 menit untuk menyelesaikan 1 jam."
        ),
        q(
            20, QuizCategoryId.MATH, DifficultyLevel.CHALLENGING,
            "Pola angka berikutnya: 5, 10, 15, 20, ... Angka apakah yang hilang?",
            "🎯",
            listOf("22", "25", "30", "24"),
            1,
            "Ini adalah pola lompat 5! Setiap langkah bertambah 5 sehingga setelah 20 adalah 25."
        ),

        // ==================== ALAM & SAINS (21-30) ====================
        q(
            21, QuizCategoryId.SCIENCE, DifficultyLevel.EASY,
            "Benda langit apakah yang memberi cahaya hangat di siang hari?",
            "☀️",
            listOf("Bulan", "Matahari", "Komet", "Awan"),
            1,
            "Matahari adalah bintang raksasa terdekat dari Bumi yang membuat tumbuhan bisa tumbuh subur!"
        ),
        q(
            22, QuizCategoryId.SCIENCE, DifficultyLevel.EASY,
            "Apa yang kita butuhkan dari udara agar paru-paru kita bisa bernapas?",
            "🌬️",
            listOf("Oksigen", "Asap", "Pasir", "Garam"),
            0,
            "Pohon dan tumbuhan hijau menghasilkan oksigen segar yang kita hirup setiap hari."
        ),
        q(
            23, QuizCategoryId.SCIENCE, DifficultyLevel.EASY,
            "Saat air dipanaskan hingga mendidih, air berubah menjadi...",
            "💨",
            listOf("Es batu", "Uap air", "Salju", "Batu"),
            1,
            "Air punya tiga wujud: padat (es), cair (air minum), dan gas (uap air)."
        ),
        q(
            24, QuizCategoryId.SCIENCE, DifficultyLevel.EASY,
            "Bagian tumbuhan yang menyerap air dan mineral dari dalam tanah adalah...",
            "🌱",
            listOf("Bunga", "Akar", "Buah", "Biji"),
            1,
            "Akar bekerja seperti sedotan alami yang kuat sekaligus menjaga pohon tetap berdiri kokoh."
        ),
        q(
            25, QuizCategoryId.SCIENCE, DifficultyLevel.EASY,
            "Setelah hujan reda dan matahari bersinar, sering muncul lengkungan warna-warni bernama...",
            "🌈",
            listOf("Pelangi", "Petir", "Kabut", "Badai"),
            0,
            "Pelangi muncul ketika cahaya matahari memantul dan terurai melewati tetesan air hujan!"
        ),
        q(
            26, QuizCategoryId.SCIENCE, DifficultyLevel.CHALLENGING,
            "Planet manakah di tata surya kita yang terkenal memiliki cincin indah besar?",
            "🪐",
            listOf("Mars", "Saturnus", "Merkurius", "Venus"),
            1,
            "Cincin Saturnus terbuat dari miliaran bongkahan es, debu, dan batu yang mengorbit planet!"
        ),
        q(
            27, QuizCategoryId.SCIENCE, DifficultyLevel.CHALLENGING,
            "Gaya apakah yang membuat bola jatuh kembali ke bawah saat kita lempar ke atas?",
            "🌍",
            listOf("Gaya Magnet", "Gaya Gravitasi", "Gaya Gesek", "Gaya Listrik"),
            1,
            "Gravitasi Bumi menarik semua benda ke arah pusat Bumi agar kita tidak melayang di udara."
        ),
        q(
            28, QuizCategoryId.SCIENCE, DifficultyLevel.CHALLENGING,
            "Planet yang dijuluki 'Planet Merah' karena tanahnya kaya zat besi berkarat adalah...",
            "🔴",
            listOf("Jupiter", "Mars", "Neptunus", "Uranus"),
            1,
            "Para ilmuwan sudah mengirim robot penjelajah seperti Perseverance untuk meneliti permukaan Mars!"
        ),
        q(
            29, QuizCategoryId.SCIENCE, DifficultyLevel.CHALLENGING,
            "Proses tumbuhan hijau membuat makanannya sendiri dengan bantuan sinar matahari disebut...",
            "🌿",
            listOf("Fotosintesis", "Evaporasi", "Migrasi", "Hibernasi"),
            0,
            "Daun berwarna hijau karena mengandung klorofil yang menangkap cahaya matahari untuk fotosintesis."
        ),
        q(
            30, QuizCategoryId.SCIENCE, DifficultyLevel.CHALLENGING,
            "Alat yang digunakan untuk melihat benda-benda luar angkasa yang sangat jauh adalah...",
            "🔭",
            listOf("Mikroskop", "Teleskop", "Termometer", "Kompas"),
            1,
            "Dengan teleskop, kita bisa melihat kawah Bulan dan cincin Saturnus dengan lebih jelas!"
        ),

        // ==================== WARNA & BENTUK (31-40) ====================
        q(
            31, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.EASY,
            "Jika cat warna MERAH dicampur dengan KUNING, warna apa yang akan muncul?",
            "🎨",
            listOf("Ungu", "Oranye", "Hijau", "Biru"),
            1,
            "Merah + Kuning = Oranye! Merah, kuning, dan biru disebut warna primer."
        ),
        q(
            32, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.EASY,
            "Bentuk bangun datar apakah yang tidak memiliki sudut sama sekali seperti roda?",
            "⭕",
            listOf("Segitiga", "Persegi", "Lingkaran", "Trapesium"),
            2,
            "Lingkaran memiliki sisi lengkung yang menyatu sempurna sehingga bisa menggelinding lancar!"
        ),
        q(
            33, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.EASY,
            "Berapakah jumlah sisi dan sudut yang dimiliki oleh sebuah Segitiga?",
            "🔺",
            listOf("2 sisi", "3 sisi", "4 sisi", "5 sisi"),
            1,
            "Sesuai namanya, segi-tiga selalu mempunyai 3 sisi lurus dan 3 titik sudut!"
        ),
        q(
            34, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.EASY,
            "Jika cat BIRU dicampur dengan KUNING, warna segar apakah yang tercipta?",
            "💚",
            listOf("Hijau", "Cokelat", "Merah Muda", "Abu-abu"),
            0,
            "Biru + Kuning menghasilkan warna Hijau seperti warna daun dan rumput di taman!"
        ),
        q(
            35, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.EASY,
            "Lampu lalu lintas yang berwarna HIJAU memberi tanda kepada kendaraan untuk...",
            "🚦",
            listOf("Berhenti", "Bersiap", "Jalan Terus", "Putar Balik"),
            2,
            "Merah berarti berhenti, kuning berarti bersiap-hati-hati, dan hijau berarti jalan!"
        ),
        q(
            36, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.CHALLENGING,
            "Bangun ruang berbentuk kotak yang memiliki 6 sisi persegi sama besar seperti dadu disebut...",
            "🎲",
            listOf("Bola", "Kubus", "Kerucut", "Tabung"),
            1,
            "Kubus memiliki 6 sisi persegi yang sama luas, 12 rusuk, dan 8 titik sudut."
        ),
        q(
            37, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.CHALLENGING,
            "Bentuk apakah yang paling mirip dengan topi ulang tahun atau cone es krim?",
            "🥳",
            listOf("Kerucut", "Prisma", "Balok", "Lingkaran"),
            0,
            "Kerucut memiliki alas berbentuk lingkaran dan ujung atas yang runcing!"
        ),
        q(
            38, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.CHALLENGING,
            "Ada berapa warna utama pada pelangi menurut urutan ME-JI-KU-HI-BI-NI-U?",
            "🌈",
            listOf("5 warna", "6 warna", "7 warna", "8 warna"),
            2,
            "7 warna pelangi adalah Merah, Jingga, Kuning, Hijau, Biru, Nila, dan Ungu!"
        ),
        q(
            39, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.CHALLENGING,
            "Bangun datar yang memiliki 5 sisi sama panjang seperti simbol bintang atau rumah kecil adalah...",
            "⬠",
            listOf("Segi Empat", "Segi Lima (Pentagon)", "Segi Enam", "Segi Delapan"),
            1,
            "Pentagon adalah bangun segi lima. Sarang lebah madu berbentuk heksagon atau segi enam!"
        ),
        q(
            40, QuizCategoryId.COLORS_SHAPES, DifficultyLevel.CHALLENGING,
            "Campuran warna MERAH dan BIRU akan menghasilkan warna yang mana?",
            "🍇",
            listOf("Ungu", "Kuning", "Hijau", "Putih"),
            0,
            "Merah + Biru = Ungu! Warna ungu sering terlihat pada buah anggur dan bunga lavender."
        ),

        // ==================== BUAH & SAYUR (41-50) ====================
        q(
            41, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.EASY,
            "Sayuran berwarna oranye yang tumbuh di dalam tanah dan sangat baik untuk mata adalah...",
            "🥕",
            listOf("Bayam", "Wortel", "Mentimun", "Kubis"),
            1,
            "Wortel kaya akan vitamin A dan beta-karoten yang membantu mata melihat lebih jernih!"
        ),
        q(
            42, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.EASY,
            "Buah tropis berkulit kuning melengkung yang menjadi favorit monyet adalah...",
            "🍌",
            listOf("Pisang", "Semangka", "Stroberi", "Anggur"),
            0,
            "Pisang mengandung kalium yang memberi energi cepat untuk berlari dan bermain!"
        ),
        q(
            43, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.EASY,
            "Buah besar berkulit hijau bergaris yang bagian dalamnya merah berair dan manis adalah...",
            "🍉",
            listOf("Melon", "Semangka", "Alpukat", "Nanas"),
            1,
            "Lebih dari 90% isi semangka adalah air, jadi sangat menyegarkan saat cuaca panas!"
        ),
        q(
            44, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.EASY,
            "Buah kecil berwarna merah dengan bintik biji di luar kulitnya adalah...",
            "🍓",
            listOf("Ceri", "Stroberi", "Apel", "Jeruk"),
            1,
            "Stroberi adalah satu-satunya buah yang bijinya berada di bagian luar kulitnya!"
        ),
        q(
            45, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.EASY,
            "Sayuran hijau yang bentuknya mirip pohon mini adalah...",
            "🥦",
            listOf("Brokoli", "Terong", "Jagung", "Bawang"),
            0,
            "Brokoli adalah bunga sayuran yang kaya serat dan vitamin C agar tubuh tidak mudah sakit!"
        ),
        q(
            46, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.CHALLENGING,
            "Buah jeruk dan lemon terkenal sangat kaya akan vitamin apa?",
            "🍊",
            listOf("Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K"),
            1,
            "Vitamin C membantu daya tahan tubuh melawan flu dan mempercepat penyembuhan luka!"
        ),
        q(
            47, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.CHALLENGING,
            "Buah asal Indonesia yang dijuluki 'Raja Buah' dan berkulit duri tajam adalah...",
            "🍈",
            listOf("Rambutan", "Durian", "Salak", "Manggis"),
            1,
            "Sementara durian dijuluki Raja Buah, manggis sering dijuluki sebagai Ratu Buah!"
        ),
        q(
            48, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.CHALLENGING,
            "Bagian manakah dari tanaman kentang yang biasa kita masak dan makan?",
            "🥔",
            listOf("Bunganya", "Umbi batangnya", "Daunnya", "Kulit kayunya"),
            1,
            "Kentang tumbuh di bawah tanah sebagai umbi yang menyimpan cadangan energi pati."
        ),
        q(
            49, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.CHALLENGING,
            "Buah berkulit cokelat bersisik seperti ular asli Indonesia adalah...",
            "🌴",
            listOf("Salak", "Sawo", "Duku", "Lengkeng"),
            0,
            "Di luar negeri salak dikenal sebagai 'snake fruit' karena pola sisik kulitnya!"
        ),
        q(
            50, QuizCategoryId.FRUITS_VEGGIES, DifficultyLevel.CHALLENGING,
            "Mengapa kita dianjurkan makan sayur hijau seperti bayam setiap hari?",
            "🥬",
            listOf("Mengandung zat besi & serat", "Agar cepat mengantuk", "Karena rasanya asin", "Membuat gigi berwarna hijau"),
            0,
            "Zat besi dalam bayam membantu sel darah membawa oksigen ke seluruh tubuh dan otak!"
        ),

        // ==================== NUSANTARA KITA (51-60) ====================
        q(
            51, QuizCategoryId.INDONESIA, DifficultyLevel.EASY,
            "Apa dua warna pada bendera kebangsaan Indonesia, Sang Saka Merah Putih?",
            "🇮🇩",
            listOf("Merah dan Kuning", "Merah dan Putih", "Biru dan Putih", "Hijau dan Putih"),
            1,
            "Warna merah melambangkan keberanian, sedangkan warna putih melambangkan kesucian hati!"
        ),
        q(
            52, QuizCategoryId.INDONESIA, DifficultyLevel.EASY,
            "Apakah lagu kebangsaan negara Indonesia?",
            "🎶",
            listOf("Indonesia Raya", "Garuda Pancasila", "Halo-Halo Bandung", "Tanah Airku"),
            0,
            "Lagu Indonesia Raya diciptakan oleh Wage Rudolf Supratman dan dikumandangkan pertama kali tahun 1928."
        ),
        q(
            53, QuizCategoryId.INDONESIA, DifficultyLevel.EASY,
            "Burung mitologi yang menjadi lambang negara Indonesia adalah...",
            "🦅",
            listOf("Burung Cenderawasih", "Burung Garuda", "Burung Elang", "Burung Merak"),
            1,
            "Di dada Garuda Pancasila terdapat perisai berisi lima lambang sila Pancasila!"
        ),
        q(
            54, QuizCategoryId.INDONESIA, DifficultyLevel.EASY,
            "Alat musik tradisional dari Jawa Barat yang terbuat dari bambu dan dimainkan dengan digoyangkan adalah...",
            "🎋",
            listOf("Angklung", "Gamelan", "Sasando", "Tifa"),
            0,
            "Angklung telah diakui oleh UNESCO sebagai warisan budaya dunia dari Indonesia!"
        ),
        q(
            55, QuizCategoryId.INDONESIA, DifficultyLevel.EASY,
            "Kain tradisional Indonesia yang dilukis dengan lilin malam dan canting adalah...",
            "👘",
            listOf("Batik", "Sutra", "Lurik", "Ulos"),
            0,
            "Setiap tanggal 2 Oktober, kita merayakan Hari Batik Nasional!"
        ),
        q(
            56, QuizCategoryId.INDONESIA, DifficultyLevel.CHALLENGING,
            "Semboyan bangsa Indonesia yang berarti 'Berbeda-beda tetapi tetap satu jua' adalah...",
            "🤝",
            listOf("Tut Wuri Handayani", "Bhinneka Tunggal Ika", "Bersatu Kita Teguh", "Pancasila Sakti"),
            1,
            "Semboyan Bhinneka Tunggal Ika tertulis pada pita putih yang dicengkeram oleh Burung Garuda."
        ),
        q(
            57, QuizCategoryId.INDONESIA, DifficultyLevel.CHALLENGING,
            "Rumah adat dengan atap melengkung runcing seperti tanduk kerbau dari Sumatera Barat disebut...",
            "🏠",
            listOf("Rumah Joglo", "Rumah Gadang", "Rumah Honai", "Rumah Tongkonan"),
            1,
            "Rumah Gadang dibangun tanpa paku besi, melainkan menggunakan pasak kayu yang kuat!"
        ),
        q(
            58, QuizCategoryId.INDONESIA, DifficultyLevel.CHALLENGING,
            "Tanggal berapakah Hari Kemerdekaan Republik Indonesia diperingati setiap tahun?",
            "🎉",
            listOf("17 Agustus", "28 Oktober", "10 November", "1 Juni"),
            0,
            "Proklamasi kemerdekaan Indonesia dibacakan oleh Ir. Soekarno pada 17 Agustus 1945."
        ),
        q(
            59, QuizCategoryId.INDONESIA, DifficultyLevel.CHALLENGING,
            "Candi Buddha terbesar di dunia yang terletak di Magelang, Jawa Tengah adalah...",
            "🛕",
            listOf("Candi Prambanan", "Candi Borobudur", "Candi Mendut", "Candi Muaro Jambi"),
            1,
            "Candi Borobudur tersusun dari jutaan balok batu vulkanik dan memiliki 2.672 panel relief!"
        ),
        q(
            60, QuizCategoryId.INDONESIA, DifficultyLevel.CHALLENGING,
            "Burung indah dari Papua yang sering dijuluki sebagai 'Burung Surga' adalah...",
            "🪶",
            listOf("Jalak Bali", "Cenderawasih", "Maleo", "Rangkong"),
            1,
            "Burung Cenderawasih jantan memiliki bulu ekor berwarna cerah memukau untuk menari!"
        )
    )
}
