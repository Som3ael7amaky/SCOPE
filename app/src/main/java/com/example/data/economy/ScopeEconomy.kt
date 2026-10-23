package com.example.data.economy

import com.example.data.model.ShopCategory
import com.example.data.model.ShopItem
import com.example.data.model.StudentBadge

object ScopeEconomy {
    val storeCatalog: List<ShopItem> = listOf(
        // Frames
        ShopItem(
            id = "frame_cyber_neon",
            name = "إطار النيون السيبراني ⚡",
            category = ShopCategory.FRAME,
            description = "توهج أزرق هولوغرافي متدفق مع زوايا نيون نشطة وتأثير نبض ضوئي",
            sparksPrice = 80,
            tokensPrice = 0,
            previewColorHex = "#06B6D4",
            rarity = "نادر"
        ),
        ShopItem(
            id = "frame_dev_crown_holo",
            name = "تاج المطور الهولوجرامي 👑",
            category = ShopCategory.FRAME,
            description = "إطار هولوغرافي أسطوري مع إشعاع ذهبي وتاج متلألئ للأوائل والمطورين",
            sparksPrice = 300,
            tokensPrice = 30,
            previewColorHex = "#FFD700",
            rarity = "أسطوري"
        ),
        ShopItem(
            id = "frame_celestial_gold",
            name = "إطار الذهب السماوي",
            category = ShopCategory.FRAME,
            description = "حواف مذهبة ملكية مع جزيئات ضوء براقة",
            sparksPrice = 0,
            tokensPrice = 15,
            previewColorHex = "#F59E0B",
            rarity = "أسطوري"
        ),
        ShopItem(
            id = "frame_holo_prism",
            name = "منشور الهولو بريزم",
            category = ShopCategory.FRAME,
            description = "طيف ألوان ديناميكي متدرج يعكس زوايا الضوء",
            sparksPrice = 120,
            tokensPrice = 5,
            previewColorHex = "#8B5CF6",
            rarity = "نادر"
        ),
        ShopItem(
            id = "frame_emerald_scholar",
            name = "زمرد الباحث",
            category = ShopCategory.FRAME,
            description = "توهج زمردي هادئ يعبر عن التركيز والاتزان",
            sparksPrice = 60,
            tokensPrice = 0,
            previewColorHex = "#10B981",
            rarity = "شائع"
        ),
        ShopItem(
            id = "frame_obsidian_elite",
            name = "أوبسيديان إيليت 3D",
            category = ShopCategory.FRAME,
            description = "إطار بلوري أسود بركاني فائق الفخامة",
            sparksPrice = 220,
            tokensPrice = 20,
            previewColorHex = "#312E81",
            rarity = "أسطوري"
        ),

        // Auras
        ShopItem(
            id = "aura_solar_amber",
            name = "وهج الشمس الذهبي",
            category = ShopCategory.AURA,
            description = "هالة عنبرية مشرقة تنبض بالحيوية حول صولا",
            sparksPrice = 50,
            tokensPrice = 0,
            previewColorHex = "#F59E0B",
            rarity = "شائع"
        ),
        ShopItem(
            id = "aura_mental_energy_fire",
            name = "هالة الطاقة الذهنية 🔥",
            category = ShopCategory.AURA,
            description = "شعلة نار ذهبية متوهجة تحيط بالبطاقة وتجسد الطاقة الذهنية المتوقدة",
            sparksPrice = 180,
            tokensPrice = 15,
            previewColorHex = "#EF4444",
            rarity = "أسطوري"
        ),
        ShopItem(
            id = "aura_quantum_violet",
            name = "الهالة الكوانتية البنفسجية",
            category = ShopCategory.AURA,
            description = "إشعاع بنفسجي عميق يوحي بالتفكير الحسابي الخارق",
            sparksPrice = 100,
            tokensPrice = 0,
            previewColorHex = "#7C3AED",
            rarity = "نادر"
        ),
        ShopItem(
            id = "aura_cyberpunk_cyan",
            name = "سايبربانك سيان بلص",
            category = ShopCategory.AURA,
            description = "حلقات كهرومغناطيسية تركوازية فائقة الوضوح",
            sparksPrice = 0,
            tokensPrice = 10,
            previewColorHex = "#06B6D4",
            rarity = "نادر"
        ),
        ShopItem(
            id = "aura_emerald_matrix",
            name = "ماتريكس الزمرد الحي",
            category = ShopCategory.AURA,
            description = "جزيئات طاقة خضراء متصاعدة ترمز لقوة التعلم",
            sparksPrice = 0,
            tokensPrice = 14,
            previewColorHex = "#059669",
            rarity = "أسطوري"
        ),

        // Titles
        ShopItem(
            id = "title_algo_architect",
            name = "مهندس الخوارزميات",
            category = ShopCategory.TITLE,
            description = "لقب رسمي للطلاب المتميزين في حل المشكلات الهندسية",
            sparksPrice = 0,
            tokensPrice = 0,
            previewColorHex = "#2563EB",
            rarity = "شائع"
        ),
        ShopItem(
            id = "title_focus_master",
            name = "سيد التركيز",
            category = ShopCategory.TITLE,
            description = "شارة تدل على إتمام ساعات تركيز متواصلة دون تشتت",
            sparksPrice = 140,
            tokensPrice = 5,
            previewColorHex = "#10B981",
            rarity = "نادر"
        ),
        ShopItem(
            id = "title_streak_legend",
            name = "صائد الإنجازات",
            category = ShopCategory.TITLE,
            description = "يُمنح للطلاب الملتزمين بالحفاظ على شعلة الدراسة",
            sparksPrice = 110,
            tokensPrice = 8,
            previewColorHex = "#F97316",
            rarity = "نادر"
        ),
        ShopItem(
            id = "title_scope_luminary",
            name = "نجم SCOPE الأسطوري",
            category = ShopCategory.TITLE,
            description = "أعلى الألقاب التقديرية في منظومة SCOPE الذكية",
            sparksPrice = 0,
            tokensPrice = 25,
            previewColorHex = "#F59E0B",
            rarity = "أسطوري"
        ),

        // Glass FX
        ShopItem(
            id = "glass_holographic",
            name = "زجاج هولوغرافي متدرج",
            category = ShopCategory.GLASS_FX,
            description = "شفافية زجاجية مع انعكاسات ضوئية خافتة فائقة النعومة",
            sparksPrice = 70,
            tokensPrice = 0,
            previewColorHex = "#38BDF8",
            rarity = "شائع"
        ),
        ShopItem(
            id = "glass_frosted_obsidian",
            name = "أوبسيديان مظلل معتم",
            category = ShopCategory.GLASS_FX,
            description = "مظهر داكن غامر مستوحى من زجاج الهواتف المتقدمة",
            sparksPrice = 150,
            tokensPrice = 8,
            previewColorHex = "#1E293B",
            rarity = "نادر"
        ),
        ShopItem(
            id = "glass_velvet_midnight",
            name = "ميدنايت فيلفيت زجاجي",
            category = ShopCategory.GLASS_FX,
            description = "زجاج ليلي كحلي يبرز عناصر الشاشة بنقاء مذهل",
            sparksPrice = 0,
            tokensPrice = 16,
            previewColorHex = "#312E81",
            rarity = "أسطوري"
        )
    )

    val allBadges: List<StudentBadge> = listOf(
        StudentBadge(
            id = "badge_first_spark",
            title = "الشرارة الأولى ⚡",
            description = "سجلت أول نشاط دراسي وحصدت شرارات التركيز في SCOPE",
            iconEmoji = "⚡",
            criteria = "إكمال مهمة أو جلسة دراسية واحدة"
        ),
        StudentBadge(
            id = "badge_streak_3",
            title = "شعلة الاستمرار 🔥",
            description = "حافظت على سلسلة حضور والتزام دراسي لـ 3 أيام متتالية",
            iconEmoji = "🔥",
            criteria = "شعلة حضور 3 أيام"
        ),
        StudentBadge(
            id = "badge_deep_dive",
            title = "الغواص العميق ⏱️",
            description = "أتممت جلسة تركيز كاملة مع درع الحماية الأكاديمي",
            iconEmoji = "⏱️",
            criteria = "جلسة بومودورو مكتملة"
        ),
        StudentBadge(
            id = "badge_sola_aligned",
            title = "تناغم صولا 🤖",
            description = "أكملت الاستبيان التشخيصي الذكي وخصصت نبرة وأسلوب رفيقتك صولا",
            iconEmoji = "🤖",
            criteria = "إكمال تشخيص صولا"
        ),
        StudentBadge(
            id = "badge_task_crusher",
            title = "قاهر المهام 📋",
            description = "أنجزت 5 مهام دراسية متتالية بكفاءة عالية",
            iconEmoji = "📋",
            criteria = "إنجاز 5 مهام دراسية"
        ),
        StudentBadge(
            id = "badge_night_owl",
            title = "بومة الليل 🌙",
            description = "أنتجت ودرست بتركيز فائق في أوقات الهدوء الليلي",
            iconEmoji = "🌙",
            criteria = "جلسة تركيز بعد العاشرة مساءً"
        ),
        StudentBadge(
            id = "badge_store_collector",
            title = "جامع التحف 🎨",
            description = "اقتنيت أول مظهر أو إطار 3D من متجر التخصيص",
            iconEmoji = "🎨",
            criteria = "شراء عنصر من المتجر"
        )
    )
}
