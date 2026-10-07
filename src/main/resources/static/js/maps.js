// HLSP Maps picker: works without a paid key (OSM), upgrades to Google when key is set.
window.HLSPMaps = (() => {
  const DEFAULT = { lat: 30.3429, lng: 77.9620, city: "Premnagar, Dehradun" }; // Premnagar
  async function reverseGeocode(lat, lng) {
    try {
      const r = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`);
      const j = await r.json();
      return j.display_name || `${lat},${lng}`;
    } catch { return `${lat.toFixed(5)}, ${lng.toFixed(5)}`; }
  }
  function detect() {
    return new Promise((resolve) => {
      if (!navigator.geolocation) return resolve(DEFAULT);
      navigator.geolocation.getCurrentPosition(
        p => resolve({ lat: p.coords.latitude, lng: p.coords.longitude }),
        () => resolve(DEFAULT), { timeout: 5000 });
    });
  }
  function waShare(phone, text) { return `https://wa.me/91${(phone||'').replace(/\D/g,'').slice(-10)}?text=${encodeURIComponent(text)}`; }
  return { DEFAULT, reverseGeocode, detect, waShare };
})();
