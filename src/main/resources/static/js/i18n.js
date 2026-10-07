// HLSP i18n: EN <-> Hindi (Devanagari) <-> Hinglish (Roman). Persisted in localStorage hlsp-lang.
// Server also honours ?lang=hi via Spring MessageSource for SEO/first paint; this layer
// translates data-i18n elements instantly without a full reload.
window.HLSPLang = (() => {
  const DICT = {
    hi: {
      "Home": "होम", "Services": "सर्विसेज़", "Book Service": "सर्विस बुक करें", "About": "हमारे बारे में",
      "Contact": "संपर्क करें", "Log in": "लॉग इन", "Sign up": "साइन अप", "My Bookings": "मेरी बुकिंग",
      "Your home deserves the best care": "आपका घर सबसे अच्छी देखभाल का हक़दार है",
      "What do you need help with?": "आपको किस चीज़ में मदद चाहिए?", "Search": "खोजें",
      "Add to cart": "कार्ट में जोड़ें", "View pros": "प्रो देखें", "View all services": "सभी सर्विसेज़ देखें",
      "How it works": "यह कैसे काम करता है", "Set your location": "अपनी लोकेशन सेट करें",
      "Use current location": "मौजूदा लोकेशन इस्तेमाल करें", "Choose location": "लोकेशन चुनें",
      "Book trusted, background-verified professionals for cleaning, repairs, wellness and more — all at your doorstep.":
        "सफ़ाई, रिपेयर, वेलनेस और बहुत कुछ के लिए भरोसेमंद, बैकग्राउंड-वेरिफ़ाइड प्रोफ़ेशनल बुक करें - सीधे आपके दरवाज़े पर।"
    },
    hinglish: {
      "Home": "Home", "Services": "Services", "Book Service": "Service book karo", "About": "Hamare baare mein",
      "Contact": "Sampark karo", "Log in": "Login", "Sign up": "Sign up karo", "My Bookings": "Meri bookings",
      "Your home deserves the best care": "Aapka ghar sabse achhi care deserve karta hai",
      "What do you need help with?": "Aapko kis cheez mein madad chahiye?", "Search": "Search karo",
      "Add to cart": "Cart mein daalo", "View pros": "Pro dekho", "View all services": "Saari services dekho",
      "How it works": "Ye kaise kaam karta hai", "Set your location": "Apni location set karo",
      "Use current location": "Abhi wali location use karo", "Choose location": "Location chuno",
      "Book trusted, background-verified professionals for cleaning, repairs, wellness and more — all at your doorstep.":
        "Cleaning, repair, wellness aur bohot kuch ke liye trusted, background-verified professionals book karo - seedha aapke darwaze par."
    }
  };

  function get() { return localStorage.getItem("hlsp-lang") || "en"; }

  function apply(lang) {
    localStorage.setItem("hlsp-lang", lang);
    document.documentElement.lang = lang === "en" ? "en" : "hi";
    if (lang === "en") { document.querySelectorAll("[data-i18n-en]").forEach(el => { el.textContent = el.dataset.i18nEn; }); syncToggle(lang); return; }
    const dict = DICT[lang] || DICT.hi;
    document.querySelectorAll("[data-i18n-en]").forEach(el => {
      const en = el.dataset.i18nEn;
      if (dict[en]) el.textContent = dict[en];
    });
    document.querySelectorAll("input[data-i18n-ph-en]").forEach(el => {
      const en = el.dataset.i18nPhEn; if (dict[en]) el.placeholder = dict[en];
    });
    syncToggle(lang);
    // keep server locale cookie in sync for Thymeleaf #{...} strings on next navigation
    document.cookie = "hlsp-lang=" + (lang === "hi" ? "hi" : "en") + ";path=/;max-age=31536000";
  }

  function syncToggle(lang) {
    document.querySelectorAll(".hlsp-lang-toggle").forEach(b => {
      b.textContent = lang === "hi" ? "English" : lang === "hinglish" ? "हिंदी" : "हिंदी | Hinglish";
      b.dataset.lang = lang;
    });
  }

  function cycle(el) {
    const cur = get();
    const next = cur === "en" ? "hi" : cur === "hi" ? "hinglish" : "en";
    apply(next);
  }

  document.addEventListener("DOMContentLoaded", () => {
    // stamp English originals for instant switch-back
    document.querySelectorAll("nav a, .side-menu-link, .hero h1, .hero p, .search-form button, .service-card-actions .btn, .section-title, .how-it-works h2")
      .forEach(el => { if (!el.dataset.i18nEn && el.textContent.trim()) el.dataset.i18nEn = el.textContent.trim(); });
    const ph = document.getElementById("searchInput");
    if (ph) ph.dataset.i18nPhEn = ph.placeholder;
    apply(get());
  });

  return { get, apply, cycle };
})();
