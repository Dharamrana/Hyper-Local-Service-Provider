// HLSP Maps picker: Rapido-style "Select on map" — fixed centre pin, drag map under it.
// Works without a paid key (OSM via Leaflet), reverse-geocodes with Nominatim.
window.HLSPMaps = (() => {
  const CENTER = { lat: 30.3429, lng: 77.9620, label: "Prem Nagar, Dehradun" };
  const LOCALITIES = [
    { label: "Prem Nagar",   lat: 30.3429, lng: 77.9620, pincode: "248007" },
    { label: "Suddowala",    lat: 30.3350, lng: 77.9550, pincode: "248015" },
    { label: "Kheri Gaon",   lat: 30.3255, lng: 77.9405, pincode: "248001" },
    { label: "Ballupur",     lat: 30.3480, lng: 77.9700, pincode: "248001" },
    { label: "Vasant Vihar", lat: 30.3280, lng: 78.0180, pincode: "248006" },
    { label: "Rajpur Road",  lat: 30.3600, lng: 78.0500, pincode: "248001" },
    { label: "Clement Town", lat: 30.2720, lng: 78.0450, pincode: "248002" },
    { label: "Sahastradhara Road", lat: 30.3750, lng: 78.0700, pincode: "248013" }
  ];
  const DEFAULT = { lat: CENTER.lat, lng: CENTER.lng, city: CENTER.label };

  function nearestLocality(lat, lng) {
    let best = null, bestKm = Infinity;
    for (const l of LOCALITIES) {
      const dLat = (l.lat - lat) * 111, dLng = (l.lng - lng) * 111 * Math.cos(lat * Math.PI / 180);
      const d = Math.sqrt(dLat * dLat + dLng * dLng);
      if (d < bestKm) { bestKm = d; best = l; }
    }
    return best && bestKm <= 15 ? best.label : "Dehradun";
  }

  async function reverseGeocode(lat, lng) {
    try {
      const r = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`, { headers: { "Accept": "application/json" } });
      const j = await r.json();
      return j.display_name || `${nearestLocality(lat, lng)}, Dehradun`;
    } catch { return `${nearestLocality(lat, lng)}, Dehradun · ${lat.toFixed(5)}, ${lng.toFixed(5)}`; }
  }

  function detect() {
    return new Promise((resolve) => {
      if (!navigator.geolocation) return resolve(DEFAULT);
      navigator.geolocation.getCurrentPosition(
        (p) => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
        () => resolve(DEFAULT), { timeout: 5000 });
    });
  }

  // Creates the picker inside #mapId. opts: { latEl, lngEl, addressEl, labelEl, onConfirm }
  function initRapidoPicker(mapId, opts) {
    const el = document.getElementById(mapId);
    if (!el || typeof L === "undefined") return null;

    let startLat = parseFloat((document.getElementById(opts.latEl) || {}).value) || CENTER.lat;
    let startLng = parseFloat((document.getElementById(opts.lngEl) || {}).value) || CENTER.lng;

    const map = L.map(el, { zoomControl: false }).setView([startLat, startLng], 14);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OSM</a>', maxZoom: 19
    }).addTo(map);
    L.control.zoom({ position: "bottomright" }).addTo(map);
    // Fixed centre pin (Rapido pattern: pin stays, map moves under it)
    const pinEl = document.createElement("div"); pinEl.className = "hlsp-center-pin";
    pinEl.innerHTML = '<i class="fas fa-map-marker-alt"></i><span class="hlsp-pin-shadow"></span>';
    el.appendChild(pinEl);

    const labelEl = document.getElementById(opts.labelEl);
    const addrEl = document.getElementById(opts.addressEl);
    let geocodeTimer = null;

    function fmt(lat, lng) { return `${lat.toFixed(5)}, ${lng.toFixed(5)}`; }
    function current() { const c = map.getCenter(); return { lat: c.lat, lng: c.lng }; }

    async function updateLabel() {
      const { lat, lng } = current();
      const locality = nearestLocality(lat, lng);
      if (labelEl) labelEl.textContent = `Selected: ${locality} · ${fmt(lat, lng)}`;
      clearTimeout(geocodeTimer);
      geocodeTimer = setTimeout(async () => {
        const name = await reverseGeocode(lat, lng);
        if (labelEl) labelEl.textContent = `Selected: ${locality} · ${fmt(lat, lng)}`;
        if (addrEl && !addrEl.dataset.userEdited) addrEl.value = name;
      }, 350);
    }

    function syncInputs() {
      const { lat, lng } = current();
      const latIn = document.getElementById(opts.latEl); const lngIn = document.getElementById(opts.lngEl);
      if (latIn) latIn.value = lat.toFixed(6);
      if (lngIn) lngIn.value = lng.toFixed(6);
      updateLabel();
    }
    function refreshList() {
      if (typeof window.hlspRefreshNearbyFromPin === "function") window.hlspRefreshNearbyFromPin();
    }

    map.on("moveend", () => { syncInputs(); refreshList(); });
    map.on("move", updateLabel);
    setTimeout(() => { map.invalidateSize(); updateLabel(); }, 120);

    return {
      getLocation() { const { lat, lng } = current(); return { lat, lng, label: labelEl ? labelEl.textContent : "" }; },
      flyTo(lat, lng, label) { map.flyTo([lat, lng], 15, { duration: 0.6 }); },
      confirm() {
        syncInputs();
        if (typeof opts.onConfirm === "function") opts.onConfirm(this.getLocation());
      }
    };
  }

  function waShare(phone, text) { return `https://wa.me/91${(phone || "").replace(/\D/g, "").slice(-10)}?text=${encodeURIComponent(text)}`; }
  return { DEFAULT, CENTER, LOCALITIES, nearestLocality, reverseGeocode, detect, initRapidoPicker, waShare };
})();
